package net.bananashelp20.forgermod.CreativeModeTabs;

import net.bananashelp20.forgermod.block.ModBlocks;
import net.bananashelp20.forgermod.item.ModItems;
import net.minecraft.world.level.ItemLike;

public class RegistryClass {
    public static ItemLike getDisplayItemForForgerIngredientsTab() {
        return ModItems.INANISIUM_INGOT.get();
    }
    public static ItemLike getDisplayItemForForgerWeaponsTab() {
        return ModItems.CLAYMORE.get();
    }
    public static ItemLike getDisplayItemForForgerItemsTab() {
        return ModItems.ADVANCED_HANDLE.get();
    }
    public static ItemLike getDisplayItemForForgerMiscellaneousTab() {
        return ModBlocks.FORGE.get();
    }
    public static ItemLike getDisplayItemForForgerBlocksTab() {
        return ModBlocks.RUBY_DEEPSLATE_ORE.get();
    }

    public static ItemLike[] getIngredientTabRegister() {
        return new ItemLike[] {
                ModBlocks.DAMASK_BLOCK.get(),
                ModItems.DAMASK_INGOT.get(),

                ModBlocks.SCRAP_BLOCK.get(),
                ModItems.SCRAP_INGOT.get(),

                ModBlocks.INANISIUM_BLOCK.get(),
                ModItems.INANISIUM_INGOT.get(),
                ModItems.INANISIUM_SHARD.get(),

                ModBlocks.IGNISIUM_BLOCK.get(),
                ModItems.IGNISIUM_INGOT.get(),
                ModItems.IGNISIUM_SHARD.get(),

                ModBlocks.PULSITE_BLOCK.get(),
                ModItems.PULSITE_INGOT.get(),
                ModItems.PULSITE_SHARD.get(),

                ModBlocks.SOMNIUM_BLOCK.get(),
                ModItems.SOMNIUM_INGOT.get(),
                ModItems.SOMNIUM_SHARD.get(),

                ModBlocks.VULNUSIUM_BLOCK.get(),
                ModItems.VULNUSIUM_INGOT.get(),
                ModItems.VULNUSIUM_SHARD.get(),

                ModBlocks.MORSIUM_BLOCK.get(),
                ModItems.MORSIUM_INGOT.get(),
                ModItems.MORSIUM_SHARD.get(),

                ModBlocks.OVERGROWN_BLOCK.get(),
                ModItems.LUSH_INGOT.get(),
                ModItems.LUSH_SHARD.get(),

                ModBlocks.ELECTRIUM_BLOCK.get(),
                ModItems.ELECTRIUM_INGOT.get(),
                ModItems.ELECTRIUM_SHARD.get(),

                ModBlocks.TAIFUNITE_BLOCK.get(),
                ModItems.TAIFUNITE_INGOT.get(),
                ModItems.TAIFUNITE_SHARD.get(),

                ModBlocks.CARBON_STEEL_BLOCK.get(),
                ModItems.CARBON_STEEL_INGOT.get(),
                ModItems.UNREFINED_CARBON_STEEL.get(),

                ModBlocks.STEEL_BLOCK.get(),
                ModItems.STEEL_INGOT.get(),
                ModItems.UNREFINED_STEEL.get(),

                ModBlocks.DEVELOPIUM_BLOCK.get(),
                ModItems.DEVELOPIUM_INGOT.get(),

                ModItems.RUBY_GEMSTONE.get(),
                ModItems.AMBER_GEMSTONE.get(),
                ModItems.AMETHYST_GEMSTONE.get(),
                ModItems.JADE_GEMSTONE.get()
        };
    }
    public static ItemLike[] getWeaponTabRegister() {
        return new ItemLike[] {
                ModItems.SCRAP_SWORD.get(),
                ModItems.DAMASK_KNIFE.get(),
                ModItems.DAMASK_SWORD.get(),
                ModItems.STEEL_SWORD.get(),
                ModItems.CLAYMORE.get(),
                ModItems.CARBON_STEEL_AXE.get(),
                ModItems.CARBON_STEEL_DAGGER.get(),
                ModItems.RUSTY_CLAYMORE.get(),
                ModItems.RUSTY_AXE.get(),
                ModItems.RUSTY_DAGGER.get(),
                ModItems.STUMPFL_BAT.get(),

                ModItems.INFERNAL_CLAYMORE.get(),
                ModItems.INFERNAL_CLAYMORE_RUBY.get(),
                ModItems.INFERNAL_CLAYMORE_AMBER.get(),
                ModItems.INFERNAL_CLAYMORE_AMETHYST.get(),
                ModItems.INFERNAL_CLAYMORE_JADE.get(),
                ModItems.MOLTEN_AXE.get(),
                ModItems.MOLTEN_AXE_RUBY.get(),
                ModItems.MOLTEN_AXE_AMBER.get(),
                ModItems.MOLTEN_AXE_AMETHYST.get(),
                ModItems.MOLTEN_AXE_JADE.get(),
                ModItems.EMBERFANG_DAGGER.get(),
                ModItems.EMBERFANG_DAGGER_RUBY.get(),
                ModItems.EMBERFANG_DAGGER_AMBER.get(),
                ModItems.EMBERFANG_DAGGER_AMETHYST.get(),
                ModItems.EMBERFANG_DAGGER_JADE.get(),

                ModItems.CLAYMORE_OF_THE_VOID.get(),
                ModItems.CLAYMORE_OF_THE_VOID_RUBY.get(),
                ModItems.CLAYMORE_OF_THE_VOID_AMBER.get(),
                ModItems.CLAYMORE_OF_THE_VOID_AMETHYST.get(),
                ModItems.CLAYMORE_OF_THE_VOID_JADE.get(),
                ModItems.NULLIFIED_AXE.get(),
                ModItems.NULLIFIED_AXE_RUBY.get(),
                ModItems.NULLIFIED_AXE_AMBER.get(),
                ModItems.NULLIFIED_AXE_AMETHYST.get(),
                ModItems.NULLIFIED_AXE_JADE.get(),
                ModItems.RIFTFANG_DAGGER.get(),
                ModItems.RIFTFANG_DAGGER_RUBY.get(),
                ModItems.RIFTFANG_DAGGER_AMBER.get(),
                ModItems.RIFTFANG_DAGGER_AMETHYST.get(),
                ModItems.RIFTFANG_DAGGER_JADE.get(),

                ModItems.OVERGROWN_CLAYMORE.get(),
                ModItems.OVERGROWN_CLAYMORE_RUBY.get(),
                ModItems.OVERGROWN_CLAYMORE_AMBER.get(),
                ModItems.OVERGROWN_CLAYMORE_AMETHYST.get(),
                ModItems.OVERGROWN_CLAYMORE_JADE.get(),
                ModItems.VERDANT_AXE.get(),
                ModItems.VERDANT_AXE_RUBY.get(),
                ModItems.VERDANT_AXE_AMBER.get(),
                ModItems.VERDANT_AXE_AMETHYST.get(),
                ModItems.VERDANT_AXE_JADE.get(),
                ModItems.LEAFCUTTER_DAGGER.get(),
                ModItems.LEAFCUTTER_DAGGER_RUBY.get(),
                ModItems.LEAFCUTTER_DAGGER_AMBER.get(),
                ModItems.LEAFCUTTER_DAGGER_AMETHYST.get(),
                ModItems.LEAFCUTTER_DAGGER_JADE.get(),

                ModItems.HOLLOW_CLAYMORE.get(),
                ModItems.HOLLOW_CLAYMORE_RUBY.get(),
                ModItems.HOLLOW_CLAYMORE_AMBER.get(),
                ModItems.HOLLOW_CLAYMORE_AMETHYST.get(),
                ModItems.HOLLOW_CLAYMORE_JADE.get(),
                ModItems.GHOST_AXE.get(),
                ModItems.GHOST_AXE_RUBY.get(),
                ModItems.GHOST_AXE_AMBER.get(),
                ModItems.GHOST_AXE_AMETHYST.get(),
                ModItems.GHOST_AXE_JADE.get(),
                ModItems.DEATHWISPER_DAGGER.get(),
                ModItems.DEATHWISPER_DAGGER_RUBY.get(),
                ModItems.DEATHWISPER_DAGGER_AMBER.get(),
                ModItems.DEATHWISPER_DAGGER_AMETHYST.get(),
                ModItems.DEATHWISPER_DAGGER_JADE.get(),

                ModItems.CURSEBLOOD_CLAYMORE.get(),
                ModItems.CURSEBLOOD_CLAYMORE_RUBY.get(),
                ModItems.CURSEBLOOD_CLAYMORE_AMBER.get(),
                ModItems.CURSEBLOOD_CLAYMORE_AMETHYST.get(),
                ModItems.CURSEBLOOD_CLAYMORE_JADE.get(),
                ModItems.WOUNDMAKER_AXE.get(),
                ModItems.WOUNDMAKER_AXE_RUBY.get(),
                ModItems.WOUNDMAKER_AXE_AMBER.get(),
                ModItems.WOUNDMAKER_AXE_AMETHYST.get(),
                ModItems.WOUNDMAKER_AXE_JADE.get(),
                ModItems.ASSASSIN_DAGGER.get(),
                ModItems.ASSASSIN_DAGGER_RUBY.get(),
                ModItems.ASSASSIN_DAGGER_AMBER.get(),
                ModItems.ASSASSIN_DAGGER_AMETHYST.get(),
                ModItems.ASSASSIN_DAGGER_JADE.get(),

                ModItems.DREAMBOUND_CLAYMORE.get(),
                ModItems.DREAMBOUND_CLAYMORE_RUBY.get(),
                ModItems.DREAMBOUND_CLAYMORE_AMBER.get(),
                ModItems.DREAMBOUND_CLAYMORE_AMETHYST.get(),
                ModItems.DREAMBOUND_CLAYMORE_JADE.get(),
                ModItems.DREAMWEAVER_AXE.get(),
                ModItems.DREAMWEAVER_AXE_RUBY.get(),
                ModItems.DREAMWEAVER_AXE_AMBER.get(),
                ModItems.DREAMWEAVER_AXE_AMETHYST.get(),
                ModItems.DREAMWEAVER_AXE_JADE.get(),
                ModItems.NIGHTMARE_DAGGER.get(),
                ModItems.NIGHTMARE_DAGGER_RUBY.get(),
                ModItems.NIGHTMARE_DAGGER_AMBER.get(),
                ModItems.NIGHTMARE_DAGGER_AMETHYST.get(),
                ModItems.NIGHTMARE_DAGGER_JADE.get(),

                ModItems.SHRIEKING_CLAYMORE.get(),
                ModItems.SHRIEKING_CLAYMORE_RUBY.get(),
                ModItems.SHRIEKING_CLAYMORE_AMBER.get(),
                ModItems.SHRIEKING_CLAYMORE_AMETHYST.get(),
                ModItems.SHRIEKING_CLAYMORE_JADE.get(),
                ModItems.ECHOING_AXE.get(),
                ModItems.ECHOING_AXE_RUBY.get(),
                ModItems.ECHOING_AXE_AMBER.get(),
                ModItems.ECHOING_AXE_AMETHYST.get(),
                ModItems.ECHOING_AXE_JADE.get(),
                ModItems.WARDENS_NEEDLE.get(),
                ModItems.WARDENS_NEEDLE_RUBY.get(),
                ModItems.WARDENS_NEEDLE_AMBER.get(),
                ModItems.WARDENS_NEEDLE_AMETHYST.get(),
                ModItems.WARDENS_NEEDLE_JADE.get(),

                ModItems.CLAYMORE_OF_THUNDER.get(),
                ModItems.CLAYMORE_OF_THUNDER_RUBY.get(),
                ModItems.CLAYMORE_OF_THUNDER_AMBER.get(),
                ModItems.CLAYMORE_OF_THUNDER_AMETHYST.get(),
                ModItems.CLAYMORE_OF_THUNDER_JADE.get(),
                ModItems.VOLTAGE_AXE.get(),
                ModItems.VOLTAGE_AXE_RUBY.get(),
                ModItems.VOLTAGE_AXE_AMBER.get(),
                ModItems.VOLTAGE_AXE_AMETHYST.get(),
                ModItems.VOLTAGE_AXE_JADE.get(),
                ModItems.STATIC_DAGGER.get(),
                ModItems.STATIC_DAGGER_RUBY.get(),
                ModItems.STATIC_DAGGER_AMBER.get(),
                ModItems.STATIC_DAGGER_AMETHYST.get(),
                ModItems.STATIC_DAGGER_JADE.get(),

                ModItems.STORMING_CLAYMORE.get(),
                ModItems.STORMING_CLAYMORE_RUBY.get(),
                ModItems.STORMING_CLAYMORE_AMBER.get(),
                ModItems.STORMING_CLAYMORE_AMETHYST.get(),
                ModItems.STORMING_CLAYMORE_JADE.get(),
                ModItems.SKYBREAKER_AXE.get(),
                ModItems.SKYBREAKER_AXE_RUBY.get(),
                ModItems.SKYBREAKER_AXE_AMBER.get(),
                ModItems.SKYBREAKER_AXE_AMETHYST.get(),
                ModItems.SKYBREAKER_AXE_JADE.get(),
                ModItems.DEAD_CALM_DAGGER.get(),
                ModItems.DEAD_CALM_DAGGER_RUBY.get(),
                ModItems.DEAD_CALM_DAGGER_AMBER.get(),
                ModItems.DEAD_CALM_DAGGER_AMETHYST.get(),
                ModItems.DEAD_CALM_DAGGER_JADE.get(),
        };
    }
    public static ItemLike[] getItemTabRegister() {
        return new ItemLike[] {
                ModItems.CARBON_STEEL_CROSS_GUARD.get(),
                ModItems.SHARPENED_BLADE.get(),
                ModItems.HANDLE.get(),
                ModItems.ADVANCED_HANDLE.get(),
                ModItems.ANCIENT_UPGRADE_TEMPLATE.get(),
                ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()
        };
    }
    public static ItemLike[] getMiscellaneousTabRegister() {
        return new ItemLike[] {
                ModBlocks.ANCIENT_SWORD_STAND.get(),
                ModBlocks.FORGE.get(),
                ModBlocks.INFUSION_TABLE.get()
        };
    }
    public static ItemLike[] getBlocksTabRegister() {
        return new ItemLike[] {
                ModBlocks.JADE_STONE_ORE.get(),
                ModBlocks.JADE_DEEPSLATE_ORE.get(),
                ModBlocks.JADE_NETHER_ORE.get(),
                ModBlocks.JADE_END_ORE.get(),
                ModBlocks.JADE_OBSIDIAN_ORE.get(),

                ModBlocks.RUBY_STONE_ORE.get(),
                ModBlocks.RUBY_DEEPSLATE_ORE.get(),
                ModBlocks.RUBY_NETHER_ORE.get(),
                ModBlocks.RUBY_END_ORE.get(),
                ModBlocks.RUBY_OBSIDIAN_ORE.get(),

                ModBlocks.AMETHYST_STONE_ORE.get(),
                ModBlocks.AMETHYST_DEEPSLATE_ORE.get(),
                ModBlocks.AMETHYST_NETHER_ORE.get(),
                ModBlocks.AMETHYST_END_ORE.get(),
                ModBlocks.AMETHYST_OBSIDIAN_ORE.get(),

                ModBlocks.AMBER_STONE_ORE.get(),
                ModBlocks.AMBER_DEEPSLATE_ORE.get(),
                ModBlocks.AMBER_NETHER_ORE.get(),
                ModBlocks.AMBER_END_ORE.get(),
                ModBlocks.AMBER_OBSIDIAN_ORE.get(),

                ModBlocks.SHARDIUM_STONE_ORE.get(),
                ModBlocks.SHARDIUM_DEEPSLATE_ORE.get(),
                ModBlocks.SHARDIUM_NETHER_ORE.get(),
                ModBlocks.SHARDIUM_END_ORE.get(),
                ModBlocks.SHARDIUM_OBSIDIAN_ORE.get()
        };
    }
}
