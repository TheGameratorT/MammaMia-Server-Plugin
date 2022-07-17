package com.thegameratort.mammamia;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class MammaMiaCommand implements CommandExecutor, TabExecutor
{
    @SuppressWarnings("ConstantConditions")
    public MammaMiaCommand(MammaMia plugin)
    {
        PluginCommand cmd = plugin.getCommand("mammamia");
        cmd.setExecutor(this);
        cmd.setTabCompleter(this);
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args)
    {
        if (args.length > 0)
        {
            if (!sender.hasPermission("mm." + args[0]))
            {
                sender.sendMessage(ChatColor.RED + "You do not have permission to use that command.");
                return true;
            }
            if (args[0].equals("menu"))
            {
                if (sender instanceof Player)
                    MammaMiaMenu.open(null, (Player) sender);
            }
            else if (args[0].equals("respawn"))
            {
                if (args.length < 2)
                {
                    sender.sendMessage(ChatColor.RED + "You need to specify a player to respawn.");
                }
                else
                {
                    Player target = Bukkit.getPlayer(args[1]);
                    if (target != null)
                    {
                        if (!target.isDead())
                            target.setHealth(0);
                        target.spigot().respawn();
                    }
                    else
                    {
                        sender.sendMessage(ChatColor.RED + "Could not find that player.");
                    }
                }
            }
        }
        return true;
    }

    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args)
    {
        List<String> list = new ArrayList<>();
        if (args.length == 1)
        {
            list.add("menu");
            list.add("respawn");
        }
        else if (args.length == 2)
        {
            if (args[0].equals("respawn"))
            {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    list.add(player.getName());
                }
            }
        }
        return list;
    }
}
