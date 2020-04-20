package lol.kangaroo.spigot.listeners;

import java.util.Locale;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import lol.kangaroo.common.util.MSG;
import net.md_5.bungee.api.ChatColor;

public class JoinMessageListener implements Listener {
	
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onJoinMessage(PlayerJoinEvent e) {
		
		if(e.getPlayer() == null || !e.getPlayer().isOnline()) return;
		e.setJoinMessage(MSG.PLAYER_JOINMESSAGE.getMessage(Locale.getDefault(), ChatColor.stripColor(e.getPlayer().getDisplayName())));
		
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onLeaveMessage(PlayerQuitEvent e) {
		
		if(e.getPlayer() == null || !e.getPlayer().isOnline()) return;
		e.setQuitMessage(MSG.PLAYER_LEAVEMESSAGE.getMessage(Locale.getDefault(), ChatColor.stripColor(e.getPlayer().getDisplayName())));
		 
	}
	

}
