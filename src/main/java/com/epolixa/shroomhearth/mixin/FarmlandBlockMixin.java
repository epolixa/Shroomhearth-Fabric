package com.epolixa.shroomhearth.mixin;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.FarmlandBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(FarmlandBlock.class)
public class FarmlandBlockMixin {
    @Redirect(method="fallOn",at=@At(value="INVOKE",target="Lnet/minecraft/util/RandomSource;nextFloat()F"))
    private float nextFloat(RandomSource random){
        return random.nextFloat() + 2f;
    }
}
