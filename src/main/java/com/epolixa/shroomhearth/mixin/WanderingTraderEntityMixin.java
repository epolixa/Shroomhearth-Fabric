package com.epolixa.shroomhearth.mixin;

import com.epolixa.shroomhearth.Shroomhearth;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.tags.InstrumentTags;
import net.minecraft.tags.PotionTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.component.InstrumentComponent;
import net.minecraft.world.item.component.OminousBottleAmplifier;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Mixin(WanderingTrader.class)
public abstract class WanderingTraderEntityMixin extends AbstractVillager {

    private static final TagKey<Item> BLACKLIST = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Shroomhearth.MOD_ID, "wandering_trader_blacklist"));
    private static final TagKey<Item> SELL_ONLY = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Shroomhearth.MOD_ID, "wandering_trader_sell_only"));
    private static final TagKey<Item> UNCOMMON = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Shroomhearth.MOD_ID, "wandering_trader_uncommon"));
    private static final TagKey<Item> RARE = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Shroomhearth.MOD_ID, "wandering_trader_rare"));
    private static final TagKey<Item> EPIC = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Shroomhearth.MOD_ID, "wandering_trader_epic"));
    private static final TagKey<Item> POTION_ITEMS = TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(Shroomhearth.MOD_ID, "potions"));


    public WanderingTraderEntityMixin(EntityType<? extends WanderingTrader> entityType, Level world) {
        super(entityType, world);
    }


    @Inject(method = "updateTrades(Lnet/minecraft/server/level/ServerLevel;)V", at = @At("TAIL"))
    public void fillRecipes(CallbackInfo info) {
        try {
            // Capture and clear existing offers
            MerchantOffers tradeOfferList = this.getOffers();
            int offerCount = tradeOfferList.size(); // Offer count should match vanilla - set from existing offer list
            tradeOfferList.clear();

            // Keep track of selected items so that the same item isn't added to offers twice
            List<Item> selectedItems = new ArrayList<>();

            // Begin generating offers
            for (int i = 0; i < offerCount; i++) {
                // Pick a random item from the entire item registry
                Item rItem = BuiltInRegistries.ITEM.getRandom(this.random).get().value();
                ItemStack rItemStack = rItem.getDefaultInstance();

                // Check if this item can be added to offers
                if (rItemStack.is(BLACKLIST) || selectedItems.contains(rItem) || !rItem.isEnabled(this.level().enabledFeatures())) {
                    i--; // Skip and try again
                } else {
                    // Add item to picked list so we don't pick it again
                    selectedItems.add(rItem);

                    // Build an offer for the item
                    tradeOfferList.add(buildTradeOffer(rItem, rItemStack));
                }
            }

            // Shuffle tradeOffers order
            Collections.shuffle(tradeOfferList);

            logOffers(tradeOfferList);

        } catch (Exception e) {
            Shroomhearth.LOG.error("Caught error: ", e);
            e.printStackTrace();
        }
    }


    /** Prepares a TradeOffer for a given Item */
    private MerchantOffer buildTradeOffer(Item item, ItemStack itemStack) {
        MerchantOffer tradeOffer = null;

        try {
            int playerCountMultiplier = Math.max(1, this.level().players().size()); // Get the number of players in the world
            Rarity rarity = getRarity(itemStack);
            ItemStack emeraldStack = rarity == Rarity.EPIC ? new ItemStack(Items.EMERALD_BLOCK.asItem()) : new ItemStack(Items.EMERALD.asItem());
            boolean isBuying = false;
            int itemCount = 1;
            int emeraldCount = 1;
            int maxUses = 1;
            int xp = 6;

            /* Set offer parameters based on rarity
            Common Offers:
            - 1 to 4 items (Selling) or 1 to 8 items (Buying)
            - 1 to 8 emeralds (Selling) or 1 to 4 emeralds (Buying)
            - 4 to 8 uses x player count
            - 25% chance to be Buying

            Uncommon Offers:
            - 1 item
            - 16 to 32 emeralds
            - 1 use x player count

            Rare Offers:
            - 1 item
            - 32 to 64 emeralds
            - 1 use

            Epic Offers:
            - 1 item
            - 8 to 16 emerald blocks
            - 1 use
            */
            switch (rarity) {
                case UNCOMMON:
                    emeraldCount = this.random.nextIntBetweenInclusive(16, 32);
                    maxUses = playerCountMultiplier;
                    xp = this.random.nextIntBetweenInclusive(6, 12);
                    break;
                case RARE:
                    emeraldCount = this.random.nextIntBetweenInclusive(32, 64);
                    xp = this.random.nextIntBetweenInclusive(12, 24);
                    break;
                case EPIC:
                    emeraldCount = this.random.nextIntBetweenInclusive(8, 16);
                    xp = this.random.nextIntBetweenInclusive(24, 48);
                    break;
                default: // COMMON
                    isBuying = !itemStack.is(SELL_ONLY) && this.random.nextInt(4) == 0; // 25% chance for the offer to be Buying instead of Selling
                    itemCount = this.random.nextIntBetweenInclusive(1, isBuying ? 8 : 4);
                    emeraldCount = this.random.nextIntBetweenInclusive(1, isBuying ? 4 : 8);
                    maxUses = this.random.nextIntBetweenInclusive(4, 8) * playerCountMultiplier;
                    xp = this.random.nextIntBetweenInclusive(3, 6);
                    break;
            }

            // Potions - random tradeable potion
            if (itemStack.is(POTION_ITEMS)) {
                Potion randomPotion = BuiltInRegistries.POTION.getRandom(this.random).filter(holder -> holder.is(PotionTags.TRADEABLE)).map(holder -> holder.value()).orElse(null);
                if (randomPotion != null) {
                    itemStack = PotionContents.createItemStack(item, BuiltInRegistries.POTION.wrapAsHolder(randomPotion));
                }
            }

            // Enchanted Book - random enchant
            if (itemStack.is(Items.ENCHANTED_BOOK)) {
                itemStack = EnchantmentHelper.enchantItem(
                        this.random,
                        Items.BOOK.getDefaultInstance(),
                        this.random.nextIntBetweenInclusive(1, 30),
                        this.level()
                            .registryAccess()
                            .lookupOrThrow(Registries.ENCHANTMENT)
                            .getOrThrow(EnchantmentTags.ON_RANDOM_LOOT)
                            .stream()
                );
            }

            // Goat Horn - random instrument
            if (itemStack.is(Items.GOAT_HORN)) {
                var instrumentLookup = this.level().registryAccess().lookupOrThrow(Registries.INSTRUMENT);
                var randomHorn = instrumentLookup
                    .get(InstrumentTags.GOAT_HORNS)
                    .flatMap(named -> named.getRandomElement(this.random));

                if (randomHorn.isPresent()) {
                    itemStack.set(DataComponents.INSTRUMENT, new InstrumentComponent(randomHorn.get()));
                }
            }

            // Ominous Bottle - random level
            if (itemStack.is(Items.OMINOUS_BOTTLE)) {
                // Stored as amplifier 0-4, which corresponds to Bad Omen levels I-V.
                int badOmenAmplifier = this.random.nextIntBetweenInclusive(0, 4);
                itemStack.set(DataComponents.OMINOUS_BOTTLE_AMPLIFIER, new OminousBottleAmplifier(badOmenAmplifier));
            }

            // Normalize item counts against max stack sizes
            itemStack.setCount(Math.min(itemStack.getMaxStackSize(), itemCount));
            emeraldStack.setCount(Math.min(emeraldStack.getMaxStackSize(), emeraldCount));

            // Initialize trade offer
            if (isBuying) {
                ItemCost tradedItem = new ItemCost(itemStack.getItem(), itemStack.getCount());
                tradeOffer = new MerchantOffer(tradedItem, emeraldStack, maxUses, xp, 0.0f);
            } else { // Selling
                ItemCost tradedItem = new ItemCost(emeraldStack.getItem(), emeraldStack.getCount());
                tradeOffer = new MerchantOffer(tradedItem, itemStack, maxUses, xp, 0.0f);
            }

        } catch (Exception e) {
            Shroomhearth.LOG.error("Caught error: " + e);
            e.printStackTrace();
        }

        return tradeOffer;
    }


    /** Identifies the rarity of a given item stack based on rarity overrides */
    private Rarity getRarity(ItemStack itemStack) {
        Rarity rarity = itemStack.getRarity();

        try {
            if (itemStack.is(UNCOMMON)) {
                rarity = Rarity.UNCOMMON;
            } else if (itemStack.is(RARE)) {
                rarity = Rarity.RARE;
            } else if (itemStack.is(EPIC)) {
                rarity = Rarity.EPIC;
            }
        } catch (Exception e) {
            Shroomhearth.LOG.error("Caught error: " + e);
            e.printStackTrace();
        }

        return rarity;
    }


    private void logOffers(MerchantOffers tradeOfferList) {
        Shroomhearth.LOG.debug("Wandering Trader offers:");
        tradeOfferList.forEach(tradeOffer -> Shroomhearth.LOG.debug("- " + tradeOffer.getMaxUses() + " uses of " + tradeOffer.getBaseCostA().getCount() + " " + tradeOffer.getBaseCostA().getItem().getDescriptionId() + " for " + tradeOffer.getResult().getCount() + " " + tradeOffer.getResult().getItem().getDescriptionId()));
    }

}
