package com.thegameratort.mammamia.ffa;

import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.MVWorldManager;
import com.onarandombox.MultiverseCore.api.MultiverseWorld;
import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.WorldGroup;
import com.onarandombox.multiverseinventories.profile.WorldGroupManager;
import com.onarandombox.multiverseinventories.profile.container.ProfileContainer;
import com.thegameratort.mammamia.*;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class FFAGame {
	private final FFAManager manager;
	private final MammaMia plugin;
	private final MultiverseCore mvCore;
	private final long id;
	private final String name;
	private final LinkedList<String> playerNames = new LinkedList<>();
	private boolean started = false;

	private final HashMap<String, FFATeam> teamForName = new HashMap<>();
	private final HashMap<String, FFATeam> teamForPlayer = new HashMap<>();

	public FFAGame(FFAManager manager, long id, String name) {
		this.manager = manager;
		this.plugin = manager.getPlugin();
		this.mvCore = plugin.getMvCore();
		this.id = id;
		this.name = name;
	}

	public FFATeam createTeam(CommandSender sender, long teamId, String teamName, String colorName) {
		if (!beginFfaStoppedCommand(sender)) {
			return null;
		}

		if (teamForName.containsKey(teamName)) {
			sender.sendMessage("Could not create the team because a team with that name already exists.");
			return null;
		}

		NamedTextColor teamColor = NamedTextColor.NAMES.value(colorName);
		if (teamColor == null) {
			sender.sendMessage("Invalid team color.");
			return null;
		}

		Scoreboard board = plugin.getServer().getScoreboardManager().getMainScoreboard();
		String boardTeamName = getBoardTeamName(teamId);
		Team boardTeam = board.registerNewTeam(boardTeamName);
		boardTeam.displayName(Component.text(teamName));
		boardTeam.color(teamColor);

		String ownerName = sender.getName();

		FFATeam team = new FFATeam(this, teamId, teamName, boardTeam, ownerName);

		teamForName.put(teamName, team);
		teamForPlayer.put(ownerName, team);

		sender.sendMessage("Team \"" + teamName + "\" created.");

		return team;
	}

	public @Nullable Long destroyTeam(CommandSender sender) {
		FFATeam team = beginOwnedTeamCommand(sender);
		if (team == null) {
			return null;
		}

		Team boardTeam = team.getScoreboardTeam();
		if (boardTeam != null) {
			boardTeam.unregister();
		}

		String teamName = team.getName();

		sender.sendMessage("Team \"" + teamName + "\" destroyed.");
		broadcastTeamChat(team, Component.text("The team you were a member of has been destroyed."), List.of((Player) sender));

		teamForName.remove(teamName);
		teamForPlayer.entrySet().removeIf(entry -> entry.getValue().equals(team));

		return team.getId();
	}

	public void joinTeam(CommandSender sender, String teamName) {
		if (!beginFfaStoppedCommand(sender)) {
			return;
		}

		FFATeam team = teamForName.get(teamName);
		if (team == null) {
			sender.sendMessage("That team does not exist.");
			return;
		}

		String senderName = sender.getName();

		FFATeam senderTeam = teamForPlayer.get(senderName);
		if (senderTeam != null) {
			if (senderTeam == team) {
				String ownerName = senderTeam.getOwnerName();
				if (ownerName.equals(senderName)) {
					sender.sendMessage("You can not join your own team.");
					return;
				}
				sender.sendMessage("You are already on that team.");
				return;
			}
			sender.sendMessage("You are already on a team.");
			return;
		}

		teamForPlayer.put(senderName, team);
		team.getScoreboardTeam().addEntry(senderName);

		sender.sendMessage("You have joined the \"" + teamName + "\" team.");
		broadcastTeamChat(team, Component.text(senderName + " has joined your team."), List.of((Player) sender));
	}

	public void leaveTeam(CommandSender sender) {
		if (!beginFfaStoppedCommand(sender)) {
			return;
		}

		FFATeam senderTeam = beginInTeamCommand(sender);
		if (senderTeam == null) {
			return;
		}

		String senderName = sender.getName();

		String ownerName = senderTeam.getOwnerName();
		if (ownerName.equals(senderName)) {
			sender.sendMessage("You can not leave your own team.");
			return;
		}

		senderTeam.getScoreboardTeam().removeEntry(senderName);
		teamForPlayer.remove(senderName);

		String senderTeamName = senderTeam.getName();

		sender.sendMessage("You have left the \"" + senderTeamName + "\" team.");
		broadcastTeamChat(senderTeam, Component.text(senderName + " has left your team."), List.of((Player) sender));
	}

	public void setTeamOwner(CommandSender sender, String newOwnerName) {
		FFATeam senderTeam = beginOwnedTeamCommand(sender);
		if (senderTeam == null) {
			return;
		}

		FFATeam newOwnerTeam = teamForPlayer.get(newOwnerName);
		if (newOwnerTeam != senderTeam) {
			sender.sendMessage(newOwnerName + " is not on your team.");
			return;
		}

		String ownerName = sender.getName();
		if (ownerName.equals(newOwnerName)) {
			sender.sendMessage("You already own this team.");
			return;
		}

		Player newOwnerPlayer = plugin.getServer().getPlayer(newOwnerName);
		if (newOwnerPlayer == null) {
			sender.sendMessage("Player " + newOwnerName + " is not online.");
			return;
		}

		senderTeam.setOwnerName(newOwnerName);

		String senderTeamName = senderTeam.getName();

		newOwnerPlayer.sendMessage("You now own the team \"" + senderTeamName + "\".");
		broadcastTeamChat(senderTeam, Component.text(newOwnerName + " now owns the team \"" + senderTeamName + "\"."), List.of(newOwnerPlayer));
	}

	public void setTeamName(CommandSender sender, String teamName) {
		FFATeam senderTeam = beginOwnedTeamCommand(sender);
		if (senderTeam == null) {
			return;
		}

		String senderTeamName = senderTeam.getName();

		if (teamName.equals(senderTeamName)) {
			sender.sendMessage("The team is already named \"" + teamName + "\".");
			return;
		}

		if (teamForName.containsKey(teamName)) {
			sender.sendMessage("A team with that name already exists.");
			return;
		}

		senderTeam.setName(teamName);
		senderTeam.getScoreboardTeam().displayName(Component.text(teamName));

		sender.sendMessage("The team name has been changed to \"" + teamName + "\".");
		broadcastTeamChat(senderTeam, Component.text("Your team was renamed to \"" + teamName + "\"."), List.of((Player) sender));
	}

	public void generateWorld(CommandSender sender, String seed) {
		if (!beginFfaStoppedCommand(sender)) {
			return;
		}

		LoadingScreen ls = new LoadingScreen(this.plugin);
		ls.show(this.getParticipants(), () -> {
			DelayedExecutor de = new DelayedExecutor(this.plugin);

			String worldName = getWorldName();

			de.add(e -> {
				MVWorldManager worldManager = this.mvCore.getMVWorldManager();
				String actualSeed = seed != null ? seed : Long.valueOf((new Random()).nextLong()).toString();

				if (!this.regenWorld(sender, worldManager, worldName, actualSeed)) {
					closeLoading(ls, "lobby");
					e.cancel();
				}

				MultiverseInventories mvInv = this.plugin.getMvInv();
				WorldGroupManager worldGroupMgr = mvInv.getGroupManager();
				WorldGroup invWorldGroup = mvInv.getGroupManager().getGroup(worldName);
				if (invWorldGroup == null) {
					invWorldGroup = worldGroupMgr.newEmptyGroup(worldName);
					invWorldGroup.addWorld(worldName);
					worldGroupMgr.updateGroup(invWorldGroup);
				} else {
					ProfileContainer ffaInv = invWorldGroup.getGroupProfileContainer();
					OfflinePlayer[] players = Bukkit.getOfflinePlayers();

					for (OfflinePlayer player : players) {
						ffaInv.removeAllPlayerData(player);
					}
				}

				mvInv.reloadConfig();
			}, 20);

			de.add(e -> {
				closeLoading(ls, worldName);
			}, 20);

			de.add(e -> {
				for (Player player : this.getParticipants()) {
					WorldUtils.revokePlayerAdvancements(player);
					player.setGameMode(GameMode.SURVIVAL);
					player.setRespawnLocation(null, true);
				}
			}, 1);

			de.runAll();
		});
	}

	private void closeLoading(LoadingScreen ls, String worldName) {
		Location spawn = this.mvCore.getMVWorldManager().getMVWorld(worldName).getSpawnLocation();
		ls.close(spawn);
	}

	public void addPlayer(Player player) {
		this.playerNames.add(player.getName());

		Location spawn = this.mvCore.getMVWorldManager().getMVWorld(getWorldName()).getSpawnLocation();
		this.mvCore.getSafeTTeleporter().safelyTeleport(Bukkit.getConsoleSender(), player, spawn, false);
	}

	public void removePlayer(Player player) {
		Location spawn = this.mvCore.getMVWorldManager().getMVWorld("lobby").getSpawnLocation();
		this.mvCore.getSafeTTeleporter().safelyTeleport(Bukkit.getConsoleSender(), player, spawn, false);

		this.playerNames.remove(player.getName());
	}

	public LinkedList<Player> getParticipants() {
		LinkedList<Player> players = new LinkedList<>();
		for (String playerName : this.playerNames) {
			Player player = Bukkit.getPlayer(playerName);
			if (player != null) {
				players.add(player);
			}
		}
		return players;
	}

	public long getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public boolean isStarted() {
		return this.started;
	}

	public String getWorldName() {
		return "ffa_" + this.id;
	}

	public String getBoardTeamName(long teamId) {
		return getWorldName() + "_team_" + teamId;
	}

	private boolean regenWorld(CommandSender sender, MVWorldManager worldManager, String name, String seed) {
		this.broadcastActionBar("Preparing \"" + name + "\"");

		MultiverseWorld mvWorld = worldManager.getMVWorld(name);
		if (mvWorld == null) {
			if (!worldManager.addWorld(name, World.Environment.NORMAL, seed, WorldType.NORMAL, true, null)) {
				sender.sendMessage("Failed to create world \"" + name + "\".");
				return false;
			}
			mvWorld = worldManager.getMVWorld(name);
		} else {
			if (!worldManager.regenWorld(name, true, false, seed)) {
				sender.sendMessage("Failed to regenerate world \"" + name + "\".");
				return false;
			}
		}

		WorldUtils.fixWorldSpawnLocation(mvWorld);
		return true;
	}

	private boolean destroyWorld(MVWorldManager worldManager, String name) {
		return worldManager.deleteWorld(name, true, true);
	}

	private void broadcastActionBar(String msg) {
		for (Player player : getParticipants()) {
			player.sendActionBar(Component.text(msg));
		}
	}

	private void broadcastTeamChat(FFATeam team, Component text, List<Player> excludePlayers) {
		for (Player player : team.getOnlinePlayers()) {
			if (excludePlayers.contains(player)) {
				continue;
			}
			player.sendMessage(text);
		}
	}

	private boolean beginFfaStoppedCommand(CommandSender sender) {
		if (this.started) {
			sender.sendMessage("Please stop the FFA game first.");
			return false;
		}
		return true;
	}

	private FFATeam beginInTeamCommand(CommandSender sender) {
		FFATeam senderTeam = teamForPlayer.get(sender.getName());
		if (senderTeam == null) {
			sender.sendMessage("You must be in a team to use this command.");
			return null;
		}
		return senderTeam;
	}

	private @Nullable FFATeam beginOwnedTeamCommand(CommandSender sender) {
		if (!beginFfaStoppedCommand(sender)) {
			return null;
		}

		FFATeam senderTeam = beginInTeamCommand(sender);
		if (senderTeam == null) {
			return null;
		}

		String senderName = sender.getName();

		String ownerName = senderTeam.getOwnerName();
		if (!ownerName.equals(senderName)) {
			sender.sendMessage("You do not own this team.");
			return null;
		}

		return senderTeam;
	}
}
