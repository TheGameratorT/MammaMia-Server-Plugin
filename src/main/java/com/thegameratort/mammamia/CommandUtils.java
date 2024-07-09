package com.thegameratort.mammamia;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandUtils {
	public static boolean beginPlayerOnlyCommand(CommandSender sender) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("This command can only be run by a player.");
			return false;
		}
		return true;
	}
}
