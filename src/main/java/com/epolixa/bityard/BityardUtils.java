package com.epolixa.bityard;

import com.epolixa.bityard.mixin.SignBlockEntityAccessor;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.scoreboard.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class BityardUtils {

    public static String getDyeHex(DyeColor color) {
        String hex = "#1D1D21"; // default black

        try {
            // refer to https://minecraft.gamepedia.com/Dye#Item_data
            switch(color) {
                case RED:
                    hex = "#B02E26";
                    break;
                case GREEN:
                    hex = "#5E7C16";
                    break;
                case PURPLE:
                    hex = "#8932B8";
                    break;
                case CYAN:
                    hex = "#169C9C";
                    break;
                case LIGHT_GRAY:
                    hex = "#9D9D97";
                    break;
                case GRAY:
                    hex = "#474F52";
                    break;
                case PINK:
                    hex = "#F38BAA";
                    break;
                case LIME:
                    hex = "#80C71F";
                    break;
                case YELLOW:
                    hex = "#FED83D";
                    break;
                case LIGHT_BLUE:
                    hex = "#3AB3DA";
                    break;
                case MAGENTA:
                    hex = "#C74EBD";
                    break;
                case ORANGE:
                    hex = "#F9801D";
                    break;
                case BLACK:
                    hex = "#1D1D21";
                    break;
                case BROWN:
                    hex = "#835432";
                    break;
                case BLUE:
                    hex = "#3C44AA";
                    break;
                case WHITE:
                    hex = "#F9FFFE";
                    break;
                default:
                    hex = "#1D1D21";
                    break;
            }
        } catch (Exception e) {Bityard.LOG.error(e);}

        return hex;
    }

    // return a random value between two values
    public static int inRange(Random r, int min, int max) {
        return r.nextInt((max - min) + 1) + min;
    }

    // returns either the current set config score value or the default if it does not exist
    public static int getConfig(String key, MinecraftServer server) {
        int value = 0;

        try {
            //log("enter");

            if (server == null) {
                server = Bityard.getServer();
            }

            if (server != null) {
                Scoreboard scoreboard = server.getScoreboard();
                ScoreboardObjective objective = scoreboard.getObjective(key);
                if (objective != null) {
                    ScoreboardPlayerScore score = scoreboard.getPlayerScore(Bityard.MOD_ID, objective);
                    if (score != null) {
                        value = score.getScore();
                        return value;
                    }
                }
            }

            value = Bityard.defaultConfig.get(key) == null ? 0 : Bityard.defaultConfig.get(key); // if server is still null at this point (did not initialize) just use default config

            //log("exit");
        } catch (Exception e) {Bityard.LOG.error(e);}

        return value;
    }


    public static void setConfig(String key, int value, MinecraftServer server) {
        try {
            if (server == null) {
                server = Bityard.getServer();
            }

            if (server != null) {
                String configPlayerName = Bityard.MOD_ID;
                Scoreboard scoreboard = server.getScoreboard();
                if (scoreboard.getObjective(key) == null) {
                    scoreboard.addObjective(key, ScoreboardCriterion.DUMMY, Text.Serializer.fromJson("{\"text\":\"" + key + "\"}"), ScoreboardCriterion.RenderType.INTEGER);
                }
                ScoreboardObjective objective = scoreboard.getObjective(key);
                scoreboard.getPlayerScore(configPlayerName, objective).setScore((Integer) value);
            }
        } catch (Exception e) {Bityard.LOG.error(e);}
    }


    public static void setMOTD(MinecraftServer server) {
        try {
            final int MOTD_X1 = getConfig("MOTD_X1", server);
            final int MOTD_Y1 = getConfig("MOTD_Y1", server);
            final int MOTD_Z1 = getConfig("MOTD_Z1", server);
            final int MOTD_X2 = getConfig("MOTD_X2", server);
            final int MOTD_Y2 = getConfig("MOTD_Y2", server);
            final int MOTD_Z2 = getConfig("MOTD_Z2", server);

            // Look for signs within message board area
            ServerWorld world = server.getOverworld();
            List<SignBlockEntity> signs = new ArrayList<SignBlockEntity>();
            for (int x = MOTD_X1; x <= MOTD_X2; x++) {
                for (int y = MOTD_Y1; y <= MOTD_Y2; y++) {
                    for (int z = MOTD_Z1; z <= MOTD_Z2; z++) {
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
                String motdColor = getDyeHex(sign.getTextColor());
                String motdJSON = "[{\"text\":\"" + motdMessage + "\",\"color\":\"" + motdColor + "\"}]";
                Bityard.LOG.info("MOTD set to: " + motdJSON);
                server.getServerMetadata().setDescription(Text.Serializer.fromJson(motdJSON));
            }
        } catch (Exception e) {Bityard.LOG.error(e);}
    }

    public static void setupScoreboardConfig(MinecraftServer server) {
        try {
            // capture the scoreboard
            ServerScoreboard scoreboard = server.getScoreboard();

            String configPlayerName = Bityard.MOD_ID;

            // for each default value...
            for (Map.Entry<String, Integer> entry : Bityard.defaultConfig.entrySet()) {
                // first check if scoreboard objective exists
                if (scoreboard.getObjective(entry.getKey()) == null) {
                    // if it does not exist, create it
                    scoreboard.addObjective(entry.getKey(), ScoreboardCriterion.DUMMY, Text.Serializer.fromJson("{\"text\":\"" + entry.getKey() + "\"}"), ScoreboardCriterion.RenderType.INTEGER);
                }
                ScoreboardObjective objective = scoreboard.getObjective(entry.getKey());
                // check if objective has been set for fake player, then set it if not
                if (!scoreboard.playerHasObjective(configPlayerName, objective)) {
                    // then create a fake player for it with a default hardcoded value
                    scoreboard.getPlayerScore(configPlayerName, objective).setScore((Integer) entry.getValue());
                }
            }
        } catch (Exception e) {Bityard.LOG.error(e);}
    }

}
