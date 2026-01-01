package net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.recipes.special;

import net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.recipes.InterpretedRecipe;

import java.util.ArrayList;

public class InterpretedConversionRecipe extends InterpretedRecipe {
    String category;
    String blockName;
    String itemName;
    String conversion;
    int id;

    public InterpretedConversionRecipe(String category, String item, String conversion, String block, int id) {
        super(new ArrayList<>());
        this.itemName = item;
        this.blockName = block;
        this.category = category;
        this.conversion = conversion;
        this.id = id;
    }

    @Override
    public String toString() {
        String[] result = getCorrectItemWithType(blockName.split(" "));
        String[] currentItem = getCorrectItemWithType(itemName.split(" "));
        String ret = "";
        if (conversion.contains("->")) {
            ret +=  "        ShapedRecipeBuilder.shaped(RecipeCategory." + category.toUpperCase() + ", " + result[0] + "s." + result[1].toUpperCase() + (result[0].toUpperCase().contains("MOD") ? ".get()" : "") + ")\n" +
                    "                .pattern(\"AAA\")\n" +
                    "                .pattern(\"AAA\")\n" +
                    "                .pattern(\"AAA\")\n" +
                    "                .define('A', " + currentItem[0] + "s." + currentItem[1].toUpperCase() + (currentItem[0].toUpperCase().contains("MOD") ? ".get()" : "") + ")\n" +
                    "                .unlockedBy(getHasName(" + currentItem[0] + "s." + currentItem[1].toUpperCase() + (currentItem[0].toUpperCase().contains("MOD") ? ".get()" : "") + ")," +
                    " has(" + currentItem[0] + "s." + currentItem[1].toUpperCase() + (currentItem[0].toUpperCase().contains("MOD") ? ".get()" : "") + ")).save(output);\n";
        }

        if (conversion.equals("<->")) ret += "\n";
        String temp[] = result;
        result = currentItem;
        currentItem = temp;
        if (conversion.contains("<-")) {
            ret += "        ShapelessRecipeBuilder.shapeless(RecipeCategory." + category.toUpperCase() + ", " + result[0] + "s." + result[1].toUpperCase() + (result[0].toUpperCase().contains("MOD") ? ".get()" : "") + ", " + 9 + ")\n" +
                   "                .requires(" + currentItem[0] + "s." + currentItem[1].toUpperCase() + (currentItem[0].toUpperCase().contains("MOD") ? ".get()" : "") + ")\n" +
                   "                .unlockedBy(getHasName(" + currentItem[0] + "s." + currentItem[1].toUpperCase() + ".get()), has(" + currentItem[0] + "s." + currentItem[1].toUpperCase() + ".get())).save(output, ForgerMod.MOD_ID + \":" + result[1].toLowerCase() + "_from_shapeless_crafting_" + id + "\");\n";
        }
        return ret;
    }
}
