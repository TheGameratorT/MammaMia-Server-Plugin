package com.thegameratort.mammamia.pvp;

import com.thegameratort.mammamia.MammaMia;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class PvPCommand implements CommandExecutor, TabExecutor
{
    private final PvPManager pvpMgr;

    @SuppressWarnings("ConstantConditions")
    public PvPCommand(@NotNull MammaMia plugin, PvPManager manager) {
        this.pvpMgr = manager;
        PluginCommand cmd = plugin.getCommand("pvp");
        cmd.setExecutor(this);
        cmd.setTabCompleter(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length > 0) {
            String subCommand = args[0];
            if (!sender.hasPermission("mm.pvp." + subCommand)) {
                sender.sendMessage(ChatColor.RED + "You do not have permission to use that command.");
                return true;
            }
            switch (subCommand) {
                case "startDuel" -> {
                    int arenaID = Integer.parseInt(args[1]);
                    Player player1 = Bukkit.getPlayer(args[2]);
                    Player player2 = Bukkit.getPlayer(args[3]);
                    pvpMgr.startDuel(sender, arenaID, player1, player2, false);
                }
                case "startSpleef" -> {
                    int arenaID = Integer.parseInt(args[1]);
                    Player player1 = Bukkit.getPlayer(args[2]);
                    Player player2 = Bukkit.getPlayer(args[3]);
                    pvpMgr.startDuel(sender, arenaID, player1, player2, true);
                }
                /*case "createGame" -> {
                    String sArenaID = args[1];
                    int arenaID = Integer.parseInt(sArenaID);
                    pvpMgr.createGame(arenaID, 0, null, (gameID, a) -> sender.sendMessage("Game " + gameID + " created."));
                }*/
                case "removeGame" -> {
                    String sGameID = args[1];
                    int gameID = Integer.parseInt(sGameID);
                    pvpMgr.removeGameByID(gameID);
                    sender.sendMessage("Game " + sGameID + " removed.");
                }
                case "listGames" -> {
                    pvpMgr.listGames(sender);
                }
                case "menu" -> {
                    if (!(sender instanceof Player))
                        return true;
                    PvPMenu.open(null, (Player) sender);
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
            list.add("startDuel");
            list.add("startSpleef");
            //list.add("createGame");
            list.add("removeGame");
            list.add("listGames");
        }
        else if (argCount > 1) {
            String subCmd = args[0];
            switch (subCmd) {
                case "startDuel", "startSpleef" -> {
                    switch (argCount) {
                        case 2 -> {
                            list.add("<arenaID>");
                        }
                        case 3, 4 -> {
                            for (Player player : Bukkit.getOnlinePlayers())
                                list.add(player.getName());
                        }
                    }
                }
                case "createGame", "removeGame" -> {
                    if (argCount == 2) {
                        list.add("<gameID>");
                    }
                }
            }
        }
        return list;
    }
}
