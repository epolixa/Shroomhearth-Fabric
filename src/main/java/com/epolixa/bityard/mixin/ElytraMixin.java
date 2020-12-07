package com.epolixa.bityard.mixin;

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
public abstract class ElytraMixin {

    @Shadow public abstract String getEntityName();

    @Shadow public abstract void playSound(SoundEvent event, SoundCategory category, float volume, float pitch);

    // Mixin to startFallFlying to add a dragon flap sound effect
    @Inject(method = "startFallFlying", at = @At("TAIL"))
    public void startFallFlying(CallbackInfo info) {
        try {
            System.out.println("[ElytraMixin][startFallFlying] " + this.getEntityName() + " started elytra");

            // Play a dragon flap sound at the player's location
            this.playSound(SoundEvents.ENTITY_ENDER_DRAGON_FLAP, SoundCategory.PLAYERS, 0.4f, 2f);

        } catch (Exception e) {
            System.out.println("[ElytraMixin][startFallFlying] caught error: " + e.toString());
        }
    }

}
