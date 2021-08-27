package com.thegameratort.mammamia;

import java.util.ArrayList;
import java.util.Collection;
import javax.annotation.Nullable;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class LoadingScreen
{
    private final MammaMia plugin;
    private final ArrayList<Integer> taskIDs = new ArrayList<>();
    private Collection<? extends Player> players;

    public LoadingScreen(MammaMia plugin)
    {
        this.plugin = plugin;
    }

    public void show(Collection<? extends Player> players, @Nullable Runnable afterShow)
    {
        Location location = this.plugin.getMvCore().getAnchorManager().getAnchorLocation("loading_screen");
        if (location == null) {
            this.plugin.getLogger().warning("Could not find \"loading_screen\" anchor, skipping loading screen...");
            if (afterShow != null) {
                afterShow.run();
            }
            return;
        }

        this.players = players;
        Entity armorStand = this.getArmorStand(location.getWorld());

        for (Player player : players) {
            this.plugin.getMvCore().getSafeTTeleporter().safelyTeleport(Bukkit.getConsoleSender(), player, location, false);
        }

        Bukkit.getScheduler().scheduleSyncDelayedTask(this.plugin, () -> {
            for (Player player : this.players) {
                player.setGameMode(GameMode.SPECTATOR);
                if (armorStand != null) {
                    int taskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(this.plugin, () -> {
                        player.setSpectatorTarget(armorStand);
                    }, 0L, 1L);
                    this.taskIDs.add(taskID);
                }
            }

            if (afterShow != null) {
                afterShow.run();
            }
        }, 1);
    }

    public void close()
    {
        for (int taskID : this.taskIDs) {
            Bukkit.getScheduler().cancelTask(taskID);
        }
        for (Player player : this.players) {
            player.setSpectatorTarget(null);
        }
        this.taskIDs.clear();
    }

    private Entity getArmorStand(World world)
    {
        for (Entity entity : world.getEntities()) {
            if (entity.getName().equals("load_spect"))
                return entity;
        }
        return null;
    }
}
