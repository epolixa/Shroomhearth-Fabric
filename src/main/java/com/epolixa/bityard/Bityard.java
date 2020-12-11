package com.epolixa.bityard;

import com.epolixa.bityard.mixin.SignBlockEntityAccessor;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Bityard implements ModInitializer {

    public static final String MOD_ID = "bityard";

    @Override
    public void onInitialize() {
        try {
            BityardUtils.log("[onInitialize] Enter");

            BityardUtils.log("[onInitialize] registering server lifecycle events");
            ServerLifecycleEvents.SERVER_STARTED.register((server) -> onServerStarted(server));

            BityardUtils.log("[onInitialize] Exit");
        } catch (Exception e) {
            BityardUtils.log("[onInitialize] caught error: " + e);
        }
    }

    private void onServerStarted(MinecraftServer server) {
        try {
            BityardUtils.log("[onServerStarted] Enter: server = " + server.toString());

            final int MOTD_X_MIN = 0;
            final int MOTD_Y_MIN = 64;
            final int MOTD_Z_MIN = 0;
            final int MOTD_X_MAX = 10;
            final int MOTD_Y_MAX = 74;
            final int MOTD_Z_MAX = 0;

            // Look for signs within message board area
            ServerWorld world = server.getOverworld();
            List<SignBlockEntity> signs = new ArrayList<SignBlockEntity>();
            for (int x = MOTD_X_MIN; x <= MOTD_X_MAX; x++) {
                for (int y = MOTD_Y_MIN; y <= MOTD_Y_MAX; y++) {
                    for (int z = MOTD_Z_MIN; z <= MOTD_Z_MAX; z++) {
                        BlockEntity blockEntity = world.getBlockEntity(new BlockPos(x,y,z));
                        if (blockEntity instanceof SignBlockEntity) {
                            BityardUtils.log("[onServerStarted] sign block entity found at: " + x + ", " + y + ", " + z);
                            signs.add((SignBlockEntity) blockEntity);
                        }
                    }
                }
            }

            // Set server motd from a random sign in the area
            if (!signs.isEmpty()) {
                Random random = world.getRandom();
                SignBlockEntity sign = signs.get(random.nextInt(signs.size()));
                Text[] signText = ((SignBlockEntityAccessor) sign).getText();
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
                BityardUtils.log("[onServerStarted] selected motd: " + motdJSON);
                server.getServerMetadata().setDescription(Text.Serializer.fromJson(motdJSON));
            }

            BityardUtils.log("[onServerStarted] Exit");
        } catch (Exception e) {
            BityardUtils.log("[onServerStarted] caught error: " + e);
        }
    }
}