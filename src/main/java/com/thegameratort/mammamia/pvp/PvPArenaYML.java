package com.thegameratort.mammamia.pvp;

import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;

@SerializableAs("MM_PvPArena")
public class PvPArenaYML implements ConfigurationSerializable {
    public String name;
    public String type;
    public String env;
    public Vector arenaMinPos;
    public Vector arenaMaxPos;
    public Location spawnPos1;
    public Location spawnPos2;
    public Location respawnPos;

    @Override
    @NotNull
    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<>();

        result.put("name", this.name);
        result.put("type", this.type);
        result.put("env", this.env);
        result.put("arenaMinPos", this.arenaMinPos);
        result.put("arenaMaxPos", this.arenaMaxPos);
        result.put("spawnPos1", this.spawnPos1);
        result.put("spawnPos2", this.spawnPos2);
        result.put("respawnPos", this.respawnPos);

        return result;
    }

    @NotNull
    public static PvPArenaYML deserialize(@NotNull Map<String, Object> args) {
        PvPArenaYML arena = new PvPArenaYML();

        arena.name = (String) args.get("name");
        arena.type = (String) args.get("type");
        arena.env = (String) args.get("env");
        arena.arenaMinPos = (Vector) args.get("arenaMinPos");
        arena.arenaMaxPos = (Vector) args.get("arenaMaxPos");
        arena.spawnPos1 = (Location) args.get("spawnPos1");
        arena.spawnPos2 = (Location) args.get("spawnPos2");
        arena.respawnPos = (Location) args.get("respawnPos");

        return arena;
    }
}
