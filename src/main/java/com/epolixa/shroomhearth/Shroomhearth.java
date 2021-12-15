package com.epolixa.shroomhearth;

import com.epolixa.shroomhearth.event.UseCauldronCallback;
import com.epolixa.shroomhearth.event.UseGlowstoneDustCallback;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class Shroomhearth implements DedicatedServerModInitializer {

    public static final String MOD_ID = "shroomhearth_fabric";
    public static Logger LOG;
    public static Config CONFIG;

    @Override
    public void onInitializeServer() {
        LOG = LogManager.getLogger();

        ServerLifecycleEvents.SERVER_STARTING.register(server -> {
            CONFIG = new Config();
        });

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            MOTD.setMOTD(server);
        });

        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            CONFIG.shutdown();
        });

        UseBlockCallback.EVENT.register(UseCauldronCallback::onUseCauldronCallback);
        UseBlockCallback.EVENT.register(UseGlowstoneDustCallback::onUseGlowstoneDustCallback);

        Shroomhearth.LOG.info("Initialized Shroomhearth");
    }
}