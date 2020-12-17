package com.epolixa.bityard;

import com.epolixa.bityard.mixin.SignBlockEntityAccessor;
import com.google.common.collect.Maps;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.scoreboard.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.*;

public class Bityard implements ModInitializer {

    public static final String MOD_ID = "bityard";
    public static final Map<String, Integer> defaultConfig = Maps.newHashMap();

    @Override
    public void onInitialize() {
        try {
            BityardUtils.log("enter");

            BityardUtils.log("registering server lifecycle events");
            ServerLifecycleEvents.SERVER_STARTED.register((server) -> onServerStarted(server));

            initializeDefaultConfig();

            BityardUtils.log("exit");
        } catch (Exception e) {
            BityardUtils.log("caught error: " + e);
        }
    }

    private void onServerStarted(MinecraftServer server) {
        try {
            BityardUtils.log("enter: server = " + server.toString());

            setMOTD(server);
            setupScoreboardConfig(server);

            BityardUtils.log("exit");
        } catch (Exception e) {
            BityardUtils.log("caught error: " + e);
        }
    }


    private void initializeDefaultConfig() {
        try {
            BityardUtils.log("enter");

            // AFK idle time
            defaultConfig.put("IDLE_TIME", 240);

            // message of the day board area coords
            defaultConfig.put("MOTD_X1", -346);
            defaultConfig.put("MOTD_Y1", 67);
            defaultConfig.put("MOTD_Z1", 259);
            defaultConfig.put("MOTD_X2", -346);
            defaultConfig.put("MOTD_Y2", 73);
            defaultConfig.put("MOTD_Z2", 264);

            // community gateway coords
            defaultConfig.put("GATE_X", -378); // spawn gateway coords
            defaultConfig.put("GATE_Y", 70);
            defaultConfig.put("GATE_Z", 267);
            defaultConfig.put("GATE_EXIT_X", -378); // spawn gateway exit coords
            defaultConfig.put("GATE_EXIT_Y", 66);
            defaultConfig.put("GATE_EXIT_Z", 261);
            defaultConfig.put("RET_GATE_X", -6225); // return gateway coords
            defaultConfig.put("RET_GATE_Y", 73);
            defaultConfig.put("RET_GATE_Z", 1099);

            BityardUtils.log("exit");
        } catch (Exception e) {
            BityardUtils.log("caught error: " + e);
        }
    }

    // returns either the current set config score value or the default if it does not exist
    private int getConfig(MinecraftServer server, String key) {
        int value = 0;

        try {
            BityardUtils.log("enter");

            Scoreboard scoreboard = server.getScoreboard();
            ScoreboardObjective objective = scoreboard.getObjective(key);
            if (objective != null) {
                ScoreboardPlayerScore score = scoreboard.getPlayerScore(MOD_ID, objective);
                if (score != null) {
                    value = score.getScore();
                    return value;
                }
            }

            value = defaultConfig.get(key);

            BityardUtils.log("exit");
        } catch (Exception e) {
            BityardUtils.log("caught error: " + e);
        }

        return value;
    }

    private void setMOTD(MinecraftServer server) {
        try {
            BityardUtils.log("enter: server = " + server.toString());

            final int MOTD_X1 = getConfig(server, "MOTD_X1");
            final int MOTD_Y1 = getConfig(server, "MOTD_Y1");
            final int MOTD_Z1 = getConfig(server, "MOTD_Z1");
            final int MOTD_X2 = getConfig(server, "MOTD_X2");
            final int MOTD_Y2 = getConfig(server, "MOTD_Y2");
            final int MOTD_Z2 = getConfig(server, "MOTD_Z2");

            // Look for signs within message board area
            ServerWorld world = server.getOverworld();
            List<SignBlockEntity> signs = new ArrayList<SignBlockEntity>();
            for (int x = MOTD_X1; x <= MOTD_X2; x++) {
                for (int y = MOTD_Y1; y <= MOTD_Y2; y++) {
                    for (int z = MOTD_Z1; z <= MOTD_Z2; z++) {
                        BlockEntity blockEntity = world.getBlockEntity(new BlockPos(x,y,z));
                        if (blockEntity instanceof SignBlockEntity) {
                            BityardUtils.log("sign block entity found at: " + x + ", " + y + ", " + z);
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
                BityardUtils.log("selected motd: " + motdJSON);
                server.getServerMetadata().setDescription(Text.Serializer.fromJson(motdJSON));
            }

            BityardUtils.log("exit");
        } catch (Exception e) {
            BityardUtils.log("caught error: " + e);
        }
    }

    private void setupScoreboardConfig(MinecraftServer server) {
        try {
            BityardUtils.log("enter");

            // capture the scoreboard
            ServerScoreboard scoreboard = server.getScoreboard();

            String configPlayerName = MOD_ID;

            // for each default value...
            for (Map.Entry<String, Integer> entry : defaultConfig.entrySet()) {
                BityardUtils.log("setting up config score \"" + entry.getKey() + "\"");
                // first check if scoreboard objective exists
                if (scoreboard.getObjective(entry.getKey()) == null) {
                    BityardUtils.log("creating missing objective for \"" + entry.getKey() + "\"");
                    // if it does not exist, create it
                    scoreboard.addObjective(entry.getKey(), ScoreboardCriterion.DUMMY, Text.Serializer.fromJson("{\"text\":\"" + entry.getKey() + "\"}"), ScoreboardCriterion.RenderType.INTEGER);
                }
                ScoreboardObjective objective = scoreboard.getObjective(entry.getKey());
                // check if objective has been set for fake player, then set it if not
                if (!scoreboard.playerHasObjective(configPlayerName, objective)) {
                    BityardUtils.log("setting \"" + entry.getKey() + "\" score for \"" + configPlayerName + "\" to default " + entry.getValue());
                    // then create a fake player for it with a default hardcoded value
                    scoreboard.getPlayerScore(configPlayerName, objective).setScore((Integer) entry.getValue());
                }
                BityardUtils.log("\"" + configPlayerName + "\" has score " + scoreboard.getPlayerScore(configPlayerName, objective).getScore() + " for \"" + entry.getKey() + "\"");
            }

            BityardUtils.log("exit");
        } catch (Exception e) {
            BityardUtils.log("caught error: " + e);
        }
    }
}