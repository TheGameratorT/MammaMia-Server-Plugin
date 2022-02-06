package com.thegameratort.mammamia.gui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public class GUIInstance
{
    public final Player owner;
    public final Inventory inv;
    public final GUIMenu menu;

    public GUIInstance(Player owner, Inventory inv, GUIMenu menu)
    {
        this.owner = owner;
        this.inv = inv;
        this.menu = menu;
    }
}
