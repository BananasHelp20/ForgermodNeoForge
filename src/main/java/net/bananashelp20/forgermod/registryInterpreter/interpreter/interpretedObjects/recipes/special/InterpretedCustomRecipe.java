package net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.recipes.special;

import net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.recipes.InterpretedRecipe;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class InterpretedCustomRecipe extends InterpretedRecipe {
    ArrayList<String> itemsNeeded;
    String reslutItem;
    int recipeID; //only important for duplicates
    File recipeClass;

    public InterpretedCustomRecipe(File recipeClass, ArrayList<String> itemsNeeded, String resultItem, int id) {
        super(new ArrayList<>(Arrays.asList(resultItem)));
        this.itemsNeeded = itemsNeeded;
        this.reslutItem = resultItem;
        this.recipeID = id;
    }
}
