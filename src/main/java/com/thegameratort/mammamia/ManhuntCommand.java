package com.thegameratort.mammamia;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class ManhuntCommand implements CommandExecutor, TabExecutor
{
    private final MammaMia plugin;

    public ManhuntCommand(MammaMia plugin)
    {
        this.plugin = plugin;
        PluginCommand cmd = plugin.getCommand("manhunt");
        cmd.setExecutor(this);
        cmd.setTabCompleter(this);
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args)
    {
        if (args.length > 0)
        {
            TrackManager trackMgr;
            if (!sender.hasPermission("mm.mh." + args[0]))
            {
                sender.sendMessage(ChatColor.RED + "You do not have permission to use that command.");
                return true;
            }
            switch (args[0])
            {
                case "new":
                    this.plugin.getMhMgr().regen(sender);
                    break;
                case "start":
                    this.plugin.getMhMgr().start(sender);
                    break;
                case "resume":
                    this.plugin.getMhMgr().resume(sender);
                    break;
                case "stop":
                    this.plugin.getMhMgr().stop(sender);
                    break;
                case "join":
                    this.plugin.getMhMgr().join(sender, args);
                    break;
                case "leave":
                    this.plugin.getMhMgr().leave(sender);
                    break;
                case "set":
                    this.plugin.getMhMgr().set(sender, args);
                    break;
                case "cockhunt":
                    this.plugin.getMhMgr().cockhunt(sender);
                    break;
                case "reloadtracks":
                    trackMgr = this.plugin.getDiscordMgr().getTrackMgr();
                    if (trackMgr != null)
                        trackMgr.reload();
                    break;
                case "botvc":
                    this.plugin.getDiscordMgr().joinVC();
                    break;
            }
        }
        return true;
    }

    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args)
    {
        List<String> list = new ArrayList<>();
        switch (args.length)
        {
            case 1:
                list.add("new");
                list.add("start");
                list.add("resume");
                list.add("stop");
                list.add("join");
                list.add("set");
                list.add("botvc");
                break;
            case 2:
                switch (args[0])
                {
                    case "join":
                        Collections.addAll(list, this.plugin.getMhMgr().getTeamNames());
                        break;
                    case "set":
                        for (Player player : Bukkit.getOnlinePlayers())
                            list.add(player.getName());
                        break;
                }
                break;
            case 3:
                if (args[0].equals("set"))
                {
                    list.add("none");
                    Collections.addAll(list, this.plugin.getMhMgr().getTeamNames());
                }
                break;
        }
        return list;
    }
}