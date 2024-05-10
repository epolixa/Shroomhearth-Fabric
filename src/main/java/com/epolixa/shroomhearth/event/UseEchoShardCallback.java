package com.epolixa.shroomhearth.event;

import com.epolixa.shroomhearth.Shroomhearth;
import com.epolixa.shroomhearth.ShroomhearthUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class UseEchoShardCallback {

    public static ActionResult onUseSculkShriekerEchoShardCallback(PlayerEntity player, World world, Hand hand, BlockHitResult hitResult) {
        try {
            BlockPos pos = hitResult.getBlockPos();
            BlockState state = world.getBlockState(pos);
            if (!state.isAir()) {
                ActionResult actionResult = player.isSneaking() ? ActionResult.PASS : state.onUse(world, player, hitResult);
                if (actionResult.isAccepted()) {
                    return ActionResult.FAIL;
                } else {
                    ItemStack handItemStack = player.getStackInHand(hand);
                    if (handItemStack.isOf(Items.ECHO_SHARD)) {
                        if (state.getProperties().contains(Properties.CAN_SUMMON) && state.getBlock().equals(Blocks.SCULK_SHRIEKER)) {
                            if (!state.get(Properties.CAN_SUMMON)) {
                                world.setBlockState(pos, state.with(Properties.CAN_SUMMON, true));
                                player.swingHand(hand, true);
                                world.playSound(null, pos, SoundEvents.BLOCK_SCULK_SHRIEKER_BREAK, SoundCategory.BLOCKS, 1f, 0.8f);
                                if (!player.isCreative()) {
                                    handItemStack.decrement(1);
                                }
                                ShroomhearthUtils.grantAdvancement(player, "shroomhearth_fabric", "prepare_for_trouble", "impossible");
                                return ActionResult.SUCCESS;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Shroomhearth.LOG.error("Caught error: " + e);
            e.printStackTrace();
        }
        return ActionResult.PASS;
    }


    public static ActionResult onUseMobEchoShardCallback(PlayerEntity player, World world, Hand hand, Entity entity, EntityHitResult hitResult) {
        try {
            if (entity instanceof LivingEntity && !(entity instanceof PlayerEntity)) {
                LivingEntity livingEntity = (LivingEntity) entity;
                ItemStack handItemStack = player.getStackInHand(hand);
                if (handItemStack.isOf(Items.ECHO_SHARD) && !livingEntity.isSilent()) {
                    Vec3d pos = livingEntity.getPos();
                    Box box = livingEntity.getBoundingBox(livingEntity.getPose());
                    livingEntity.setSilent(true);
                    player.swingHand(hand, true);
                    world.playSound(null, livingEntity.getBlockPos(), SoundEvents.BLOCK_SCULK_SPREAD, SoundCategory.NEUTRAL, 1f, 0.8f);
                    ((ServerWorld)world).spawnParticles(
                            ParticleTypes.SCULK_CHARGE_POP,
                            pos.getX(),
                            pos.getY() + livingEntity.getHeight()/2,
                            pos.getZ(),
                            8, box.getLengthX()/2, box.getLengthY()/2, box.getLengthZ()/2, 0.01f);
                    if (!player.isCreative()) {
                        handItemStack.decrement(1);
                    }
                    return ActionResult.SUCCESS;
                }
            }
        } catch (Exception e) {
            Shroomhearth.LOG.error("Caught error: " + e);
            e.printStackTrace();
        }
        return ActionResult.PASS;
    }
}
