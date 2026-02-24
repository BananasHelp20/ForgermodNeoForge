package net.bananashelp20.forgermod.registryInterpreter.interpreter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import static net.bananashelp20.forgermod.registryInterpreter.interpreter.RegistryInterpreter.*;
import static net.bananashelp20.forgermod.registryInterpreter.interpreter.ListObjects.TitledTable.*;
import static net.bananashelp20.forgermod.registryInterpreter.interpreter.interpreterHelperClasses.FileIO.getContentFromFile;

public class RegistryInterpreterHelperMethods {
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    public static String unchangedModBlockFileContent = getContentFromFile(modBlocksFile);
    public static String unchangedModRegistryContent = getContentFromFile(modRegistry);
    public static String unchangedModItemTagProviderContent = getContentFromFile(modItemTagProviderFile);
    public static String unchangedModToolTiersFile = getContentFromFile(modToolTiersFile);
    public static String unchangedModBlockStateProviderFile = getContentFromFile(modBlockStateProviderFile);
    public static String unchangedModBlockLootTableProviderFile = getContentFromFile(modBlockLootTableProviderFile);
    public static String unchangedModBlockTagProviderFile = getContentFromFile(modBlockTagProviderFile);
    public static String unchangedModItemModelProviderFile = getContentFromFile(modItemModelProviderFile);
    public static String unchangedModRecipeProviderFile = getContentFromFile(modRecipeProviderFile);
    public static String unchangedModItemsFileContent = getContentFromFile(modItemsFile);
    public static String unchangedModCreativeModeTabsFileContent = getContentFromFile(modCreativeTabsFile);

    public static ArrayList<Integer> getArraylistFromBracketsAsIntegers(ArrayList<String> bracketStringList, int indexExpander) {
        ArrayList<String> temp = getArraylistFromBrackets(bracketStringList, indexExpander);
        ArrayList<Integer> toRet = new ArrayList<>();

        for (int i = 0; i < temp.size(); i++) {
            toRet.add(Integer.parseInt(temp.get(i)));
        }

        return toRet;
    }

    public static void gemstoneInfusionRecipe(ArrayList<ArrayList<String>> recipeStringObjects, ArrayList<String> recipeText, int objIndex, int textIndex, String gemstone) {
        recipeStringObjects.get(objIndex).add("{custom");
        recipeStringObjects.get(objIndex).add(recipeText.get(textIndex));
        recipeStringObjects.get(objIndex).add("[");
        recipeStringObjects.get(objIndex).add("moditem " + gemstone + "_gemstone");
        recipeStringObjects.get(objIndex).add("moditem " + recipeText.get(textIndex+1).split(" ")[1].substring(1, recipeText.get(textIndex+1).split(" ")[1].length()-1));
        recipeStringObjects.get(objIndex).add("moditem gemstone_upgrade_template");
        recipeStringObjects.get(objIndex).add("]");
        recipeStringObjects.get(objIndex).add("[");
        recipeStringObjects.get(objIndex).add("moditem " + recipeText.get(textIndex+1).split(" ")[1].substring(1, recipeText.get(textIndex+1).split(" ")[1].length()-1) + "_" + gemstone);
        recipeStringObjects.get(objIndex).add("]");
    }

    public static String userInput(Scanner s) {
        System.out.println();
        System.out.print(ANSI_CYAN + "#USER> " + ANSI_RESET);
        String line = s.nextLine();
        System.out.println();
        return line;
    }

    public static String userInputWithoutLineBreak(Scanner s) {
        System.out.print(ANSI_CYAN + "#USER> " + ANSI_RESET);
        String line = s.nextLine();
        return line;
    }

    public static void success(String msg) {
        System.out.println(ANSI_GREEN + msg + ANSI_RESET);
    }

    public static void error(String msg) {
        System.out.println(ANSI_RED + msg + ANSI_RESET);
    }

    public static void warning(String msg) {
        System.out.println(ANSI_YELLOW + msg + ANSI_RESET);
    }

    public static void printFileFromList(ArrayList<String> listedFile) {
        for (int i = 0; i < listedFile.size(); i++) {
            System.out.print(listedFile.get(i));
        }
    }

