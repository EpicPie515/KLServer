package lol.kangaroo.spigot.commands.chat;

import java.util.UUID;

import org.bukkit.Server;
import org.bukkit.entity.Player;

import lol.kangaroo.common.permissions.Rank;
import lol.kangaroo.common.player.BasePlayer;
import lol.kangaroo.common.player.CachedPlayer;
import lol.kangaroo.common.player.PlayerVariable;
import lol.kangaroo.common.util.MSG;
import lol.kangaroo.common.util.MessageWrapper;
import lol.kangaroo.spigot.commands.CommandExecutor;
import lol.kangaroo.spigot.commands.Subcommand;
import lol.kangaroo.spigot.player.PlayerManager;
import lol.kangaroo.spigot.util.Message;
import lol.kangaroo.spigot.util.PluginMessage;

public class ClearSub extends Subcommand {

	public ClearSub(PlayerManager pm, Server server, CommandExecutor parent) {
		super(pm, server, parent, "clear", Rank.JRMOD.getPerm());
	}

	@Override
	public void execute(Player sender, BasePlayer bp, String label, String[] args) {
		if(args.length > 0) {
			UUID tu = pm.getFromCurrent(args[0]);
			CachedPlayer target = pm.getCachedPlayer(tu);
			if(tu == null || target == null) {
				Message.sendMessage(bp, MSG.PREFIX_ERROR, MSG.PLAYER_NOTFOUND);
				return;
			}
			Player tp = server.getPlayer(tu);
			if(tp == null) {
				Message.sendMessage(bp, MSG.PREFIX_ERROR, MSG.PLAYER_NOTFOUND);
				return;
			}
			for(int i = 0; i < 100; i++) {
				tp.sendMessage(" ");
				tp.sendMessage("   ");
			}
			Message.sendMessage(bp, MSG.COMMAND_CHAT_CLEAR_OTHER, pm.getRankManager().getPrefix(target, true) + target.getVariable(PlayerVariable.NICKNAME));
			return;
		}
		for(int i = 0; i < 100; i++) {
			server.broadcastMessage(" ");
			server.broadcastMessage("   ");
		}
		Message.sendMessage(bp, MSG.COMMAND_CHAT_CLEAR);
		PluginMessage.sendToProxy(sender, "AdminAlert", new MessageWrapper("ChatClear"));
	}

	@Override
	public void executeConsole(String label, String[] args) {
		for(int i = 0; i < 100; i++) {
			server.broadcastMessage(" ");
			server.broadcastMessage("   ");
		}
		Message.sendConsole(MSG.COMMAND_CHAT_CLEAR);
	}

	
	
}
