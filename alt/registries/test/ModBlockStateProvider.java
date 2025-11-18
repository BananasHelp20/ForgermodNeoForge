package net.bananashelp20.forgermod.registries.test;

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
        //generate MODELS!
        blockWithItem(ModBlocks.NEWBLOCK);
        blockWithItem(ModBlocks.NEWBLOCKADFDSF);
        blockWithItem(ModBlocks.NEWBLOCKASDJF);
        blockWithItem(ModBlocks.NEWBLOCKABHASD);
        blockWithItem(ModBlocks.NEWBLOCK3);
        blockWithItem(ModBlocks.ABCBLOCK);
        blockWithItem(ModBlocks.NEWBLOCKADSF);
        blockWithItem(ModBlocks.NEWBLOCKADSF);
        blockWithItem(ModBlocks.SIMPLEBLOCKTEST);
        blockWithItem(ModBlocks.NEWSPECIALBLOCK);
        blockWithItem(ModBlocks.SPECIALBLOCKTEST);
    }
}