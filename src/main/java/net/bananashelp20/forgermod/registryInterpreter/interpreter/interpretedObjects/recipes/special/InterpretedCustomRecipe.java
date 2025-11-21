package net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.recipes.special;

import net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.recipes.InterpretedRecipe;

import java.io.File;
import java.util.ArrayList;

public class InterpretedCustomRecipe extends InterpretedRecipe {
    ArrayList<String> itemsNeeded;
    ArrayList<String> resultItems;
    int recipeID; //only important for duplicates
    File recipeClass;
    boolean multipleOutputs;

    public InterpretedCustomRecipe(String recipeClassPath, ArrayList<String> itemsNeeded, ArrayList<String> resultItems, int id) {
        super(itemsNeeded);
        this.itemsNeeded = itemsNeeded;
        this.resultItems = resultItems;
        this.recipeID = id;
        this.recipeClass = new File(recipeClassPath);
        this.multipleOutputs = (resultItems.size() > 1);
    }

    public String getOutputItems() {
        String[] result = resultItems.get(0).split(" ");
        if (!multipleOutputs) {
            return "            new ItemStack(" + result[0] + "s." + result[1].toUpperCase() + (result[0].toUpperCase().contains("MOD") ? ".get()" : "") + "), \n";
        }
        String ret = "            {";
        for (int i = 0; i < resultItems.size(); i++) {
            result = getCorrectItemWithType(resultItems.get(i).split(" "));
            ret += "new ItemStack(" + result[0] + "s." + result[1].toUpperCase() + (result[0].toUpperCase().contains("MOD") ? ".get()" : "") + "), ";
        }
        return ret.substring(0, ret.length()-2) + "},\n";
    }

    public String getInputItems() {
        String ret = "            {";
        String[] result;
        for (int i = 0; i < itemsNeeded.size(); i++) {
            result = getCorrectItemWithType(itemsNeeded.get(i).split(" "));
            ret += result[0] + "s." + result[1].toUpperCase() + (result[0].toUpperCase().contains("MOD") ? ".get()" : "") + ", ";
        }
        return ret.substring(0, ret.length()-2) + "},\n";
    }

    @Override
    public String toString() {
        return getInputItems() + getOutputItems();
    }
}
