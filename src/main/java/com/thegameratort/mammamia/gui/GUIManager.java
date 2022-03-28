package com.thegameratort.mammamia.gui;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.ArrayList;
import java.util.Map;

public class GUIManager
{
    private static final ArrayList<GUIInstance> instances = new ArrayList<>();

    public static void openMenu(Player player, GUIMenu menu)
    {
        removeMenuInstanceIfOpen(player);
        Inventory inv = Bukkit.createInventory(null, menu.rows * 9, menu.title);
        for (Map.Entry<Integer, GUIIcon> entry : menu.icons.entrySet())
            inv.setItem(entry.getKey(), entry.getValue().stack);
        instances.add(new GUIInstance(player, inv, menu));
        player.openInventory(inv);
    }

    public static void closeMenu(Player player)
    {
        int instanceID = getMenuInstanceID(player);
        if (instanceID == -1)
            return;
        GUIInstance instance = instances.get(instanceID);
        instances.remove(instanceID);
        instance.inv.close();
        player.updateInventory();
    }

    public static int getMenuInstanceID(Player owner)
    {
        int instaceCount = instances.size();
        for (int i = 0; i < instaceCount; i++)
        {
            GUIInstance instance = instances.get(i);
            if (instance.owner == owner)
                return i;
        }
        return -1;
    }

    public static int getMenuInstanceID(Inventory inv)
    {
        int instaceCount = instances.size();
        for (int i = 0; i < instaceCount; i++)
        {
            GUIInstance instance = instances.get(i);
            if (instance.inv == inv)
                return i;
        }
        return -1;
    }

    public static GUIInstance getMenuInstance(int instanceID)
    {
        return instances.get(instanceID);
    }

    public static void removeMenuInstance(int instanceID)
    {
        instances.remove(instanceID);
    }

    private static void removeMenuInstanceIfOpen(Player player)
    {
        int instanceID = getMenuInstanceID(player);
        if (instanceID != -1)
            instances.remove(instanceID);
    }
}
