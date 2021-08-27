package com.thegameratort.mammamia;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventListener;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Future;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import org.bukkit.configuration.ConfigurationSection;

public class TrackManager extends AudioEventAdapter
{
    private final MammaMia plugin;
    private final DiscordManager discordMgr;
    private final AudioPlayerManager playerManager;
    private final AudioPlayer player;
    private AudioEventListener listener;
    public HashMap<String, AudioTrack> tracks = new HashMap<>();

    public TrackManager(MammaMia plugin, DiscordManager discordMgr, AudioPlayerManager playerManager, AudioPlayer player)
    {
        this.plugin = plugin;
        this.discordMgr = discordMgr;
        this.playerManager = playerManager;
        this.player = player;
        player.addListener(this);
        this.loadTracks();
    }

    public AudioTrack startTrack(String trackName)
    {
        if (!this.tracks.containsKey(trackName))
        {
            this.plugin.getLogger().info("Could not find track " + trackName);
            return null;
        }
        this.discordMgr.joinVC();
        AudioTrack track = (this.tracks.get(trackName)).makeClone();
        this.player.startTrack(track, false);
        this.plugin.getLogger().info("Started track " + trackName);
        return track;
    }

    public void stopTrack()
    {
        this.player.stopTrack();
    }

    public void loadTracks()
    {
        ConfigurationSection musicSec = this.plugin.getConfig().getConfigurationSection("tracks");
        if (musicSec == null)
            return;
        this.plugin.getLogger().info("Loading tracks...");
        ArrayList<Future<Void>> futures = new ArrayList<>();
        for (String trackName : musicSec.getKeys(false))
        {
            String trackPath = musicSec.getString(trackName);
            Future<Void> future = loadTrack(trackName, trackPath);
            if (future != null)
                futures.add(future);
        }
        for (Future<Void> future : futures)
        {
            synchronized (future)
            {
                try {
                    future.get();
                } catch (InterruptedException|java.util.concurrent.ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }
        this.plugin.getLogger().info("Tracks loaded.");
    }

    public Future<Void> loadTrack(final String trackName, String trackPath)
    {
        if (trackPath == null)
        {
            this.plugin.getLogger().info("Empty track path for " + trackName);
            return null;
        }

        final String absPath;
        if (trackPath.startsWith("http")) {
            absPath = trackPath;
        }
        else
        {
            File file = new File(trackPath);
            if (file.isAbsolute())
            {
                absPath = trackPath;
            }
            else
            {
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

    public void reload()
    {
        this.tracks.clear();
        this.plugin.reloadConfig();
        this.loadTracks();
    }

    public void setListener(@Nullable AudioEventListener listener)
    {
        if (this.listener != null) {
            this.player.removeListener(this.listener);
        }
        if (listener != null) {
            this.player.addListener(listener);
        }
        this.listener = listener;
    }

    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception)
    {
        Logger logger = this.plugin.getLogger();
        logger.warning("Audio track exception: " + exception.getMessage());
        logger.warning(exception.getCause().getMessage());
        exception.printStackTrace();
    }

    public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs)
    {
        this.plugin.getLogger().warning("Audio track is stuck.");
    }
}
