package com.thegameratort.mammamia.manhunt;

import com.thegameratort.mammamia.MammaMia;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;

public class ManhuntListener implements Listener
{
    private final MammaMia plugin;
    private final ManhuntManager mhMgr;
    private final ArrayList<Inventory> invs = new ArrayList<>();

    static class TrackingTarget
    {
        Player player;
        int teamID;

        TrackingTarget(Player player, int teamID)
        {
            this.player = player;
            this.teamID = teamID;
        }
    };

    ManhuntListener(MammaMia plugin, ManhuntManager mhMgr)
    {
        this.plugin = plugin;
        this.mhMgr = mhMgr;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        Player player = event.getPlayer();
        if (player.getInventory().getItemInMainHand().getType() == Material.COMPASS)
        {
            int teamID = this.mhMgr.getPlayerTeam(player);
            if (teamID != ManhuntTeam.Hunters && teamID != ManhuntTeam.Runners)
                return;

            Inventory inv = Bukkit.createInventory(null, 9, "Select a player to track");
            for (TrackingTarget target : this.getAvailableTargets(player, teamID))
            {
                ItemStack stack = new ItemStack(Material.PLAYER_HEAD, 1);
                SkullMeta meta = (SkullMeta)stack.getItemMeta();
                meta.setOwningPlayer(target.player);
                meta.setDisplayName(this.mhMgr.getTeamColor(target.teamID) + target.player.getName());
                stack.setItemMeta(meta);
                inv.addItem(stack);
            }
            player.openInventory(inv);
            this.invs.add(inv);
        }
    }

    private ArrayList<TrackingTarget> getAvailableTargets(Player player, int teamID)
    {
        // Get the available targets in order.
        // Trackable runners show before hunters do and runners can't track hunters.

        ArrayList<TrackingTarget> targets = new ArrayList<>();

        ArrayList<Player> hunters = this.mhMgr.getTeamPlayers(ManhuntTeam.Hunters);
        ArrayList<Player> runners = this.mhMgr.getTeamPlayers(ManhuntTeam.Runners);

        for (Player runner : runners)
        {
            if (runner != player)
                targets.add(new TrackingTarget(runner, ManhuntTeam.Runners));
        }

        if (teamID == ManhuntTeam.Hunters)
        {
            for (Player hunter : hunters)
            {
                if (hunter != player)
                    targets.add(new TrackingTarget(hunter, ManhuntTeam.Hunters));
            }
        }

        return targets;
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event)
    {
        Inventory inv = event.getInventory();
        this.invs.remove(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event)
    {
        Inventory inv = event.getInventory();
        if (!this.invs.contains(inv))
            return;

        Player hunter = (Player)event.getWhoClicked();
        ItemStack item = event.getCurrentItem();
        if (item == null)
            return;

        ItemMeta imeta = item.getItemMeta();
        if (!(imeta instanceof SkullMeta))
        {
            event.setCancelled(true);
            return;
        }

        SkullMeta smeta = (SkullMeta)imeta;
        OfflinePlayer runner = smeta.getOwningPlayer();
        String runnerName = runner.getName();

        if (runner instanceof Player)
        {
            this.mhMgr.setPlayerTarget(hunter, (Player)runner, this.mhMgr.getPlayerCompass(hunter));
            hunter.sendMessage("Compass is now tracking " + runnerName);
        }
        else
        {
            hunter.sendMessage("That player who you call " + runnerName + " is a ghost.");
        }

        event.setCancelled(true);
        hunter.closeInventory();
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event)
    {
        Player player = event.getPlayer();
        Material material = event.getItemDrop().getItemStack().getType();
        if (material == Material.COMPASS)
        {
            int teamID = this.mhMgr.getPlayerTeam(player);
            if (teamID == ManhuntTeam.Hunters || teamID == ManhuntTeam.Runners)
                event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event)
    {
        Player target = event.getPlayer();
        for (Player player : this.mhMgr.getParticipants())
        {
            if (this.mhMgr.getPlayerInTeam(player, ManhuntTeam.Spectators))
                continue;
            if (target == this.mhMgr.getPlayerTarget(player))
                this.mhMgr.updateCompassTarget(player, target);
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event)
    {
        Player player = event.getPlayer();
        if (this.mhMgr.getPlayerInTeam(player, ManhuntTeam.Hunters))
        {
            Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, () -> {
                this.mhMgr.giveTrackingCompass(player, this.mhMgr.getPlayerTarget(player));
            }, 1);
        }
    }

    @EventHandler
    public void onPlayerEnterPortal(PlayerPortalEvent event)
    {
        Player p = event.getPlayer();
        World dw = event.getTo().getWorld();
        String pn = p.getName();
        String dn = dw.getName();
        this.mhMgr.setRunnerPortal(pn, event.getFrom());
        if (this.mhMgr.getAlertWrongDimension())
        {
            int teamID = this.mhMgr.getPlayerTeam(p);
            switch(teamID)
            {
                case ManhuntTeam.Hunters:
                    Player runner = this.mhMgr.getPlayerTarget(p);
                    if (runner != null)
                    {
                        if (dw == runner.getWorld())
                        {
                            Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, () -> {
                                this.mhMgr.updateCompassTarget(p, runner);
                            }, 1);
                        }
                        else
                        {
                            p.sendActionBar("Your target is not in that dimension.");
                        }
                    }
                    break;
                case ManhuntTeam.Runners:
                    for (Player hunter : this.mhMgr.getPlayersTrackingPlayer(p)) {
                        // only hunters get this message
                        if (this.mhMgr.getPlayerInTeam(hunter, ManhuntTeam.Hunters)) {
                            if (dw != hunter.getWorld())
                                hunter.sendActionBar("Your target left your dimension.");
                        }
                    }
                    break;
            }
        }

        if (dn.equals("manhunt_nether"))
            this.mhMgr.awardPlayerAdvancement(p, "story/enter_the_nether");
        else if (dn.equals("manhunt_the_end"))
            this.mhMgr.awardPlayerAdvancement(p, "story/enter_the_end");
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event)
    {
        Player player = event.getEntity();
        int teamID = this.mhMgr.getPlayerTeam(player);

        if (teamID == ManhuntTeam.Spectators)
            return;

        event.getDrops().removeIf(i -> (i.getType() == Material.COMPASS));

        if (teamID == ManhuntTeam.Runners)
        {
            for (Player hunter : this.mhMgr.getPlayersTrackingPlayer(player))
                this.mhMgr.clearPlayerTarget(hunter);

            player.setGameMode(GameMode.SPECTATOR);
            Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, () -> {
                this.mhMgr.setPlayerTeam(player, teamID, ManhuntTeam.Spectators);
            }, 1);
        }
    }
}
