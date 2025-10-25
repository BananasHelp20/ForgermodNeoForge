package net.bananashelp20.forgermod.datagen;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.block.ModBlocks;
import net.bananashelp20.forgermod.item.ModItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class ModRecipeProvider extends RecipeProvider implements IConditionBuilder {
    public ModRecipeProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> pRegistries) {
        super(packOutput, pRegistries);
    }

    protected static final List<ItemLike> DAMASK_SMELTABLES = List.of(
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
    );

    @Override
    protected void buildRecipes(RecipeOutput pRecipeOutput) {
        shapedRecipes(pRecipeOutput);
        shapelessRecipes(pRecipeOutput);
        smeltingAndBlastingRecipes(pRecipeOutput);
    }

    private static void shapedRecipes(RecipeOutput output) {

        //weapons
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.REINFORCED_IRON_SWORD.get())
                .pattern(" D ")
                .pattern("CAC")
                .pattern(" D ")
                .define('A', ModItems.SCRAP_IRON_SWORD.get())
                .define('C', ModItems.SCRAP_IRON_INGOT.get())
                .define('D', ModItems.REINFORCED_IRON_INGOT.get())
                .unlockedBy(getHasName(ModItems.REINFORCED_IRON_INGOT.get()), has(ModItems.REINFORCED_IRON_INGOT.get())).save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SCRAP_IRON_SWORD.get())
                .pattern(" B ")
                .pattern("CAC")
                .pattern(" B ")
                .define('A', Items.IRON_SWORD)
                .define('C', Items.COPPER_INGOT)
                .define('B', ModItems.SCRAP_IRON_INGOT.get())
                .unlockedBy(getHasName(ModItems.SCRAP_IRON_INGOT.get()), has(ModItems.SCRAP_IRON_INGOT.get())).save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.DAMASK_SWORD.get())
                .pattern("A")
                .pattern("A")
                .pattern("B")
                .define('A', ModItems.DAMASK_INGOT.get())
                .define('B', ModItems.HANDLE.get())
                .unlockedBy(getHasName(ModItems.DAMASK_INGOT.get()), has(ModItems.DAMASK_INGOT.get())).save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.STEEL_SWORD.get())
                .pattern("A")
                .pattern("A")
                .pattern("B")
                .define('A', ModItems.STEEL_INGOT.get())
                .define('B', ModItems.HANDLE.get())
                .unlockedBy(getHasName(ModItems.STEEL_INGOT.get()), has(ModItems.STEEL_INGOT.get())).save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.DAMASK_KNIFE.get())
                .pattern(" ")
                .pattern("A")
                .pattern("B")
                .define('A', ModItems.DAMASK_INGOT.get())
                .define('B', ModItems.HANDLE.get())
                .unlockedBy(getHasName(ModItems.DAMASK_INGOT.get()), has(ModItems.DAMASK_INGOT.get())).save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.CLAYMORE.get())
                .pattern("A")
                .pattern("B")
                .pattern("C")
                .define('A', ModItems.SHARPENED_BLADE.get())
                .define('B', ModItems.CARBON_STEEL_CROSS_GUARD.get())
                .define('C', ModItems.ADVANCED_HANDLE.get())
                .unlockedBy(getHasName(ModItems.DAMASK_INGOT.get()), has(ModItems.DAMASK_INGOT.get())).save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.CLAYMORE.get())
                .pattern("A")
                .pattern("B")
                .pattern("C")
                .define('A', ModItems.SHARPENED_BLADE.get())
                .define('B', ModItems.CARBON_STEEL_CROSS_GUARD.get())
                .define('C', ModItems.ADVANCED_HANDLE.get())
                .unlockedBy(getHasName(ModItems.DAMASK_INGOT.get()), has(ModItems.DAMASK_INGOT.get())).save(output, ForgerMod.MOD_ID + ":claymore_from_simple_crafting");

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.CLAYMORE.get())
                .pattern(" A ")
                .pattern("DBE")
                .pattern(" C ")
                .define('A', ModItems.SHARPENED_BLADE.get())
                .define('B', ModItems.RUSTY_CLAYMORE.get())
                .define('C', ModItems.HANDLE.get())
                .define('D', ModItems.DAMASK_INGOT.get())
                .define('E', ModItems.CARBON_STEEL_INGOT.get())
                .unlockedBy(getHasName(ModItems.DAMASK_INGOT.get()), has(ModItems.DAMASK_INGOT.get())).save(output, ForgerMod.MOD_ID + ":claymore_from_upgrading_rusty_claymore");

        //items
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.ADVANCED_HANDLE.get())
                .pattern(" C ")
                .pattern("ABA")
                .pattern(" C ")
                .define('A', ModItems.DAMASK_INGOT.get())
                .define('B', ModItems.HANDLE.get())
                .define('C', ModItems.CARBON_STEEL_INGOT.get())
                .unlockedBy(getHasName(ModItems.CARBON_STEEL_INGOT.get()), has(ModItems.CARBON_STEEL_INGOT.get())).save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.HANDLE.get())
                .pattern("   ")
                .pattern("ABA")
                .pattern("   ")
                .define('A', ModItems.DAMASK_INGOT.get())
                .define('B', Items.STICK)
                .unlockedBy(getHasName(ModItems.DAMASK_INGOT.get()), has(ModItems.DAMASK_INGOT.get())).save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.CARBON_STEEL_CROSS_GUARD.get())
                .pattern("C C")
                .pattern("ABA")
                .pattern(" C ")
                .define('A', ModItems.DAMASK_INGOT.get())
                .define('B', Items.NETHERITE_INGOT)
                .define('C', ModItems.CARBON_STEEL_INGOT.get())
                .unlockedBy(getHasName(ModItems.DAMASK_INGOT.get()), has(ModItems.DAMASK_INGOT.get())).save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SHARPENED_BLADE.get())
                .pattern("B")
                .pattern("A")
                .pattern("B")
                .define('A', ModItems.DAMASK_INGOT.get())
                .define('B', ModItems.CARBON_STEEL_INGOT.get())
                .unlockedBy(getHasName(ModItems.DAMASK_INGOT.get()), has(ModItems.DAMASK_INGOT.get())).save(output);


        //ingredients
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.UNREFINED_CARBON_STEEL.get())
                .pattern("BAA")
                .pattern("ACA")
                .pattern("AAB")
                .define('A', Items.COAL_BLOCK)
                .define('B', Items.DIAMOND)
                .define('C', ModItems.STEEL_INGOT.get())
                .unlockedBy(getHasName(ModItems.UNREFINED_STEEL.get()), has(ModItems.UNREFINED_STEEL.get())).save(output, ForgerMod.MOD_ID + ":unrefined_carbon_steel_from_primal_crafting");

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.UNREFINED_STEEL.get())
                .pattern("ABA")
                .pattern("BCB")
                .pattern("ABA")
                .define('A', Items.IRON_BLOCK)
                .define('B', Items.COAL_BLOCK)
                .define('C', Items.NETHERITE_INGOT)
                .unlockedBy(getHasName(Items.NETHERITE_INGOT), has(Items.NETHERITE_INGOT)).save(output, ForgerMod.MOD_ID + ":unrefined_steel_from_primal_crafting");

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SCRAP_INGOT.get())
                .pattern("AAA")
                .pattern("BCB")
                .pattern("AAA")
                .define('A', Items.IRON_INGOT)
                .define('B', Items.NETHERITE_SCRAP)
                .define('C', Items.NETHERITE_INGOT)
                .unlockedBy(getHasName(Items.NETHERITE_INGOT), has(Items.NETHERITE_INGOT)).save(output, ForgerMod.MOD_ID + ":scrap_ingot_from_primal_crafting");

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModItems.SCRAP_IRON_INGOT.get())
                .pattern("AAA")
                .pattern("ACA")
                .pattern("ABA")
                .define('A', Items.IRON_INGOT)
                .define('B', Items.COPPER_INGOT)
                .define('C', Items.NETHERITE_INGOT)
                .unlockedBy(getHasName(Items.NETHERITE_INGOT), has(Items.NETHERITE_INGOT)).save(output, ForgerMod.MOD_ID + ":scrap_iron_ingot_from_primal_crafting");

        //materialblocks
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.PULSITE_BLOCK.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.PULSITE_INGOT.get())
                .unlockedBy(getHasName(ModItems.PULSITE_INGOT.get()), has(ModItems.PULSITE_INGOT.get())).save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.CARBON_STEEL_BLOCK.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.CARBON_STEEL_INGOT.get())
                .unlockedBy(getHasName(ModItems.CARBON_STEEL_INGOT.get()), has(ModItems.CARBON_STEEL_INGOT.get())).save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.STEEL_BLOCK.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.STEEL_INGOT.get())
                .unlockedBy(getHasName(ModItems.STEEL_INGOT.get()), has(ModItems.STEEL_INGOT.get())).save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.OVERGROWN_BLOCK.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.LUSH_INGOT.get())
                .unlockedBy(getHasName(ModItems.LUSH_INGOT.get()), has(ModItems.LUSH_INGOT.get())).save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.VULNUSIUM_BLOCK.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.VULNUSIUM_INGOT.get())
                .unlockedBy(getHasName(ModItems.VULNUSIUM_INGOT.get()), has(ModItems.VULNUSIUM_INGOT.get())).save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.SOMNIUM_BLOCK.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.SOMNIUM_INGOT.get())
                .unlockedBy(getHasName(ModItems.SOMNIUM_INGOT.get()), has(ModItems.SOMNIUM_INGOT.get())).save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.MORSIUM_BLOCK.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.MORSIUM_INGOT.get())
                .unlockedBy(getHasName(ModItems.MORSIUM_INGOT.get()), has(ModItems.MORSIUM_INGOT.get())).save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.DAMASK_BLOCK.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.DAMASK_INGOT.get())
                .unlockedBy(getHasName(ModItems.DAMASK_INGOT.get()), has(ModItems.DAMASK_INGOT.get())).save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.SCRAP_BLOCK.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.SCRAP_INGOT.get())
                .unlockedBy(getHasName(ModItems.SCRAP_INGOT.get()), has(ModItems.SCRAP_INGOT.get())).save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.SCRAP_IRON_BLOCK.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.SCRAP_IRON_INGOT.get())
                .unlockedBy(getHasName(ModItems.SCRAP_IRON_INGOT.get()), has(ModItems.SCRAP_IRON_INGOT.get())).save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.INANISIUM_BLOCK.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.INANISIUM_INGOT.get())
                .unlockedBy(getHasName(ModItems.INANISIUM_INGOT.get()), has(ModItems.INANISIUM_INGOT.get())).save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.IGNISIUM_BLOCK.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.IGNISIUM_INGOT.get())
                .unlockedBy(getHasName(ModItems.IGNISIUM_INGOT.get()), has(ModItems.IGNISIUM_INGOT.get())).save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.REINFORCED_IRON_BLOCK.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.REINFORCED_IRON_INGOT.get())
                .unlockedBy(getHasName(ModItems.REINFORCED_IRON_INGOT.get()), has(ModItems.REINFORCED_IRON_INGOT.get())).save(output);

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ModBlocks.DEVELOPIUM_BLOCK.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItems.DEVELOPIUM_INGOT.get())
                .unlockedBy(getHasName(ModItems.DEVELOPIUM_INGOT.get()), has(ModItems.DEVELOPIUM_INGOT.get())).save(output);
    }

    private static void shapelessRecipes(RecipeOutput output) {
        //block to item conversions
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.DEVELOPIUM_INGOT.get(), 9)
                .requires(ModBlocks.DEVELOPIUM_BLOCK.get())
                .unlockedBy(getHasName(ModBlocks.DEVELOPIUM_BLOCK.get()), has(ModBlocks.DEVELOPIUM_BLOCK.get())).save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.CARBON_STEEL_INGOT.get(), 9)
                .requires(ModBlocks.CARBON_STEEL_BLOCK.get())
                .unlockedBy(getHasName(ModBlocks.CARBON_STEEL_BLOCK.get()), has(ModBlocks.CARBON_STEEL_BLOCK.get())).save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.STEEL_INGOT.get(), 9)
                .requires(ModBlocks.STEEL_BLOCK.get())
                .unlockedBy(getHasName(ModBlocks.STEEL_BLOCK.get()), has(ModBlocks.STEEL_BLOCK.get())).save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.LUSH_INGOT.get(), 9)
                .requires(ModBlocks.OVERGROWN_BLOCK.get())
                .unlockedBy(getHasName(ModBlocks.OVERGROWN_BLOCK.get()), has(ModBlocks.OVERGROWN_BLOCK.get())).save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.VULNUSIUM_INGOT.get(), 9)
                .requires(ModBlocks.VULNUSIUM_BLOCK.get())
                .unlockedBy(getHasName(ModBlocks.VULNUSIUM_BLOCK.get()), has(ModBlocks.VULNUSIUM_BLOCK.get())).save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.SOMNIUM_INGOT.get(), 9)
                .requires(ModBlocks.SOMNIUM_BLOCK.get())
                .unlockedBy(getHasName(ModBlocks.SOMNIUM_BLOCK.get()), has(ModBlocks.SOMNIUM_BLOCK.get())).save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.MORSIUM_INGOT.get(), 9)
                .requires(ModBlocks.MORSIUM_BLOCK.get())
                .unlockedBy(getHasName(ModBlocks.MORSIUM_BLOCK.get()), has(ModBlocks.MORSIUM_BLOCK.get())).save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.DAMASK_INGOT.get(), 9)
                .requires(ModBlocks.DAMASK_BLOCK.get())
                .unlockedBy(getHasName(ModBlocks.DAMASK_BLOCK.get()), has(ModBlocks.DAMASK_BLOCK.get())).save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.SCRAP_INGOT.get(), 9)
                .requires(ModBlocks.SCRAP_BLOCK.get())
                .unlockedBy(getHasName(ModBlocks.SCRAP_BLOCK.get()), has(ModBlocks.SCRAP_BLOCK.get())).save(output, ForgerMod.MOD_ID + ":scrap_ingot_from_block_to_item_crafting");

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.INANISIUM_INGOT.get(), 9)
                .requires(ModBlocks.INANISIUM_BLOCK.get())
                .unlockedBy(getHasName(ModBlocks.INANISIUM_BLOCK.get()), has(ModBlocks.INANISIUM_BLOCK.get())).save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.IGNISIUM_INGOT.get(), 9)
                .requires(ModBlocks.IGNISIUM_BLOCK.get())
                .unlockedBy(getHasName(ModBlocks.IGNISIUM_BLOCK.get()), has(ModBlocks.IGNISIUM_BLOCK.get())).save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.REINFORCED_IRON_INGOT.get(), 9)
                .requires(ModBlocks.REINFORCED_IRON_BLOCK.get())
                .unlockedBy(getHasName(ModBlocks.REINFORCED_IRON_BLOCK.get()), has(ModBlocks.REINFORCED_IRON_BLOCK.get())).save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.PULSITE_INGOT.get(), 9)
                .requires(ModBlocks.PULSITE_BLOCK.get())
                .unlockedBy(getHasName(ModBlocks.PULSITE_BLOCK.get()), has(ModBlocks.PULSITE_BLOCK.get())).save(output);

        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ModItems.SCRAP_IRON_INGOT.get(), 9)
                .requires(ModBlocks.SCRAP_IRON_BLOCK.get())
                .unlockedBy(getHasName(ModBlocks.SCRAP_IRON_BLOCK.get()), has(ModBlocks.SCRAP_IRON_BLOCK.get())).save(output, ForgerMod.MOD_ID + ":damask_scrap_iron_ingot_from_block_to_item_crafting");

    }


        private static void smeltingAndBlastingRecipes(RecipeOutput output) {
        oreSmelting(output, DAMASK_SMELTABLES, RecipeCategory.MISC, ModItems.DAMASK_INGOT.get(), 0.5f, 200, "damask_ingot");
        oreBlasting(output, DAMASK_SMELTABLES, RecipeCategory.MISC, ModItems.DAMASK_INGOT.get(), 1f, 100, "damask_ingot");

        oreBlasting(output, CARBON_STEEL_SMELTABLES, RecipeCategory.MISC, ModItems.CARBON_STEEL_INGOT.get(), 1f, 100, "carbon_steel_ingot");
        oreBlasting(output, STEEL_SMELTABLES, RecipeCategory.MISC, ModItems.STEEL_INGOT.get(), 1f, 100, "steel_ingot");

        oreSmelting(output, REINFORCED_IRON_SMELTABLES, RecipeCategory.MISC, ModItems.REINFORCED_IRON_INGOT.get(), 0.5f, 200, "reinforced_iron_ingot");
        oreBlasting(output, REINFORCED_IRON_SMELTABLES, RecipeCategory.MISC, ModItems.REINFORCED_IRON_INGOT.get(), 1f, 100, "reinforced_iron_ingot");

        oreSmelting(output, JADE_SMELTABLES, RecipeCategory.MISC, ModItems.JADE_GEMSTONE.get(), 2f, 100, "jade_gemstone");
        oreBlasting(output, JADE_SMELTABLES, RecipeCategory.MISC, ModItems.JADE_GEMSTONE.get(), 2f, 100, "jade_gemstone");

        oreSmelting(output, RUBY_SMELTABLES, RecipeCategory.MISC, ModItems.RUBY_GEMSTONE.get(), 2f, 100, "ruby_gemstone");
        oreBlasting(output, RUBY_SMELTABLES, RecipeCategory.MISC, ModItems.RUBY_GEMSTONE.get(), 2f, 100, "ruby_gemstone");

        oreSmelting(output, AMBER_SMELTABLES, RecipeCategory.MISC, ModItems.AMBER_GEMSTONE.get(), 2f, 100, "amber_gemstone");
        oreBlasting(output, AMBER_SMELTABLES, RecipeCategory.MISC, ModItems.AMBER_GEMSTONE.get(), 2f, 100, "amber_gemstone");

        oreSmelting(output, AMETHYST_SMELTABLES, RecipeCategory.MISC, ModItems.AMETHYST_GEMSTONE.get(), 2f, 100, "amethyst_gemstone");
        oreBlasting(output, AMETHYST_SMELTABLES, RecipeCategory.MISC, ModItems.AMETHYST_GEMSTONE.get(), 2f, 100, "amethyst_gemstone");
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
}