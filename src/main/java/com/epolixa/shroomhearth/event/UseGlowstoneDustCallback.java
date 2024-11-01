package com.epolixa.shroomhearth.event;

import com.epolixa.shroomhearth.Shroomhearth;
import com.epolixa.shroomhearth.ShroomhearthUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.joml.Vector3f;
import net.minecraft.world.World;
import org.apache.commons.lang3.ArrayUtils;

import static net.minecraft.block.Block.dropStack;

public class UseGlowstoneDustCallback {

    private static final int[] lightLevels = {6, 9, 12, 15};
    private static final TagKey<Item> DUST_SCRAPING_TOOLS = TagKey.of(RegistryKeys.ITEM, Identifier.of(Shroomhearth.MOD_ID, "dust_scraping_tools"));

    public static ActionResult onUseGlowstoneDustCallback(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        try {
            BlockPos pos = hitResult.getBlockPos();
            BlockState state = world.getBlockState(pos);
            ItemStack handItemStack = player.getStackInHand(hand);
            if (!state.isAir() && handItemStack.isOf(Items.GLOWSTONE_DUST) && !player.isSneaking()) {
                // Get block state adjacent to hit result
                BlockState sideState = getSideState(world, pos, hitResult.getSide());

                // If light block is already there, increase its level
                if (sideState != null && sideState.getBlock().equals(Blocks.LIGHT)) {
                    int level = sideState.get(Properties.LEVEL_15);
                    // If level is less than max light level
                    if (level < lightLevels[lightLevels.length - 1]) {
                        // Find the next highest light level
                        for (int ll : lightLevels) {
                            if (level < ll) {
                                return placeLightBlockWithDust(world, player, hand, getSidePos(pos, hitResult.getSide()), ll, handItemStack);
                            }
                        }
                    }

                // If light block is not there, but water or air are, place a new light block
                } else if (sideState.getBlock().equals(Blocks.WATER) || sideState.isAir()) {
                    return placeLightBlockWithDust(world, player, hand, getSidePos(pos, hitResult.getSide()), lightLevels[0], handItemStack);
                }
            }
        } catch (Exception e) {
            Shroomhearth.LOG.error("Caught error: " + e);
            e.printStackTrace();
        }
        return ActionResult.PASS;
    }


    public static ActionResult onUseScrapingToolOnGlowstoneDustCallback(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        try {
            BlockPos pos = hitResult.getBlockPos();
            BlockState state = world.getBlockState(pos);
            ItemStack handItemStack = player.getStackInHand(hand);
            if (!state.isAir() && handItemStack.isIn(DUST_SCRAPING_TOOLS) && !player.isSneaking()) {
                Direction side = hitResult.getSide();
                BlockState sideState = getSideState(world, pos, side);
                if (sideState != null && sideState.getBlock().equals(Blocks.LIGHT)) {
                    return scrapeLightBlockWithTool(world, player, hand, getSidePos(pos, side), handItemStack);
                }
            }
        } catch (Exception e) {
            Shroomhearth.LOG.error("Caught error: " + e);
            e.printStackTrace();
        }
        return ActionResult.PASS;
    }


    private static ActionResult placeLightBlockWithDust(World world, PlayerEntity player, Hand hand, BlockPos pos, int level, ItemStack handItemStack) {
        try {
            BlockState blockState = world.getBlockState(pos);
            boolean waterlogged = false;
            if (blockState.getBlock() == Blocks.WATER) {
                waterlogged = true;
            } else if (blockState.getBlock() == Blocks.LIGHT) {
                waterlogged = blockState.get(Properties.WATERLOGGED);
            }
            world.setBlockState(pos, Blocks.LIGHT.getDefaultState().with(Properties.LEVEL_15, level).with(Properties.WATERLOGGED, waterlogged));
            player.swingHand(hand, true);
            world.playSound(null, pos, SoundEvents.BLOCK_POWDER_SNOW_PLACE, SoundCategory.BLOCKS, 1f, 2f);
            ((ServerWorld)world).spawnParticles(
                    new DustParticleEffect(16759902, 1.0f),
                    pos.getX() + 0.5,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5,
                    (level * 4) / 15,
                    0.25f, 0.25f, 0.25f, 0.1f);
            if (!player.isCreative()) {
                handItemStack.decrement(1);
            }
            ShroomhearthUtils.grantAdvancement(player, "shroomhearth_fabric", "there_be_light", "impossible");
            return ActionResult.SUCCESS;
        } catch (Exception e) {
            Shroomhearth.LOG.error("Caught error: " + e);
            e.printStackTrace();
        }
        return ActionResult.PASS;
    }


    private static ActionResult scrapeLightBlockWithTool(World world, PlayerEntity player, Hand hand, BlockPos pos, ItemStack handItemStack) {
        try {
            BlockState blockState = world.getBlockState(pos);
            int level = blockState.get(Properties.LEVEL_15);
            boolean waterlogged = blockState.get(Properties.WATERLOGGED);
            world.setBlockState(pos, waterlogged ? Blocks.WATER.getDefaultState() : Blocks.AIR.getDefaultState());
            player.swingHand(hand, true);
            world.playSound(null, pos, SoundEvents.ITEM_AXE_SCRAPE, SoundCategory.BLOCKS, 1f, 2f);
            ((ServerWorld)world).spawnParticles(
                    new DustParticleEffect(16759902, 1.0f),
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
                dropStack(world, pos, new ItemStack(Items.GLOWSTONE_DUST, dust));
                handItemStack.damage(1, player, ShroomhearthUtils.getEquipmentSlotFromHand(hand));
            }
            return ActionResult.SUCCESS;
        } catch (Exception e) {
            Shroomhearth.LOG.error("Caught error: " + e);
            e.printStackTrace();
        }
        return ActionResult.PASS;
    }


    private static BlockState getSideState(World world, BlockPos pos, Direction side) {
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
            return new BlockPos(pos.add(side.getOffsetX(), side.getOffsetY(), side.getOffsetZ()));
        } catch (Exception e) {
            Shroomhearth.LOG.error("Caught error: " + e);
            e.printStackTrace();
        }
        return null;
    }
}
