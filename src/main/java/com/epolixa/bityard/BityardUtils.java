package com.epolixa.bityard;

import net.minecraft.util.DyeColor;

public class BityardUtils {

    public static void log(String msg) {
        try {
            StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
            System.out.println("[" + stackTraceElements[2].getClassName() + "] : " + msg);
        } catch (Exception e) {
            System.out.println("[BityardUtils.log] caught error: " + e);
        }
    }

    public static String getDyeHex(DyeColor color) {
        log("[getDyeHex] Enter: color = " + color.getName());
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
            log("[getDyeHex] caught error: " + e);
        }
        log("[getDyeHex] Exit: hex = " + hex);
        return hex;
    }

}
