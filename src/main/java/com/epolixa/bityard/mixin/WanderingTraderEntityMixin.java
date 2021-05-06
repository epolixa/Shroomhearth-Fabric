package com.epolixa.bityard.mixin;

import com.epolixa.bityard.Bityard;
import com.epolixa.bityard.BityardUtils;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.item.*;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
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

import java.util.*;

@Mixin(WanderingTraderEntity.class)
public abstract class WanderingTraderEntityMixin extends MerchantEntity {

    /*private final int RARE_OFFERS = 1;
    private final int EXPENSIVE_OFFERS = 4;
    private final int FAIR_OFFERS = 8;
    private final int CHEAP_OFFERS = 16;*/

    private final int RARE_PRICE = 64;
    private final int EXPENSIVE_PRICE = 42;
    private final int FAIR_PRICE = 16;
    private final int CHEAP_PRICE = 4;

    /*private final int RARE_COUNT = 1;
    private final int EXPENSIVE_COUNT = 1;
    private final int FAIR_COUNT = 2;
    private final int CHEAP_COUNT = 4;*/

    private final Tag<Item> COMMON   = TagRegistry.item(new Identifier(Bityard.MOD_ID, "common"));
    private final Tag<Item> UNCOMMON   = TagRegistry.item(new Identifier(Bityard.MOD_ID, "uncommon")); // any uncategorized considered "uncommon"
    private final Tag<Item> RARE   = TagRegistry.item(new Identifier(Bityard.MOD_ID, "rare"));
    private final Tag<Item> EPIC   = TagRegistry.item(new Identifier(Bityard.MOD_ID, "epic"));
    private final Tag<Item> OMINOUS   = TagRegistry.item(new Identifier(Bityard.MOD_ID, "ominous"));


    private final LootTable TRADER = Bityard.getServer().getLootManager().getTable(new Identifier(Bityard.MOD_ID, "trader"));

    public WanderingTraderEntityMixin(EntityType<? extends WanderingTraderEntity> entityType, World world) {
        super(entityType, world);
        this.teleporting = true;
    }

    @Inject(method = "fillRecipes()V", at = @At("TAIL"))
    public void fillRecipes(CallbackInfo info) {
        try {
            BityardUtils.log("enter");

            // capture existing offers
            TradeOfferList tradeOfferList = this.getOffers();
            BityardUtils.log("trader started with offers: " + tradeOfferList.toString());

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

                Bityard.LOG.info("picked random item: " + item.getTranslationKey());

                // check if item should be added to offers
                if (item == Items.EMERALD || pickedItems.contains(item)) { i--; } // skip and try again
                else {
                    pickedItems.add(item); // add item to picked list so we don't pick it again
                    tradeOfferList.add(buildTradeOffer(r, lootStack)); // build an offer for the item
                }
            }

            // mix up tradeOffers order
            Collections.shuffle(tradeOfferList);

            Bityard.LOG.debug("set offers to: " + tradeOfferList.toString());

            BityardUtils.log("exit");
        } catch (Exception e) {
            BityardUtils.logError(e);
        }
    }


    // Prepares a TradeOffer for an Item
    private TradeOffer buildTradeOffer(Random r, ItemStack lootStack) {
        TradeOffer tradeOffer = null;

        try {
            Item item = lootStack.getItem();

            BityardUtils.log("enter: item = " + item.getTranslationKey());

            // Setup sell item
            ItemStack buyStack = new ItemStack(Items.EMERALD);
            int offers = lootStack.getCount();
            lootStack.setCount(1);

            // Adjust item count, sell price, offer amount depending on if item is cheap, fair, or expensive
            if (item.isIn(OMINOUS)) { buyStack.setCount(BityardUtils.inRange(r, 48, 64)); }
            else if (item.isIn(EPIC)) { buyStack.setCount(BityardUtils.inRange(r, 32, 48)); }
            else if (item.isIn(RARE)) { buyStack.setCount(BityardUtils.inRange(r, 16, 32)); }
            else if (item.isIn(UNCOMMON)) {
                buyStack.setCount(BityardUtils.inRange(r, 4, 16));
                lootStack.setCount(BityardUtils.inRange(r, 1, Math.min(item.getMaxCount(), 2)));
            }
            else {
                buyStack.setCount(BityardUtils.inRange(r, 1, 4));
                lootStack.setCount(BityardUtils.inRange(r, 1, Math.min(item.getMaxCount(), 4)));
            }

            // Setup offer
            tradeOffer = new TradeOffer(buyStack, lootStack, offers, BityardUtils.inRange(r, 3, 6), 0.2f);

            BityardUtils.log("exit");
        } catch (Exception e) {BityardUtils.logError(e);}

        return tradeOffer;
    }

    // setup an enchantment for an item
    private ItemStack addRandomEnchantment(Random r, ItemStack itemStack) {
        try {
            BityardUtils.log("enter: itemStack = " + itemStack.toString());

            if (itemStack.getItem() == Items.ENCHANTED_BOOK) {
                BityardUtils.log("found enchanted book...");
                Enchantment chosen = Registry.ENCHANTMENT.get(r.nextInt(Registry.ENCHANTMENT.getIds().size()));
                int lvl = 1 + (int) (Math.random() * ((chosen.getMaxLevel() - 1) + 1));
                ListTag listTag = EnchantedBookItem.getEnchantmentTag(itemStack);
                CompoundTag compoundTag = new CompoundTag();
                compoundTag.putString("id", String.valueOf(Registry.ENCHANTMENT.getId(chosen)));
                compoundTag.putShort("lvl", (short)lvl);
                listTag.add(compoundTag);
                itemStack.getOrCreateTag().put("StoredEnchantments", listTag);
            } else {
                List<Enchantment> possible = new ArrayList<Enchantment>();
                Iterator<Enchantment> enchantmentIterator = Registry.ENCHANTMENT.iterator();
                while (enchantmentIterator.hasNext()) {
                    Enchantment enchantment = enchantmentIterator.next();
                    if (enchantment.isAcceptableItem(itemStack)) {
                        possible.add(enchantment);
                    }
                }
                if (possible.size() >= 1) {
                    Enchantment chosen = possible.get(r.nextInt(possible.size()));
                    int lvl = 1 + (int) (Math.random() * ((chosen.getMaxLevel() - 1) + 1));
                    itemStack.addEnchantment(chosen, lvl);
                }
            }

            BityardUtils.log("exit");
        } catch (Exception e) {BityardUtils.logError(e);}

        return itemStack;
    }

    // setup potion effect on an item
    private ItemStack addRandomEffect(Random r, ItemStack itemStack) {
        try {
            BityardUtils.log("enter: item = " + itemStack.toString());

            List<Potion> potionBlacklist = new ArrayList<Potion>();
            potionBlacklist.add(Potions.AWKWARD);
            potionBlacklist.add(Potions.MUNDANE);
            potionBlacklist.add(Potions.THICK);
            potionBlacklist.add(Potions.WATER);
            potionBlacklist.add(Potions.EMPTY);
            Potion chosen;
            do {
                chosen = Registry.POTION.getRandom(r);
            } while (potionBlacklist.contains(chosen));
            PotionUtil.setPotion(itemStack, chosen);

            BityardUtils.log("exit");
        } catch (Exception e) {BityardUtils.logError(e);}

        return itemStack;
    }

}
