package com.thegameratort.mammamia.manhunt;

import com.thegameratort.mammamia.MammaMia;
import com.thegameratort.mammamia.track.TrackEventAdapter;
import com.thegameratort.mammamia.track.TrackManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TranslatableComponent;
import net.kyori.adventure.text.format.NamedTextColor;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ManhuntTrackManager extends TrackEventAdapter implements Listener
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
    private final ArrayList<String> rngTrolls = new ArrayList<>();
    private final ArrayList<String> rngFitmcs = new ArrayList<>();
    private boolean isFinale = false;
    private boolean isFitmcTrack = false;
    private boolean rngIsFeelingExcited;
    private int lastTrollTrackTick = 0;

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
        new TrackEntry("mh_tenseMin", true),
        new TrackEntry("mh_tenseMax", false)
    };

    private final TrackEntry[] specialTracks = {
        new TrackEntry("mh_hunterDeath", false),
        new TrackEntry("mh_runnerDeath", false),
        new TrackEntry("mh_hunterDeathByRunner", false)
    };

    private final TrackEntry[] orgasmTracks = {
        new TrackEntry("ch_orgasm1", false),
        new TrackEntry("ch_orgasm2", false),
        new TrackEntry("ch_orgasm3", false),
        new TrackEntry("ch_orgasm4", false)
    };

    private final TrackEntry[] trollTracks = {
        new TrackEntry("ch_troll1", false),
        new TrackEntry("ch_troll2", false),
        new TrackEntry("ch_troll3", false)
    };

    private final TrackEntry[] fitmcTracks = {
        new TrackEntry("ch_fit1", false),
        new TrackEntry("ch_fit2", false),
        new TrackEntry("ch_fit3", false),
        new TrackEntry("ch_fit4", false),
        new TrackEntry("ch_fit5", false),
        new TrackEntry("ch_fit6", false),
        new TrackEntry("ch_fit7", false),
        new TrackEntry("ch_fit8", false),
        new TrackEntry("ch_fit9", false),
        new TrackEntry("ch_fit10", false),
        new TrackEntry("ch_fit11", false)
    };

    private final TrackEntry startTrack = new TrackEntry("mh_start", false);
    private final TrackEntry fightTrack = new TrackEntry("mh_fight", false);
    private final TrackEntry endTrack = new TrackEntry("mh_end", false);
    private final TrackEntry finaleTrack = new TrackEntry("mh_finale", false);

    ManhuntTrackManager(MammaMia plugin, ManhuntManager mhMgr)
    {
        this.plugin = plugin;
        this.mhMgr = mhMgr;
        this.trackMgr = plugin.getTrackMgr();
    }

    public void start()
    {
        Bukkit.getPluginManager().registerEvents(this, this.plugin);
        this.trackMgr.setListener(this);
        this.updaterTaskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(this.plugin, this::update, 0L, 20L);
        this.rngIsFeelingExcited = this.random.nextInt(2) != 0;
    }

    public void stop()
    {
        this.stopTracks();
        HandlerList.unregisterAll(this);
        this.trackMgr.setListener(null);
        Bukkit.getScheduler().cancelTask(this.updaterTaskID);
    }

    private void update()
    {
        if (this.mhMgr.getCockhunt() && this.mhMgr.getCockhuntHasFitmc() && !this.isSpecial)
        {
            int currentTick = Bukkit.getCurrentTick();
            if (currentTick - this.lastTrollTrackTick > 20 * 60) // 1 minute must have passed
            {
                if (this.random.nextInt(500) == 0)
                {
                    this.playFitMcTrack();
                    this.lastTrollTrackTick = currentTick;
                }
            }
        }
        if (this.isSpecial)
            return;
        TrackEntry track = getNextTrack();
        proposeTrack(track);
    }

    //public void onTrackStart() {}

    public void onTrackEnd()
    {
        if (this.blockEndEvent) {
            this.blockEndEvent = false;
            return;
        }
        if (this.isFitmcTrack)
        {
            for (Player player : this.mhMgr.getParticipants())
                sendFakeFitMCMessage(player, "multiplayer.player.left");
        }
        this.isPlaying = false;
        this.isSpecial = false;
        this.isFitmcTrack = false;
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
        this.trackMgr.startTrack(track.trackName, 0);
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
                case "mh_tenseMax":
                case "mh_tenseMed":
                    if (di.distance < 10)
                        track = this.fightTrack; // mh_fight | no wait
                    else if (di.distance < 200)
                        track = this.tenseTracks[0]; // mh_tenseMax | wait
                    else if (di.distance < 300)
                        track = this.tenseTracks[3]; // mh_tenseMin | wait
                case "mh_tenseMin":
                    // track starts at 300 blocks, but only
                    // force stops at 500 blocks, otherwise
                    // wait for track to end
                    if (di.distance >= 500)
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
                case NORMAL -> {
                    if (di.distance < 10) {
                        track = this.fightTrack; // mh_fight | no wait
                        break;
                    }
                    if (di.distance < 100) {
                        track = this.tenseTracks[4]; // mh_tenseMax | no wait
                        break;
                    }
                    if (di.distance < 200) {
                        track = this.tenseTracks[1]; // mh_tenseMed | no wait
                        break;
                    }
                    if (di.distance < 300)
                        track = this.tenseTracks[2]; // mh_tenseMin | no wait
                }
                case NETHER -> {
                    if (di.distance < 80) {
                        track = this.netherTracks[3]; // mh_netherFightStart | no wait
                        break;
                    }
                    if (di.distance < 150)
                        track = this.netherTracks[0]; // mh_netherTenseStart | no wait
                }
            }
            if (track == null)
                track = getNextEnvTrack(di.env);
        }
        if (skipCurrent) {
            track = track.makeCopy();
            track.playOnEnd = false;
        }
        if (mhMgr.getDebug())
        {
            String distanceStr = di.distance == Double.MAX_VALUE ? "INF" : Long.toString(Math.round(di.distance));
            String debugMsg =
                "{" + distanceStr + ";" + di.env + "}, " +
                "{" + track.trackName + ";" + track.playOnEnd + "}";
            for (Player player : mhMgr.getParticipants())
            {
                if (player.isOp())
                    player.sendMessage(debugMsg);
            }
            this.plugin.getLogger().info(debugMsg);
        }
        return track;
    }

    private TrackEntry getNextEnvTrack(World.Environment env)
    {
        return switch (env) {
            default -> getDifferentTrack(this.calmTracks);
            case NETHER -> getDifferentTrack(this.dangerTracks);
            case THE_END -> this.endTrack;
        };
    }

    private TrackEntry getDifferentTrack(TrackEntry[] tracks)
    {
        while (true) {
            TrackEntry track = tracks[this.random.nextInt(tracks.length)];
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
        if (!this.isFinale)
            startSpecialTrack(track);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event)
    {
        if (this.mhMgr.getCockhunt() && !this.isSpecial)
        {
            int currentTick = Bukkit.getCurrentTick();
            if (currentTick - this.lastTrollTrackTick > 20 * 60) // 1 minute must have passed
            {
                int rng = this.random.nextInt(900);
                if (rng == 0)
                {
                    boolean hentaiFlag = this.mhMgr.getCockhuntHasHentai();
                    boolean trollFlag = this.mhMgr.getCockhuntHasTroll();
                    if (hentaiFlag || trollFlag)
                    {
                        boolean playHentai = this.rngIsFeelingExcited;
                        if (!hentaiFlag)
                            playHentai = false;
                        if (!trollFlag)
                            playHentai = true;

                        if (playHentai)
                            this.playSomeTrollRngTrack(this.orgasmTracks, this.rngOrgasms);
                        else
                            this.playSomeTrollRngTrack(this.trollTracks, this.rngTrolls);
                        this.rngIsFeelingExcited = !playHentai;
                        this.lastTrollTrackTick = currentTick;
                    }
                }
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

    private void playSomeTrollRngTrack(TrackEntry[] tracks, ArrayList<String> rngTracks)
    {
        TrackEntry track;
        if (rngTracks.size() >= tracks.length)
            rngTracks.clear();
        do {
            track = tracks[this.random.nextInt(tracks.length)];
        } while (rngTracks.contains(track.trackName));
        rngTracks.add(track.trackName);
        startSpecialTrack(track);
    }

    private void playFitMcTrack()
    {
        stopTracks();
        this.isSpecial = true; // Set in advance to prevent another music from playing
        this.isFitmcTrack = true;

        for (Player player : this.mhMgr.getParticipants())
            sendFakeFitMCMessage(player, "multiplayer.player.joined");

        Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, () ->
            this.playSomeTrollRngTrack(this.fitmcTracks, this.rngFitmcs)
        , 30);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event)
    {
        Player killer;
        boolean hunterKilledByRunner;
        Player player = event.getEntity();
        int pTeamID = this.mhMgr.getPlayerTeam(player);
        switch (pTeamID) {
            case ManhuntTeam.Hunters -> {
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
            }
            case ManhuntTeam.Runners -> startSpecialTrack(this.specialTracks[1]); // mh_runnerDeath | no wait
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event)
    {
        Entity victim = event.getEntity();
        if (!(victim instanceof EnderDragon dragon))
            return;
        if (dragon.getHealth() <= 50)
        {
            if (!isFinale)
                startTrack(this.finaleTrack); // mh_finale | no wait
            isFinale = true;
        }
    }

    private void sendFakeFitMCMessage(Player player, String key)
    {
        TranslatableComponent msg =
            Component.translatable(key)
                .args(List.of(Component.text("FitMC")))
                .color(NamedTextColor.YELLOW);
        player.sendMessage(msg);
    }
}
