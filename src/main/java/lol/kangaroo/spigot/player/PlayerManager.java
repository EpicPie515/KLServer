package lol.kangaroo.spigot.player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;

import lol.kangaroo.common.database.DatabaseManager;
import lol.kangaroo.common.permissions.PermissionManager;
import lol.kangaroo.common.permissions.Rank;
import lol.kangaroo.common.player.BasePlayer;
import lol.kangaroo.common.player.CachedPlayer;
import lol.kangaroo.common.player.DatabasePlayer;
import lol.kangaroo.common.player.PlayerVariable;
import lol.kangaroo.common.player.PlayerVariableManager;
import lol.kangaroo.common.player.punish.PunishManager;
import lol.kangaroo.common.util.DoubleObject;
import lol.kangaroo.common.util.MSG;
import lol.kangaroo.common.util.ObjectMutable;
import lol.kangaroo.spigot.KLSpigotPlugin;
import lol.kangaroo.spigot.permissions.RankManager;
import lol.kangaroo.spigot.util.Message;

public class PlayerManager {
	
	private DatabaseManager db;
	
	private Server server;
	private KLSpigotPlugin pl;
	
	private PlayerVariableManager pvm;
	
	private PlayerCacheManager pcm;
	
	private PunishManager pum;
	private PermissionManager prm;
	
	public Set<UUID> unbannedJoining = new HashSet<>();
	
	public Map<UUID, PermissionAttachment> attachments = new HashMap<>();
	
	public PlayerManager(DatabaseManager db, Server server, KLSpigotPlugin pl, PlayerVariableManager pvm, PlayerCacheManager pcm, PunishManager pum, PermissionManager prm) {
		this.db = db;
		this.pl = pl;
		this.server = server;
		this.pvm = pvm;
		this.pcm = pcm;
		this.pum = pum;
		this.prm = prm;
	}
	
	/**
	 * Pass-through getter to lessen the amount of arguments on functions that already have PlayerManager
	 * @return
	 */
	public RankManager getRankManager() {
		return pl.getRankManager();
	}
	
	/**
	 * Pass-through getter to lessen the amount of arguments on functions that already have PlayerManager
	 * @return
	 */
	public PunishManager getPunishManager() {
		return pum;
	}
	
	public PlayerCacheManager getPlayerCacheManager() {
		return pcm;
	}
	
	public PermissionManager getPermissionManager() {
		return prm;
	}
	
	public Set<BasePlayer> convertBukkitPlayers(Collection<Player> players) {
		Set<BasePlayer> converts = new HashSet<>();
		Iterator<Player> it = players.iterator();
		while(it.hasNext()) {
			Player pp = it.next();
			converts.add(getCachedPlayer(pp.getUniqueId()));
		}
		return converts;
	}
	
	/**
	 * Verifies that the UUID is of a player that has joined the server before.
	 * @param uuid UUID to check.
	 * @return whether the player exists.
	 */
	public boolean playerExists(UUID uuid) {
		if(uuid == null) return false;
		
		ObjectMutable<Boolean> b = new ObjectMutable<>(false);
		
		db.query("SELECT `UUID` FROM `users` WHERE `UUID`=?", rs -> {
			try {
				if(rs.next()) b.set(true);
			} catch (SQLException e) {
				e.printStackTrace();
			} 
		}, uuid);
		return b.get();
	}
	
	/**
	 * Gets a DatabasePlayer object which is always directly returning database values, non-cached.
	 * When possible, use {@link #getCachedPlayer(UUID)} instead.
	 * 
	 * Returns null if the given UUID has never joined the server.
	 * 
	 * @param uuid The UUID of the player.
	 * @return a new DatabasePlayer object
	 */
	public DatabasePlayer getDatabasePlayer(UUID uuid) {
		if(uuid == null) return null;
		
		ObjectMutable<Boolean> b = new ObjectMutable<>(false);
		
		db.query("SELECT `UUID` FROM `users` WHERE `UUID`=?", rs -> {
			try {
				if(rs == null) {
					// stacktrace it
					throw new RuntimeException("WTF HOW IS RESULTSET NULL");
				}
				if(rs.next()) b.set(true);
			} catch (SQLException e) {
				e.printStackTrace();
			} 
		}, uuid);
		if(b.get())
			return new DatabasePlayer(uuid, pvm, pum);
		else
			return null;
	}
	
