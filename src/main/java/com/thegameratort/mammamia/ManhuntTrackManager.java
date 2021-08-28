package com.thegameratort.mammamia;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import java.util.ArrayList;
import java.util.Random;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

public class ManhuntTrackManager extends AudioEventAdapter implements Listener
{
    private final MammaMia plugin;
    private final ManhuntManager mhMgr;
    private final TrackManager trackMgr;
    private final Random random = new Random();
    private int updaterTaskID;

    private static class TrackEntry
    {
        String trackName = "";
        boolean playOnEnd = false;

        TrackEntry() {}

        TrackEntry(String trackName, boolean playOnEnd)
        {
            this.trackName = trackName;
            this.playOnEnd = playOnEnd;
        }

        public TrackEntry makeCopy()
        {
            TrackEntry entry = new TrackEntry();
            entry.trackName = this.trackName;
            entry.playOnEnd = this.playOnEnd;
            return entry;
        }

        public boolean equals(TrackEntry other)
        {
            return other.trackName.equals(this.trackName);
        }
    }

    private static class DistanceInfo
    {
        double distance;
        World.Environment env;

        private DistanceInfo() {}
    }

    private boolean isPlaying = false;
    private boolean isSpecial = false;
    private boolean blockEndEvent = false;
    private TrackEntry currentTrack = new TrackEntry();
    private TrackEntry nextTrack = new TrackEntry();
    private final ArrayList<String> rngOrgasms = new ArrayList<>();
    private boolean isFinale = false;

    private final TrackEntry[] calmTracks = {
            new TrackEntry("mh_calm1", true),
            new TrackEntry("mh_calm2", true),
            new TrackEntry("mh_calm3", true),
            new TrackEntry("mh_calm4", true)
    };

    private final TrackEntry[] dangerTracks = {
            new TrackEntry("mh_danger1", true),
            new TrackEntry("mh_danger2", true),
            new TrackEntry("mh_danger3", true),
            new TrackEntry("mh_danger4", true)
    };

    private final TrackEntry[] netherTracks = {
            new TrackEntry("mh_netherTenseStart", false),
            new TrackEntry("mh_netherTenseLoop", true),
            new TrackEntry("mh_netherFightStart", true),
            new TrackEntry("mh_netherFightStart", false),
            new TrackEntry("mh_netherFightLoop", true),
            new TrackEntry("mh_netherFightEnd", true)
    };

    private final TrackEntry[] tenseTracks = {
            new TrackEntry("mh_tenseMax", true),
            new TrackEntry("mh_tenseMed", false),
            new TrackEntry("mh_tenseMin", false),
            new TrackEntry("mh_tenseMin", true)
    };

    private final TrackEntry[] specialTracks = {
            new TrackEntry("mh_hunterDeath", false),
            new TrackEntry("mh_runnerDeath", false),
            new TrackEntry("mh_hunterDeathByRunner", false)
    };

    private final TrackEntry[] orgasmTracks = {
            new TrackEntry("ch_orgasm1", false),
            new TrackEntry("ch_orgasm2", false),
            new TrackEntry("ch_orgasm4", false),
            new TrackEntry("ch_orgasm3", false)
    };

    private final TrackEntry startTrack = new TrackEntry("mh_start", false);
    private final TrackEntry fightTrack = new TrackEntry("mh_fight", false);
    private final TrackEntry endTrack = new TrackEntry("mh_end", false);
    private final TrackEntry finaleTrack = new TrackEntry("mh_finale", false);

    ManhuntTrackManager(MammaMia plugin)
    {
        this.plugin = plugin;
        this.mhMgr = plugin.getMhMgr();
        this.trackMgr = plugin.getDiscordMgr().getTrackMgr();
    }

    public void start()
    {
        Bukkit.getPluginManager().registerEvents(this, this.plugin);
        this.trackMgr.setListener(this);
        this.updaterTaskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(this.plugin, this::update, 0L, 20L);
    }

    public void stop()
    {
        HandlerList.unregisterAll(this);
        this.trackMgr.setListener(null);
        Bukkit.getScheduler().cancelTask(this.updaterTaskID);
    }

