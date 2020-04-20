package lol.kangaroo.spigot.commands;

import java.util.Arrays;
import java.util.List;

import org.bukkit.Server;
import org.bukkit.entity.Player;

import lol.kangaroo.common.player.BasePlayer;
import lol.kangaroo.spigot.player.PlayerManager;

public abstract class Subcommand {
	
	protected PlayerManager pm;
	protected Server server;
	protected String label;
	protected String perm;
	protected CommandExecutor parent;
	protected List<String> aliases;
	
	public Subcommand(PlayerManager pm, Server server, CommandExecutor parent, String label, String perm, String... aliases) {
		this.pm = pm;
		this.server = server;
		this.label = label;
		this.perm = perm;
		this.parent = parent;
		this.aliases = Arrays.asList(aliases);
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
}
