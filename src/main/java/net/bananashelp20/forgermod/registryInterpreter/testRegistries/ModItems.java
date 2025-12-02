package net.bananashelp20.forgermod.registryInterpreter.testRegistries;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.item.ModToolTiers;
import net.bananashelp20.forgermod.item.custom.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.Supplier;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ForgerMod.MOD_ID);

    public static DeferredItem<Item> createItem(String name) {
        return ITEMS.register(name, () -> new Item(new Item.Properties()));
    }

    public static DeferredItem<Item> createItemWithDescription(String name, String descriptionName) {
        return ITEMS.register(name,
                () -> new Item(new Item.Properties()) {
                    @Override
                    public void appendHoverText(ItemStack pStack, TooltipContext pContext, List<Component> pTooltipComponents, TooltipFlag pTooltipFlag) {
                        pTooltipComponents.add(Component.translatable(descriptionName));
                        super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
                    }
                });
    }

    public static DeferredItem<Item> createItemWithRarityAndDescription(String name, String descriptionName, Rarity rarity) {
        return ITEMS.register(name,
                () -> new Item(new Item.Properties().rarity(rarity)) {
                    @Override
                    public void appendHoverText(ItemStack pStack, TooltipContext pContext, List<Component> pTooltipComponents, TooltipFlag pTooltipFlag) {
                        pTooltipComponents.add(Component.translatable(descriptionName));
                        super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
                    }
                });
    }

    public static DeferredItem<SwordItem> createSwordItem(String name, Tier tier, int damage, float speed) {
        return ITEMS.register(
                name, () -> new SwordItem(
                    tier, new Item.Properties().attributes(SwordItem.createAttributes(tier, damage, -speed)))
                );
    }

    public static DeferredItem<SwordItem> createSwordItemWithDescription(String name, Tier tier, int damage, float speed, String descriptionName) {
        return ITEMS.register(name,
                () -> new SwordItem(tier, new Item.Properties()
                        .attributes(SwordItem.createAttributes(tier, damage, -speed))) //Tier/Damage/attackSpeed
                {
                    @Override
                    public void appendHoverText(ItemStack pStack, TooltipContext pContext, List<Component> pTooltipComponents, TooltipFlag pTooltipFlag) {
                        pTooltipComponents.add(Component.translatable(descriptionName));
                        super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
                    }
                });
    }

    public static DeferredItem<SwordItem> createSwordItemWithRarityAndDescription(String name, Tier tier, Rarity rarity, int damage, float speed, String descriptionName) {
        return ITEMS.register(name,
                () -> new SwordItem(tier, new Item.Properties()
                        .rarity(rarity)
                        .attributes(SwordItem.createAttributes(tier, damage, -speed))) //Tier/Damage/attackSpeed
                {
                    @Override
                    public void appendHoverText(ItemStack pStack, TooltipContext pContext, List<Component> pTooltipComponents, TooltipFlag pTooltipFlag) {
                        pTooltipComponents.add(Component.translatable(descriptionName));
                        super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
                    }
                });
    }

    public static DeferredItem<SwordItem> createSpecialSwordItem(String name, Supplier<SwordItemWithEffect> weapon) {
        return ITEMS.register(name, weapon);
    }

    private static final int DEFAULT_RUSTY_DAMAGE = -2;
    private static final float DEFAULT_RUSTY_SPEED = 3.2f;
    private static final int SCRAP_IRON_SWORD_DAMAGE = 3;
    private static final float SCRAP_IRON_SWORD_SPEED = 2.6f;
    private static final int REINFORCED_IRON_SWORD_DAMAGE = 3;
    private static final float REINFOCED_IRON_SWORD_SPEED = 2.8f;
    private static final int KNIFE_DAMASK_DAMAGE = 0;
    private static final float KNIFE_DAMASK_SPEED = 1f;
    private static final int SCRAP_SWORD_DAMAGE = 4;
    private static final float SCRAP_SWORD_SPEED = 2.2f;
    private static final int DAMASK_SWORD_DAMAGE = 3;
    private static final float DAMASK_SWORD_SPEED = 2.4f;
    private static final int STEEL_SWORD_DAMAGE = 3;
    private static final float STEEL_SWORD_SPEED = 2.4f;
    private static final int DEFAULT_CLAYMORE_DAMAGE = 3;
    private static final float DEFAULT_CLAYMORE_SPEED = 2.8f;

    private static final int DEFAULT_SPECIAL_DAMAGE = 4;
    private static final float DEFAULT_SPECIAL_SPEED = 2.8f;
    private static final int GEMSTONE_SPECIAL_DAMAGE = 5;
    private static final float GEMSTONE_SPECIAL_SPEED = 2.4f;

    //!GENERATE
    public static final DeferredItem<Item> INANISIUM_SHARD = createItem("inanisium_shard"); //!PRESERVE

    public static final DeferredItem<Item> ITEM1 = createItem("item1");
    public static final DeferredItem<Item> ITEM2 = createItemWithDescription("item2", "tooltips.forgermod.item2.tooltip");
    public static final DeferredItem<Item> ITEM3 = createItemWithRarityAndDescription("item3", "tooltips.forgermod.item3.tooltip", Rarity.UNCOMMON);
    public static final DeferredItem<SwordItem> SWORD1 = createSwordItem("sword1", ModToolTiers.STEEL, STEEL_SWORD_DAMAGE, STEEL_SWORD_SPEED);
    public static final DeferredItem<SwordItem> SWORD2 = createSwordItemWithRarityAndDescription("sword2", ModToolTiers.STEEL, Rarity.UNCOMMON, STEEL_SWORD_DAMAGE, STEEL_SWORD_SPEED, "tooltips.forgermod.sword2.tooltip");
    public static final DeferredItem<SwordItem> SWORD3 = createSwordItemWithRarityAndDescription("sword3", ModToolTiers.STEEL, Rarity.UNCOMMON, STEEL_SWORD_DAMAGE, STEEL_SWORD_SPEED, "tooltips.forgermod.sword3.tooltip");
    public static final DeferredItem<SwordItem> SWORD4 = createSpecialSwordItem("sword4", () -> new ElectriumWeapon("no_gemstone"));
    public static final DeferredItem<SwordItem> SWORD4_RUBY = createSpecialSwordItem("sword4_ruby", () -> new ElectriumWeapon("ruby"));
    public static final DeferredItem<SwordItem> SWORD4_AMBER = createSpecialSwordItem("sword4_amber", () -> new ElectriumWeapon("amber"));
    public static final DeferredItem<SwordItem> SWORD4_JADE = createSpecialSwordItem("sword4_jade", () -> new ElectriumWeapon("jade"));
    public static final DeferredItem<SwordItem> SWORD4_AMETHYST = createSpecialSwordItem("sword4_amethyst", () -> new ElectriumWeapon("amethyst"));

}