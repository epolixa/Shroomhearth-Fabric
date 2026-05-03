package com.epolixa.shroomhearth.mixin;

import com.epolixa.shroomhearth.Shroomhearth;
import com.epolixa.shroomhearth.ShroomhearthUtils;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.*;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BeaconBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
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
    @Inject(method = "applyEffects(Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;ILnet/minecraft/core/Holder;Lnet/minecraft/core/Holder;)V", at = @At("HEAD"))
    private static void applyPlayerEffects(Level world, BlockPos pos, int beaconLevel, @Nullable Holder<MobEffect> primaryEffect, @Nullable Holder<MobEffect> secondaryEffect, CallbackInfo info) {
        try {
            if (!world.isClientSide() && primaryEffect != null) { // check for same conditions as applying primary effect to a player
                double d = (double)(beaconLevel * 10 + 10);

                AABB box = (new AABB(pos)).inflate(d).expandTowards(0.0D, (double)world.getHeight(), 0.0D);
                List<Player> playerList = world.getEntitiesOfClass(Player.class, box);
                Iterator playerListIterator = playerList.iterator();

                Player player;
                while(playerListIterator.hasNext()) {
                    player = (Player)playerListIterator.next();
                    // check if player already has same status effect as primary
                    // only show message to newly affected players
                    if (!player.hasEffect(primaryEffect)) {
                        // check for a signs adjacent to beacon block
                        List<SignBlockEntity> signs = new ArrayList<>();
                        List<Vec3i> signOffsets= new ArrayList<>();
                        signOffsets.add(new Vec3i(0,1,0));
                        signOffsets.add(new Vec3i(1,0,0));
                        signOffsets.add(new Vec3i(-1,0,0));
                        signOffsets.add(new Vec3i(0,0,1));
                        signOffsets.add(new Vec3i(0,0,-1));
                        for (Vec3i offset : signOffsets) {
                            BlockEntity beaconSign = world.getBlockEntity(pos.offset(offset));
                            if (beaconSign instanceof SignBlockEntity) {signs.add((SignBlockEntity) beaconSign);}
                        }
                        // check for banner above beacon
                        boolean showIllagerAlt = false;
                        BlockEntity banner = world.getBlockEntity(pos.offset(new Vec3i(0,1,0)));
                        if (banner instanceof BannerBlockEntity) {
                            // check if banner is ominous
                            showIllagerAlt = isOminous((BannerBlockEntity) banner);
                        }
                        if (!signs.isEmpty()) {
                            RandomSource random = world.getRandom();
                            SignBlockEntity sign = signs.get(random.nextInt(signs.size()));
                            SignBlockEntityAccessor signAccessor = (SignBlockEntityAccessor)sign;
                            SignText frontText = signAccessor.getFrontText(); //Text[] signText = signAccessor.getTexts();
                            Component[] frontMessages = frontText.getMessages(false);
                            DyeColor color = sign.getText(true).getColor(); // potentially enhance to consider front/back as random options
                            StringBuilder sb = new StringBuilder();
                            for (int i = 0; i < frontMessages.length; i++) // parse sign rows
                            {
                                Component rowText = frontMessages[i];
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
                                //sendSubtitleToPlayer("{\"text\":\""+sb.toString()+"\","+"\"color\":\""+ShroomhearthUtils.getDyeHex(color)+"\","+"\"bold\":"+frontText.hasGlowingText()+(showIllagerAlt?",\"font\":\"illageralt\"}]":"}"), player);
                                sendSubtitleToPlayer2(sb.toString(), player, TextColor.fromRgb(color.getTextColor()), frontText.hasGlowingText(), showIllagerAlt);

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

    /*private static void sendSubtitleToPlayer(String subtitle, Player player) {
        try {
            ServerPlayer serverPlayer = (ServerPlayer) player;
            Function<Component, Packet<?>> constructor = ClientboundSetSubtitleTextPacket::new;
            CommandSourceStack source = serverPlayer.createCommandSourceStack();
            Component subtitleText = ComponentSerialization.CODEC.parse(JsonOps.INSTANCE, JsonParser.parseString(subtitle)).getOrThrow();
            serverPlayer.connection.send((Packet)constructor.apply(ComponentUtils.updateForEntity(source, subtitleText, serverPlayer, 0)));
            constructor = ClientboundSetTitleTextPacket::new;
            serverPlayer.connection.send((Packet)constructor.apply(ComponentUtils.updateForEntity(source, Component.nullToEmpty(""), serverPlayer, 0)));
        } catch (Exception e) {
            Shroomhearth.LOG.error("Caught error: " + e);
            e.printStackTrace();
        }
    }*/


    private static void sendSubtitleToPlayer2(String text, Player player, TextColor color, boolean glowing, boolean illagerAlt) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;

        Style style = Style.EMPTY
            .withColor(color)
            .withBold(glowing);

        if (illagerAlt) {
            style = style.withFont(new FontDescription.Resource(Identifier.parse("minecraft:illageralt")));
        }

        Component subtitle = Component.literal(text).setStyle(style);

        serverPlayer.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
        serverPlayer.connection.send(new ClientboundSetTitleTextPacket(Component.empty()));
    }

    private static boolean isOminous(BannerBlockEntity banner) {
        boolean ret = false;
        try {
            ret = banner.getPatterns().layers().size() >= 8;
        } catch (Exception e) {
            Shroomhearth.LOG.error("Caught error: " + e);
            e.printStackTrace();
        }
        return ret;
    }

}
