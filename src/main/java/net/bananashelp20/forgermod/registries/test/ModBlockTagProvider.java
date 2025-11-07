package net.bananashelp20.forgermod.registries.test;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.util.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
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
        tag(ModTags.Blocks.INCORRECT_FOR_STEEL_TOOL)
                .addTag(BlockTags.INCORRECT_FOR_DIAMOND_TOOL)
                .remove(ModTags.Blocks.NEEDS_STEEL_TOOL)
        ;

        tag(ModTags.Blocks.INCORRECT_FOR_CARBON_STEEL_TOOL)
                .addTag(ModTags.Blocks.INCORRECT_FOR_STEEL_TOOL)
                .remove(ModTags.Blocks.NEEDS_CARBON_STEEL_TOOL)
        ;

        tag(ModTags.Blocks.INCORRECT_FOR_ELECTRIUM_TOOL)
                .addTag(ModTags.Blocks.INCORRECT_FOR_CARBON_STEEL_TOOL)
                .remove(ModTags.Blocks.NEEDS_ELECTRIUM_TOOL)
        ;

        tag(ModTags.Blocks.INCORRECT_FOR_MORSIUM_TOOL)
                .addTag(ModTags.Blocks.INCORRECT_FOR_CARBON_STEEL_TOOL)
                .remove(ModTags.Blocks.NEEDS_MORSIUM_TOOL)
        ;

        tag(ModTags.Blocks.INCORRECT_FOR_VULNUSIUM_TOOL)
                .addTag(ModTags.Blocks.INCORRECT_FOR_CARBON_STEEL_TOOL)
                .remove(ModTags.Blocks.NEEDS_VULNUSIUM_TOOL)
        ;

        tag(ModTags.Blocks.INCORRECT_FOR_INANISIUM_TOOL)
                .addTag(ModTags.Blocks.INCORRECT_FOR_CARBON_STEEL_TOOL)
                .remove(ModTags.Blocks.NEEDS_INANISIUM_TOOL)
        ;

        tag(ModTags.Blocks.INCORRECT_FOR_IGNISIUM_TOOL)
                .addTag(ModTags.Blocks.INCORRECT_FOR_CARBON_STEEL_TOOL)
                .remove(ModTags.Blocks.NEEDS_IGNISIUM_TOOL)
        ;

        tag(ModTags.Blocks.INCORRECT_FOR_PULSITE_TOOL)
                .addTag(ModTags.Blocks.INCORRECT_FOR_CARBON_STEEL_TOOL)
                .remove(ModTags.Blocks.NEEDS_PULSITE_TOOL)
        ;

        tag(ModTags.Blocks.INCORRECT_FOR_LUSH_TOOL)
                .addTag(ModTags.Blocks.INCORRECT_FOR_CARBON_STEEL_TOOL)
                .remove(ModTags.Blocks.NEEDS_LUSH_TOOL)
        ;

        tag(ModTags.Blocks.INCORRECT_FOR_SOMNIUM_TOOL)
                .addTag(ModTags.Blocks.INCORRECT_FOR_CARBON_STEEL_TOOL)
                .remove(ModTags.Blocks.NEEDS_SOMNIUM_TOOL)
        ;

        tag(ModTags.Blocks.INCORRECT_FOR_TAIFUNITE_TOOL)
                .addTag(ModTags.Blocks.INCORRECT_FOR_CARBON_STEEL_TOOL)
                .remove(ModTags.Blocks.NEEDS_TAIFUNITE_TOOL)
        ;

        tag(ModTags.Blocks.INCORRECT_FOR_CARBON_STEEL_TOOL)
                .addTag(ModTags.Blocks.INCORRECT_FOR_STEEL_TOOL)
                .remove(ModTags.Blocks.NEEDS_CARBON_STEEL_TOOL)
        ;

        //generate TAGS!
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(ModBlocks.NEWBLOCK.get())
                .add(ModBlocks.NEWBLOCKADFDSF.get())
                .add(ModBlocks.NEWBLOCKASDJF.get())
                .add(ModBlocks.NEWBLOCKABHASD.get())
                .add(ModBlocks.NEWBLOCK3.get())
                .add(ModBlocks.ABCBLOCK.get())
                .add(ModBlocks.NEWBLOCKADSF.get())
                .add(ModBlocks.NEWSPECIALBLOCK.get())
        ;
        tag(BlockTags.MINEABLE_WITH_AXE)
        ;
        tag(BlockTags.MINEABLE_WITH_SHOVEL)
                .add(ModBlocks.NEWCOMPLEXBLOCK.get())
        ;
        tag(BlockTags.MINEABLE_WITH_HOE)
                .add(ModBlocks.NEWCOMPLEXBLOCK2.get())
        ;

        tag(BlockTags.NEEDS_STONE_TOOL)
                .add(ModBlocks.NEWCOMPLEXBLOCK2.get())
        ;

        tag(BlockTags.NEEDS_IRON_TOOL)
                .add(ModBlocks.ABCBLOCK.get())
                .add(ModBlocks.NEWCOMPLEXBLOCK.get())
        ;

        tag(BlockTags.NEEDS_DIAMOND_TOOL)
                .add(ModBlocks.NEWBLOCK.get())
                .add(ModBlocks.NEWBLOCKADFDSF.get())
                .add(ModBlocks.NEWBLOCKASDJF.get())
                .add(ModBlocks.NEWBLOCKABHASD.get())
                .add(ModBlocks.NEWBLOCK3.get())
                .add(ModBlocks.NEWSPECIALBLOCK.get())
        ;

        tag(ModTags.Blocks.NEEDS_STEEL_TOOL)
        ;

        tag(ModTags.Blocks.NEEDS_CARBON_STEEL_TOOL)
                .add(ModBlocks.NEWBLOCKADSF.get())
        ;

        tag(ModTags.Blocks.NEEDS_IGNISIUM_TOOL)
        ;

        tag(ModTags.Blocks.NEEDS_ELECTRIUM_TOOL)
        ;

        tag(ModTags.Blocks.NEEDS_INANISIUM_TOOL)
        ;

        tag(ModTags.Blocks.NEEDS_LUSH_TOOL)
        ;

        tag(ModTags.Blocks.NEEDS_MORSIUM_TOOL)
        ;

        tag(ModTags.Blocks.NEEDS_PULSITE_TOOL)
        ;

        tag(ModTags.Blocks.NEEDS_SOMNIUM_TOOL)
        ;

        tag(ModTags.Blocks.NEEDS_TAIFUNITE_TOOL)
        ;

        tag(ModTags.Blocks.NEEDS_VULNUSIUM_TOOL)
        ;
    }
}