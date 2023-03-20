package com.epolixa.shroomhearth;

import com.epolixa.shroomhearth.mixin.SignBlockEntityAccessor;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

import java.util.ArrayList;
import java.util.List;

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
                Text[] signTexts = signAccessor.getTexts();
                StringBuilder signMessage = new StringBuilder();
                for (Text rowText : signTexts) { // parse sign rows
                    String row = rowText.getString();
                    if (row.length() > 0) {
                        if (signMessage.toString().length() > 0) {
                            signMessage.append(" ");
                        }
                        signMessage.append(row);
                    }
                }

                // May bring back this text component method later if workaround for setDescription can be figured out
                //String motdColor = ShroomhearthUtils.getDyeHex(sign.getTextColor());
                //String motdJSON = "[{\"text\":\"" + signMessage + "\",\"color\":\"" + motdColor + "\",\"bold\":\"" + signAccessor.isGlowingText() + "\"}]";
                //server.getServerMetadata().setDescription(Text.Serializer.fromJson(motdJSON));

                StringBuilder motd = new StringBuilder();
                motd.append(ShroomhearthUtils.getDyeColorCode(sign.getTextColor()));
                if (signAccessor.isGlowingText()) motd.append(Formatting.BOLD);
                motd.append(signMessage);
                motd.append(Formatting.RESET);
                server.setMotd(motd.toString());

                Shroomhearth.LOG.info("MOTD set to: " + motd);
            } else {
                Shroomhearth.LOG.info("Did not find any signs to set MOTD from, defaulting to server.properties");
            }
        } catch (Exception e) {
            Shroomhearth.LOG.error("Caught error: " + e);
            e.printStackTrace();
        }
    }
}
