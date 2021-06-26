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

                ActionResult actionResult = state.onUse(world, player, hand, hitResult);

                if (actionResult.isAccepted()) {
                    return ActionResult.FAIL;
                } else if (!state.isAir()) {
                    Direction side = hitResult.getSide();
                    BlockPos sidePos = new BlockPos(pos.add(side.getOffsetX(), side.getOffsetY(), side.getOffsetZ()));
                    BlockState sideState = world.getBlockState(sidePos);

                    if (sideState.getBlock() == Blocks.LIGHT) {
                        int level = sideState.get(Properties.LEVEL_15);
                        if (level < 15) {
                            placeLightBlockWithDust(world, player, sidePos, level + 1, handItemStack);
                            return ActionResult.SUCCESS;
                        }
                    } else if (sideState.isAir() || sideState.getBlock() == Blocks.WATER) {
                        placeLightBlockWithDust(world, player, sidePos, 1, handItemStack);
                        return ActionResult.SUCCESS;
                    }
                }
            }

        } catch (Exception e) {
            Bityard.LOG.error("Caught error: " + e);
            e.printStackTrace();
        }
        return ActionResult.PASS;
    }


    private static void placeLightBlockWithDust(World world, PlayerEntity player, BlockPos pos, int level, ItemStack handItemStack) {
        try {
            BlockState blockState = world.getBlockState(pos);
            boolean waterlogged = false;
            if (blockState.getBlock() == Blocks.WATER) {
                waterlogged = true;
            } else if (blockState.getBlock() == Blocks.LIGHT) {
                waterlogged = blockState.get(Properties.WATERLOGGED);
            }
            world.setBlockState(pos, Blocks.LIGHT.getDefaultState().with(Properties.LEVEL_15, level).with(Properties.WATERLOGGED, waterlogged));
            world.playSound(null, pos, SoundEvents.BLOCK_POWDER_SNOW_PLACE, SoundCategory.BLOCKS, 1f, 2f);
            if (!player.isCreative()) {
                handItemStack.decrement(1);
            }
        } catch (Exception e) {
            Bityard.LOG.error("Caught error: " + e);
            e.printStackTrace();
        }
    }
}