    public static boolean isPartOfItemTags(String searched, boolean ignoreCase) {
        ArrayList<String> itemTags = new ArrayList<>(Arrays.asList(
            "wool",
            "planks",
            "stone_bricks",
            "wooden_buttons",
            "stone_buttons",
            "buttons",
            "wool_carpets",
            "wooden_doors",
            "wooden_stairs",
            "wooden_slabs",
            "wooden_fences",
            "fence_gates",
            "wooden_pressure_plates",
            "wooden_trapdoors",
            "doors",
            "saplings",
            "logs_that_burn",
            "logs",
            "dark_oak_logs",
            "oak_logs",
            "birch_logs",
            "acacia_logs",
            "cherry_logs",
            "jungle_logs",
            "spruce_logs",
            "mangrove_logs",
            "crimson_stems",
            "warped_stems",
            "bamboo_blocks",
            "wart_blocks",
            "banners",
            "sand",
            "smelts_to_glass",
            "stairs",
            "slabs",
            "walls",
            "anvil",
            "rails",
            "leaves",
            "trapdoors",
            "small_flowers",
            "beds",
            "fences",
            "tall_flowers",
            "flowers",
            "piglin_repellents",
            "piglin_loved",
            "ignored_by_piglin_babies",
            "meat",
            "sniffer_food",
            "piglin_food",
            "fox_food",
            "cow_food",
            "goat_food",
            "sheep_food",
            "wolf_food",
            "cat_food",
            "horse_food",
            "horse_tempt_items",
            "camel_food",
            "armadillo_food",
            "bee_food",
            "chicken_food",
            "frog_food",
            "hoglin_food",
            "llama_food",
            "llama_tempt_items",
            "ocelot_food",
            "panda_food",
            "pig_food",
            "rabbit_food",
            "strider_food",
            "strider_tempt_items",
            "turtle_food",
            "parrot_food",
            "parrot_poisonous_food",
            "axolotl_food",
            "gold_ores",
            "iron_ores",
            "diamond_ores",
            "redstone_ores",
            "lapis_ores",
            "coal_ores",
            "emerald_ores",
            "copper_ores",
            "non_flammable_wood",
            "soul_fire_base_blocks",
            "candles",
            "dirt",
            "terracotta",
            "completes_find_tree_tutorial",
            "boats",
            "chest_boats",
            "fishes",
            "signs",
            "creeper_drop_music_discs",
            "coals",
            "arrows",
            "lectern_books",
            "bookshelf_books",
            "beacon_payment_items",
            "stone_tool_materials",
            "stone_crafting_materials",
            "freeze_immune_wearables",
            "dampens_vibrations",
            "cluster_max_harvestables",
            "compasses",
            "hanging_signs",
            "creeper_igniters",
            "noteblock_top_instruments",
            "foot_armor",
            "leg_armor",
            "chest_armor",
            "head_armor",
            "skulls",
            "trimmable_armor",
            "trim_materials",
            "trim_templates",
            "decorated_pot_sherds",
            "decorated_pot_ingredients",
            "swords",
            "axes",
            "hoes",
            "pickaxes",
            "shovels",
            "breaks_decorated_pots",
            "villager_plantable_seeds",
            "dyeable"
        ));

        for (int i = 0; i < itemTags.size(); i++) {
            if (itemTags.get(i).toUpperCase().equalsIgnoreCase(searched)) return true;
        }
        return false;
    }

    public static void removeDuplicatesFromTagList(ArrayList<ArrayList<String>> tags) {
        for (int i = 0; i < tags.size(); i++) {
            removeDuplicatesOf(tags.get(i).get(1), tags, i);
        }
        removeDuplicatesOf("!NO_TOOL", tags, 0);
    }

    private static void removeDuplicatesOf(String s, ArrayList<ArrayList<String>> elements, int fromPoint) {
        for (int i = fromPoint+1; i < elements.size(); i++) {
            if (s.equalsIgnoreCase(elements.get(i).get(1))) {
                elements.remove(i);
                i--;
            }
        }
    }

    public static String getListAsString(ArrayList<String> list) {
        String content = "";
        for (int i = 0; i < list.size(); i++) {
            content += list.get(i) + "\n";
        }
        return content;
    }

    public static void printRegistryFromList(ArrayList<?> o) {
        for (Object object : o) {
            System.out.println(object.toString());
        }
    }

    public static ArrayList<String> subCollection(int index1, int index2, ArrayList<String> arrayList) {
        ArrayList<String> toReturn = new ArrayList<>();
        for (int i = index1; i < arrayList.size() && i < index2; i++) {
            toReturn.add(arrayList.get(i));
        }
        return toReturn;
    }
}