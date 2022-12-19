package com.epolixa.shroomhearth.event;

import com.epolixa.shroomhearth.Shroomhearth;
import com.epolixa.shroomhearth.ShroomhearthUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class UseCauldronCallback {

    private static final TagKey<Item> WASHABLE             = TagKey.of(RegistryKeys.ITEM, new Identifier(Shroomhearth.MOD_ID, "washable"));
    private static final TagKey<Item> WASHABLE_TERRACOTTA  = TagKey.of(RegistryKeys.ITEM, new Identifier(Shroomhearth.MOD_ID, "washable_terracotta"));
    private static final TagKey<Item> WASHABLE_GLASS       = TagKey.of(RegistryKeys.ITEM, new Identifier(Shroomhearth.MOD_ID, "washable_glass"));
    private static final TagKey<Item> WASHABLE_GLASS_PANES = TagKey.of(RegistryKeys.ITEM, new Identifier(Shroomhearth.MOD_ID, "washable_glass_panes"));
    private static final TagKey<Item> WASHABLE_CANDLES     = TagKey.of(RegistryKeys.ITEM, new Identifier(Shroomhearth.MOD_ID, "washable_candles"));
    private static final TagKey<Item> WASHABLE_DIRT        = TagKey.of(RegistryKeys.ITEM, new Identifier(Shroomhearth.MOD_ID, "washable_dirt"));

    public static ActionResult onUseCauldronCallback(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        try {
            BlockPos pos = hitResult.getBlockPos();
            BlockState state = world.getBlockState(pos);
            if (state.getBlock() == Blocks.WATER_CAULDRON && !player.isSneaking()) {
                int level = state.get(Properties.LEVEL_3);
                if (level > 0) {
                    boolean washed = false;
                    ItemStack itemStack = player.getStackInHand(hand);
                    Item item = itemStack.getItem();
                    if (itemStack.isIn(WASHABLE)) {
                        if (itemStack.isIn(WASHABLE_TERRACOTTA)) {
                            item = Items.TERRACOTTA;
                        } else if (itemStack.isIn(WASHABLE_GLASS)) {
                            item = Items.GLASS;
                        } else if (itemStack.isIn(WASHABLE_GLASS_PANES)) {
                            item = Items.GLASS_PANE;
                        } else if (itemStack.isIn(WASHABLE_CANDLES)) {
                            item = Items.CANDLE;
                        } else if (itemStack.isIn(WASHABLE_DIRT)) {
                            item = Items.MUD;
                        }else {
                            switch (item.getTranslationKey()) {
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
                            world.setBlockState(pos, Blocks.WATER_CAULDRON.getDefaultState().with(Properties.LEVEL_3, level - 1));
                        } else {
                            world.setBlockState(pos, Blocks.CAULDRON.getDefaultState());
                        }
                        world.playSound(null, pos, SoundEvents.ENTITY_VILLAGER_WORK_LEATHERWORKER, SoundCategory.BLOCKS, 1f, 1f);
                        player.setStackInHand(hand, new ItemStack(item, itemStack.getCount()));

                        ShroomhearthUtils.grantAdvancement(player, "shroomhearth_fabric", "wash_block", "impossible");

                        return ActionResult.SUCCESS;
                    }
                }
            }
        } catch (Exception e) {
            Shroomhearth.LOG.error("Caught error: " + e);
            e.printStackTrace();
        }
        return ActionResult.PASS;
    }
}
