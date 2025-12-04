package net.bananashelp20.forgermod.registryInterpreter.testRegistries;

import net.bananashelp20.forgermod.ForgerMod;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredBlock;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, ForgerMod.MOD_ID, exFileHelper);
    }

    public void blockWithItem(DeferredBlock<?> deferredBlock) {
        simpleBlockWithItem(deferredBlock.get(), cubeAll(deferredBlock.get()));
    }

    @Override
    protected void registerStatesAndModels() {
        //!GENERATE MODELS

        blockWithItem(ModBlocks.BLOCK1);
        blockWithItem(ModBlocks.BLOCK2);
    }
}