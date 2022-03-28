package com.thegameratort.mammamia.gui;

import org.bukkit.entity.Player;

import java.util.HashMap;

public class GUIMenu
{
    public int rows;
    public String title;
    public HashMap<Integer, GUIIcon> icons = new HashMap<>();

    public GUIMenu(int rows, String title)
    {
        this.rows = rows;
        this.title = title;
    }

    public void setIcon(int slot, GUIIcon icon)
    {
        icons.put(slot, icon);
    }

    public GUIIcon getIcon(int slot)
    {
        return icons.get(slot);
    }

    public void open(Player player)
    {
        GUIManager.openMenu(player, this);
    }
}
