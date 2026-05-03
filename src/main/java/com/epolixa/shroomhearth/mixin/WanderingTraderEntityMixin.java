package com.epolixa.shroomhearth.mixin;

import com.epolixa.shroomhearth.Shroomhearth;
import com.epolixa.shroomhearth.ShroomhearthUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.item.*;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(WanderingTrader.class)
public abstract class WanderingTraderEntityMixin extends AbstractVillager {

    private static final TagKey<Item> BLACKLIST = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Shroomhearth.MOD_ID, "trader_blacklist"));
    private static final TagKey<Item> POTIONS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Shroomhearth.MOD_ID, "potions"));
    private static final TagKey<Item> SPECIALS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Shroomhearth.MOD_ID, "trader_specials"));

    public WanderingTraderEntityMixin(EntityType<? extends WanderingTrader> entityType, Level world) {
        super(entityType, world);
    }

    @Inject(method = "updateTrades(Lnet/minecraft/server/level/ServerLevel;)V", at = @At("TAIL"))
    public void fillRecipes(CallbackInfo info) {
        try {
            RandomSource r = this.random;

            // capture and empty existing offers
            MerchantOffers tradeOfferList = this.getOffers();
            tradeOfferList.clear();

            // add trades for random items
            List<Item> pickedItems = new ArrayList<>(); // items already added to offers
            int offerCount = ShroomhearthUtils.inRange(r, 1, 8); // number of offers
            for (int i = 0; i < offerCount; i++) {
                Item rItem = BuiltInRegistries.ITEM.byId(r.nextInt(BuiltInRegistries.ITEM.size()));
                ItemStack rItemStack = rItem.getDefaultInstance();

                // check if item should be added to offers
                if (rItemStack.is(BLACKLIST) || rItemStack.is(SPECIALS) || pickedItems.contains(rItem) || !rItem.isEnabled(this.level().enabledFeatures())) {
                    i--; // skip and try again
                } else {
                    pickedItems.add(rItem); // add item to picked list so we don't pick it again
                    tradeOfferList.add(buildTradeOffer(r, rItemStack, offerCount)); // build an offer for the item
                }
            }

            // mix up tradeOffers order
            Collections.shuffle(tradeOfferList);

            // chance to add a special offer
            if (r.nextBoolean()) {
                tradeOfferList.add(buildSpecialTradeOffer(r));
            }
        } catch (Exception e) {
            Shroomhearth.LOG.error("Caught error: " + e);
            e.printStackTrace();
        }
    }


    // Prepares a TradeOffer for an Item
    private MerchantOffer buildTradeOffer(RandomSource r, ItemStack itemStack, int offerCount) {
        MerchantOffer tradeOffer = null;

        try {
            Item item = itemStack.getItem();
            itemStack.setCount(ShroomhearthUtils.inRange(r, 1, Math.min(item.getDefaultMaxStackSize(), 4)));

            // special case for potions
            if (itemStack.is(POTIONS)) {
                //PotionContentsComponent.setPotion(itemStack, Registries.POTION.get(r.nextInt(Registries.POTION.size())));
                Potion randomPotion = BuiltInRegistries.POTION.byId(r.nextInt(BuiltInRegistries.POTION.size()));
                PotionContents.createItemStack(item, BuiltInRegistries.POTION.wrapAsHolder(randomPotion));
            }

            ItemStack emeraldStack = new ItemStack(Items.EMERALD.asItem());
            emeraldStack.setCount(r.nextInt(3) + 1);

            // set uses based on variety
            int uses = ShroomhearthUtils.inRange(r, (32/offerCount), (128/offerCount));

            // small chance to be a buy offer instead of sell offer
            if (r.nextInt(3) == 0) { // buy
                ItemCost tradedItem = new ItemCost(itemStack.getItem(), itemStack.getCount());
                tradeOffer = new MerchantOffer(tradedItem, emeraldStack, uses, ShroomhearthUtils.inRange(r, 3, 6), 0.2f);
            } else { // sell
                ItemCost tradedItem = new ItemCost(emeraldStack.getItem(), emeraldStack.getCount());
                tradeOffer = new MerchantOffer(tradedItem, itemStack, uses, ShroomhearthUtils.inRange(r, 3, 6), 0.2f);
            }

        } catch (Exception e) {
            Shroomhearth.LOG.error("Caught error: " + e);
            e.printStackTrace();
        }

        return tradeOffer;
    }


    private MerchantOffer buildSpecialTradeOffer(RandomSource r) {
        MerchantOffer tradeOffer = null;

        try {
            // Pull a random item from trader_specials tag
            Item item = BuiltInRegistries.ITEM.getRandomElementOf(SPECIALS, r).get().value();
            ItemStack itemStack = item.getDefaultInstance();

            ItemStack emeraldStack = new ItemStack(Items.EMERALD.asItem());
            emeraldStack.setCount(ShroomhearthUtils.inRange(r, 24, 40));

            ItemCost tradedItem = new ItemCost(emeraldStack.getItem(), emeraldStack.getCount());
            tradeOffer = new MerchantOffer(tradedItem, itemStack, 1, ShroomhearthUtils.inRange(r, 3, 6), 0.2f);

        } catch (Exception e) {
            Shroomhearth.LOG.error("Caught error: " + e);
            e.printStackTrace();
        }

        return tradeOffer;
    }

    private void logOffers(MerchantOffers tradeOfferList) {
        Shroomhearth.LOG.info("Wandering Trader offers:");
        tradeOfferList.forEach(tradeOffer -> Shroomhearth.LOG.info("- " + tradeOffer.getMaxUses() + " uses of " + tradeOffer.getResult().getCount() + " " + tradeOffer.getResult().getItem().getDescriptionId() + " for " + tradeOffer.getBaseCostA().getCount() + " " + tradeOffer.getBaseCostA().getItem().getDescriptionId()));
    }
}
