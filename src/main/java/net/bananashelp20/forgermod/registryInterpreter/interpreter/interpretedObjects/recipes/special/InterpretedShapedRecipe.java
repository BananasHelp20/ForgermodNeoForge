package net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.recipes.special;

import net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.recipes.InterpretedRecipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class InterpretedShapedRecipe extends InterpretedRecipe {
    String reslutItem;
    int recipeID; //only important for duplicates
    HashMap<Character, String> meanings;
    String[] pattern; //e.g. 0:XAX 1:ABC 2:AXB or 0:BB 1:AX 2: XA
    String unlockedBy;

    public InterpretedShapedRecipe(String[] pattern, HashMap<Character, String> meanings, String unlockedBy, String resultItem, int id) {
        super(new ArrayList<>(Arrays.asList(resultItem)));
        this.reslutItem = resultItem;
        this.recipeID = id;
        this.pattern = pattern;
        this.meanings = meanings;
        this.unlockedBy = unlockedBy;
    }
}
