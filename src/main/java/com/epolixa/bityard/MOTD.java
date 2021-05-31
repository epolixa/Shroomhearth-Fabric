package com.epolixa.bityard;

import com.epolixa.bityard.mixin.SignBlockEntityAccessor;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MOTD {

    // set random MOTD
    public static void setMOTD(MinecraftServer server) {
        try {
            // Look for signs within message board area
            ServerWorld world = server.getOverworld();
            List<SignBlockEntity> signs = new ArrayList<>();
            for (int x = Bityard.CONFIG.getMotdMinX(); x <= Bityard.CONFIG.getMotdMaxX(); x++) {
                for (int y = Bityard.CONFIG.getMotdMinY(); y <= Bityard.CONFIG.getMotdMaxY(); y++) {
                    for (int z = Bityard.CONFIG.getMotdMinZ(); z <= Bityard.CONFIG.getMotdMaxZ(); z++) {
                        BlockEntity blockEntity = world.getBlockEntity(new BlockPos(x,y,z));
                        if (blockEntity instanceof SignBlockEntity) {
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
                for (Text rowText : signText) { // parse sign rows
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
                Bityard.LOG.info("MOTD set to: " + motdJSON);
                server.getServerMetadata().setDescription(Text.Serializer.fromJson(motdJSON));
            }
        } catch (Exception e) {
            Bityard.LOG.error("Caught error: " + e);
            e.printStackTrace();
        }
    }
}
