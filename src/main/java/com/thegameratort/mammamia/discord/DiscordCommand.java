package com.thegameratort.mammamia.discord;

import com.thegameratort.mammamia.MammaMia;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class DiscordCommand implements CommandExecutor, TabExecutor {
    private final DiscordManager manager;

    @SuppressWarnings("ConstantConditions")
    public DiscordCommand(@NotNull MammaMia plugin, DiscordManager manager) {
        this.manager = manager;
        PluginCommand cmd = plugin.getCommand("discord");
        cmd.setExecutor(this);
        cmd.setTabCompleter(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length > 0) {
            String subCommand = args[0];
            if (!sender.hasPermission("mm.discord." + subCommand)) {
                sender.sendMessage(ChatColor.RED + "You do not have permission to use that command.");
                return true;
            }
            switch (subCommand) {
                case "joinVC" -> manager.joinVC();
                case "leaveVC" -> manager.leaveVC();
                case "startup" -> manager.startup();
                case "shutdown" -> manager.shutdown();
                case "enable" -> manager.enable(sender);
                case "disable" -> manager.disable();
                case "set" -> {
                    if (args.length > 2) {
                        String param = args[1];
                        String value = args[2];
                        switch (param) {
                            case "botToken" -> manager.setBotToken(sender, value);
                            case "botStatus" -> manager.setBotStatus(value);
                            case "guildID" -> setLongParam(sender, param, value, manager::setGuildID);
                            case "voiceChannelID" -> setLongParam(sender, param, value, manager::setVoiceChannelID);
                            default -> sender.sendMessage(ChatColor.RED + "Invalid parameter \"" + param + "\"");
                        }
                    }
                }
            }
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        List<String> list = new ArrayList<>();
        int argCount = args.length;
        if (argCount == 1) {
            list.add("joinVC");
            list.add("leaveVC");
            list.add("set");
            list.add("startup");
            list.add("shutdown");
            list.add("enable");
            list.add("disable");
        }
        else if (argCount > 1) {
            String subCmd = args[0];
            if (subCmd.equals("set")) {
                switch (argCount) {
                    case 2 -> {
                        list.add("botToken");
                        list.add("botStatus");
                        list.add("guildID");
                        list.add("voiceChannelID");
                    }
                    case 3 -> {
                        list.add("<botToken>");
                        list.add("<botStatus>");
                        list.add("<guildID>");
                        list.add("<voiceChannelID>");
                    }
                }
            }
        }
        return list;
    }

    private interface ISetLong {
        void run(CommandSender sender, long value);
    }

    private void setLongParam(CommandSender sender, String param, String sID, ISetLong func) {
        long ID;
        try {
            ID = Long.parseLong(sID);
        } catch (Exception ex) {
            sender.sendMessage("Invalid value for parameter \"" + param + "\", expected a long integer.");
            return;
        }
        func.run(sender, ID);
    }
}
