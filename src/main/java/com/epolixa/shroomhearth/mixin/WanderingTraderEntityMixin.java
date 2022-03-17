package com.epolixa.shroomhearth.mixin;

import com.epolixa.shroomhearth.Shroomhearth;
import com.epolixa.shroomhearth.ShroomhearthUtils;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.item.*;
import net.minecraft.potion.PotionUtil;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.village.TradeOffer;
import net.minecraft.village.TradeOfferList;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(WanderingTraderEntity.class)
public abstract class WanderingTraderEntityMixin extends MerchantEntity {

    private static final TagKey<Item> BLACKLIST = TagKey.of(Registry.ITEM_KEY, new Identifier(Shroomhearth.MOD_ID, "trader_blacklist"));
    private static final TagKey<Item> POTIONS = TagKey.of(Registry.ITEM_KEY, new Identifier(Shroomhearth.MOD_ID, "potions"));

    public WanderingTraderEntityMixin(EntityType<? extends WanderingTraderEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "fillRecipes()V", at = @At("TAIL"))
    public void fillRecipes(CallbackInfo info) {
        try {
            // capture existing offers
            TradeOfferList tradeOfferList = this.getOffers();

            // add trades for random items
            List<Item> pickedItems = new ArrayList<>(); // items already added to offers
            tradeOfferList.forEach(tradeOffer -> pickedItems.add(tradeOffer.getSellItem().getItem())); // include default wandering trader stuff in rolls

            Random r = this.random;

            // add random items
            int num_offers = tradeOfferList.size();
            for (int i = 0; i < num_offers; i++) {
                Item rItem = Registry.ITEM.get(r.nextInt(Registry.ITEM.size()));
                ItemStack rItemStack = rItem.getDefaultStack();

                // check if item should be added to offers
                if (rItemStack.isIn(BLACKLIST) || pickedItems.contains(rItem)) {
                    i--; // skip and try again
                } else {
                    pickedItems.add(rItem); // add item to picked list so we don't pick it again
                    tradeOfferList.add(buildTradeOffer(r, rItemStack)); // build an offer for the item
                }
            }

            // mix up tradeOffers order
            Collections.shuffle(tradeOfferList);

            logOffers(tradeOfferList);
        } catch (Exception e) {
            Shroomhearth.LOG.error("Caught error: " + e);
            e.printStackTrace();
        }
    }


    // Prepares a TradeOffer for an Item
    private TradeOffer buildTradeOffer(Random r, ItemStack itemStack) {
        TradeOffer tradeOffer = null;

        try {
            Item item = itemStack.getItem();
            itemStack.setCount(ShroomhearthUtils.inRange(r, 1, Math.min(item.getMaxCount(), 4)));

            // special case for potions
            if (itemStack.isIn(POTIONS)) {
                PotionUtil.setPotion(itemStack, Registry.POTION.get(r.nextInt(Registry.POTION.size())));
            }

            ItemStack emeraldStack = new ItemStack(Items.EMERALD.asItem());
            emeraldStack.setCount(r.nextInt(3) + 1);

            int uses = ShroomhearthUtils.inRange(r, 4, 12);

            // small chance to be a buy offer instead of sell offer
            if (r.nextInt(3) == 0) { // buy
                tradeOffer = new TradeOffer(itemStack, emeraldStack, uses, ShroomhearthUtils.inRange(r, 3, 6), 0.2f);
            } else { // sell
                tradeOffer = new TradeOffer(emeraldStack, itemStack, uses, ShroomhearthUtils.inRange(r, 3, 6), 0.2f);
            }

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
