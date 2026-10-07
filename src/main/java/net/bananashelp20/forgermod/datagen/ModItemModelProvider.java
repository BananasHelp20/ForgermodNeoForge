package net.bananashelp20.forgermod.datagen;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.item.ModItems;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.generators.ItemModelBuilder;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredItem;

public class ModItemModelProvider extends ItemModelProvider {
    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, ForgerMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        //ingredients
        basicItem(ModItems.MORSIUM_SHARD.get());
        basicItem(ModItems.IGNISIUM_SHARD.get());
        basicItem(ModItems.INANISIUM_SHARD.get());
        basicItem(ModItems.LUSH_SHARD.get());
        basicItem(ModItems.PULSITE_SHARD.get());
        basicItem(ModItems.SOMNIUM_SHARD.get());
        basicItem(ModItems.VULNUSIUM_SHARD.get());
        basicItem(ModItems.DAMASK_INGOT.get());
        basicItem(ModItems.SCRAP_INGOT.get());
        basicItem(ModItems.INANISIUM_INGOT.get());
        basicItem(ModItems.IGNISIUM_INGOT.get());
        basicItem(ModItems.SCRAP_IRON_INGOT.get());
        basicItem(ModItems.REINFORCED_IRON_INGOT.get());
        basicItem(ModItems.LUSH_INGOT.get());
        basicItem(ModItems.MORSIUM_INGOT.get());
        basicItem(ModItems.SOMNIUM_INGOT.get());
        basicItem(ModItems.VULNUSIUM_INGOT.get());
        basicItem(ModItems.PULSITE_INGOT.get());
        basicItem(ModItems.STEEL_INGOT.get());
        basicItem(ModItems.CARBON_STEEL_INGOT.get());
        basicItem(ModItems.UNREFINED_CARBON_STEEL.get());
        basicItem(ModItems.UNREFINED_STEEL.get());
        basicItem(ModItems.ELECTRIUM_INGOT.get());
        basicItem(ModItems.ELECTRIUM_SHARD.get());
        basicItem(ModItems.TAIFUNITE_INGOT.get());
        basicItem(ModItems.AMBER_GEMSTONE.get());
        basicItem(ModItems.AMETHYST_GEMSTONE.get());
        basicItem(ModItems.RUBY_GEMSTONE.get());
        basicItem(ModItems.TAIFUNITE_SHARD.get());
        basicItem(ModItems.DEVELOPIUM_INGOT.get());
        basicItem(ModItems.DEVELOPIUM_SHARD.get());
        basicItem(ModItems.GEMSTONE_UPGRADE_TEMPLATE.get());
        basicItem(ModItems.JADE_GEMSTONE.get());

