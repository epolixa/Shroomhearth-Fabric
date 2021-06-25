package com.epolixa.bityard.event;

import com.epolixa.bityard.Bityard;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public class UseGlowstoneDustCallback {

    public static ActionResult onUseGlowstoneDustCallback(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        try {
            ItemStack handItemStack = player.getStackInHand(hand);
            if (handItemStack.isOf(Items.GLOWSTONE_DUST)) {
                BlockPos pos = hitResult.getBlockPos();
                BlockState state = world.getBlockState(pos);

                state.getBlock().

                if (!state.isAir()) {
                    Direction side = hitResult.getSide();
                    BlockPos sidePos = new BlockPos(pos.add(side.getOffsetX(), side.getOffsetY(), side.getOffsetZ()));
                    BlockState sideState = world.getBlockState(sidePos);

                    if (sideState.getBlock() == Blocks.LIGHT) {
                        int level = sideState.get(Properties.LEVEL_15);
                        if (level < 15) {
                            placeLightBlockWithDust(world, player, sidePos, level + 1, hand, handItemStack);
                        }
                    } else if (sideState.isAir()) {
                        placeLightBlockWithDust(world, player, sidePos, 1, hand, handItemStack);
                    }
                }
            }

        } catch (Exception e) {
            Bityard.LOG.error("Caught error: " + e);
            e.printStackTrace();
        }
        return ActionResult.PASS;
    }


    private static void placeLightBlockWithDust(World world, PlayerEntity player, BlockPos pos, int level, Hand hand, ItemStack handItemStack) {
        try {
            world.setBlockState(pos, Blocks.LIGHT.getDefaultState().with(Properties.LEVEL_15, level));
            world.playSound(null, pos, SoundEvents.BLOCK_POWDER_SNOW_PLACE, SoundCategory.BLOCKS, 1f, 2f);
            player.swingHand(hand, true);
            if (!player.isCreative()) {
                handItemStack.setCount(handItemStack.getCount() - 1);
            }
        } catch (Exception e) {
            Bityard.LOG.error("Caught error: " + e);
            e.printStackTrace();
        }
    }
}
