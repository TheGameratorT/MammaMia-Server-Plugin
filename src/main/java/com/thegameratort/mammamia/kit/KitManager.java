package com.thegameratort.mammamia.kit;

import com.thegameratort.mammamia.ConfigFile;
import com.thegameratort.mammamia.MammaMia;
import com.thegameratort.mammamia.MenuUtils;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class KitManager {
    private final MammaMia plugin;
    private ConfigFile config;

    private final ArrayList<Kit> kits = new ArrayList<>();

    public KitManager(@NotNull MammaMia plugin) {
        this.plugin = plugin;
        loadKits();
        new KitCommand(this.plugin, this);
    }

    public void createKitFromPlayer(Player player, String name) {
        Kit kit = new Kit();
        PlayerInventory inv = player.getInventory();
        kit.name = name;
        kit.icon = getDefaultItemIcon(name);
        kit.copyInventory(inv);
        kits.add(kit);
        saveKits();
    }

    public void updateKit(Kit kit, Player player) {
        PlayerInventory inv = player.getInventory();
        kit.copyInventory(inv);
        saveKits();
    }

    public void removeKit(Kit kit) {
        kits.remove(kit);
        saveKits();
    }

    public void listKits(CommandSender sender) {
        StringBuilder msg = new StringBuilder();
        msg.append("================\n");
        msg.append("Kits\n");
        msg.append("================\n");
        for (Kit kit : kits) {
            msg.append(kit.name).append("\n");
        }
        msg.append("================\n");
        sender.sendMessage(msg.toString());
    }

    public void setKitIcon(Kit kit, ItemStack icon) {
        kit.icon = icon;
        saveKits();
    }

    public void setKitName(Kit kit, String name) {
        kit.name = name;
        saveKits();
    }

    public ArrayList<Kit> filterKitsByFlag(List<Kit> kits, String flag) {
        return filterKitsByFlags(kits, Collections.singletonList(flag));
    }

    public ArrayList<Kit> filterKitsByFlags(List<Kit> kits, List<String> flags) {
        ArrayList<Kit> filtered = new ArrayList<>();
        for (Kit kit : kits) {
            for (String flag : flags) {
                if (kit.flags.contains(flag)) {
                    filtered.add(kit);
                    break;
                }
            }
        }
        return filtered;
    }

    public Kit getKitByName(String name) {
        for (Kit kit : kits) {
            if (kit.name.equals(name)) {
                return kit;
            }
        }
        return null;
    }

    public ArrayList<String> getKitNames() {
        ArrayList<String> names = new ArrayList<>();
        for (Kit kit : kits) {
            names.add(kit.name);
        }
        return names;
    }

    public ArrayList<Kit> getKits() {
        return kits;
    }

    private ItemStack getDefaultItemIcon(String name) {
        ItemStack stack = new ItemStack(Material.BARRIER);
        ItemMeta meta = stack.getItemMeta();
        meta.setDisplayName(MenuUtils.defNameFmt(name));
        meta.setLore(Collections.singletonList(MenuUtils.defLoreFmt("A kit")));
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        stack.setItemMeta(meta);
        return stack;
    }

    public void reload() {
        kits.clear();
        loadKits();
    }

    @SuppressWarnings("unchecked")
    private void loadKits() {
        config = ConfigFile.loadConfig(plugin, "kits.yml");
        List<Kit> savedKits = (List<Kit>) config.getList("kits");
        if (savedKits != null) {
            kits.addAll(savedKits);
        }
    }

    private void saveKits() {
        config.set("kits", kits);
        config.saveConfig();
    }
}
