package com.epolixa.shroomhearth.event;

import com.epolixa.shroomhearth.Shroomhearth;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;

public class ItemFrameInteractionCallback {

    public static ActionResult onUseItemFrameCallback(PlayerEntity player, World world, Hand hand, Entity entity, EntityHitResult hitResult) {
        try {
            if (entity instanceof ItemFrameEntity) {
                System.out.println("Interacted with item frame");

                ItemFrameEntity itemFrame = (ItemFrameEntity) entity;

                ItemStack handItemStack = player.getStackInHand(hand);
                System.out.println("item frame held item stack: " + itemFrame.getHeldItemStack().toString());

                if (handItemStack.isOf(Items.SHEARS) && !itemFrame.getHeldItemStack().isOf(Items.AIR) && !itemFrame.isInvisible()) {
                    System.out.println("Interacted with item frame using Shears");

                    itemFrame.setInvisible(true);
                    System.out.println("Set item frame invisible");

                    world.playSound(null, itemFrame.getBlockPos(), SoundEvents.BLOCK_BEEHIVE_SHEAR, SoundCategory.BLOCKS, 1f, 1.2f);

                    if (!player.isCreative()) {
                        System.out.println("Player is not Creative");

                        handItemStack.damage(1, player, p -> p.sendToolBreakStatus(hand));
                        System.out.println("Damaged shears");
                    }

                    return ActionResult.SUCCESS;
                }
            }
        } catch (Exception e) {
            Shroomhearth.LOG.error("Caught error: " + e);
            e.printStackTrace();
        }
        return ActionResult.PASS;
    }


    public static ActionResult onAttackItemFrameCallback(PlayerEntity player, World world, Hand hand, Entity entity, EntityHitResult hitResult) {
        try {
            if (entity instanceof ItemFrameEntity) {
                System.out.println("Attacked item frame");

                ItemFrameEntity itemFrame = (ItemFrameEntity) entity;

                if (itemFrame.isInvisible()) {
                    itemFrame.setInvisible(false);
                    System.out.println("Set item frame visible");
                }
            }
        } catch (Exception e) {
            Shroomhearth.LOG.error("Caught error: " + e);
            e.printStackTrace();
        }
        return ActionResult.PASS;
    }
}
