package net.bananashelp20.forgermod.registryInterpreter.testRegistries;
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

        //!GENERATE BLOCK_TAGS
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(ModBlocks.DAMASK_BLOCK.get())
        ;
        tag(BlockTags.NEEDS_DIAMOND_TOOL)
                .add(ModBlocks.DAMASK_BLOCK.get())
        ;
        tag(ModTags.Blocks.NEEDS_CARBON_STEEL_TOOL)
                .add(ModBlocks.PULSITE_BLOCK.get())
        ;
        tag(BlockTags.NEEDS_IRON_TOOL)
                .add(ModBlocks.SCRAP_BLOCK.get())
        ;
        tag(ModTags.Blocks.NEEDS_STEEL_TOOL)
        ;
    }
}
