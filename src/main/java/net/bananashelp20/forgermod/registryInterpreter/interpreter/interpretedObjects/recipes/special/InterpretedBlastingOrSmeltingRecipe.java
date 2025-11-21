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
        return "        oreSmelting(output, " + result[1].toUpperCase() + "_SMELTABLES, RecipeCategory." + category + ", " + result[0] + "s." + result[1].toUpperCase() + (result[0].toUpperCase().contains("MOD") ? ".get()" : "") + ", " + smeltingProperties[0] + ", \"" + result[0].toLowerCase() + "\");\n";
    }

    public String getBlastingRecipe() {
        String[] result = getCorrectItemWithType(reslutItem.split(" "));
        return "        oreBlasting(output, " + result[1].toUpperCase() + "_SMELTABLES, RecipeCategory." + category + ", " + result[0] + "s." + result[1].toUpperCase() + (result[0].toUpperCase().contains("MOD") ? ".get()" : "") + ", " + smeltingProperties[1] + ", \"" + result[0].toLowerCase() + "\");\n";
    }

    public String getIngredientList() {
        String currentItem[];
        String ret = "protected static final List<ItemLike> DAMASK_SMELTABLES = List.of(\n";
        for (int i = 0; i < inputItems.size(); i++) {
            currentItem = getCorrectItemWithType(inputItems.get(i).split(" "));
            ret += "            " + currentItem[0] + "s." + currentItem[1].toUpperCase() + (currentItem[0].toUpperCase().contains("MOD") ? ".get()" : "") + ",\n";
        }
        return ret + "    );";
    }

    @Override
    public String toString() {
        String[] result = reslutItem.split(" ");
        String ret;
        if (this.smeltingAndBlasting) {
            ret = getSmeltingRecipe();
            ret += getBlastingRecipe();
        } else if (smelting) {
            ret = getSmeltingRecipe();
        } else {
            ret = getBlastingRecipe();
        }
        return ret;
    }
}