        //weapons
        handheldItem(ModItems.CLAYMORE_OF_THE_VOID);
        handheldItem(ModItems.CLAYMORE_OF_THE_VOID_JADE);
        handheldItem(ModItems.CLAYMORE_OF_THE_VOID_RUBY);
        handheldItem(ModItems.CLAYMORE_OF_THE_VOID_AMBER);
        handheldItem(ModItems.CLAYMORE_OF_THE_VOID_AMETHYST);
        handheldItem(ModItems.INFERNAL_CLAYMORE);
        handheldItem(ModItems.INFERNAL_CLAYMORE_JADE);
        handheldItem(ModItems.INFERNAL_CLAYMORE_RUBY);
        handheldItem(ModItems.INFERNAL_CLAYMORE_AMBER);
        handheldItem(ModItems.INFERNAL_CLAYMORE_AMETHYST);
        handheldItem(ModItems.SCRAP_IRON_SWORD);
        handheldItem(ModItems.REINFORCED_IRON_SWORD);
        handheldItem(ModItems.CLAYMORE);
        handheldItem(ModItems.DAMASK_KNIFE);
        handheldItem(ModItems.OVERGROWN_CLAYMORE);
        handheldItem(ModItems.OVERGROWN_CLAYMORE_JADE);
        handheldItem(ModItems.OVERGROWN_CLAYMORE_RUBY);
        handheldItem(ModItems.OVERGROWN_CLAYMORE_AMBER);
        handheldItem(ModItems.OVERGROWN_CLAYMORE_AMETHYST);
        handheldItem(ModItems.HOLLOW_CLAYMORE);
        handheldItem(ModItems.HOLLOW_CLAYMORE_JADE);
        handheldItem(ModItems.HOLLOW_CLAYMORE_RUBY);
        handheldItem(ModItems.HOLLOW_CLAYMORE_AMBER);
        handheldItem(ModItems.HOLLOW_CLAYMORE_AMETHYST);
        handheldItem(ModItems.DREAMBOUND_CLAYMORE);
        handheldItem(ModItems.DREAMBOUND_CLAYMORE_JADE);
        handheldItem(ModItems.DREAMBOUND_CLAYMORE_RUBY);
        handheldItem(ModItems.DREAMBOUND_CLAYMORE_AMBER);
        handheldItem(ModItems.DREAMBOUND_CLAYMORE_AMETHYST);
        handheldItem(ModItems.CURSEBLOOD_CLAYMORE);
        handheldItem(ModItems.CURSEBLOOD_CLAYMORE_JADE);
        handheldItem(ModItems.CURSEBLOOD_CLAYMORE_RUBY);
        handheldItem(ModItems.CURSEBLOOD_CLAYMORE_AMBER);
        handheldItem(ModItems.CURSEBLOOD_CLAYMORE_AMETHYST);
        handheldItem(ModItems.SHRIEKING_CLAYMORE);
        handheldItem(ModItems.SHRIEKING_CLAYMORE_JADE);
        handheldItem(ModItems.SHRIEKING_CLAYMORE_RUBY);
        handheldItem(ModItems.SHRIEKING_CLAYMORE_AMBER);
        handheldItem(ModItems.SHRIEKING_CLAYMORE_AMETHYST);
        handheldItem(ModItems.STORMING_CLAYMORE);
        handheldItem(ModItems.STORMING_CLAYMORE_JADE);
        handheldItem(ModItems.STORMING_CLAYMORE_RUBY);
        handheldItem(ModItems.STORMING_CLAYMORE_AMBER);
        handheldItem(ModItems.STORMING_CLAYMORE_AMETHYST);
        handheldItem(ModItems.CLAYMORE_OF_THUNDER);
        handheldItem(ModItems.CLAYMORE_OF_THUNDER_RUBY);
        handheldItem(ModItems.CLAYMORE_OF_THUNDER_AMBER);
        handheldItem(ModItems.CLAYMORE_OF_THUNDER_AMETHYST);
        handheldItem(ModItems.CLAYMORE_OF_THUNDER_JADE);
        handheldItem(ModItems.DAMASK_SWORD);
        handheldItem(ModItems.SCRAP_SWORD);
        handheldItem(ModItems.RUSTY_CLAYMORE);
        handheldItem(ModItems.STEEL_SWORD);
        handheldItem(ModItems.STUMPFL_BAT);
//        handheldItem(ModItems.CARBON_STEEL_AXE);
//        handheldItem(ModItems.VERDANT_AXE);
//        handheldItem(ModItems.VERDANT_AXE_JADE);
//        handheldItem(ModItems.VERDANT_AXE_RUBY);
//        handheldItem(ModItems.VERDANT_AXE_AMBER);
//        handheldItem(ModItems.VERDANT_AXE_AMETHYST);
//        handheldItem(ModItems.GHOST_AXE);
//        handheldItem(ModItems.GHOST_AXE_JADE);
//        handheldItem(ModItems.GHOST_AXE_RUBY);
//        handheldItem(ModItems.GHOST_AXE_AMBER);
//        handheldItem(ModItems.GHOST_AXE_AMETHYST);
//        handheldItem(ModItems.DREAMWEAVER_AXE);
//        handheldItem(ModItems.DREAMWEAVER_AXE_JADE);
//        handheldItem(ModItems.DREAMWEAVER_AXE_RUBY);
//        handheldItem(ModItems.DREAMWEAVER_AXE_AMBER);
//        handheldItem(ModItems.DREAMWEAVER_AXE_AMETHYST);
//        handheldItem(ModItems.WOUNDMAKER_AXE);
//        handheldItem(ModItems.WOUNDMAKER_AXE_JADE);
//        handheldItem(ModItems.WOUNDMAKER_AXE_RUBY);
//        handheldItem(ModItems.WOUNDMAKER_AXE_AMBER);
//        handheldItem(ModItems.WOUNDMAKER_AXE_AMETHYST);
//        handheldItem(ModItems.ECHOING_AXE);
//        handheldItem(ModItems.ECHOING_AXE_JADE);
//        handheldItem(ModItems.ECHOING_AXE_RUBY);
//        handheldItem(ModItems.ECHOING_AXE_AMBER);
//        handheldItem(ModItems.ECHOING_AXE_AMETHYST);
//        handheldItem(ModItems.SKYBREAKER_AXE);
//        handheldItem(ModItems.SKYBREAKER_AXE_JADE);
//        handheldItem(ModItems.SKYBREAKER_AXE_RUBY);
//        handheldItem(ModItems.SKYBREAKER_AXE_AMBER);
//        handheldItem(ModItems.SKYBREAKER_AXE_AMETHYST);
//        handheldItem(ModItems.VOLTAGE_AXE);
//        handheldItem(ModItems.VOLTAGE_AXE_RUBY);
//        handheldItem(ModItems.VOLTAGE_AXE_AMBER);
//        handheldItem(ModItems.VOLTAGE_AXE_AMETHYST);
//        handheldItem(ModItems.VOLTAGE_AXE_JADE);
//        handheldItem(ModItems.NULLIFIED_AXE);
//        handheldItem(ModItems.NULLIFIED_AXE_JADE);
//        handheldItem(ModItems.NULLIFIED_AXE_RUBY);
//        handheldItem(ModItems.NULLIFIED_AXE_AMBER);
//        handheldItem(ModItems.NULLIFIED_AXE_AMETHYST);
//        handheldItem(ModItems.MOLTEN_AXE);
//        handheldItem(ModItems.MOLTEN_AXE_JADE);
//        handheldItem(ModItems.MOLTEN_AXE_RUBY);
//        handheldItem(ModItems.MOLTEN_AXE_AMBER);
//        handheldItem(ModItems.MOLTEN_AXE_AMETHYST);
//        handheldItem(ModItems.RUSTY_AXE);
//        handheldItem(ModItems.CARBON_STEEL_DAGGER);
//        handheldItem(ModItems.LEAFCUTTER_DAGGER);
//        handheldItem(ModItems.LEAFCUTTER_DAGGER_JADE);
//        handheldItem(ModItems.LEAFCUTTER_DAGGER_RUBY);
//        handheldItem(ModItems.LEAFCUTTER_DAGGER_AMBER);
//        handheldItem(ModItems.LEAFCUTTER_DAGGER_AMETHYST);
//        handheldItem(ModItems.DEATHWISPER_DAGGER);
//        handheldItem(ModItems.DEATHWISPER_DAGGER_JADE);
//        handheldItem(ModItems.DEATHWISPER_DAGGER_RUBY);
//        handheldItem(ModItems.DEATHWISPER_DAGGER_AMBER);
//        handheldItem(ModItems.DEATHWISPER_DAGGER_AMETHYST);
//        handheldItem(ModItems.NIGHTMARE_DAGGER);
//        handheldItem(ModItems.NIGHTMARE_DAGGER_JADE);
//        handheldItem(ModItems.NIGHTMARE_DAGGER_RUBY);
//        handheldItem(ModItems.NIGHTMARE_DAGGER_AMBER);
//        handheldItem(ModItems.NIGHTMARE_DAGGER_AMETHYST);
//        handheldItem(ModItems.ASSASSIN_DAGGER);
//        handheldItem(ModItems.ASSASSIN_DAGGER_JADE);
//        handheldItem(ModItems.ASSASSIN_DAGGER_RUBY);
//        handheldItem(ModItems.ASSASSIN_DAGGER_AMBER);
//        handheldItem(ModItems.ASSASSIN_DAGGER_AMETHYST);
//        handheldItem(ModItems.WARDENS_NEEDLE);
//        handheldItem(ModItems.WARDENS_NEEDLE_JADE);
//        handheldItem(ModItems.WARDENS_NEEDLE_RUBY);
//        handheldItem(ModItems.WARDENS_NEEDLE_AMBER);
//        handheldItem(ModItems.WARDENS_NEEDLE_AMETHYST);
//        handheldItem(ModItems.DEAD_CALM_DAGGER);
//        handheldItem(ModItems.DEAD_CALM_DAGGER_JADE);
//        handheldItem(ModItems.DEAD_CALM_DAGGER_RUBY);
//        handheldItem(ModItems.DEAD_CALM_DAGGER_AMBER);
//        handheldItem(ModItems.DEAD_CALM_DAGGER_AMETHYST);
//        handheldItem(ModItems.STATIC_DAGGER);
//        handheldItem(ModItems.STATIC_DAGGER_RUBY);
//        handheldItem(ModItems.STATIC_DAGGER_AMBER);
//        handheldItem(ModItems.STATIC_DAGGER_AMETHYST);
//        handheldItem(ModItems.STATIC_DAGGER_JADE);
//        handheldItem(ModItems.RIFTFANG_DAGGER);
//        handheldItem(ModItems.RIFTFANG_DAGGER_JADE);
//        handheldItem(ModItems.RIFTFANG_DAGGER_RUBY);
//        handheldItem(ModItems.RIFTFANG_DAGGER_AMBER);
//        handheldItem(ModItems.RIFTFANG_DAGGER_AMETHYST);
//        handheldItem(ModItems.EMBERFANG_DAGGER);
//        handheldItem(ModItems.EMBERFANG_DAGGER_JADE);
//        handheldItem(ModItems.EMBERFANG_DAGGER_RUBY);
//        handheldItem(ModItems.EMBERFANG_DAGGER_AMBER);
//        handheldItem(ModItems.EMBERFANG_DAGGER_AMETHYST);
//        handheldItem(ModItems.RUSTY_DAGGER);

        //items
        basicItem(ModItems.CARBON_STEEL_CROSS_GUARD.get());
        handheldItem(ModItems.SHARPENED_BLADE);
        basicItem(ModItems.ADVANCED_HANDLE.get());
        basicItem(ModItems.HANDLE.get());
        basicItem(ModItems.ANCIENT_UPGRADE_TEMPLATE.get());
    }

    private ItemModelBuilder handheldItem(DeferredItem<?> item) {
        return withExistingParent(item.getId().getPath(), ResourceLocation.parse("item/handheld"))
                .texture("layer0", ResourceLocation.fromNamespaceAndPath(ForgerMod.MOD_ID, "item/" + item.getId().getPath()));
    }

    private ItemModelBuilder simpleBlockItem(DeferredItem<?> item) {
        return withExistingParent(item.getId().getPath(),
                ResourceLocation.parse("item/generated")).texture("layer0",
                ResourceLocation.fromNamespaceAndPath(ForgerMod.MOD_ID,"item/" + item.getId().getPath()));
    }
}