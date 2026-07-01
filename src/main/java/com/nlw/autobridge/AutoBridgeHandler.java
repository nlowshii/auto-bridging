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

public class AutoBridgeHandler {

    private static final double MOVE_THRESHOLD_SQ = 0.0009;

    private static int groundY = Integer.MIN_VALUE;

    public static void tick(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null || client.level == null || client.gameMode == null) {
            return;
        }

        ItemStack held = player.getMainHandItem();
        if (!(held.getItem() instanceof BlockItem)) {
            return;
        }

        BlockPos rawFeet = player.blockPosition();

        if (player.onGround()) {
            groundY = rawFeet.getY();
        } else if (groundY == Integer.MIN_VALUE) {
            groundY = rawFeet.getY();
        }

        BlockPos feet = new BlockPos(rawFeet.getX(), groundY, rawFeet.getZ());
        Direction moveDir = getMovementDirection(player);
        BlockPos aheadFoot = feet.relative(moveDir);

        BlockPos[] candidates = {
                feet.below(),
                aheadFoot.below()
        };

        for (BlockPos target : candidates) {
            if (tryPlace(client, player, target)) {
                return;
            }
        }
    }

    private static boolean tryPlace(Minecraft client, LocalPlayer player, BlockPos target) {
        if (!client.level.getBlockState(target).isAir()) {
            return false;
        }

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
            return false;
        }

        Vec3 hitVec = new Vec3(
                target.getX() + 0.5,
                target.getY() + 0.5,
                target.getZ() + 0.5
        );

        player.lookAt(EntityAnchorArgument.Anchor.EYES, hitVec);

        BlockHitResult hitResult = new BlockHitResult(hitVec, placeAgainst, supportPos, false);

        client.gameMode.useItemOn(player, InteractionHand.MAIN_HAND, hitResult);
        player.swing(InteractionHand.MAIN_HAND);
        return true;
    }

    private static Direction getMovementDirection(LocalPlayer player) {
        double dx = player.getDeltaMovement().x;
        double dz = player.getDeltaMovement().z;
        double horizontalSpeedSq = dx * dx + dz * dz;

        if (horizontalSpeedSq > MOVE_THRESHOLD_SQ) {
            float moveYaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
            return Direction.fromYRot(moveYaw);
        }

        return Direction.fromYRot(player.getYRot());
    }
}
