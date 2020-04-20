package lol.kangaroo.spigot.commands;

import java.util.Locale;
import java.util.regex.Pattern;

import org.bukkit.GameMode;
import org.bukkit.Server;
import org.bukkit.entity.Player;

import lol.kangaroo.common.permissions.Rank;
import lol.kangaroo.common.player.BasePlayer;
import lol.kangaroo.common.player.CachedPlayer;
import lol.kangaroo.common.player.PlayerVariable;
import lol.kangaroo.common.util.MSG;
import lol.kangaroo.spigot.player.PlayerManager;
import lol.kangaroo.spigot.util.Message;

public class GamemodeCommand extends CommandExecutor {

	public GamemodeCommand(PlayerManager pm, Server server) {
		super(pm, server, "gamemode", Rank.SRMOD.getPerm(), "gm", "gmc", "gms", "sgamemode", "sgmc", "sgms", "sgm");
	}

	private static final Pattern CREATIVE_PATTERN = Pattern.compile("^c.*|1$");
	private static final Pattern SURVIVAL_PATTERN = Pattern.compile("^s.*|0$");
	private static final Pattern ADVENTURE_PATTERN = Pattern.compile("^a.*|2$");

	@Override
	public void execute(Player sender, BasePlayer bp, String label, String[] args) {
		int mode = -1;
		boolean arg1islabel = true;
		if(label.equalsIgnoreCase("gmc") || label.equalsIgnoreCase("sgmc")) {
			mode = 1;
		} else if(label.equalsIgnoreCase("gms") || label.equalsIgnoreCase("sgms")) {
			mode = 0;
		} else if(args.length == 0) {
			Message.sendMessage(bp, MSG.PREFIX_ERROR, MSG.COMMAND_GAMEMODE_USAGE);
			return;
		} else {
			arg1islabel = false;
			String m = args[0].toLowerCase();
			if(CREATIVE_PATTERN.matcher(m).matches()) {
				mode = 1;
			} else if(SURVIVAL_PATTERN.matcher(m).matches()) {
				mode = 0;
			} else if(ADVENTURE_PATTERN.matcher(m).matches()) {
				mode = 2;
			} else {
				Message.sendMessage(bp, MSG.PREFIX_ERROR, MSG.COMMAND_GAMEMODE_USAGE);
				return;
			}
		}
		int ind = arg1islabel ? 0 : 1;
		if(args.length >= ind+1) {
			String t = args[ind];
			CachedPlayer target = pm.getCachedPlayer(pm.getFromCurrent(t));
			if(target == null) {
				Message.sendMessage(bp, MSG.PREFIX_ERROR, MSG.PLAYER_NOTFOUND);
				return;
			}
			Player tp = server.getPlayer(target.getUniqueId());
			if(tp == null) {
				Message.sendMessage(bp, MSG.PREFIX_ERROR, MSG.PLAYER_OFFLINE);
				return;
			}
			GameMode gm = GameMode.SURVIVAL;
			String bpmodeStr = MSG.COMMAND_GAMEMODE_SURVIVAL.getMessage(bp);
			String targetModeStr = MSG.COMMAND_GAMEMODE_SURVIVAL.getMessage(target);
			if(mode == 1) {
				gm = GameMode.CREATIVE;
				bpmodeStr = MSG.COMMAND_GAMEMODE_CREATIVE.getMessage(bp);
				targetModeStr = MSG.COMMAND_GAMEMODE_CREATIVE.getMessage(target);
			} else if(mode == 2) {
				gm = GameMode.ADVENTURE;
				bpmodeStr = MSG.COMMAND_GAMEMODE_ADVENTURE.getMessage(bp);
				targetModeStr = MSG.COMMAND_GAMEMODE_ADVENTURE.getMessage(target);
			}
			tp.setGameMode(gm);
			boolean silent = label.toLowerCase().startsWith("s");
			Message.sendMessage(bp, MSG.PREFIX_PLAYER, MSG.COMMAND_GAMEMODE_SWITCH_OTHER, pm.getRankManager().getPrefix(target, true) + target.getVariable(PlayerVariable.NICKNAME), bpmodeStr, silent ? MSG.ADMIN_SILENT.getMessage(bp) : "");
			if(!silent)
				Message.sendMessage(target, MSG.PREFIX_PLAYER, MSG.COMMAND_GAMEMODE_SWITCH, targetModeStr);
		} else {
			GameMode gm = GameMode.SURVIVAL;
			String bpmodeStr = MSG.COMMAND_GAMEMODE_SURVIVAL.getMessage(bp);
			if(mode == 1) {
				gm = GameMode.CREATIVE;
				bpmodeStr = MSG.COMMAND_GAMEMODE_CREATIVE.getMessage(bp);
			} else if(mode == 2) {
				gm = GameMode.ADVENTURE;
				bpmodeStr = MSG.COMMAND_GAMEMODE_ADVENTURE.getMessage(bp);
			}
			sender.setGameMode(gm);
			Message.sendMessage(bp, MSG.PREFIX_PLAYER, MSG.COMMAND_GAMEMODE_SWITCH, bpmodeStr);
		}
	}

