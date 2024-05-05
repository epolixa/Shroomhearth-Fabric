package com.epolixa.shroomhearth.mixin;

import com.epolixa.shroomhearth.Shroomhearth;
import com.epolixa.shroomhearth.ShroomhearthUtils;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.*;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;

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
                        // check for a signs adjacent to beacon block
                        List<SignBlockEntity> signs = new ArrayList<>();
                        List<Vec3i> signOffsets= new ArrayList<>();
                        signOffsets.add(new Vec3i(0,1,0));
                        signOffsets.add(new Vec3i(1,0,0));
                        signOffsets.add(new Vec3i(-1,0,0));
                        signOffsets.add(new Vec3i(0,0,1));
                        signOffsets.add(new Vec3i(0,0,-1));
                        for (Vec3i offset : signOffsets) {
                            BlockEntity beaconSign = world.getBlockEntity(pos.add(offset));
                            if (beaconSign instanceof SignBlockEntity) {signs.add((SignBlockEntity) beaconSign);}
                        }
                        // check for banner above beacon
                        boolean showIllagerAlt = false;
                        BlockEntity banner = world.getBlockEntity(pos.add(new Vec3i(0,1,0)));
                        if (banner instanceof BannerBlockEntity) {
                            // check if banner is ominous
                            showIllagerAlt = isOminous((BannerBlockEntity) banner);
                        }
                        if (!signs.isEmpty()) {
                            Random random = world.getRandom();
                            SignBlockEntity sign = signs.get(random.nextInt(signs.size()));
                            SignBlockEntityAccessor signAccessor = (SignBlockEntityAccessor)sign;
                            SignText frontText = signAccessor.getFrontText(); //Text[] signText = signAccessor.getTexts();
                            Text[] frontMessages = frontText.getMessages(false);
                            DyeColor color = sign.getText(true).getColor(); // potentially enhance to consider front/back as random options
                            StringBuilder sb = new StringBuilder();
                            for (int i = 0; i < frontMessages.length; i++) // parse sign rows
                            {
                                Text rowText = frontMessages[i];
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
                                sendSubtitleToPlayer("{\"text\":\""+sb.toString()+"\","+"\"color\":\""+ShroomhearthUtils.getDyeHex(color)+"\","+"\"bold\":\""+frontText.isGlowing()+"\""+(showIllagerAlt?",\"font\":\"illageralt\"}":"}"), player);

                                // grant advancement to player
                                ShroomhearthUtils.grantAdvancement(player, "shroomhearth_fabric", "liminal_message", "impossible");
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Shroomhearth.LOG.error("Caught error: " + e);
            e.printStackTrace();
        }
    }

    private static void sendSubtitleToPlayer(String subtitle, PlayerEntity player) {
        try {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
            Function<Text, Packet<?>> constructor = SubtitleS2CPacket::new;
            ServerCommandSource source = player.getServer().getCommandSource();
            Text subtitleText = Text.Serialization.fromJson(subtitle);
            serverPlayer.networkHandler.sendPacket((Packet)constructor.apply(Texts.parse(source, subtitleText, serverPlayer, 0)));
            constructor = TitleS2CPacket::new;
            serverPlayer.networkHandler.sendPacket((Packet)constructor.apply(Texts.parse(source, Text.of(""), serverPlayer, 0)));
        } catch (Exception e) {
            Shroomhearth.LOG.error("Caught error: " + e);
            e.printStackTrace();
        }
    }

    private static boolean isOminous(BannerBlockEntity banner) {
        boolean ret = false;
        try {
            ret = banner.getPatterns().size() >= 8;
        } catch (Exception e) {
            Shroomhearth.LOG.error("Caught error: " + e);
            e.printStackTrace();
        }
        return ret;
    }

}
