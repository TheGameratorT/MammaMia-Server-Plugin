package com.thegameratort.mammamia.ffa;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.thegameratort.mammamia.CommandUtils;
import com.thegameratort.mammamia.ConfigFile;
import com.thegameratort.mammamia.MammaMia;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class FFAManager {
	private final MammaMia plugin;
	private FFAListener listener = null;
	private final ConfigFile config;
	private final MultiverseCore mvCore;

	private final HashMap<Long, FFAGame> gameForId = new HashMap<>();
	private final HashMap<String, FFAGame> gameForName = new HashMap<>();
	private final HashMap<String, FFAGame> gameForPlayer = new HashMap<>();

	private final HashMap<Long, FFATeam> teamForId = new HashMap<>();

	public FFAManager(MammaMia plugin)
	{
		this.plugin = plugin;
		this.mvCore = plugin.getMvCore();

		this.config = ConfigFile.loadConfig(this.plugin, "ffa.yml");

		new FFACommand(plugin, this);
	}

	public void createGameCommand(CommandSender sender, String[] args) {
		if (args.length < 3 || args.length > 4) {
			sender.sendMessage("Invalid argument count.");
			return;
		}

		String gameName = args[2];

		if (gameName.equals("none")) {
			sender.sendMessage("The game must not be named \"none\".");
			return;
		}

		if (gameForName.containsKey(gameName)) {
			sender.sendMessage("That game already exists.");
			return;
		}

		String gameSeed = args.length > 3 ? args[3] : null;

		long gameId = getFreeGameId();

		FFAGame game = new FFAGame(this, gameId, gameName);
		game.generateWorld(sender, gameSeed);

		gameForId.put(gameId, game);
		gameForName.put(gameName, game);
	}

	public void destroyGameCommand(CommandSender sender, String[] args) {
		if (args.length != 3) {
			sender.sendMessage("Invalid argument count.");
			return;
		}

		String gameName = args[2];

		FFAGame game = gameForName.get(gameName);
		if (game == null) {
			sender.sendMessage("Could not find that game.");
			return;
		}

		long gameId = game.getId();

		gameForId.remove(gameId, game);
		gameForName.remove(gameName, game);
	}

	public void resetGameCommand(CommandSender sender, String[] args) {
		if (args.length < 3 || args.length > 4) {
			sender.sendMessage("Invalid argument count.");
			return;
		}

		String gameName = args[2];

		FFAGame game = gameForName.get(gameName);
		if (game == null) {
			sender.sendMessage("Could not find that game.");
			return;
		}

		String gameSeed = args.length > 3 ? args[3] : null;

		game.generateWorld(sender, gameSeed);
	}

	/*public void setGameCommand(CommandSender sender, String[] args) {
		if (args.length != 4) {
			sender.sendMessage("Invalid argument count.");
			return;
		}

		String playerName = args[2];
		Player player = Bukkit.getPlayer(playerName);
		if (player == null) {
			sender.sendMessage("Could not find that player.");
			return;
		}

		String gameName = args[3];
		FFAGame game = gameForName.get(gameName);
		if (game == null) {
			sender.sendMessage("Could not find that game.");
			return;
		}

		FFAGame playerGame = gameForPlayer.get(playerName);
		if (playerGame != null) {
			// Player is in a game
			if (playerGame.isStarted()) {
				sender.sendMessage("Could not set the game for the player because the player is in a game that has already started.");
				return;
			}
			playerGame.removePlayer(player);
			gameForPlayer.remove(playerName);
		}

		if (!gameName.equals("none")) {
			game.addPlayer(player);
			gameForPlayer.put(playerName, game);
		}
	}*/

	public void joinGameCommand(CommandSender sender, String[] args) {
		if (!(sender instanceof Player player)) {
			sender.sendMessage("This command can only be run by a player.");
			return;
		}
		if (args.length != 3) {
			sender.sendMessage("Invalid argument count.");
			return;
		}

		String gameName = args[2];
		FFAGame game = gameForName.get(gameName);
		if (game == null) {
			sender.sendMessage("Could not find that game.");
			return;
		}

		String playerName = player.getName();

		FFAGame playerGame = gameForPlayer.get(playerName);
		if (playerGame != null) {
			// Player is in a game
			if (playerGame.isStarted()) {
				sender.sendMessage("Could not leave the game because you are in a game which has already started.");
				return;
			}
			playerGame.removePlayer(player);
			gameForPlayer.remove(playerName);
		}
	}

	public void leaveGameCommand(CommandSender sender) {
		if (!(sender instanceof Player player)) {
			sender.sendMessage("This command can only be run by a player.");
			return;
		}

		String playerName = player.getName();

		FFAGame playerGame = gameForPlayer.get(playerName);
		if (playerGame != null) {
			// Player is in a game
			if (playerGame.isStarted()) {
				sender.sendMessage("Could not leave the game because it has already started.");
				return;
			}
			playerGame.removePlayer(player);
			gameForPlayer.remove(playerName);
		}
	}

	public void startGameCommand(CommandSender sender) {
		if (!(sender instanceof Player player)) {
			sender.sendMessage("This command can only be run by a player.");
			return;
		}

		// TODO
	}

	public void createTeamCommand(CommandSender sender, String[] args) {
		FFAGame game = beginTeamCommand(sender, args, 4);
		if (game == null) {
			return;
		}

		String teamName = args[2];
		String teamColor = args[3];

		long teamId = getFreeTeamId();

		FFATeam team = game.createTeam(sender, teamId, teamName, teamColor);
		if (team == null) {
			return;
		}

		teamForId.put(teamId, team);
	}

	public void destroyTeamCommand(CommandSender sender, String[] args) {
		FFAGame game = beginTeamCommand(sender, args, 2);
		if (game == null) {
			return;
		}

		Long teamId = game.destroyTeam(sender);
		if (teamId == null) {
			return;
		}

		teamForId.remove(teamId);
	}

	public void joinTeamCommand(CommandSender sender, String[] args) {
		FFAGame game = beginTeamCommand(sender, args, 3);
		if (game == null) {
			return;
		}

		String teamName = args[2];

		game.joinTeam(sender, teamName);
	}

	public void leaveTeamCommand(CommandSender sender) {
		FFAGame game = beginTeamCommand(sender, null, 0);
		if (game == null) {
			return;
		}

		game.leaveTeam(sender);
	}

	public void setOwnerTeamCommand(CommandSender sender, String[] args) {
		FFAGame game = beginTeamCommand(sender, args, 3);
		if (game == null) {
			return;
		}

		String newOwnerName = args[2];

		game.setTeamOwner(sender, newOwnerName);
	}

	public void renameTeamCommand(CommandSender sender, String[] args) {
		FFAGame game = beginTeamCommand(sender, args, 3);
		if (game == null) {
			return;
		}

		String newTeamName = args[2];

		game.setTeamName(sender, newTeamName);
	}

	public MammaMia getPlugin() {
		return plugin;
	}

	private Long getFreeGameId() {
		long id = 1;
		while (gameForId.containsKey(id)) {
			id++;
		}
		return id;
	}

	private Long getFreeTeamId() {
		long id = 1;
		while (teamForId.containsKey(id)) {
			id++;
		}
		return id;
	}

	private FFAGame beginTeamCommand(CommandSender sender, String[] args, int expectedArgsLength) {
		if (!CommandUtils.beginPlayerOnlyCommand(sender)) {
			return null;
		}
		if (args != null && args.length != expectedArgsLength) {
			sender.sendMessage("Invalid argument count.");
			return null;
		}
		FFAGame game = gameForPlayer.get(sender.getName());
		if (game == null) {
			sender.sendMessage("You must be in an FFA game.");
			return null;
		}
		return game;
	}
}
