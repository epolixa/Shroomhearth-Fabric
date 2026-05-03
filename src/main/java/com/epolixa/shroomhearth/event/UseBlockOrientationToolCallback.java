package com.epolixa.shroomhearth.event;

import com.epolixa.shroomhearth.Shroomhearth;
import com.epolixa.shroomhearth.ShroomhearthUtils;
import net.minecraft.block.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractChestBlock;
import net.minecraft.world.level.block.BellBlock;
import net.minecraft.world.level.block.BigDripleafBlock;
import net.minecraft.world.level.block.BigDripleafStemBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.LeverBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BellAttachType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;

public class UseBlockOrientationToolCallback {

    private static final TagKey<Item> BLOCK_ORIENTING_TOOLS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Shroomhearth.MOD_ID, "block_orienting_tools"));
    private static final TagKey<Block> NON_ORIENTABLE = TagKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(Shroomhearth.MOD_ID, "non_orientable"));

    public static InteractionResult onUseBlockOrientationToolCallback(Player player, Level world, InteractionHand hand, BlockHitResult hitResult) {
        try {
            BlockPos pos = hitResult.getBlockPos();
            BlockState state = world.getBlockState(pos);
            ItemStack handItemStack = player.getItemInHand(hand);
            if (!state.isAir() && handItemStack.is(BLOCK_ORIENTING_TOOLS) && !player.isShiftKeyDown()) {
                if (state.is(NON_ORIENTABLE)) {
                    return InteractionResult.PASS;
                } else if (state.getProperties().contains(BlockStateProperties.FACING)) {
                    return cycleState(player, world, hand, state, pos, BlockStateProperties.FACING);
                } else if (state.getProperties().contains(BlockStateProperties.HORIZONTAL_FACING)) {
                    if (state.getBlock() instanceof AbstractChestBlock && state.getProperties().contains(BlockStateProperties.CHEST_TYPE) && state.getValue(BlockStateProperties.CHEST_TYPE) != ChestType.SINGLE) { // special case to fix double chests
                        Direction direction = state.getValue(BlockStateProperties.HORIZONTAL_FACING);
                        direction = state.getValue(BlockStateProperties.CHEST_TYPE) == ChestType.LEFT ? direction.getClockWise() : direction.getCounterClockWise();
                        BlockPos neighborPos = pos.relative(direction);
                        BlockState neighborState = world.getBlockState(neighborPos);
                        world.setBlockAndUpdate(neighborPos, neighborState.setValue(BlockStateProperties.CHEST_TYPE, ChestType.SINGLE));
                        state = state.setValue(BlockStateProperties.CHEST_TYPE, ChestType.SINGLE);
                    } else if (state.getBlock() instanceof TrapDoorBlock && !state.getValue(BlockStateProperties.OPEN)) { // special case to toggle trapdoors open first
                        return cycleState(player, world, hand, state, pos, BlockStateProperties.OPEN);
                    } else if (state.getBlock() instanceof BigDripleafBlock && world.getBlockState(pos.below()).getBlock() instanceof BigDripleafStemBlock) { // skip big dripleaf if it is tall
                        return InteractionResult.PASS;
                    } else if (state.getBlock() instanceof BellBlock && (state.getValue(BlockStateProperties.BELL_ATTACHMENT) == BellAttachType.SINGLE_WALL || state.getValue(BlockStateProperties.BELL_ATTACHMENT) == BellAttachType.DOUBLE_WALL)) { // skip bell if wall attached
                        return InteractionResult.PASS;
                    } else if ((state.getBlock() instanceof LeverBlock || state.getBlock() instanceof ButtonBlock) && state.getValue(BlockStateProperties.ATTACH_FACE) == AttachFace.WALL) { // skip wall levers and buttons
                        return InteractionResult.PASS;
                    }
                    return cycleState(player, world, hand, state, pos, BlockStateProperties.HORIZONTAL_FACING);
                } else if (state.getProperties().contains(BlockStateProperties.ORIENTATION)) {
                    return cycleState(player, world, hand, state, pos, BlockStateProperties.ORIENTATION);
                } else if (state.getProperties().contains(BlockStateProperties.RAIL_SHAPE)) {
                    return cycleState(player, world, hand, state, pos, BlockStateProperties.RAIL_SHAPE);
                } else if (state.getProperties().contains(BlockStateProperties.RAIL_SHAPE_STRAIGHT)) {
                    return cycleState(player, world, hand, state, pos, BlockStateProperties.RAIL_SHAPE_STRAIGHT);
                } else if (state.getProperties().contains(BlockStateProperties.AXIS)) {
                    return cycleState(player, world, hand, state, pos, BlockStateProperties.AXIS);
                } else if (state.getProperties().contains(BlockStateProperties.FACING_HOPPER)) {
                    return cycleState(player, world, hand, state, pos, BlockStateProperties.FACING_HOPPER);
                } else if (state.getProperties().contains(BlockStateProperties.ROTATION_16)) {
                    return cycleState(player, world, hand, state, pos, BlockStateProperties.ROTATION_16);
                } else if (state.getProperties().contains(BlockStateProperties.HALF)) {
                    return cycleState(player, world, hand, state, pos, BlockStateProperties.HALF);
                }
            }
        } catch (Exception e) {
            Shroomhearth.LOG.error("Caught error: " + e);
            e.printStackTrace();
        }
        return InteractionResult.PASS;
    }

    public static InteractionResult cycleState(Player player, Level world, InteractionHand hand, BlockState state, BlockPos pos, Property property) {
        BlockState nextState = state.cycle(property);

        // special case for rails
        if (property == BlockStateProperties.RAIL_SHAPE || property == BlockStateProperties.RAIL_SHAPE_STRAIGHT) nextState = fixRailState(world, nextState, pos, property);

        world.setBlock(pos, nextState, Block.UPDATE_CLIENTS);
        world.updateNeighborsAt(pos, state.getBlock(), null);
        player.swing(hand, true);
        world.playSound(null, pos, state.getSoundType().getHitSound(), SoundSource.BLOCKS, 0.8f, 1.1f);
        ShroomhearthUtils.grantAdvancement(player, "shroomhearth_fabric", "orient_block", "impossible");
        return InteractionResult.SUCCESS;
    }

    public static BlockState fixRailState(BlockGetter world, BlockState state, BlockPos pos, Property property) {
        boolean canAscend = false;
        BlockPos adjPos = null;
        if (state.getProperties().contains(BlockStateProperties.RAIL_SHAPE)) {
            while (state.getValue(BlockStateProperties.RAIL_SHAPE).isSlope()) {
                switch(state.getValue(BlockStateProperties.RAIL_SHAPE)) {
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
                canAscend = Block.canSupportRigidBlock(world, adjPos);
                if (canAscend) break;
                else state = state.cycle(property);
            }
        } else if (state.getProperties().contains(BlockStateProperties.RAIL_SHAPE_STRAIGHT)) {
            while (state.getValue(BlockStateProperties.RAIL_SHAPE_STRAIGHT).isSlope()) {
                switch(state.getValue(BlockStateProperties.RAIL_SHAPE_STRAIGHT)) {
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
                canAscend = Block.canSupportRigidBlock(world, adjPos);
                if (canAscend) break;
                else state = state.cycle(property);
            }
        }
        return state;
    }
}
