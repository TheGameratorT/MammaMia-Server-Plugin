package com.thegameratort.mammamia.kit;

import com.thegameratort.mammamia.MenuUtils;
import com.thegameratort.mammamia.gui.GUIIcon;
import com.thegameratort.mammamia.gui.GUIMenu;
import org.bukkit.entity.Player;

import java.util.List;

public class KitMenu {
    public interface KitSelectEvent {
        void onKitSelected(Kit kit);
    }

    public static void open(GUIMenu parent, Player player, List<Kit> kits, KitSelectEvent event) {
        int kitCount = kits.size();
        int rows = 2 + (kitCount + 5 - 1) / 5;

        GUIMenu menu = new GUIMenu(rows, "Kit Menu");

        int slot = 10;
        int rowc = 0;
        for (Kit kit : kits) {
            menu.setIcon(slot, new GUIIcon(kit.icon, (player1) -> {
                kit.giveToPlayer(player1);
                if (event != null) {
                    event.onKitSelected(kit);
                }
                return true;
            }));
            rowc++;
            if (rowc > 4) {
                rowc = 0;
                slot += 5;
            }
            else {
                slot++;
            }
        }

        menu.setIcon(16, MenuUtils.createReturnIcon(parent));

        menu.open(player);
    }
}
