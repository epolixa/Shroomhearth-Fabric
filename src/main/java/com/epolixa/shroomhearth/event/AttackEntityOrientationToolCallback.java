package com.epolixa.shroomhearth.event;

import com.epolixa.shroomhearth.Shroomhearth;
import com.epolixa.shroomhearth.ShroomhearthUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.world.World;

public class AttackEntityOrientationToolCallback {

    private static final TagKey<Item> ORIENTING_TOOLS = TagKey.of(RegistryKeys.ITEM, new Identifier(Shroomhearth.MOD_ID, "block_orienting_tools"));

    public static ActionResult onAttackEntityOrientationToolCallback(PlayerEntity player, World world, Hand hand, Entity entity, EntityHitResult hitResult) {
        try {
            if (entity instanceof BoatEntity) {
                BoatEntity boat = (BoatEntity) entity;
                ItemStack handItemStack = player.getStackInHand(hand);
                if (handItemStack.isIn(ORIENTING_TOOLS)) {
                    switch(player.getHorizontalFacing()) {
                        case NORTH:
                            boat.setYaw(180);
                            break;
                        case SOUTH:
                            boat.setYaw(0);
                            break;
                        case EAST:
                            boat.setYaw(-90);
                            break;
                        case WEST:
                            boat.setYaw(90);
                            break;
                    }
                    ShroomhearthUtils.grantAdvancement(player, "shroomhearth_fabric", "orient_block", "impossible");
                }
            }

        } catch (Exception e) {
            Shroomhearth.LOG.error("Caught error: " + e);
            e.printStackTrace();
        }
        return ActionResult.PASS;
    }

}
