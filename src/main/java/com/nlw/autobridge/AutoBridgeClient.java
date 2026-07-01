package com.lowsii.autobridge;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class AutoBridgeClient implements ClientModInitializer {

    public enum BridgeMode {
        CLASSIC,
        DIAGONAL
    }

    private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath("autobridge", "main")
    );

    public static KeyMapping toggleKey;
    public static KeyMapping modeKey;
    public static KeyMapping attackToggleKey;

    public static boolean enabled = false;
    public static BridgeMode mode = BridgeMode.CLASSIC;
    public static boolean attackEnabled = false;

    @Override
    public void onInitializeClient() {
        toggleKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.autobridge.toggle",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_B,
                CATEGORY
        ));

        modeKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.autobridge.mode",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_N,
                CATEGORY
        ));

        attackToggleKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.autobridge.attack_toggle",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_M,
                CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (toggleKey.consumeClick()) {
                enabled = !enabled;
                if (client.player != null) {
                    client.player.sendSystemMessage(
                            Component.literal("[AutoBridge] " + (enabled ? "ON" : "OFF"))
                    );
                }
            }

            while (modeKey.consumeClick()) {
                mode = mode == BridgeMode.CLASSIC ? BridgeMode.DIAGONAL : BridgeMode.CLASSIC;
                if (client.player != null) {
                    client.player.sendSystemMessage(
                            Component.literal("[AutoBridge] Mode: " + mode.name())
                    );
                }
            }

            while (attackToggleKey.consumeClick()) {
                attackEnabled = !attackEnabled;
                if (client.player != null) {
                    client.player.sendSystemMessage(
                            Component.literal("[AutoAttack] " + (attackEnabled ? "ON" : "OFF"))
                    );
                }
            }

            if (enabled) {
                AutoBridgeHandler.tick(client);
            }

            if (attackEnabled) {
                AutoAttackHandler.tick(client);
            }
        });
    }
}
