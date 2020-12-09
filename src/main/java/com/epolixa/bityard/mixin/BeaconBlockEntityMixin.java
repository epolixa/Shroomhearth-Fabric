package com.epolixa.bityard.mixin;

import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;
import java.util.List;

@Mixin(BeaconBlockEntity.class)
public abstract class BeaconBlockEntityMixin extends BlockEntity {

    public BeaconBlockEntityMixin() {
        super(BlockEntityType.BEACON);
    }

    // Inject to applyPlayerChanges to look for signs and send title to newly affected players
    // Inject at HEAD before status effects have been applied
    @Inject(method = "applyPlayerEffects()V", at = @At("HEAD"))
    public void applyPlayerEffects(CallbackInfo info) {
        try {
            System.out.println("[BeaconBlockEntityMixin][applyPlayerEffects] enter");

            StatusEffect primary = ((BeaconBlockEntityAccessor)this).getPrimary();
            int level = ((BeaconBlockEntityAccessor)this).getLevel();
            System.out.println("[BeaconBlockEntityMixin][applyPlayerEffects] captured primary as " + primary.getTranslationKey() + " " + level);

            if (!this.world.isClient && primary != null) { // check for same conditions as applying primary effect to a player
                System.out.println("[BeaconBlockEntityMixin][applyPlayerEffects] world is not client and primary is not null");

                double distance = (double)(level * 10 + 10);

                Box box = (new Box(this.pos)).expand(distance).stretch(0.0D, (double)this.world.getHeight(), 0.0D);
                List<PlayerEntity> playerList = this.world.getNonSpectatingEntities(PlayerEntity.class, box);
                Iterator playerListIterator = playerList.iterator();

                PlayerEntity player;
                while(playerListIterator.hasNext()) {
                    player = (PlayerEntity)playerListIterator.next();
                    System.out.println("[BeaconBlockEntityMixin][applyPlayerEffects] found player in range: " + player.getEntityName());

                    // check if player already has same status effect as primary
                    // only show message to newly affected players
                    if (!player.hasStatusEffect(primary)) {
                        // send a title message to the player
                        sendSubtitleToPlayer("{\"text\":\"Hello, epo!\"}", player);
                    }
                }
            }

            System.out.println("[BeaconBlockEntityMixin][applyPlayerEffects] exit");
        } catch (Exception e) {
            System.out.println("[BeaconBlockEntityMixin][applyPlayerEffects] caught error: " + e.toString());
        }
    }

    private void sendSubtitleToPlayer(String subtitle, PlayerEntity player) {
        System.out.println("[BeaconBlockEntityMixin][sendSubtitleToPlayer] enter - subtitle = " + subtitle);
        try {
            String entityName = player.getEntityName();
            String subtitleCommand = "/title " + entityName + " subtitle " + subtitle;
            String titleCommand = "/title " + entityName + " title {\"text\":\"\"}";
            System.out.println("[BeaconBlockEntityMixin][applyPlayerEffects] executing command: " + subtitleCommand);
            MinecraftServer server = player.getServer();
            server.getCommandManager().execute(server.getCommandSource(), subtitleCommand);
            server.getCommandManager().execute(server.getCommandSource(), titleCommand);
        } catch (Exception e) {
            System.out.println("[BeaconBlockEntityMixin][sendSubtitleToPlayer] caught error: " + e);
        }
        System.out.println("[BeaconBlockEntityMixin][sendSubtitleToPlayer] exit");
    }
}
