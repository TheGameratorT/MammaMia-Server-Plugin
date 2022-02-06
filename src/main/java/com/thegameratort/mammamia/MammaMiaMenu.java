package com.thegameratort.mammamia;

import com.thegameratort.mammamia.gui.GUIMenu;
import com.thegameratort.mammamia.manhunt.ManhuntMenu;
import com.thegameratort.mammamia.pvp.PvPMenu;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class MammaMiaMenu
{
    public static void open(GUIMenu parent, Player player)
    {
        GUIMenu menu = new GUIMenu(3, "Mammma Mia Menu");

        menu.setIcon(10, MenuUtils.createIcon(Material.ENDER_EYE, "Manhunt menu", "Opens the manhunt menu.", player1 -> {
            ManhuntMenu.open(menu, player1);
            return false;
        }));
        menu.setIcon(11, MenuUtils.createIcon(Material.GOLDEN_SWORD, "PvP menu", "Opens the PvP menu.", player1 -> {
            PvPMenu.open(menu, player1);
            return false;
        }));

        menu.setIcon(16, MenuUtils.createReturnIcon(parent));

        menu.open(player);
    }
}
