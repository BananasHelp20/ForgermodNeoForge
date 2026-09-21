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
        handheldItem(ModItems.CARBON_STEEL_AXE);
        handheldItem(ModItems.OVERGROWN_AXE);
        handheldItem(ModItems.OVERGROWN_AXE_JADE);
        handheldItem(ModItems.OVERGROWN_AXE_RUBY);
        handheldItem(ModItems.OVERGROWN_AXE_AMBER);
        handheldItem(ModItems.OVERGROWN_AXE_AMETHYST);
        handheldItem(ModItems.HOLLOW_AXE);
        handheldItem(ModItems.HOLLOW_AXE_JADE);
        handheldItem(ModItems.HOLLOW_AXE_RUBY);
        handheldItem(ModItems.HOLLOW_AXE_AMBER);
        handheldItem(ModItems.HOLLOW_AXE_AMETHYST);
        handheldItem(ModItems.DREAMBOUND_AXE);
        handheldItem(ModItems.DREAMBOUND_AXE_JADE);
        handheldItem(ModItems.DREAMBOUND_AXE_RUBY);
        handheldItem(ModItems.DREAMBOUND_AXE_AMBER);
        handheldItem(ModItems.DREAMBOUND_AXE_AMETHYST);
        handheldItem(ModItems.CURSEBLOOD_AXE);
        handheldItem(ModItems.CURSEBLOOD_AXE_JADE);
        handheldItem(ModItems.CURSEBLOOD_AXE_RUBY);
        handheldItem(ModItems.CURSEBLOOD_AXE_AMBER);
        handheldItem(ModItems.CURSEBLOOD_AXE_AMETHYST);
        handheldItem(ModItems.SHRIEKING_AXE);
        handheldItem(ModItems.SHRIEKING_AXE_JADE);
        handheldItem(ModItems.SHRIEKING_AXE_RUBY);
        handheldItem(ModItems.SHRIEKING_AXE_AMBER);
        handheldItem(ModItems.SHRIEKING_AXE_AMETHYST);
        handheldItem(ModItems.STORMING_AXE);
        handheldItem(ModItems.STORMING_AXE_JADE);
        handheldItem(ModItems.STORMING_AXE_RUBY);
        handheldItem(ModItems.STORMING_AXE_AMBER);
        handheldItem(ModItems.STORMING_AXE_AMETHYST);
        handheldItem(ModItems.AXE_OF_THUNDER);
        handheldItem(ModItems.AXE_OF_THUNDER_RUBY);
        handheldItem(ModItems.AXE_OF_THUNDER_AMBER);
        handheldItem(ModItems.AXE_OF_THUNDER_AMETHYST);
        handheldItem(ModItems.AXE_OF_THUNDER_JADE);
        handheldItem(ModItems.AXE_OF_THE_VOID);
        handheldItem(ModItems.AXE_OF_THE_VOID_JADE);
        handheldItem(ModItems.AXE_OF_THE_VOID_RUBY);
        handheldItem(ModItems.AXE_OF_THE_VOID_AMBER);
        handheldItem(ModItems.AXE_OF_THE_VOID_AMETHYST);
        handheldItem(ModItems.INFERNAL_AXE);
        handheldItem(ModItems.INFERNAL_AXE_JADE);
        handheldItem(ModItems.INFERNAL_AXE_RUBY);
        handheldItem(ModItems.INFERNAL_AXE_AMBER);
        handheldItem(ModItems.INFERNAL_AXE_AMETHYST);
        handheldItem(ModItems.RUSTY_AXE);
        handheldItem(ModItems.CARBON_STEEL_KNIFE);
        handheldItem(ModItems.OVERGROWN_KNIFE);
        handheldItem(ModItems.OVERGROWN_KNIFE_JADE);
        handheldItem(ModItems.OVERGROWN_KNIFE_RUBY);
        handheldItem(ModItems.OVERGROWN_KNIFE_AMBER);
        handheldItem(ModItems.OVERGROWN_KNIFE_AMETHYST);
        handheldItem(ModItems.HOLLOW_KNIFE);
        handheldItem(ModItems.HOLLOW_KNIFE_JADE);
        handheldItem(ModItems.HOLLOW_KNIFE_RUBY);
        handheldItem(ModItems.HOLLOW_KNIFE_AMBER);
        handheldItem(ModItems.HOLLOW_KNIFE_AMETHYST);
        handheldItem(ModItems.DREAMBOUND_KNIFE);
        handheldItem(ModItems.DREAMBOUND_KNIFE_JADE);
        handheldItem(ModItems.DREAMBOUND_KNIFE_RUBY);
        handheldItem(ModItems.DREAMBOUND_KNIFE_AMBER);
        handheldItem(ModItems.DREAMBOUND_KNIFE_AMETHYST);
        handheldItem(ModItems.CURSEBLOOD_KNIFE);
        handheldItem(ModItems.CURSEBLOOD_KNIFE_JADE);
        handheldItem(ModItems.CURSEBLOOD_KNIFE_RUBY);
        handheldItem(ModItems.CURSEBLOOD_KNIFE_AMBER);
        handheldItem(ModItems.CURSEBLOOD_KNIFE_AMETHYST);
        handheldItem(ModItems.SHRIEKING_KNIFE);
        handheldItem(ModItems.SHRIEKING_KNIFE_JADE);
        handheldItem(ModItems.SHRIEKING_KNIFE_RUBY);
        handheldItem(ModItems.SHRIEKING_KNIFE_AMBER);
        handheldItem(ModItems.SHRIEKING_KNIFE_AMETHYST);
        handheldItem(ModItems.STORMING_KNIFE);
        handheldItem(ModItems.STORMING_KNIFE_JADE);
        handheldItem(ModItems.STORMING_KNIFE_RUBY);
        handheldItem(ModItems.STORMING_KNIFE_AMBER);
        handheldItem(ModItems.STORMING_KNIFE_AMETHYST);
        handheldItem(ModItems.KNIFE_OF_THUNDER);
        handheldItem(ModItems.KNIFE_OF_THUNDER_RUBY);
        handheldItem(ModItems.KNIFE_OF_THUNDER_AMBER);
        handheldItem(ModItems.KNIFE_OF_THUNDER_AMETHYST);
        handheldItem(ModItems.KNIFE_OF_THUNDER_JADE);
        handheldItem(ModItems.KNIFE_OF_THE_VOID);
        handheldItem(ModItems.KNIFE_OF_THE_VOID_JADE);
        handheldItem(ModItems.KNIFE_OF_THE_VOID_RUBY);
        handheldItem(ModItems.KNIFE_OF_THE_VOID_AMBER);
        handheldItem(ModItems.KNIFE_OF_THE_VOID_AMETHYST);
        handheldItem(ModItems.INFERNAL_KNIFE);
        handheldItem(ModItems.INFERNAL_KNIFE_JADE);
        handheldItem(ModItems.INFERNAL_KNIFE_RUBY);
        handheldItem(ModItems.INFERNAL_KNIFE_AMBER);
        handheldItem(ModItems.INFERNAL_KNIFE_AMETHYST);
        handheldItem(ModItems.RUSTY_KNIFE);

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