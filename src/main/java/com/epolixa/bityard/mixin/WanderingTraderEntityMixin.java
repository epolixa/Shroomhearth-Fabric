package com.epolixa.bityard.mixin;

import com.epolixa.bityard.Bityard;
import com.epolixa.bityard.BityardUtils;
import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.entity.projectile.FishingBobberEntity;
import net.minecraft.item.*;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextType;
import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.potion.Potions;
import net.minecraft.server.world.ServerWorld;
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

    private final int RARE_OFFERS = 2;
    private final int EXPENSIVE_OFFERS = 4;
    private final int FAIR_OFFERS = 8;
    private final int CHEAP_OFFERS = 16;

    private final int RARE_PRICE = 64;
    private final int EXPENSIVE_PRICE = 32;
    private final int FAIR_PRICE = 16;
    private final int CHEAP_PRICE = 8;

    private final int RARE_COUNT = 1;
    private final int EXPENSIVE_COUNT = 2;
    private final int FAIR_COUNT = 4;
    private final int CHEAP_COUNT = 8;

    private final Tag<Item> CHEAP   = TagRegistry.item(new Identifier(Bityard.MOD_ID, "cheap"));
    private final Tag<Item> EXPENSIVE   = TagRegistry.item(new Identifier(Bityard.MOD_ID, "expensive"));
    private final Tag<Item> RARE   = TagRegistry.item(new Identifier(Bityard.MOD_ID, "rare"));
    private final Tag<Item> ENCHANTABLE = TagRegistry.item(new Identifier(Bityard.MOD_ID, "enchantable"));
    private final Tag<Item> POTIONS     = TagRegistry.item(new Identifier(Bityard.MOD_ID, "potions"));

    private final LootTable REDSTONE = Bityard.getServer().getLootManager().getTable(new Identifier(Bityard.MOD_ID, "redstone/redstone"));
    private final LootTable TRANSPORTATION = Bityard.getServer().getLootManager().getTable(new Identifier(Bityard.MOD_ID, "transportation/transportation"));

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
            Random r = this.random;
            //List<Item> pickedItems = new ArrayList<Item>(); // items already added to offers

            // add at least one nice item
            /*Item expensiveItem = EXPENSIVE.values().get(r.nextInt(EXPENSIVE.values().size()));
            pickedItems.add(expensiveItem);
            tradeOfferList.add(buildTradeOffer(r, expensiveItem));*/

            // setup lootcontext
            LootContext.Builder builder = this.getLootContextBuilder(false, DamageSource.GENERIC);

            // add items from Redstone category
            tradeOfferList.add(buildTradeOffer(r, REDSTONE.generateLoot(builder.build(LootContextTypes.ENTITY)).get(0).getItem()));
            tradeOfferList.add(buildTradeOffer(r, TRANSPORTATION.generateLoot(builder.build(LootContextTypes.ENTITY)).get(0).getItem()));

            // add other random items
            /*for (int i = 0; i < NUM_OFFERS - 1; i++) {
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
            }*/

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
            ItemStack buyItem = new ItemStack(Items.EMERALD);
            int offers = 1;

            // Add applicable enchants
            /*if (item == Items.ENCHANTED_BOOK || item.isIn(ENCHANTABLE) && BityardUtils.inRange(r,0,1) == 1) {
                sellItem = addRandomEnchantment(r, sellItem);
            }*/

            // Add applicable effects
            /*if (item.isIn(POTIONS)) {
                sellItem = addRandomEffect(r, sellItem);
            }*/

            // Adjust item count, sell price, offer amount depending on if item is cheap, fair, or expensive
            if (item.isIn(RARE)) {
                sellItem.setCount(BityardUtils.inRange(r, 1, Math.min(item.getMaxCount(), this.RARE_COUNT)));
                buyItem.setCount(BityardUtils.inRange(r, this.EXPENSIVE_PRICE, this.RARE_PRICE));
                offers = BityardUtils.inRange(r, 1, this.RARE_OFFERS);
            } else if (item.isIn(EXPENSIVE)) {
                sellItem.setCount(BityardUtils.inRange(r, 1, Math.min(item.getMaxCount(), this.EXPENSIVE_COUNT)));
                buyItem.setCount(BityardUtils.inRange(r, this.FAIR_PRICE, this.EXPENSIVE_PRICE));
                offers = BityardUtils.inRange(r, 1, this.EXPENSIVE_OFFERS);
            } else if (item.isIn(CHEAP)) {
                sellItem.setCount(BityardUtils.inRange(r, 1, Math.min(item.getMaxCount(), this.CHEAP_COUNT)));
                buyItem.setCount(BityardUtils.inRange(r, 1, this.CHEAP_PRICE));
                offers = BityardUtils.inRange(r, this.FAIR_OFFERS, this.CHEAP_OFFERS);
            } else {
                sellItem.setCount(BityardUtils.inRange(r, 1, Math.min(item.getMaxCount(), this.FAIR_COUNT)));
                buyItem.setCount(BityardUtils.inRange(r, 1, this.FAIR_PRICE));
                offers = BityardUtils.inRange(r, this.EXPENSIVE_OFFERS, this.FAIR_OFFERS);
            }

            // Setup offer
            tradeOffer = new TradeOffer(buyItem, sellItem, offers, BityardUtils.inRange(r, 3, 6), 0.2f);

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
