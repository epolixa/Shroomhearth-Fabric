package com.epolixa.bityard;

import com.google.common.collect.Maps;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class Bityard implements ModInitializer {

    public static final String MOD_ID = "bityard_fabric";
    public static final Logger LOG = LogManager.getLogger();
    public static final Map<String, Integer> defaultConfig = Maps.newHashMap();
    private static MinecraftServer server = null;

    @Override
    public void onInitialize() {
        try {
            initializeDefaultConfig();

            ServerLifecycleEvents.SERVER_STARTED.register(this::onServerStarted);

            UseBlockCallback.EVENT.register(UseCauldronCallback::onUseCauldronCallback);

            Bityard.LOG.info("Initialized");
        } catch (Exception e) {Bityard.LOG.error(e);}
    }

    private void onServerStarted(MinecraftServer server) {
        try {
            this.server = server;
            BityardUtils.setMOTD(server);
            BityardUtils.setupScoreboardConfig(server);
        } catch (Exception e) {Bityard.LOG.error(e);}
    }


    public static MinecraftServer getServer() {
        MinecraftServer s = null;

        try {
            s = server;
        } catch (Exception e) {Bityard.LOG.error(e);}

        return s;
    }

    private void initializeDefaultConfig() {
        try {
            //BityardUtils.log("enter");

            // Enable/disable most logging (except errors)
            //defaultConfig.put("ENABLE_LOG", 1);

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

            //BityardUtils.log("exit");
        } catch (Exception e) {Bityard.LOG.error(e);}
    }
}