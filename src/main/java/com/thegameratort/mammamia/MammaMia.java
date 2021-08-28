package com.thegameratort.mammamia;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.multiverseinventories.MultiverseInventories;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class MammaMia extends JavaPlugin
{
    private MultiverseCore mvCore = null;
    private MultiverseInventories mvInv = null;
    private ManhuntManager mhMgr = null;
    private DiscordManager discordMgr = null;

    public MammaMia() {}

    public void onEnable()
    {
        PluginManager pluginManager = this.getServer().getPluginManager();

        this.mvCore = (MultiverseCore)pluginManager.getPlugin("Multiverse-Core");
        if (this.mvCore == null)
        {
            this.getLogger().severe("Multiverse-Core not found, disabling...");
            pluginManager.disablePlugin(this);
            return;
        }

        this.mvInv = (MultiverseInventories)pluginManager.getPlugin("Multiverse-Inventories");
        if (this.mvInv == null)
        {
            this.getLogger().severe("Multiverse-Inventories not found, disabling...");
            pluginManager.disablePlugin(this);
            return;
        }

        new LabyModListener(this);

        this.saveDefaultConfig();
        this.discordMgr = new DiscordManager(this);
        this.mhMgr = new ManhuntManager(this);
    }

    public void onDisable()
    {
        this.discordMgr.shutdown();
    }

    public MultiverseCore getMvCore()
    {
        return this.mvCore;
    }

    public MultiverseInventories getMvInv()
    {
        return this.mvInv;
    }

    public void setMhMgr(ManhuntManager mhMgr)
    {
        this.mhMgr = mhMgr;
    }

    public ManhuntManager getMhMgr()
    {
        return this.mhMgr;
    }

    public DiscordManager getDiscordMgr()
    {
        return this.discordMgr;
    }
}
