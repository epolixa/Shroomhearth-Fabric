package com.epolixa.bityard.mixin;

import com.epolixa.bityard.Bityard;
import com.epolixa.bityard.BityardUtils;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.item.*;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.tag.Tag;
import net.minecraft.util.Identifier;
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

    //private final Tag<Item> COMMON   = TagRegistry.item(new Identifier(Bityard.MOD_ID, "common"));
    private final Tag<Item> UNCOMMON   = TagRegistry.item(new Identifier(Bityard.MOD_ID, "uncommon")); // any uncategorized considered "common"
    private final Tag<Item> RARE   = TagRegistry.item(new Identifier(Bityard.MOD_ID, "rare"));
    private final Tag<Item> EPIC   = TagRegistry.item(new Identifier(Bityard.MOD_ID, "epic"));
    private final Tag<Item> OMINOUS   = TagRegistry.item(new Identifier(Bityard.MOD_ID, "ominous"));

    private final LootTable TRADER = this.getServer().getLootManager().getTable(new Identifier(Bityard.MOD_ID, "trader"));

    public WanderingTraderEntityMixin(EntityType<? extends WanderingTraderEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "fillRecipes()V", at = @At("TAIL"))
    public void fillRecipes(CallbackInfo info) {
        try {
            // capture existing offers
            TradeOfferList tradeOfferList = this.getOffers();

            // add trades for random items
            List<Item> pickedItems = new ArrayList<Item>(); // items already added to offers
            tradeOfferList.forEach(tradeOffer -> {pickedItems.add(tradeOffer.getSellItem().getItem());}); // include default wandering trader stuff in rolls

            // setup lootcontext
            LootContext.Builder builder = this.getLootContextBuilder(false, DamageSource.GENERIC);

            Random r = this.random;

            // add random items
            int num_offers = tradeOfferList.size();
            for (int i = 0; i < num_offers; i++) {
                List<ItemStack> loot = TRADER.generateLoot(builder.build(LootContextTypes.ENTITY));
                ItemStack lootStack = loot.get(BityardUtils.inRange(r, 0, loot.size() - 1)); // next selected random loot
                Item item = lootStack.getItem();

                // check if item should be added to offers
                if (item == Items.EMERALD || pickedItems.contains(item)) {
                    i--; // skip and try again
                } else {
                    pickedItems.add(item); // add item to picked list so we don't pick it again
                    tradeOfferList.add(buildTradeOffer(r, lootStack)); // build an offer for the item
                }
            }

            // mix up tradeOffers order
            Collections.shuffle(tradeOfferList);

            logOffers(tradeOfferList);
        } catch (Exception e) {
            Bityard.LOG.error("Caught error: " + e);
            e.printStackTrace();
        }
    }


    // Prepares a TradeOffer for an Item
    private TradeOffer buildTradeOffer(Random r, ItemStack lootStack) {
        TradeOffer tradeOffer = null;

        try {
            Item item = lootStack.getItem();

            // Setup sell item
            ItemStack buyStack = new ItemStack(Items.EMERALD);
            int uses = lootStack.getCount();
            lootStack.setCount(1);

            // Adjust item count, sell price, offer amount depending on if item is cheap, fair, or expensive
            if (OMINOUS.contains(item)) { buyStack.setCount(BityardUtils.inRange(r, Bityard.CONFIG.getOminousMinPrice(), Bityard.CONFIG.getOminousMaxPrice())); }
            else if (EPIC.contains(item)) { buyStack.setCount(BityardUtils.inRange(r, Bityard.CONFIG.getEpicMinPrice(), Bityard.CONFIG.getEpicMaxPrice())); }
            else if (RARE.contains(item)) {
                buyStack.setCount(BityardUtils.inRange(r, Bityard.CONFIG.getRareMinPrice(), Bityard.CONFIG.getRareMaxPrice()));
                uses += Bityard.CONFIG.getRareUsesBonus();
            }
            else if (UNCOMMON.contains(item)) {
                buyStack.setCount(BityardUtils.inRange(r, Bityard.CONFIG.getUncommonMinPrice(), Bityard.CONFIG.getUncommonMaxPrice()));
                lootStack.setCount(BityardUtils.inRange(r, 1, Math.min(item.getMaxCount(), 2)));
                uses += Bityard.CONFIG.getUncommonUsesBonus();
            }
            else {
                buyStack.setCount(BityardUtils.inRange(r, Bityard.CONFIG.getCommonMinPrice(), Bityard.CONFIG.getCommonMaxPrice()));
                lootStack.setCount(BityardUtils.inRange(r, 1, Math.min(item.getMaxCount(), 4)));
                uses += Bityard.CONFIG.getCommonUsesBonus();
            }

            // Setup offer
            tradeOffer = new TradeOffer(buyStack, lootStack, uses, BityardUtils.inRange(r, 3, 6), 0.2f);
        } catch (Exception e) {
            Bityard.LOG.error("Caught error: " + e);
            e.printStackTrace();
        }

        return tradeOffer;
    }

    private void logOffers(TradeOfferList tradeOfferList) {
        Bityard.LOG.info("Wandering Trader offers:");
        tradeOfferList.forEach(tradeOffer -> {
            Bityard.LOG.info("- " + tradeOffer.getMaxUses() + " uses of " + tradeOffer.getSellItem().getCount() + " " + tradeOffer.getSellItem().getTranslationKey() + " for " + tradeOffer.getOriginalFirstBuyItem().getCount() + " " + tradeOffer.getOriginalFirstBuyItem().getTranslationKey());
        });
    }
}
