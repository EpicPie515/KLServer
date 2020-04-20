package lol.kangaroo.spigot.config;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class ConfigManager {
	
	private File dataFolder;
	
	private Map<String, File> configFiles = new HashMap<>();
	
	public ConfigManager(File dataFolder) {
		this.dataFolder = dataFolder;
	}
	
	public FileConfiguration getConfig(String name) {
		if(configFiles.containsKey(name)) {
			return YamlConfiguration.loadConfiguration(configFiles.get(name));
		} else {
			File f = new File(dataFolder, name + ".yml");
			configFiles.put(name, f);
			return YamlConfiguration.loadConfiguration(f);
		}
	}
	
}
