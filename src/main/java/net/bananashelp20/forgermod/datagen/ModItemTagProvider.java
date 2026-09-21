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

        tag(ItemTags.SWORDS)
                // base variants
                .add(ModItems.OVERGROWN_CLAYMORE.get())
                .add(ModItems.HOLLOW_CLAYMORE.get())
                .add(ModItems.INFERNAL_CLAYMORE.get())
                .add(ModItems.CLAYMORE_OF_THE_VOID.get())
                .add(ModItems.CURSEBLOOD_CLAYMORE.get())
                .add(ModItems.DREAMBOUND_CLAYMORE.get())
                .add(ModItems.SHRIEKING_CLAYMORE.get())
                .add(ModItems.CLAYMORE_OF_THUNDER.get())
                .add(ModItems.STORMING_CLAYMORE.get())

                .add(ModItems.OVERGROWN_AXE.get())
                .add(ModItems.HOLLOW_AXE.get())
                .add(ModItems.INFERNAL_AXE.get())
                .add(ModItems.AXE_OF_THE_VOID.get())
                .add(ModItems.CURSEBLOOD_AXE.get())
                .add(ModItems.DREAMBOUND_AXE.get())
                .add(ModItems.SHRIEKING_AXE.get())
                .add(ModItems.AXE_OF_THUNDER.get())
                .add(ModItems.STORMING_AXE.get())

                .add(ModItems.OVERGROWN_KNIFE.get())
                .add(ModItems.HOLLOW_KNIFE.get())
                .add(ModItems.INFERNAL_KNIFE.get())
                .add(ModItems.KNIFE_OF_THE_VOID.get())
                .add(ModItems.CURSEBLOOD_KNIFE.get())
                .add(ModItems.DREAMBOUND_KNIFE.get())
                .add(ModItems.SHRIEKING_KNIFE.get())
                .add(ModItems.KNIFE_OF_THUNDER.get())
                .add(ModItems.STORMING_KNIFE.get())

                // THUNDER variants
                .add(ModItems.CLAYMORE_OF_THUNDER_RUBY.get())
                .add(ModItems.CLAYMORE_OF_THUNDER_AMBER.get())
                .add(ModItems.CLAYMORE_OF_THUNDER_AMETHYST.get())
                .add(ModItems.CLAYMORE_OF_THUNDER_JADE.get())

                .add(ModItems.AXE_OF_THUNDER_RUBY.get())
                .add(ModItems.AXE_OF_THUNDER_AMBER.get())
                .add(ModItems.AXE_OF_THUNDER_AMETHYST.get())
                .add(ModItems.AXE_OF_THUNDER_JADE.get())

                .add(ModItems.KNIFE_OF_THUNDER_RUBY.get())
                .add(ModItems.KNIFE_OF_THUNDER_AMBER.get())
                .add(ModItems.KNIFE_OF_THUNDER_AMETHYST.get())
                .add(ModItems.KNIFE_OF_THUNDER_JADE.get())

                // SHRIEKING variants
                .add(ModItems.SHRIEKING_CLAYMORE_RUBY.get())
                .add(ModItems.SHRIEKING_CLAYMORE_AMBER.get())
                .add(ModItems.SHRIEKING_CLAYMORE_AMETHYST.get())
                .add(ModItems.SHRIEKING_CLAYMORE_JADE.get())

                .add(ModItems.SHRIEKING_AXE_RUBY.get())
                .add(ModItems.SHRIEKING_AXE_AMBER.get())
                .add(ModItems.SHRIEKING_AXE_AMETHYST.get())
                .add(ModItems.SHRIEKING_AXE_JADE.get())

                .add(ModItems.SHRIEKING_KNIFE_RUBY.get())
                .add(ModItems.SHRIEKING_KNIFE_AMBER.get())
                .add(ModItems.SHRIEKING_KNIFE_AMETHYST.get())
                .add(ModItems.SHRIEKING_KNIFE_JADE.get())

                // DREAMBOUND variants
                .add(ModItems.DREAMBOUND_CLAYMORE_RUBY.get())
                .add(ModItems.DREAMBOUND_CLAYMORE_AMBER.get())
                .add(ModItems.DREAMBOUND_CLAYMORE_AMETHYST.get())
                .add(ModItems.DREAMBOUND_CLAYMORE_JADE.get())

                .add(ModItems.DREAMBOUND_AXE_RUBY.get())
                .add(ModItems.DREAMBOUND_AXE_AMBER.get())
                .add(ModItems.DREAMBOUND_AXE_AMETHYST.get())
                .add(ModItems.DREAMBOUND_AXE_JADE.get())

                .add(ModItems.DREAMBOUND_KNIFE_RUBY.get())
                .add(ModItems.DREAMBOUND_KNIFE_AMBER.get())
                .add(ModItems.DREAMBOUND_KNIFE_AMETHYST.get())
                .add(ModItems.DREAMBOUND_KNIFE_JADE.get())

                // CURSEBLOOD variants
                .add(ModItems.CURSEBLOOD_CLAYMORE_RUBY.get())
                .add(ModItems.CURSEBLOOD_CLAYMORE_AMBER.get())
                .add(ModItems.CURSEBLOOD_CLAYMORE_AMETHYST.get())
                .add(ModItems.CURSEBLOOD_CLAYMORE_JADE.get())

                .add(ModItems.CURSEBLOOD_AXE_RUBY.get())
                .add(ModItems.CURSEBLOOD_AXE_AMBER.get())
                .add(ModItems.CURSEBLOOD_AXE_AMETHYST.get())
                .add(ModItems.CURSEBLOOD_AXE_JADE.get())

                .add(ModItems.CURSEBLOOD_KNIFE_RUBY.get())
                .add(ModItems.CURSEBLOOD_KNIFE_AMBER.get())
                .add(ModItems.CURSEBLOOD_KNIFE_AMETHYST.get())
                .add(ModItems.CURSEBLOOD_KNIFE_JADE.get())

                // VOID variants
                .add(ModItems.CLAYMORE_OF_THE_VOID_RUBY.get())
                .add(ModItems.CLAYMORE_OF_THE_VOID_AMBER.get())
                .add(ModItems.CLAYMORE_OF_THE_VOID_AMETHYST.get())
                .add(ModItems.CLAYMORE_OF_THE_VOID_JADE.get())

                .add(ModItems.AXE_OF_THE_VOID_RUBY.get())
                .add(ModItems.AXE_OF_THE_VOID_AMBER.get())
                .add(ModItems.AXE_OF_THE_VOID_AMETHYST.get())
                .add(ModItems.AXE_OF_THE_VOID_JADE.get())

                .add(ModItems.KNIFE_OF_THE_VOID_RUBY.get())
                .add(ModItems.KNIFE_OF_THE_VOID_AMBER.get())
                .add(ModItems.KNIFE_OF_THE_VOID_AMETHYST.get())
                .add(ModItems.KNIFE_OF_THE_VOID_JADE.get())

                // INFERNAL variants
                .add(ModItems.INFERNAL_CLAYMORE_RUBY.get())
                .add(ModItems.INFERNAL_CLAYMORE_AMBER.get())
                .add(ModItems.INFERNAL_CLAYMORE_AMETHYST.get())
                .add(ModItems.INFERNAL_CLAYMORE_JADE.get())

                .add(ModItems.INFERNAL_AXE_RUBY.get())
                .add(ModItems.INFERNAL_AXE_AMBER.get())
                .add(ModItems.INFERNAL_AXE_AMETHYST.get())
                .add(ModItems.INFERNAL_AXE_JADE.get())

                .add(ModItems.INFERNAL_KNIFE_RUBY.get())
                .add(ModItems.INFERNAL_KNIFE_AMBER.get())
                .add(ModItems.INFERNAL_KNIFE_AMETHYST.get())
                .add(ModItems.INFERNAL_KNIFE_JADE.get())

                // HOLLOW variants
                .add(ModItems.HOLLOW_CLAYMORE_RUBY.get())
                .add(ModItems.HOLLOW_CLAYMORE_AMBER.get())
                .add(ModItems.HOLLOW_CLAYMORE_AMETHYST.get())
                .add(ModItems.HOLLOW_CLAYMORE_JADE.get())

                .add(ModItems.HOLLOW_AXE_RUBY.get())
                .add(ModItems.HOLLOW_AXE_AMBER.get())
                .add(ModItems.HOLLOW_AXE_AMETHYST.get())
                .add(ModItems.HOLLOW_AXE_JADE.get())

                .add(ModItems.HOLLOW_KNIFE_RUBY.get())
                .add(ModItems.HOLLOW_KNIFE_AMBER.get())
                .add(ModItems.HOLLOW_KNIFE_AMETHYST.get())
                .add(ModItems.HOLLOW_KNIFE_JADE.get())

                // STORMING variants
                .add(ModItems.STORMING_CLAYMORE_RUBY.get())
                .add(ModItems.STORMING_CLAYMORE_AMBER.get())
                .add(ModItems.STORMING_CLAYMORE_AMETHYST.get())
                .add(ModItems.STORMING_CLAYMORE_JADE.get())

                .add(ModItems.STORMING_AXE_RUBY.get())
                .add(ModItems.STORMING_AXE_AMBER.get())
                .add(ModItems.STORMING_AXE_AMETHYST.get())
                .add(ModItems.STORMING_AXE_JADE.get())

                .add(ModItems.STORMING_KNIFE_RUBY.get())
                .add(ModItems.STORMING_KNIFE_AMBER.get())
                .add(ModItems.STORMING_KNIFE_AMETHYST.get())
                .add(ModItems.STORMING_KNIFE_JADE.get())

                // OVERGROWN variants
                .add(ModItems.OVERGROWN_CLAYMORE_RUBY.get())
                .add(ModItems.OVERGROWN_CLAYMORE_AMBER.get())
                .add(ModItems.OVERGROWN_CLAYMORE_AMETHYST.get())
                .add(ModItems.OVERGROWN_CLAYMORE_JADE.get())

                .add(ModItems.OVERGROWN_AXE_RUBY.get())
                .add(ModItems.OVERGROWN_AXE_AMBER.get())
                .add(ModItems.OVERGROWN_AXE_AMETHYST.get())
                .add(ModItems.OVERGROWN_AXE_JADE.get())

                .add(ModItems.OVERGROWN_KNIFE_RUBY.get())
                .add(ModItems.OVERGROWN_KNIFE_AMBER.get())
                .add(ModItems.OVERGROWN_KNIFE_AMETHYST.get())
                .add(ModItems.OVERGROWN_KNIFE_JADE.get())

                // other stuff
                .add(ModItems.CLAYMORE.get())
                .add(ModItems.RUSTY_CLAYMORE.get())
                .add(ModItems.CARBON_STEEL_AXE.get())
                .add(ModItems.RUSTY_AXE.get())
                .add(ModItems.CARBON_STEEL_KNIFE.get())
                .add(ModItems.RUSTY_KNIFE.get())

                .add(ModItems.DAMASK_KNIFE.get())

                .add(ModItems.STEEL_SWORD.get())
                .add(ModItems.DAMASK_SWORD.get())
                .add(ModItems.REINFORCED_IRON_SWORD.get())
                .add(ModItems.SCRAP_IRON_SWORD.get())
                .add(ModItems.SCRAP_SWORD.get())

                .add(ModItems.STUMPFL_BAT.get())
        ;
    }
}