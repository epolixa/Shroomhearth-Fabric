package com.epolixa.bityard.mixin;

import com.epolixa.bityard.BityardUtils;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.registry.Registry;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Random;

@Mixin(WanderingTraderEntity.class)
public abstract class WanderingTraderEntityMixin extends MerchantEntity {

    public WanderingTraderEntityMixin(EntityType<? extends WanderingTraderEntity> entityType, World world) {
        super(entityType, world);
        this.teleporting = true;
    }

    @Inject(method = "fillRecipes()V", at = @At("TAIL"))
    public void fillRecipes(CallbackInfo info) {
        try {
            BityardUtils.log("enter");

            // Overwrite offers
            TradeOfferList tradeOfferList = this.getOffers();
            BityardUtils.log("trader started with offers: " + tradeOfferList.toString());
            tradeOfferList.clear();

            // Try adding a random item as a recipe from the registry
            Random r = this.world.random;
            Item item = Registry.ITEM.getRandom(r);
            BityardUtils.log("picked random item: " + item.toString());

            ItemStack itemStack = new ItemStack(item);
            TradeOffer tradeOffer = new TradeOffer(itemStack, itemStack, itemStack, 64, 64, 64, 64, 64);
            tradeOfferList.add(tradeOffer);

            BityardUtils.log("set offers to: " + tradeOfferList.toString());

            BityardUtils.log("exit");
        } catch (Exception e) {
            BityardUtils.log("caught error: " + e);
        }
    }

}
