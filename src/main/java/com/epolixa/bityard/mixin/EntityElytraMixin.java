package com.epolixa.bityard.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityElytraMixin {

    // Inject to setSneaking to call stopFallFlying
    @Inject(method = "setSneaking(Z)V", at = @At("TAIL"))
    public void setSneaking(boolean sneaking, CallbackInfo info) {
        try {
            System.out.println("[EntityElytraMixin][setSneaking] sneaking set");

            if ((Object)this instanceof PlayerEntity) { // check if this is a player
                System.out.println("[EntityElytraMixin][setSneaking] sneaker is player");
                PlayerEntity player = (PlayerEntity)(Object)this; // cast to a player
                if (player.isFallFlying() && sneaking) { // if sneaking is engaged while the player is gliding
                    System.out.println("[EntityElytraMixin][setSneaking] stop fall flying");
                    player.stopFallFlying(); // stop gliding
                }
            }

        } catch (Exception e) {
            System.out.println("[EntityElytraMixin][setSneaking] caught error: " + e.toString());
        }
    }
}
