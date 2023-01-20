package com.epolixa.shroomhearth.mixin;

import net.minecraft.block.FarmlandBlock;
import net.minecraft.util.math.random.Random;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FarmlandBlock.class)
public class FarmlandMixin {
    @Redirect(method="onLandedUpon",at=@At(value="INVOKE",target="Lnet/minecraft/util/math/random/Random;nextFloat()F"))
    private float nextFloat(Random random){
        return random.nextFloat() + 2f;
    }
}
