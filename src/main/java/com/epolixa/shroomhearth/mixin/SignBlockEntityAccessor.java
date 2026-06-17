package com.epolixa.shroomhearth.mixin;

import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SignBlockEntity.class)
public interface SignBlockEntityAccessor {

    @Accessor("frontText")
    public SignText getFrontText();

    /*@Accessor("texts")
    public Text[] getTexts();

    @Accessor("glowingText")
    public boolean isGlowingText();*/

}

