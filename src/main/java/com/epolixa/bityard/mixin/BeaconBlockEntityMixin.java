package com.epolixa.bityard.mixin;

import net.minecraft.block.entity.BeaconBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BeaconBlockEntity.class)
public abstract class BeaconBlockEntityMixin {

    // Inject to applyPlayerChanges to look for signs and send title to newly affected players
    @Inject(method = "applyPlayerEffects", at = @At("TAIL"))
    public void applyPlayerEffects(CallbackInfo info) {
        try {
            System.out.println("[BeaconBlockEntityMixin][applyPlayerEffects] enter");



            System.out.println("[BeaconBlockEntityMixin][applyPlayerEffects] exit");
        } catch (Exception e) {
            System.out.println("[BeaconBlockEntityMixin][applyPlayerEffects] caught error: " + e.toString());
        }
    }
}
