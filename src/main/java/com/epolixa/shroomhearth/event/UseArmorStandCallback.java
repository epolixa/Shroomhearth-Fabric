package com.epolixa.shroomhearth.event;

import com.epolixa.shroomhearth.Shroomhearth;
import com.epolixa.shroomhearth.mixin.ArmorStandEntityAccessor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;

public class UseArmorStandCallback {

    public static ActionResult onUseArmorStandCallback(PlayerEntity player, World world, Hand hand, Entity entity, EntityHitResult hitResult) {
        try {
            if (entity instanceof ArmorStandEntity) {
                ArmorStandEntity armorStand = (ArmorStandEntity) entity;
                ItemStack handItemStack = player.getStackInHand(hand);
                if (handItemStack.isOf(Items.STICK) && !armorStand.shouldShowArms()) {
                    ArmorStandEntityAccessor asea = (ArmorStandEntityAccessor) armorStand;
                    asea.callSetShowArms(true);
                    world.playSound(null, armorStand.getBlockPos(), SoundEvents.ENTITY_ARMOR_STAND_HIT, SoundCategory.NEUTRAL, 1f, 1.2f);
                    if (!player.isCreative()) {
                        armorStand.damage(DamageSource.player(player), 0.1f);
                        handItemStack.decrement(1);
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
}
