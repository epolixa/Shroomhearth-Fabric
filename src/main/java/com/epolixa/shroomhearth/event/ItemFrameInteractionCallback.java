package com.epolixa.shroomhearth.event;

import com.epolixa.shroomhearth.Shroomhearth;
import com.epolixa.shroomhearth.ShroomhearthUtils;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

public class ItemFrameInteractionCallback {

    public static InteractionResult onUseItemFrameCallback(Player player, Level world, InteractionHand hand, Entity entity, EntityHitResult hitResult) {
        try {
            if (entity instanceof ItemFrame) {
                ItemFrame itemFrame = (ItemFrame) entity;
                ItemStack handItemStack = player.getItemInHand(hand);
                if (handItemStack.is(Items.SHEARS) && !itemFrame.getItem().is(Items.AIR) && !itemFrame.isInvisible()) {
                    itemFrame.setInvisible(true);
                    player.swing(hand, true);
                    world.playSound(null, itemFrame.blockPosition(), SoundEvents.BEEHIVE_SHEAR, SoundSource.BLOCKS, 1f, 1.2f);
                    if (!player.isCreative()) handItemStack.hurtAndBreak(1, player, ShroomhearthUtils.getEquipmentSlotFromHand(hand));
                    ShroomhearthUtils.grantAdvancement(player, "shroomhearth_fabric", "frameless", "impossible");
                    return InteractionResult.SUCCESS;
                }
            }
        } catch (Exception e) {
            Shroomhearth.LOG.error("Caught error: " + e);
            e.printStackTrace();
        }
        return InteractionResult.PASS;
    }


    public static InteractionResult onAttackItemFrameCallback(Player player, Level world, InteractionHand hand, Entity entity, EntityHitResult hitResult) {
        try {
            if (entity instanceof ItemFrame) {
                ItemFrame itemFrame = (ItemFrame) entity;
                if (itemFrame.isInvisible()) itemFrame.setInvisible(false);
            }
        } catch (Exception e) {
            Shroomhearth.LOG.error("Caught error: " + e);
            e.printStackTrace();
        }
        return InteractionResult.PASS;
    }
}