	@Override
	public void executeConsole(String label, String[] args) {
		int mode = -1;
		boolean arg1islabel = true;
		if(label.equalsIgnoreCase("gmc") || label.equalsIgnoreCase("sgmc")) {
			mode = 1;
		} else if(label.equalsIgnoreCase("gms") || label.equalsIgnoreCase("sgms")) {
			mode = 0;
		} else if(args.length == 0) {
			Message.sendConsole(MSG.PREFIX_ERROR, MSG.COMMAND_GAMEMODE_USAGE);
			return;
		} else {
			arg1islabel = false;
			String m = args[0].toLowerCase();
			if(CREATIVE_PATTERN.matcher(m).matches()) {
				mode = 1;
			} else if(SURVIVAL_PATTERN.matcher(m).matches()) {
				mode = 0;
			} else if(ADVENTURE_PATTERN.matcher(m).matches()) {
				mode = 2;
			} else {
				Message.sendConsole(MSG.PREFIX_ERROR, MSG.COMMAND_GAMEMODE_USAGE);
				return;
			}
		}
		int ind = arg1islabel ? 0 : 1;
		if(args.length >= ind+1) {
			String t = args[ind];
			CachedPlayer target = pm.getCachedPlayer(pm.getFromCurrent(t));
			if(target == null) {
				Message.sendConsole(MSG.PREFIX_ERROR, MSG.PLAYER_NOTFOUND);
				return;
			}
			Player tp = server.getPlayer(target.getUniqueId());
			if(tp == null) {
				Message.sendConsole(MSG.PREFIX_ERROR, MSG.PLAYER_OFFLINE);
				return;
			}
			GameMode gm = GameMode.SURVIVAL;
			String bpmodeStr = MSG.COMMAND_GAMEMODE_SURVIVAL.getMessage(Locale.getDefault());
			String targetModeStr = MSG.COMMAND_GAMEMODE_SURVIVAL.getMessage(target);
			if(mode == 1) {
				gm = GameMode.CREATIVE;
				bpmodeStr = MSG.COMMAND_GAMEMODE_CREATIVE.getMessage(Locale.getDefault());
				targetModeStr = MSG.COMMAND_GAMEMODE_CREATIVE.getMessage(target);
			} else if(mode == 2) {
				gm = GameMode.ADVENTURE;
				bpmodeStr = MSG.COMMAND_GAMEMODE_ADVENTURE.getMessage(Locale.getDefault());
				targetModeStr = MSG.COMMAND_GAMEMODE_ADVENTURE.getMessage(target);
			}
			tp.setGameMode(gm);
			boolean silent = label.toLowerCase().startsWith("s");
			Message.sendConsole(MSG.PREFIX_PLAYER, MSG.COMMAND_GAMEMODE_SWITCH_OTHER, pm.getRankManager().getPrefix(target, true) + target.getVariable(PlayerVariable.NICKNAME), bpmodeStr, silent ? MSG.ADMIN_SILENT.getMessage(Locale.getDefault()) : "");
			if(!silent)
				Message.sendMessage(target, MSG.PREFIX_PLAYER, MSG.COMMAND_GAMEMODE_SWITCH, targetModeStr);
		} else {
			Message.sendConsole(MSG.PREFIX_ERROR, MSG.COMMAND_GAMEMODE_USAGE);
			return;
		}
	}
	
}
