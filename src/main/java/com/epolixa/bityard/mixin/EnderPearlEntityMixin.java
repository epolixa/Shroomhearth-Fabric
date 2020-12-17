package com.epolixa.bityard.mixin;

import com.epolixa.bityard.BityardUtils;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.util.hit.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnderPearlEntity.class)
public abstract class EnderPearlEntityMixin {

    // Inject to setSneaking to call stopFallFlying
    @Inject(method = "onCollision", at = @At("HEAD"))
    public void onCollision(HitResult hitResult, CallbackInfo info) {
        try {
            BityardUtils.log("enter");



            BityardUtils.log("exit");
        } catch (Exception e) {BityardUtils.logError(e);}
    }
}
