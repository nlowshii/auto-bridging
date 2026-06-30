package com.lowsii.autobridge;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class AutoBridgeClient implements ClientModInitializer {

    public static KeyMapping toggleKey;
    public static boolean enabled = false;

    @Override
    public void onInitializeClient() {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
                "key.autobridge.toggle",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                "category.autobridge"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.consumeClick()) {
                enabled = !enabled;
                if (client.player != null) {
                    client.player.displayClientMessage(
                            Component.literal("[AutoBridge] " + (enabled ? "ON" : "OFF")),
                            true
                    );
                }
            }

            if (enabled) {
                AutoBridgeHandler.tick(client);
            }
        });
    }
}
