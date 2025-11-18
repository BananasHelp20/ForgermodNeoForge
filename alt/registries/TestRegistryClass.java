package net.bananashelp20.forgermod.registries;

import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.registries.test.ModBlocks;
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
        return ModBlocks.ABCBLOCK;
    }
    public static ItemLike getDisplayItemForForgerBlocksTab() {
        return ModBlocks.NEWBLOCK;
    }

    //STARTGENERATING!
    public static ItemLike[] getIngredientsTabRegister() {
        return new ItemLike[] {
                ModItems.REINFORCED_IRON_INGOT.get(),
                ModItems.SCRAP_IRON_INGOT.get(),
                ModItems.DAMASK_INGOT.get(),
                ModItems.SCRAP_INGOT.get(),
                ModItems.INANISIUM_INGOT.get(),
                ModItems.INANISIUM_SHARD.get(),
                ModItems.IGNISIUM_INGOT.get(),
                ModItems.IGNISIUM_SHARD.get(),
                ModItems.PULSITE_INGOT.get(),
                ModItems.PULSITE_SHARD.get(),
                ModItems.SOMNIUM_INGOT.get(),
                ModItems.SOMNIUM_SHARD.get(),
                ModItems.VULNUSIUM_INGOT.get(),
                ModItems.VULNUSIUM_SHARD.get(),
                ModItems.MORSIUM_INGOT.get(),
                ModItems.MORSIUM_SHARD.get(),
                ModItems.LUSH_INGOT.get(),
                ModItems.LUSH_SHARD.get(),
                ModItems.ELECTRIUM_INGOT.get(),
                ModItems.ELECTRIUM_SHARD.get(),
                ModItems.TAIFUNITE_INGOT.get(),
                ModItems.TAIFUNITE_SHARD.get(),
                ModItems.CARBON_STEEL_INGOT.get(),
                ModItems.UNREFINED_CARBON_STEEL.get(),
                ModItems.STEEL_INGOT.get(),
                ModItems.UNREFINED_STEEL.get(),
                ModItems.DEVELOPIUM_INGOT.get(),
                ModItems.RUBY_GEMSTONE.get(),
                ModItems.AMBER_GEMSTONE.get(),
                ModItems.AMETHYST_GEMSTONE.get(),
                ModItems.JADE_GEMSTONE.get()
        };
    }

    public static ItemLike[] getWeaponTabRegister() {
        return new ItemLike[] {
                ModBlocks.SIMPLEBLOCKTEST.get(),
                ModItems.AMBER_GEMSTONE.get(),
                ModItems.ADVANCED_HANDLE.get()
        };
    }

    public static ItemLike[] getItemTabRegister() {
        return new ItemLike[] {
                ModBlocks.NEWBLOCKADSF.get(),
        };
    }

    public static ItemLike[] getMiscellaneousTabRegister() {
        return new ItemLike[] {
        };
    }

    public static ItemLike[] getBlocksTabRegister() {
        return new ItemLike[] {
                ModBlocks.NEWBLOCK.get(), //PRESERVE!
        };
    }
}
