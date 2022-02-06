package com.thegameratort.mammamia.pvp;

import com.thegameratort.mammamia.MenuUtils;
import com.thegameratort.mammamia.gui.GUIMenu;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class PvPMenu {
    public static void open(GUIMenu parent, Player player) {
        GUIMenu menu = new GUIMenu(3, "PvP Menu");

        menu.setIcon(10, MenuUtils.createIcon(
                Material.IRON_SWORD,
                "Duel",
                "Fight against another player.",
                player1 -> {
                    return false;
                }));

        menu.setIcon(11, MenuUtils.createIcon(
                Material.DIAMOND_SWORD,
                "Free For All",
                "Fight against multiple players.",
                player1 -> {
                    return false;
                }));

        menu.setIcon(16, MenuUtils.createReturnIcon(parent));

        menu.open(player);
    }

    public static void openDuels(GUIMenu parent, Player player) {

    }

    public static void openFFA(GUIMenu parent, Player player) {

    }
}
