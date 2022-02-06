package com.thegameratort.mammamia.kit;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.LinkedHashMap;
import java.util.Map;

@SerializableAs("MM_KitItem")
public class KitItem implements ConfigurationSerializable {
    public int slot;
    public ItemStack stack;

    public KitItem(int slot, ItemStack stack) {
        this.slot = slot;
        this.stack = stack;
    }

    @Override
    @NotNull
    public Map<String, Object> serialize() {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("slot", slot);
        result.put("stack", stack);
        return result;
    }

    @NotNull
    public static KitItem deserialize(@NotNull Map<String, Object> args) {
        int slot = (Integer) args.get("slot");
        ItemStack stack = (ItemStack) args.get("stack");
        return new KitItem(slot, stack);
    }
}
