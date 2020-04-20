package lol.kangaroo.spigot.listeners;

import java.util.Locale;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import lol.kangaroo.common.permissions.Rank;
import lol.kangaroo.common.player.CachedPlayer;
import lol.kangaroo.common.player.PlayerVariable;
import lol.kangaroo.common.util.MSG;
import lol.kangaroo.spigot.player.PlayerLevel;
import lol.kangaroo.spigot.player.PlayerManager;

public class ChatListener implements Listener {
	
	private PlayerManager pm;
	
	public ChatListener(PlayerManager pm) {
		this.pm = pm;
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onChat(AsyncPlayerChatEvent e) {
		Player p = e.getPlayer();
		if(p == null || !p.isOnline()) {
			e.setCancelled(true);
			// Most likely TPS lag, player disconnects. this might not even ever happen, just a guard clause in case it does.
			return;
		}
		CachedPlayer cp = pm.getCachedPlayer(p.getUniqueId());
		if(cp == null) {
			e.setFormat(MSG.CHAT_FORMAT.getMessage(Locale.getDefault(), "", "%s", "%s"));
			return;
		}
		String dn = pm.getRankManager().getPrefix(cp, true) + ((String)cp.getVariable(PlayerVariable.NICKNAME));
		Rank r = pm.getRankManager().getRank(cp, true);
		if(r.isSrStaff()) e.setMessage(MSG.color(e.getMessage()));
		int level = PlayerLevel.getPlayerLevel(cp, true);
		PlayerLevel l = PlayerLevel.getLevel(level);
		e.setFormat(MSG.CHAT_FORMAT.getMessage(Locale.getDefault(), l.getFormatted(level, true), dn, "%2$s"));
	}

}
