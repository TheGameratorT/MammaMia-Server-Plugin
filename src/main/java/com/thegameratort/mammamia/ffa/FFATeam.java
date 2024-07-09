package com.thegameratort.mammamia.ffa;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;

import java.util.LinkedList;

public class FFATeam {
	private final FFAGame game;
	private final long id;
	private String name;
	private final Team scoreboardTeam;
	private String ownerName;

	public FFATeam(FFAGame game, long id, String name, Team scoreboardTeam, String ownerName) {
		this.game = game;
		this.id = id;
		this.ownerName = ownerName;
		this.name = name;
		this.scoreboardTeam = scoreboardTeam;
	}

	public FFAGame getGame() {
		return game;
	}

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Team getScoreboardTeam() {
		return scoreboardTeam;
	}

	public String getOwnerName() {
		return ownerName;
	}

	public void setOwnerName(String ownerName) {
		this.ownerName = ownerName;
	}

	public LinkedList<Player> getOnlinePlayers() {
		LinkedList<Player> players = new LinkedList<>();
		for (String entry : scoreboardTeam.getEntries()) {
			Player player = Bukkit.getPlayer(entry);
			if (player != null) {
				players.add(player);
			}
		}
		return players;
	}
}
