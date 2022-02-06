package com.thegameratort.mammamia.gui;

import com.thegameratort.mammamia.MammaMia;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;

public class GUIListener implements Listener
{
    public GUIListener(MammaMia plugin)
    {
        Server server = plugin.getServer();
        server.getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event)
    {
        Inventory inv = event.getInventory();
        int instanceID = GUIManager.getMenuInstanceID(inv);
        if (instanceID == -1)
            return;

        GUIInstance instance = GUIManager.getMenuInstance(instanceID);
        Player player = instance.owner;
        GUIIcon icon = instance.menu.getIcon(event.getRawSlot());

        boolean close = false;
        if (icon != null)
        {
            if (icon.clickEvent != null)
                close = icon.clickEvent.onClick(player);
        }

        event.setCancelled(true);
        if (close)
            GUIManager.closeMenu(player);
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event)
    {
        Inventory inv = event.getInventory();
        int instanceID = GUIManager.getMenuInstanceID(inv);
        if (instanceID != -1)
            GUIManager.removeMenuInstance(instanceID);
    }
}
