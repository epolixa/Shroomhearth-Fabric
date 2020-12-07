package com.epolixa.bityard;

import net.fabricmc.api.ModInitializer;

public class Bityard implements ModInitializer {

    public static final String MOD_ID = "bityard";

    @Override
    public void onInitialize() {
        System.out.println("[Bityard] initializing");

        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        System.out.println("[Bityard] initialized");
    }
}