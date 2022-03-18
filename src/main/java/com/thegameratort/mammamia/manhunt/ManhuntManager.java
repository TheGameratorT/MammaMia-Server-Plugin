package com.thegameratort.mammamia.manhunt;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.profile.container.ProfileContainer;
import com.thegameratort.mammamia.ConfigFile;
import com.thegameratort.mammamia.DelayedExecutor;
import com.thegameratort.mammamia.LoadingScreen;
import com.thegameratort.mammamia.MammaMia;
import org.bukkit.*;
import org.bukkit.World.Environment;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.CompassMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;

public class ManhuntManager
{
    private final MammaMia plugin;
    private ManhuntListener listener = null;
    private CockhuntListener cockListener = null;
    private ManhuntTrackManager mhTrackMgr = null;
    private final ConfigFile config;
    private final MultiverseCore mvCore;
    public Team[] teams = new Team[3];
    private final HashMap<String, String> targets = new HashMap<>();
    private final HashMap<String, Location> portals = new HashMap<>();
    private final String[] teamNames = { "hunters", "runners", "spectators" };
    private boolean isStarted;
    private boolean isCockhunt;
    private boolean isDebug = false;
    private final boolean compassTOO;
    private final boolean alertWrongDimension;

    public ManhuntManager(MammaMia plugin)
    {
        this.plugin = plugin;
        this.mvCore = plugin.getMvCore();

        this.config = ConfigFile.loadConfig(this.plugin, "manhunt.yml");

        this.isStarted = config.getBoolean("started");
        this.isCockhunt = config.getBoolean("cockhunt");
        this.compassTOO = config.getBoolean("compassTracksOutsideOverworld");
        this.alertWrongDimension = config.getBoolean("alertWrongDimension");

        ConfigurationSection targetSec = config.getConfigurationSection("targets");
        if (targetSec != null)
        {
            for (String key : targetSec.getKeys(false))
                this.targets.put(key, targetSec.getString(key));
        }

        ConfigurationSection portalSec = config.getConfigurationSection("portals");
        if (portalSec != null)
        {
            for (String key : portalSec.getKeys(false))
                this.portals.put(key, portalSec.getLocation(key));
        }

        Scoreboard board = plugin.getServer().getScoreboardManager().getMainScoreboard();

        for(int i = 0; i < 3; ++i)
        {
            String teamName = this.teamNames[i];
            Team team = board.getTeam(teamName);
            if (team == null)
            {
                team = board.registerNewTeam(teamName);
                String colorChar = this.config.getString(teamName + "Color");
                team.setColor(ChatColor.getByChar(colorChar));
            }

            this.teams[i] = team;
        }

        if (this.isStarted)
            this.startListeners();

        new ManhuntCommand(plugin, this);
    }

    public void regen(CommandSender sender)
    {
        if (this.isStarted)
        {
            sender.sendMessage("Please stop the manhunt first.");
            return;
        }

        LoadingScreen ls = new LoadingScreen(this.plugin);
        ls.show(this.getParticipants(), () -> {
            DelayedExecutor de = new DelayedExecutor(this.plugin);

            de.add(() -> {
                MVWorldManager worldManager = this.mvCore.getMVWorldManager();
                String seed = Long.valueOf((new Random()).nextLong()).toString();
                this.regenWorld(worldManager, "manhunt", seed);
                this.regenWorld(worldManager, "manhunt_nether", seed);
                this.regenWorld(worldManager, "manhunt_the_end", seed);

                MultiverseInventories mvInv = this.plugin.getMvInv();
                ProfileContainer manhuntInv = mvInv.getGroupManager().getGroup("manhunt").getGroupProfileContainer();
                OfflinePlayer[] players = Bukkit.getOfflinePlayers();

                for (OfflinePlayer player : players)
                    manhuntInv.removeAllPlayerData(player);

                mvInv.reloadConfig();
            }, 20);

            de.add(() -> {
                ls.close();
                Location spawn = this.mvCore.getMVWorldManager().getMVWorld("manhunt").getSpawnLocation();

                for (Player player : this.getParticipants()) {
                    this.mvCore.getSafeTTeleporter().safelyTeleport(Bukkit.getConsoleSender(), player, spawn, false);
                }
            }, 20);

            de.add(() -> {
                for (Player player : this.getParticipants())
                {
                    this.revokePlayerAdvancements(player);
                    player.setGameMode(GameMode.SURVIVAL);
                    player.setBedSpawnLocation(null, true);
                }
            }, 1);

            de.runAll();
        });
    }

