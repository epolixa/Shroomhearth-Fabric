package com.epolixa.shroomhearth.mixin;

import com.epolixa.shroomhearth.Shroomhearth;
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
            if ((Object)this instanceof PlayerEntity) { // check if this is a player
                PlayerEntity player = (PlayerEntity)(Object)this; // cast to a player
                if (player.isGliding() && sneaking) { // if sneaking is engaged while the player is gliding
                    player.stopGliding(); // stop gliding
                }
            }
        } catch (Exception e) {
            Shroomhearth.LOG.error("Caught error: " + e);
            e.printStackTrace();
        }
    }
}
