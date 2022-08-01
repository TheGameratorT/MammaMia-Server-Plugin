package com.thegameratort.mammamia;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.thegameratort.mammamia.discord.DiscordManager;
import com.thegameratort.mammamia.gui.GUIListener;
import com.thegameratort.mammamia.kit.Kit;
import com.thegameratort.mammamia.kit.KitItem;
import com.thegameratort.mammamia.kit.KitManager;
import com.thegameratort.mammamia.manhunt.ManhuntManager;
import com.thegameratort.mammamia.pvp.PvPArenaYML;
import com.thegameratort.mammamia.pvp.PvPManager;
import com.thegameratort.mammamia.track.TrackManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class MammaMia extends JavaPlugin {
    private MultiverseCore mvCore = null;
    private MultiverseInventories mvInv = null;
    private ManhuntManager mhMgr = null;
    private PvPManager pvpMgr = null;
    private KitManager kitMgr = null;
    private TrackManager trackMgr = null;
    private DiscordManager discordMgr = null;
    private ProtocolManager protocolMgr = null;

    public MammaMia() {}

    public void onEnable() {
        PluginManager pluginManager = this.getServer().getPluginManager();

        this.mvCore = (MultiverseCore) pluginManager.getPlugin("Multiverse-Core");
        if (this.mvCore == null) {
            this.getLogger().severe("Multiverse-Core not found, disabling...");
            pluginManager.disablePlugin(this);
            return;
        }

        this.mvInv = (MultiverseInventories) pluginManager.getPlugin("Multiverse-Inventories");
        if (this.mvInv == null) {
            this.getLogger().severe("Multiverse-Inventories not found, disabling...");
            pluginManager.disablePlugin(this);
            return;
        }

        if (!pluginManager.isPluginEnabled("ProtocolLib")) {
            this.getLogger().severe("ProtocolLib not found, disabling...");
            pluginManager.disablePlugin(this);
            return;
        }
        this.protocolMgr = ProtocolLibrary.getProtocolManager();

        ConfigurationSerialization.registerClass(PvPArenaYML.class, "MM_PvPArena");
        ConfigurationSerialization.registerClass(Kit.class, "MM_Kit");
        ConfigurationSerialization.registerClass(KitItem.class, "MM_KitItem");

        this.saveDefaultConfig();

        //new LabyModListener(this);

        this.trackMgr = new TrackManager(this);
        this.discordMgr = new DiscordManager(this);
        new GUIListener(this);
        new MammaMiaCommand(this);

        this.mhMgr = new ManhuntManager(this);
        this.pvpMgr = new PvPManager(this);
        this.kitMgr = new KitManager(this);

        // Do not broadcast achievements that spectators get
        this.protocolMgr.addPacketListener(new PacketAdapter(
            this,
            ListenerPriority.NORMAL,
            PacketType.Play.Server.SYSTEM_CHAT
        ) {
            @Override
            public void onPacketSending(PacketEvent event) {
                String chatData = event.getPacket().getStrings().read(0);
                if (chatData != null)
                {
                    // If it is an advancement message packet
                    if (chatData.startsWith("{\"translate\":\"chat.type.advancement.task\""))
                    {
                        int pos1 = chatData.indexOf("insertion") + 12;
                        int pos2 = chatData.indexOf('\"', pos1);
                        String playerName = chatData.substring(pos1, pos2);
                        Player player = Bukkit.getPlayer(playerName);
                        if (player != null)
                        {
                            if (player.getGameMode() == GameMode.SPECTATOR)
                                event.setCancelled(true);
                        }
                    }
                }
            }
        });
    }

    public void onDisable() {
        this.discordMgr.shutdown();
        this.trackMgr.destroy();
    }

    public MultiverseCore getMvCore() { return this.mvCore; }

    public MultiverseInventories getMvInv() { return this.mvInv; }

    public ManhuntManager getMhMgr() { return this.mhMgr; }

    public PvPManager getPvpMgr() { return this.pvpMgr; }

    public KitManager getKitMgr() { return this.kitMgr; }

    public DiscordManager getDiscordMgr() { return this.discordMgr; }

    public TrackManager getTrackMgr() { return this.trackMgr; }

    public ProtocolManager getProtocolMgr() { return this.protocolMgr; }
}
