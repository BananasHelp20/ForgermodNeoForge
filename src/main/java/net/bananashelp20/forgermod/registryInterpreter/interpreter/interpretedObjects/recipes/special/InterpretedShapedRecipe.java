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
    String category;

    public InterpretedShapedRecipe(String category, String[] pattern, HashMap<Character, String> meanings, String unlockedBy, String resultItem, int id) {
        super(new ArrayList<>(Arrays.asList(resultItem)));
        this.reslutItem = resultItem;
        this.recipeID = id;
        this.pattern = pattern;
        this.meanings = meanings;
        this.unlockedBy = unlockedBy;
    }

    @Override
    public String toString() {
        String[] result = reslutItem.split(" ");
        String[] currentItem;
        String ret = "        ShapedRecipeBuilder.shaped(RecipeCategory." + category.toUpperCase() + ", " + result[0] + "s." + result[1].toUpperCase() + (result[0].toUpperCase().contains("MOD") ? ".get()" : "") + ")\n" +
                "                .pattern(\"" + pattern[0] +"\")\n" +
                "                .pattern(\"" + pattern[1] + "\")\n" +
                "                .pattern(\"" + pattern[2] + "\")\n";
        for (char i = 'A'; i < meanings.size(); i++) {
            if (meanings.get(i) != null) {
                currentItem = meanings.get(i).split(" ");
                ret += "                .define(" + i + ", " + currentItem[0] + "s." + currentItem[1].toUpperCase() + (currentItem[0].toUpperCase().contains("MOD") ? ".get()" : "") + ")\n";
            }
        }
        currentItem = unlockedBy.split(" ");
        return ret + "                .unlockedBy(getHasName(" + currentItem[0] + "s." + currentItem[1].toUpperCase() + (currentItem[0].toUpperCase().contains("MOD") ? ".get()" : "") + ")," +
                " has(" + currentItem[0] + "s." + currentItem[1].toUpperCase() + (currentItem[0].toUpperCase().contains("MOD") ? ".get()" : "") + ")).save(output);\n";
    }
}
