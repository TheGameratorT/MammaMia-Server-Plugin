package com.thegameratort.mammamia.manhunt;

import com.thegameratort.mammamia.MammaMia;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class CockhuntListener implements Listener
{
    private final MammaMia plugin;
    private final ManhuntManager mhMgr;
    private final Random random = new Random();
    private final ArrayList<Player> rngPlayers = new ArrayList<>();
    private final HashMap<Player, ArrayList<String>> rngCockMsg = new HashMap<>();
    private final ArrayList<Player> rngChatPlayers = new ArrayList<>();
    private final HashMap<Player, ArrayList<String>> rngChatMsg = new HashMap<>();
    private int updaterTaskID;

    private final String[] hunterCockMsg = {
        "You have to catch him man, he is sus.",
        "Seriously man, you suck at this game. Hurry up!",
        "Go catch that pingas!",
        "Bruh, you still didn't catch him? Loser.",
        "Did you know that he drops a pingas? NOW GO!!!"
    };

    private final String[] runnerCockMsg = {
        "Remember that they want your pingas, so run away!",
        "Faster man, go faster, or else they will get you.",
        "RUN, YOU DON'T WANT THEM TO CATCH YOU, DO YOU?",
        "You don't want to become an average fan do you?",
        "Run away, they're coming for your pingas!"
    };

    private final String[] chatMsg = {
        "amogus", "sus", "imposter", "amogusus", "'mogus",
        "i'm gay", "fric froc", "pingas", "banana", "monke",
        "what the dog doin", "https://youtu.be/dQw4w9WgXcQ",
        "hi call?", "whatsapp", "get real", "hopps",
        "it's immune, not ignore", "i unknow the asm",
        "your mum", "taco's mum", "Xdddddd", "Shit!",
        "_ZN8Particle7Handler29runEmitterInitialVelocityAxisEmmRK4Vec3RK7VecFx16RKlRKsSA_PNS_14ControllerBaseE"
    };

    CockhuntListener(MammaMia plugin)
    {
        this.plugin = plugin;
        this.mhMgr = plugin.getMhMgr();
    }

    void start()
    {
        this.updaterTaskID = Bukkit.getScheduler().scheduleSyncRepeatingTask(this.plugin, this::onTick, 0L, 20L);
        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    }

    void stop()
    {
        HandlerList.unregisterAll(this);
        Bukkit.getScheduler().cancelTask(this.updaterTaskID);
    }

    private void onTick()
    {
        int chance = this.random.nextInt(500);
        if (chance == 0)
        {
            Player player = this.getNextRandomPlayer(this.rngPlayers);
            if (player != null)
            {
                int teamID = this.mhMgr.getPlayerTeam(player);
                String msg = this.getNextRandomMsg(player, teamID);
                switch(teamID)
                {
                    case ManhuntTeam.Hunters:
                        player.sendMessage(ChatColor.GOLD + msg);
                        break;
                    case ManhuntTeam.Runners:
                        player.sendMessage(ChatColor.RED + msg);
                }
            }
        }
        else if (chance == 1)
        {
            Player player = this.getNextRandomPlayer(this.rngChatPlayers);
            if (player != null)
            {
                String msg = getNextRandomChatMsg(player);
                player.chat(msg);
            }
        }
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event)
    {
        Player player = event.getEntity();
        if (this.mhMgr.getPlayerInTeam(player, ManhuntTeam.Runners))
        {
            String playerName = player.getName();
            for (Player hunter : this.mhMgr.getTeamPlayers(ManhuntTeam.Hunters))
            {
                hunter.sendMessage(playerName + " died, enjoy his pingas.");
                ItemStack stack = new ItemStack(Material.END_ROD, 1);
                ItemMeta meta = stack.getItemMeta();
                meta.setDisplayName(player.getName() + "'s PINGAS");
                stack.setItemMeta(meta);
                hunter.getInventory().addItem(stack);
            }
        }
    }

    private Player getNextRandomPlayer(ArrayList<Player> rngPlayers)
    {
        ArrayList<Player> players = this.mhMgr.getParticipants();
        int playerCount = players.size();
        if (playerCount == 0)
            return null;

        int rngPlayerCount = rngPlayers.size();
        if (rngPlayerCount >= playerCount)
            rngPlayers.clear();

        Player player;
        do {
            player = players.get(this.random.nextInt(playerCount));
        } while(rngPlayers.contains(player));

        rngPlayers.add(player);
        return player;
    }

    private String getNextRandomMsg(Player player, int teamID)
    {
        ArrayList<String> rngMsgs = this.rngCockMsg.computeIfAbsent(player, k -> new ArrayList<>());
        String[] cockMsgs;
        switch(teamID)
        {
            case ManhuntTeam.Hunters:
                cockMsgs = this.hunterCockMsg;
                break;
            case ManhuntTeam.Runners:
                cockMsgs = this.runnerCockMsg;
                break;
            default:
                return null;
        }

        if (rngMsgs.size() >= cockMsgs.length)
            rngMsgs.clear();

        String msg;
        do {
            msg = cockMsgs[this.random.nextInt(cockMsgs.length)];
        } while(rngMsgs.contains(msg));

        rngMsgs.add(msg);
        return msg;
    }

    private String getNextRandomChatMsg(Player player)
    {
        ArrayList<String> rngMsgs = this.rngChatMsg.computeIfAbsent(player, k -> new ArrayList<>());
        if (rngMsgs.size() >= chatMsg.length)
            rngMsgs.clear();

        String msg;
        do {
            msg = chatMsg[this.random.nextInt(chatMsg.length)];
        } while(rngMsgs.contains(msg));

        rngMsgs.add(msg);
        return msg;
    }
}
