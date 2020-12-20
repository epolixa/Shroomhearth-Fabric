package com.epolixa.bityard.mixin;

import com.epolixa.bityard.Bityard;
import com.epolixa.bityard.BityardUtils;
import net.minecraft.block.Blocks;
import net.minecraft.block.DragonEggBlock;
import net.minecraft.block.EndGatewayBlock;
import net.minecraft.block.entity.EndGatewayBlockEntity;
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
                BlockPos dragonEggPos = null;
                DragonEggBlock dragonEggBlock = null;

                // check for dragon egg around hit pos
                for (int x = hitPos.getX() - 1; x <= hitPos.getX() + 1; x++) {
                    for (int y = hitPos.getY() - 1; y <= hitPos.getY() + 1; y++) {
                        for (int z = hitPos.getZ() - 1; z <= hitPos.getZ() + 1; z++) {
                            BlockPos blockPos = new BlockPos(x,y,z);
                            if (this.world.getBlockState(blockPos).getBlock() instanceof DragonEggBlock) {
                                BityardUtils.log("found dragon egg near ender pearl collision");
                                dragonEggBlock = (DragonEggBlock) world.getBlockState(blockPos).getBlock();
                                dragonEggPos = blockPos;
                                this.remove();
                            }
                        }
                    }
                }

                if (dragonEggBlock != null) {
                    BityardUtils.log("setting end gateway at dragon egg position");
                    this.world.setBlockState(dragonEggPos, Blocks.END_GATEWAY.getDefaultState());
                    // play particle

                    BityardUtils.log("updating new end gateway exit coords");
                    EndGatewayBlockEntity endGatewayBlockEntity = (EndGatewayBlockEntity) this.world.getBlockEntity(dragonEggPos);
                    endGatewayBlockEntity.setExitPortalPos(new BlockPos(BityardUtils.getConfig("GATE_EXIT_X", this.getServer()),
                                                                        BityardUtils.getConfig("GATE_EXIT_X", this.getServer()),
                                                                        BityardUtils.getConfig("GATE_EXIT_X", this.getServer())),
                                                true);

                    BityardUtils.log("checking old gateway location");
                    BlockPos oldEndGatewayPos = new BlockPos(BityardUtils.getConfig("RET_GATE_X", this.getServer()),
                                                            BityardUtils.getConfig("RET_GATE_Y", this.getServer()),
                                                            BityardUtils.getConfig("RET_GATE_Z", this.getServer()));
                    if (this.world.getBlockState(oldEndGatewayPos).getBlock() instanceof EndGatewayBlock) {
                        BityardUtils.log("found old end gateway, clearing it before updating coords");
                        this.world.setBlockState(oldEndGatewayPos, Blocks.AIR.getDefaultState());
                        // play particle
                    }

                    BityardUtils.log("updating return gateway coords in scoreboard");
                    BityardUtils.setConfig("RET_GATE_X", dragonEggPos.getX(), this.getServer());
                    BityardUtils.setConfig("RET_GATE_Y", dragonEggPos.getY(), this.getServer());
                    BityardUtils.setConfig("RET_GATE_Z", dragonEggPos.getZ(), this.getServer());

                }

            }

            BityardUtils.log("exit");
        } catch (Exception e) {BityardUtils.logError(e);}
    }
}
