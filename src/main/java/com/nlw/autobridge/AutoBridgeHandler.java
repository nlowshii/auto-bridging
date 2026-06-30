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

    public static void tick(Minecraft client) {
        LocalPlayer player = client.player;
        if (player == null || client.level == null || client.gameMode == null) {
            return;
        }

        ItemStack held = player.getMainHandItem();
        if (!(held.getItem() instanceof BlockItem)) {
            return;
        }

        BlockPos feet = player.blockPosition();
        BlockPos underFeet = feet.below();

        if (!client.level.getBlockState(underFeet).isAir()) {
            return;
        }

        Direction facing = Direction.fromYRot(player.getYRot());
        BlockPos target = underFeet.relative(facing.getOpposite());

        if (!client.level.getBlockState(target).isAir()) {
            return;
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
            return;
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
    }
}
