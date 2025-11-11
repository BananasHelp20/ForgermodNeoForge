package net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.recipes.special;

import net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.recipes.InterpretedRecipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class InterpretedBlastingOrSmeltingRecipe extends InterpretedRecipe {
    String inputItem;
    String reslutItem;
    int recipeID; //only important for duplicates
    boolean smeltingAndBlasting;
    boolean smelting; //false --> blasting

    public InterpretedBlastingOrSmeltingRecipe(boolean smeltingAndBlasting, String inputItem, String resultItem, int id) {
        super(new ArrayList<>(Arrays.asList(inputItem)));
        this.reslutItem = resultItem;
        this.recipeID = id;
        this.inputItem = inputItem;
        this.smeltingAndBlasting = smeltingAndBlasting;
    }

    public InterpretedBlastingOrSmeltingRecipe(String inputItem, String resultItem, boolean smelting, int id) {
        super(new ArrayList<>(Arrays.asList(inputItem)));
        this.reslutItem = resultItem;
        this.recipeID = id;
        this.inputItem = inputItem;
        this.smelting = smelting;
    }
}
