package com.epolixa.shroomhearth;

import com.epolixa.shroomhearth.event.*;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
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
        UseEntityCallback.EVENT.register(UseEchoShardCallback::onUseMobEchoShardCallback);
        UseEntityCallback.EVENT.register(ItemFrameInteractionCallback::onUseItemFrameCallback);
        AttackEntityCallback.EVENT.register(ItemFrameInteractionCallback::onAttackItemFrameCallback);
        AttackEntityCallback.EVENT.register(AttackEntityOrientationToolCallback::onAttackEntityOrientationToolCallback);
        UseBlockCallback.EVENT.register(UseEchoShardCallback::onUseSculkShriekerEchoShardCallback);
        UseBlockCallback.EVENT.register(UseGlowstoneDustCallback::onUseGlowstoneDustCallback);
        UseBlockCallback.EVENT.register(UseGlowstoneDustCallback::onUseScrapingToolOnGlowstoneDustCallback);
        UseBlockCallback.EVENT.register(UseBlockOrientationToolCallback::onUseBlockOrientationToolCallback);

        Shroomhearth.LOG.info("Initialized Shroomhearth");
    }
}