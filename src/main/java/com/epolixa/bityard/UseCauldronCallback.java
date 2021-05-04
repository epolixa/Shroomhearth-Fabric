package com.epolixa.bityard;

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
        BlockPos pos = hitResult.getBlockPos();
        BlockState state = world.getBlockState(pos);
        if (state.getBlock() == Blocks.CAULDRON && !player.isSneaking()) {
            int level = state.get(Properties.LEVEL_3);
            if (level > 0) {
                boolean washed = false;
                ItemStack itemStack = player.getStackInHand(hand);
                Item item = itemStack.getItem();
                if (item.isIn(TagRegistry.item(new Identifier(Bityard.MOD_ID, "washable")))) {
                    switch(item.getTranslationKey()) {
                        case "block.minecraft.white_concrete_powder": item = Items.WHITE_CONCRETE; break;
                        case "block.minecraft.orange_concrete_powder": item = Items.ORANGE_CONCRETE; break;
                        case "block.minecraft.magenta_concrete_powder": item = Items.MAGENTA_CONCRETE; break;
                        case "block.minecraft.light_blue_concrete_powder": item = Items.LIGHT_BLUE_CONCRETE; break;
                        case "block.minecraft.yellow_concrete_powder": item = Items.YELLOW_CONCRETE; break;
                        case "block.minecraft.lime_concrete_powder": item = Items.LIME_CONCRETE; break;
                        case "block.minecraft.pink_concrete_powder": item = Items.PINK_CONCRETE; break;
                        case "block.minecraft.gray_concrete_powder": item = Items.GRAY_CONCRETE; break;
                        case "block.minecraft.light_gray_concrete_powder": item = Items.LIGHT_GRAY_CONCRETE; break;
                        case "block.minecraft.cyan_concrete_powder": item = Items.CYAN_CONCRETE; break;
                        case "block.minecraft.purple_concrete_powder": item = Items.PURPLE_CONCRETE; break;
                        case "block.minecraft.blue_concrete_powder": item = Items.BLUE_CONCRETE; break;
                        case "block.minecraft.brown_concrete_powder": item = Items.BROWN_CONCRETE; break;
                        case "block.minecraft.green_concrete_powder": item = Items.GREEN_CONCRETE; break;
                        case "block.minecraft.red_concrete_powder": item = Items.RED_CONCRETE; break;
                        case "block.minecraft.black_concrete_powder": item = Items.BLACK_CONCRETE; break;
                        case "block.minecraft.white_terracotta":
                        case "block.minecraft.orange_terracotta":
                        case "block.minecraft.magenta_terracotta":
                        case "block.minecraft.light_blue_terracotta":
                        case "block.minecraft.yellow_terracotta":
                        case "block.minecraft.lime_terracotta":
                        case "block.minecraft.pink_terracotta":
                        case "block.minecraft.gray_terracotta":
                        case "block.minecraft.light_gray_terracotta":
                        case "block.minecraft.cyan_terracotta":
                        case "block.minecraft.purple_terracotta":
                        case "block.minecraft.blue_terracotta":
                        case "block.minecraft.brown_terracotta":
                        case "block.minecraft.green_terracotta":
                        case "block.minecraft.red_terracotta":
                        case "block.minecraft.black_terracotta": item = Items.TERRACOTTA; break;
                        case "block.minecraft.white_stained_glass":
                        case "block.minecraft.orange_stained_glass":
                        case "block.minecraft.magenta_stained_glass":
                        case "block.minecraft.light_blue_stained_glass":
                        case "block.minecraft.yellow_stained_glass":
                        case "block.minecraft.lime_stained_glass":
                        case "block.minecraft.pink_stained_glass":
                        case "block.minecraft.gray_stained_glass":
                        case "block.minecraft.light_gray_stained_glass":
                        case "block.minecraft.cyan_stained_glass":
                        case "block.minecraft.purple_stained_glass":
                        case "block.minecraft.blue_stained_glass":
                        case "block.minecraft.brown_stained_glass":
                        case "block.minecraft.green_stained_glass":
                        case "block.minecraft.red_stained_glass":
                        case "block.minecraft.black_stained_glass": item = Items.GLASS; break;
                        case "block.minecraft.white_stained_glass_pane":
                        case "block.minecraft.orange_stained_glass_pane":
                        case "block.minecraft.magenta_stained_glass_pane":
                        case "block.minecraft.light_blue_stained_glass_pane":
                        case "block.minecraft.yellow_stained_glass_pane":
                        case "block.minecraft.lime_stained_glass_pane":
                        case "block.minecraft.pink_stained_glass_pane":
                        case "block.minecraft.gray_stained_glass_pane":
                        case "block.minecraft.light_gray_stained_glass_pane":
                        case "block.minecraft.cyan_stained_glass_pane":
                        case "block.minecraft.purple_stained_glass_pane":
                        case "block.minecraft.blue_stained_glass_pane":
                        case "block.minecraft.brown_stained_glass_pane":
                        case "block.minecraft.green_stained_glass_pane":
                        case "block.minecraft.red_stained_glass_pane":
                        case "block.minecraft.black_stained_glass_pane": item = Items.GLASS_PANE; break;
                        default: break;
                    }
                    washed = true;
                }
                if (washed) {
                    world.setBlockState(pos, Blocks.CAULDRON.getDefaultState().with(Properties.LEVEL_3, level - 1));
                    world.playSound(null, pos, SoundEvents.ENTITY_VILLAGER_WORK_LEATHERWORKER, SoundCategory.BLOCKS, 1f, 1f);
                    player.setStackInHand(hand, new ItemStack(item, itemStack.getCount()));
                    System.out.println("success");
                    return ActionResult.SUCCESS;
                }
            }
        }
        return ActionResult.PASS;
    }
}
