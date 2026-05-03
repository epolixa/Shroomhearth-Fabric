package com.epolixa.shroomhearth.mixin;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.FarmBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FarmBlock.class)
public class FarmlandMixin {
    @Redirect(method="fallOn",at=@At(value="INVOKE",target="Lnet/minecraft/util/RandomSource;nextFloat()F"))
    private float nextFloat(RandomSource random){
        return random.nextFloat() + 2f;
    }
}
