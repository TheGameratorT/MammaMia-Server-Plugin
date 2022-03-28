package com.thegameratort.mammamia.manhunt;

import com.thegameratort.mammamia.MenuUtils;
import com.thegameratort.mammamia.gui.GUIMenu;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.Collection;

public class ManhuntMenu
{
    public static void open(GUIMenu parent, Player player)
    {
        GUIMenu menu = new GUIMenu(3, "Manhunt Menu");

        menu.setIcon(10, MenuUtils.createIcon(Material.GRASS_BLOCK, "New manhunt", "Regenerates the manhunt world.", "manhunt new"));
        menu.setIcon(11, MenuUtils.createIcon(Material.OAK_SAPLING, "Start manhunt", "Starts the manhunt.", "manhunt start"));
        menu.setIcon(12, MenuUtils.createIcon(Material.DIAMOND_AXE, "Resume manhunt", "Resumes the manhunt.", "manhunt resume"));
        menu.setIcon(13, MenuUtils.createIcon(Material.BARRIER, "Stop manhunt", "Stops the manhunt.", "manhunt stop"));
        menu.setIcon(14, MenuUtils.createIcon(Material.IRON_HELMET, "Select team", "Opens the manhunt menu team.", player1 -> {
            openSetTeamMenu(menu, player1);
            return false;
        }));

        menu.setIcon(16, MenuUtils.createReturnIcon(parent));

        menu.open(player);
    }

    private static void openChooseTeamMenu(GUIMenu parent, Player owner, Player target)
    {
        GUIMenu menu = new GUIMenu(3, "Manhunt Team Menu");

        String joinHuntersCmd, joinRunnersCmd, joinSpectatorsCmd, leaveCmd, leaveText;
        if (owner == target)
        {
            joinHuntersCmd = "manhunt join hunters";
            joinRunnersCmd = "manhunt join runners";
            joinSpectatorsCmd = "manhunt join spectators";
            leaveCmd = "manhunt leave";
            leaveText = "Leave";
        }
        else
        {
            String targetName = target.getName();
            joinHuntersCmd = "manhunt set " + targetName + " hunters";
            joinRunnersCmd = "manhunt set " + targetName + " runners";
            joinSpectatorsCmd = "manhunt set " + targetName + " spectators";
            leaveCmd = "manhunt set " + targetName + " none";
            leaveText = "None";
        }

        menu.setIcon(10, MenuUtils.createIcon(Material.IRON_AXE, "Hunter", "Joins the hunters team.", joinHuntersCmd));
        menu.setIcon(11, MenuUtils.createIcon(Material.ENDER_EYE, "Runner", "Joins the runners team.", joinRunnersCmd));
        menu.setIcon(12, MenuUtils.createIcon(Material.WARPED_ROOTS, "Spectator", "Joins the spectators team.", joinSpectatorsCmd));
        menu.setIcon(14, MenuUtils.createIcon(Material.BARRIER, leaveText, "Leaves the manhunt.", leaveCmd));
        menu.setIcon(16, MenuUtils.createReturnIcon(parent));

        menu.open(owner);
    }

    private static void openSetTeamMenu(GUIMenu parent, Player player)
    {
        if (!player.hasPermission("mm.mh.set"))
        {
            openChooseTeamMenu(parent, player, player);
            return;
        }

        Collection<? extends Player> oplayers = Bukkit.getOnlinePlayers();
        int playerCount = oplayers.size();
        int rows = 2 + (playerCount + 5 - 1) / 5;

        GUIMenu menu = new GUIMenu(rows, "Manhunt Team Menu");

        int slot = 10;
        int rowc = 0;
        for (Player oplayer : oplayers)
        {
            menu.setIcon(slot, MenuUtils.createHeadIcon(oplayer, player1 -> {
                openChooseTeamMenu(menu, player1, oplayer);
                return false;
            }));
            rowc++;
            if (rowc > 4)
            {
                rowc = 0;
                slot += 5;
            }
            else
            {
                slot++;
            }
        }

        menu.setIcon(16, MenuUtils.createReturnIcon(parent));

        menu.open(player);
    }
}
