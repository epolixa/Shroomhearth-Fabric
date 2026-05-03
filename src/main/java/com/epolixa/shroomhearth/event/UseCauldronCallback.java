package com.epolixa.shroomhearth.event;

import com.epolixa.shroomhearth.Shroomhearth;
import com.epolixa.shroomhearth.ShroomhearthUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
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

public class UseCauldronCallback {

    private static final TagKey<Item> WASHABLE             = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Shroomhearth.MOD_ID, "washable"));
    private static final TagKey<Item> WASHABLE_TERRACOTTA  = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Shroomhearth.MOD_ID, "washable_terracotta"));
    private static final TagKey<Item> WASHABLE_GLASS       = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Shroomhearth.MOD_ID, "washable_glass"));
    private static final TagKey<Item> WASHABLE_GLASS_PANES = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Shroomhearth.MOD_ID, "washable_glass_panes"));
    private static final TagKey<Item> WASHABLE_CANDLES     = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Shroomhearth.MOD_ID, "washable_candles"));
    private static final TagKey<Item> WASHABLE_DIRT        = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Shroomhearth.MOD_ID, "washable_dirt"));
    private static final TagKey<Item> WASHABLE_BUNDLES     = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Shroomhearth.MOD_ID, "washable_bundles"));

    public static InteractionResult onUseCauldronCallback(Player player, Level world, InteractionHand hand, BlockHitResult hitResult) {
        try {
            BlockPos pos = hitResult.getBlockPos();
            BlockState state = world.getBlockState(pos);
            if (state.getBlock() == Blocks.WATER_CAULDRON && !player.isShiftKeyDown()) {
                int level = state.getValue(BlockStateProperties.LEVEL_CAULDRON);
                if (level > 0) {
                    boolean washed = false;
                    ItemStack itemStack = player.getItemInHand(hand);
                    Item item = itemStack.getItem();
                    if (itemStack.is(WASHABLE)) {
                        if (itemStack.is(WASHABLE_TERRACOTTA)) {
                            item = Items.TERRACOTTA;
                        } else if (itemStack.is(WASHABLE_GLASS)) {
                            item = Items.GLASS;
                        } else if (itemStack.is(WASHABLE_GLASS_PANES)) {
                            item = Items.GLASS_PANE;
                        } else if (itemStack.is(WASHABLE_CANDLES)) {
                            item = Items.CANDLE;
                        } else if (itemStack.is(WASHABLE_DIRT)) {
                            item = Items.MUD;
                        } else if (itemStack.is(WASHABLE_BUNDLES)) {
                            item = Items.BUNDLE;
                        } else {
                            switch (item.getDescriptionId()) {
                                case "block.minecraft.white_concrete_powder" -> item = Items.WHITE_CONCRETE;
                                case "block.minecraft.orange_concrete_powder" -> item = Items.ORANGE_CONCRETE;
                                case "block.minecraft.magenta_concrete_powder" -> item = Items.MAGENTA_CONCRETE;
                                case "block.minecraft.light_blue_concrete_powder" -> item = Items.LIGHT_BLUE_CONCRETE;
                                case "block.minecraft.yellow_concrete_powder" -> item = Items.YELLOW_CONCRETE;
                                case "block.minecraft.lime_concrete_powder" -> item = Items.LIME_CONCRETE;
                                case "block.minecraft.pink_concrete_powder" -> item = Items.PINK_CONCRETE;
                                case "block.minecraft.gray_concrete_powder" -> item = Items.GRAY_CONCRETE;
                                case "block.minecraft.light_gray_concrete_powder" -> item = Items.LIGHT_GRAY_CONCRETE;
                                case "block.minecraft.cyan_concrete_powder" -> item = Items.CYAN_CONCRETE;
                                case "block.minecraft.purple_concrete_powder" -> item = Items.PURPLE_CONCRETE;
                                case "block.minecraft.blue_concrete_powder" -> item = Items.BLUE_CONCRETE;
                                case "block.minecraft.brown_concrete_powder" -> item = Items.BROWN_CONCRETE;
                                case "block.minecraft.green_concrete_powder" -> item = Items.GREEN_CONCRETE;
                                case "block.minecraft.red_concrete_powder" -> item = Items.RED_CONCRETE;
                                case "block.minecraft.black_concrete_powder" -> item = Items.BLACK_CONCRETE;
                                default -> {}
                            }
                        }

                        washed = true;
                    }

                    if (washed) {
                        if (level > 1) {
                            world.setBlockAndUpdate(pos, Blocks.WATER_CAULDRON.defaultBlockState().setValue(BlockStateProperties.LEVEL_CAULDRON, level - 1));
                        } else {
                            world.setBlockAndUpdate(pos, Blocks.CAULDRON.defaultBlockState());
                        }

                        world.playSound(null, pos, SoundEvents.VILLAGER_WORK_LEATHERWORKER, SoundSource.BLOCKS, 1f, 1f);
                        ((ServerLevel)world).sendParticles(ParticleTypes.SPLASH,
                            pos.getX() + 0.5,
                            pos.getY() + 1.25,
                            pos.getZ() + 0.5,
                            8,
                            0.25f, 0.25, 0.25f, 0.05f
                        );

                        // Disabled functionality to wash entire stack at once
                        //player.setItemInHand(hand, new ItemStack(item, itemStack.getCount()));

                        // Instead, wash items one at a time
                        itemStack.shrink(1);
                        ItemStack washedStack = new ItemStack(item, 1);
                        if (itemStack.isEmpty()) {
                            player.setItemInHand(hand, washedStack);
                        } else if (!player.getInventory().add(washedStack)) {
                            player.drop(washedStack, false);
                        }

                        ShroomhearthUtils.grantAdvancement(player, Shroomhearth.MOD_ID, "all_washed_up", "all_washed_up");

                        return InteractionResult.SUCCESS;
                    }
                }
            }
        } catch (Exception e) {
            Shroomhearth.LOG.error("Caught error: " + e);
            e.printStackTrace();
        }
        return InteractionResult.PASS;
    }
}
