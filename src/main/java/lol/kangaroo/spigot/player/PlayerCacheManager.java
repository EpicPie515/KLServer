package lol.kangaroo.spigot.player;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bukkit.entity.Player;

import lol.kangaroo.common.player.CachedPlayer;
import lol.kangaroo.common.player.DatabasePlayer;
import lol.kangaroo.common.player.PlayerVariable;
import lol.kangaroo.spigot.KLSpigotPlugin;

public class PlayerCacheManager {
	
	private KLSpigotPlugin pl;
	
	private PlayerManager pm;
	
	
	private final boolean liteMode;
	private final long pullUpdateInterval;
	private final long flushInterval;
	
	private Map<UUID, CachedPlayer> uuidCache = new HashMap<>();

	/** If this server is in lite mode, the cache will only update on player join/leave, intended for games that don't last long, restart the server, or require high performance. 
	* Even in lite mode the cache will flush still.
	* */
	public PlayerCacheManager(KLSpigotPlugin pl, boolean liteMode, long pullUpdateInterval, long flushInterval) {
		this.pl = pl;
		this.liteMode = liteMode;
		// Converted from millis to ticks
		this.pullUpdateInterval = pullUpdateInterval / 50L;
		this.flushInterval = flushInterval / 50L;
	}

	public void scheduleUpdateTasks(PlayerManager pm) {
		this.pm = pm;
		if(!liteMode) {
			pl.getServer().getScheduler().runTaskTimerAsynchronously(pl, () -> {
				pullCacheUpdate();
			}, 100L, pullUpdateInterval);
		}
		pl.getServer().getScheduler().runTaskTimerAsynchronously(pl, () -> {
			flushCacheUpdate();
		}, 100L, flushInterval);
	}
	
	public void updatePlayer(CachedPlayer cp) {
		// Cached variables to use later.
		Map<PlayerVariable, Object> map = cp.getAllVariablesMap();
		// Gets a DatabasePlayer for comparison.
		DatabasePlayer dbpl = pm.getDatabasePlayer(cp.getUniqueId());
		// Getting all DB Variables in one connection.
		Map<PlayerVariable, Object> dbMap = dbpl.getAllVariablesMap();
		
		// for any variables in the db, check if they are different from cache. If so, update them. 
		// (used to be checking the cached for updates in db, however, logically, it should be the current way instead,
		// as if something isnt in db then its not gonna get updated anyway, but if something isnt in cache it needs to be added.)
		for(PlayerVariable pv : dbMap.keySet()) {
			if(!map.get(pv).equals(dbMap.get(pv))) {
				map.put(pv, dbMap.get(pv));
			}
		}
		
		cp.setAllVariablesMap(map);
	}
	
	public void pullCacheUpdate() {
		for(CachedPlayer cp : uuidCache.values()) {
			updatePlayer(cp);
		}
	}

	public void flushCacheUpdate() {
		Set<CachedPlayer> tr = new HashSet<>();
		for(CachedPlayer cp : uuidCache.values()) {
			Player pp = pl.getServer().getPlayer(cp.getUniqueId());
			if(pp == null || !pp.isOnline()) {
				tr.add(cp);
			}
		}
		for(CachedPlayer cp : tr) removeFromPlayerCache(cp);
		pm.getPunishManager().flushPunishmentCache();
		System.gc();
	}
	
	public Set<CachedPlayer> getPlayerCache() {
		return new HashSet<>(uuidCache.values());
	}
	
	public Map<UUID, CachedPlayer> getUUIDCache() {
		return uuidCache;
	}
	
	public boolean isInPlayerCache(CachedPlayer cp) {
		return uuidCache.containsKey(cp.getUniqueId());
	}
	
	public boolean isInPlayerCache(UUID uuid) {
		return uuidCache.containsKey(uuid);
	}
	
	public void addToPlayerCache(CachedPlayer cp) {
		uuidCache.put(cp.getUniqueId(), cp);
	}
	
	public void removeFromPlayerCache(CachedPlayer cp) {
		uuidCache.remove(cp.getUniqueId());
	}
	
	/**
	 * Removes @param oldPlayer, adds @param newPlayer.
	 * 
	 * Does not maintain same index, but its a @Set anyway so any index shouldn't be used.
	 */
	public void replaceInPlayerCache(CachedPlayer oldPlayer, CachedPlayer newPlayer) {
		removeFromPlayerCache(oldPlayer);
		addToPlayerCache(newPlayer);
	}
	
}
