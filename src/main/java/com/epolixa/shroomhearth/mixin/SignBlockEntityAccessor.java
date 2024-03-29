package com.epolixa.shroomhearth.mixin;

import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
import net.minecraft.text.Text;
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

