package com.epolixa.shroomhearth.event;

import com.epolixa.shroomhearth.Shroomhearth;
import com.epolixa.shroomhearth.ShroomhearthUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class UseEchoShardCallback {

    public static InteractionResult onUseSculkShriekerEchoShardCallback(Player player, Level world, InteractionHand hand, BlockHitResult hitResult) {
        try {
            BlockPos pos = hitResult.getBlockPos();
            BlockState state = world.getBlockState(pos);
            ItemStack handItemStack = player.getItemInHand(hand);
            if (state.getBlock().equals(Blocks.SCULK_SHRIEKER) && handItemStack.is(Items.ECHO_SHARD) && !player.isShiftKeyDown()) {
                if (!state.getValue(BlockStateProperties.CAN_SUMMON)) {
                    world.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.CAN_SUMMON, true));
                    player.swing(hand, true);
                    world.playSound(null, pos, SoundEvents.SCULK_SHRIEKER_BREAK, SoundSource.BLOCKS, 1f, 0.8f);
                    if (!player.isCreative()) {
                        handItemStack.shrink(1);
                    }
                    ShroomhearthUtils.grantAdvancement(player, "shroomhearth_fabric", "prepare_for_trouble", "impossible");
                    return InteractionResult.SUCCESS;
                }
            }
        } catch (Exception e) {
            Shroomhearth.LOG.error("Caught error: " + e);
            e.printStackTrace();
        }
        return InteractionResult.PASS;
    }


    public static InteractionResult onUseMobEchoShardCallback(Player player, Level world, InteractionHand hand, Entity entity, EntityHitResult hitResult) {
        try {
            if (entity instanceof LivingEntity && !(entity instanceof Player)) {
                LivingEntity livingEntity = (LivingEntity) entity;
                ItemStack handItemStack = player.getItemInHand(hand);
                if (handItemStack.is(Items.ECHO_SHARD) && !livingEntity.isSilent()) {
                    Vec3 pos = livingEntity.position();
                    AABB box = livingEntity.getLocalBoundsForPose(livingEntity.getPose());
                    livingEntity.setSilent(true);
                    player.swing(hand, true);
                    world.playSound(null, livingEntity.blockPosition(), SoundEvents.SCULK_BLOCK_SPREAD, SoundSource.NEUTRAL, 1f, 0.8f);
                    ((ServerLevel)world).sendParticles(
                            ParticleTypes.SCULK_CHARGE_POP,
                            pos.x(),
                            pos.y() + livingEntity.getBbHeight()/2,
                            pos.z(),
                            8, box.getXsize()/2, box.getYsize()/2, box.getZsize()/2, 0.01f);
                    if (!player.isCreative()) {
                        handItemStack.shrink(1);
                    }
                    return InteractionResult.SUCCESS;
                }
            }
        } catch (Exception e) {
            Shroomhearth.LOG.error("Caught error: " + e);
            e.printStackTrace();
        }
        return InteractionResult.PASS;
    }
}
