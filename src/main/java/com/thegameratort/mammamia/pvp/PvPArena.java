package com.thegameratort.mammamia.pvp;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.List;

public class PvPArena {
    public String name;
    public int type;
    public int env;
    public Vector arenaMinPos;
    public Vector arenaMaxPos;
    public Location spawnPos1;
    public Location spawnPos2;
    public Location respawnPos;

    public PvPArenaYML toYml(List<String> gameTypes, List<String> nameForEnv) {
        PvPArenaYML arenaYML = new PvPArenaYML();
        arenaYML.name = this.name;
        arenaYML.type = gameTypes.get(this.type);
        arenaYML.env = nameForEnv.get(this.env);
        arenaYML.arenaMinPos = this.arenaMinPos;
        arenaYML.arenaMaxPos = this.arenaMaxPos;
        arenaYML.spawnPos1 = this.spawnPos1;
        arenaYML.spawnPos2 = this.spawnPos2;
        arenaYML.respawnPos = this.respawnPos;
        return arenaYML;
    }

    public static PvPArena fromYml(PvPArenaYML arenaYML, List<String> gameTypes, List<String> nameForEnv) {
        PvPArena arena = new PvPArena();
        arena.name = arenaYML.name;
        arena.type = gameTypes.indexOf(arenaYML.type);
        arena.env = nameForEnv.indexOf(arenaYML.env);
        arena.arenaMinPos = arenaYML.arenaMinPos;
        arena.arenaMaxPos = arenaYML.arenaMaxPos;
        arena.spawnPos1 = arenaYML.spawnPos1;
        arena.spawnPos2 = arenaYML.spawnPos2;
        arena.respawnPos = arenaYML.respawnPos;
        return arena;
    }
}
