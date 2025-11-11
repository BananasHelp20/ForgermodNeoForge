package net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.recipes.special;

import net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.recipes.InterpretedRecipe;

import java.util.ArrayList;

public class InterpretedShapelessRecipe extends InterpretedRecipe {
    ArrayList<String> itemsNeeded;
    String reslutItem;
    int recipeID; //only important for duplicates
    String unlockedBy;
    String category;
    int resltCount;

    public InterpretedShapelessRecipe(ArrayList<String> itemsNeeded, String category,  String unlockedBy, String resultItem, int resultCount, int id) {
        super(itemsNeeded);
        this.itemsNeeded = itemsNeeded;
        this.reslutItem = resultItem;
        this.recipeID = id;
        this.unlockedBy = unlockedBy;
        this.resltCount = resultCount;
    }

    @Override
    public String toString() {
        String[] result = reslutItem.split(" ");
        return "ShapelessRecipeBuilder.shapeless(RecipeCategory." + category.toUpperCase() + ", " + result[0] + "s." + result[1].toUpperCase() + (result[0].toUpperCase().contains("MOD") ? ".get()" : "") + ", " + resltCount + ")\n" +
                "                .requires(ModBlocks.SCRAP_IRON_BLOCK.get())\n" +
                "                .unlockedBy(getHasName(ModBlocks." + unlockedBy.toUpperCase() + ".get()), has(ModBlocks." + unlockedBy.toUpperCase() + ".get())).save(output, ForgerMod.MOD_ID + \":" + result[1].toLowerCase() + "_from_shapeless_crafting" + recipeID + "\");\n";
    }
}
