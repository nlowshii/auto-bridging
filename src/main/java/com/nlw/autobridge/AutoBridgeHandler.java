package com.lowsii.autobridge;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Very small, "legit" style auto-bridge:
 * every client tick, if the block under the player's feet is a gap and the
 * player is holding a block, it looks at the gap and places a block there.
 *
 * It does NOT fake rotations server-side (no silent/packet rotation) — the
 * player's actual view is turned to face the placement, same as a manual
 * bridge, just automated. This keeps it closer to a QoL utility instead of
 * something built to dodge anti-cheat detection.
 *
 * NOTE: many multiplayer servers treat any automated block placement as
 * cheating regardless of how "legit" it looks, even on servers without strict
 * anti-cheat. Use this on singleplayer / your own server / places that
 * explicitly allow it.
 */
public class AutoBridgeHandler {

    public static void tick(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null || client.level == null || client.gameMode == null) {
            return;
        }

        ItemStack held = player.getMainHandItem();
        if (!(held.getItem() instanceof BlockItem)) {
            return; // nothing placeable in hand
        }

        BlockPos feet = player.blockPosition();
        BlockPos underFeet = feet.below();

        if (!client.level.getBlockState(underFeet).isAir()) {
            return; // already standing on solid ground, nothing to bridge yet
        }

        // Bridge in the direction the player is walking backwards away from
        Direction facing = Direction.fromYRot(player.getYRot());
        BlockPos target = underFeet.relative(facing.getOpposite());

        if (!client.level.getBlockState(target).isAir()) {
            return; // target already occupied
        }

        // Find a solid neighbour of the target block to place against
        Direction placeAgainst = null;
        BlockPos supportPos = null;
        for (Direction dir : Direction.values()) {
            BlockPos neighbour = target.relative(dir);
            if (!client.level.getBlockState(neighbour).isAir()) {
                placeAgainst = dir.getOpposite();
                supportPos = neighbour;
                break;
            }
        }
        if (placeAgainst == null) {
            return; // no support yet, keep walking until one exists
        }

        Vec3 hitVec = new Vec3(
                target.getX() + 0.5,
                target.getY() + 0.5,
                target.getZ() + 0.5
        );

        // Turn the player's actual view towards the placement spot
        player.lookAt(EntityAnchorArgument.Anchor.EYES, hitVec);

        BlockHitResult hitResult = new BlockHitResult(hitVec, placeAgainst, supportPos, false);

        client.gameMode.useItemOn(player, InteractionHand.MAIN_HAND, hitResult);
        player.swing(InteractionHand.MAIN_HAND);
    }
}
