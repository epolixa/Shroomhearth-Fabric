package com.epolixa.shroomhearth.event;

import com.epolixa.shroomhearth.Shroomhearth;
import net.minecraft.block.*;
import net.minecraft.block.enums.Attachment;
import net.minecraft.block.enums.ChestType;
import net.minecraft.block.enums.WallMountLocation;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.tag.TagKey;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Position;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

public class UseBlockOrientationToolCallback {

    private static final TagKey<Item> BLOCK_ORIENTING_TOOLS = TagKey.of(Registry.ITEM_KEY, new Identifier(Shroomhearth.MOD_ID, "block_orienting_tools"));
    private static final TagKey<Block> NON_ORIENTABLE = TagKey.of(Registry.BLOCK_KEY, new Identifier(Shroomhearth.MOD_ID, "non_orientable"));

    public static ActionResult onUseBlockOrientationToolCallback(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        try {
            BlockPos pos = hitResult.getBlockPos();
            BlockState state = world.getBlockState(pos);
            Shroomhearth.LOG.info("[onUseBlockOrientationToolCallback] hit " + state.getBlock().getTranslationKey());
            if (!state.isAir()) {
                ActionResult actionResult = player.isSneaking() ? ActionResult.PASS : state.onUse(world, player, hand, hitResult);
                if (actionResult.isAccepted()) {
                    return ActionResult.FAIL;
                } else {
                    ItemStack handItemStack = player.getStackInHand(hand);
                    if (handItemStack.isIn(BLOCK_ORIENTING_TOOLS)) {
                        Shroomhearth.LOG.info("[onUseBlockOrientationToolCallback] using compass");
                        if (state.isIn(NON_ORIENTABLE)) {
                            return ActionResult.PASS;
                        } else if (state.getProperties().contains(Properties.FACING)) {
                            return cycleState(world, state, pos, Properties.FACING);
                        } else if (state.getProperties().contains(Properties.HORIZONTAL_FACING)) {
                            if (state.getBlock() instanceof AbstractChestBlock && state.get(Properties.CHEST_TYPE) != ChestType.SINGLE) { // special case to fix chests
                                Direction direction = state.get(Properties.HORIZONTAL_FACING);
                                direction = state.get(Properties.CHEST_TYPE) == ChestType.LEFT ? direction.rotateYClockwise() : direction.rotateYCounterclockwise();
                                BlockPos neighborPos = pos.offset(direction);
                                BlockState neighborState = world.getBlockState(neighborPos);
                                world.setBlockState(neighborPos, neighborState.with(Properties.CHEST_TYPE, ChestType.SINGLE));
                                state = state.with(Properties.CHEST_TYPE, ChestType.SINGLE);
                            } else if (state.getBlock() instanceof TrapdoorBlock && !state.get(Properties.OPEN)) { // special case to toggle trapdoors open first
                                return cycleState(world, state, pos, Properties.OPEN);
                            } else if (state.getBlock() instanceof BigDripleafBlock && world.getBlockState(pos.down()).getBlock() instanceof BigDripleafStemBlock) { // skip big dripleaf if it is tall
                                return ActionResult.PASS;
                            } else if (state.getBlock() instanceof BellBlock && (state.get(Properties.ATTACHMENT) == Attachment.SINGLE_WALL || state.get(Properties.ATTACHMENT) == Attachment.DOUBLE_WALL)) { // skip bell if wall attached
                                return ActionResult.PASS;
                            } else if ((state.getBlock() instanceof LeverBlock || state.getBlock() instanceof AbstractButtonBlock) && state.get(Properties.WALL_MOUNT_LOCATION) == WallMountLocation.WALL) { // skip wall levers and buttons
                                return ActionResult.PASS;
                            }
                            return cycleState(world, state, pos, Properties.HORIZONTAL_FACING);
                        } else if (state.getProperties().contains(Properties.AXIS)) {
                            return cycleState(world, state, pos, Properties.AXIS);
                        } else if (state.getProperties().contains(Properties.HOPPER_FACING)) {
                            return cycleState(world, state, pos, Properties.HOPPER_FACING);
                        } else if (state.getProperties().contains(Properties.ROTATION)) {
                            return cycleState(world, state, pos, Properties.ROTATION);
                        } else if (state.getProperties().contains(Properties.BLOCK_HALF)) {
                            return cycleState(world, state, pos, Properties.BLOCK_HALF);
                        }
                    }
                }
            }

        } catch (Exception e) {
            Shroomhearth.LOG.error("Caught error: " + e);
            e.printStackTrace();
        }
        return ActionResult.PASS;
    }

    public static ActionResult cycleState(World world, BlockState state, BlockPos pos, Property property) {
        Shroomhearth.LOG.info("[onUseBlockOrientationToolCallback] cycle " + property.getName());
        world.setBlockState(pos, state.cycle(property), Block.NOTIFY_LISTENERS);
        world.updateNeighborsAlways(pos, state.getBlock());
        world.playSound(null, pos, state.getBlock().getSoundGroup(state).getHitSound(), SoundCategory.BLOCKS, 0.8f, 1.1f);
        return ActionResult.SUCCESS;
    }
}
