package com.epolixa.bityard.mixin;

import com.epolixa.bityard.BityardUtils;
import net.minecraft.block.*;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.network.packet.c2s.query.QueryRequestC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerQueryNetworkHandler;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Mixin(ServerQueryNetworkHandler.class)
public abstract class ServerQueryNetworkHandlerMixin {

    @Shadow @Final private MinecraftServer server;

    private final int MOTD_X_MIN = 0;
    private final int MOTD_Y_MIN = 64;
    private final int MOTD_Z_MIN = 0;
    private final int MOTD_X_MAX = 10;
    private final int MOTD_Y_MAX = 74;
    private final int MOTD_Z_MAX = 0;

    @Inject(method = "onRequest", at = @At("HEAD"))
    public void onRequest(QueryRequestC2SPacket packet, CallbackInfo info) {
        try {
            System.out.println("[ServerQueryNetworkHandlerMixin][onRequest] enter");

            // Look for signs within message board area
            ServerWorld world = server.getOverworld();
            System.out.println("[ServerQueryNetworkHandlerMixin][onRequest] world: " + world.toString());
            List<SignBlockEntity> signs = new ArrayList<SignBlockEntity>();
            for (int x = MOTD_X_MIN; x <= MOTD_X_MAX; x++) {
                for (int y = MOTD_Y_MIN; y <= MOTD_Y_MAX; y++) {
                    for (int z = MOTD_Z_MIN; z <= MOTD_Z_MAX; z++) {
                        BlockPos blockPos = new BlockPos(x,y,z);
                        BlockState blockState = world.getBlockState(blockPos);
                        BlockEntity blockEntity = world.getBlockEntity(blockPos);
                        if (blockEntity instanceof SignBlockEntity) {
                            System.out.println("[ServerQueryNetworkHandlerMixin][onRequest] sign block entity found at: " + x + ", " + y + ", " + z);
                            signs.add((SignBlockEntity)blockEntity);
                        } else if (blockState.getBlock() instanceof AbstractSignBlock) {
                            System.out.println("[ServerQueryNetworkHandlerMixin][onRequest] sign block found at: " + x + ", " + y + ", " + z);
                            AbstractSignBlock signBlock = (AbstractSignBlock)(blockState.getBlock());
                            ChunkPos signChunkPos = world.getChunk(blockPos).getPos();
                            SignBlockEntity signBlockEntity = (SignBlockEntity)(signBlock.createBlockEntity(world.getExistingChunk(signChunkPos.x, signChunkPos.z)));
                            signs.add(signBlockEntity);
                        }
                    }
                }
            }

            // Set server motd from a random sign in the area
            if (!signs.isEmpty()) {
                System.out.println("[ServerQueryNetworkHandlerMixin][onRequest] signs: " + signs.toString());
                Random random = world.getRandom();
                SignBlockEntity sign = signs.get(random.nextInt(signs.size()));
                System.out.println("[ServerQueryNetworkHandlerMixin][onRequest] picked sign: " + sign.toString());
                Text[] signText = ((SignBlockEntityAccessor)sign).getText();
                System.out.println("[ServerQueryNetworkHandlerMixin][onRequest] sign text: " + signText.toString());
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
                String motdMessage = sb.toString();
                String motdColor = BityardUtils.getDyeHex(sign.getTextColor());
                String motdJSON = "[{\"text\":\"" + motdMessage + "\",\"color\":\"" + motdColor + "\"}]";
                server.getServerMetadata().setDescription(Text.Serializer.fromJson(motdJSON));
            }

            System.out.println("[ServerQueryNetworkHandlerMixin][onRequest] exit");
        } catch (Exception e) {
            System.out.println("[ServerQueryNetworkHandlerMixin][onRequest] caught error: " + e);
        }
    }
}
