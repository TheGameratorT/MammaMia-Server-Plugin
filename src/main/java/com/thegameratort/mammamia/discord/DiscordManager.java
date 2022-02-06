package com.thegameratort.mammamia.discord;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.thegameratort.mammamia.MammaMia;
import com.thegameratort.mammamia.track.TrackManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.managers.AudioManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

import javax.security.auth.login.LoginException;
import java.util.logging.Logger;

public class DiscordManager {
    private final MammaMia plugin;
    private final Logger logger;
    private final AudioPlayer player;
    private JDA client = null;
    private Guild guild = null;
    private VoiceChannel voiceChannel = null;
    private AudioManager audioManager = null;

    private boolean enabled;

    public DiscordManager(MammaMia plugin) {
        this.plugin = plugin;
        this.logger = this.plugin.getLogger();

        TrackManager trackManager = this.plugin.getTrackMgr();
        trackManager.setDiscordManager(this);

        this.player = trackManager.getPlayer();

        new DiscordCommand(plugin, this);

        this.enabled = getConfig().getBoolean("enabled", false);
        if (this.enabled) {
            startup();
        }
    }

    public void joinVC() {
        if (!isActive()) {
            this.logger.info("Can't join voice channel, please start the bot first.");
            return;
        }
        if (this.voiceChannel == null) {
            this.logger.info("Please set a valid voice channel before trying to join one.");
            return;
        }
        if (this.audioManager.isConnected())
            return;
        this.logger.info("Connecting to voice channel...");
        this.audioManager.openAudioConnection(this.voiceChannel);
    }

    public void updateVC() {
        this.logger.info("Updating voice channel...");
        this.audioManager.openAudioConnection(this.voiceChannel);
    }

    public void leaveVC() {
        if (this.audioManager == null)
            return;
        if (!this.audioManager.isConnected())
            return;
        this.logger.info("Disconnecting from voice channel...");
        this.audioManager.closeAudioConnection();
    }

    public boolean isConnectedToVC() {
        return this.audioManager != null && this.audioManager.isConnected();
    }

    public void setBotToken(CommandSender sender, String botToken) {
        if (this.isActive()) {
            sender.sendMessage(ChatColor.RED + "Please shutdown the bot before changing it's token.");
            return;
        }
        getConfig().set("botToken", botToken);
        this.plugin.saveConfig();
    }

    public void setBotStatus(String botStatus) {
        getConfig().set("botStatus", botStatus);
        this.plugin.saveConfig();
        this.client.getPresence().setActivity(Activity.playing(botStatus));
    }

    public void setGuildID(CommandSender sender, long guildID) {
        getConfig().set("guildID", guildID);
        this.plugin.saveConfig();
        setCurrentGuild(sender, guildID);
    }

    public void setVoiceChannelID(CommandSender sender, long voiceChannelID) {
        getConfig().set("voiceID", voiceChannelID);
        this.plugin.saveConfig();
        setCurrentVoiceChannel(sender, voiceChannelID);
    }

    private boolean setCurrentGuild(CommandSender sender, long guildID) {
        Guild guild = this.client.getGuildById(guildID);
        if (guild == null) {
            msgSender(sender, "The Discord guild ID is invalid.");
            return false;
        }
        if (isConnectedToVC()) {
            msgSender(sender, "Please disconnect the bot from the voice channel before changing the current guild.");
            return false;
        }
        this.guild = guild;
        updateAudioManager();
        this.voiceChannel = null;
        return true;
    }

    private boolean setCurrentVoiceChannel(CommandSender sender, long voiceChannelID) {
        if (this.guild == null) {
            msgSender(sender, "Can not set a voice channel if a guild isn't set.");
            return false;
        }
        VoiceChannel voiceChannel = this.guild.getVoiceChannelById(voiceChannelID);
        if (voiceChannel == null) {
            msgSender(sender, "The Discord voice channel ID is invalid.");
            return false;
        }
        this.voiceChannel = voiceChannel;
        if (isConnectedToVC()) {
            updateVC();
        }
        return true;
    }

    public void enable(CommandSender sender) {
        if (this.enabled)
            return;

        startup();

        this.enabled = true;
        getConfig().set("enabled", true);
        this.plugin.saveConfig();
    }

    public void disable() {
        if (!this.enabled)
            return;
        shutdown();
        this.enabled = false;
        getConfig().set("enabled", false);
        this.plugin.saveConfig();
    }

    public void startup() {
        if (isActive()) {
            return;
        }

        this.logger.info("Connecting to Discord...");

        ConfigurationSection config = getConfig();
        String botToken = config.getString("botToken", "");
        String botStatus = config.getString("botStatus", "");

        JDABuilder builder = JDABuilder.createDefault(botToken);

        if (!botStatus.isEmpty())
            builder.setActivity(Activity.playing(botStatus));

        try {
            this.client = builder.build();
            this.client.awaitReady();
        } catch (LoginException e) {
            this.logger.warning("LoginException: Discord token is invalid. " + e.getMessage());
            this.client = null;
            return;
        } catch (InterruptedException e) {
            e.printStackTrace();
            this.client = null;
            return;
        }

        this.logger.info("Connected to Discord.");

        long guildID = config.getLong("guildID", -1L);
        if (guildID == -1) {
            this.logger.warning("The Discord guild ID is missing.");
            return;
        }
        if (!setCurrentGuild(null, guildID)) return;

        long voiceID = config.getLong("voiceID", -1L);
        if (voiceID == -1) {
            this.logger.warning("The Discord voice channel ID is missing.");
            return;
        }
        /*if (!*/setCurrentVoiceChannel(null, voiceID)/*) return*/;
    }

    public void shutdown() {
        if (!isActive())
            return;
        this.logger.info("Shutting down the bot...");
        leaveVC();
        this.audioManager = null;
        this.client.shutdown();
        this.client = null;
        this.logger.info("Bot shutdown successfully.");
    }

    private void updateAudioManager() {
        this.audioManager = this.guild.getAudioManager();
        this.audioManager.setSendingHandler(new AudioPlayerSendHandler(this.player));
        this.audioManager.setSelfDeafened(true);
    }

    private void msgSender(CommandSender sender, String msg) {
        if (sender == null) {
            this.logger.warning(msg);
        } else {
            sender.sendMessage(ChatColor.RED + msg);
        }
    }

    private ConfigurationSection getConfig() {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("discord");
        if (section == null) {
            section = plugin.getConfig().createSection("discord");
        }
        return section;
    }

    public boolean isActive() {
        return this.client != null;
    }
}
