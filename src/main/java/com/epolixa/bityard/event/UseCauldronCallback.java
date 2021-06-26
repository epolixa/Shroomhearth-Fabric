package com.epolixa.bityard.event;

import com.epolixa.bityard.Bityard;
import com.epolixa.bityard.BityardUtils;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
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
                    if (TagRegistry.item(new Identifier(Bityard.MOD_ID, "washable")).contains(item)) {
                        if (TagRegistry.item(new Identifier(Bityard.MOD_ID, "washable_terracotta")).contains(item)) {
                            item = Items.TERRACOTTA;
                        } else if (TagRegistry.item(new Identifier(Bityard.MOD_ID, "washable_glass")).contains(item)) {
                            item = Items.GLASS;
                        } else if (TagRegistry.item(new Identifier(Bityard.MOD_ID, "washable_glass_panes")).contains(item)) {
                            item = Items.GLASS_PANE;
                        } else if (TagRegistry.item(new Identifier(Bityard.MOD_ID, "washable_candles")).contains(item)) {
                            item = Items.CANDLE;
                        } else {
                            switch (item.getTranslationKey()) {
                                case "block.minecraft.white_concrete_powder":
                                    item = Items.WHITE_CONCRETE;
                                    break;
                                case "block.minecraft.orange_concrete_powder":
                                    item = Items.ORANGE_CONCRETE;
                                    break;
                                case "block.minecraft.magenta_concrete_powder":
                                    item = Items.MAGENTA_CONCRETE;
                                    break;
                                case "block.minecraft.light_blue_concrete_powder":
                                    item = Items.LIGHT_BLUE_CONCRETE;
                                    break;
                                case "block.minecraft.yellow_concrete_powder":
                                    item = Items.YELLOW_CONCRETE;
                                    break;
                                case "block.minecraft.lime_concrete_powder":
                                    item = Items.LIME_CONCRETE;
                                    break;
                                case "block.minecraft.pink_concrete_powder":
                                    item = Items.PINK_CONCRETE;
                                    break;
                                case "block.minecraft.gray_concrete_powder":
                                    item = Items.GRAY_CONCRETE;
                                    break;
                                case "block.minecraft.light_gray_concrete_powder":
                                    item = Items.LIGHT_GRAY_CONCRETE;
                                    break;
                                case "block.minecraft.cyan_concrete_powder":
                                    item = Items.CYAN_CONCRETE;
                                    break;
                                case "block.minecraft.purple_concrete_powder":
                                    item = Items.PURPLE_CONCRETE;
                                    break;
                                case "block.minecraft.blue_concrete_powder":
                                    item = Items.BLUE_CONCRETE;
                                    break;
                                case "block.minecraft.brown_concrete_powder":
                                    item = Items.BROWN_CONCRETE;
                                    break;
                                case "block.minecraft.green_concrete_powder":
                                    item = Items.GREEN_CONCRETE;
                                    break;
                                case "block.minecraft.red_concrete_powder":
                                    item = Items.RED_CONCRETE;
                                    break;
                                case "block.minecraft.black_concrete_powder":
                                    item = Items.BLACK_CONCRETE;
                                    break;
                                default:
                                    break;
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

                        BityardUtils.grantAdvancement(player, "bityard", "wash_block", "impossible");

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
}
