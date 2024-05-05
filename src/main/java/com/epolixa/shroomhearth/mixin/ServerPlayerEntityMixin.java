package com.epolixa.shroomhearth.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.util.math.BlockPos.Mutable;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {
    @Shadow public ServerPlayNetworkHandler networkHandler;

    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Inject(method = "playerTick", at = @At("HEAD"))
    private void showLight(CallbackInfo ci) {
        try {
            if (this.getMainHandStack().getItem() == Items.GLOWSTONE_DUST) {
                for (int l = 0; l < 667; l++) {
                    int radius = 16;
                    int i = this.getBlockPos().getX() + this.random.nextInt(radius) - this.random.nextInt(radius);
                    int j = this.getBlockPos().getY() + this.random.nextInt(radius) - this.random.nextInt(radius);
                    int k = this.getBlockPos().getZ() + this.random.nextInt(radius) - this.random.nextInt(radius);
                    Mutable randomPos = new Mutable(i, j, k);
                    World world = this.getWorld();
                    BlockState randomState = world.getBlockState(randomPos);
                    if (randomState.getBlock() == Blocks.LIGHT) {
                        ((ServerWorld)world).spawnParticles(this.networkHandler.getPlayer(),
                                new DustParticleEffect(new Vector3f(1.0f, 0.9f, 0.1f), 1.0f),
                                true,
                                randomPos.getX() + 0.5,
                                randomPos.getY() + 0.5,
                                randomPos.getZ() + 0.5,
                                (randomState.getLuminance() * 4) / 15,
                                0.25f, 0.25f, 0.25f, 0.1f);
                    }
                }
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }
}
