package com.thegameratort.mammamia;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;

public class ConfigFile extends YamlConfiguration {
    private final MammaMia plugin;
    private final File configFile;

    private ConfigFile(MammaMia plugin, File configFile) {
        this.plugin = plugin;
        this.configFile = configFile;
    }

    public static ConfigFile loadConfig(MammaMia plugin, String fileName) {
        File configFile = new File(plugin.getDataFolder(), fileName);
        if (!configFile.exists()) {
            plugin.saveResource(fileName, false);
        }

        ConfigFile iConfigFile = new ConfigFile(plugin, configFile);
        try {
            iConfigFile.load(configFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
        return iConfigFile;
    }

    public void saveConfig() {
        try {
            this.save(this.configFile);
        } catch (IOException ex) {
            this.plugin.getLogger().log(Level.SEVERE, "Could not save config to " + this.configFile, ex);
        }
    }
}
