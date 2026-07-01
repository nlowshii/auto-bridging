package com.lowsii.autobridge;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.AABB;

import java.util.List;

/**
 * Turns the player's actual camera towards a nearby hostile mob, or a
 * neutral mob that is currently targeting the player, and attacks it.
 * The camera really turns (visible rotation, no packet spoofing), same
 * philosophy as AutoBridgeHandler.
 */
public class AutoAttackHandler {

    private static final double RANGE = 3.0;
    private static final float ATTACK_STRENGTH_THRESHOLD = 0.9f;

    public static void tick(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null || client.level == null || client.gameMode == null) {
            return;
        }

        LivingEntity target = findTarget(client, player);
        if (target == null) {
            return;
        }

        player.lookAt(EntityAnchorArgument.Anchor.EYES, target.position().add(0, target.getBbHeight() / 2.0, 0));

        if (player.getAttackStrengthScale(0.0f) < ATTACK_STRENGTH_THRESHOLD) {
            return;
        }

        client.gameMode.attack(player, target);
        player.swing(InteractionHand.MAIN_HAND);
    }

    private static LivingEntity findTarget(Minecraft client, LocalPlayer player) {
        AABB searchBox = player.getBoundingBox().inflate(RANGE);
        List<Entity> nearby = client.level.getEntities(player, searchBox);

        LivingEntity closest = null;
        double closestDistSq = Double.MAX_VALUE;

        for (Entity entity : nearby) {
            if (!(entity instanceof LivingEntity living) || !living.isAlive()) {
                continue;
            }

            boolean isHostile = living instanceof Enemy;
            boolean isRetaliating = living instanceof Mob mob && mob.getTarget() == player;

            if (!isHostile && !isRetaliating) {
                continue;
            }

            double distSq = player.distanceToSqr(living);
            if (distSq < closestDistSq) {
                closestDistSq = distSq;
                closest = living;
            }
        }

        return closest;
    }
}
