package net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.recipes.special;

import net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.recipes.InterpretedRecipe;

import java.util.ArrayList;

public class InterpretedBlastingOrSmeltingRecipe extends InterpretedRecipe {
    ArrayList<String> inputItems;
    String reslutItem;
    int recipeID; //only important for duplicates
    boolean smeltingAndBlasting = false;
    boolean smelting = false; //false --> blasting
    String category;
    String[] smeltingProperties = new String[2];

    public InterpretedBlastingOrSmeltingRecipe(boolean smeltingAndBlasting, String category, ArrayList<String> inputItems, String resultItem, int id, String cookingProperties, String blastingProperties) {
        super(inputItems);
        this.reslutItem = resultItem;
        this.recipeID = id;
        this.inputItems = inputItems;
        this.smeltingAndBlasting = smeltingAndBlasting;
        this.category = category;
        smeltingProperties[0] = cookingProperties;
        smeltingProperties[1] = blastingProperties;
    }

    public InterpretedBlastingOrSmeltingRecipe(String category, ArrayList<String> inputItems, String resultItem, boolean smelting, int id, String smeltingProperties) {
        super(inputItems);
        this.reslutItem = resultItem;
        this.recipeID = id;
        this.inputItems = inputItems;
        this.smelting = smelting;
        this.category = category;
        this.smeltingProperties[0] = smeltingProperties;
    }

    public String getSmeltingRecipe() {
        String[] result = getCorrectItemWithType(reslutItem.split(" "));
        return "        oreSmelting(output, " + getIngredientList() + ", RecipeCategory." + category.toUpperCase() + ", " + result[0] + "s." + result[1].toUpperCase() + (result[0].toUpperCase().contains("MOD") ? ".get()" : "") + ", " + smeltingProperties[0].trim() + ", \"" + result[1].toLowerCase() + "\");\n";
    }

    public String getBlastingRecipe() {
        String[] result = getCorrectItemWithType(reslutItem.split(" "));
        return "        oreBlasting(output, " + getIngredientList() +  ", RecipeCategory." + category.toUpperCase() + ", " + result[0] + "s." + result[1].toUpperCase() + (result[0].toUpperCase().contains("MOD") ? ".get()" : "") + ", " + smeltingProperties[0].trim() + ", \"" + result[1].toLowerCase() + "\");\n";
    }

    public String getIngredientList() {
        String currentItem[];
        String ret = "List.of(";
        for (int i = 0; i < inputItems.size(); i++) {
            currentItem = getCorrectItemWithType(inputItems.get(i).split(" "));
            ret += currentItem[0] + "s." + currentItem[1].toUpperCase() + (currentItem[0].toUpperCase().contains("MOD") ? ".get()" : "") + ", ";
        }
        return ret.substring(0, ret.length()-2) + ")";
    }

    @Override
    public String toString() {
        String[] result = getCorrectItemWithType(reslutItem.split(" "));
        String ret;
        if (this.smeltingAndBlasting) {
            ret = getSmeltingRecipe();
            ret +=  "        oreBlasting(output, " + getIngredientList() +  ", RecipeCategory." + category.toUpperCase() + ", " + result[0] + "s." + result[1].toUpperCase() + (result[0].toUpperCase().contains("MOD") ? ".get()" : "") + ", " + smeltingProperties[1].trim() + ", \"" + result[1].toLowerCase() + "\");\n";
        } else if (smelting) {
            ret = getSmeltingRecipe();
        } else {
            ret = getBlastingRecipe();
        }
        return ret;
    }
}
