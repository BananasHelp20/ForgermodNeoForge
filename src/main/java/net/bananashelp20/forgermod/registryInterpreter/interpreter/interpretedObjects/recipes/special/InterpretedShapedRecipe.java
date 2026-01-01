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
        this.category = category;
    }

    @Override
    public String toString() {
        String[] result = getCorrectItemWithType(reslutItem.split(" "));
        String[] currentItem;
        if (pattern[0].equals("XXX")) {
            pattern[0] = "";
        }
        if (pattern[2].equals("XXX")) {
            pattern[2] = "";
        }

        if (pattern[0].endsWith("X") && pattern[1].endsWith("X") && pattern[2].endsWith("X")) {
            pattern[0] = "" + pattern[0].charAt(0) + pattern[0].charAt(1);
            pattern[1] = "" + pattern[1].charAt(0) + pattern[1].charAt(1);
            pattern[2] = "" + pattern[2].charAt(0) + pattern[2].charAt(1);
        }
        if (pattern[0].startsWith("X") && pattern[1].startsWith("X") && pattern[2].startsWith("X")) {
            pattern[0] = pattern[0].substring(1);
            pattern[1] = pattern[1].substring(1);
            pattern[2] = pattern[2].substring(1);
        }

        if (pattern[0].endsWith("X") && pattern[1].endsWith("X") && pattern[2].endsWith("X") && pattern[0].isEmpty()) {
            pattern[1] = "" + pattern[1].charAt(0) + pattern[1].charAt(1);
            pattern[2] = "" + pattern[2].charAt(0) + pattern[2].charAt(1);
        } else if (pattern[0].endsWith("X") && pattern[1].endsWith("X") && pattern[2].endsWith("X") && pattern[2].isEmpty()) {
            pattern[0] = "" + pattern[0].charAt(0) + pattern[0].charAt(1);
            pattern[1] = "" + pattern[1].charAt(0) + pattern[1].charAt(1);
        }

        if (pattern[0].startsWith("X") && pattern[1].startsWith("X") && pattern[2].startsWith("X") && pattern[0].isEmpty()) {
            pattern[0] = pattern[0].substring(1);
            pattern[1] = pattern[1].substring(1);
        } else if (pattern[0].startsWith("X") && pattern[1].startsWith("X") && pattern[2].startsWith("X") && pattern[2].isEmpty()) {
            pattern[1] = pattern[1].substring(1);
            pattern[2] = pattern[2].substring(1);
        }

        String ret = "        ShapedRecipeBuilder.shaped(RecipeCategory." + category.toUpperCase() + ", " + result[0] + "s." + result[1].toUpperCase() + (result[0].toUpperCase().contains("MOD") ? ".get()" : "") + ")\n" +
                "                .pattern(\"" + pattern[0].replace('X', ' ') +"\")\n" +
                "                .pattern(\"" + pattern[1].replace('X', ' ') + "\")\n" +
                "                .pattern(\"" + pattern[2].replace('X', ' ') + "\")\n";

        for (char i = 'A'; i <= 'Z'; i++) {
            if (meanings.get(i) != null) {
                currentItem = getCorrectItemWithType(meanings.get(i).split(" "));
                ret += "                .define('" + i + "', " + currentItem[0] + "s." + currentItem[1].toUpperCase() + (currentItem[0].toUpperCase().contains("MOD") ? ".get()" : "") + ")\n";
            }
        }
        currentItem = getCorrectItemWithType(unlockedBy.split(" "));
        return ret + "                .unlockedBy(getHasName(" + currentItem[0] + "s." + currentItem[1].toUpperCase() + (currentItem[0].toUpperCase().contains("MOD") ? ".get()" : "") + ")," +
                " has(" + currentItem[0] + "s." + currentItem[1].toUpperCase() + (currentItem[0].toUpperCase().contains("MOD") ? ".get()" : "") + ")).save(output);\n";
    }
}
