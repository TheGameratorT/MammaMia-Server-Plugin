package com.thegameratort.mammamia.kit;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@SerializableAs("MM_Kit")
public class Kit implements ConfigurationSerializable {
    public String name;
    public ItemStack icon;
    public ItemStack helmet;
    public ItemStack chestplate;
    public ItemStack leggings;
    public ItemStack boots;
    public ItemStack offhand;
    public final ArrayList<KitItem> items = new ArrayList<>();
    public final ArrayList<String> flags = new ArrayList<>();

    public void giveToPlayer(Player player) {
        PlayerInventory inv = player.getInventory();
        inv.clear();

        inv.setHelmet(this.helmet);
        inv.setChestplate(this.chestplate);
        inv.setLeggings(this.leggings);
        inv.setBoots(this.boots);

        inv.setItemInOffHand(this.offhand);

        for (KitItem item : this.items) {
            inv.setItem(item.slot, item.stack);
        }

        player.updateInventory();
    }

    public void copyInventory(PlayerInventory inventory) {
        this.helmet = inventory.getHelmet();
        this.chestplate = inventory.getChestplate();
        this.leggings = inventory.getLeggings();
        this.boots = inventory.getBoots();
        this.offhand = inventory.getItemInOffHand();
        this.items.clear();
        int slotCount = inventory.getSize();
        for (int i = 0 ; i < slotCount; i++) {
            ItemStack stack = inventory.getItem(i);
            if (stack != null) {
                KitItem item = new KitItem(i, stack);
                this.items.add(item);
            }
        }
    }

    @Override
    @NotNull
    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<>();

        result.put("name", this.name);
        result.put("icon", this.icon);
        putIfNotNull(result, "helmet", this.helmet);
        putIfNotNull(result, "chestplate", this.chestplate);
        putIfNotNull(result, "leggings", this.leggings);
        putIfNotNull(result, "boots", this.boots);
        putIfNotNull(result, "offhand", this.offhand);
        putIfNotNull(result, "items", this.items);
        putIfNotNull(result, "flags", this.flags);

        return result;
    }

    @SuppressWarnings("unchecked")
    @NotNull
    public static Kit deserialize(@NotNull Map<String, Object> args) {
        Kit kit = new Kit();

        kit.name = (String) args.get("name");
        kit.icon = (ItemStack) args.get("icon");
        kit.helmet = (ItemStack) args.get("helmet");
        kit.chestplate = (ItemStack) args.get("chestplate");
        kit.leggings = (ItemStack) args.get("leggings");
        kit.boots = (ItemStack) args.get("boots");
        kit.offhand = (ItemStack) args.get("offhand");

        List<KitItem> savedItems = (List<KitItem>) args.get("items");
        if (savedItems != null) {
            kit.items.addAll(savedItems);
        }

        List<String> savedFlags = (List<String>) args.get("flags");
        if (savedFlags != null) {
            kit.flags.addAll(savedFlags);
        }

        return kit;
    }

    private void putIfNotNull(Map<String, Object> map, String key, Object val) {
        if (val != null) {
            map.put(key, val);
        }
    }
}
