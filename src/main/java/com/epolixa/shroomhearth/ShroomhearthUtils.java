package com.epolixa.shroomhearth;

import net.minecraft.ChatFormatting;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;

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
        String code = ChatFormatting.GRAY.toString(); // default server list description gray

        try {
            // https://minecraft.fandom.com/wiki/Formatting_codes#Color_codes
            code = switch (color) {
                case RED -> ChatFormatting.RED.toString(); // red
                case GREEN -> ChatFormatting.DARK_GREEN.toString(); // dark_green
                case PURPLE -> ChatFormatting.DARK_PURPLE.toString(); // dark_purple
                case CYAN -> ChatFormatting.DARK_AQUA.toString(); // dark_aqua
                case LIGHT_GRAY -> ChatFormatting.GRAY.toString(); // gray
                case GRAY -> ChatFormatting.DARK_GRAY.toString(); // dark_gray
                case PINK -> ChatFormatting.LIGHT_PURPLE.toString(); // light_purple
                case LIME -> ChatFormatting.GREEN.toString(); // green
                case YELLOW -> ChatFormatting.YELLOW.toString(); // yellow
                case LIGHT_BLUE -> ChatFormatting.BLUE.toString(); // blue
                case MAGENTA -> ChatFormatting.AQUA.toString(); // aqua
                case ORANGE -> ChatFormatting.GOLD.toString(); // gold
                case BLACK -> ChatFormatting.BLACK.toString(); // black
                case BROWN -> ChatFormatting.DARK_RED.toString(); // dark_red
                case BLUE -> ChatFormatting.DARK_BLUE.toString(); // dark_blue
                case WHITE -> ChatFormatting.WHITE.toString(); // white
                default -> ChatFormatting.GRAY.toString();
            };
        } catch (Exception e) {
            Shroomhearth.LOG.error("Caught error: " + e);
            e.printStackTrace();
        }

        return code;
    }

    // return a random int between two ints
    public static int inRange(RandomSource r, int min, int max) {
        return r.nextInt((max - min) + 1) + min;
    }

    // grant an advancement
    public static void grantAdvancement(Player player, String namespace, String id, String criterion) {
        try {
            ServerPlayer serverPlayer = (ServerPlayer) player;
            AdvancementHolder advancement = player.level().getServer().getAdvancements().get(Identifier.fromNamespaceAndPath(namespace, id));
            if (advancement != null) {
                if (!serverPlayer.getAdvancements().getOrStartProgress(advancement).isDone()) {
                    serverPlayer.getAdvancements().award(advancement, criterion);
                }
            } else {
                Shroomhearth.LOG.warn("Advancement \"" + namespace + ":" + id + "\" not identified, may be missing datapack");
            }

        } catch (Exception e) {
            Shroomhearth.LOG.error("Caught error: " + e);
            e.printStackTrace();
        }
    }

    public static EquipmentSlot getEquipmentSlotFromHand(InteractionHand hand) {
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

}
