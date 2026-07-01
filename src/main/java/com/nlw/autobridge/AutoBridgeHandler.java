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
        int[] offset = getMovementOffset(player);
        BlockPos aheadFoot = feet.offset(offset[0], 0, offset[1]);

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

    /**
     * Returns a horizontal offset {dx, dz}, each in {-1, 0, 1}, based on the
     * player's actual movement vector. In CLASSIC mode this snaps to one of
     * the 4 cardinal directions; in DIAGONAL mode it snaps to one of the 8
     * compass octants. Falls back to facing direction when not moving.
     */
    private static int[] getMovementOffset(LocalPlayer player) {
        double dx = player.getDeltaMovement().x;
        double dz = player.getDeltaMovement().z;
        double horizontalSpeedSq = dx * dx + dz * dz;

        float yaw;
        if (horizontalSpeedSq > MOVE_THRESHOLD_SQ) {
            yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        } else {
            yaw = player.getYRot();
        }

        double normalized = ((yaw % 360) + 360) % 360;

        if (AutoBridgeClient.mode == AutoBridgeClient.BridgeMode.CLASSIC) {
            int quadrant = (int) Math.round(normalized / 90.0) % 4;
            return switch (quadrant) {
                case 0 -> new int[]{0, 1};
                case 1 -> new int[]{-1, 0};
                case 2 -> new int[]{0, -1};
                default -> new int[]{1, 0};
            };
        }

        int octant = (int) Math.round(normalized / 45.0) % 8;
        return switch (octant) {
            case 0 -> new int[]{0, 1};
            case 1 -> new int[]{-1, 1};
            case 2 -> new int[]{-1, 0};
            case 3 -> new int[]{-1, -1};
            case 4 -> new int[]{0, -1};
            case 5 -> new int[]{1, -1};
            case 6 -> new int[]{1, 0};
            default -> new int[]{1, 1};
        };
    }
}
