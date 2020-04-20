package lol.kangaroo.spigot.commands;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;

import lol.kangaroo.common.player.BasePlayer;
import lol.kangaroo.common.player.CachedPlayer;
import lol.kangaroo.common.util.MSG;
import lol.kangaroo.spigot.KLSpigotPlugin;
import lol.kangaroo.spigot.player.PlayerManager;
import lol.kangaroo.spigot.util.Message;

public abstract class CommandExecutor {
	

	protected Map<String, Subcommand> subCommands = new HashMap<>();
	
	protected PlayerManager pm;
	protected Server server;
	protected String label;
	protected String perm;
	protected List<String> aliases;
	
	public CommandExecutor(PlayerManager pm, Server server, String label, String perm, String... aliases) {
		this.pm = pm;
		this.server = server;
		this.label = label;
		this.perm = perm;
		this.aliases = Arrays.asList(aliases);
	}
	
	protected void registerSubcommand(Subcommand sub) {
		subCommands.put(sub.getLabel(), sub);
		for(String al : sub.getAliases())
			subCommands.put(al, sub);
	}
	
	public String getLabel() {
		return label;
	}
	
	public String getPermission() {
		return perm;
	}
	
	public List<String> getAliases() {
		return aliases;
	}
	
	/**
	 * @param sender the ProxiedPlayer Sender.
	 * @param bp the BasePlayer sender.
	 */
	public abstract void execute(Player sender, BasePlayer bp, String label, String[] args);
	
	public abstract void executeConsole(String label, String[] args);
	
	private static Set<CommandExecutor> commands = new HashSet<>();
	
	public static void registerCommand(CommandExecutor ce) {
		commands.add(ce);
		Command labelCommand = new Command(ce.getLabel(), "", "", ce.getAliases()) {
			@Override
			public boolean execute(CommandSender sender, String commandLabel, String[] args) {
				return CommandExecutor.execute(ce, sender, commandLabel, args);
			}
		};
		try {
			SimpleCommandMap cMap = null;
			Class<? extends Object> craftServer = KLSpigotPlugin.instance.getServer().getClass();
			Field commandMapField = craftServer.getDeclaredField("commandMap");
			commandMapField.setAccessible(true);
			cMap = (SimpleCommandMap) commandMapField.get(KLSpigotPlugin.instance.getServer());
			boolean s = cMap.register("klol", labelCommand);
			if(!s) KLSpigotPlugin.instance.getServer().getLogger().warning("Command registered non-unique: " + ce.getLabel());
		} catch (SecurityException | IllegalArgumentException | NoSuchFieldException | IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	public static boolean execute(CommandExecutor c, CommandSender cs, String label, String[] args) {
		if(cs instanceof Player) {
			Player pp = (Player) cs;
			CachedPlayer cp = KLSpigotPlugin.instance.getPlayerManager().getCachedPlayer(pp.getUniqueId());
			if(c.getLabel().equalsIgnoreCase(label)) {
				if(pp.hasPermission(c.getPermission())) {
					c.execute(pp, cp, label, args);
					return true;
				} else {
					Message.sendMessage(cp, MSG.PREFIX_ERROR, MSG.NO_PERM);
					return false;
				}
			} else
				for(String s : c.getAliases())
					if(s.equalsIgnoreCase(label)) {
						if(pp.hasPermission(c.getPermission())) {
							c.execute(pp, cp, label, args);
							return true;
						} else {
							Message.sendMessage(cp, MSG.PREFIX_ERROR, MSG.NO_PERM);
							return false;
						}
					}
			return false;
		}
		if(c.getLabel().equalsIgnoreCase(label)) {
			c.executeConsole(label, args);
			return true;
		} else
			for(String s : c.getAliases())
				if(s.equalsIgnoreCase(label)) {
					c.executeConsole(label, args);
					return true;
				}
		return false;
	}
}
