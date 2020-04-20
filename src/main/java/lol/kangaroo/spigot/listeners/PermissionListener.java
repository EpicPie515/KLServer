package lol.kangaroo.spigot.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import lol.kangaroo.common.player.CachedPlayer;
import lol.kangaroo.common.player.PlayerVariable;
import lol.kangaroo.spigot.player.PlayerManager;
import lol.kangaroo.spigot.util.ThreadManager;

public class PermissionListener implements Listener {
	
	private PlayerManager pm;
	
	public PermissionListener(PlayerManager pm) {
		this.pm = pm;
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		if(p == null || !p.isOnline()) return;
		ThreadManager.async(() -> {
			CachedPlayer cp = pm.getCachedPlayer(e.getPlayer().getUniqueId());
			if(cp == null) return;
			pm.setJoinedPlayerPermissions(cp);
			String displayName = pm.getRankManager().getRank(cp, true).getColor() + ((String)cp.getVariable(PlayerVariable.NICKNAME));
			ThreadManager.sync(() -> {
				p.setDisplayName(displayName);
			});
		});
	}
}
