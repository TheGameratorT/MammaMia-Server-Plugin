package com.thegameratort.mammamia.track;

import com.thegameratort.mammamia.MammaMia;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TrackCommand implements CommandExecutor, TabExecutor {
    private final MammaMia plugin;
    private final TrackManager manager;

    @SuppressWarnings("ConstantConditions")
    public TrackCommand(@NotNull MammaMia plugin, TrackManager manager) {
        this.plugin = plugin;
        this.manager = manager;
        PluginCommand cmd = plugin.getCommand("track");
        cmd.setExecutor(this);
        cmd.setTabCompleter(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length > 0) {
            String subCommand = args[0];
            if (!sender.hasPermission("mm.track." + subCommand)) {
                sender.sendMessage(ChatColor.RED + "You do not have permission to use that command.");
                return true;
            }
            switch (subCommand) {
                case "play" -> {
                    if (args.length > 2) {
                        String offset = args[2];
                        long offsetInt;
                        try { offsetInt = Long.parseLong(offset); } catch (Exception e) {
                            sender.sendMessage(ChatColor.RED + "Invalid argument for offset, expected a number.");
                            return true;
                        }
                        manager.startTrack(args[1], offsetInt);
                    }
                }
                case "stop" -> manager.stopTrack();
                case "reload" -> manager.reload();
            }
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> list = new ArrayList<>();
        int argCount = args.length;
        if (argCount == 1) {
            list.add("play");
            list.add("stop");
            list.add("reload");
        }
        else if (argCount > 1) {
            String subCmd = args[0];
            switch (subCmd) {
                case "play" -> {
                    switch (argCount) {
                        case 2 -> list.addAll(manager.getTrackNames());
                        case 3 -> list.add("<offset>");
                        case 4 -> list.add("<looping>");
                    }
                }
            }
        }
        return list;
    }
}
