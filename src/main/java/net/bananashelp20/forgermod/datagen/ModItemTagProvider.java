package net.bananashelp20.forgermod.datagen;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.ModToolTiers;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.registries.DeferredItem;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModItemTagProvider extends ItemTagsProvider {
    public ModItemTagProvider(PackOutput pOutput, CompletableFuture<HolderLookup.Provider> pLookupProvider, CompletableFuture<TagLookup<Block>> pBlockTags, @Nullable ExistingFileHelper existingFileHelper) {
        super(pOutput, pLookupProvider, pBlockTags, ForgerMod.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider pProvider) {
//        tag(ModTags.Items.TRANSFORMABLE_ITEMS)
//
//        ;
        tag(ItemTags.SWORDS)
                // base special claymores
                .add(ModItems.OVERGROWN_CLAYMORE.get())
                .add(ModItems.HOLLOW_CLAYMORE.get())
                .add(ModItems.INFERNAL_CLAYMORE.get())
                .add(ModItems.CLAYMORE_OF_THE_VOID.get())
                .add(ModItems.CURSEBLOOD_CLAYMORE.get())
                .add(ModItems.DREAMBOUND_CLAYMORE.get())
                .add(ModItems.SHRIEKING_CLAYMORE.get())
                .add(ModItems.CLAYMORE_OF_THUNDER.get())
                .add(ModItems.STORMING_CLAYMORE.get())

                // CLAYMORE_OF_THUNDER variants
                .add(ModItems.CLAYMORE_OF_THUNDER_RUBY.get())
                .add(ModItems.CLAYMORE_OF_THUNDER_AMBER.get())
                .add(ModItems.CLAYMORE_OF_THUNDER_AMETHYST.get())
                .add(ModItems.CLAYMORE_OF_THUNDER_JADE.get())

                // SHRIEKING_CLAYMORE variants
                .add(ModItems.SHRIEKING_CLAYMORE_RUBY.get())
                .add(ModItems.SHRIEKING_CLAYMORE_AMBER.get())
                .add(ModItems.SHRIEKING_CLAYMORE_AMETHYST.get())
                .add(ModItems.SHRIEKING_CLAYMORE_JADE.get())

                // DREAMBOUND_CLAYMORE variants
                .add(ModItems.DREAMBOUND_CLAYMORE_RUBY.get())
                .add(ModItems.DREAMBOUND_CLAYMORE_AMBER.get())
                .add(ModItems.DREAMBOUND_CLAYMORE_AMETHYST.get())
                .add(ModItems.DREAMBOUND_CLAYMORE_JADE.get())

                // CURSEBLOOD_CLAYMORE variants
                .add(ModItems.CURSEBLOOD_CLAYMORE_RUBY.get())
                .add(ModItems.CURSEBLOOD_CLAYMORE_AMBER.get())
                .add(ModItems.CURSEBLOOD_CLAYMORE_AMETHYST.get())
                .add(ModItems.CURSEBLOOD_CLAYMORE_JADE.get())

                // CLAYMORE_OF_THE_VOID variants
                .add(ModItems.CLAYMORE_OF_THE_VOID_RUBY.get())
                .add(ModItems.CLAYMORE_OF_THE_VOID_AMBER.get())
                .add(ModItems.CLAYMORE_OF_THE_VOID_AMETHYST.get())
                .add(ModItems.CLAYMORE_OF_THE_VOID_JADE.get())

                // INFERNAL_CLAYMORE variants
                .add(ModItems.INFERNAL_CLAYMORE_RUBY.get())
                .add(ModItems.INFERNAL_CLAYMORE_AMBER.get())
                .add(ModItems.INFERNAL_CLAYMORE_AMETHYST.get())
                .add(ModItems.INFERNAL_CLAYMORE_JADE.get())

                // HOLLOW_CLAYMORE variants
                .add(ModItems.HOLLOW_CLAYMORE_RUBY.get())
                .add(ModItems.HOLLOW_CLAYMORE_AMBER.get())
                .add(ModItems.HOLLOW_CLAYMORE_AMETHYST.get())
                .add(ModItems.HOLLOW_CLAYMORE_JADE.get())

                // STORMING_CLAYMORE variants
                .add(ModItems.STORMING_CLAYMORE_RUBY.get())
                .add(ModItems.STORMING_CLAYMORE_AMBER.get())
                .add(ModItems.STORMING_CLAYMORE_AMETHYST.get())
                .add(ModItems.STORMING_CLAYMORE_JADE.get())

                // OVERGROWN_CLAYMORE variants
                .add(ModItems.OVERGROWN_CLAYMORE_RUBY.get())
                .add(ModItems.OVERGROWN_CLAYMORE_AMBER.get())
                .add(ModItems.OVERGROWN_CLAYMORE_AMETHYST.get())
                .add(ModItems.OVERGROWN_CLAYMORE_JADE.get())

                // normal swords
                .add(ModItems.STEEL_SWORD.get())
                .add(ModItems.STUMPFL_BAT.get())
                .add(ModItems.DAMASK_KNIFE.get())
                .add(ModItems.DAMASK_SWORD.get())
                .add(ModItems.REINFORCED_IRON_SWORD.get())
                .add(ModItems.SCRAP_IRON_SWORD.get())
                .add(ModItems.SCRAP_SWORD.get())
                .add(ModItems.RUSTY_CLAYMORE.get())
        ;
    }
}