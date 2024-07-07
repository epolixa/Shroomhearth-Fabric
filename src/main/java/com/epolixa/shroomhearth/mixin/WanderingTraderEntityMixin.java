package com.epolixa.shroomhearth.mixin;

import com.epolixa.shroomhearth.Shroomhearth;
import com.epolixa.shroomhearth.ShroomhearthUtils;
import net.minecraft.component.type.PotionContentsComponent;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.item.*;
import net.minecraft.potion.Potion;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.resource.featuretoggle.FeatureSet;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.random.Random;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.village.TradedItem;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(WanderingTraderEntity.class)
public abstract class WanderingTraderEntityMixin extends MerchantEntity {

    private static final TagKey<Item> BLACKLIST = TagKey.of(RegistryKeys.ITEM, Identifier.of(Shroomhearth.MOD_ID, "trader_blacklist"));
    private static final TagKey<Item> POTIONS = TagKey.of(RegistryKeys.ITEM, Identifier.of(Shroomhearth.MOD_ID, "potions"));
    private static final TagKey<Item> SPECIALS = TagKey.of(RegistryKeys.ITEM, Identifier.of(Shroomhearth.MOD_ID, "trader_specials"));

    public WanderingTraderEntityMixin(EntityType<? extends WanderingTraderEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "fillRecipes()V", at = @At("TAIL"))
    public void fillRecipes(CallbackInfo info) {
        try {
            Random r = this.random;

            // capture andd empty existing offers
            TradeOfferList tradeOfferList = this.getOffers();
            tradeOfferList.clear();

            // add trades for random items
            List<Item> pickedItems = new ArrayList<>(); // items already added to offers
            int offerCount = ShroomhearthUtils.inRange(r, 1, 8); // number of offers
            for (int i = 0; i < offerCount; i++) {
                Item rItem = Registries.ITEM.get(r.nextInt(Registries.ITEM.size()));
                ItemStack rItemStack = rItem.getDefaultStack();

                // check if item should be added to offers
                if (rItemStack.isIn(BLACKLIST) || rItemStack.isIn(SPECIALS) || pickedItems.contains(rItem) || !rItem.isEnabled(this.getWorld().getEnabledFeatures())) {
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
    private TradeOffer buildTradeOffer(Random r, ItemStack itemStack, int offerCount) {
        TradeOffer tradeOffer = null;

        try {
            Item item = itemStack.getItem();
            itemStack.setCount(ShroomhearthUtils.inRange(r, 1, Math.min(item.getMaxCount(), 4)));

            // special case for potions
            if (itemStack.isIn(POTIONS)) {
                //PotionContentsComponent.setPotion(itemStack, Registries.POTION.get(r.nextInt(Registries.POTION.size())));
                Potion randomPotion = Registries.POTION.get(r.nextInt(Registries.POTION.size()));
                PotionContentsComponent.createStack(item, Registries.POTION.getEntry(randomPotion));
            }

            ItemStack emeraldStack = new ItemStack(Items.EMERALD.asItem());
            emeraldStack.setCount(r.nextInt(3) + 1);

            // set uses based on variety
            int uses = ShroomhearthUtils.inRange(r, (32/offerCount), (128/offerCount));

            // small chance to be a buy offer instead of sell offer
            if (r.nextInt(3) == 0) { // buy
                TradedItem tradedItem = new TradedItem(itemStack.getItem(), itemStack.getCount());
                tradeOffer = new TradeOffer(tradedItem, emeraldStack, uses, ShroomhearthUtils.inRange(r, 3, 6), 0.2f);
            } else { // sell
                TradedItem tradedItem = new TradedItem(emeraldStack.getItem(), emeraldStack.getCount());
                tradeOffer = new TradeOffer(tradedItem, itemStack, uses, ShroomhearthUtils.inRange(r, 3, 6), 0.2f);
            }

        } catch (Exception e) {
            Shroomhearth.LOG.error("Caught error: " + e);
            e.printStackTrace();
        }

        return tradeOffer;
    }


    private TradeOffer buildSpecialTradeOffer(Random r) {
        TradeOffer tradeOffer = null;

        try {
            // Pull a random item from trader_specials tag
            Item item = Registries.ITEM.getEntryList(SPECIALS).flatMap(list -> list.getRandom(r)).get().value();
            ItemStack itemStack = item.getDefaultStack();

            ItemStack emeraldStack = new ItemStack(Items.EMERALD.asItem());
            emeraldStack.setCount(ShroomhearthUtils.inRange(r, 24, 40));

            TradedItem tradedItem = new TradedItem(emeraldStack.getItem(), emeraldStack.getCount());
            tradeOffer = new TradeOffer(tradedItem, itemStack, 1, ShroomhearthUtils.inRange(r, 3, 6), 0.2f);

        } catch (Exception e) {
            Shroomhearth.LOG.error("Caught error: " + e);
            e.printStackTrace();
        }

        return tradeOffer;
    }

    private void logOffers(TradeOfferList tradeOfferList) {
        Shroomhearth.LOG.info("Wandering Trader offers:");
        tradeOfferList.forEach(tradeOffer -> Shroomhearth.LOG.info("- " + tradeOffer.getMaxUses() + " uses of " + tradeOffer.getSellItem().getCount() + " " + tradeOffer.getSellItem().getTranslationKey() + " for " + tradeOffer.getOriginalFirstBuyItem().getCount() + " " + tradeOffer.getOriginalFirstBuyItem().getTranslationKey()));
    }
}
