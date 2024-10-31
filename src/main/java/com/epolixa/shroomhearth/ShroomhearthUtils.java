package com.epolixa.shroomhearth;

import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementEntry;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
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

    public static String getDyeColorCode(DyeColor color) {
        String code = Formatting.GRAY.toString(); // default server list description gray

        try {
            // https://minecraft.fandom.com/wiki/Formatting_codes#Color_codes
            code = switch (color) {
                case RED -> Formatting.RED.toString(); // red
                case GREEN -> Formatting.DARK_GREEN.toString(); // dark_green
                case PURPLE -> Formatting.DARK_PURPLE.toString(); // dark_purple
                case CYAN -> Formatting.DARK_AQUA.toString(); // dark_aqua
                case LIGHT_GRAY -> Formatting.GRAY.toString(); // gray
                case GRAY -> Formatting.DARK_GRAY.toString(); // dark_gray
                case PINK -> Formatting.LIGHT_PURPLE.toString(); // light_purple
                case LIME -> Formatting.GREEN.toString(); // green
                case YELLOW -> Formatting.YELLOW.toString(); // yellow
                case LIGHT_BLUE -> Formatting.BLUE.toString(); // blue
                case MAGENTA -> Formatting.AQUA.toString(); // aqua
                case ORANGE -> Formatting.GOLD.toString(); // gold
                case BLACK -> Formatting.BLACK.toString(); // black
                case BROWN -> Formatting.DARK_RED.toString(); // dark_red
                case BLUE -> Formatting.DARK_BLUE.toString(); // dark_blue
                case WHITE -> Formatting.WHITE.toString(); // white
                default -> Formatting.GRAY.toString();
            };
        } catch (Exception e) {
            Shroomhearth.LOG.error("Caught error: " + e);
            e.printStackTrace();
        }

        return code;
    }

    // return a random int between two ints
    public static int inRange(Random r, int min, int max) {
        return r.nextInt((max - min) + 1) + min;
    }

    // grant an advancement
    public static void grantAdvancement(PlayerEntity player, String namespace, String id, String criterion) {
        try {
            ServerPlayerEntity serverPlayer = (ServerPlayerEntity) player;
            AdvancementEntry advancement = player.getServer().getAdvancementLoader().get(Identifier.of(namespace, id));
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

    public static EquipmentSlot getEquipmentSlotFromHand(Hand hand) {
        EquipmentSlot slot = EquipmentSlot.MAINHAND;
        try {
            slot = switch (hand) {
                case MAIN_HAND -> EquipmentSlot.MAINHAND;
                case OFF_HAND -> EquipmentSlot.OFFHAND;
            };
        } catch (Exception e) {
            Shroomhearth.LOG.error("Caught error: " + e);
            e.printStackTrace();
        }
        return slot;
    }

    public static int getIntFromColor(int r, int g, int b) {
        int ret = 0;
        try {
            r = (r << 16) & 0x00FF0000; //Shift red 16-bits and mask out other stuff
            g = (g << 8) & 0x0000FF00; //Shift Green 8-bits and mask out other stuff
            b = b & 0x000000FF; //Mask out anything not blue.
            ret = 0xFF000000 | r | g | b; //0xFF000000 for 100% Alpha. Bitwise OR everything together.
        } catch (Exception e) {
            Shroomhearth.LOG.error("Caught error: " + e);
            e.printStackTrace();
        }
        return ret;
    }

}
