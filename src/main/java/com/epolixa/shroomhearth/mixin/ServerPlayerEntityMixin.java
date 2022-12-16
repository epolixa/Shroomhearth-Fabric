package com.epolixa.shroomhearth.mixin;

import com.epolixa.shroomhearth.Shroomhearth;
import com.epolixa.shroomhearth.ShroomhearthUtils;
import com.mojang.authlib.GameProfile;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.LightBlock;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.ParticleS2CPacket;
import net.minecraft.particle.BlockStateParticleEffect;
import net.minecraft.particle.DustParticleEffect;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.util.math.BlockPos.Mutable;

import java.util.List;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {
    @Shadow public ServerPlayNetworkHandler networkHandler;

    @Shadow protected abstract void worldChanged(ServerWorld origin);

    @Unique
    private long showLightTimer = 0;

    public ServerPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    @Inject(method = "playerTick", at = @At("HEAD"))
    private void showLight(CallbackInfo ci) {
        try {
            //showLightTimer++;
            if (/*showLightTimer > 10 && */this.getMainHandStack().getItem() == Items.GLOWSTONE_DUST) {
                //showLightTimer = 0;
                //List<ArmorStandEntity> armorStands = this.world.getEntitiesByClass(ArmorStandEntity.class, new Box(this.getBlockPos().add(10, 10, 10), this.getBlockPos().add(-10, -10, -10)), entity -> true);
                //List<LightBlock> lightBlocks = this.world.getBlockState(new Box(this.getBlockPos().add(10, 10, 10), this.getBlockPos().add(-10, -10, -10)));

                /*ParticleEffect particleEffect = new DustParticleEffect(new Vector3f(0.8f, 0.2f, 0.2f), 1f);

                for (ArmorStandEntity armorStand : armorStands) {
                    this.networkHandler.sendPacket(new ParticleS2CPacket(particleEffect,
                            false,
                            armorStand.getX(),
                            armorStand.getY() + armorStand.getHeight() / 2,
                            armorStand.getZ(),
                            0.2f, 0.2f, 0.2f, 0.1f, 3));
                }*/

                //this.world.addParticle(new BlockStateParticleEffect(ParticleTypes.BLOCK_MARKER, randomState), (double)i + 0.5D, (double)j + 0.5D, (double)k + 0.5D, 0.0D, 0.0D, 0.0D);

                for (int l = 0; l < 667; l++) {
                    int radius = 16;
                    int i = this.getBlockPos().getX() + this.random.nextInt(radius) - this.random.nextInt(radius);
                    int j = this.getBlockPos().getY() + this.random.nextInt(radius) - this.random.nextInt(radius);
                    int k = this.getBlockPos().getZ() + this.random.nextInt(radius) - this.random.nextInt(radius);
                    Mutable randomPos = new Mutable(i, j, k);
                    BlockState randomState = this.world.getBlockState(randomPos);

                    //System.out.println("randomState block: " + randomState.getBlock().toString() + " at " + randomPos.toString());
                    //Shroomhearth.LOG.info("randomState block: " + randomState.getBlock().toString() + " at " + randomPos);

                    if (randomState.getBlock() == Blocks.LIGHT) {
                        ((ServerWorld)this.world).spawnParticles(this.networkHandler.getPlayer(),
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
