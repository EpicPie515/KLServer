package lol.kangaroo.spigot.util;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteStreams;

import lol.kangaroo.common.player.CachedPlayer;
import lol.kangaroo.common.util.MessageWrapper;
import lol.kangaroo.spigot.KLSpigotPlugin;
import lol.kangaroo.spigot.player.PlayerManager;

public class PluginMessage implements PluginMessageListener {
	
	private static KLSpigotPlugin plugin;
	private static Server ps;
	private static PlayerManager pm;
	
	public static void init(KLSpigotPlugin pl) {
		plugin = pl;
		ps = plugin.getServer();
		pm = plugin.getPlayerManager();
		ps.getMessenger().registerOutgoingPluginChannel(plugin, "BungeeCord");
		ps.getMessenger().registerOutgoingPluginChannel(plugin, "CommandAction");
		ps.getMessenger().registerOutgoingPluginChannel(plugin, "AdminAlert");
		PluginMessage plm = new PluginMessage();
		ps.getMessenger().registerIncomingPluginChannel(plugin, "CommandGUI", plm);
		ps.getMessenger().registerIncomingPluginChannel(plugin, "BungeeCord", plm);
	}
	
	public static void sendToProxy(Player p, String channel, MessageWrapper m) {
		if(!ps.getMessenger().isOutgoingChannelRegistered(plugin, channel)) ps.getMessenger().registerOutgoingPluginChannel(plugin, channel);
		p.sendPluginMessage(plugin, channel, m.b.toByteArray());
		m.close();
	}

	public static void sendToProxy(String channel, MessageWrapper m) {
		if(!ps.getMessenger().isOutgoingChannelRegistered(plugin, channel)) ps.getMessenger().registerOutgoingPluginChannel(plugin, channel);
		// TODO if this doesnt work use a random player
		ps.sendPluginMessage(plugin, channel, m.b.toByteArray());
		m.close();
	}

	@Override
	public void onPluginMessageReceived(String channel, Player p, byte[] message) {
		CachedPlayer cp = pm.getCachedPlayer(p.getUniqueId());
		if(channel.equals("CommandGUI")) {
			ByteArrayDataInput in = ByteStreams.newDataInput(message);
			String subchannel = in.readUTF();
			String argStr = in.readUTF();
			// TODO not use gui because tetrahedron will
			if(subchannel.equals("Ban")) {
				String[] args = argStr.split(" ");
				if(args.length > 0) {
					
				}
			}
		} else if(channel.equals("PermissionCheck")) {
			ps.getScheduler().runTaskAsynchronously(plugin, () -> {
				pm.removeExpiredPermissions(cp, plugin.getPermissionManager().getExpiredPlayerPermissions(cp));
			});
		}
	}
}
