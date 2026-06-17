package com.epolixa.shroomhearth.event;

import com.epolixa.shroomhearth.Shroomhearth;
import com.epolixa.shroomhearth.ShroomhearthUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

public class AttackEntityOrientationToolCallback {

    private static final TagKey<Item> ORIENTING_TOOLS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Shroomhearth.MOD_ID, "block_orienting_tools"));

    public static InteractionResult onAttackEntityOrientationToolCallback(Player player, Level world, InteractionHand hand, Entity entity, EntityHitResult hitResult) {
        try {
            if (entity instanceof Boat) {
                Boat boat = (Boat) entity;
                ItemStack handItemStack = player.getItemInHand(hand);
                if (handItemStack.is(ORIENTING_TOOLS)) {
                    switch(player.getDirection()) {
                        case NORTH:
                            boat.setYRot(180);
                            break;
                        case SOUTH:
                            boat.setYRot(0);
                            break;
                        case EAST:
                            boat.setYRot(-90);
                            break;
                        case WEST:
                            boat.setYRot(90);
                            break;
                    }
                    ShroomhearthUtils.grantAdvancement(player, Shroomhearth.MOD_ID, "tilt_controls", "tilt_controls");
                }
            }

        } catch (Exception e) {
            Shroomhearth.LOG.error("Caught error: " + e);
            e.printStackTrace();
        }
        return InteractionResult.PASS;
    }

}
