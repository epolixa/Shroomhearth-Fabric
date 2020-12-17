package com.epolixa.bityard.mixin;

import com.epolixa.bityard.BityardUtils;
import net.minecraft.block.DragonEggBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EnderPearlEntity.class)
public abstract class EnderPearlEntityMixin extends ThrownItemEntity {

    public EnderPearlEntityMixin(EntityType<? extends EnderPearlEntity> entityType, World world) {
        super(entityType, world);
    }

    // Inject to setSneaking to call stopFallFlying
    @Inject(method = "onCollision", at = @At("HEAD"))
    public void onCollision(HitResult hitResult, CallbackInfo info) {
        try {
            BityardUtils.log("enter");

            if (!this.world.isClient &&
                !this.removed &&
                ((ServerWorld) this.world).getRegistryManager().getDimensionTypes().getId(this.world.getDimension()).equals(DimensionType.OVERWORLD_ID)) { // yikes

                BlockPos hitPos = new BlockPos(hitResult.getPos());
                // check for dragon egg around hit pos
                for (int x = hitPos.getX() - 1; x <= hitPos.getX() + 1; x++) {
                    for (int y = hitPos.getY() - 1; y <= hitPos.getY() + 1; y++) {
                        for (int z = hitPos.getZ() - 1; z <= hitPos.getZ() + 1; z++) {
                            BlockPos blockPos = new BlockPos(x,y,z);
                            if (world.getBlockState(blockPos).getBlock() instanceof DragonEggBlock) {
                                BityardUtils.log("found dragon egg near ender pearl collision");
                                this.remove();
                            }
                        }
                    }
                }

            }

            BityardUtils.log("exit");
        } catch (Exception e) {BityardUtils.logError(e);}
    }
}
