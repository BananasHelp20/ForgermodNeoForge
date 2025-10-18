package net.bananashelp20.forgermod.item;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.item.custom.OvergrownWeapon;
import net.bananashelp20.forgermod.item.custom.SwordItemWithEffect;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

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
                    public void appendHoverText(ItemStack pStack, Item.TooltipContext pContext, List<Component> pTooltipComponents, TooltipFlag pTooltipFlag) {
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
                    public void appendHoverText(ItemStack pStack, Item.TooltipContext pContext, List<Component> pTooltipComponents, TooltipFlag pTooltipFlag) {
                        pTooltipComponents.add(Component.translatable(descriptionName));
                        super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
                    }
                });
    }

    public static DeferredItem<SwordItem> createSwordItemWithRarityAndDescriptionAndEffect(String name, Tier tier, Rarity rarity, int damage, float speed, String descriptionName, Holder<MobEffect> effect, int durationInTicks, int amplifier) {
        return ITEMS.register(name,
                () -> new OvergrownWeapon(tier, effect, durationInTicks, amplifier, new Item.Properties()
                        .rarity(rarity)
                        .attributes(SwordItem.createAttributes(tier, damage, -speed))) //Tier/Damage/attackSpeed
                {
                    @Override
                    public void appendHoverText(ItemStack pStack, Item.TooltipContext pContext, List<Component> pTooltipComponents, TooltipFlag pTooltipFlag) {
                        pTooltipComponents.add(Component.translatable(descriptionName));
                        super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
                    }
                });
    }

    public static DeferredItem<SwordItem> createAmberUpgradedSpecialItem(String name, Tier tier, Rarity rarity, int damage, float speed, String descriptionName, String gemstoneExtraTooltip, Holder<MobEffect> effect, int durationInTicks, int amplifier) {
        return ITEMS.register(name,
                () -> new OvergrownWeapon(tier, effect, durationInTicks, amplifier, new Item.Properties()
                        .rarity(rarity)
                        .fireResistant()
                        .attributes(SwordItem.createAttributes(tier, damage, -speed))) //Tier/Damage/attackSpeed
                {
                    @Override
                    public void appendHoverText(ItemStack pStack, Item.TooltipContext pContext, List<Component> pTooltipComponents, TooltipFlag pTooltipFlag) {
                        pTooltipComponents.add(Component.translatable(descriptionName));
                        pTooltipComponents.add(Component.translatable(gemstoneExtraTooltip));
                        super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
                    }
                });
    }

    public static DeferredItem<SwordItem> createGemstoneSpecialItem(String name, Tier tier, Rarity rarity, int damage, float speed, String descriptionName, String gemstoneExtraTooltip, Holder<MobEffect> effect, int durationInTicks, int amplifier) {
        return ITEMS.register(name,
                () -> new OvergrownWeapon(tier, effect, durationInTicks, amplifier, new Item.Properties()
                        .rarity(rarity)
                        .attributes(SwordItem.createAttributes(tier, damage, -speed))) //Tier/Damage/attackSpeed
                {
                    @Override
                    public void appendHoverText(ItemStack pStack, Item.TooltipContext pContext, List<Component> pTooltipComponents, TooltipFlag pTooltipFlag) {
                        pTooltipComponents.add(Component.translatable(descriptionName));
                        pTooltipComponents.add(Component.translatable(gemstoneExtraTooltip));
                        super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
                    }
                });
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

    /**ingredients*/
    //upgrade shards
    public static final DeferredItem<Item> INANISIUM_SHARD = createItem("inanisium_shard");
    public static final DeferredItem<Item> IGNISIUM_SHARD = createItem("ignisium_shard");
    public static final DeferredItem<Item> MORSIUM_SHARD = createItem("morsium_shard");
    public static final DeferredItem<Item> SOMNIUM_SHARD = createItem("somnium_shard");
    public static final DeferredItem<Item> VULNUSIUM_SHARD = createItem("vulnusium_shard");
    public static final DeferredItem<Item> LUSH_SHARD = createItem("lush_shard");
    public static final DeferredItem<Item> PULSITE_SHARD = createItem("pulsite_shard");
    public static final DeferredItem<Item> ELECTRIUM_SHARD = createItem("electrium_shard");
    public static final DeferredItem<Item> TAIFUNITE_SHARD = createItem("taifunite_shard");
    public static final DeferredItem<Item> DEVELOPIUM_SHARD = createItem("developium_shard");
    
    //templates
    public static final DeferredItem<Item> ANCIENT_UPGRADE_TEMPLATE = createItemWithDescription("ancient_upgrade_template", "tooltips.forgermod.ancient_upgrade_template.tooltip");
    public static final DeferredItem<Item> GEMSTONE_UPGRADE_TEMPLATE = createItemWithDescription("gemstone_upgrade_template", "tooltips.forgermod.gemstone_upgrade_template.tooltip");

    //special
    public static final DeferredItem<Item> INANISIUM_INGOT = createItemWithDescription("inanisium_ingot", "tooltips.forgermod.inanisium_ingot.tooltip");
    public static final DeferredItem<Item> IGNISIUM_INGOT = createItemWithDescription("ignisium_ingot", "tooltips.forgermod.ignisium_ingot.tooltip");
    public static final DeferredItem<Item> MORSIUM_INGOT = createItemWithDescription("morsium_ingot", "tooltips.forgermod.morsium_ingot.tooltip");
    public static final DeferredItem<Item> SOMNIUM_INGOT = createItemWithDescription("somnium_ingot", "tooltips.forgermod.somnium_ingot.tooltip");
    public static final DeferredItem<Item> VULNUSIUM_INGOT = createItemWithDescription("vulnusium_ingot", "tooltips.forgermod.vulnusium_ingot.tooltip");
    public static final DeferredItem<Item> LUSH_INGOT = createItemWithDescription("lush_ingot", "tooltips.forgermod.lush_ingot.tooltip");
    public static final DeferredItem<Item> PULSITE_INGOT = createItemWithDescription("pulsite_ingot", "tooltips.forgermod.pulsite_ingot.tooltip");
    public static final DeferredItem<Item> ELECTRIUM_INGOT = createItemWithDescription("electrium_ingot", "tooltips.forgermod.electrium_ingot.tooltip");
    public static final DeferredItem<Item> TAIFUNITE_INGOT = createItemWithDescription("taifunite_ingot", "tooltips.forgermod.taifunite_ingot.tooltip");
    public static final DeferredItem<Item> DEVELOPIUM_INGOT = createItemWithDescription("developium_ingot", "tooltips.forgermod.developium_ingot.tooltip");

    //bare materials or unrefined metals
    public static final DeferredItem<Item> SCRAP_INGOT = createItem("scrap_ingot");
    public static final DeferredItem<Item> SCRAP_IRON_INGOT = createItem("scrap_iron_ingot");
    public static final DeferredItem<Item> UNREFINED_STEEL = createItem("unrefined_steel");
    public static final DeferredItem<Item> UNREFINED_CARBON_STEEL = createItem("unrefined_carbon_steel");
    public static final DeferredItem<Item> SHARPENED_BLADE = createItemWithDescription("sharpened_blade", "tooltips.forgermod.sharpened_blade.tooltip");
    public static final DeferredItem<Item> CARBON_STEEL_CROSS_GUARD = createItemWithDescription("carbon_steel_cross_guard", "tooltips.forgermod.carbon_steel_cross_guard.tooltip");
    public static final DeferredItem<Item> HANDLE = createItemWithDescription("handle", "tooltips.forgermod.handle.tooltip");
    public static final DeferredItem<Item> ADVANCED_HANDLE = createItemWithDescription("advanced_handle", "tooltips.forgermod.advanced_handle.tooltip");

    //refined/reinforced metals or end products
    public static final DeferredItem<Item> DAMASK_INGOT = createItem("damask_ingot");
    public static final DeferredItem<Item> REINFORCED_IRON_INGOT = createItem("reinforced_iron_ingot");
    public static final DeferredItem<Item> CARBON_STEEL_INGOT = createItem("carbon_steel_ingot");
    public static final DeferredItem<Item> STEEL_INGOT = createItem("steel_ingot");

//    public static final DeferredItem<Item> TITANIUM_INGOT = createItem("titanium_ingot");

    //gemstones
    public static final DeferredItem<Item> RUBY_GEMSTONE = createItemWithDescription("ruby_gemstone", "tooltips.forgermod.ruby.tooltip");
    public static final DeferredItem<Item> AMBER_GEMSTONE = createItemWithDescription("amber_gemstone", "tooltips.forgermod.amber.tooltip");
    public static final DeferredItem<Item> AMETHYST_GEMSTONE = createItemWithDescription("amethyst_gemstone", "tooltips.forgermod.amethyst.tooltip");
    public static final DeferredItem<Item> JADE_GEMSTONE = createItemWithDescription("jade_gemstone", "tooltips.forgermod.jade.tooltip");

    /**Tools (incl. Swords)*/

    //swords and claymores
    public static final DeferredItem<SwordItem> STEEL_SWORD = createSwordItem("steel_sword", ModToolTiers.STEEL, STEEL_SWORD_DAMAGE, STEEL_SWORD_SPEED);
    public static final DeferredItem<SwordItem> STUMPFL_BAT = createSwordItemWithDescription("stumpfl_bat", ModToolTiers.DEVELOPIUM, 7770, 1f, "tooltips.forgermod.stumpfl_bat.tooltip");
    public static final DeferredItem<SwordItem> DAMASK_SWORD = createSwordItem("damask_sword", ModToolTiers.DAMASK, DAMASK_SWORD_DAMAGE, DAMASK_SWORD_SPEED);
    public static final DeferredItem<SwordItem> REINFORCED_IRON_SWORD = createSwordItem("reinforced_iron_sword", ModToolTiers.REINFORCED_IRON, REINFORCED_IRON_SWORD_DAMAGE, REINFOCED_IRON_SWORD_SPEED);
    public static final DeferredItem<SwordItem> SCRAP_IRON_SWORD = createSwordItem("scrap_iron_sword", ModToolTiers.SCRAP_IRON, SCRAP_IRON_SWORD_DAMAGE, SCRAP_IRON_SWORD_SPEED);
    public static final DeferredItem<SwordItem> SCRAP_SWORD = createSwordItem("scrap_sword", ModToolTiers.SCRAP, SCRAP_SWORD_DAMAGE, SCRAP_SWORD_SPEED);
    public static final DeferredItem<SwordItem> RUSTY_CLAYMORE = createSwordItemWithRarityAndDescription("rusty_claymore", ModToolTiers.CARBON_STEEL, Rarity.UNCOMMON, DEFAULT_RUSTY_DAMAGE, DEFAULT_RUSTY_SPEED, "tooltips.forgermod.rusty_claymore.tooltip");
    public static final DeferredItem<SwordItem> CLAYMORE = createSwordItemWithRarityAndDescription("claymore", ModToolTiers.CARBON_STEEL, Rarity.UNCOMMON, DEFAULT_CLAYMORE_DAMAGE, DEFAULT_CLAYMORE_SPEED, "tooltips.forgermod.claymore.tooltip");
    public static final DeferredItem<SwordItem> DAMASK_KNIFE = createSwordItem("damask_knife", ModToolTiers.DAMASK, KNIFE_DAMASK_DAMAGE, KNIFE_DAMASK_SPEED);

    //special claymores
    public static final DeferredItem<SwordItem> OVERGROWN_CLAYMORE = createGemstoneSpecialItem("overgrown_claymore", ModToolTiers.LUSH, Rarity.EPIC, DEFAULT_SPECIAL_DAMAGE, DEFAULT_SPECIAL_SPEED, "tooltips.forgermod.overgrown_claymore.tooltip", "tooltips.forgermod.no_gemstone.tooltip_extra", MobEffects.GLOWING, 200, 3);
    public static final DeferredItem<SwordItem> HOLLOW_CLAYMORE = createGemstoneSpecialItem("hollow_claymore", ModToolTiers.MORSIUM, Rarity.EPIC, DEFAULT_SPECIAL_DAMAGE, DEFAULT_SPECIAL_SPEED, "tooltips.forgermod.hollow_claymore.tooltip", "tooltips.forgermod.no_gemstone.tooltip_extra", MobEffects.WEAKNESS, 100, 3);
    public static final DeferredItem<SwordItem> INFERNAL_CLAYMORE = createGemstoneSpecialItem("infernal_claymore", ModToolTiers.IGNISIUM, Rarity.EPIC, DEFAULT_SPECIAL_DAMAGE, DEFAULT_SPECIAL_SPEED, "tooltips.forgermod.infernal_claymore.tooltip", "tooltips.forgermod.no_gemstone.tooltip_extra", MobEffects.GLOWING, 2000, 1);
    public static final DeferredItem<SwordItem> CLAYMORE_OF_THE_VOID = createGemstoneSpecialItem("claymore_of_the_void", ModToolTiers.INANISIUM, Rarity.EPIC, DEFAULT_SPECIAL_DAMAGE, DEFAULT_SPECIAL_SPEED, "tooltips.forgermod.claymore_of_the_void.tooltip", "tooltips.forgermod.no_gemstone.tooltip_extra", MobEffects.BLINDNESS, 80, 1);
    public static final DeferredItem<SwordItem> CURSEBLOOD_CLAYMORE = createGemstoneSpecialItem("curseblood_claymore", ModToolTiers.VULNUSIUM, Rarity.EPIC, DEFAULT_SPECIAL_DAMAGE, DEFAULT_SPECIAL_SPEED, "tooltips.forgermod.curseblood_claymore.tooltip", "tooltips.forgermod.no_gemstone.tooltip_extra", MobEffects.WITHER, 80, 1);
    public static final DeferredItem<SwordItem> DREAMBOUND_CLAYMORE = createGemstoneSpecialItem("dreambound_claymore", ModToolTiers.SOMNIUM, Rarity.EPIC, DEFAULT_SPECIAL_DAMAGE, DEFAULT_SPECIAL_SPEED, "tooltips.forgermod.dreambound_claymore.tooltip", "tooltips.forgermod.no_gemstone.tooltip_extra", MobEffects.CONFUSION, 80, 4);
    public static final DeferredItem<SwordItem> SHRIEKING_CLAYMORE = createGemstoneSpecialItem("shrieking_claymore", ModToolTiers.PULSITE, Rarity.EPIC, DEFAULT_SPECIAL_DAMAGE, DEFAULT_SPECIAL_SPEED, "tooltips.forgermod.shrieking_claymore.tooltip", "tooltips.forgermod.no_gemstone.tooltip_extra", MobEffects.DARKNESS, 140, 5);
    public static final DeferredItem<SwordItem> CLAYMORE_OF_THUNDER = createGemstoneSpecialItem("claymore_of_thunder", ModToolTiers.ELECTRIUM, Rarity.EPIC, DEFAULT_SPECIAL_DAMAGE, DEFAULT_SPECIAL_SPEED, "tooltips.forgermod.claymore_of_thunder.tooltip", "tooltips.forgermod.no_gemstone.tooltip_extra", MobEffects.MOVEMENT_SLOWDOWN, 100, 3);
    public static final DeferredItem<SwordItem> STORMING_CLAYMORE = createGemstoneSpecialItem("storming_claymore", ModToolTiers.TAIFUNITE, Rarity.EPIC, DEFAULT_SPECIAL_DAMAGE, DEFAULT_SPECIAL_SPEED, "tooltips.forgermod.storming_claymore.tooltip", "tooltips.forgermod.no_gemstone.tooltip_extra", MobEffects.LEVITATION, 60, 2);

    //gemstone infused variants
    public static final DeferredItem<SwordItem> CLAYMORE_OF_THUNDER_RUBY = createGemstoneSpecialItem("claymore_of_thunder_ruby", ModToolTiers.ELECTRIUM, Rarity.EPIC, GEMSTONE_SPECIAL_DAMAGE, DEFAULT_SPECIAL_SPEED, "tooltips.forgermod.claymore_of_thunder.tooltip", "tooltips.forgermod.ruby.tooltip_extra", MobEffects.MOVEMENT_SLOWDOWN, 100, 3);
    public static final DeferredItem<SwordItem> CLAYMORE_OF_THUNDER_AMBER = createAmberUpgradedSpecialItem("claymore_of_thunder_amber", ModToolTiers.ELECTRIUM, Rarity.EPIC, DEFAULT_SPECIAL_DAMAGE, DEFAULT_SPECIAL_SPEED, "tooltips.forgermod.claymore_of_thunder.tooltip", "tooltips.forgermod.amber.tooltip_extra", MobEffects.MOVEMENT_SLOWDOWN, 100, 3);
    public static final DeferredItem<SwordItem> CLAYMORE_OF_THUNDER_AMETHYST = createGemstoneSpecialItem("claymore_of_thunder_amethyst", ModToolTiers.ELECTRIUM, Rarity.EPIC, DEFAULT_SPECIAL_DAMAGE, GEMSTONE_SPECIAL_SPEED, "tooltips.forgermod.claymore_of_thunder.tooltip", "tooltips.forgermod.amethyst.tooltip_extra", MobEffects.MOVEMENT_SLOWDOWN, 100, 3);
    public static final DeferredItem<SwordItem> CLAYMORE_OF_THUNDER_JADE = createGemstoneSpecialItem("claymore_of_thunder_jade", ModToolTiers.ELECTRIUM, Rarity.EPIC, DEFAULT_SPECIAL_DAMAGE, DEFAULT_SPECIAL_SPEED, "tooltips.forgermod.claymore_of_thunder.tooltip", "tooltips.forgermod.jade.tooltip_extra", MobEffects.MOVEMENT_SLOWDOWN, 120, 4);

    public static final DeferredItem<SwordItem> SHRIEKING_CLAYMORE_RUBY = createGemstoneSpecialItem("shrieking_claymore_ruby", ModToolTiers.PULSITE, Rarity.EPIC, GEMSTONE_SPECIAL_DAMAGE, DEFAULT_SPECIAL_SPEED, "tooltips.forgermod.shrieking_claymore.tooltip", "tooltips.forgermod.ruby.tooltip_extra", MobEffects.DARKNESS, 140, 5);
    public static final DeferredItem<SwordItem> SHRIEKING_CLAYMORE_AMBER = createAmberUpgradedSpecialItem("shrieking_claymore_amber", ModToolTiers.PULSITE, Rarity.EPIC, DEFAULT_SPECIAL_DAMAGE, DEFAULT_SPECIAL_SPEED, "tooltips.forgermod.shrieking_claymore.tooltip", "tooltips.forgermod.amber.tooltip_extra", MobEffects.DARKNESS, 140, 5);
    public static final DeferredItem<SwordItem> SHRIEKING_CLAYMORE_AMETHYST = createGemstoneSpecialItem("shrieking_claymore_amethyst", ModToolTiers.PULSITE, Rarity.EPIC, DEFAULT_SPECIAL_DAMAGE, GEMSTONE_SPECIAL_SPEED, "tooltips.forgermod.shrieking_claymore.tooltip", "tooltips.forgermod.amethyst.tooltip_extra", MobEffects.DARKNESS, 140, 5);
    public static final DeferredItem<SwordItem> SHRIEKING_CLAYMORE_JADE = createGemstoneSpecialItem("shrieking_claymore_jade", ModToolTiers.PULSITE, Rarity.EPIC, DEFAULT_SPECIAL_DAMAGE, DEFAULT_SPECIAL_SPEED, "tooltips.forgermod.shrieking_claymore.tooltip", "tooltips.forgermod.jade.tooltip_extra", MobEffects.DARKNESS, 160, 6);

    public static final DeferredItem<SwordItem> DREAMBOUND_CLAYMORE_RUBY = createGemstoneSpecialItem("dreambound_claymore_ruby", ModToolTiers.SOMNIUM, Rarity.EPIC, GEMSTONE_SPECIAL_DAMAGE, DEFAULT_SPECIAL_SPEED, "tooltips.forgermod.dreambound_claymore.tooltip", "tooltips.forgermod.ruby.tooltip_extra", MobEffects.CONFUSION, 80, 4);
    public static final DeferredItem<SwordItem> DREAMBOUND_CLAYMORE_AMBER = createAmberUpgradedSpecialItem("dreambound_claymore_amber", ModToolTiers.SOMNIUM, Rarity.EPIC, DEFAULT_SPECIAL_DAMAGE, DEFAULT_SPECIAL_SPEED, "tooltips.forgermod.dreambound_claymore.tooltip", "tooltips.forgermod.amber.tooltip_extra", MobEffects.CONFUSION, 80, 4);
    public static final DeferredItem<SwordItem> DREAMBOUND_CLAYMORE_AMETHYST = createGemstoneSpecialItem("dreambound_claymore_amethyst", ModToolTiers.SOMNIUM, Rarity.EPIC, DEFAULT_SPECIAL_DAMAGE, GEMSTONE_SPECIAL_SPEED, "tooltips.forgermod.dreambound_claymore.tooltip", "tooltips.forgermod.amethyst.tooltip_extra", MobEffects.CONFUSION, 80, 4);
    public static final DeferredItem<SwordItem> DREAMBOUND_CLAYMORE_JADE = createGemstoneSpecialItem("dreambound_claymore_jade", ModToolTiers.SOMNIUM, Rarity.EPIC, DEFAULT_SPECIAL_DAMAGE, DEFAULT_SPECIAL_SPEED, "tooltips.forgermod.dreambound_claymore.tooltip", "tooltips.forgermod.jade.tooltip_extra", MobEffects.CONFUSION, 100, 5);

    public static final DeferredItem<SwordItem> CURSEBLOOD_CLAYMORE_RUBY = createGemstoneSpecialItem("curseblood_claymore_ruby", ModToolTiers.VULNUSIUM, Rarity.EPIC, GEMSTONE_SPECIAL_DAMAGE, DEFAULT_SPECIAL_SPEED, "tooltips.forgermod.curseblood_claymore.tooltip", "tooltips.forgermod.ruby.tooltip_extra", MobEffects.WITHER, 80, 2);
    public static final DeferredItem<SwordItem> CURSEBLOOD_CLAYMORE_AMBER = createAmberUpgradedSpecialItem("curseblood_claymore_amber", ModToolTiers.VULNUSIUM, Rarity.EPIC, DEFAULT_SPECIAL_DAMAGE, DEFAULT_SPECIAL_SPEED, "tooltips.forgermod.curseblood_claymore.tooltip", "tooltips.forgermod.amber.tooltip_extra", MobEffects.WITHER, 80, 2);
    public static final DeferredItem<SwordItem> CURSEBLOOD_CLAYMORE_AMETHYST = createGemstoneSpecialItem("curseblood_claymore_amethyst", ModToolTiers.VULNUSIUM, Rarity.EPIC, DEFAULT_SPECIAL_DAMAGE, GEMSTONE_SPECIAL_SPEED, "tooltips.forgermod.curseblood_claymore.tooltip", "tooltips.forgermod.amethyst.tooltip_extra", MobEffects.WITHER, 80, 2);
    public static final DeferredItem<SwordItem> CURSEBLOOD_CLAYMORE_JADE = createGemstoneSpecialItem("curseblood_claymore_jade", ModToolTiers.VULNUSIUM, Rarity.EPIC, DEFAULT_SPECIAL_DAMAGE, DEFAULT_SPECIAL_SPEED, "tooltips.forgermod.curseblood_claymore.tooltip", "tooltips.forgermod.jade.tooltip_extra", MobEffects.WITHER, 100, 3);

    public static final DeferredItem<SwordItem> CLAYMORE_OF_THE_VOID_RUBY = createGemstoneSpecialItem("claymore_of_the_void_ruby", ModToolTiers.INANISIUM, Rarity.EPIC, GEMSTONE_SPECIAL_DAMAGE, DEFAULT_SPECIAL_SPEED, "tooltips.forgermod.claymore_of_the_void.tooltip", "tooltips.forgermod.ruby.tooltip_extra", MobEffects.BLINDNESS, 80, 1);
    public static final DeferredItem<SwordItem> CLAYMORE_OF_THE_VOID_AMBER = createAmberUpgradedSpecialItem("claymore_of_the_void_amber", ModToolTiers.INANISIUM, Rarity.EPIC, DEFAULT_SPECIAL_DAMAGE, DEFAULT_SPECIAL_SPEED, "tooltips.forgermod.claymore_of_the_void.tooltip", "tooltips.forgermod.amber.tooltip_extra", MobEffects.BLINDNESS, 80, 1);
    public static final DeferredItem<SwordItem> CLAYMORE_OF_THE_VOID_AMETHYST = createGemstoneSpecialItem("claymore_of_the_void_amethyst", ModToolTiers.INANISIUM, Rarity.EPIC, DEFAULT_SPECIAL_DAMAGE, GEMSTONE_SPECIAL_SPEED, "tooltips.forgermod.claymore_of_the_void.tooltip", "tooltips.forgermod.amethyst.tooltip_extra", MobEffects.BLINDNESS, 80, 1);
    public static final DeferredItem<SwordItem> CLAYMORE_OF_THE_VOID_JADE = createGemstoneSpecialItem("claymore_of_the_void_jade", ModToolTiers.INANISIUM, Rarity.EPIC, DEFAULT_SPECIAL_DAMAGE, DEFAULT_SPECIAL_SPEED, "tooltips.forgermod.claymore_of_the_void.tooltip", "tooltips.forgermod.jade.tooltip_extra", MobEffects.BLINDNESS, 100, 2);

    public static final DeferredItem<SwordItem> INFERNAL_CLAYMORE_RUBY = createGemstoneSpecialItem("infernal_claymore_ruby", ModToolTiers.IGNISIUM, Rarity.EPIC, GEMSTONE_SPECIAL_DAMAGE, DEFAULT_SPECIAL_SPEED, "tooltips.forgermod.infernal_claymore.tooltip", "tooltips.forgermod.ruby.tooltip_extra", MobEffects.GLOWING, 2000, 1);
    public static final DeferredItem<SwordItem> INFERNAL_CLAYMORE_AMBER = createAmberUpgradedSpecialItem("infernal_claymore_amber", ModToolTiers.IGNISIUM, Rarity.EPIC, DEFAULT_SPECIAL_DAMAGE, DEFAULT_SPECIAL_SPEED, "tooltips.forgermod.infernal_claymore.tooltip", "tooltips.forgermod.amber.tooltip_extra", MobEffects.GLOWING, 2000, 1);
    public static final DeferredItem<SwordItem> INFERNAL_CLAYMORE_AMETHYST = createGemstoneSpecialItem("infernal_claymore_amethyst", ModToolTiers.IGNISIUM, Rarity.EPIC, DEFAULT_SPECIAL_DAMAGE, GEMSTONE_SPECIAL_SPEED, "tooltips.forgermod.infernal_claymore.tooltip", "tooltips.forgermod.amethyst.tooltip_extra", MobEffects.GLOWING, 2000, 1);
    public static final DeferredItem<SwordItem> INFERNAL_CLAYMORE_JADE = createGemstoneSpecialItem("infernal_claymore_jade", ModToolTiers.IGNISIUM, Rarity.EPIC, DEFAULT_SPECIAL_DAMAGE, DEFAULT_SPECIAL_SPEED, "tooltips.forgermod.infernal_claymore.tooltip", "tooltips.forgermod.jade.tooltip_extra", MobEffects.GLOWING, 2020, 2);

    public static final DeferredItem<SwordItem> HOLLOW_CLAYMORE_RUBY = createGemstoneSpecialItem("hollow_claymore_ruby", ModToolTiers.MORSIUM, Rarity.EPIC, GEMSTONE_SPECIAL_DAMAGE, DEFAULT_SPECIAL_SPEED, "tooltips.forgermod.hollow_claymore.tooltip", "tooltips.forgermod.ruby.tooltip_extra", MobEffects.WEAKNESS, 100, 3);
    public static final DeferredItem<SwordItem> HOLLOW_CLAYMORE_AMBER = createAmberUpgradedSpecialItem("hollow_claymore_amber", ModToolTiers.MORSIUM, Rarity.EPIC, DEFAULT_SPECIAL_DAMAGE, DEFAULT_SPECIAL_SPEED, "tooltips.forgermod.hollow_claymore.tooltip", "tooltips.forgermod.amber.tooltip_extra", MobEffects.WEAKNESS, 100, 3);
    public static final DeferredItem<SwordItem> HOLLOW_CLAYMORE_AMETHYST = createGemstoneSpecialItem("hollow_claymore_amethyst", ModToolTiers.MORSIUM, Rarity.EPIC, DEFAULT_SPECIAL_DAMAGE, GEMSTONE_SPECIAL_SPEED, "tooltips.forgermod.hollow_claymore.tooltip", "tooltips.forgermod.amethyst.tooltip_extra", MobEffects.WEAKNESS, 100, 3);
    public static final DeferredItem<SwordItem> HOLLOW_CLAYMORE_JADE = createGemstoneSpecialItem("hollow_claymore_jade", ModToolTiers.MORSIUM, Rarity.EPIC, DEFAULT_SPECIAL_DAMAGE, DEFAULT_SPECIAL_SPEED, "tooltips.forgermod.hollow_claymore.tooltip", "tooltips.forgermod.jade.tooltip_extra", MobEffects.WEAKNESS, 120, 4);

    public static final DeferredItem<SwordItem> STORMING_CLAYMORE_RUBY = createGemstoneSpecialItem("storming_claymore_ruby", ModToolTiers.TAIFUNITE, Rarity.EPIC, GEMSTONE_SPECIAL_DAMAGE, DEFAULT_SPECIAL_SPEED, "tooltips.forgermod.storming_claymore.tooltip", "tooltips.forgermod.ruby.tooltip_extra", MobEffects.LEVITATION, 60, 2);
    public static final DeferredItem<SwordItem> STORMING_CLAYMORE_AMBER = createAmberUpgradedSpecialItem("storming_claymore_amber", ModToolTiers.TAIFUNITE, Rarity.EPIC, DEFAULT_SPECIAL_DAMAGE, DEFAULT_SPECIAL_SPEED, "tooltips.forgermod.storming_claymore.tooltip", "tooltips.forgermod.amber.tooltip_extra", MobEffects.LEVITATION, 60, 2);
    public static final DeferredItem<SwordItem> STORMING_CLAYMORE_AMETHYST = createGemstoneSpecialItem("storming_claymore_amethyst", ModToolTiers.TAIFUNITE, Rarity.EPIC, DEFAULT_SPECIAL_DAMAGE, GEMSTONE_SPECIAL_SPEED, "tooltips.forgermod.storming_claymore.tooltip", "tooltips.forgermod.amethyst.tooltip_extra", MobEffects.LEVITATION, 60, 2);
    public static final DeferredItem<SwordItem> STORMING_CLAYMORE_JADE = createGemstoneSpecialItem("storming_claymore_jade", ModToolTiers.TAIFUNITE, Rarity.EPIC, DEFAULT_SPECIAL_DAMAGE, DEFAULT_SPECIAL_SPEED, "tooltips.forgermod.storming_claymore.tooltip", "tooltips.forgermod.jade.tooltip_extra", MobEffects.LEVITATION, 80, 3);

    public static final DeferredItem<SwordItem> OVERGROWN_CLAYMORE_RUBY = createGemstoneSpecialItem("overgrown_claymore_ruby", ModToolTiers.LUSH, Rarity.EPIC, GEMSTONE_SPECIAL_DAMAGE, DEFAULT_SPECIAL_SPEED, "tooltips.forgermod.overgrown_claymore.tooltip", "tooltips.forgermod.ruby.tooltip_extra", MobEffects.HUNGER, 200, 3);
    public static final DeferredItem<SwordItem> OVERGROWN_CLAYMORE_AMBER = createAmberUpgradedSpecialItem("overgrown_claymore_amber", ModToolTiers.LUSH, Rarity.EPIC, DEFAULT_SPECIAL_DAMAGE, DEFAULT_SPECIAL_SPEED, "tooltips.forgermod.overgrown_claymore.tooltip", "tooltips.forgermod.amber.tooltip_extra", MobEffects.HUNGER, 200, 3);
    public static final DeferredItem<SwordItem> OVERGROWN_CLAYMORE_AMETHYST = createGemstoneSpecialItem("overgrown_claymore_amethyst", ModToolTiers.LUSH, Rarity.EPIC, DEFAULT_SPECIAL_DAMAGE, GEMSTONE_SPECIAL_SPEED, "tooltips.forgermod.overgrown_claymore.tooltip", "tooltips.forgermod.amethyst.tooltip_extra", MobEffects.HUNGER, 200, 3);
    public static final DeferredItem<SwordItem> OVERGROWN_CLAYMORE_JADE = createGemstoneSpecialItem("overgrown_claymore_jade", ModToolTiers.LUSH, Rarity.EPIC, DEFAULT_SPECIAL_DAMAGE, DEFAULT_SPECIAL_SPEED, "tooltips.forgermod.overgrown_claymore.tooltip", "tooltips.forgermod.jade.tooltip_extra", MobEffects.HUNGER, 220, 4);

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}