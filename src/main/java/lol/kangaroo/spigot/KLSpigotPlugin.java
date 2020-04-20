package lol.kangaroo.spigot;

import java.util.List;
import java.util.Locale;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import lol.kangaroo.common.database.Auth;
import lol.kangaroo.common.database.DatabaseManager;
import lol.kangaroo.common.database.Logs;
import lol.kangaroo.common.database.Setting;
import lol.kangaroo.common.permissions.PermissionManager;
import lol.kangaroo.common.player.PlayerHistory;
import lol.kangaroo.common.player.PlayerVariableManager;
import lol.kangaroo.common.player.punish.PunishManager;
import lol.kangaroo.common.util.I18N;
import lol.kangaroo.common.util.MSG;
import lol.kangaroo.spigot.commands.ChatCommand;
import lol.kangaroo.spigot.commands.CommandExecutor;
import lol.kangaroo.spigot.commands.GamemodeCommand;
import lol.kangaroo.spigot.config.ConfigManager;
import lol.kangaroo.spigot.listeners.ChatListener;
import lol.kangaroo.spigot.listeners.JoinMessageListener;
import lol.kangaroo.spigot.listeners.PermissionListener;
import lol.kangaroo.spigot.permissions.RankManager;
import lol.kangaroo.spigot.player.Money;
import lol.kangaroo.spigot.player.PlayerCacheManager;
import lol.kangaroo.spigot.player.PlayerLevel;
import lol.kangaroo.spigot.player.PlayerManager;
import lol.kangaroo.spigot.util.PluginMessage;

public class KLSpigotPlugin extends JavaPlugin {
	
	public static KLSpigotPlugin instance;
	
	private ConfigManager configManager;

	private DatabaseManager db;
	
	private I18N i18n;
	
	private PlayerVariableManager pvm;
	private PlayerCacheManager pcm;
	private PunishManager pum;
	private PlayerManager pm;
	private PermissionManager prm;
	
	private RankManager rm;
	
	private PermissionListener prl;
	private JoinMessageListener jml;
	private ChatListener chl;
	
	@Override
	public void onEnable() {
		Locale.setDefault(new Locale("en", "US"));
		
		instance = this;
		init();
		setupLanguages();
		registerListeners();
	}
	
	private void init() {
		configManager = new ConfigManager(getDataFolder());
		
		FileConfiguration settings = configManager.getConfig("settings");
		ConfigurationSection dbSettings = settings.getConfigurationSection("db");
		db = new DatabaseManager(dbSettings.getString("user"), dbSettings.getString("pass"), dbSettings.getString("db"), dbSettings.getString("host"), dbSettings.getInt("port"));
		ConfigurationSection cacheSettings = settings.getConfigurationSection("cache-settings");
		Setting.init(db);
		Auth.init(db);
		Logs.init(db);
		
		pvm = new PlayerVariableManager(db);
		pcm = new PlayerCacheManager(this, cacheSettings.getBoolean("lite-mode"), cacheSettings.getLong("update-interval"), cacheSettings.getLong("flush-interval"));
		pum = new PunishManager(db);
		prm = new PermissionManager(db);
		pm = new PlayerManager(db, getServer(), this, pvm, pcm, pum, prm);
		rm = new RankManager(pm);
		PlayerHistory.init(db);
		
		PluginMessage.init(this);
		
		Money.init(pm);
		PlayerLevel.init(pm, configManager);

		CommandExecutor.registerCommand(new ChatCommand(pm, getServer()));
		CommandExecutor.registerCommand(new GamemodeCommand(pm, getServer()));
		
		pcm.scheduleUpdateTasks(pm);
		
		// TODO change to PlayerProfile concept, a PlayerAccess that determines permissions (1 per user, non-changing, non-public) 
		// and PlayerProfile (multiple per user, each with individual name histories, links to nicknaming and stuff. By "multiple" most likely just 2, one real and one nicknamed)
		// 
		// TODO take a reaallll deep look at the server-side cache, then make it only update when told to by bungee, unless *maybe* a fallback scheduled-task.
	}
	
	private void setupLanguages() {
		FileConfiguration settings = configManager.getConfig("settings");
		List<String> langs = settings.getConfigurationSection("languages").getStringList("locales");
		Locale[] locales = new Locale[langs.size()];
		for(int i = 0; i < langs.size(); i++) {
			String[] lc = langs.get(i).split("_");
			locales[i] = new Locale(lc[0], lc[1]);
		}
			
		MSG.init(i18n = new I18N(locales, this.getDataFolder(), this.getClass().getProtectionDomain().getCodeSource().getLocation()));
	}
	
	private void registerListeners() {
		
		PluginManager pluginManager = getServer().getPluginManager();

		pluginManager.registerEvents(prl = new PermissionListener(pm), this);
		pluginManager.registerEvents(jml = new JoinMessageListener(), this);
		pluginManager.registerEvents(chl = new ChatListener(pm), this);
		
	}
	
	
	public ConfigManager getConfigManager() {
		return configManager;
	}
	
	public DatabaseManager getDatabaseManager() {
		return db;
	}

	public PlayerVariableManager getPlayerVariableManager() {
		return pvm;
	}
	
	public PlayerManager getPlayerManager() {
		return pm;
	}
	
	public PlayerCacheManager getPlayerCacheManager() {
		return pcm;
	}
	
	public PunishManager getPunishManager() {
		return pum;
	}
	
	public PermissionManager getPermissionManager() {
		return prm;
	}
	
	public RankManager getRankManager() {
		return rm;
	}
	
	public I18N getI18N() {
		return i18n;
	}

	public PermissionListener getPermissionListener() {
		return prl;
	}

	public JoinMessageListener getJoinMessageListener() {
		return jml;
	}

	public ChatListener getChatListener() {
		return chl;
	}
	
	
}
