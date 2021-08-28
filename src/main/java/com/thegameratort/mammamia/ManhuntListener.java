package com.thegameratort.mammamia;

import java.util.ArrayList;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

public class ManhuntListener implements Listener
{
    private final MammaMia plugin;
    private final ManhuntManager mhMgr;
    private final ArrayList<Inventory> invs = new ArrayList<>();

    ManhuntListener(MammaMia plugin)
    {
        this.plugin = plugin;
        this.mhMgr = plugin.getMhMgr();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event)
    {
        Player player = event.getPlayer();
        if (player.getInventory().getItemInMainHand().getType() == Material.COMPASS)
        {
            if (!this.mhMgr.getPlayerInTeam(player, ManhuntTeam.Hunters))
                return;

            Inventory inv = Bukkit.createInventory(null, 9, "Select a player to track");
            for (Player runner : this.mhMgr.getTeamPlayers(ManhuntTeam.Runners))
            {
                ItemStack stack = new ItemStack(Material.PLAYER_HEAD, 1);
                SkullMeta meta = (SkullMeta)stack.getItemMeta();
                meta.setOwningPlayer(runner);
                meta.setDisplayName(runner.getName());
                stack.setItemMeta(meta);
                inv.addItem(stack);
            }
            player.openInventory(inv);
            this.invs.add(inv);
        }
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
            this.mhMgr.setHunterTarget(hunter, (Player)runner, this.mhMgr.getPlayerCompass(hunter));
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
        if (this.mhMgr.getPlayerInTeam(player, ManhuntTeam.Hunters) && material == Material.COMPASS)
            event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event)
    {
        Player runner = event.getPlayer();
        if (!this.mhMgr.getPlayerInTeam(runner, ManhuntTeam.Runners))
            return;
        for (Player hunter : this.mhMgr.getTeamPlayers(ManhuntTeam.Hunters))
        {
            if (runner == this.mhMgr.getHunterTarget(hunter))
                this.mhMgr.updateCompassTarget(hunter, runner);
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event)
    {
        Player player = event.getPlayer();
        if (this.mhMgr.getPlayerInTeam(player, ManhuntTeam.Hunters))
        {
            Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, () -> {
                this.mhMgr.giveHunterCompass(player, this.mhMgr.getHunterTarget(player));
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
                    Player runner = this.mhMgr.getHunterTarget(p);
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
                    for (Player hunter : this.mhMgr.getRunnerHunters(p)) {
                        if (dw != hunter.getWorld())
                            hunter.sendActionBar("Your target left your dimension.");
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
        switch (teamID)
        {
            case ManhuntTeam.Hunters:
                event.getDrops().removeIf(i -> (i.getType() == Material.COMPASS));
                break;
            case ManhuntTeam.Runners:
                for (Player hunter : this.mhMgr.getRunnerHunters(player))
                    this.mhMgr.removeHunterTarget(hunter);
                player.setGameMode(GameMode.SPECTATOR);
                Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, () -> {
                    this.mhMgr.setPlayerTeam(player, teamID, ManhuntTeam.Spectators);
                }, 1);
                break;
        }
    }
}
