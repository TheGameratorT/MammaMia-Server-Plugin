package com.thegameratort.mammamia;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;

public class LabyModListener implements PluginMessageListener
{
    // Force disables Minimap and Damage Indicators on LabyMod

    private static final String permsJson = "{\"IMPROVED_LAVA\":false,\"CROSSHAIR_SYNC\":false,\"REFILL_FIX\":false,\"GUI_ALL\":true,\"GUI_POTION_EFFECTS\":true,\"GUI_ARMOR_HUD\":true,\"GUI_ITEM_HUD\":true,\"BLOCKBUILD\":true,\"TAGS\":true,\"CHAT\":true,\"ANIMATIONS\":true,\"SATURATION_BAR\":true,\"RANGE\":false,\"SLOWDOWN\":false}";
    private static final String addonPermsJson = "{\"686fdf14-225d-4475-b062-9f68be19f4d1\":{\"allowed\":false},\"211222f3-2650-4463-b986-e1d29c46117f\":{\"allowed\":false},\"8d4ed1eb-4b1c-46bc-aac9-8abc8c6a1308\":{\"allowed\":false},\"5d77d5b6-bebc-45bf-82f2-336bba64b9b1\":{\"allowed\":false,\"fairplay\":true}}";
    private JsonObject permsJsonObj = null;
    private JsonObject addonPermsJsonObj = null;

    LabyModListener(MammaMia plugin)
    {
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, "labymod3:main", this);

        JsonParser parser = new JsonParser();
        permsJsonObj = parser.parse(permsJson).getAsJsonObject();
        addonPermsJsonObj = parser.parse(addonPermsJson).getAsJsonObject();
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte[] message)
    {
        if (!channel.equals("labymod3:main"))
            return;

        DataInputStream in = new DataInputStream(new ByteArrayInputStream(message));

        ByteBuf buf = Unpooled.wrappedBuffer(message);
        String key = LabyModProtocol.readString(buf, Short.MAX_VALUE);
        String json = LabyModProtocol.readString(buf, Short.MAX_VALUE);

        // LabyMod user joins the server
        if (key.equals("INFO"))
        {
            // Handle the json message
            LabyModProtocol.sendLabyModMessage(player, "PERMISSIONS", permsJsonObj);
            LabyModProtocol.sendLabyModMessage(player, "addon_permissions", addonPermsJsonObj);
        }
    }
}
