package com.epolixa.shroomhearth.event;

import com.epolixa.shroomhearth.Shroomhearth;
import com.epolixa.shroomhearth.ShroomhearthUtils;
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
                ItemFrameEntity itemFrame = (ItemFrameEntity) entity;
                ItemStack handItemStack = player.getStackInHand(hand);
                if (handItemStack.isOf(Items.SHEARS) && !itemFrame.getHeldItemStack().isOf(Items.AIR) && !itemFrame.isInvisible()) {
                    itemFrame.setInvisible(true);
                    player.swingHand(hand, true);
                    world.playSound(null, itemFrame.getBlockPos(), SoundEvents.BLOCK_BEEHIVE_SHEAR, SoundCategory.BLOCKS, 1f, 1.2f);
                    if (!player.isCreative()) handItemStack.damage(1, player, p -> p.sendToolBreakStatus(hand));
                    ShroomhearthUtils.grantAdvancement(player, "shroomhearth_fabric", "frameless", "impossible");
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
                ItemFrameEntity itemFrame = (ItemFrameEntity) entity;
                if (itemFrame.isInvisible()) itemFrame.setInvisible(false);
            }
        } catch (Exception e) {
            Shroomhearth.LOG.error("Caught error: " + e);
            e.printStackTrace();
        }
        return ActionResult.PASS;
    }
}
