package com.epolixa.shroomhearth;

import com.epolixa.shroomhearth.mixin.SignBlockEntityAccessor;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import java.util.ArrayList;
import java.util.List;

public class MOTD {

    // set random MOTD
    public static void setMOTD(MinecraftServer server) {
        try {
            // Look for signs within message board area
            ServerLevel world = server.overworld();
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
                RandomSource random = world.getRandom();
                SignBlockEntity sign = signs.get(random.nextInt(signs.size()));
                SignBlockEntityAccessor signAccessor = (SignBlockEntityAccessor)sign;
                SignText frontText = signAccessor.getFrontText();
                Component[] signTexts = frontText.getMessages(false);
                StringBuilder signMessage = new StringBuilder();
                for (Component rowText : signTexts) { // parse sign rows
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
                motd.append(ShroomhearthUtils.getDyeColorCode(sign.getText(true).getColor()));
                if (frontText.hasGlowingText()) motd.append(ChatFormatting.BOLD);
                motd.append(signMessage);
                motd.append(ChatFormatting.RESET);
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
