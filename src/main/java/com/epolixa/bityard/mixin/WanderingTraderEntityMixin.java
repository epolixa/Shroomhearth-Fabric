package com.epolixa.bityard.mixin;

import com.epolixa.bityard.Bityard;
import com.epolixa.bityard.BityardUtils;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.item.*;
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

import java.util.ArrayList;
import java.util.Iterator;
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

            // add at least one nice item
            Item expensiveItem = EXPENSIVE.values().get(r.nextInt(EXPENSIVE.values().size()));
            pickedItems.add(expensiveItem);
            tradeOfferList.add(buildTradeOffer(r, expensiveItem));

            // add other random items
            for (int i = 0; i < NUM_OFFERS - 1; i++) {
                Item item = Registry.ITEM.getRandom(r); // next selected random item
                //Item item = ENCHANTABLE.values().get(r.nextInt(ENCHANTABLE.values().size()));
                //Item item = Items.ENCHANTED_BOOK;
                BityardUtils.log("picked random item: " + item.toString());

                // check if item should be added to offers
                if (item.isIn(BLACKLIST) || pickedItems.contains(item)) { i--; } // skip and try again
                else {
                    pickedItems.add(item); // add item to picked list so we don't pick it again
                    tradeOfferList.add(buildTradeOffer(r, item)); // build an offer for the item
                }
            }

            BityardUtils.log("set offers to: " + tradeOfferList.toString());

            BityardUtils.log("exit");
        } catch (Exception e) {BityardUtils.logError(e);}
    }


    // Prepares a TradeOffer for an Item
    private TradeOffer buildTradeOffer(Random r, Item item) {
        TradeOffer tradeOffer = null;

        try {
            BityardUtils.log("enter: item = " + item.toString());

            // Setup sell item
            ItemStack sellItem = new ItemStack(item);

            // Add applicable enchants
            if (item == Items.ENCHANTED_BOOK || item.isIn(ENCHANTABLE) && BityardUtils.inRange(r,0,1) == 1) {
                sellItem = addRandomEnchantment(r, sellItem);
            }

            // Add applicable effects
            if (item.isIn(POTIONS)) {
                sellItem = addRandomEffect(r, sellItem);
            }

            // If item is rare, adjust sell price
            if (item.isIn(EXPENSIVE)) {
                sellItem.setCount(1);
            } else {
                sellItem.setCount(BityardUtils.inRange(r, this.MIN_AMOUNT, Math.min(item.getMaxCount(), this.MAX_AMOUNT)));
            }

            // Setup offer
            ItemStack buyItem = new ItemStack(Items.EMERALD, BityardUtils.inRange(r, MIN_PRICE, MAX_PRICE) + (item.isIn(EXPENSIVE) ? this.BONUS_PRICE : 0));
            int maxUses = BityardUtils.inRange(r, this.MIN_USES, item.isIn(EXPENSIVE) ? this.MAX_USES / 2 : this.MAX_USES);
            tradeOffer = new TradeOffer(buyItem, sellItem, maxUses, BityardUtils.inRange(r, 3, 6), 0.2f);

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
