package net.bananashelp20.forgermod.item;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.item.custom.*;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.*;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.Supplier;

import static net.bananashelp20.forgermod.item.ModSpecialRegistry.*;

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

    public static DeferredItem<SwordItem> createSwordItemWithRarityAndDescriptionBeingFireResistent(String name, Tier tier, Rarity rarity, int damage, float speed, String descriptionName) {
        return ITEMS.register(name,
                () -> new SwordItem(tier, new Item.Properties()
                        .rarity(rarity)
                        .attributes(SwordItem.createAttributes(tier, damage, -speed)).fireResistant()) //Tier/Damage/attackSpeed
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

    //STARTGENERATING
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
    public static final DeferredItem<Item> RAW_METEORIC_IRON = createItem("raw_meteoric_iron");

    //refined/reinforced metals or end products
    public static final DeferredItem<Item> DAMASK_INGOT = createItem("damask_ingot");
    public static final DeferredItem<Item> REINFORCED_IRON_INGOT = createItem("reinforced_iron_ingot");
    public static final DeferredItem<Item> CARBON_STEEL_INGOT = createItem("carbon_steel_ingot");
    public static final DeferredItem<Item> STEEL_INGOT = createItem("steel_ingot");
    public static final DeferredItem<Item> TITANIUM_INGOT = createItem("titanium_ingot");
    public static final DeferredItem<Item> METEORIC_IRON_INGOT = createItem("meteoric_iron_ingot");
    public static final DeferredItem<Item> TUNGSTEN_INGOT = createItem("tungsten_ingot");

    //gemstones
    public static final DeferredItem<Item> RUBY_GEMSTONE = createItemWithDescription("ruby_gemstone", "tooltips.forgermod.ruby.tooltip");
    public static final DeferredItem<Item> AMBER_GEMSTONE = createItemWithDescription("amber_gemstone", "tooltips.forgermod.amber.tooltip");
    public static final DeferredItem<Item> AMETHYST_GEMSTONE = createItemWithDescription("amethyst_gemstone", "tooltips.forgermod.amethyst.tooltip");
    public static final DeferredItem<Item> JADE_GEMSTONE = createItemWithDescription("jade_gemstone", "tooltips.forgermod.jade.tooltip");
    public static final DeferredItem<Item> ALEXANDRITE_GEMSTONE = createItemWithDescription("alexandrite_gemstone", "tooltips.forgermod.alexandrite.tooltip");

    /**Tools (incl. Swords)*/
    //swords
    public static final DeferredItem<SwordItem> STEEL_SWORD = createSwordItem("steel_sword", ModToolTiers.STEEL, STEEL_SWORD_DAMAGE, STEEL_SWORD_SPEED);
    public static final DeferredItem<SwordItem> DAMASK_SWORD = createSwordItem("damask_sword", ModToolTiers.DAMASK, DAMASK_SWORD_DAMAGE, DAMASK_SWORD_SPEED);
    public static final DeferredItem<SwordItem> REINFORCED_IRON_SWORD = createSwordItem("reinforced_iron_sword", ModToolTiers.REINFORCED_IRON, REINFORCED_IRON_SWORD_DAMAGE, REINFOCED_IRON_SWORD_SPEED);
    public static final DeferredItem<SwordItem> SCRAP_IRON_SWORD = createSwordItem("scrap_iron_sword", ModToolTiers.SCRAP_IRON, SCRAP_IRON_SWORD_DAMAGE, SCRAP_IRON_SWORD_SPEED);
    public static final DeferredItem<SwordItem> SCRAP_SWORD = createSwordItem("scrap_sword", ModToolTiers.SCRAP, SCRAP_SWORD_DAMAGE, SCRAP_SWORD_SPEED);

    //claymores
    public static final DeferredItem<SwordItem> CLAYMORE = createSwordItemWithRarityAndDescriptionBeingFireResistent("claymore", ModToolTiers.CARBON_STEEL, Rarity.UNCOMMON, DEFAULT_CLAYMORE_DAMAGE, DEFAULT_CLAYMORE_SPEED, "tooltips.forgermod.claymore.tooltip");
    public static final DeferredItem<SwordItem> RUSTY_CLAYMORE = createSwordItemWithRarityAndDescription("rusty_claymore", ModToolTiers.CARBON_STEEL, Rarity.UNCOMMON, DEFAULT_RUSTY_CLAYMORE_DAMAGE, DEFAULT_RUSTY_CLAYMORE_SPEED, "tooltips.forgermod.rusty_claymore.tooltip");
    public static final DeferredItem<SwordItem> OVERGROWN_CLAYMORE = createSpecialSwordItem("overgrown_claymore", () -> new LushWeapon("no_gemstone", "claymore"));
    public static final DeferredItem<SwordItem> HOLLOW_CLAYMORE = createSpecialSwordItem("hollow_claymore", () -> new MorsiumWeapon("no_gemstone", "claymore"));
    public static final DeferredItem<SwordItem> INFERNAL_CLAYMORE = createSpecialSwordItem("infernal_claymore", () -> new IgnisiumWeapon("no_gemstone", "claymore"));
    public static final DeferredItem<SwordItem> CLAYMORE_OF_THE_VOID = createSpecialSwordItem("claymore_of_the_void", () -> new InanisiumWeapon("no_gemstone", "claymore"));
    public static final DeferredItem<SwordItem> CURSEBLOOD_CLAYMORE = createSpecialSwordItem("curseblood_claymore", () -> new VulnusiumWeapon("no_gemstone", "claymore"));
    public static final DeferredItem<SwordItem> DREAMBOUND_CLAYMORE = createSpecialSwordItem("dreambound_claymore", () -> new SomniumWeapon("no_gemstone", "claymore"));
    public static final DeferredItem<SwordItem> SHRIEKING_CLAYMORE = createSpecialSwordItem("shrieking_claymore", () -> new PulsiteWeapon("no_gemstone", "claymore"));
    public static final DeferredItem<SwordItem> CLAYMORE_OF_THUNDER = createSpecialSwordItem("claymore_of_thunder", () -> new ElectriumWeapon("no_gemstone", "claymore"));
    public static final DeferredItem<SwordItem> STORMING_CLAYMORE = createSpecialSwordItem("storming_claymore", () -> new TaifuniteWeapon("no_gemstone", "claymore"));

    //axes
    public static final DeferredItem<SwordItem> CARBON_STEEL_AXE = createSwordItemWithRarityAndDescriptionBeingFireResistent("axe", ModToolTiers.CARBON_STEEL, Rarity.UNCOMMON, DEFAULT_AXE_DAMAGE, DEFAULT_AXE_SPEED, "tooltips.forgermod.axe.tooltip");
    public static final DeferredItem<SwordItem> RUSTY_AXE = createSwordItemWithRarityAndDescription("rusty_axe", ModToolTiers.CARBON_STEEL, Rarity.UNCOMMON, DEFAULT_RUSTY_AXE_DAMAGE, DEFAULT_RUSTY_AXE_SPEED, "tooltips.forgermod.rusty_axe.tooltip");
    public static final DeferredItem<SwordItem> OVERGROWN_AXE = createSpecialSwordItem("nature_axe", () -> new LushWeapon("no_gemstone", "axe"));
    public static final DeferredItem<SwordItem> HOLLOW_AXE = createSpecialSwordItem("hollow_axe", () -> new MorsiumWeapon("no_gemstone", "axe"));
    public static final DeferredItem<SwordItem> INFERNAL_AXE = createSpecialSwordItem("infernal_axe", () -> new IgnisiumWeapon("no_gemstone", "axe"));
    public static final DeferredItem<SwordItem> AXE_OF_THE_VOID = createSpecialSwordItem("axe_of_the_void", () -> new InanisiumWeapon("no_gemstone", "axe"));
    public static final DeferredItem<SwordItem> CURSEBLOOD_AXE = createSpecialSwordItem("curseblood_axe", () -> new VulnusiumWeapon("no_gemstone", "axe"));
    public static final DeferredItem<SwordItem> DREAMBOUND_AXE = createSpecialSwordItem("dreambound_axe", () -> new SomniumWeapon("no_gemstone", "axe"));
    public static final DeferredItem<SwordItem> SHRIEKING_AXE = createSpecialSwordItem("shrieking_axe", () -> new PulsiteWeapon("no_gemstone", "axe"));
    public static final DeferredItem<SwordItem> AXE_OF_THUNDER = createSpecialSwordItem("axe_of_thunder", () -> new ElectriumWeapon("no_gemstone", "axe"));
    public static final DeferredItem<SwordItem> STORMING_AXE = createSpecialSwordItem("storming_axe", () -> new TaifuniteWeapon("no_gemstone", "axe"));

    //knifes
    public static final DeferredItem<SwordItem> CARBON_STEEL_KNIFE = createSwordItemWithRarityAndDescriptionBeingFireResistent("knife", ModToolTiers.CARBON_STEEL, Rarity.UNCOMMON, DEFAULT_KNIFE_DAMAGE, DEFAULT_KNIFE_SPEED, "tooltips.forgermod.knife.tooltip");
    public static final DeferredItem<SwordItem> DAMASK_KNIFE = createSwordItem("damask_knife", ModToolTiers.DAMASK, KNIFE_DAMASK_DAMAGE, KNIFE_DAMASK_SPEED);
    public static final DeferredItem<SwordItem> RUSTY_KNIFE = createSwordItemWithRarityAndDescription("rusty_knife", ModToolTiers.CARBON_STEEL, Rarity.UNCOMMON, DEFAULT_RUSTY_KNIFE_DAMAGE, DEFAULT_RUSTY_KNIFE_SPEED, "tooltips.forgermod.rusty_knife.tooltip");
    public static final DeferredItem<SwordItem> OVERGROWN_KNIFE = createSpecialSwordItem("nature_knife", () -> new LushWeapon("no_gemstone", "knife"));
    public static final DeferredItem<SwordItem> HOLLOW_KNIFE = createSpecialSwordItem("hollow_knife", () -> new MorsiumWeapon("no_gemstone", "knife"));
    public static final DeferredItem<SwordItem> INFERNAL_KNIFE = createSpecialSwordItem("infernal_knife", () -> new IgnisiumWeapon("no_gemstone", "knife"));
    public static final DeferredItem<SwordItem> KNIFE_OF_THE_VOID = createSpecialSwordItem("knife_of_the_void", () -> new InanisiumWeapon("no_gemstone", "knife"));
    public static final DeferredItem<SwordItem> CURSEBLOOD_KNIFE = createSpecialSwordItem("curseblood_knife", () -> new VulnusiumWeapon("no_gemstone", "knife"));
    public static final DeferredItem<SwordItem> DREAMBOUND_KNIFE = createSpecialSwordItem("dreambound_knife", () -> new SomniumWeapon("no_gemstone", "knife"));
    public static final DeferredItem<SwordItem> SHRIEKING_KNIFE = createSpecialSwordItem("shrieking_knife", () -> new PulsiteWeapon("no_gemstone", "knife"));
    public static final DeferredItem<SwordItem> KNIFE_OF_THUNDER = createSpecialSwordItem("knife_of_thunder", () -> new ElectriumWeapon("no_gemstone", "knife"));
    public static final DeferredItem<SwordItem> STORMING_KNIFE = createSpecialSwordItem("storming_knife", () -> new TaifuniteWeapon("no_gemstone", "knife"));

    //other weapons
    public static final DeferredItem<SwordItem> STUMPFL_BAT = createSwordItemWithDescription("stumpfl_bat", ModToolTiers.DEVELOPIUM, 7770, 1f, "tooltips.forgermod.stumpfl_bat.tooltip");

    //gemstone infused variants
    public static final DeferredItem<SwordItem> CLAYMORE_OF_THUNDER_RUBY = createSpecialSwordItem("claymore_of_thunder_ruby", () -> new ElectriumWeapon("ruby", "claymore"));
    public static final DeferredItem<SwordItem> CLAYMORE_OF_THUNDER_AMBER = createSpecialSwordItem("claymore_of_thunder_amber", () -> new ElectriumWeapon("amber", "claymore"));
    public static final DeferredItem<SwordItem> CLAYMORE_OF_THUNDER_AMETHYST = createSpecialSwordItem("claymore_of_thunder_amethyst", () -> new ElectriumWeapon("amethyst", "claymore"));
    public static final DeferredItem<SwordItem> CLAYMORE_OF_THUNDER_JADE = createSpecialSwordItem("claymore_of_thunder_jade", () -> new ElectriumWeapon("jade", "claymore"));

    public static final DeferredItem<SwordItem> SHRIEKING_CLAYMORE_RUBY = createSpecialSwordItem("shrieking_claymore_ruby", () -> new PulsiteWeapon("ruby", "claymore"));
    public static final DeferredItem<SwordItem> SHRIEKING_CLAYMORE_AMBER = createSpecialSwordItem("shrieking_claymore_amber", () -> new PulsiteWeapon("amber", "claymore"));
    public static final DeferredItem<SwordItem> SHRIEKING_CLAYMORE_AMETHYST = createSpecialSwordItem("shrieking_claymore_amethyst", () -> new PulsiteWeapon("amethyst", "claymore"));
    public static final DeferredItem<SwordItem> SHRIEKING_CLAYMORE_JADE = createSpecialSwordItem("shrieking_claymore_jade", () -> new PulsiteWeapon("jade", "claymore"));

    public static final DeferredItem<SwordItem> DREAMBOUND_CLAYMORE_RUBY = createSpecialSwordItem("dreambound_claymore_ruby", () -> new SomniumWeapon("ruby", "claymore"));
    public static final DeferredItem<SwordItem> DREAMBOUND_CLAYMORE_AMBER = createSpecialSwordItem("dreambound_claymore_amber", () -> new SomniumWeapon("amber", "claymore"));
    public static final DeferredItem<SwordItem> DREAMBOUND_CLAYMORE_AMETHYST = createSpecialSwordItem("dreambound_claymore_amethyst", () -> new SomniumWeapon("amethyst", "claymore"));
    public static final DeferredItem<SwordItem> DREAMBOUND_CLAYMORE_JADE = createSpecialSwordItem("dreambound_claymore_jade", () -> new SomniumWeapon("jade", "claymore"));

    public static final DeferredItem<SwordItem> CURSEBLOOD_CLAYMORE_RUBY = createSpecialSwordItem("curseblood_claymore_ruby", () -> new VulnusiumWeapon("ruby", "claymore"));
    public static final DeferredItem<SwordItem> CURSEBLOOD_CLAYMORE_AMBER = createSpecialSwordItem("curseblood_claymore_amber", () -> new VulnusiumWeapon("amber", "claymore"));
    public static final DeferredItem<SwordItem> CURSEBLOOD_CLAYMORE_AMETHYST = createSpecialSwordItem("curseblood_claymore_amethyst", () -> new VulnusiumWeapon("amethyst", "claymore"));
    public static final DeferredItem<SwordItem> CURSEBLOOD_CLAYMORE_JADE = createSpecialSwordItem("curseblood_claymore_jade",() -> new VulnusiumWeapon("jade", "claymore"));

    public static final DeferredItem<SwordItem> CLAYMORE_OF_THE_VOID_RUBY = createSpecialSwordItem("claymore_of_the_void_ruby",() -> new InanisiumWeapon("ruby", "claymore"));
    public static final DeferredItem<SwordItem> CLAYMORE_OF_THE_VOID_AMBER = createSpecialSwordItem("claymore_of_the_void_amber",() -> new InanisiumWeapon("amber", "claymore"));
    public static final DeferredItem<SwordItem> CLAYMORE_OF_THE_VOID_AMETHYST = createSpecialSwordItem("claymore_of_the_void_amethyst",() -> new InanisiumWeapon("amethyst", "claymore"));
    public static final DeferredItem<SwordItem> CLAYMORE_OF_THE_VOID_JADE = createSpecialSwordItem("claymore_of_the_void_jade",() -> new InanisiumWeapon("jade", "claymore"));

    public static final DeferredItem<SwordItem> INFERNAL_CLAYMORE_RUBY = createSpecialSwordItem("infernal_claymore_ruby",() -> new IgnisiumWeapon("ruby", "claymore"));
    public static final DeferredItem<SwordItem> INFERNAL_CLAYMORE_AMBER = createSpecialSwordItem("infernal_claymore_amber",() -> new IgnisiumWeapon("amber", "claymore"));
    public static final DeferredItem<SwordItem> INFERNAL_CLAYMORE_AMETHYST = createSpecialSwordItem("infernal_claymore_amethyst",() -> new IgnisiumWeapon("amethyst", "claymore"));
    public static final DeferredItem<SwordItem> INFERNAL_CLAYMORE_JADE = createSpecialSwordItem("infernal_claymore_jade",() -> new IgnisiumWeapon("jade", "claymore"));

    public static final DeferredItem<SwordItem> HOLLOW_CLAYMORE_RUBY = createSpecialSwordItem("hollow_claymore_ruby",() -> new MorsiumWeapon("ruby", "claymore"));
    public static final DeferredItem<SwordItem> HOLLOW_CLAYMORE_AMBER = createSpecialSwordItem("hollow_claymore_amber",() -> new MorsiumWeapon("amber", "claymore"));
    public static final DeferredItem<SwordItem> HOLLOW_CLAYMORE_AMETHYST = createSpecialSwordItem("hollow_claymore_amethyst",() -> new MorsiumWeapon("amethyst", "claymore"));
    public static final DeferredItem<SwordItem> HOLLOW_CLAYMORE_JADE = createSpecialSwordItem("hollow_claymore_jade",() -> new MorsiumWeapon("jade", "claymore"));

    public static final DeferredItem<SwordItem> STORMING_CLAYMORE_RUBY = createSpecialSwordItem("storming_claymore_ruby",() -> new TaifuniteWeapon("ruby", "claymore"));
    public static final DeferredItem<SwordItem> STORMING_CLAYMORE_AMBER = createSpecialSwordItem("storming_claymore_amber",() -> new TaifuniteWeapon("amber", "claymore"));
    public static final DeferredItem<SwordItem> STORMING_CLAYMORE_AMETHYST = createSpecialSwordItem("storming_claymore_amethyst",() -> new TaifuniteWeapon("amethyst", "claymore"));
    public static final DeferredItem<SwordItem> STORMING_CLAYMORE_JADE = createSpecialSwordItem("storming_claymore_jade",() -> new TaifuniteWeapon("jade", "claymore"));

    public static final DeferredItem<SwordItem> OVERGROWN_CLAYMORE_RUBY = createSpecialSwordItem("overgrown_claymore_ruby",() -> new LushWeapon("ruby", "claymore"));
    public static final DeferredItem<SwordItem> OVERGROWN_CLAYMORE_AMBER = createSpecialSwordItem("overgrown_claymore_amber",() -> new LushWeapon("amber", "claymore"));
    public static final DeferredItem<SwordItem> OVERGROWN_CLAYMORE_AMETHYST = createSpecialSwordItem("overgrown_claymore_amethyst",() -> new LushWeapon("amethyst", "claymore"));
    public static final DeferredItem<SwordItem> OVERGROWN_CLAYMORE_JADE = createSpecialSwordItem("overgrown_claymore_jade",() -> new LushWeapon("jade", "claymore"));

    public static final DeferredItem<SwordItem> AXE_OF_THUNDER_RUBY = createSpecialSwordItem("axe_of_thunder_ruby", () -> new ElectriumWeapon("ruby", "axe"));
    public static final DeferredItem<SwordItem> AXE_OF_THUNDER_AMBER = createSpecialSwordItem("axe_of_thunder_amber", () -> new ElectriumWeapon("amber", "axe"));
    public static final DeferredItem<SwordItem> AXE_OF_THUNDER_AMETHYST = createSpecialSwordItem("axe_of_thunder_amethyst", () -> new ElectriumWeapon("amethyst", "axe"));
    public static final DeferredItem<SwordItem> AXE_OF_THUNDER_JADE = createSpecialSwordItem("axe_of_thunder_jade", () -> new ElectriumWeapon("jade", "axe"));

    public static final DeferredItem<SwordItem> SHRIEKING_AXE_RUBY = createSpecialSwordItem("shrieking_axe_ruby", () -> new PulsiteWeapon("ruby", "axe"));
    public static final DeferredItem<SwordItem> SHRIEKING_AXE_AMBER = createSpecialSwordItem("shrieking_axe_amber", () -> new PulsiteWeapon("amber", "axe"));
    public static final DeferredItem<SwordItem> SHRIEKING_AXE_AMETHYST = createSpecialSwordItem("shrieking_axe_amethyst", () -> new PulsiteWeapon("amethyst", "axe"));
    public static final DeferredItem<SwordItem> SHRIEKING_AXE_JADE = createSpecialSwordItem("shrieking_axe_jade", () -> new PulsiteWeapon("jade", "axe"));

    public static final DeferredItem<SwordItem> DREAMBOUND_AXE_RUBY = createSpecialSwordItem("dreambound_axe_ruby", () -> new SomniumWeapon("ruby", "axe"));
    public static final DeferredItem<SwordItem> DREAMBOUND_AXE_AMBER = createSpecialSwordItem("dreambound_axe_amber", () -> new SomniumWeapon("amber", "axe"));
    public static final DeferredItem<SwordItem> DREAMBOUND_AXE_AMETHYST = createSpecialSwordItem("dreambound_axe_amethyst", () -> new SomniumWeapon("amethyst", "axe"));
    public static final DeferredItem<SwordItem> DREAMBOUND_AXE_JADE = createSpecialSwordItem("dreambound_axe_jade", () -> new SomniumWeapon("jade", "axe"));

    public static final DeferredItem<SwordItem> CURSEBLOOD_AXE_RUBY = createSpecialSwordItem("curseblood_axe_ruby", () -> new VulnusiumWeapon("ruby", "axe"));
    public static final DeferredItem<SwordItem> CURSEBLOOD_AXE_AMBER = createSpecialSwordItem("curseblood_axe_amber", () -> new VulnusiumWeapon("amber", "axe"));
    public static final DeferredItem<SwordItem> CURSEBLOOD_AXE_AMETHYST = createSpecialSwordItem("curseblood_axe_amethyst", () -> new VulnusiumWeapon("amethyst", "axe"));
    public static final DeferredItem<SwordItem> CURSEBLOOD_AXE_JADE = createSpecialSwordItem("curseblood_axe_jade",() -> new VulnusiumWeapon("jade", "axe"));

    public static final DeferredItem<SwordItem> AXE_OF_THE_VOID_RUBY = createSpecialSwordItem("axe_of_the_void_ruby",() -> new InanisiumWeapon("ruby", "axe"));
    public static final DeferredItem<SwordItem> AXE_OF_THE_VOID_AMBER = createSpecialSwordItem("axe_of_the_void_amber",() -> new InanisiumWeapon("amber", "axe"));
    public static final DeferredItem<SwordItem> AXE_OF_THE_VOID_AMETHYST = createSpecialSwordItem("axe_of_the_void_amethyst",() -> new InanisiumWeapon("amethyst", "axe"));
    public static final DeferredItem<SwordItem> AXE_OF_THE_VOID_JADE = createSpecialSwordItem("axe_of_the_void_jade",() -> new InanisiumWeapon("jade", "axe"));

    public static final DeferredItem<SwordItem> INFERNAL_AXE_RUBY = createSpecialSwordItem("infernal_axe_ruby",() -> new IgnisiumWeapon("ruby", "axe"));
    public static final DeferredItem<SwordItem> INFERNAL_AXE_AMBER = createSpecialSwordItem("infernal_axe_amber",() -> new IgnisiumWeapon("amber", "axe"));
    public static final DeferredItem<SwordItem> INFERNAL_AXE_AMETHYST = createSpecialSwordItem("infernal_axe_amethyst",() -> new IgnisiumWeapon("amethyst", "axe"));
    public static final DeferredItem<SwordItem> INFERNAL_AXE_JADE = createSpecialSwordItem("infernal_axe_jade",() -> new IgnisiumWeapon("jade", "axe"));

    public static final DeferredItem<SwordItem> HOLLOW_AXE_RUBY = createSpecialSwordItem("hollow_axe_ruby",() -> new MorsiumWeapon("ruby", "axe"));
    public static final DeferredItem<SwordItem> HOLLOW_AXE_AMBER = createSpecialSwordItem("hollow_axe_amber",() -> new MorsiumWeapon("amber", "axe"));
    public static final DeferredItem<SwordItem> HOLLOW_AXE_AMETHYST = createSpecialSwordItem("hollow_axe_amethyst",() -> new MorsiumWeapon("amethyst", "axe"));
    public static final DeferredItem<SwordItem> HOLLOW_AXE_JADE = createSpecialSwordItem("hollow_axe_jade",() -> new MorsiumWeapon("jade", "axe"));

    public static final DeferredItem<SwordItem> STORMING_AXE_RUBY = createSpecialSwordItem("storming_axe_ruby",() -> new TaifuniteWeapon("ruby", "axe"));
    public static final DeferredItem<SwordItem> STORMING_AXE_AMBER = createSpecialSwordItem("storming_axe_amber",() -> new TaifuniteWeapon("amber", "axe"));
    public static final DeferredItem<SwordItem> STORMING_AXE_AMETHYST = createSpecialSwordItem("storming_axe_amethyst",() -> new TaifuniteWeapon("amethyst", "axe"));
    public static final DeferredItem<SwordItem> STORMING_AXE_JADE = createSpecialSwordItem("storming_axe_jade",() -> new TaifuniteWeapon("jade", "axe"));

    public static final DeferredItem<SwordItem> OVERGROWN_AXE_RUBY = createSpecialSwordItem("overgrown_axe_ruby",() -> new LushWeapon("ruby", "axe"));
    public static final DeferredItem<SwordItem> OVERGROWN_AXE_AMBER = createSpecialSwordItem("overgrown_axe_amber",() -> new LushWeapon("amber", "axe"));
    public static final DeferredItem<SwordItem> OVERGROWN_AXE_AMETHYST = createSpecialSwordItem("overgrown_axe_amethyst",() -> new LushWeapon("amethyst", "axe"));
    public static final DeferredItem<SwordItem> OVERGROWN_AXE_JADE = createSpecialSwordItem("overgrown_axe_jade",() -> new LushWeapon("jade", "axe"));

    public static final DeferredItem<SwordItem> KNIFE_OF_THUNDER_RUBY = createSpecialSwordItem("knife_of_thunder_ruby", () -> new ElectriumWeapon("ruby", "knife"));
    public static final DeferredItem<SwordItem> KNIFE_OF_THUNDER_AMBER = createSpecialSwordItem("knife_of_thunder_amber", () -> new ElectriumWeapon("amber", "knife"));
    public static final DeferredItem<SwordItem> KNIFE_OF_THUNDER_AMETHYST = createSpecialSwordItem("knife_of_thunder_amethyst", () -> new ElectriumWeapon("amethyst", "knife"));
    public static final DeferredItem<SwordItem> KNIFE_OF_THUNDER_JADE = createSpecialSwordItem("knife_of_thunder_jade", () -> new ElectriumWeapon("jade", "knife"));

    public static final DeferredItem<SwordItem> SHRIEKING_KNIFE_RUBY = createSpecialSwordItem("shrieking_knife_ruby", () -> new PulsiteWeapon("ruby", "knife"));
    public static final DeferredItem<SwordItem> SHRIEKING_KNIFE_AMBER = createSpecialSwordItem("shrieking_knife_amber", () -> new PulsiteWeapon("amber", "knife"));
    public static final DeferredItem<SwordItem> SHRIEKING_KNIFE_AMETHYST = createSpecialSwordItem("shrieking_knife_amethyst", () -> new PulsiteWeapon("amethyst", "knife"));
    public static final DeferredItem<SwordItem> SHRIEKING_KNIFE_JADE = createSpecialSwordItem("shrieking_knife_jade", () -> new PulsiteWeapon("jade", "knife"));

    public static final DeferredItem<SwordItem> DREAMBOUND_KNIFE_RUBY = createSpecialSwordItem("dreambound_knife_ruby", () -> new SomniumWeapon("ruby", "knife"));
    public static final DeferredItem<SwordItem> DREAMBOUND_KNIFE_AMBER = createSpecialSwordItem("dreambound_knife_amber", () -> new SomniumWeapon("amber", "knife"));
    public static final DeferredItem<SwordItem> DREAMBOUND_KNIFE_AMETHYST = createSpecialSwordItem("dreambound_knife_amethyst", () -> new SomniumWeapon("amethyst", "knife"));
    public static final DeferredItem<SwordItem> DREAMBOUND_KNIFE_JADE = createSpecialSwordItem("dreambound_knife_jade", () -> new SomniumWeapon("jade", "knife"));

    public static final DeferredItem<SwordItem> CURSEBLOOD_KNIFE_RUBY = createSpecialSwordItem("curseblood_knife_ruby", () -> new VulnusiumWeapon("ruby", "knife"));
    public static final DeferredItem<SwordItem> CURSEBLOOD_KNIFE_AMBER = createSpecialSwordItem("curseblood_knife_amber", () -> new VulnusiumWeapon("amber", "knife"));
    public static final DeferredItem<SwordItem> CURSEBLOOD_KNIFE_AMETHYST = createSpecialSwordItem("curseblood_knife_amethyst", () -> new VulnusiumWeapon("amethyst", "knife"));
    public static final DeferredItem<SwordItem> CURSEBLOOD_KNIFE_JADE = createSpecialSwordItem("curseblood_knife_jade",() -> new VulnusiumWeapon("jade", "knife"));

    public static final DeferredItem<SwordItem> KNIFE_OF_THE_VOID_RUBY = createSpecialSwordItem("knife_of_the_void_ruby",() -> new InanisiumWeapon("ruby", "knife"));
    public static final DeferredItem<SwordItem> KNIFE_OF_THE_VOID_AMBER = createSpecialSwordItem("knife_of_the_void_amber",() -> new InanisiumWeapon("amber", "knife"));
    public static final DeferredItem<SwordItem> KNIFE_OF_THE_VOID_AMETHYST = createSpecialSwordItem("knife_of_the_void_amethyst",() -> new InanisiumWeapon("amethyst", "knife"));
    public static final DeferredItem<SwordItem> KNIFE_OF_THE_VOID_JADE = createSpecialSwordItem("knife_of_the_void_jade",() -> new InanisiumWeapon("jade", "knife"));

    public static final DeferredItem<SwordItem> INFERNAL_KNIFE_RUBY = createSpecialSwordItem("infernal_knife_ruby",() -> new IgnisiumWeapon("ruby", "knife"));
    public static final DeferredItem<SwordItem> INFERNAL_KNIFE_AMBER = createSpecialSwordItem("infernal_knife_amber",() -> new IgnisiumWeapon("amber", "knife"));
    public static final DeferredItem<SwordItem> INFERNAL_KNIFE_AMETHYST = createSpecialSwordItem("infernal_knife_amethyst",() -> new IgnisiumWeapon("amethyst", "knife"));
    public static final DeferredItem<SwordItem> INFERNAL_KNIFE_JADE = createSpecialSwordItem("infernal_knife_jade",() -> new IgnisiumWeapon("jade", "knife"));

    public static final DeferredItem<SwordItem> HOLLOW_KNIFE_RUBY = createSpecialSwordItem("hollow_knife_ruby",() -> new MorsiumWeapon("ruby", "knife"));
    public static final DeferredItem<SwordItem> HOLLOW_KNIFE_AMBER = createSpecialSwordItem("hollow_knife_amber",() -> new MorsiumWeapon("amber", "knife"));
    public static final DeferredItem<SwordItem> HOLLOW_KNIFE_AMETHYST = createSpecialSwordItem("hollow_knife_amethyst",() -> new MorsiumWeapon("amethyst", "knife"));
    public static final DeferredItem<SwordItem> HOLLOW_KNIFE_JADE = createSpecialSwordItem("hollow_knife_jade",() -> new MorsiumWeapon("jade", "knife"));

    public static final DeferredItem<SwordItem> STORMING_KNIFE_RUBY = createSpecialSwordItem("storming_knife_ruby",() -> new TaifuniteWeapon("ruby", "knife"));
    public static final DeferredItem<SwordItem> STORMING_KNIFE_AMBER = createSpecialSwordItem("storming_knife_amber",() -> new TaifuniteWeapon("amber", "knife"));
    public static final DeferredItem<SwordItem> STORMING_KNIFE_AMETHYST = createSpecialSwordItem("storming_knife_amethyst",() -> new TaifuniteWeapon("amethyst", "knife"));
    public static final DeferredItem<SwordItem> STORMING_KNIFE_JADE = createSpecialSwordItem("storming_knife_jade",() -> new TaifuniteWeapon("jade", "knife"));

    public static final DeferredItem<SwordItem> OVERGROWN_KNIFE_RUBY = createSpecialSwordItem("overgrown_knife_ruby",() -> new LushWeapon("ruby", "knife"));
    public static final DeferredItem<SwordItem> OVERGROWN_KNIFE_AMBER = createSpecialSwordItem("overgrown_knife_amber",() -> new LushWeapon("amber", "knife"));
    public static final DeferredItem<SwordItem> OVERGROWN_KNIFE_AMETHYST = createSpecialSwordItem("overgrown_knife_amethyst",() -> new LushWeapon("amethyst", "knife"));
    public static final DeferredItem<SwordItem> OVERGROWN_KNIFE_JADE = createSpecialSwordItem("overgrown_knife_jade",() -> new LushWeapon("jade", "knife"));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}