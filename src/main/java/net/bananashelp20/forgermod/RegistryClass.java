package net.bananashelp20.forgermod;

import net.bananashelp20.forgermod.block.ModBlocks;
import net.bananashelp20.forgermod.block.custom.AncientSwordStandBlock;
import net.bananashelp20.forgermod.item.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;

import java.util.List;
import java.util.function.Supplier;

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
                ModBlocks.REINFORCED_IRON_BLOCK.get(),
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
                ModItems.SCRAP_IRON_SWORD.get(),
                ModItems.REINFORCED_IRON_SWORD.get(),
                ModItems.SCRAP_SWORD.get(),
                ModItems.DAMASK_KNIFE.get(),
                ModItems.DAMASK_SWORD.get(),
                ModItems.STEEL_SWORD.get(),
                ModItems.CLAYMORE.get(),
                ModItems.RUSTY_CLAYMORE.get(),
                ModItems.STUMPFL_BAT.get(),
        
                ModItems.INFERNAL_CLAYMORE.get(),
                ModItems.INFERNAL_CLAYMORE_RUBY.get(),
                ModItems.INFERNAL_CLAYMORE_AMBER.get(),
                ModItems.INFERNAL_CLAYMORE_AMETHYST.get(),
                ModItems.INFERNAL_CLAYMORE_JADE.get(),

                ModItems.CLAYMORE_OF_THE_VOID.get(),
                ModItems.CLAYMORE_OF_THE_VOID_RUBY.get(),
                ModItems.CLAYMORE_OF_THE_VOID_AMBER.get(),
                ModItems.CLAYMORE_OF_THE_VOID_AMETHYST.get(),
                ModItems.CLAYMORE_OF_THE_VOID_JADE.get(),

                ModItems.OVERGROWN_CLAYMORE.get(),
                ModItems.OVERGROWN_CLAYMORE_RUBY.get(),
                ModItems.OVERGROWN_CLAYMORE_AMBER.get(),
                ModItems.OVERGROWN_CLAYMORE_AMETHYST.get(),
                ModItems.OVERGROWN_CLAYMORE_JADE.get(),

                ModItems.HOLLOW_CLAYMORE.get(),
                ModItems.HOLLOW_CLAYMORE_RUBY.get(),
                ModItems.HOLLOW_CLAYMORE_AMBER.get(),
                ModItems.HOLLOW_CLAYMORE_AMETHYST.get(),
                ModItems.HOLLOW_CLAYMORE_JADE.get(),

                ModItems.CURSEBLOOD_CLAYMORE.get(),
                ModItems.CURSEBLOOD_CLAYMORE_RUBY.get(),
                ModItems.CURSEBLOOD_CLAYMORE_AMBER.get(),
                ModItems.CURSEBLOOD_CLAYMORE_AMETHYST.get(),
                ModItems.CURSEBLOOD_CLAYMORE_JADE.get(),

                ModItems.DREAMBOUND_CLAYMORE.get(),
                ModItems.DREAMBOUND_CLAYMORE_RUBY.get(),
                ModItems.DREAMBOUND_CLAYMORE_AMBER.get(),
                ModItems.DREAMBOUND_CLAYMORE_AMETHYST.get(),
                ModItems.DREAMBOUND_CLAYMORE_JADE.get(),

                ModItems.SHRIEKING_CLAYMORE.get(),
                ModItems.SHRIEKING_CLAYMORE_RUBY.get(),
                ModItems.SHRIEKING_CLAYMORE_AMBER.get(),
                ModItems.SHRIEKING_CLAYMORE_AMETHYST.get(),
                ModItems.SHRIEKING_CLAYMORE_JADE.get(),

                ModItems.CLAYMORE_OF_THUNDER.get(),
                ModItems.CLAYMORE_OF_THUNDER_RUBY.get(),
                ModItems.CLAYMORE_OF_THUNDER_AMBER.get(),
                ModItems.CLAYMORE_OF_THUNDER_AMETHYST.get(),
                ModItems.CLAYMORE_OF_THUNDER_JADE.get(),

                ModItems.STORMING_CLAYMORE.get(),
                ModItems.STORMING_CLAYMORE_RUBY.get(),
                ModItems.STORMING_CLAYMORE_AMBER.get(),
                ModItems.STORMING_CLAYMORE_AMETHYST.get(),
                ModItems.STORMING_CLAYMORE_JADE.get()
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
//                ModBlocks.ANCIENT_SWORD_STAND.get(),
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
                ModBlocks.AMBER_OBSIDIAN_ORE.get()
        };
    }
}