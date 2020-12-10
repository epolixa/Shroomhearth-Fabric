package com.epolixa.bityard.mixin;

import com.epolixa.bityard.BityardUtils;
import net.minecraft.network.packet.c2s.query.QueryRequestC2SPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerQueryNetworkHandler;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ServerQueryNetworkHandler.class)
public class ServerQueryNetworkHandlerMixin {

    @Shadow @Final private MinecraftServer server;

    @Inject(method = "onRequest", at = @At("HEAD"))
    public void onRequest(QueryRequestC2SPacket packet, CallbackInfo info) {
        try {
            System.out.println("[ServerQueryNetworkHandlerMixin][onRequest] enter");

            String motdMessage = "Hello, epo!";
            String motdColor = BityardUtils.getDyeHex(DyeColor.GREEN);
            String motdJSON = "[{\"text\":\"" + motdMessage + "\",\"color\":\"" + motdColor + "\"}]";

            server.getServerMetadata().setDescription(Text.Serializer.fromJson(motdJSON));

            System.out.println("[ServerQueryNetworkHandlerMixin][onRequest] exit");
        } catch (Exception e) {
            System.out.println("[ServerQueryNetworkHandlerMixin][onRequest] caught error: " + e);
        }
    }
}
