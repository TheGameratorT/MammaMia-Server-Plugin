package com.thegameratort.mammamia.manhunt;

import com.thegameratort.mammamia.MammaMia;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ManhuntCommand implements CommandExecutor, TabExecutor {
    private final ManhuntManager manager;

    @SuppressWarnings("ConstantConditions")
    public ManhuntCommand(MammaMia plugin, ManhuntManager manager) {
        this.manager = manager;
        PluginCommand cmd = plugin.getCommand("manhunt");
        cmd.setExecutor(this);
        cmd.setTabCompleter(this);
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (args.length > 0) {
            if (!sender.hasPermission("mm.mh." + args[0])) {
                sender.sendMessage(ChatColor.RED + "You do not have permission to use that command.");
                return true;
            }
            switch (args[0]) {
                case "new" -> manager.regen(sender);
                case "start" -> manager.start(sender);
                case "resume" -> manager.resume(sender);
                case "stop" -> manager.stop(sender);
                case "join" -> manager.join(sender, args);
                case "leave" -> manager.leave(sender);
                case "set" -> manager.set(sender, args);
                case "cockhunt" -> manager.cockhunt(sender);
                case "debug" -> manager.toggleDebug(sender);
                case "menu" -> {
                    if (sender instanceof Player player)
                        ManhuntMenu.open(null, player);
                }
            }
        }
        return true;
    }

    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        List<String> list = new ArrayList<>();
        int argCount = args.length;
        if (argCount == 1) {
            list.add("new");
            list.add("start");
            list.add("resume");
            list.add("stop");
            list.add("join");
            list.add("leave");
            list.add("set");
            list.add("cockhunt");
            list.add("debug");
            list.add("menu");
        }
        else if (argCount > 1) {
            String subCmd = args[0];
            switch (subCmd) {
                case "join" -> {
                    if (argCount == 2) {
                        Collections.addAll(list, manager.getTeamNames());
                    }
                }
                case "set" -> {
                    switch (argCount) {
                        case 2 -> {
                            for (Player player : Bukkit.getOnlinePlayers()) {
                                list.add(player.getName());
                            }
                        }
                        case 3 -> {
                            list.add("none");
                            Collections.addAll(list, manager.getTeamNames());
                        }
                    }
                }
            }
        }
        return list;
    }
}
