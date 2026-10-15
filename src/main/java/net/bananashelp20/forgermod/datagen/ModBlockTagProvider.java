package net.bananashelp20.forgermod.datagen;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.block.ModBlocks;
import net.bananashelp20.forgermod.util.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.SoundType;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModBlockTagProvider extends BlockTagsProvider {

    public ModBlockTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, ForgerMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
        tag(ModTags.Blocks.INCORRECT_FOR_SCRAP_TOOL).addTag(BlockTags.INCORRECT_FOR_IRON_TOOL)
                .addTag(ModTags.Blocks.NEEDS_STEEL_TOOL).addTag(ModTags.Blocks.NEEDS_TITANIUM_TOOL)
                .addTag(ModTags.Blocks.NEEDS_CARBON_STEEL_TOOL);
        tag(ModTags.Blocks.INCORRECT_FOR_SCRAP_IRON_TOOL).addTag(BlockTags.INCORRECT_FOR_IRON_TOOL)
                .addTag(ModTags.Blocks.NEEDS_STEEL_TOOL).addTag(ModTags.Blocks.NEEDS_TITANIUM_TOOL)
                .addTag(ModTags.Blocks.NEEDS_CARBON_STEEL_TOOL);
        tag(ModTags.Blocks.INCORRECT_FOR_METEORIC_IRON_TOOL).addTag(BlockTags.INCORRECT_FOR_IRON_TOOL)
                .addTag(ModTags.Blocks.NEEDS_STEEL_TOOL).addTag(ModTags.Blocks.NEEDS_TITANIUM_TOOL)
                .addTag(ModTags.Blocks.NEEDS_CARBON_STEEL_TOOL);
        tag(ModTags.Blocks.INCORRECT_FOR_REINFORCED_IRON_TOOL).addTag(BlockTags.INCORRECT_FOR_IRON_TOOL)
                .addTag(ModTags.Blocks.NEEDS_STEEL_TOOL).addTag(ModTags.Blocks.NEEDS_TITANIUM_TOOL)
                .addTag(ModTags.Blocks.NEEDS_CARBON_STEEL_TOOL);
        tag(ModTags.Blocks.INCORRECT_FOR_STEEL_TOOL)
                .addTag(BlockTags.INCORRECT_FOR_IRON_TOOL)
                .addTag(ModTags.Blocks.NEEDS_TITANIUM_TOOL)
                .addTag(ModTags.Blocks.NEEDS_CARBON_STEEL_TOOL);

        tag(ModTags.Blocks.INCORRECT_FOR_DAMASK_TOOL).addTag(ModTags.Blocks.NEEDS_TITANIUM_TOOL)
                .addTag(ModTags.Blocks.NEEDS_CARBON_STEEL_TOOL);
        tag(ModTags.Blocks.INCORRECT_FOR_TITANIUM_TOOL).addTag(ModTags.Blocks.NEEDS_CARBON_STEEL_TOOL);
        tag(ModTags.Blocks.INCORRECT_FOR_TUNGSTEN_TOOL).addTag(ModTags.Blocks.NEEDS_CARBON_STEEL_TOOL);

        tag(ModTags.Blocks.INCORRECT_FOR_CARBON_STEEL_TOOL)
                .addTag(BlockTags.INCORRECT_FOR_NETHERITE_TOOL);

        tag(ModTags.Blocks.INCORRECT_FOR_INANISIUM_TOOL);
        tag(ModTags.Blocks.INCORRECT_FOR_IGNISIUM_TOOL);
        tag(ModTags.Blocks.INCORRECT_FOR_MORSIUM_TOOL);
        tag(ModTags.Blocks.INCORRECT_FOR_SOMNIUM_TOOL);
        tag(ModTags.Blocks.INCORRECT_FOR_VULNUSIUM_TOOL);
        tag(ModTags.Blocks.INCORRECT_FOR_LUSH_TOOL);
        tag(ModTags.Blocks.INCORRECT_FOR_PULSITE_TOOL);
        tag(ModTags.Blocks.INCORRECT_FOR_ELECTRIUM_TOOL);
        tag(ModTags.Blocks.INCORRECT_FOR_TAIFUNITE_TOOL);
        tag(ModTags.Blocks.INCORRECT_FOR_DEVELOPIUM_TOOL);

        tag(BlockTags.INCORRECT_FOR_DIAMOND_TOOL)
                .add(ModBlocks.TUNGSTEN_STONE_ORE.get(), ModBlocks.TUNGSTEN_DEEPSLATE_ORE.get())
                .add(ModBlocks.CARBON_STEEL_BLOCK.get())
                .add(ModBlocks.INANISIUM_BLOCK.get(), ModBlocks.IGNISIUM_BLOCK.get(), ModBlocks.OVERGROWN_BLOCK.get())
                .add(ModBlocks.VULNUSIUM_BLOCK.get(), ModBlocks.SOMNIUM_BLOCK.get(), ModBlocks.MORSIUM_BLOCK.get())
                .add(ModBlocks.ELECTRIUM_BLOCK.get(), ModBlocks.TAIFUNITE_BLOCK.get(), ModBlocks.DEVELOPIUM_BLOCK.get());

        //generate TAGS!
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ModBlocks.AUGMENTATION_TABLE.get())
                .add(ModBlocks.METEORIC_IRON_STONE_ORE.get())
                .add(ModBlocks.METEORIC_IRON_END_ORE.get())
                .add(ModBlocks.TITANIUM_STONE_ORE.get())
                .add(ModBlocks.TITANIUM_DEEPSLATE_ORE.get())
                .add(ModBlocks.TUNGSTEN_STONE_ORE.get())
                .add(ModBlocks.TUNGSTEN_DEEPSLATE_ORE.get())
                .add(ModBlocks.DOLOMITE_STONE_ORE.get())
                .add(ModBlocks.DAMASK_BLOCK.get())
                .add(ModBlocks.INANISIUM_BLOCK.get())
                .add(ModBlocks.IGNISIUM_BLOCK.get())
                .add(ModBlocks.REINFORCED_IRON_BLOCK.get())
                .add(ModBlocks.OVERGROWN_BLOCK.get())
                .add(ModBlocks.VULNUSIUM_BLOCK.get())
                .add(ModBlocks.SOMNIUM_BLOCK.get())
                .add(ModBlocks.MORSIUM_BLOCK.get())
                .add(ModBlocks.SCRAP_BLOCK.get())
                .add(ModBlocks.SCRAP_IRON_BLOCK.get())
                .add(ModBlocks.PULSITE_BLOCK.get())
                .add(ModBlocks.CARBON_STEEL_BLOCK.get())
                .add(ModBlocks.STEEL_BLOCK.get())
                .add(ModBlocks.ELECTRIUM_BLOCK.get())
                .add(ModBlocks.TAIFUNITE_BLOCK.get())
                .add(ModBlocks.DEVELOPIUM_BLOCK.get())

                //ores
                .add(ModBlocks.JADE_STONE_ORE.get())
                .add(ModBlocks.JADE_NETHER_ORE.get())
                .add(ModBlocks.JADE_STONE_ORE.get())
                .add(ModBlocks.JADE_DEEPSLATE_ORE.get())
                .add(ModBlocks.JADE_OBSIDIAN_ORE.get())

                .add(ModBlocks.RUBY_STONE_ORE.get())
                .add(ModBlocks.RUBY_NETHER_ORE.get())
                .add(ModBlocks.RUBY_STONE_ORE.get())
                .add(ModBlocks.RUBY_DEEPSLATE_ORE.get())
                .add(ModBlocks.RUBY_OBSIDIAN_ORE.get())

                .add(ModBlocks.AMETHYST_STONE_ORE.get())
                .add(ModBlocks.AMETHYST_NETHER_ORE.get())
                .add(ModBlocks.AMETHYST_STONE_ORE.get())
                .add(ModBlocks.AMETHYST_DEEPSLATE_ORE.get())
                .add(ModBlocks.AMETHYST_OBSIDIAN_ORE.get())

                .add(ModBlocks.AMBER_STONE_ORE.get())
                .add(ModBlocks.AMBER_NETHER_ORE.get())
                .add(ModBlocks.AMBER_STONE_ORE.get())
                .add(ModBlocks.AMBER_DEEPSLATE_ORE.get())
                .add(ModBlocks.AMBER_OBSIDIAN_ORE.get())

                .add(ModBlocks.SHARDIUM_STONE_ORE.get())
                .add(ModBlocks.SHARDIUM_DEEPSLATE_ORE.get())
                .add(ModBlocks.SHARDIUM_NETHER_ORE.get())
                .add(ModBlocks.SHARDIUM_END_ORE.get())
                .add(ModBlocks.SHARDIUM_OBSIDIAN_ORE.get())
        ;

        tag(BlockTags.MINEABLE_WITH_AXE)

        ;

        tag(BlockTags.MINEABLE_WITH_HOE)

        ;

        tag(BlockTags.MINEABLE_WITH_SHOVEL)

        ;

        tag(BlockTags.NEEDS_IRON_TOOL)
                .add(ModBlocks.METEORIC_IRON_STONE_ORE.get())
                .add(ModBlocks.METEORIC_IRON_END_ORE.get())
                .add(ModBlocks.DOLOMITE_STONE_ORE.get())
                .add(ModBlocks.REINFORCED_IRON_BLOCK.get())
                .add(ModBlocks.STEEL_BLOCK.get())
                .add(ModBlocks.SCRAP_BLOCK.get())
                .add(ModBlocks.SCRAP_IRON_BLOCK.get())
                .add(ModBlocks.ANCIENT_SWORD_STAND.get())
        ;

        tag(BlockTags.NEEDS_DIAMOND_TOOL)
                .add(ModBlocks.TITANIUM_STONE_ORE.get())
                .add(ModBlocks.TITANIUM_DEEPSLATE_ORE.get())
                .add(ModBlocks.DAMASK_BLOCK.get())
                .add(ModBlocks.JADE_STONE_ORE.get())
                .add(ModBlocks.JADE_END_ORE.get())
                .add(ModBlocks.JADE_NETHER_ORE.get())
                .add(ModBlocks.JADE_DEEPSLATE_ORE.get())
                .add(ModBlocks.JADE_OBSIDIAN_ORE.get())
                .add(ModBlocks.RUBY_STONE_ORE.get())
                .add(ModBlocks.RUBY_END_ORE.get())
                .add(ModBlocks.RUBY_NETHER_ORE.get())
                .add(ModBlocks.RUBY_DEEPSLATE_ORE.get())
                .add(ModBlocks.RUBY_OBSIDIAN_ORE.get())
                .add(ModBlocks.AMETHYST_STONE_ORE.get())
                .add(ModBlocks.AMETHYST_END_ORE.get())
                .add(ModBlocks.AMETHYST_NETHER_ORE.get())
                .add(ModBlocks.AMETHYST_DEEPSLATE_ORE.get())
                .add(ModBlocks.AMETHYST_OBSIDIAN_ORE.get())
                .add(ModBlocks.AMBER_STONE_ORE.get())
                .add(ModBlocks.AMBER_END_ORE.get())
                .add(ModBlocks.AMBER_NETHER_ORE.get())
                .add(ModBlocks.AMBER_DEEPSLATE_ORE.get())
                .add(ModBlocks.AMBER_OBSIDIAN_ORE.get())
                .add(ModBlocks.SHARDIUM_STONE_ORE.get())
                .add(ModBlocks.SHARDIUM_DEEPSLATE_ORE.get())
                .add(ModBlocks.SHARDIUM_NETHER_ORE.get())
                .add(ModBlocks.SHARDIUM_END_ORE.get())
                .add(ModBlocks.SHARDIUM_OBSIDIAN_ORE.get())
        ;

        tag(ModTags.Blocks.NEEDS_STEEL_TOOL)
        ;

        tag(ModTags.Blocks.NEEDS_TITANIUM_TOOL)
                .add(ModBlocks.TUNGSTEN_STONE_ORE.get(), ModBlocks.TUNGSTEN_DEEPSLATE_ORE.get());

        tag(ModTags.Blocks.NEEDS_CARBON_STEEL_TOOL)
                .add(ModBlocks.CARBON_STEEL_BLOCK.get())
                .add(ModBlocks.INANISIUM_BLOCK.get())
                .add(ModBlocks.IGNISIUM_BLOCK.get())
                .add(ModBlocks.OVERGROWN_BLOCK.get())
                .add(ModBlocks.VULNUSIUM_BLOCK.get())
                .add(ModBlocks.SOMNIUM_BLOCK.get())
                .add(ModBlocks.MORSIUM_BLOCK.get())
                .add(ModBlocks.ELECTRIUM_BLOCK.get())
                .add(ModBlocks.TAIFUNITE_BLOCK.get())
                .add(ModBlocks.DEVELOPIUM_BLOCK.get())
        ;
    }
}
