package com.epolixa.shroomhearth.mixin;

import com.epolixa.shroomhearth.Shroomhearth;
import com.epolixa.shroomhearth.ShroomhearthUtils;
import net.minecraft.block.Blocks;
import net.minecraft.block.DragonEggBlock;
import net.minecraft.block.EndGatewayBlock;
import net.minecraft.block.entity.EndGatewayBlockEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Style;
import net.minecraft.text.TextColor;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
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
            if (!this.world.isClient && !this.isRemoved() && this.world.getDimension().natural()) {

                BlockPos hitPos = new BlockPos(hitResult.getPos());
                BlockPos dragonEggPos = null;
                DragonEggBlock dragonEggBlock = null;

                // check for dragon egg around hit pos
                for (int x = hitPos.getX() - 1; x <= hitPos.getX() + 1; x++) {
                    for (int y = hitPos.getY() - 1; y <= hitPos.getY() + 1; y++) {
                        for (int z = hitPos.getZ() - 1; z <= hitPos.getZ() + 1; z++) {
                            BlockPos blockPos = new BlockPos(x,y,z);
                            if (this.world.getBlockState(blockPos).getBlock() instanceof DragonEggBlock) {
                                dragonEggBlock = (DragonEggBlock) world.getBlockState(blockPos).getBlock();
                                dragonEggPos = blockPos;
                            }
                        }
                    }
                }

                if (dragonEggBlock != null) {
                    MinecraftServer s = this.getServer();
                    PlayerEntity p = (PlayerEntity) this.getOwner();

                    this.world.setBlockState(dragonEggPos, Blocks.END_GATEWAY.getDefaultState());
                    // play particle
                    /*ParticleS2CPacket particlePacket = new ParticleS2CPacket(
                                                        ParticleTypes.EXPLOSION, true,
                                                        this.prevX, this.prevY, this.prevZ,
                                                        0.0F, 0.0F, 0.0F, 1.0F, 1);
                    ServerSidePacketRegistry.INSTANCE.sendToPlayer((PlayerEntity) this.getOwner(), particlePacket);
                    this.world.playSound(this.prevX, this.prevY, this.prevZ, SoundEvents.ENTITY_DRAGON_FIREBALL_EXPLODE, SoundCategory.BLOCKS, 0.4f, 2f, true);*/

                    EndGatewayBlockEntity endGatewayBlockEntity = (EndGatewayBlockEntity) this.world.getBlockEntity(dragonEggPos);
                    endGatewayBlockEntity.setExitPortalPos(new BlockPos(Shroomhearth.CONFIG.getSpawnGatewayExitX(), Shroomhearth.CONFIG.getSpawnGatewayExitY(), Shroomhearth.CONFIG.getSpawnGatewayExitZ()), true);

                    BlockPos oldEndGatewayPos = new BlockPos(Shroomhearth.CONFIG.getReturnGatewayX(), Shroomhearth.CONFIG.getReturnGatewayY(), Shroomhearth.CONFIG.getReturnGatewayZ());
                    if (!dragonEggPos.equals(oldEndGatewayPos) && this.world.getBlockState(oldEndGatewayPos).getBlock() instanceof EndGatewayBlock) {
                        this.world.setBlockState(oldEndGatewayPos, Blocks.AIR.getDefaultState());
                    }

                    Shroomhearth.CONFIG.setReturnGatewayX(dragonEggPos.getX());
                    Shroomhearth.CONFIG.setReturnGatewayY(dragonEggPos.getY());
                    Shroomhearth.CONFIG.setReturnGatewayZ(dragonEggPos.getZ());

                    BlockPos spawnGatewayPos = new BlockPos(Shroomhearth.CONFIG.getSpawnGatewayX(), Shroomhearth.CONFIG.getSpawnGatewayY(), Shroomhearth.CONFIG.getSpawnGatewayZ());
                    if (!(this.world.getBlockState(spawnGatewayPos).getBlock() instanceof EndGatewayBlock)) {
                        this.world.setBlockState(spawnGatewayPos, Blocks.END_GATEWAY.getDefaultState());
                    }
                    EndGatewayBlockEntity spawnGatewayBlockEntity = (EndGatewayBlockEntity) this.world.getBlockEntity(spawnGatewayPos);
                    spawnGatewayBlockEntity.setExitPortalPos(new BlockPos(Math.round(p.getX()), Math.round(p.getY()), Math.round(p.getZ())), true);

                    // make announcement
                    TextColor pColor = p.getDisplayName().getStyle().getColor();
                    String pColorName = "white";
                    if (pColor != null) { pColorName = p.getDisplayName().getStyle().getColor().getName(); }
                    s.getCommandManager().execute(s.getCommandSource(), "tellraw @a [{\"text\":\"The \"}, {\"color\":\"light_purple\",\"text\":\"Community Gateway\"}, {\"text\":\" was relocated to " + dragonEggPos.getX() + ", " + dragonEggPos.getY() + ", " + dragonEggPos.getZ() + " by \"}, {\"color\":\"" + pColorName + "\",\"text\": \"" + p.getEntityName() + "\"}]");

                    // grant advancement to player
                    ShroomhearthUtils.grantAdvancement(p, "community", "community_coordinator", "relocated_gateway");

                    this.remove(RemovalReason.DISCARDED);
                }
            }
        } catch (Exception e) {
            Shroomhearth.LOG.error("Caught error: " + e);
            e.printStackTrace();
        }
    }
}
