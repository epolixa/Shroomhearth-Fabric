package com.epolixa.bityard.mixin;

import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.entity.effect.StatusEffect;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BeaconBlockEntity.class)
public interface BeaconBlockEntityAccessor {

    @Accessor("primary")
    public StatusEffect getPrimary();

    @Accessor("level")
    public int getLevel();

}
