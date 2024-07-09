package com.thegameratort.mammamia.ffa;

import com.thegameratort.mammamia.MammaMia;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class FFACommand implements CommandExecutor, TabExecutor {
	private final FFAManager manager;

	@SuppressWarnings("ConstantConditions")
	public FFACommand(MammaMia plugin, FFAManager manager) {
		this.manager = manager;
		PluginCommand cmd = plugin.getCommand("ffa");
		cmd.setExecutor(this);
		cmd.setTabCompleter(this);
	}

	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
		if (args.length > 0) {
			if (!sender.hasPermission("mm.ffa." + args[0])) {
				sender.sendMessage(ChatColor.RED + "You do not have permission to use that command.");
				return true;
			}
			switch (args[0]) {
				case "game" -> {
					switch (args[1]) {
						case "create" -> manager.createGameCommand(sender, args);
						case "destroy" -> manager.destroyGameCommand(sender, args);
						case "reset" -> manager.resetGameCommand(sender, args);
						//case "set" -> manager.setGameCommand(sender, args);
						case "join" -> manager.joinGameCommand(sender, args);
						case "leave" -> manager.leaveGameCommand(sender);
						case "start" -> manager.startGameCommand(sender);
					}
				}
				case "team" -> {
					switch (args[1]) {
						case "create" -> manager.createTeamCommand(sender, args);
						case "destroy" -> manager.destroyTeamCommand(sender, args);
						case "join" -> manager.joinTeamCommand(sender, args);
						case "leave" -> manager.leaveTeamCommand(sender);
						case "setowner" -> manager.setOwnerTeamCommand(sender, args);
						case "rename" -> manager.renameTeamCommand(sender, args);
					}
				}
			}
		}
		return true;
	}

	public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
		List<String> list = new ArrayList<>();

		int argCount = args.length;

		if (argCount == 1) {

			list.add("team");
			list.add("game");

		} else if (argCount > 1) {

			switch (args[0]) {
				case "game" -> {
					if (argCount == 2) {
						list.addAll(List.of("create", "destroy", "reset", "set", "join", "leave"));
					}
				}
				case "team" -> {
					if (argCount == 2) {
						list.addAll(List.of("create", "destroy", "join", "leave", "setowner", "rename"));
					} else /* if (argCount > 2) */ {
						switch (args[1]) {
							case "create" -> {
								if (argCount == 3) {
									list.add("<name>");
								} else if (argCount == 4) {
									list.add("<color>");
								}
							}
							case "join", "rename" -> {
								if (argCount == 3) {
									list.add("<name>");
								}
							}
							case "setowner" -> {
								if (argCount == 3) {
									for (Player player : Bukkit.getOnlinePlayers()) {
										list.add(player.getName());
									}
								}
							}
						}
					}
				}
			}

		}
		return list;
	}
}
