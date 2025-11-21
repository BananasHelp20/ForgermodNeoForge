package net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.recipes;

import java.util.ArrayList;

public class InterpretedRecipe {
    ArrayList<String> recipeItems;
    public InterpretedRecipe(ArrayList<String> recipeItems) {
        this.recipeItems = recipeItems;
    }

    protected static String[] getCorrectItemWithType(String[] item) {
        if (item[0].toUpperCase().contains("MOD")) {
            if (item[0].toUpperCase().contains("ITEM")) {
                item[0] = "ModItem";
            } else {
                item[0] = "ModBlock";
            }
        } else {
            if (item[0].toUpperCase().contains("ITEM")) {
                item[0] = "Item";
            } else {
                item[0] = "Block";
            }
        }
        return item;
    }
}
