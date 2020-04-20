package lol.kangaroo.spigot.util;

import lol.kangaroo.spigot.KLSpigotPlugin;

public class ThreadManager {
	
	public static void async(Runnable run) {
		KLSpigotPlugin pl = KLSpigotPlugin.instance;
		pl.getServer().getScheduler().runTaskAsynchronously(pl, run);
	}
	
	public static void sync(Runnable run) {
		KLSpigotPlugin pl = KLSpigotPlugin.instance;
		pl.getServer().getScheduler().runTask(pl, run);
	}
	
}
