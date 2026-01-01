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
        this.category = category;
    }

    @Override
    public String toString() {
        String[] result = getCorrectItemWithType(reslutItem.split(" "));
        String[] unlockedByItem = getCorrectItemWithType(unlockedBy.split(" "));
        String[] currentItem;
        String ret = "        ShapelessRecipeBuilder.shapeless(RecipeCategory." + category.toUpperCase() + ", " + result[0] + "s." + result[1].toUpperCase() + (result[0].toUpperCase().contains("MOD") ? ".get()" : "") + ", " + resltCount + ")\n";
        for (int i = 0; i < itemsNeeded.size(); i++) {
            currentItem = getCorrectItemWithType(itemsNeeded.get(i).split(" "));
            ret += "                .requires(" + currentItem[0] + "s." + currentItem[1].toUpperCase() + (currentItem[0].toUpperCase().contains("MOD") ? ".get()" : "") + ")\n";
        }
        return ret + "                .unlockedBy(getHasName(" + unlockedByItem[0] + "s." + unlockedByItem[1].toUpperCase() + ".get()), has(" + unlockedByItem[0] + "s." + unlockedByItem[1].toUpperCase() + (result[0].toUpperCase().contains("MOD") ? ".get()" : "") + ")).save(output, ForgerMod.MOD_ID + \":" + result[1].toLowerCase() + "_from_shapeless_crafting_" + recipeID + "\");\n";
    }
}
