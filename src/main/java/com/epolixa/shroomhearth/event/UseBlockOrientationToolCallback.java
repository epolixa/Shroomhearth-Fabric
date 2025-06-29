package com.epolixa.shroomhearth.event;

import com.epolixa.shroomhearth.Shroomhearth;
import com.epolixa.shroomhearth.ShroomhearthUtils;
import net.minecraft.block.*;
import net.minecraft.block.enums.Attachment;
import net.minecraft.block.enums.BlockFace;
import net.minecraft.block.enums.ChestType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.property.Properties;
import net.minecraft.state.property.Property;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

public class UseBlockOrientationToolCallback {

    private static final TagKey<Item> BLOCK_ORIENTING_TOOLS = TagKey.of(RegistryKeys.ITEM, Identifier.of(Shroomhearth.MOD_ID, "block_orienting_tools"));
    private static final TagKey<Block> NON_ORIENTABLE = TagKey.of(RegistryKeys.BLOCK, Identifier.of(Shroomhearth.MOD_ID, "non_orientable"));

    public static ActionResult onUseBlockOrientationToolCallback(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        try {
            BlockPos pos = hitResult.getBlockPos();
            BlockState state = world.getBlockState(pos);
            ItemStack handItemStack = player.getStackInHand(hand);
            if (!state.isAir() && handItemStack.isIn(BLOCK_ORIENTING_TOOLS) && !player.isSneaking()) {
                if (state.isIn(NON_ORIENTABLE)) {
                    return ActionResult.PASS;
                } else if (state.getProperties().contains(Properties.FACING)) {
                    return cycleState(player, world, hand, state, pos, Properties.FACING);
                } else if (state.getProperties().contains(Properties.HORIZONTAL_FACING)) {
                    if (state.getBlock() instanceof AbstractChestBlock && state.getProperties().contains(Properties.CHEST_TYPE) && state.get(Properties.CHEST_TYPE) != ChestType.SINGLE) { // special case to fix double chests
                        Direction direction = state.get(Properties.HORIZONTAL_FACING);
                        direction = state.get(Properties.CHEST_TYPE) == ChestType.LEFT ? direction.rotateYClockwise() : direction.rotateYCounterclockwise();
                        BlockPos neighborPos = pos.offset(direction);
                        BlockState neighborState = world.getBlockState(neighborPos);
                        world.setBlockState(neighborPos, neighborState.with(Properties.CHEST_TYPE, ChestType.SINGLE));
                        state = state.with(Properties.CHEST_TYPE, ChestType.SINGLE);
                    } else if (state.getBlock() instanceof TrapdoorBlock && !state.get(Properties.OPEN)) { // special case to toggle trapdoors open first
                        return cycleState(player, world, hand, state, pos, Properties.OPEN);
                    } else if (state.getBlock() instanceof BigDripleafBlock && world.getBlockState(pos.down()).getBlock() instanceof BigDripleafStemBlock) { // skip big dripleaf if it is tall
                        return ActionResult.PASS;
                    } else if (state.getBlock() instanceof BellBlock && (state.get(Properties.ATTACHMENT) == Attachment.SINGLE_WALL || state.get(Properties.ATTACHMENT) == Attachment.DOUBLE_WALL)) { // skip bell if wall attached
                        return ActionResult.PASS;
                    } else if ((state.getBlock() instanceof LeverBlock || state.getBlock() instanceof ButtonBlock) && state.get(Properties.BLOCK_FACE) == BlockFace.WALL) { // skip wall levers and buttons
                        return ActionResult.PASS;
                    }
                    return cycleState(player, world, hand, state, pos, Properties.HORIZONTAL_FACING);
                } else if (state.getProperties().contains(Properties.ORIENTATION)) {
                    return cycleState(player, world, hand, state, pos, Properties.ORIENTATION);
                } else if (state.getProperties().contains(Properties.RAIL_SHAPE)) {
                    return cycleState(player, world, hand, state, pos, Properties.RAIL_SHAPE);
                } else if (state.getProperties().contains(Properties.STRAIGHT_RAIL_SHAPE)) {
                    return cycleState(player, world, hand, state, pos, Properties.STRAIGHT_RAIL_SHAPE);
                } else if (state.getProperties().contains(Properties.AXIS)) {
                    return cycleState(player, world, hand, state, pos, Properties.AXIS);
                } else if (state.getProperties().contains(Properties.HOPPER_FACING)) {
                    return cycleState(player, world, hand, state, pos, Properties.HOPPER_FACING);
                } else if (state.getProperties().contains(Properties.ROTATION)) {
                    return cycleState(player, world, hand, state, pos, Properties.ROTATION);
                } else if (state.getProperties().contains(Properties.BLOCK_HALF)) {
                    return cycleState(player, world, hand, state, pos, Properties.BLOCK_HALF);
                }
            }
        } catch (Exception e) {
            Shroomhearth.LOG.error("Caught error: " + e);
            e.printStackTrace();
        }
        return ActionResult.PASS;
    }

    public static ActionResult cycleState(PlayerEntity player, World world, Hand hand, BlockState state, BlockPos pos, Property property) {
        BlockState nextState = state.cycle(property);

        // special case for rails
        if (property == Properties.RAIL_SHAPE || property == Properties.STRAIGHT_RAIL_SHAPE) nextState = fixRailState(world, nextState, pos, property);

        world.setBlockState(pos, nextState, Block.NOTIFY_LISTENERS);
        world.updateNeighborsAlways(pos, state.getBlock(), null);
        player.swingHand(hand, true);
        world.playSound(null, pos, state.getSoundGroup().getHitSound(), SoundCategory.BLOCKS, 0.8f, 1.1f);
        ShroomhearthUtils.grantAdvancement(player, "shroomhearth_fabric", "orient_block", "impossible");
        return ActionResult.SUCCESS;
    }

    public static BlockState fixRailState(BlockView world, BlockState state, BlockPos pos, Property property) {
        boolean canAscend = false;
        BlockPos adjPos = null;
        if (state.getProperties().contains(Properties.RAIL_SHAPE)) {
            while (state.get(Properties.RAIL_SHAPE).isAscending()) {
                switch(state.get(Properties.RAIL_SHAPE)) {
                    case ASCENDING_EAST:
                        adjPos = pos.east();
                        break;
                    case ASCENDING_NORTH:
                        adjPos = pos.north();
                        break;
                    case ASCENDING_SOUTH:
                        adjPos = pos.south();
                        break;
                    case ASCENDING_WEST:
                        adjPos = pos.west();
                        break;
                    default:
                        break;
                }
                canAscend = Block.hasTopRim(world, adjPos);
                if (canAscend) break;
                else state = state.cycle(property);
            }
        } else if (state.getProperties().contains(Properties.STRAIGHT_RAIL_SHAPE)) {
            while (state.get(Properties.STRAIGHT_RAIL_SHAPE).isAscending()) {
                switch(state.get(Properties.STRAIGHT_RAIL_SHAPE)) {
                    case ASCENDING_EAST:
                        adjPos = pos.east();
                        break;
                    case ASCENDING_NORTH:
                        adjPos = pos.north();
                        break;
                    case ASCENDING_SOUTH:
                        adjPos = pos.south();
                        break;
                    case ASCENDING_WEST:
                        adjPos = pos.west();
                        break;
                    default:
                        break;
                }
                canAscend = Block.hasTopRim(world, adjPos);
                if (canAscend) break;
                else state = state.cycle(property);
            }
        }
        return state;
    }
}
