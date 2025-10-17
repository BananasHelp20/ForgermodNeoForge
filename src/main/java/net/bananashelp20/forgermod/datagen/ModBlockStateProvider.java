package net.bananashelp20.forgermod.datagen;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.block.ModBlocks;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, ForgerMod.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        blockWithItem(ModBlocks.DAMASK_BLOCK);
        blockWithItem(ModBlocks.INANISIUM_BLOCK);
        blockWithItem(ModBlocks.SCRAP_BLOCK);
        blockWithItem(ModBlocks.IGNISIUM_BLOCK);
        blockWithItem(ModBlocks.SCRAP_IRON_BLOCK);
        blockWithItem(ModBlocks.REINFORCED_IRON_BLOCK);
        blockWithItem(ModBlocks.OVERGROWN_BLOCK);
        blockWithItem(ModBlocks.SOMNIUM_BLOCK);
        blockWithItem(ModBlocks.MORSIUM_BLOCK);
        blockWithItem(ModBlocks.VULNUSIUM_BLOCK);
        blockWithItem(ModBlocks.CARBON_STEEL_BLOCK);
        blockWithItem(ModBlocks.PULSITE_BLOCK);
        blockWithItem(ModBlocks.STEEL_BLOCK);
        blockWithItem(ModBlocks.ELECTRIUM_BLOCK);
        blockWithItem(ModBlocks.TAIFUNITE_BLOCK);
        blockWithItem(ModBlocks.DEVELOPIUM_BLOCK);
        blockWithItem(ModBlocks.INFUSION_TABLE);
    }

    private void blockWithItem(DeferredBlock<?> deferredBlock) {
        simpleBlockWithItem(deferredBlock.get(), cubeAll(deferredBlock.get()));
    }
}
