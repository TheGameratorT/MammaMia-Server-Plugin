package com.thegameratort.mammamia.track;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.thegameratort.mammamia.MammaMia;
import com.thegameratort.mammamia.discord.DiscordManager;
import org.bukkit.configuration.ConfigurationSection;

import javax.annotation.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Logger;

public class TrackManager extends AudioEventAdapter {
    private final MammaMia plugin;
    private final Logger logger;
    private final AudioPlayer player;
    private final AudioPlayerManager playerManager;
    private DiscordManager discordManager;
    private TrackEventAdapter listener;
    private final HashMap<String, AudioTrack> tracks = new HashMap<>();

    private boolean trackPlaying = false;
    private String trackName = "";

    public TrackManager(MammaMia plugin) {
        this.plugin = plugin;
        this.logger = this.plugin.getLogger();

        this.playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(this.playerManager);
        AudioSourceManagers.registerLocalSource(this.playerManager);

        this.player = this.playerManager.createPlayer();
        this.player.addListener(this);

        this.loadTracks();

        new TrackCommand(this.plugin, this);
    }

    public void startTrack(String trackName, long trackOffset) {
        AudioTrack audioTrack = this.tracks.get(trackName);
        if (audioTrack == null) {
            this.logger.info("Could not find track " + trackName);
            return;
        }
        this.trackName = trackName;
        // Play on discord
        if (this.discordManager.isActive()) {
            this.discordManager.joinVC();
        }
        this.player.startTrack(audioTrack.makeClone(), false);
        if (trackOffset != 0) {
            this.player.getPlayingTrack().setPosition(trackOffset);
        }
        this.trackPlaying = true;
        if (this.listener != null)
            this.listener.onTrackStart();
        this.logger.info("Started track " + trackName);
    }

    public void stopTrack() {
        // Stop on discord
        this.player.stopTrack();
        this.trackPlaying = false;
    }

    public void loadTracks() {
        ConfigurationSection trackSec = this.plugin.getConfig().getConfigurationSection("tracks");
        if (trackSec == null) {
            this.logger.warning("Track section not found in config, skipping track loading...");
            return;
        }
        this.logger.info("Loading tracks...");
        ArrayList<Future<Void>> futures = new ArrayList<>();
        for (String trackName : trackSec.getKeys(false)) {
            String trackPath = trackSec.getString(trackName);
            Future<Void> future = loadTrack(trackName, trackPath);
            if (future != null)
                futures.add(future);
        }
        for (Future<Void> future : futures) {
            synchronized (future) {
                try {
                    future.get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }
        this.logger.info("Tracks loaded.");
    }

    public Future<Void> loadTrack(final String trackName, String trackPath) {
        if (trackPath == null) {
            this.plugin.getLogger().warning("Empty track path for " + trackName);
            return null;
        }

        final String absPath;
        if (trackPath.startsWith("http")) {
            absPath = trackPath;
        }
        else {
            File file = new File(trackPath);
            if (file.isAbsolute()) {
                absPath = trackPath;
            }
            else {
                String dataFolder = this.plugin.getDataFolder().getAbsolutePath();
                absPath = dataFolder + "\\audio\\" + trackPath;
            }
        }

        return this.playerManager.loadItemOrdered(this, absPath, new AudioLoadResultHandler() {
            public void trackLoaded(AudioTrack track) {
                TrackManager.this.tracks.put(trackName, track);
                TrackManager.this.plugin.getLogger().info("Loaded track " + trackName);
            }

            public void playlistLoaded(AudioPlaylist playlist) {
                TrackManager.this.plugin.getLogger().warning("Playlists are not supported.");
            }

            public void noMatches() {
                TrackManager.this.plugin.getLogger().warning("Track " + trackName + " not found: " + absPath);
            }

            public void loadFailed(FriendlyException exception) {
                TrackManager.this.plugin.getLogger().warning("Load track " + trackName + " failed: " + exception.getMessage());
                exception.printStackTrace();
            }
        });
    }

    public void reload() {
        this.plugin.reloadConfig();
        this.tracks.clear();
        this.loadTracks();
    }

    public void setListener(@Nullable TrackEventAdapter listener) {
        this.listener = listener;
    }

    public Set<String> getTrackNames() {
        return this.tracks.keySet();
    }

    public String getTrackName() {
        return this.trackName;
    }

    /**
     * Get the current position of the track in milliseconds
     * */
    public long getTrackPosition() {
        return this.player.getPlayingTrack().getPosition();
    }

    public boolean getPlaying() {
        return this.trackPlaying;
    }

    public AudioPlayer getPlayer() {
        return this.player;
    }

    public AudioPlayerManager getPlayerManager() {
        return this.playerManager;
    }

    public void setDiscordManager(DiscordManager discordManager) {
        this.discordManager = discordManager;
    }

    public void destroy() {
        this.player.destroy();
        this.playerManager.shutdown();
    }

    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        this.trackPlaying = false;
        if (this.listener != null)
            this.listener.onTrackEnd();
    }

    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        Logger logger = this.plugin.getLogger();
        logger.warning("Audio track exception: " + exception.getMessage());
        logger.warning(exception.getCause().getMessage());
        exception.printStackTrace();
    }

    public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
        this.plugin.getLogger().warning("Audio track is stuck.");
    }
}