    private boolean start_common(CommandSender sender)
    {
        if (this.isStarted) {
            sender.sendMessage("Manhunt is already started.");
            return false;
        }

        if (!this.isDebug)
        {
            int hunterCount = this.getTeamPlayers(ManhuntTeam.Hunters).size();
            int runnerCount = this.getTeamPlayers(ManhuntTeam.Runners).size();
            if (hunterCount == 0 && runnerCount == 0) {
                sender.sendMessage("Unable to start the Manhunt, no hunters nor runners found.");
                return false;
            }
            if (hunterCount == 0) {
                sender.sendMessage("Unable to start the Manhunt, no hunters found.");
                return false;
            }
            if (runnerCount == 0) {
                sender.sendMessage("Unable to start the Manhunt, no runners found.");
                return false;
            }
        }

        this.startListeners();
        this.setStarted(true);
        return true;
    }

    public boolean start(CommandSender sender)
    {
        if (!this.start_common(sender))
            return false;

        for (Player player : this.getParticipants())
        {
            if (getPlayerInTeam(player, ManhuntTeam.Spectators))
                continue;
            player.setGameMode(GameMode.SURVIVAL);
            player.getInventory().clear();
            player.setHealth(20);
            player.setFoodLevel(20);
            player.setSaturation(0.6f);
            this.revokePlayerAdvancements(player);
        }

        if (this.isCockhunt)
        {
            for (Player runner : this.getTeamPlayers(ManhuntTeam.Runners))
                runner.sendTitle("Run as fast as possible!", "They want your pingas...", 10, 70, 20);
        }

        for (Player hunter : this.getTeamPlayers(ManhuntTeam.Hunters))
        {
            if (this.isCockhunt)
                hunter.sendMessage("GO GET THAT PINGAS! :>");
            this.giveHunterCompass(hunter, null);
        }

        for (Player player : getTeamPlayers(ManhuntTeam.Spectators))
            player.setGameMode(GameMode.SPECTATOR);

        this.targets.clear();
        this.portals.clear();
        this.config.set("targets", this.targets);
        this.config.set("portals", this.portals);
        this.config.saveConfig();
        if (this.mhTrackMgr != null)
            this.mhTrackMgr.startStartTrack();

        sender.sendMessage("Manhunt has started!");
        return true;
    }

    public boolean resume(CommandSender sender)
    {
        if (!this.start_common(sender))
            return false;
        sender.sendMessage("Manhunt has resumed!");
        return true;
    }

    public void stop(CommandSender sender)
    {
        if (!this.isStarted)
        {
            sender.sendMessage("Manhunt is already stopped.");
            return;
        }

        this.stopListeners();

        sender.sendMessage("Manhunt is now stopped.");
        this.setStarted(false);
    }

    public void join(CommandSender sender, String[] args)
    {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be run by a player.");
            return;
        }
        Player player = (Player)sender;
        if (this.isStarted) {
            player.sendMessage("Please stop the manhunt first.");
            return;
        }
        if (args.length == 1) {
            player.sendMessage("Please choose a team to join.");
            return;
        }
        int currentTeamID = this.getPlayerTeam(player);
        int newTeamID = this.getTeamIDFromName(args[1]);
        if (newTeamID == -1) {
            player.sendMessage("Invalid team specified.");
            return;
        }

