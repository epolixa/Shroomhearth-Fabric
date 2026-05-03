package com.epolixa.shroomhearth.event;

import com.epolixa.shroomhearth.Shroomhearth;
import com.epolixa.shroomhearth.ShroomhearthUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import org.joml.Vector3f;
import org.apache.commons.lang3.ArrayUtils;

import static net.minecraft.world.level.block.Block.popResourceFromFace;

public class UseGlowstoneDustCallback {

    private static final int[] lightLevels = {6, 9, 12, 15};
    private static final TagKey<Item> DUST_SCRAPING_TOOLS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Shroomhearth.MOD_ID, "dust_scraping_tools"));

    public static InteractionResult onUseGlowstoneDustCallback(Player player, Level world, InteractionHand hand, BlockHitResult hitResult) {
        try {
            BlockPos pos = hitResult.getBlockPos();
            BlockState state = world.getBlockState(pos);
            ItemStack handItemStack = player.getItemInHand(hand);
            if (!state.isAir() && handItemStack.is(Items.GLOWSTONE_DUST) && !player.isShiftKeyDown()) {
                // Get block state adjacent to hit result
                BlockState sideState = getSideState(world, pos, hitResult.getDirection());

                // If light block is already there, increase its level
                if (sideState != null && sideState.getBlock().equals(Blocks.LIGHT)) {
                    int level = sideState.getValue(BlockStateProperties.LEVEL);
                    // If level is less than max light level
                    if (level < lightLevels[lightLevels.length - 1]) {
                        // Find the next highest light level
                        for (int ll : lightLevels) {
                            if (level < ll) {
                                return placeLightBlockWithDust(world, player, hand, getSidePos(pos, hitResult.getDirection()), ll, handItemStack);
                            }
                        }
                    }

                // If light block is not there, but water or air are, place a new light block
                } else if (sideState.getBlock().equals(Blocks.WATER) || sideState.isAir()) {
                    return placeLightBlockWithDust(world, player, hand, getSidePos(pos, hitResult.getDirection()), lightLevels[0], handItemStack);
                }
            }
        } catch (Exception e) {
            Shroomhearth.LOG.error("Caught error: " + e);
            e.printStackTrace();
        }
        return InteractionResult.PASS;
    }


    public static InteractionResult onUseScrapingToolOnGlowstoneDustCallback(Player player, Level world, InteractionHand hand, BlockHitResult hitResult) {
        try {
            BlockPos pos = hitResult.getBlockPos();
            BlockState state = world.getBlockState(pos);
            ItemStack handItemStack = player.getItemInHand(hand);
            if (!state.isAir() && handItemStack.is(DUST_SCRAPING_TOOLS) && !player.isShiftKeyDown()) {
                Direction side = hitResult.getDirection();
                BlockState sideState = getSideState(world, pos, side);
                if (sideState != null && sideState.getBlock().equals(Blocks.LIGHT)) {
                    return scrapeLightBlockWithTool(world, player, hand, getSidePos(pos, side), handItemStack);
                }
            }
        } catch (Exception e) {
            Shroomhearth.LOG.error("Caught error: " + e);
            e.printStackTrace();
        }
        return InteractionResult.PASS;
    }


    private static InteractionResult placeLightBlockWithDust(Level world, Player player, InteractionHand hand, BlockPos pos, int level, ItemStack handItemStack) {
        try {
            BlockState blockState = world.getBlockState(pos);
            boolean waterlogged = false;
            if (blockState.getBlock() == Blocks.WATER) {
                waterlogged = true;
            } else if (blockState.getBlock() == Blocks.LIGHT) {
                waterlogged = blockState.getValue(BlockStateProperties.WATERLOGGED);
            }
            world.setBlockAndUpdate(pos, Blocks.LIGHT.defaultBlockState().setValue(BlockStateProperties.LEVEL, level).setValue(BlockStateProperties.WATERLOGGED, waterlogged));
            player.swing(hand, true);
            world.playSound(null, pos, SoundEvents.POWDER_SNOW_PLACE, SoundSource.BLOCKS, 1f, 2f);
            ((ServerLevel)world).sendParticles(
                    new DustParticleOptions(16759902, 1.0f),
                    pos.getX() + 0.5,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5,
                    (level * 4) / 15,
                    0.25f, 0.25f, 0.25f, 0.1f);
            if (!player.isCreative()) {
                handItemStack.shrink(1);
            }
            ShroomhearthUtils.grantAdvancement(player, "shroomhearth_fabric", "there_be_light", "impossible");
            return InteractionResult.SUCCESS;
        } catch (Exception e) {
            Shroomhearth.LOG.error("Caught error: " + e);
            e.printStackTrace();
        }
        return InteractionResult.PASS;
    }


    private static InteractionResult scrapeLightBlockWithTool(Level world, Player player, InteractionHand hand, BlockPos pos, ItemStack handItemStack) {
        try {
            BlockState blockState = world.getBlockState(pos);
            int level = blockState.getValue(BlockStateProperties.LEVEL);
            boolean waterlogged = blockState.getValue(BlockStateProperties.WATERLOGGED);
            world.setBlockAndUpdate(pos, waterlogged ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState());
            player.swing(hand, true);
            world.playSound(null, pos, SoundEvents.AXE_SCRAPE, SoundSource.BLOCKS, 1f, 2f);
            ((ServerLevel)world).sendParticles(
                    new DustParticleOptions(16759902, 1.0f),
                    pos.getX() + 0.5,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5,
                    4, 0.25f, 0.25f, 0.25f, 0.1f);
            if (!player.isCreative()) {
                int dust = 1;
                for (int ll : lightLevels) {
                    if (level <= ll) {
                        dust += ArrayUtils.indexOf(lightLevels, ll);
                        break;
                    }
                }
                popResource(world, pos, new ItemStack(Items.GLOWSTONE_DUST, dust));
                handItemStack.hurtAndBreak(1, player, ShroomhearthUtils.getEquipmentSlotFromHand(hand));
            }
            return InteractionResult.SUCCESS;
        } catch (Exception e) {
            Shroomhearth.LOG.error("Caught error: " + e);
            e.printStackTrace();
        }
        return InteractionResult.PASS;
    }


    private static BlockState getSideState(Level world, BlockPos pos, Direction side) {
        try {
            BlockPos sidePos = getSidePos(pos, side);
            if (sidePos != null) {
                return world.getBlockState(getSidePos(pos, side));
            }
        } catch (Exception e) {
            Shroomhearth.LOG.error("Caught error: " + e);
            e.printStackTrace();
        }
        return null;
    }


    private static BlockPos getSidePos(BlockPos pos, Direction side) {
        try {
            return new BlockPos(pos.offset(side.getStepX(), side.getStepY(), side.getStepZ()));
        } catch (Exception e) {
            Shroomhearth.LOG.error("Caught error: " + e);
            e.printStackTrace();
        }
        return null;
    }
}
