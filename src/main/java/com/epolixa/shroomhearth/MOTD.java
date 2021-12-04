package com.epolixa.shroomhearth;

import com.epolixa.shroomhearth.mixin.SignBlockEntityAccessor;
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
            for (int x = Shroomhearth.CONFIG.getMotdMinX(); x <= Shroomhearth.CONFIG.getMotdMaxX(); x++) {
                for (int y = Shroomhearth.CONFIG.getMotdMinY(); y <= Shroomhearth.CONFIG.getMotdMaxY(); y++) {
                    for (int z = Shroomhearth.CONFIG.getMotdMinZ(); z <= Shroomhearth.CONFIG.getMotdMaxZ(); z++) {
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
                SignBlockEntityAccessor signAccessor = (SignBlockEntityAccessor)sign;
                Text[] signText = signAccessor.getTexts();
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
                String motdColor = ShroomhearthUtils.getDyeHex(sign.getTextColor());
                String motdJSON = "[{\"text\":\"" + motdMessage + "\",\"color\":\"" + motdColor + "\",\"bold\":\"" + signAccessor.isGlowingText() + "\"}]";
                Shroomhearth.LOG.info("MOTD set to: " + motdJSON);
                server.getServerMetadata().setDescription(Text.Serializer.fromJson(motdJSON));
            }
        } catch (Exception e) {
            Shroomhearth.LOG.error("Caught error: " + e);
            e.printStackTrace();
        }
    }
}
