package com.thegameratort.mammamia.gui;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class GUIIcon
{
    public interface ClickEvent {
        boolean onClick(Player player);
    }

    public ItemStack stack;
    public ClickEvent clickEvent;

    public GUIIcon(ItemStack stack)
    {
        this.stack = stack;
        this.clickEvent = null;
    }

    public GUIIcon(ItemStack stack, ClickEvent clickEvent)
    {
        this.stack = stack;
        this.clickEvent = clickEvent;
    }

    public GUIIcon(ItemStack stack, String command)
    {
        this.stack = stack;
        this.clickEvent = cmdClick(command);
    }

    public static ClickEvent cmdClick(String command)
    {
        return player -> {
            String cmd = command;
            boolean close = true;
            if (cmd.startsWith("."))
            {
                close = false;
                cmd = cmd.substring(1);
            }
            player.performCommand(cmd);
            return close;
        };
    }
}
