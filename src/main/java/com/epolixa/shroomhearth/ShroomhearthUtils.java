package com.epolixa.shroomhearth;

import net.minecraft.advancement.Advancement;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;

public class ShroomhearthUtils {

    public static String getDyeHex(DyeColor color) {
        String hex = "#808080"; // default server list description gray

        try {
            // refer to https://minecraft.gamepedia.com/Dye#Item_data
            hex = switch (color) {
                case RED -> "#B02E26";
                case GREEN -> "#5E7C16";
                case PURPLE -> "#8932B8";
                case CYAN -> "#169C9C";
                case LIGHT_GRAY -> "#9D9D97";
                case GRAY -> "#474F52";
                case PINK -> "#F38BAA";
                case LIME -> "#80C71F";
                case YELLOW -> "#FED83D";
                case LIGHT_BLUE -> "#3AB3DA";
                case MAGENTA -> "#C74EBD";
                case ORANGE -> "#F9801D";
                case BLACK -> "#1D1D21";
                case BROWN -> "#835432";
                case BLUE -> "#3C44AA";
                case WHITE -> "#F9FFFE";
                default -> "#808080";
            };
        } catch (Exception e) {
            Shroomhearth.LOG.error("Caught error: " + e);
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
            if (advancement != null) {
                if (!serverPlayer.getAdvancementTracker().getProgress(advancement).isDone()) {
                    serverPlayer.getAdvancementTracker().grantCriterion(advancement, criterion);
                }
            } else {
                Shroomhearth.LOG.warn("Advancement \"" + namespace + ":" + id + "\" not identified, may be missing datapack");
            }

        } catch (Exception e) {
            Shroomhearth.LOG.error("Caught error: " + e);
            e.printStackTrace();
        }
    }

}
