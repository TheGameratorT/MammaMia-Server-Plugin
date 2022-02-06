package com.thegameratort.mammamia.pvp;

import com.fastasyncworldedit.core.FaweAPI;
import com.fastasyncworldedit.core.extent.processor.lighting.RelightMode;
import com.fastasyncworldedit.core.util.EditSessionBuilder;
import com.fastasyncworldedit.core.util.TaskManager;
import com.onarandombox.MultiverseCore.MultiverseCore;
import com.onarandombox.MultiverseCore.api.SafeTTeleporter;
import com.onarandombox.multiverseinventories.MultiverseInventories;
import com.onarandombox.multiverseinventories.profile.container.ProfileContainer;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.entity.Entity;
import com.sk89q.worldedit.function.operation.ForwardExtentCopy;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.World;
import com.thegameratort.mammamia.ConfigFile;
import com.thegameratort.mammamia.LoadingScreen;
import com.thegameratort.mammamia.MammaMia;
import org.bukkit.*;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PvPManager {
    public interface GameCreatedCallback {
        void onGameCreated(PvPGame game);
    }

    private final MammaMia plugin;
    private final Server server;
    private final Logger logger;
    private final BukkitScheduler scheduler;
    private final MultiverseCore mvCore;
    private final MultiverseInventories mvInv;
    private final Random random = new Random();
    private ConfigFile config;
    private int arenaSpacing;

    private final ArrayList<String> nameForEnv = new ArrayList<>();
    private final ArrayList<String> worldForEnv = new ArrayList<>();
    private final ArrayList<ArrayList<Integer>> arenasForEnv = new ArrayList<>();

    private final ArrayList<PvPGame> games = new ArrayList<>();
    private final ArrayList<PvPArena> arenas = new ArrayList<>();
    private static final String[] gameTypes = { "duel", "ffa" };

    @SuppressWarnings("unchecked")
    public PvPManager(@NotNull MammaMia plugin) {
        this.plugin = plugin;
        this.server = plugin.getServer();
        this.logger = plugin.getLogger();
        this.scheduler = server.getScheduler();
        this.mvCore = plugin.getMvCore();
        this.mvInv = plugin.getMvInv();

        this.config = ConfigFile.loadConfig(this.plugin, "arenas.yml");

        ConfigurationSection settingsSec = this.config.getConfigurationSection("settings");
        if (settingsSec != null) {
            arenaSpacing = settingsSec.getInt("arenaSpacing");
        }

        ConfigurationSection envsSec = this.config.getConfigurationSection("environments");
        if (envsSec != null) {
            for (String env : envsSec.getKeys(false)) {
                String worldName = envsSec.getString(env);
                if (worldName == null) {
                    logger.log(Level.WARNING, "null world name in arena config for environment '" + env + "'.");
                    continue;
                }
                nameForEnv.add(env);
                worldForEnv.add(worldName);
                arenasForEnv.add(new ArrayList<>());
            }
        }

        ArrayList<PvPArenaYML> ymlArenas = (ArrayList<PvPArenaYML>) this.config.getList("arenas", new ArrayList<PvPArenaYML>());
        for (PvPArenaYML ymlArena : ymlArenas) {
            PvPArena arena = PvPArena.fromYml(ymlArena, Arrays.asList(gameTypes), nameForEnv);
            ArrayList<Integer> arenaIDs = arenasForEnv.get(arena.env);
            arenaIDs.add(arenas.size());
            arenas.add(arena);
        }

        //saveArenaConfig();

        new PvPListener(this.plugin, this);
        new PvPCommand(this.plugin, this);
    }

    private void createGame(int arenaID, PvPGameType type, ArrayList<Player> players, GameCreatedCallback callback) {
        int gameID = getNewGameID();
        int slot = getNewSlot(arenaID);

        PvPGame game = new PvPGame(gameID, arenaID, slot, type, players);
        games.add(game);

        PvPArena arena = arenas.get(arenaID);

        BlockVector3 arenaMinPos = BlockVector3.at(arena.arenaMinPos.getX(), arena.arenaMinPos.getY(), arena.arenaMinPos.getZ());
        BlockVector3 arenaMaxPos = BlockVector3.at(arena.arenaMaxPos.getX(), arena.arenaMaxPos.getY(), arena.arenaMaxPos.getZ());

        int arenaMinToPosX = slot * arenaSpacing;
        int arenaMinToPosY = arenaMinPos.getY();
        int arenaMinToPosZ = arenasForEnv.get(arena.env).indexOf(arenaID) * arenaSpacing;
        BlockVector3 arenaMinToPos = BlockVector3.at(arenaMinToPosX, arenaMinToPosY, arenaMinToPosZ);

        int arenaMaxToPosX = arenaMaxPos.getX() - (arenaMinPos.getX() - arenaMinToPosX);
        int arenaMaxToPosY = arenaMaxPos.getY();
        int arenaMaxToPosZ = arenaMaxPos.getZ() - (arenaMinPos.getZ() - arenaMinToPosZ);
        BlockVector3 arenaMaxToPos = BlockVector3.at(arenaMaxToPosX, arenaMaxToPosY, arenaMaxToPosZ);

        game.arenaMinPos = new Vector(arenaMinToPosX, arenaMinToPosY, arenaMinToPosZ);
        game.arenaMaxPos = new Vector(arenaMaxToPosX, arenaMaxToPosY, arenaMaxToPosZ);

        TaskManager.IMP.async(() -> {
            synchronized (PvPManager.class) {
                World world = FaweAPI.getWorld(worldForEnv.get(arena.env));

                EditSession editSession = new EditSessionBuilder(world)
                        .autoQueue(false)
                        .checkMemory(false)
                        .allowedRegionsEverywhere()
                        .limitUnlimited()
                        .changeSetNull()
                        .fastmode(true)
                        .build();

                Region region = new CuboidRegion(arenaMinPos, arenaMaxPos);

                ForwardExtentCopy forwardExtentCopy = new ForwardExtentCopy(editSession, region, editSession, arenaMinToPos);

                try {
                    Operations.completeLegacy(forwardExtentCopy);
                    editSession.flushQueue();
                } catch (MaxChangedBlocksException e) {
                    e.printStackTrace();
                }

                FaweAPI.fixLighting(world, region, null, RelightMode.OPTIMAL);

                TaskManager.IMP.task(() -> {
                    TaskManager.IMP.sync(() -> {
                        Region arenaToRegion = new CuboidRegion(arenaMinToPos, arenaMaxToPos);
                        removeEntitiesInRegion(world, arenaToRegion);
                        return true;
                    });
                    callback.onGameCreated(game);
                });
            }
        });
    }

    private void removeEntitiesInRegion(World world, Region region) {
        EditSession editSession = new EditSessionBuilder(world)
                .autoQueue(false)
                .checkMemory(false)
                .allowedRegionsEverywhere()
                .limitUnlimited()
                .changeSetNull()
                .fastmode(true)
                .build();

        List<? extends Entity> entities = editSession.getEntities(region);
        for (Entity entity : entities) {
            entity.remove();
        }

        editSession.flushQueue();
    }

    public void startDuel(CommandSender sender, int arenaID, Player player1, Player player2, boolean isSpleef) {
        ArrayList<Player> players = new ArrayList<>();
        players.add(player1);
        players.add(player2);

        for (Player player : players) {
            if (getPlayerGame(player) != null) {
                sender.sendMessage(ChatColor.RED + "Could not create game because " + player.getName() + " is in a match.");
                return;
            }
        }

        LoadingScreen ls = new LoadingScreen(plugin);
        ls.show(players, () -> {
            for (Player player : players) {
                player.sendActionBar("Generating arena...");
            }
            createGame(arenaID, PvPGameType.DUEL, players, (game) -> {
                ls.close();
                game.isSpleef = isSpleef;
                onDuelGameCreated(game);
            });
        });
    }

    private void onDuelGameCreated(PvPGame game) {
        PvPArena arena = getArenaByID(game.arenaID);
        List<Player> players = game.players;

        ProfileContainer pvpInv = mvInv.getGroupManager().getGroup("pvp").getGroupProfileContainer();
        for (Player player : players) {
            pvpInv.removeAllPlayerData(player);
        }
        mvInv.reloadConfig();

        org.bukkit.World world = server.getWorld(worldForEnv.get(arena.env));

        Location spawnPos1 = getArenaSpawnPos(arena.spawnPos1, world, arena.arenaMinPos, game.arenaMinPos);
        Location spawnPos2 = getArenaSpawnPos(arena.spawnPos2, world, arena.arenaMinPos, game.arenaMinPos);

        int spawnSpot1ID = random.nextInt(2);
        int spawnSpot2ID = spawnSpot1ID == 1 ? 0 : 1;

        for (Player player : players) {
            if (player.isDead()) {
                player.spigot().respawn();
            }
        }

        SafeTTeleporter teleporter = mvCore.getSafeTTeleporter();
        ConsoleCommandSender sender = Bukkit.getConsoleSender();
        teleporter.safelyTeleport(sender, players.get(spawnSpot1ID), spawnPos1, false);
        teleporter.safelyTeleport(sender, players.get(spawnSpot2ID), spawnPos2, false);

        scheduler.scheduleSyncDelayedTask(plugin, () -> {
            for (Player player : players) {
                giveSelectKitBook(player);
            }
            if (game.isSpleef) {
                for (Player player : players) {
                    Objects.requireNonNull(player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE)).setBaseValue(-20.0F);
                    player.setHealth(20.0F);
                }
            }
        }, 1);
    }

    public static Location getArenaSpawnPos(Location spawnPos, org.bukkit.World world, Vector arenaMinPos, Vector arenaMinToPos) {
        double spawnX = (spawnPos.getX() - arenaMinPos.getX()) + arenaMinToPos.getX();
        double spawnY = (spawnPos.getY() - arenaMinPos.getY()) + arenaMinToPos.getY();
        double spawnZ = (spawnPos.getZ() - arenaMinPos.getZ()) + arenaMinToPos.getZ();
        return new Location(world, spawnX, spawnY, spawnZ, spawnPos.getYaw(), spawnPos.getPitch());
    }

    private void giveSelectKitBook(Player player) {
        Inventory inv = player.getInventory();
        ItemStack stack = new ItemStack(Material.BOOK);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName("Select Kit");
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        stack.setItemMeta(meta);
        stack.addUnsafeEnchantment(Enchantment.MENDING, 1);
        inv.addItem(stack);
    }

    private int getNewSlot(int arenaID) {
        PvPArena arena = arenas.get(arenaID);
        int slot = 0;
        int gameCount = games.size();
        for (int i = 0; i < gameCount; i++) {
            PvPGame game = games.get(i);
            PvPArena garena = arenas.get(game.arenaID);
            if (garena.env != arena.env) continue;
            if (game.arenaID != arenaID) continue;
            if (game.slot != slot) continue;
            slot++;
            i = -1;
        }
        return slot;
    }

    private int getNewGameID() {
        int id = 0;
        int gameCount = games.size();
        for (int i = 0; i < gameCount; i++) {
            PvPGame game = games.get(i);
            if (game.gameID == id) {
                id++;
                i = -1;
            }
        }
        return id;
    }

    public PvPArena getArenaByID(int arenaID) {
        return arenas.get(arenaID);
    }

    public PvPGame getGameByID(int gameID) {
        for (PvPGame game : games) {
            if (game.gameID == gameID) {
                return game;
            }
        }
        return null;
    }

    public void removeGameByID(int gameID) {
        int gameCount = games.size();
        for (int i = 0; i < gameCount; i++) {
            if (games.get(i).gameID == gameID) {
                games.remove(i);
                break;
            }
        }
    }

    public void removeGame(PvPGame game) {
        games.remove(game);
    }

    public void listGames(CommandSender sender) {
        StringBuilder msg = new StringBuilder();
        msg.append("================\n");
        msg.append("Active PvP Games\n");
        for (PvPGame game : games) {
            msg.append("================\n");
            msg.append("Game ID: ").append(game.gameID).append("\n");
            msg.append("Arena ID: ").append(game.arenaID).append("\n");
            msg.append("Slot: ").append(game.slot).append("\n");
            msg.append("Type: ").append(game.type).append("\n");
            if (game.players != null) {
                msg.append("Players:\n");
                for (Player player : game.players) {
                    msg.append("  - ").append(player.getName()).append("\n");
                }
            }
        }
        msg.append("================\n");
        sender.sendMessage(msg.toString());
    }

    public String getWorldForEnv(int env) {
        return worldForEnv.get(env);
    }

    public int getPlayerGameID(Player player) {
        int gameCount = games.size();
        for (int i = 0; i < gameCount; i++) {
            if (games.get(i).players.contains(player)) {
                return i;
            }
        }
        return -1;
    }

    public PvPGame getPlayerGame(Player player) {
        for (PvPGame game : games) {
            if (game.players.contains(player)) {
                return game;
            }
        }
        return null;
    }

    public boolean isPlayerInGame(Player player) {
        return getPlayerGameID(player) != -1;
    }
}