	/**
	 * Gets a CachedPlayer object holding all variables, cached. Only updates based on the set interval.
	 * For an instantly updated, database-linked version use {@link #getDatabasePlayer(UUID)} instead.
	 * 
	 * If there is no cached player for the given UUID, and the player has joined before, one will be created.
	 * 
	 * This will return null if the player has never joined. Use {@link #playerExists(UUID)} to check beforehand.
	 * 
	 * @param uuid The UUID of the player.
	 * @return the desired CachedPlayer object, from the playerCache.
	 */
	public CachedPlayer getCachedPlayer(UUID uuid) {
		if(uuid == null) return null;
		
		if(pcm.isInPlayerCache(uuid)) {
			return pcm.getUUIDCache().get(uuid);
		}
		
		if(!playerExists(uuid)) return null;
		
		// Else, we have to create a new CachedPlayer
		// First by getting a DatabasePlayer, then using that to get the variables.
		
		DatabasePlayer dp = getDatabasePlayer(uuid);
		
		CachedPlayer cp = new CachedPlayer(uuid, dp.getAllVariablesMap(), dp.getPunishments(), dp.getActivePunishments(), pvm, pum);
		pcm.addToPlayerCache(cp);
		
		return cp;
	}
	
	public Set<BasePlayer> getOnlineStaff() {
		Set<BasePlayer> staff = new HashSet<>();
		for(Player pp : server.getOnlinePlayers()) {
			CachedPlayer cp = getCachedPlayer(pp.getUniqueId());
			if(((Rank)cp.getVariable(PlayerVariable.RANK)).isStaff())
				staff.add(cp);
		}
		return staff;
	}
	
	public Set<BasePlayer> getNotifiableStaff() {
		Set<BasePlayer> staff = new HashSet<>();
		for(Player pp : server.getOnlinePlayers()) {
			CachedPlayer cp = getCachedPlayer(pp.getUniqueId());
			if(((Rank)cp.getVariable(PlayerVariable.RANK)).isStaff() && ((Boolean)cp.getVariable(PlayerVariable.ADMIN_ALERT)))
				staff.add(cp);
		}
		return staff;
	}
	
