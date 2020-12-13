package com.epolixa.bityard;

import net.minecraft.util.DyeColor;

import java.util.Random;

public class BityardUtils {

    public static void log(String msg) {
        try {
            StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
            System.out.println("[Bityard][" + stackTraceElements[2].getClassName() + "][" + stackTraceElements[2].getMethodName() + "]: " + msg);
        } catch (Exception e) {
            System.out.println("[Bityard][BityardUtils][log]: caught error: " + e);
        }
    }

    public static String getDyeHex(DyeColor color) {
        log("enter: color = " + color.getName());
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
            log("caught error: " + e);
        }
        log("exit: hex = " + hex);
        return hex;
    }

    // return a random value between two values
    public static int inRange(Random r, int min, int max) {
        return r.nextInt((max - min) + 1) + min;
    }

}
