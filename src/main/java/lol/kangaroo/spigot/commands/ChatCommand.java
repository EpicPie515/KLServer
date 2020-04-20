package lol.kangaroo.spigot.commands;

import java.util.Arrays;

import org.bukkit.Server;
import org.bukkit.entity.Player;

import lol.kangaroo.common.permissions.Rank;
import lol.kangaroo.common.player.BasePlayer;
import lol.kangaroo.common.util.MSG;
import lol.kangaroo.spigot.commands.chat.ClearSub;
import lol.kangaroo.spigot.player.PlayerManager;
import lol.kangaroo.spigot.util.Message;

public class ChatCommand extends CommandExecutor {

	public ChatCommand(PlayerManager pm, Server server) {
		super(pm, server, "chat", Rank.PLAYER.getPerm(), "chatclear", "clearchat", "ch");
		
		registerSubcommand(new ClearSub(pm, server, this));
	}

	@Override
	public void execute(Player sender, BasePlayer bp, String label, String[] args) {
		
		if(args.length == 0 && !(label.equalsIgnoreCase("clearchat") || label.equalsIgnoreCase("chatclear"))) {
			Message.sendMessage(bp, MSG.PREFIX_ERROR, MSG.COMMAND_CHAT_USAGE);
			return;
		}
		boolean cmdFound = false;
		String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
		
		for(String k : subCommands.keySet()) {
			if(k.equalsIgnoreCase(args[0])) {
				Subcommand s = subCommands.get(k);
				cmdFound = true;
				if(sender.hasPermission(s.getPermission()))
					s.execute(sender, bp, args[0], subArgs);
				else
					Message.sendMessage(bp, MSG.PREFIX_ERROR, MSG.NO_PERM);
				break;
			}
		}
		if(label.equalsIgnoreCase("clearchat") || label.equalsIgnoreCase("chatclear")) {
			Subcommand s = subCommands.get("clear");
			cmdFound = true;
			if(sender.hasPermission(s.getPermission()))
				s.execute(sender, bp, args[0], subArgs);
			else
				Message.sendMessage(bp, MSG.PREFIX_ERROR, MSG.NO_PERM);
		}
		if(!cmdFound) {
			// TODO status
		}
	}

	@Override
	public void executeConsole(String label, String[] args) {
		if(args.length == 0) {
			Message.sendConsole(MSG.PREFIX_ERROR, MSG.COMMAND_CHAT_USAGE);
			return;
		}
		boolean cmdFound = false;
		String[] subArgs = Arrays.copyOfRange(args, 1, args.length);
		for(String k : subCommands.keySet()) {
			if(k.equalsIgnoreCase(args[0])) {
				Subcommand s = subCommands.get(k);
				cmdFound = true;
				s.executeConsole(args[0], subArgs);
				break;
			}
		}
		if(label.equalsIgnoreCase("clearchat") || label.equalsIgnoreCase("chatclear")) {
			Subcommand s = subCommands.get("clear");
			cmdFound = true;
			s.executeConsole(args[0], subArgs);
		}
		if(!cmdFound)
			Message.sendConsole(MSG.PREFIX_ERROR, MSG.UNKNOWN_COMMAND);
	}
	
}