	/**
	 * Gets a UUID of the player only if a player has that as their current exact username.
	 */
	public UUID getFromCurrentExact(String currentExactName) {
		ObjectMutable<UUID> u = new ObjectMutable<UUID>(null);
		
		db.query(
				"SELECT `UUID` FROM `cur_data` WHERE `NAME`=?",
				rs -> {
					try {
						if(rs.next())
							u.set(UUID.fromString(rs.getString(1)));
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}, currentExactName);
		
		return u.get();
	}
	
	/**
	 * Gets a UUID of the player only if a player has that as their current username.
	 */
	public UUID getFromCurrent(String currentName) {
		ObjectMutable<UUID> u = new ObjectMutable<UUID>(null);
		
		db.query(
				"SELECT `UUID` FROM `cur_data` WHERE `NAME` LIKE ?",
				rs -> {
					try {
						if(rs.next())
							u.set(UUID.fromString(rs.getString(1)));
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}, "%" + currentName + "%");
		
		return u.get();
	}
	
	/**
	 * Gets a UUID of the player that has had that username.
	 */
	public UUID getFromPastExact(String exactName) {
		ObjectMutable<UUID> u = new ObjectMutable<UUID>(null);
		
		db.query(
				"SELECT `UUID` FROM `prev_names` WHERE `NAME`=? ORDER BY `INITIAL` DESC",
				rs -> {
					try {
						if(rs.next())
							u.set(UUID.fromString(rs.getString(1)));
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}, exactName);
		
		return u.get();
	}
	
	/**
	 * Gets a UUID of the player that has had that username.
	 */
	public UUID getFromPast(String name) {
		ObjectMutable<UUID> u = new ObjectMutable<UUID>(null);
		
		db.query(
				"SELECT `UUID` FROM `prev_names` WHERE `NAME` LIKE ? ORDER BY `INITIAL` DESC",
				rs -> {
					try {
						if(rs.next())
							u.set(UUID.fromString(rs.getString(1)));
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}, "%" + name + "%");
		
		return u.get();
	}
	
	/**
	 * Gets a UUID of the player only if a player has that as their current exact nickname.
	 */
	public UUID getFromCurrentExactNick(String currentExactNick) {
		ObjectMutable<UUID> u = new ObjectMutable<UUID>(null);
		
		db.query(
				"SELECT `UUID` FROM `player_data` WHERE `TYPE`='nickname' AND `VALUE`=?",
				rs -> {
					try {
						if(rs.next())
							u.set(UUID.fromString(rs.getString(1)));
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}, currentExactNick);
		
		return u.get();
	}
	
	/**
	 * Gets a UUID of the player only if a player has that as their current nickname.
	 */
	public UUID getFromCurrentNick(String currentNick) {
		ObjectMutable<UUID> u = new ObjectMutable<UUID>(null);
		
		db.query(
				"SELECT `UUID` FROM `player_data` WHERE `TYPE`='nickname' AND `VALUE` LIKE ?",
				rs -> {
					try {
						if(rs.next())
							u.set(UUID.fromString(rs.getString(1)));
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}, "%" + currentNick + "%");
		
		return u.get();
	}
	
	/**
	 * Gets a UUID of the player only if a player has had that exact nickname.
	 */
	public UUID getFromPastExactNick(String exactNick) {
		ObjectMutable<UUID> u = new ObjectMutable<UUID>(null);
		
		db.query(
				"SELECT `UUID` FROM `prev_nicknames` WHERE `NICKNAME`=? ORDER BY `INITIAL` DESC",
				rs -> {
					try {
						if(rs.next())
							u.set(UUID.fromString(rs.getString(1)));
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}, exactNick);
		
		return u.get();
	}
	
	/**
	 * Gets a UUID of the player only if a player has had that nickname.
	 */
	public UUID getFromPastNick(String nick) {
		ObjectMutable<UUID> u = new ObjectMutable<UUID>(null);
		
		db.query(
				"SELECT `UUID` FROM `prev_nicknames` WHERE `NICKNAME` LIKE ? ORDER BY `INITIAL` DESC",
				rs -> {
					try {
						if(rs.next())
							u.set(UUID.fromString(rs.getString(1)));
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}, "%" + nick + "%");
		
		return u.get();
	}
	
	/**
	 * Combines {@link #getFromPast(String)} and {@link #getFromPastNick(String)}.
	 * Includes Past and Current (Because all current names are also stored in the name history table).
	 */
	public UUID getFromAny(String name) {
		ObjectMutable<UUID> nu = new ObjectMutable<UUID>(null);
		ObjectMutable<UUID> u = new ObjectMutable<UUID>(null);
		
		Map<DoubleObject<String, Object[]>, Consumer<ResultSet>> queries = new HashMap<>();
		
		queries.put(new DoubleObject<>("SELECT `UUID` FROM `prev_names` WHERE `NAME` LIKE ? ORDER BY `INITIAL` DESC", new Object[] {"%" + name + "%"}), rs -> {
			try {
				if(rs.next())
					u.set(UUID.fromString(rs.getString(1)));
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
		queries.put(new DoubleObject<>("SELECT `UUID` FROM `prev_nicknames` WHERE `NICKNAME` LIKE ? ORDER BY `INITIAL` DESC", new Object[] {"%" + name + "%"}), rs -> {
			try {
				if(rs.next())
					nu.set(UUID.fromString(rs.getString(1)));
			} catch (SQLException e) {
				e.printStackTrace();
			}
		});
		db.multiQuery(queries);
		
		if(u.get() != null)
			return u.get();
		return nu.get();
		
	}
	
	/**
	 * Set ALL permissions that should be set for that player.
	 */
	public void setJoinedPlayerPermissions(BasePlayer bp) {
		Map<String, Boolean> perms = prm.getAllPermissions(bp);
		Player pp = server.getPlayer(bp.getUniqueId());
		if(pp != null) {
			PermissionAttachment pa = pp.addAttachment(pl);
			for(Entry<String, Boolean> perm : perms.entrySet())
				pa.setPermission(perm.getKey(), perm.getValue());
			attachments.put(pp.getUniqueId(), pa);
		}
	}
	
	/**
	 * method server-side to reflect proxy-side expiration check, should be executed by plugin-message.
	 */
	public void removeExpiredPermissions(BasePlayer bp, Set<String> exp) {
		Player pp = server.getPlayer(bp.getUniqueId());
		if(pp != null && attachments.containsKey(pp.getUniqueId())) {
			PermissionAttachment pa = attachments.get(pp.getUniqueId());
			for(String perm : exp) {
				pa.unsetPermission(perm);
				Message.sendMessage(bp, MSG.PLAYER_REMOVEDPERM, perm.toUpperCase());
			}
		}
	}
	
	
}
