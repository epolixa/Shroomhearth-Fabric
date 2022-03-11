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
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.tag.TagKey;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3f;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import org.apache.commons.lang3.ArrayUtils;

import static net.minecraft.block.Block.dropStack;

public class UseGlowstoneDustCallback {

    private static final int[] lightLevels = {6, 9, 12, 15};
    private static final TagKey<Item> DUST_SCRAPING_TOOLS = TagKey.of(Registry.ITEM_KEY, new Identifier(Shroomhearth.MOD_ID, "dust_scraping_tools"));

    public static ActionResult onUseGlowstoneDustCallback(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        try {
            BlockPos pos = hitResult.getBlockPos();
            BlockState state = world.getBlockState(pos);
            if (!state.isAir()) {
                ActionResult actionResult = player.isSneaking() ? ActionResult.PASS : state.onUse(world, player, hand, hitResult);
                if (actionResult.isAccepted()) {
                    return ActionResult.FAIL;
                } else {
                    Direction side = hitResult.getSide();
                    BlockPos sidePos = new BlockPos(pos.add(side.getOffsetX(), side.getOffsetY(), side.getOffsetZ()));
                    BlockState sideState = world.getBlockState(sidePos);
                    ItemStack handItemStack = player.getStackInHand(hand);
                    if (handItemStack.isOf(Items.GLOWSTONE_DUST)) {
                        if (sideState.getBlock() == Blocks.LIGHT) {
                            int level = sideState.get(Properties.LEVEL_15);
                            if (level < lightLevels[lightLevels.length - 1]) {
                                for (int ll : lightLevels) {
                                    if (level < ll) {
                                        return placeLightBlockWithDust(world, player, sidePos, ll, handItemStack);
                                    }
                                }
                            }
                        } else if (sideState.getBlock() == Blocks.WATER || sideState.isAir()) {
                            return placeLightBlockWithDust(world, player, sidePos, lightLevels[0], handItemStack);
                        }
                    } else if (handItemStack.isIn(DUST_SCRAPING_TOOLS) && sideState.getBlock() == Blocks.LIGHT) {
                        return scrapeLightBlockWithTool(world, player, sidePos, hand, handItemStack);
                    }
                }
            }
        } catch (Exception e) {
            Shroomhearth.LOG.error("Caught error: " + e);
            e.printStackTrace();
        }
        return ActionResult.PASS;
    }


    private static ActionResult placeLightBlockWithDust(World world, PlayerEntity player, BlockPos pos, int level, ItemStack handItemStack) {
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
            ((ServerWorld)world).spawnParticles(
                    new DustParticleEffect(new Vec3f(1.0f, 0.9f, 0.1f), 1.0f),
                    pos.getX() + 0.5,
                    pos.getY() + 0.5,
                    pos.getZ() + 0.5,
                    4, 0.25f, 0.25f, 0.25f, 0.1f);
            if (!player.isCreative()) {
                handItemStack.decrement(1);
            }
            ShroomhearthUtils.grantAdvancement(player, "shroomhearth", "there_be_light", "impossible");
            return ActionResult.SUCCESS;
        } catch (Exception e) {
            Shroomhearth.LOG.error("Caught error: " + e);
            e.printStackTrace();
        }
        return ActionResult.PASS;
    }

    private static ActionResult scrapeLightBlockWithTool(World world, PlayerEntity player, BlockPos pos, Hand hand, ItemStack handItemStack) {
        try {
            BlockState blockState = world.getBlockState(pos);
            int level = blockState.get(Properties.LEVEL_15);
            boolean waterlogged = blockState.get(Properties.WATERLOGGED);
            world.setBlockState(pos, waterlogged ? Blocks.WATER.getDefaultState() : Blocks.AIR.getDefaultState());
            world.playSound(null, pos, SoundEvents.ITEM_AXE_SCRAPE, SoundCategory.BLOCKS, 1f, 2f);
            ((ServerWorld)world).spawnParticles(
                    new DustParticleEffect(new Vec3f(1.0f, 0.9f, 0.1f), 1.0f),
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
                handItemStack.damage(1, player, p -> p.sendToolBreakStatus(hand));
            }
            return ActionResult.SUCCESS;
        } catch (Exception e) {
            Shroomhearth.LOG.error("Caught error: " + e);
            e.printStackTrace();
        }
        return ActionResult.PASS;
    }
}
