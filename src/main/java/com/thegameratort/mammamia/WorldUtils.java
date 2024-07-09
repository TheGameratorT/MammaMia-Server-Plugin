package com.thegameratort.mammamia;

import com.onarandombox.MultiverseCore.MVWorld;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.advancement.AdvancementProgress;
import org.bukkit.entity.Player;

import java.util.Iterator;

public class WorldUtils {
	public static void fixWorldSpawnLocation(MultiverseWorld mvWorld) {
		World cbWorld = mvWorld.getCBWorld();
		Location loc0 = cbWorld.getSpawnLocation();
		Location loc1 = new Location(cbWorld, loc0.getX(), loc0.getY(), loc0.getZ());
		mvWorld.setSpawnLocation(loc1);
	}

	public static void awardPlayerAdvancement(Player player, String key) {
		NamespacedKey nkey = NamespacedKey.minecraft(key);
		AdvancementProgress progress = player.getAdvancementProgress(Bukkit.getAdvancement(nkey));
		for (String criteria : progress.getRemainingCriteria()) {
			progress.awardCriteria(criteria);
		}
	}

	public static void revokePlayerAdvancements(Player player) {
		Iterator<Advancement> iterator = Bukkit.getServer().advancementIterator();
		while (iterator.hasNext()) {
			AdvancementProgress progress = player.getAdvancementProgress(iterator.next());
			for (String criteria : progress.getAwardedCriteria()) {
				progress.revokeCriteria(criteria);
			}
		}
	}
}
