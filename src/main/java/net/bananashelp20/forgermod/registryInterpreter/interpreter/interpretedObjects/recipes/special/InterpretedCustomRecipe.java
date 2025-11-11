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

    public InterpretedCustomRecipe(File recipeClass, ArrayList<String> itemsNeeded, ArrayList<String> resultItems, boolean multipleOutputs, int id) {
        super(itemsNeeded);
        this.itemsNeeded = itemsNeeded;
        this.resultItems = resultItems;
        this.recipeID = id;
        this.recipeClass = recipeClass;
        this.multipleOutputs = multipleOutputs;
    }

    public String getOutputItems() {
        String[] result = itemsNeeded.get(0).split(" ");
        if (!multipleOutputs) {
            return "new ItemStack(" + result[0] + "s." + result[1].toUpperCase() + (result[0].toUpperCase().contains("MOD") ? ".get()" : "") + "),\n";
        }
        String ret = "{";
        for (int i = 0; i < resultItems.size(); i++) {
            result = itemsNeeded.get(i).split(" ");
            ret += "new ItemStack(" + result[0] + "s." + result[1].toUpperCase() + (result[0].toUpperCase().contains("MOD") ? ".get()" : "") + "), ";
        }
        return ret + "},\n";
    }

    public String getInputItems() {
        String ret = "{";
        String[] result;
        for (int i = 0; i < resultItems.size(); i++) {
            result = itemsNeeded.get(i).split(" ");
            ret += result[0] + "s." + result[1].toUpperCase() + (result[0].toUpperCase().contains("MOD") ? ".get()" : "") + ", ";
        }
        return ret + "},\n";
    }

    @Override
    public String toString() {
        return getInputItems() + "\n\n" + getOutputItems();
    }
}
