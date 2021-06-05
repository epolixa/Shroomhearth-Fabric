package com.epolixa.bityard.mixin;

import com.epolixa.bityard.Bityard;
import com.epolixa.bityard.BityardUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;
import java.util.List;

@Mixin(BeaconBlockEntity.class)
public abstract class BeaconBlockEntityMixin extends BlockEntity {

    public BeaconBlockEntityMixin(BlockPos pos, BlockState state) {
        super(BlockEntityType.BEACON, pos, state);
    }

    // Inject to applyPlayerChanges to look for signs and send title to newly affected players
    @Inject(method = "applyPlayerEffects(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;ILnet/minecraft/entity/effect/StatusEffect;Lnet/minecraft/entity/effect/StatusEffect;)V", at = @At("HEAD"))
    private static void applyPlayerEffects(World world, BlockPos pos, int beaconLevel, @Nullable StatusEffect primaryEffect, @Nullable StatusEffect secondaryEffect, CallbackInfo info) {
        try {
            if (!world.isClient && primaryEffect != null) { // check for same conditions as applying primary effect to a player
                double d = (double)(beaconLevel * 10 + 10);

                Box box = (new Box(pos)).expand(d).stretch(0.0D, (double)world.getHeight(), 0.0D);
                List<PlayerEntity> playerList = world.getNonSpectatingEntities(PlayerEntity.class, box);
                Iterator playerListIterator = playerList.iterator();

                PlayerEntity player;
                while(playerListIterator.hasNext()) {
                    player = (PlayerEntity)playerListIterator.next();
                    // check if player already has same status effect as primary
                    // only show message to newly affected players
                    if (!player.hasStatusEffect(primaryEffect)) {
                        // check for a sign block directly on top of beacon block
                        BlockEntity aboveBeacon = world.getBlockEntity(pos.add(0,1,0));
                        if (aboveBeacon != null && aboveBeacon instanceof SignBlockEntity) {

                            SignBlockEntity sign = (SignBlockEntity)aboveBeacon;
                            SignBlockEntityAccessor signAccessor = (SignBlockEntityAccessor)sign;
                            Text[] signText = signAccessor.getTexts(); // getTextOnRow is CLIENT-only
                            DyeColor color = sign.getTextColor();
                            StringBuilder sb = new StringBuilder();
                            for (int i = 0; i < signText.length; i++) // parse sign rows
                            {
                                Text rowText = signText[i];
                                String row = rowText.getString();
                                if (row.length() > 0) {
                                    if (sb.toString().length() > 0) {
                                        sb.append(" ");
                                    }
                                    sb.append(row);
                                }
                            }

                            if (sb.toString().length() > 0) {
                                // send a title message to the player
                                sendSubtitleToPlayer("{\"text\":\"" + sb.toString() + "\",\"color\":\"" + BityardUtils.getDyeHex(color) + "\",\"bold\":\"" + signAccessor.isGlowingText() + "\"}", player);

                                // grant advancement to player
                                world.getServer().getCommandManager().execute(world.getServer().getCommandSource(), "advancement grant " + player.getEntityName() + " only bityard:liminal_message");
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Bityard.LOG.error("Caught error: " + e);
            e.printStackTrace();
        }
    }

    private static void sendSubtitleToPlayer(String subtitle, PlayerEntity player) {
        try {
            String entityName = player.getEntityName();
            String subtitleCommand = "/title " + entityName + " subtitle " + subtitle;
            String titleCommand = "/title " + entityName + " title {\"text\":\"\"}";
            MinecraftServer server = player.getServer();
            server.getCommandManager().execute(server.getCommandSource(), subtitleCommand);
            server.getCommandManager().execute(server.getCommandSource(), titleCommand);
        } catch (Exception e) {
            Bityard.LOG.error("Caught error: " + e);
            e.printStackTrace();
        }
    }

}
