package com.epolixa.shroomhearth.mixin;

import com.epolixa.shroomhearth.Shroomhearth;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityElytraMixin {

    @Shadow public abstract String getEntityName();
    @Shadow public abstract void playSound(SoundEvent event, SoundCategory category, float volume, float pitch);

    // Inject to startFallFlying to add a dragon flap sound effect
    @Inject(method = "startFallFlying", at = @At("TAIL"))
    public void startFallFlying(CallbackInfo info) {
        try {
            // Play a dragon flap sound at the player's location
            this.playSound(SoundEvents.ENTITY_ENDER_DRAGON_FLAP, SoundCategory.PLAYERS, 0.4f, 2f);
        } catch (Exception e) {
            Shroomhearth.LOG.error("Caught error: " + e);
            e.printStackTrace();
        }
    }

    // Inject to stopFallFlying to add a dragon flap sound effect
    @Inject(method = "stopFallFlying", at = @At("TAIL"))
    public void stopFallFlying(CallbackInfo info) {
        try {
            // Play a dragon flap sound at the player's location
            this.playSound(SoundEvents.ENTITY_ENDER_DRAGON_FLAP, SoundCategory.PLAYERS, 0.4f, 1.5f);
        } catch (Exception e) {
            Shroomhearth.LOG.error("Caught error: " + e);
            e.printStackTrace();
        }
    }

}
