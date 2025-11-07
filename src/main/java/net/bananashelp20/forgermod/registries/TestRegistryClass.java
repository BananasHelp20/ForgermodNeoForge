package net.bananashelp20.forgermod.registries;

import net.bananashelp20.forgermod.block.ModBlocks;
import net.bananashelp20.forgermod.item.ModItems;
import net.minecraft.world.level.ItemLike;

public class TestRegistryClass {
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

    //STARTGENERATING!
    public static ItemLike[] getIngredientsTabRegister() {
        return new ItemLike[] {
                ModBlocks.REINFORCED_IRON_BLOCK.get(),
                ModItems.REINFORCED_IRON_INGOT.get(),

                ModBlocks.SCRAP_IRON_BLOCK.get(),
                ModItems.SCRAP_IRON_INGOT.get(),

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
                ModBlocks.REINFORCED_IRON_BLOCK.get(),
                ModBlocks.REINFORCED_IRON_BLOCK.get(),
                ModBlocks.REINFORCED_IRON_BLOCK.get(),
                ModBlocks.REINFORCED_IRON_BLOCK.get(),
                ModItems.AMBER_GEMSTONE.get(),

                ModItems.ADVANCED_HANDLE.get()
        };
    }

    public static ItemLike[] getItemTabRegister() {
        return new ItemLike[] {
        };
    }

    public static ItemLike[] getMiscellaneousTabRegister() {
        return new ItemLike[] {
        };
    }

    public static ItemLike[] getBlocksTabRegister() {
        return new ItemLike[] {
        };
    }
}