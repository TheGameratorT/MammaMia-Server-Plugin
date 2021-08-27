package com.thegameratort.mammamia;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import javax.security.auth.login.LoginException;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import org.bukkit.configuration.ConfigurationSection;

public class DiscordManager
{
    private final MammaMia plugin;
    private JDA client;
    private Guild guild;
    private VoiceChannel voiceChannel;
    private AudioPlayer player;
    private AudioPlayerManager playerManager;
    private AudioManager audioManager;
    private TrackManager trackMgr = null;

    public DiscordManager(MammaMia plugin) {
        this.plugin = plugin;
        ConfigurationSection config = plugin.getConfig().getConfigurationSection("discord");
        if (config == null)
            return;
        boolean enabled = config.getBoolean("enabled", false);
        String botToken = config.getString("botToken", "");
        String botStatus = config.getString("botStatus", "");
        long guildID = config.getLong("guildID", -1L);
        long voiceID = config.getLong("voiceID", -1L);
        if (!enabled)
            return;
        if (guildID == -1) {
            plugin.getLogger().warning("The Discord guild ID is missing.");
            return;
        }
        if (voiceID == -1) {
            plugin.getLogger().warning("The Discord voice channel ID is missing.");
            return;
        }
        JDABuilder builder = JDABuilder.createDefault(botToken);
        if (!botStatus.isEmpty())
            builder.setActivity(Activity.playing(botStatus));
        try {
            this.client = builder.build();
            this.client.awaitReady();
        } catch (LoginException e) {
            plugin.getLogger().warning("LoginException: Discord token is invalid. " + e.getMessage());
            this.client = null;
            return;
        } catch (InterruptedException e) {
            e.printStackTrace();
            this.client = null;
            return;
        }
        this.guild = this.client.getGuildById(guildID);
        if (this.guild == null) {
            plugin.getLogger().warning("The Discord guild ID is invalid.");
            return;
        }
        this.voiceChannel = this.guild.getVoiceChannelById(voiceID);
        if (this.voiceChannel == null) {
            plugin.getLogger().warning("The Discord voice channel ID is invalid.");
            return;
        }
        this.playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(this.playerManager);
        AudioSourceManagers.registerLocalSource(this.playerManager);
        this.player = this.playerManager.createPlayer();
        this.audioManager = this.guild.getAudioManager();
        this.audioManager.setSendingHandler(new AudioPlayerSendHandler(this.player));
        this.audioManager.setSelfDeafened(true);
        plugin.getLogger().info("Connected to Discord.");
        this.trackMgr = new TrackManager(plugin, this, this.playerManager, this.player);
    }

    public void joinVC() {
        if (this.audioManager.isConnected())
            return;
        this.plugin.getLogger().info("Connecting to voice chat...");
        this.audioManager.openAudioConnection(this.voiceChannel);
    }

    public void leaveVC() {
        if (!this.audioManager.isConnected())
            return;
        this.plugin.getLogger().info("Disconnecting from voice chat...");
        this.audioManager.closeAudioConnection();
    }

    public void shutdown() {
        if (this.client == null)
            return;
        this.plugin.getLogger().info("Shutting down the bot...");
        if (this.trackMgr != null) {
            this.trackMgr.setListener(null);
            this.trackMgr.stopTrack();
        }
        this.client.shutdown();
    }

    public TrackManager getTrackMgr() {
        return this.trackMgr;
    }
}