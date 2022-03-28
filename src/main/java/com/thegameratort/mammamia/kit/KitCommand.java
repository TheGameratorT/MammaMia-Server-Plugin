package com.thegameratort.mammamia.kit;

import com.thegameratort.mammamia.MammaMia;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class KitCommand implements CommandExecutor, TabExecutor  {
    private final KitManager kitMgr;

    @SuppressWarnings("ConstantConditions")
    public KitCommand(@NotNull MammaMia plugin, KitManager manager) {
        this.kitMgr = manager;
        PluginCommand cmd = plugin.getCommand("kit");
        cmd.setExecutor(this);
        cmd.setTabCompleter(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        int argCount = args.length;
        if (argCount > 0) {
            String subCommand = args[0];
            if (!sender.hasPermission("mm.kit." + subCommand)) {
                sender.sendMessage(ChatColor.RED + "You do not have permission to use that command.");
                return true;
            }
            switch (subCommand) {
                case "menu" -> {
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(ChatColor.RED + "Command must be executed by a player.");
                        return true;
                    }
                    KitMenu.open(null, (Player) sender, kitMgr.getKits(), null);
                }
                case "list" -> {
                    kitMgr.listKits(sender);
                }
                case "claim" -> {
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(ChatColor.RED + "Command must be executed by a player.");
                        return true;
                    }
                    if (argCount != 2) {
                        sender.sendMessage(ChatColor.RED + "Incorrect argument count.");
                        return true;
                    }
                    String kitName = args[1];
                    Kit kit = kitMgr.getKitByName(kitName);
                    if (kit == null) {
                        sender.sendMessage(ChatColor.RED + "That kit does not exist.");
                        return true;
                    }
                    kit.giveToPlayer((Player) sender);
                }
                case "give" -> {
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(ChatColor.RED + "Command must be executed by a player.");
                        return true;
                    }
                    if (argCount != 3) {
                        sender.sendMessage(ChatColor.RED + "Incorrect argument count.");
                        return true;
                    }
                    String playerName = args[1];
                    Player player = Bukkit.getPlayer(playerName);
                    if (player == null) {
                        sender.sendMessage(ChatColor.RED + "That player is not online.");
                        return true;
                    }
                    String kitName = args[2];
                    Kit kit = kitMgr.getKitByName(kitName);
                    if (kit == null) {
                        sender.sendMessage(ChatColor.RED + "That kit does not exist.");
                        return true;
                    }
                    kit.giveToPlayer(player);
                }
                case "create" -> {
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(ChatColor.RED + "Command must be executed by a player.");
                        return true;
                    }
                    if (argCount != 2) {
                        sender.sendMessage(ChatColor.RED + "Incorrect argument count.");
                        return true;
                    }
                    String kitName = args[1];
                    Kit kit = kitMgr.getKitByName(kitName);
                    if (kit != null) {
                        sender.sendMessage(ChatColor.RED + "That kit already exists.");
                        return true;
                    }
                    kitMgr.createKitFromPlayer((Player) sender, kitName);
                    sender.sendMessage("Created kit \"" + kitName + "\".");
                }
                case "remove" -> {
                    if (argCount != 2) {
                        sender.sendMessage(ChatColor.RED + "Incorrect argument count.");
                        return true;
                    }
                    String kitName = args[1];
                    Kit kit = kitMgr.getKitByName(kitName);
                    if (kit == null) {
                        sender.sendMessage(ChatColor.RED + "That kit does not exist.");
                        return true;
                    }
                    kitMgr.removeKit(kit);
                    sender.sendMessage("Removed kit \"" + kitName + "\".");
                }
                case "seticon" -> {
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(ChatColor.RED + "Command must be executed by a player.");
                        return true;
                    }
                    if (argCount != 2) {
                        sender.sendMessage(ChatColor.RED + "Incorrect argument count.");
                        return true;
                    }
                    String kitName = args[1];
                    Kit kit = kitMgr.getKitByName(kitName);
                    if (kit == null) {
                        sender.sendMessage(ChatColor.RED + "That kit does not exist.");
                        return true;
                    }
                    Player player = (Player) sender;
                    PlayerInventory inv = player.getInventory();
                    kitMgr.setKitIcon(kit, inv.getItemInMainHand());
                    sender.sendMessage("Changed icon for kit \"" + kitName + "\".");
                }
                case "rename" -> {
                    if (argCount != 3) {
                        sender.sendMessage(ChatColor.RED + "Incorrect argument count.");
                        return true;
                    }
                    String oldKitName = args[1];
                    String newKitName = args[2];
                    Kit kit = kitMgr.getKitByName(oldKitName);
                    if (kit == null) {
                        sender.sendMessage(ChatColor.RED + "That kit does not exist.");
                        return true;
                    }
                    kitMgr.setKitName(kit, newKitName);
                    sender.sendMessage("Renamed kit from \"" + oldKitName + "\" to \"" + newKitName + "\".");
                }
                case "update" -> {
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(ChatColor.RED + "Command must be executed by a player.");
                        return true;
                    }
                    if (argCount != 2) {
                        sender.sendMessage(ChatColor.RED + "Incorrect argument count.");
                        return true;
                    }
                    String kitName = args[1];
                    Kit kit = kitMgr.getKitByName(kitName);
                    if (kit == null) {
                        sender.sendMessage(ChatColor.RED + "That kit does not exist.");
                        return true;
                    }
                    kitMgr.updateKit(kit, (Player) sender);
                    sender.sendMessage("Updated kit \"" + kitName + "\".");
                }
                case "reload" -> {
                    kitMgr.reload();
                    sender.sendMessage("All kits were reloaded.");
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
            list.add("menu");
            list.add("list");
            list.add("claim");
            list.add("give");
            list.add("create");
            list.add("remove");
            list.add("seticon");
            list.add("rename");
            list.add("update");
            list.add("reload");
        }
        else if (argCount > 1) {
            String subCmd = args[0];
            switch (subCmd) {
                case "claim", "remove", "seticon", "update" -> {
                    if (argCount == 2) {
                        list.addAll(kitMgr.getKitNames());
                    }
                }
                case "give" -> {
                    if (argCount == 2) {
                        for (Player player : Bukkit.getOnlinePlayers())
                            list.add(player.getName());
                    }
                    else if (argCount == 3) {
                        list.addAll(kitMgr.getKitNames());
                    }
                }
                case "create" -> {
                    if (argCount == 2) {
                        list.add("<name>");
                    }
                }
                case "rename" -> {
                    if (argCount == 2) {
                        list.addAll(kitMgr.getKitNames());
                    }
                    else if (argCount == 3) {
                        list.add("<name>");
                    }
                }
            }
        }
        return list;
    }
}
