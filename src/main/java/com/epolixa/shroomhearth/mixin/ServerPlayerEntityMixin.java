package com.epolixa.shroomhearth.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public abstract class ServerPlayerEntityMixin extends Player {
    @Shadow public ServerGamePacketListenerImpl connection;

    public ServerPlayerEntityMixin(Level world, GameProfile gameProfile) {
        super(world, gameProfile);
    }

    @Inject(method = "doTick", at = @At("HEAD"))
    private void showLight(CallbackInfo ci) {
        try {
            if (this.getMainHandItem().getItem() == Items.GLOWSTONE_DUST) {
                for (int l = 0; l < 667; l++) {
                    int radius = 16;
                    int i = this.blockPosition().getX() + this.random.nextInt(radius) - this.random.nextInt(radius);
                    int j = this.blockPosition().getY() + this.random.nextInt(radius) - this.random.nextInt(radius);
                    int k = this.blockPosition().getZ() + this.random.nextInt(radius) - this.random.nextInt(radius);
                    MutableBlockPos randomPos = new MutableBlockPos(i, j, k);
                    Level world = this.level();
                    BlockState randomState = world.getBlockState(randomPos);
                    if (randomState.getBlock() == Blocks.LIGHT) {
                        ((ServerLevel)world).sendParticles(this.connection.getPlayer(),
                                new DustParticleOptions(16759902, 1.0f),
                                true,
                                false,
                                randomPos.getX() + 0.5,
                                randomPos.getY() + 0.5,
                                randomPos.getZ() + 0.5,
                                (randomState.getLightEmission() * 4) / 15,
                                0.25f, 0.25f, 0.25f, 0.1f);
                    }
                }
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }
}
