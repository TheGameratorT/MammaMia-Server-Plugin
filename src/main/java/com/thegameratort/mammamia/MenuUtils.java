package com.thegameratort.mammamia;

import com.thegameratort.mammamia.gui.GUIIcon;
import com.thegameratort.mammamia.gui.GUIMenu;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Collections;

public class MenuUtils
{
    public static GUIIcon createIcon(Material material, String name, String lore, GUIIcon.ClickEvent clickEvent)
    {
        ItemStack stack = new ItemStack(material);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(defNameFmt(name));
        meta.setLore(Collections.singletonList(defLoreFmt(lore)));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        stack.setItemMeta(meta);
        return new GUIIcon(stack, clickEvent);
    }

    public static GUIIcon createIcon(Material material, String name, String lore, String command)
    {
        return createIcon(material, name, lore, GUIIcon.cmdClick(command));
    }

    public static GUIIcon createHeadIcon(Player owner, GUIIcon.ClickEvent clickEvent)
    {
        ItemStack stack = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) stack.getItemMeta();
        meta.setOwningPlayer(owner);
        meta.setDisplayName(defNameFmt(owner.getName()));
        stack.setItemMeta(meta);
        return new GUIIcon(stack, clickEvent);
    }

    public static GUIIcon createReturnIcon(GUIMenu parent)
    {
        if (parent == null)
            return createIcon(Material.RED_BED, "Close", "Closes the menu.", player -> true);
        else
            return createIcon(Material.RED_BED, "Return", "Returns to the previous menu.", player -> {
                parent.open(player);
                return false;
            });
    }

    // Formats the name with the default menu color
    public static String defNameFmt(String title)
    {
        return ChatColor.RESET + ChatColor.YELLOW.toString() + ChatColor.BOLD + title;
    }

    // Formats the lore with the default menu color
    public static String defLoreFmt(String lore)
    {
        return ChatColor.RESET + ChatColor.GRAY.toString() + lore;
    }
}
