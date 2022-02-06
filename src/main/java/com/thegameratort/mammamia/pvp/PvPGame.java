package com.thegameratort.mammamia.pvp;

import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;

public class PvPGame {
    public int gameID;
    public int arenaID;
    public int slot;
    public PvPGameType type;
    public List<Player> players;
    public Vector arenaMinPos;
    public Vector arenaMaxPos;
    public PvPGameState state = PvPGameState.WAITING;
    public int playersReady = 0;
    public int deadPlayerCount = 0;
    public int endGameTaskID = 0;
    public boolean isSpleef;

    public PvPGame(int gameID, int arenaID, int slot, PvPGameType type, List<Player> players) {
        this.gameID = gameID;
        this.arenaID = arenaID;
        this.slot = slot;
        this.type = type;
        this.players = players;
    }

    public void create() {

    }
}
