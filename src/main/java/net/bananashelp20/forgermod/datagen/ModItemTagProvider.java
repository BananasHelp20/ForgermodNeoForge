package net.bananashelp20.forgermod.datagen;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
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

                .add(ModItems.VERDANT_AXE.get())
                .add(ModItems.GHOST_AXE.get())
                .add(ModItems.MOLTEN_AXE.get())
                .add(ModItems.NULLIFIED_AXE.get())
                .add(ModItems.WOUNDMAKER_AXE.get())
                .add(ModItems.DREAMWEAVER_AXE.get())
                .add(ModItems.ECHOING_AXE.get())
                .add(ModItems.VOLTAGE_AXE.get())
                .add(ModItems.SKYBREAKER_AXE.get())

                .add(ModItems.LEAFCUTTER_DAGGER.get())
                .add(ModItems.DEATHWISPER_DAGGER.get())
                .add(ModItems.EMBERFANG_DAGGER.get())
                .add(ModItems.RIFTFANG_DAGGER.get())
                .add(ModItems.ASSASSIN_DAGGER.get())
                .add(ModItems.NIGHTMARE_DAGGER.get())
                .add(ModItems.WARDENS_NEEDLE.get())
                .add(ModItems.STATIC_DAGGER.get())
                .add(ModItems.DEAD_CALM_DAGGER.get())

                // THUNDER variants
                .add(ModItems.CLAYMORE_OF_THUNDER_RUBY.get())
                .add(ModItems.CLAYMORE_OF_THUNDER_AMBER.get())
                .add(ModItems.CLAYMORE_OF_THUNDER_AMETHYST.get())
                .add(ModItems.CLAYMORE_OF_THUNDER_JADE.get())

                .add(ModItems.VOLTAGE_AXE_RUBY.get())
                .add(ModItems.VOLTAGE_AXE_AMBER.get())
                .add(ModItems.VOLTAGE_AXE_AMETHYST.get())
                .add(ModItems.VOLTAGE_AXE_JADE.get())

                .add(ModItems.STATIC_DAGGER_RUBY.get())
                .add(ModItems.STATIC_DAGGER_AMBER.get())
                .add(ModItems.STATIC_DAGGER_AMETHYST.get())
                .add(ModItems.STATIC_DAGGER_JADE.get())

                // SHRIEKING variants
                .add(ModItems.SHRIEKING_CLAYMORE_RUBY.get())
                .add(ModItems.SHRIEKING_CLAYMORE_AMBER.get())
                .add(ModItems.SHRIEKING_CLAYMORE_AMETHYST.get())
                .add(ModItems.SHRIEKING_CLAYMORE_JADE.get())

                .add(ModItems.ECHOING_AXE_RUBY.get())
                .add(ModItems.ECHOING_AXE_AMBER.get())
                .add(ModItems.ECHOING_AXE_AMETHYST.get())
                .add(ModItems.ECHOING_AXE_JADE.get())

                .add(ModItems.WARDENS_NEEDLE_RUBY.get())
                .add(ModItems.WARDENS_NEEDLE_AMBER.get())
                .add(ModItems.WARDENS_NEEDLE_AMETHYST.get())
                .add(ModItems.WARDENS_NEEDLE_JADE.get())

                // DREAMBOUND variants
                .add(ModItems.DREAMBOUND_CLAYMORE_RUBY.get())
                .add(ModItems.DREAMBOUND_CLAYMORE_AMBER.get())
                .add(ModItems.DREAMBOUND_CLAYMORE_AMETHYST.get())
                .add(ModItems.DREAMBOUND_CLAYMORE_JADE.get())

                .add(ModItems.DREAMWEAVER_AXE_RUBY.get())
                .add(ModItems.DREAMWEAVER_AXE_AMBER.get())
                .add(ModItems.DREAMWEAVER_AXE_AMETHYST.get())
                .add(ModItems.DREAMWEAVER_AXE_JADE.get())

                .add(ModItems.NIGHTMARE_DAGGER_RUBY.get())
                .add(ModItems.NIGHTMARE_DAGGER_AMBER.get())
                .add(ModItems.NIGHTMARE_DAGGER_AMETHYST.get())
                .add(ModItems.NIGHTMARE_DAGGER_JADE.get())

                // CURSEBLOOD variants
                .add(ModItems.CURSEBLOOD_CLAYMORE_RUBY.get())
                .add(ModItems.CURSEBLOOD_CLAYMORE_AMBER.get())
                .add(ModItems.CURSEBLOOD_CLAYMORE_AMETHYST.get())
                .add(ModItems.CURSEBLOOD_CLAYMORE_JADE.get())

                .add(ModItems.WOUNDMAKER_AXE_RUBY.get())
                .add(ModItems.WOUNDMAKER_AXE_AMBER.get())
                .add(ModItems.WOUNDMAKER_AXE_AMETHYST.get())
                .add(ModItems.WOUNDMAKER_AXE_JADE.get())

                .add(ModItems.ASSASSIN_DAGGER_RUBY.get())
                .add(ModItems.ASSASSIN_DAGGER_AMBER.get())
                .add(ModItems.ASSASSIN_DAGGER_AMETHYST.get())
                .add(ModItems.ASSASSIN_DAGGER_JADE.get())

                // VOID variants
                .add(ModItems.CLAYMORE_OF_THE_VOID_RUBY.get())
                .add(ModItems.CLAYMORE_OF_THE_VOID_AMBER.get())
                .add(ModItems.CLAYMORE_OF_THE_VOID_AMETHYST.get())
                .add(ModItems.CLAYMORE_OF_THE_VOID_JADE.get())

                .add(ModItems.NULLIFIED_AXE_RUBY.get())
                .add(ModItems.NULLIFIED_AXE_AMBER.get())
                .add(ModItems.NULLIFIED_AXE_AMETHYST.get())
                .add(ModItems.NULLIFIED_AXE_JADE.get())

                .add(ModItems.RIFTFANG_DAGGER_RUBY.get())
                .add(ModItems.RIFTFANG_DAGGER_AMBER.get())
                .add(ModItems.RIFTFANG_DAGGER_AMETHYST.get())
                .add(ModItems.RIFTFANG_DAGGER_JADE.get())

                // INFERNAL variants
                .add(ModItems.INFERNAL_CLAYMORE_RUBY.get())
                .add(ModItems.INFERNAL_CLAYMORE_AMBER.get())
                .add(ModItems.INFERNAL_CLAYMORE_AMETHYST.get())
                .add(ModItems.INFERNAL_CLAYMORE_JADE.get())

                .add(ModItems.MOLTEN_AXE_RUBY.get())
                .add(ModItems.MOLTEN_AXE_AMBER.get())
                .add(ModItems.MOLTEN_AXE_AMETHYST.get())
                .add(ModItems.MOLTEN_AXE_JADE.get())

                .add(ModItems.EMBERFANG_DAGGER_RUBY.get())
                .add(ModItems.EMBERFANG_DAGGER_AMBER.get())
                .add(ModItems.EMBERFANG_DAGGER_AMETHYST.get())
                .add(ModItems.EMBERFANG_DAGGER_JADE.get())

                // HOLLOW variants
                .add(ModItems.HOLLOW_CLAYMORE_RUBY.get())
                .add(ModItems.HOLLOW_CLAYMORE_AMBER.get())
                .add(ModItems.HOLLOW_CLAYMORE_AMETHYST.get())
                .add(ModItems.HOLLOW_CLAYMORE_JADE.get())

                .add(ModItems.GHOST_AXE_RUBY.get())
                .add(ModItems.GHOST_AXE_AMBER.get())
                .add(ModItems.GHOST_AXE_AMETHYST.get())
                .add(ModItems.GHOST_AXE_JADE.get())

                .add(ModItems.DEATHWISPER_DAGGER_RUBY.get())
                .add(ModItems.DEATHWISPER_DAGGER_AMBER.get())
                .add(ModItems.DEATHWISPER_DAGGER_AMETHYST.get())
                .add(ModItems.DEATHWISPER_DAGGER_JADE.get())

                // STORMING variants
                .add(ModItems.STORMING_CLAYMORE_RUBY.get())
                .add(ModItems.STORMING_CLAYMORE_AMBER.get())
                .add(ModItems.STORMING_CLAYMORE_AMETHYST.get())
                .add(ModItems.STORMING_CLAYMORE_JADE.get())

                .add(ModItems.SKYBREAKER_AXE_RUBY.get())
                .add(ModItems.SKYBREAKER_AXE_AMBER.get())
                .add(ModItems.SKYBREAKER_AXE_AMETHYST.get())
                .add(ModItems.SKYBREAKER_AXE_JADE.get())

                .add(ModItems.DEAD_CALM_DAGGER_RUBY.get())
                .add(ModItems.DEAD_CALM_DAGGER_AMBER.get())
                .add(ModItems.DEAD_CALM_DAGGER_AMETHYST.get())
                .add(ModItems.DEAD_CALM_DAGGER_JADE.get())

                // OVERGROWN variants
                .add(ModItems.OVERGROWN_CLAYMORE_RUBY.get())
                .add(ModItems.OVERGROWN_CLAYMORE_AMBER.get())
                .add(ModItems.OVERGROWN_CLAYMORE_AMETHYST.get())
                .add(ModItems.OVERGROWN_CLAYMORE_JADE.get())

                .add(ModItems.VERDANT_AXE_RUBY.get())
                .add(ModItems.VERDANT_AXE_AMBER.get())
                .add(ModItems.VERDANT_AXE_AMETHYST.get())
                .add(ModItems.VERDANT_AXE_JADE.get())

                .add(ModItems.LEAFCUTTER_DAGGER_RUBY.get())
                .add(ModItems.LEAFCUTTER_DAGGER_AMBER.get())
                .add(ModItems.LEAFCUTTER_DAGGER_AMETHYST.get())
                .add(ModItems.LEAFCUTTER_DAGGER_JADE.get())

                // other stuff
                .add(ModItems.CLAYMORE.get())
                .add(ModItems.RUSTY_CLAYMORE.get())
                .add(ModItems.CARBON_STEEL_AXE.get())
                .add(ModItems.RUSTY_AXE.get())
                .add(ModItems.CARBON_STEEL_DAGGER.get())
                .add(ModItems.RUSTY_DAGGER.get())

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