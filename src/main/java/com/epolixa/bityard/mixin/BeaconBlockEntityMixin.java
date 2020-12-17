package com.epolixa.bityard.mixin;

import com.epolixa.bityard.BityardUtils;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.DyeColor;
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
    @Inject(method = "applyPlayerEffects()V", at = @At("HEAD"))
    public void applyPlayerEffects(CallbackInfo info) {
        try {
            BityardUtils.log("enter");

            StatusEffect primary = ((BeaconBlockEntityAccessor)this).getPrimary();
            int level = ((BeaconBlockEntityAccessor)this).getLevel();
            BityardUtils.log("captured primary as " + primary.getTranslationKey() + " " + level);

            if (!this.world.isClient && primary != null) { // check for same conditions as applying primary effect to a player
                BityardUtils.log("world is not client and primary is not null");

                double distance = (double)(level * 10 + 10);

                Box box = (new Box(this.pos)).expand(distance).stretch(0.0D, (double)this.world.getHeight(), 0.0D);
                List<PlayerEntity> playerList = this.world.getNonSpectatingEntities(PlayerEntity.class, box);
                Iterator playerListIterator = playerList.iterator();

                PlayerEntity player;
                while(playerListIterator.hasNext()) {
                    player = (PlayerEntity)playerListIterator.next();
                    BityardUtils.log("found player in range: " + player.getEntityName());

                    // check if player already has same status effect as primary
                    // only show message to newly affected players
                    if (!player.hasStatusEffect(primary)) {
                        BityardUtils.log("player is newly affected");

                        // check for a sign block directly on top of beacon block
                        BlockEntity aboveBeacon = this.world.getBlockEntity(pos.add(0,1,0));
                        if (aboveBeacon instanceof SignBlockEntity) {
                            BityardUtils.log("sign block exists above beacon");

                            SignBlockEntity sign = (SignBlockEntity)aboveBeacon;
                            Text[] signText = ((SignBlockEntityAccessor)sign).getText(); // getTextOnRow is CLIENT-only
                            DyeColor color = sign.getTextColor();
                            StringBuilder sb = new StringBuilder();
                            for (int i = 0; i < signText.length; i++) // parse sign rows
                            {
                                Text rowText = signText[i];
                                String row = rowText.getString();
                                BityardUtils.log("parsing sign row " + (i + 1) + ": " + row);
                                if (row.length() > 0) {
                                    if (sb.toString().length() > 0) {
                                        sb.append(" ");
                                    }
                                    sb.append(row);
                                }
                            }
                            BityardUtils.log("sign text interpreted as: " + sb.toString());

                            // send a title message to the player
                            sendSubtitleToPlayer("{\"text\":\"" + sb.toString() + "\",\"color\":\"" + BityardUtils.getDyeHex(color) + "\"}", player);
                        }
                    }
                }
            }

            BityardUtils.log("exit");
        } catch (Exception e) {BityardUtils.logError(e);}
    }

    private void sendSubtitleToPlayer(String subtitle, PlayerEntity player) {
        try {
            BityardUtils.log("enter: subtitle = " + subtitle);

            String entityName = player.getEntityName();
            String subtitleCommand = "/title " + entityName + " subtitle " + subtitle;
            String titleCommand = "/title " + entityName + " title {\"text\":\"\"}";
            BityardUtils.log("executing command: " + subtitleCommand);
            MinecraftServer server = player.getServer();
            server.getCommandManager().execute(server.getCommandSource(), subtitleCommand);
            server.getCommandManager().execute(server.getCommandSource(), titleCommand);

            BityardUtils.log("exit");
        } catch (Exception e) {BityardUtils.logError(e);}
    }

}
