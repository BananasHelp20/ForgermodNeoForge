package net.bananashelp20.forgermod.registryInterpreter.testRegistries;

import net.bananashelp20.forgermod.ForgerMod;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider implements IConditionBuilder {
    public ModRecipeProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> pRegistries) {
        super(packOutput, pRegistries);
    }

    protected static void oreSmelting(RecipeOutput recipeOutput, List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult,
                                      float pExperience, int pCookingTIme, String pGroup) {
        oreCooking(recipeOutput, RecipeSerializer.SMELTING_RECIPE, SmeltingRecipe::new, pIngredients, pCategory, pResult,
                pExperience, pCookingTIme, pGroup, "_from_smelting");
    }

    protected static void oreBlasting(RecipeOutput recipeOutput, List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult,
                                      float pExperience, int pCookingTime, String pGroup) {
        oreCooking(recipeOutput, RecipeSerializer.BLASTING_RECIPE, BlastingRecipe::new, pIngredients, pCategory, pResult,
                pExperience, pCookingTime, pGroup, "_from_blasting");
    }

    protected static <T extends AbstractCookingRecipe> void oreCooking(RecipeOutput recipeOutput, RecipeSerializer<T> pCookingSerializer, AbstractCookingRecipe.Factory<T> factory,
                                                                       List<ItemLike> pIngredients, RecipeCategory pCategory, ItemLike pResult, float pExperience, int pCookingTime, String pGroup, String pRecipeName) {
        for(ItemLike itemlike : pIngredients) {
            SimpleCookingRecipeBuilder.generic(Ingredient.of(itemlike), pCategory, pResult, pExperience, pCookingTime, pCookingSerializer, factory).group(pGroup).unlockedBy(getHasName(itemlike), has(itemlike))
                    .save(recipeOutput, ForgerMod.MOD_ID + ":" + getItemName(pResult) + pRecipeName + "_" + getItemName(itemlike));
        }
    }

    /*protected static final List<ItemLike> DAMASK_SMELTABLES = List.of(
            ModItems.SCRAP_INGOT.get(),
            ModItems.DAMASK_SWORD.get(),
            ModItems.DAMASK_KNIFE.get(),
            ModItems.HANDLE.get()
    );

    protected static final List<ItemLike> REINFORCED_IRON_SMELTABLES = List.of(
            ModItems.SCRAP_IRON_INGOT.get(),
            ModItems.REINFORCED_IRON_SWORD.get()
    );

    protected static final List<ItemLike> CARBON_STEEL_SMELTABLES = List.of(
            ModItems.UNREFINED_CARBON_STEEL.get(),
            ModItems.CLAYMORE.get(),
            ModItems.CARBON_STEEL_CROSS_GUARD.get(),
            ModItems.ADVANCED_HANDLE.get(),
            ModItems.SHARPENED_BLADE.get()
    );

    protected static final List<ItemLike> STEEL_SMELTABLES = List.of(
            ModItems.UNREFINED_STEEL.get()
    );

    protected static final List<ItemLike> RUBY_SMELTABLES = List.of(
            ModBlocks.RUBY_END_ORE.get(),
            ModBlocks.RUBY_NETHER_ORE.get(),
            ModBlocks.RUBY_STONE_ORE.get(),
            ModBlocks.RUBY_DEEPSLATE_ORE.get()
    );

    protected static final List<ItemLike> AMBER_SMELTABLES = List.of(
            ModBlocks.AMBER_END_ORE.get(),
            ModBlocks.AMBER_NETHER_ORE.get(),
            ModBlocks.AMBER_STONE_ORE.get(),
            ModBlocks.RUBY_DEEPSLATE_ORE.get()
    );

    protected static final List<ItemLike> AMETHYST_SMELTABLES = List.of(
            ModBlocks.AMETHYST_END_ORE.get(),
            ModBlocks.AMETHYST_NETHER_ORE.get(),
            ModBlocks.AMETHYST_STONE_ORE.get(),
            ModBlocks.AMETHYST_DEEPSLATE_ORE.get()
    );

    protected static final List<ItemLike> JADE_SMELTABLES = List.of(
            ModBlocks.JADE_END_ORE.get(),
            ModBlocks.JADE_NETHER_ORE.get(),
            ModBlocks.JADE_STONE_ORE.get(),
            ModBlocks.JADE_DEEPSLATE_ORE.get(),
            ModBlocks.JADE_OBSIDIAN_ORE.get()
    );*/

    @Override
    protected void buildRecipes(RecipeOutput output) {
        //!GENERATE

        oreBlasting(output, List.of(ModItems.UNREFINED_CARBON_STEEL.get(), ModItems.CLAYMORE.get(), ModItems.CARBON_STEEL_CROSS_GUARD.get(), ModItems.ADVANCED_HANDLE.get(), ModItems.SHARPENED_BLADE.get()), RecipeCategory.MISC, ModItems.CARBON_STEEL_INGOT.get(), 1f, 100, "carbon_steel_ingot");

        oreBlasting(output, List.of(ModItems.UNREFINED_STEEL.get(), ModItems.STEEL_SWORD.get()), RecipeCategory.MISC, ModItems.STEEL_INGOT.get(), 1f, 100, "steel_ingot");

        oreSmelting(output, List.of(ModItems.SCRAP_INGOT.get(), ModItems.DAMASK_SWORD.get(), ModItems.DAMASK_KNIFE.get(), ModItems.HANDLE.get()), RecipeCategory.MISC, ModItems.DAMASK_INGOT.get(), 0.5f, 200, "damask_ingot");
        oreBlasting(output, List.of(ModItems.SCRAP_INGOT.get(), ModItems.DAMASK_SWORD.get(), ModItems.DAMASK_KNIFE.get(), ModItems.HANDLE.get()), RecipeCategory.MISC, ModItems.DAMASK_INGOT.get(), 1f, 100, "damask_ingot");

        oreSmelting(output, List.of(ModItems.SCRAP_IRON_INGOT.get(), ModItems.REINFORCED_IRON_SWORD.get()), RecipeCategory.MISC, ModItems.REINFORCED_IRON_INGOT.get(), 0.5f, 200, "reinforced_iron_ingot");
        oreBlasting(output, List.of(ModItems.SCRAP_IRON_INGOT.get(), ModItems.REINFORCED_IRON_SWORD.get()), RecipeCategory.MISC, ModItems.REINFORCED_IRON_INGOT.get(), 1f, 100, "reinforced_iron_ingot");

        oreSmelting(output, List.of(ModBlocks.JADE_END_ORE.get(), ModBlocks.JADE_NETHER_ORE.get(), ModBlocks.JADE_OBSIDIAN_ORE.get(), ModBlocks.JADE_STONE_ORE.get(), ModBlocks.JADE_DEEPSLATE_ORE.get()), RecipeCategory.MISC, ModItems.JADE_GEMSTONE.get(), 2f, 100, "jade_gemstone");
        oreBlasting(output, List.of(ModBlocks.JADE_END_ORE.get(), ModBlocks.JADE_NETHER_ORE.get(), ModBlocks.JADE_OBSIDIAN_ORE.get(), ModBlocks.JADE_STONE_ORE.get(), ModBlocks.JADE_DEEPSLATE_ORE.get()), RecipeCategory.MISC, ModItems.JADE_GEMSTONE.get(), 2f, 100, "jade_gemstone");

        oreSmelting(output, List.of(ModBlocks.RUBY_END_ORE.get(), ModBlocks.RUBY_NETHER_ORE.get(), ModBlocks.RUBY_OBSIDIAN_ORE.get(), ModBlocks.RUBY_STONE_ORE.get(), ModBlocks.RUBY_DEEPSLATE_ORE.get()), RecipeCategory.MISC, ModItems.RUBY_GEMSTONE.get(), 2f, 100, "ruby_gemstone");
        oreBlasting(output, List.of(ModBlocks.RUBY_END_ORE.get(), ModBlocks.RUBY_NETHER_ORE.get(), ModBlocks.RUBY_OBSIDIAN_ORE.get(), ModBlocks.RUBY_STONE_ORE.get(), ModBlocks.RUBY_DEEPSLATE_ORE.get()), RecipeCategory.MISC, ModItems.RUBY_GEMSTONE.get(), 2f, 100, "ruby_gemstone");

        oreSmelting(output, List.of(ModBlocks.AMETHYST_END_ORE.get(), ModBlocks.AMETHYST_NETHER_ORE.get(), ModBlocks.AMETHYST_OBSIDIAN_ORE.get(), ModBlocks.AMETHYST_STONE_ORE.get(), ModBlocks.AMETHYST_DEEPSLATE_ORE.get()), RecipeCategory.MISC, ModItems.AMETHYST_GEMSTONE.get(), 2f, 100, "amethyst_gemstone");
        oreBlasting(output, List.of(ModBlocks.AMETHYST_END_ORE.get(), ModBlocks.AMETHYST_NETHER_ORE.get(), ModBlocks.AMETHYST_OBSIDIAN_ORE.get(), ModBlocks.AMETHYST_STONE_ORE.get(), ModBlocks.AMETHYST_DEEPSLATE_ORE.get()), RecipeCategory.MISC, ModItems.AMETHYST_GEMSTONE.get(), 2f, 100, "amethyst_gemstone");

        oreSmelting(output, List.of(ModBlocks.AMBER_END_ORE.get(), ModBlocks.AMBER_NETHER_ORE.get(), ModBlocks.AMBER_OBSIDIAN_ORE.get(), ModBlocks.AMBER_STONE_ORE.get(), ModBlocks.AMBER_DEEPSLATE_ORE.get()), RecipeCategory.MISC, ModItems.AMBER_GEMSTONE.get(), 2f, 100, "amber_gemstone");
        oreBlasting(output, List.of(ModBlocks.AMBER_END_ORE.get(), ModBlocks.AMBER_NETHER_ORE.get(), ModBlocks.AMBER_OBSIDIAN_ORE.get(), ModBlocks.AMBER_STONE_ORE.get(), ModBlocks.AMBER_DEEPSLATE_ORE.get()), RecipeCategory.MISC, ModItems.AMBER_GEMSTONE.get(), 2f, 100, "amber_gemstone");

    }
}