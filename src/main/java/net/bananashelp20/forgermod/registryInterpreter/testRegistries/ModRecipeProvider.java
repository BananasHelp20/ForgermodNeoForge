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

        oreSmelting(output, List.of(ModItems.ITEM2.get(), ModItems.SWORD1.get()), RecipeCategory.MISC, ModItems.ITEM1.get(), 0.5f, 200, "item1");

        oreSmelting(output, List.of(ModItems.SWORD1.get(), ModItems.SWORD2.get()), RecipeCategory.MISC, ModItems.ITEM2.get(), 0.5f, 200, "item2");

        oreBlasting(output, List.of(ModItems.ITEM1.get(), ModItems.ITEM2.get()), RecipeCategory.MISC, ModItems.SWORD1.get(), 0.5f, 200, "sword1");

        oreSmelting(output, List.of(ModItems.SWORD2.get(), ModItems.SWORD4.get()), RecipeCategory.MISC, ModItems.ITEM1.get(), 0.5f, 200, "item1");
        oreBlasting(output, List.of(ModItems.SWORD2.get(), ModItems.SWORD4.get()), RecipeCategory.MISC, ModItems.ITEM1.get(), 0.5f, 200, "item1");

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SWORD2.get())
                .pattern("AAA")
                .pattern("B  ")
                .pattern("CCC")
                .define('A', ModItems.SWORD1.get())
                .define('B', ModItems.SWORD2.get())
                .define('C', Items.STICK)
                .unlockedBy(getHasName(ModItems.SWORD2.get()), has(ModItems.SWORD2.get())).save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SWORD1.get())
                .pattern("AHI")
                .pattern("BDE")
                .pattern("CFG")
                .define('A', ModItems.ITEM2.get())
                .define('B', ModItems.ITEM1.get())
                .define('C', Items.STICK)
                .define('D', Blocks.IRON_BLOCK)
                .define('E', Items.IRON_SWORD)
                .define('F', Items.IRON_INGOT)
                .define('G', Blocks.GRASS_BLOCK)
                .define('H', Blocks.DIAMOND_BLOCK)
                .define('I', Blocks.OAK_PLANKS)
                .unlockedBy(getHasName(ModItems.SWORD1.get()), has(ModItems.SWORD1.get())).save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.STICK, 9)
                .requires(ModItems.SWORD2.get())
                .requires(ModItems.SWORD3.get())
                .unlockedBy(getHasName(ModItems.SWORD1.get()), has(ModItems.SWORD1.get())).save(output, ForgerMod.MOD_ID + ":stick_from_shapeless_crafting_6");

    }
}