    private void update()
    {
        if (this.isSpecial)
            return;
        TrackEntry track = getNextTrack();
        proposeTrack(track);
        this.plugin.getLogger().info("Next candidate: " + track.trackName + " | wait: " + track.playOnEnd);
    }

    public void onTrackStart(AudioPlayer player, AudioTrack track) {}

    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason)
    {
        if (this.blockEndEvent) {
            this.blockEndEvent = false;
            return;
        }
        this.isPlaying = false;
        this.isSpecial = false;
        if (this.currentTrack.equals(this.nextTrack))
            startTrack(this.currentTrack);
        else if (!this.nextTrack.trackName.isEmpty())
            startTrack(this.nextTrack);
    }

    public void startStartTrack()
    {
        startTrack(this.startTrack);
    }

    private void proposeTrack(TrackEntry track)
    {
        if (!this.isPlaying)
            startTrack(track);
        else if (track.playOnEnd)
            this.nextTrack = track.makeCopy();
        else if (!this.currentTrack.equals(track))
            startTrack(track);
    }

    private void startSpecialTrack(TrackEntry track)
    {
        startTrack(track);
        this.isSpecial = true;
    }

    private void startTrack(TrackEntry track)
    {
        if (this.isPlaying)
            this.blockEndEvent = true;
        this.trackMgr.startTrack(track.trackName);
        this.currentTrack = track.makeCopy();
        this.nextTrack = new TrackEntry();
        this.isPlaying = true;
    }

    private void skipTrack()
    {
        this.currentTrack = new TrackEntry();
        TrackEntry entry = getNextTrack();
        proposeTrack(entry);
        this.trackMgr.stopTrack();
        this.isPlaying = false;
    }

    public void stopTracks()
    {
        if (this.isPlaying)
            this.blockEndEvent = true;
        this.trackMgr.stopTrack();
        this.isPlaying = false;
    }

    // yanderedev mode lessssgooooooo
    // <if it works don't touch it>
    private TrackEntry getNextTrack()
    {
        TrackEntry track = null;
        boolean skipCurrent = false;
        DistanceInfo di = getDistanceToRunner();
        if (isFinale) {
            if (di.env == World.Environment.THE_END)
                track = this.currentTrack;
            else
                isFinale = false;
        }
        if (track == null) {
            switch (this.currentTrack.trackName) {
                case "mh_start":
                    if (di.distance >= 300) {
                        skipCurrent = true;
                        break;
                    }
                    track = this.currentTrack;
                    break;
                case "mh_fight":
                    if (di.distance < 100)
                        track = this.currentTrack;
                    break;
                case "mh_tenseMed":
                    if (di.distance < 25)
                        track = this.fightTrack; // mh_fight | no wait
                    else if (di.distance < 200)
                        track = this.tenseTracks[0]; // mh_tenseMax | wait
                    else if (di.distance < 300)
                        track = this.tenseTracks[3]; // mh_tenseMin | wait
                case "mh_tenseMin":
                    if (di.distance >= 300)
                        skipCurrent = true;
                    break;
                case "mh_netherTenseStart":
                    if (di.distance < 80) {
                        track = this.netherTracks[2]; // mh_netherFightStart | wait
                        break;
                    }
                    if (di.distance < 150)
                        track = this.netherTracks[1]; // mh_netherTenseLoop | wait
                    break;
                case "mh_netherFightStart":
                    track = this.netherTracks[4]; // mh_netherFightLoop | wait
                    break;
                case "mh_netherFightLoop":
                    if (di.distance < 80) {
                        track = this.currentTrack;
                        break;
                    }
                    track = this.netherTracks[5]; // mh_netherFightEnd | wait
                    break;
            }
        }
        if (track == null) {
            switch (di.env) {
                case NORMAL:
                    if (di.distance < 25) {
                        track = this.fightTrack; // mh_fight | no wait
                        break;
                    }
                    if (di.distance < 100) {
                        track = this.tenseTracks[0]; // mh_tenseMax | wait
                        break;
                    }
                    if (di.distance < 200) {
                        track = this.tenseTracks[1]; // mh_tenseMed | no wait
                        break;
                    }
                    if (di.distance < 300)
                        track = this.tenseTracks[2]; // mh_tenseMin | no wait
                    break;
                case NETHER:
                    if (di.distance < 80) {
                        track = this.netherTracks[3]; // mh_netherFightStart | no wait
                        break;
                    }
                    if (di.distance < 150)
                        track = this.netherTracks[0]; // mh_netherTenseStart | no wait
                    break;
            }
            if (track == null)
                track = getNextEnvTrack(di.env);
        }
        if (skipCurrent) {
            track = track.makeCopy();
            track.playOnEnd = false;
        }
        return track;
    }

    private TrackEntry getNextEnvTrack(World.Environment env)
    {
        switch (env) {
            default:
                return getDifferentTrack(this.calmTracks);
            case NETHER:
                return getDifferentTrack(this.dangerTracks);
            case THE_END:
                return this.endTrack;
        }
    }

    private TrackEntry getDifferentTrack(TrackEntry[] tracks)
    {
        while (true) {
            TrackEntry track = tracks[(int)(Math.random() * tracks.length)];
            if (!track.equals(this.currentTrack))
                return track;
        }
    }

    private DistanceInfo getDistanceToRunner()
    {
        ArrayList<Player> hunters = this.mhMgr.getTeamPlayers(ManhuntTeam.Hunters);
        ArrayList<Player> runners = this.mhMgr.getTeamPlayers(ManhuntTeam.Runners);

        DistanceInfo info = new DistanceInfo();
        info.distance = Double.MAX_VALUE;
        info.env = World.Environment.NORMAL;

        for (Player runner : runners)
        {
            if (runner.isDead())
                continue;

            World wRunner = runner.getWorld();
            if (wRunner.getEnvironment() == World.Environment.THE_END)
            {
                info.env = World.Environment.THE_END;
                break;
            }
            for (Player hunter : hunters)
            {
                if (hunter.isDead())
                    continue;
                World wHunter = hunter.getWorld();
                if (wHunter == wRunner)
                {
                    double distance = hunter.getLocation().distance(runner.getLocation());
                    if (distance < info.distance)
                    {
                        info.distance = distance;
                        info.env = wHunter.getEnvironment();
                    }
                }
            }
        }

        return info;
    }

    private void startHunterDeathTrack(TrackEntry track)
    {
        if (!isFinale)
            startSpecialTrack(track);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event)
    {
        if (this.mhMgr.getCockhunt())
        {
            int rng = this.random.nextInt(1000);
            if (rng == 0)
            {
                TrackEntry track;
                if (this.rngOrgasms.size() >= this.orgasmTracks.length)
                    this.rngOrgasms.clear();
                do {
                    track = this.orgasmTracks[this.random.nextInt(this.orgasmTracks.length)];
                } while (this.rngOrgasms.contains(track.trackName));
                this.rngOrgasms.add(track.trackName);
                startSpecialTrack(track);
            }
        }

        if (this.currentTrack.trackName.equals("mh_start"))
        {
            Player player = event.getPlayer();
            if (this.mhMgr.getPlayerInTeam(player, ManhuntTeam.Runners))
                if (event.getBlock().getType() == Material.IRON_ORE)
                    skipTrack();
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event)
    {
        Player killer;
        boolean hunterKilledByRunner;
        Player player = event.getEntity();
        int pTeamID = this.mhMgr.getPlayerTeam(player);
        switch (pTeamID) {
            case ManhuntTeam.Hunters:
                killer = player.getKiller();
                hunterKilledByRunner = false;
                if (killer != null)
                    if (this.mhMgr.getPlayerInTeam(killer, ManhuntTeam.Runners))
                        hunterKilledByRunner = true;
                if (hunterKilledByRunner) {
                    startHunterDeathTrack(this.specialTracks[2]); // mh_hunterDeathByRunner | no wait
                    break;
                }
                startHunterDeathTrack(this.specialTracks[0]); // mh_hunterDeath | no wait
                break;
            case ManhuntTeam.Runners:
                startSpecialTrack(this.specialTracks[1]); // mh_runnerDeath | no wait
                break;
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event)
    {
        Entity victim = event.getEntity();
        if (!(victim instanceof EnderDragon))
            return;
        EnderDragon dragon = (EnderDragon)victim;
        if (dragon.getHealth() <= 50)
        {
            if (!isFinale)
                startTrack(this.finaleTrack); // mh_finale | no wait
            isFinale = true;
        }
    }
}
