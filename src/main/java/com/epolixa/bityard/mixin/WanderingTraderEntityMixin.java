package com.epolixa.bityard.mixin;

import com.epolixa.bityard.Bityard;
import com.epolixa.bityard.BityardUtils;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Mixin(WanderingTraderEntity.class)
public abstract class WanderingTraderEntityMixin extends MerchantEntity {

    private final int NUM_OFFERS  = 9;
    private final int MIN_PRICE   = 1;
    private final int MAX_PRICE   = 8;
    private final int MIN_AMOUNT  = 1;
    private final int MAX_AMOUNT  = 8;
    private final int MIN_USES    = 4;
    private final int MAX_USES    = 16;
    private final int BONUS_PRICE = 32 - MAX_PRICE;

    private final Tag<Item> BLACKLIST   = TagRegistry.item(new Identifier(Bityard.MOD_ID, "wandering_trader/blacklist"));
    private final Tag<Item> ENCHANTABLE = TagRegistry.item(new Identifier(Bityard.MOD_ID, "wandering_trader/enchantable"));
    private final Tag<Item> EXPENSIVE   = TagRegistry.item(new Identifier(Bityard.MOD_ID, "wandering_trader/expensive"));
    private final Tag<Item> POTIONS     = TagRegistry.item(new Identifier(Bityard.MOD_ID, "wandering_trader/potions"));

    public WanderingTraderEntityMixin(EntityType<? extends WanderingTraderEntity> entityType, World world) {
        super(entityType, world);
        this.teleporting = true;
    }

    @Inject(method = "fillRecipes()V", at = @At("TAIL"))
    public void fillRecipes(CallbackInfo info) {
        try {
            BityardUtils.log("enter");

            // capture and clear existing offers
            TradeOfferList tradeOfferList = this.getOffers();
            BityardUtils.log("trader started with offers: " + tradeOfferList.toString());
            tradeOfferList.clear();

            // add trades for random items
            Random r = this.world.random;
            List<Item> pickedItems = new ArrayList<Item>(); // items already added to offers

            for (int i = 0; i < NUM_OFFERS; i++) {
                Item item = Registry.ITEM.getRandom(r); // next selected random item
                BityardUtils.log("picked random item: " + item.toString());

                // check if item should be added to offers
                if (pickedItems.contains(item)) { i--; } // skip and try again
                else {
                    pickedItems.add(item); // add item to picked list so we don't pick it again
                    tradeOfferList.add(buildTradeOffer(r, item)); // build an offer for the item
                }
            }

            BityardUtils.log("set offers to: " + tradeOfferList.toString());

            BityardUtils.log("exit");
        } catch (Exception e) {
            BityardUtils.log("caught error: " + e);
        }
    }


    // Prepares a TradeOffer for an Item
    private TradeOffer buildTradeOffer(Random r, Item item) {
        TradeOffer tradeOffer = null;
        try {
            BityardUtils.log("enter: item = " + item.toString());

            // Setup sell item
            ItemStack sellItem = new ItemStack(item, BityardUtils.inRange(r, this.MIN_AMOUNT, Math.min(item.getMaxCount(), this.MAX_AMOUNT)));
            /*if (item.toString().equalsIgnoreCase("minecraft:enchanted_book") || item.isIn(ENCHANTABLE) && BityardUtils.inRange(r,0,1) == 1)) {
                itemStack = addRandomEnchantment(itemStack);
            }
            if (this.potionMaterials.contains(material)) {
                sellItem = addRandomEffect(sellItem);
            }
            if (this.expensiveMaterials.contains(material)) {
                sellItem.setAmount(1);
            } else {
                sellItem.setAmount(inRange(this.MIN_AMOUNT, Math.min(sellItem.getMaxStackSize(), this.MAX_AMOUNT)));
            }*/

            // setup other offer vars
            ItemStack buyItem = new ItemStack(Registry.ITEM.get(new Identifier("minecraft", "emerald")), BityardUtils.inRange(r, MIN_PRICE, MAX_PRICE));

            tradeOffer = new TradeOffer(buyItem, sellItem, MAX_USES, BityardUtils.inRange(r, 3, 6), 0.2f);

            BityardUtils.log("exit");
        } catch (Exception e) {
            BityardUtils.log("caught error: " + e);
        }
        return tradeOffer;
    }

}
