package com.epolixa.shroomhearth.mixin;

import com.epolixa.shroomhearth.Shroomhearth;
import com.epolixa.shroomhearth.ShroomhearthUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEnderpearl;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DragonEggBlock;
import net.minecraft.world.level.block.EndGatewayBlock;
import net.minecraft.world.level.block.entity.TheEndGatewayBlockEntity;
import net.minecraft.world.phys.HitResult;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ThrownEnderpearl.class)
public abstract class EnderPearlEntityMixin extends ThrowableItemProjectile {

    public EnderPearlEntityMixin(EntityType<? extends ThrownEnderpearl> entityType, Level world) {
        super(entityType, world);
    }

    // Inject to setSneaking to call stopFallFlying
    @Inject(method = "onHit", at = @At("HEAD"))
    public void onCollision(HitResult hitResult, CallbackInfo info) {
        try {
            Level world = this.level();

            if (!world.isClientSide() && !this.isRemoved() && world.dimension() == Level.OVERWORLD) {

                BlockPos hitPos = BlockPos.containing(hitResult.getLocation());
                BlockPos dragonEggPos = null;
                DragonEggBlock dragonEggBlock = null;

                // check for dragon egg around hit pos
                for (int x = hitPos.getX() - 1; x <= hitPos.getX() + 1; x++) {
                    for (int y = hitPos.getY() - 1; y <= hitPos.getY() + 1; y++) {
                        for (int z = hitPos.getZ() - 1; z <= hitPos.getZ() + 1; z++) {
                            BlockPos blockPos = new BlockPos(x,y,z);
                            if (world.getBlockState(blockPos).getBlock() instanceof DragonEggBlock) {
                                dragonEggBlock = (DragonEggBlock) world.getBlockState(blockPos).getBlock();
                                dragonEggPos = blockPos;
                            }
                        }
                    }
                }

                if (dragonEggBlock != null) {
                    MinecraftServer s = this.level().getServer();
                    Player p = (Player) this.getOwner();

                    world.setBlockAndUpdate(dragonEggPos, Blocks.END_GATEWAY.defaultBlockState());
                    // play particle
                    /*ParticleS2CPacket particlePacket = new ParticleS2CPacket(
                                                        ParticleTypes.EXPLOSION, true,
                                                        this.prevX, this.prevY, this.prevZ,
                                                        0.0F, 0.0F, 0.0F, 1.0F, 1);
                    ServerSidePacketRegistry.INSTANCE.sendToPlayer((PlayerEntity) this.getOwner(), particlePacket);
                    this.world.playSound(this.prevX, this.prevY, this.prevZ, SoundEvents.ENTITY_DRAGON_FIREBALL_EXPLODE, SoundCategory.BLOCKS, 0.4f, 2f, true);*/

                    TheEndGatewayBlockEntity endGatewayBlockEntity = (TheEndGatewayBlockEntity) world.getBlockEntity(dragonEggPos);
                    endGatewayBlockEntity.setExitPosition(new BlockPos(Shroomhearth.CONFIG.getSpawnGatewayExitX(), Shroomhearth.CONFIG.getSpawnGatewayExitY(), Shroomhearth.CONFIG.getSpawnGatewayExitZ()), true);

                    BlockPos oldEndGatewayPos = new BlockPos(Shroomhearth.CONFIG.getReturnGatewayX(), Shroomhearth.CONFIG.getReturnGatewayY(), Shroomhearth.CONFIG.getReturnGatewayZ());
                    if (!dragonEggPos.equals(oldEndGatewayPos) && world.getBlockState(oldEndGatewayPos).getBlock() instanceof EndGatewayBlock) {
                        world.setBlockAndUpdate(oldEndGatewayPos, Blocks.AIR.defaultBlockState());
                    }

                    Shroomhearth.CONFIG.setReturnGatewayX(dragonEggPos.getX());
                    Shroomhearth.CONFIG.setReturnGatewayY(dragonEggPos.getY());
                    Shroomhearth.CONFIG.setReturnGatewayZ(dragonEggPos.getZ());

                    BlockPos spawnGatewayPos = new BlockPos(Shroomhearth.CONFIG.getSpawnGatewayX(), Shroomhearth.CONFIG.getSpawnGatewayY(), Shroomhearth.CONFIG.getSpawnGatewayZ());
                    if (!(world.getBlockState(spawnGatewayPos).getBlock() instanceof EndGatewayBlock)) {
                        world.setBlockAndUpdate(spawnGatewayPos, Blocks.END_GATEWAY.defaultBlockState());
                    }
                    TheEndGatewayBlockEntity spawnGatewayBlockEntity = (TheEndGatewayBlockEntity) world.getBlockEntity(spawnGatewayPos);
                    spawnGatewayBlockEntity.setExitPosition(BlockPos.containing(Math.round(p.getX()), Math.round(p.getY()), Math.round(p.getZ())), true);

                    // make announcement
                    TextColor pColor = p.getDisplayName().getStyle().getColor();
                    String pColorName = "white";
                    if (pColor != null) { pColorName = p.getDisplayName().getStyle().getColor().serialize(); }
                    s.getCommands().getDispatcher().execute("tellraw @a [{\"text\":\"The \"}, {\"color\":\"light_purple\",\"text\":\"Community Gateway\"}, {\"text\":\" was relocated to " + dragonEggPos.getX() + ", " + dragonEggPos.getY() + ", " + dragonEggPos.getZ() + " by \"}, {\"color\":\"" + pColorName + "\",\"text\": \"" + p.getScoreboardName() + "\"}]", s.createCommandSourceStack());

                    // grant advancement to player
                    ShroomhearthUtils.grantAdvancement(p, Shroomhearth.MOD_ID, "community_coordinator", "community_coordinator");

                    this.remove(RemovalReason.DISCARDED);

                }
            }
        } catch (Exception e) {
            Shroomhearth.LOG.error("Caught error: " + e);
            e.printStackTrace();
        }
    }
}