        this.setPlayerTeam(player, currentTeamID, newTeamID);
        sender.sendMessage("You now belong to the " + this.teamNames[newTeamID] + " team.");
    }

    public void leave(CommandSender sender)
    {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be run by a player.");
            return;
        }

        Player player = (Player)sender;
        if (this.isStarted) {
            player.sendMessage("Please stop the manhunt first.");
            return;
        }

        int currentTeamID = this.getPlayerTeam(player);
        if (currentTeamID == ManhuntTeam.None) {
            sender.sendMessage("You don't belong to any team already.");
            return;
        }

        this.setPlayerTeam(player, currentTeamID, ManhuntTeam.None);
        sender.sendMessage("You left your team.");
    }

    public void set(CommandSender sender, String[] args)
    {
        if (this.isStarted) {
            sender.sendMessage("Please stop the manhunt first.");
            return;
        }
        if (args.length != 3) {
            sender.sendMessage("Invalid argument count.");
            return;
        }

        String playerName = args[1];
        Player player = Bukkit.getPlayer(playerName);
        if (player == null) {
            sender.sendMessage("Could not find that player.");
            return;
        }

        int currentTeamID = this.getPlayerTeam(player);
        String teamName = args[2];
        String pmsg;
        String smsg;
        if (teamName.equals("none"))
        {
            this.setPlayerTeam(player, currentTeamID, ManhuntTeam.None);
            pmsg = "You do not belong to a team anymore.";
            smsg = playerName + " was removed from his team.";
        }
        else
        {
            int newTeamID = this.getTeamIDFromName(teamName);
            if (newTeamID == -1) {
                sender.sendMessage("Invalid team specified.");
                return;
            }

            this.setPlayerTeam(player, currentTeamID, newTeamID);
            pmsg = "You were assigned to the " + teamName + " team.";
            smsg = playerName + " was assigned to the " + teamName + " team.";
        }

        player.sendMessage(pmsg);
        if (sender != player) {
            sender.sendMessage(smsg);
        }
    }

    public void cockhunt(CommandSender sender)
    {
        if (this.isCockhunt)
        {
            sender.sendMessage("Cockhunt was disabled!");
            this.isCockhunt = false;
        }
        else
        {
            sender.sendMessage("Cockhunt was enabled!");
            this.isCockhunt = true;
        }
        this.config.set("cockhunt", this.isCockhunt);
        this.config.saveConfig();
    }

    public void toggleDebug(CommandSender sender)
    {
        if (this.isDebug)
        {
            sender.sendMessage("Debug was disabled!");
            this.isDebug = false;
        }
        else
        {
            sender.sendMessage("Debug was enabled!");
            this.isDebug = true;
        }
    }

    public void updateCompassTarget(Player hunter, Player runner)
    {
        ItemStack compass = this.getPlayerCompass(hunter);
        if (compass == null)
            return;

        if (hunter.getWorld() == runner.getWorld())
        {
            this.setCompassTarget(hunter, runner.getLocation(), compass);
        }
        else
        {
            Location loc = this.getRunnerPortal(runner.getName());
            if (loc == null) {
                return;
            }

            this.setCompassTarget(hunter, loc, compass);
        }
    }

    public void setCompassTarget(Player hunter, Location targetLoc, ItemStack compass)
    {
        hunter.setCompassTarget(targetLoc);
        if (this.compassTOO) {
            CompassMeta meta = (CompassMeta)compass.getItemMeta();
            if (hunter.getWorld().getEnvironment() != Environment.NORMAL)
                meta.setLodestone(targetLoc);
            else
                meta.setLodestone(null);
            compass.setItemMeta(meta);
        }
    }

    public ItemStack getPlayerCompass(Player player)
    {
        PlayerInventory inv = player.getInventory();

        for (int i = 0; i < inv.getSize(); i++) {
            ItemStack stack = inv.getItem(i);
            if (stack != null && stack.getType() == Material.COMPASS) {
                return stack;
            }
        }

        return null;
    }

    public void setRunnerPortal(String runnerName, Location portalLocation)
    {
        this.portals.put(runnerName, portalLocation);
        this.config.set("portals", this.portals);
        this.config.saveConfig();
    }

    public Location getRunnerPortal(String runnerName)
    {
        return this.portals.get(runnerName);
    }

    public void setHunterTarget(Player hunter, Player runner, @Nullable ItemStack compass)
    {
        this.targets.put(hunter.getName(), runner.getName());
        this.config.set("targets", this.targets);
        this.config.saveConfig();

        if (compass != null)
        {
            ItemMeta meta = compass.getItemMeta();
            meta.setDisplayName("Tracking: " + runner.getName());
            compass.setItemMeta(meta);
            compass.addUnsafeEnchantment(Enchantment.MENDING, 1);
            this.updateCompassTarget(hunter, runner);
        }
    }

    public void removeHunterTarget(Player hunter)
    {
        this.targets.remove(hunter.getName());
        this.config.set("targets", this.targets);
        this.config.saveConfig();

        ItemStack compass = this.getPlayerCompass(hunter);
        if (compass != null) {
            CompassMeta meta = (CompassMeta)compass.getItemMeta();
            meta.setDisplayName(null);
            meta.setLodestone(null);
            compass.setItemMeta(meta);
            compass.removeEnchantment(Enchantment.MENDING);
        }
    }

    public void giveHunterCompass(Player hunter, @Nullable Player target)
    {
        ItemStack compass = new ItemStack(Material.COMPASS, 1);
        CompassMeta meta = (CompassMeta)compass.getItemMeta();
        meta.setLodestoneTracked(false);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        compass.setItemMeta(meta);
        hunter.getInventory().addItem(compass);
        if (target != null) {
            this.setHunterTarget(hunter, target, this.getPlayerCompass(hunter));
        }
    }

    public Player getHunterTarget(Player hunter)
    {
        String runnerName = this.targets.get(hunter.getName());
        return runnerName == null ? null : Bukkit.getPlayer(runnerName);
    }

    public int getTeamIDFromName(String teamName)
    {
        for(int i = 0; i < 3; ++i)
        {
            if (teamName.equals(this.teamNames[i]))
                return i;
        }
        return -1;
    }

    public int getPlayerTeam(Player player)
    {
        for (int i = 0; i < 3; i++)
        {
            for (String entry : this.teams[i].getEntries())
            {
                if (entry.equals(player.getName()))
                    return i;
            }
        }
        return -1;
    }

    public boolean getPlayerInTeam(Player player, int teamID)
    {
        return this.teams[teamID].getEntries().contains(player.getName());
    }

    public void setPlayerTeam(Player player, int currentTeamID, int newTeamID)
    {
        Location spawn;
        Team team;
        if (newTeamID != ManhuntTeam.None)
        {
            if (currentTeamID == ManhuntTeam.None)
            {
                spawn = this.mvCore.getMVWorldManager().getMVWorld("manhunt").getSpawnLocation();
                this.mvCore.getSafeTTeleporter().safelyTeleport(Bukkit.getConsoleSender(), player, spawn, false);
            }
            team = this.teams[newTeamID];
            team.addEntry(player.getName());
        }
        else
        {
            if (currentTeamID != ManhuntTeam.None)
            {
                spawn = this.mvCore.getMVWorldManager().getMVWorld("lobby").getSpawnLocation();
                this.mvCore.getSafeTTeleporter().safelyTeleport(Bukkit.getConsoleSender(), player, spawn, false);
            }
            team = this.teams[currentTeamID];
            team.removeEntry(player.getName());
        }
    }

    public ArrayList<Player> getTeamPlayers(int teamID)
    {
        ArrayList<Player> players = new ArrayList<>();
        for (String entry : this.teams[teamID].getEntries())
        {
            Player player = Bukkit.getPlayer(entry);
            if (player == null)
                continue;
            players.add(player);
        }
        return players;
    }

    public ArrayList<Player> getParticipants()
    {
        ArrayList<Player> players = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            for (String entry : this.teams[i].getEntries()) {
                Player player = Bukkit.getPlayer(entry);
                if (player == null)
                    continue;
                players.add(player);
            }
        }
        return players;
    }

    public ArrayList<Player> getRunnerHunters(Player runner)
    {
        ArrayList<Player> hunters = new ArrayList<>();
        for (Player hunter : getTeamPlayers(ManhuntTeam.Hunters))
        {
            if (getHunterTarget(hunter) == runner)
                hunters.add(hunter);
        }
        return hunters;
    }

    public boolean getStarted()
    {
        return this.isStarted;
    }

    private void setStarted(boolean flag)
    {
        this.isStarted = flag;
        this.config.set("started", flag);
        this.config.saveConfig();
    }

    public boolean getCockhunt()
    {
        return this.isCockhunt;
    }

    public boolean getAlertWrongDimension()
    {
        return this.alertWrongDimension;
    }

    public String[] getTeamNames()
    {
        return this.teamNames;
    }

    private void startListeners()
    {
        this.listener = new ManhuntListener(this.plugin, this);
        this.plugin.getServer().getPluginManager().registerEvents(this.listener, this.plugin);
        if (this.isCockhunt)
        {
            this.cockListener = new CockhuntListener(this.plugin);
            this.cockListener.start();
        }
        if (this.plugin.getTrackMgr() != null /* TODO: THIS CHECK MUST BE UPDATED */)
        {
            this.mhTrackMgr = new ManhuntTrackManager(this.plugin, this);
            this.mhTrackMgr.start();
        }
    }

    private void stopListeners()
    {
        HandlerList.unregisterAll(this.listener);
        this.listener = null;
        if (this.isCockhunt)
        {
            this.cockListener.stop();
            this.cockListener = null;
        }
        if (this.mhTrackMgr != null)
        {
            this.mhTrackMgr.stop();
            this.mhTrackMgr = null;
        }
    }

    private void regenWorld(MVWorldManager worldManager, String name, String seed)
    {
        this.broadcastActionBar("Preparing \"" + name + "\"");
        worldManager.regenWorld(name, true, false, seed);
        MultiverseWorld mvWorld = worldManager.getMVWorld(name);
        World cbWorld = mvWorld.getCBWorld();
        Location loc0 = cbWorld.getSpawnLocation();
        Location loc1 = new Location(cbWorld, loc0.getX(), loc0.getY(), loc0.getZ());
        mvWorld.setSpawnLocation(loc1);
    }

    private void broadcastActionBar(String msg)
    {
        for (Player player : getParticipants())
            player.sendActionBar(msg);
    }

    public void awardPlayerAdvancement(Player player, String key)
    {
        NamespacedKey nkey = NamespacedKey.minecraft(key);
        AdvancementProgress progress = player.getAdvancementProgress(Bukkit.getAdvancement(nkey));
        for (String criteria : progress.getRemainingCriteria())
            progress.awardCriteria(criteria);
    }

    public void revokePlayerAdvancements(Player player)
    {
        Iterator<Advancement> iterator = Bukkit.getServer().advancementIterator();
        while (iterator.hasNext()) {
            AdvancementProgress progress = player.getAdvancementProgress(iterator.next());
            for (String criteria : progress.getAwardedCriteria())
                progress.revokeCriteria(criteria);
        }
    }
}
