package com.thegameratort.mammamia.pvp;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.thegameratort.mammamia.DelayedExecutor;
import com.thegameratort.mammamia.MammaMia;
import com.thegameratort.mammamia.kit.KitManager;
import com.thegameratort.mammamia.kit.KitMenu;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class PvPListener implements Listener {
    private final MammaMia plugin;
    private final Server server;
    private final BukkitScheduler scheduler;
    private final MultiverseCore mvCore;
    private final PvPManager pvpMgr;

    public PvPListener(@NotNull MammaMia plugin, PvPManager manager) {
        this.plugin = plugin;
        this.server = plugin.getServer();
        this.scheduler = server.getScheduler();
        this.mvCore = plugin.getMvCore();
        this.pvpMgr = manager;
        server.getPluginManager().registerEvents(this, plugin);
    }

    public void doDuelCountdown(PvPGame game) {
        List<Player> players = game.players;
        DelayedExecutor executor = new DelayedExecutor(plugin);
        for (int i = 3; i > 0; i--) {
            String title = ChatColor.BLUE + Integer.toString(i);
            executor.add(() -> {
                for (Player player : players) {
                    player.sendTitle(title, null, 10, 70, 20);
                    player.playSound(player.getLocation(), Sound.BLOCK_STONE_BUTTON_CLICK_OFF, 1.0F, 2.0F);
                }
            }, 20);
        }
        executor.add(() -> {
            for (Player player : players) {
                String titleMsg = game.isSpleef ? "Dig!" : "Fight!";
                player.sendTitle(ChatColor.RED + titleMsg, null, 10, 20, 20);
                player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_PLACE, 1.0F, 2.0F);
            }
            game.state = PvPGameState.FIGHTING;
        }, 20);
        executor.runAll();
    }

    public void doDuelEndgame(PvPGame game, Player loser, boolean isDraw) {
        game.state = PvPGameState.ENDED;
        List<Player> players = game.players;
        if (isDraw) {
            scheduler.scheduleSyncDelayedTask(plugin, () -> {
                for (Player player : players) {
                    player.spigot().respawn();
                    player.sendTitle(ChatColor.RED + "Draw!", null, 10, 20, 20);
                }
            }, 20 * 2);
        } else {
            scheduler.scheduleSyncDelayedTask(plugin, () -> {
                loser.spigot().respawn();
            }, 20 * 2);

            DelayedExecutor executor = new DelayedExecutor(plugin);
            Player winner = players.get(players.indexOf(loser) == 1 ? 0 : 1);
            for (int i = 0; i < 16; i++) {
                executor.add(() -> {
                    winner.playSound(winner.getLocation(), Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, 1.0F, 1.0F);
                }, 5);
            }
            executor.runAll();
        }

        scheduler.scheduleSyncDelayedTask(plugin, () -> {
            for (Player oplayer : game.players) {
                sendPlayerToLobby(oplayer);
                pvpMgr.removeGame(game);
            }
        }, 20 * 8);
    }

    public void sendPlayerToLobby(Player player) {
        Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_MAX_HEALTH)).setBaseValue(20.0D);
        Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)).setBaseValue(2.0D);
        player.setHealth(20.0D);
        player.setFoodLevel(20);

        Location lobby = this.mvCore.getMVWorldManager().getMVWorld("lobby").getSpawnLocation();
        this.mvCore.getSafeTTeleporter().safelyTeleport(Bukkit.getConsoleSender(), player, lobby, false);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        PvPGame game = pvpMgr.getPlayerGame(player);
        if (game == null) {
            return;
        }

        if (game.state != PvPGameState.WAITING) {
            return;
        }

        if (player.getInventory().getItemInMainHand().getType() == Material.BOOK) {
            KitManager kitMgr = plugin.getKitMgr();
            KitMenu.open(null, player, kitMgr.getKits(), kit -> {
                game.playersReady++;
                if (game.playersReady == game.players.size()) {
                    doDuelCountdown(game);
                }
            });
        }
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        PvPGame game = pvpMgr.getPlayerGame(player);
        if (game == null) {
            return;
        }

        if (game.state == PvPGameState.WAITING) {
            Location from = event.getFrom();
            Location to = event.getTo();
            from.setPitch(to.getPitch());
            from.setYaw(to.getYaw());
            event.setTo(from);
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        PvPGame game = pvpMgr.getPlayerGame(player);
        if (game == null) {
            return;
        }

        if (game.state != PvPGameState.FIGHTING) {
            event.setCancelled(true);
            return;
        } else if (game.isSpleef) {
            player.sendMessage(ChatColor.RED + "You can't place blocks during spleef matches!");
            event.setCancelled(true);
            return;
        }

        Vector block = event.getBlock().getLocation().toVector();
        if (!block.isInAABB(game.arenaMinPos, game.arenaMaxPos)) {
            player.sendMessage(ChatColor.RED + "You can't place blocks there!");
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        PvPGame game = pvpMgr.getPlayerGame(player);
        if (game == null) {
            return;
        }

        if (game.state != PvPGameState.FIGHTING) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        PvPGame game = pvpMgr.getPlayerGame(player);
        if (game == null) {
            return;
        }

        event.getDrops().clear();

        if (game.type == PvPGameType.DUEL) {
            game.deadPlayerCount++;
            if (game.deadPlayerCount == 2) {
                scheduler.cancelTask(game.endGameTaskID);
                doDuelEndgame(game, player, true);
            } else {
                game.endGameTaskID = scheduler.scheduleSyncDelayedTask(plugin, () -> {
                    doDuelEndgame(game, player, false);
                }, 1);
            }
        }
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        PvPGame game = pvpMgr.getPlayerGame(player);
        if (game == null) {
            return;
        }

        PvPArena arena = pvpMgr.getArenaByID(game.arenaID);

        World world = server.getWorld(pvpMgr.getWorldForEnv(arena.env));
        Location spawn = PvPManager.getArenaSpawnPos(arena.respawnPos, world, arena.arenaMinPos, game.arenaMinPos);

        player.setGameMode(GameMode.SPECTATOR);

        event.setRespawnLocation(spawn);
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        PvPGame game = pvpMgr.getPlayerGame(player);
        if (game == null) {
            return;
        }

        if (game.state != PvPGameState.FIGHTING) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerBucketEmpty(PlayerBucketEmptyEvent event) {
        Player player = event.getPlayer();
        PvPGame game = pvpMgr.getPlayerGame(player);
        if (game == null) {
            return;
        }

        if (game.state != PvPGameState.FIGHTING) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PvPGame game = pvpMgr.getPlayerGame(player);
        if (game == null) {
            return;
        }

        game.state = PvPGameState.ENDED;
        doDuelEndgame(game, player, false);
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (!(entity instanceof Player)) {
            return;
        }

        Player player = (Player) entity;
        PvPGame game = pvpMgr.getPlayerGame(player);
        if (game == null) {
            return;
        }

        if (game.state == PvPGameState.ENDED) {
            event.setCancelled(true);
            return;
        }

        if (game.isSpleef) {
            if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
                event.setDamage(1000.0D);
            }
        }
    }

    @EventHandler
    public void onEntityShootBow(EntityShootBowEvent event) {
        Entity shooter = event.getEntity();
        if (!(shooter instanceof Player)) {
            return;
        }

        Player player = (Player) shooter;
        PvPGame game = pvpMgr.getPlayerGame(player);
        if (game == null) {
            return;
        }

        if (game.state == PvPGameState.WAITING) {
            event.setCancelled(true);
        }
    }
}
