package com.epolixa.bityard;

import net.minecraft.advancement.Advancement;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.core.jmx.Server;

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
        } catch (Exception e) {
            Bityard.LOG.error("Caught error: " + e);
            e.printStackTrace();
        }

        return hex;
    }

    // return a random int between two ints
    public static int inRange(Random r, int min, int max) {
        return r.nextInt((max - min) + 1) + min;
    }

    // grant an advancement
    public static void grantAdvancement(PlayerEntity player, String namespace, String id, String criterion) {
        try {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
            Advancement advancement = player.getServer().getAdvancementLoader().get(new Identifier(namespace, id));
            if (!serverPlayer.getAdvancementTracker().getProgress(advancement).isDone()) {
                serverPlayer.getAdvancementTracker().grantCriterion(advancement, criterion);
            }
        } catch (Exception e) {
            Bityard.LOG.error("Caught error: " + e);
            e.printStackTrace();
        }
    }

}
