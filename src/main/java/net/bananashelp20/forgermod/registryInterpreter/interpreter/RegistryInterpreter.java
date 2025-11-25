package net.bananashelp20.forgermod.registryInterpreter.interpreter;

import net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.blocks.InterpretedBlock;
import net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.blocks.special.InterpretedComplexBlock;
import net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.blocks.special.InterpretedSimpleBlock;
import net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.blocks.special.InterpretedSpecialBlock;
import net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.creativeTabs.InterpretedCreativeTab;
import net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.items.InterpretedItem;
import net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.items.special.*;
import net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.recipes.InterpretedRecipe;
import net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.recipes.special.InterpretedBlastingOrSmeltingRecipe;
import net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.recipes.special.InterpretedCustomRecipe;
import net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.recipes.special.InterpretedShapedRecipe;
import net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.recipes.special.InterpretedShapelessRecipe;
import net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.toolTiers.InterpretedToolTier;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

public class RegistryInterpreter {
    public static void main(String[] args) throws FileNotFoundException {
        try {
            if (!generateCode()) {
                System.out.print(ANSI_RED + "#SYSTEM@INFO> CRITICAL: Interpreter was interrupted during " + (stillGenerating ? "generating" : "writing") + " phase, because an " + ANSI_RESET + ANSI_BLACK + " ERROR " + ANSI_RESET + ANSI_RED + " occured" + ANSI_RESET);
                System.out.print(ANSI_RED + "#SYSTEM@INFO> Trying to restore all overridden code!" + ANSI_RESET);
                rewriteAllAfterError();
                System.out.print(ANSI_RED + "#SYSTEM@INFO> Restored all overriden code!" + ANSI_RESET);
                throw new FileNotFoundException(ANSI_RED + "Code could not be generated, an Error occurred" + ANSI_RESET);
            }
        } catch (Exception e) {
            System.out.print(ANSI_RED + "#SYSTEM@INFO> CRITICAL: Interpreter was interrupted during " + (stillGenerating ? "generating" : "writing") + " phase!" + ANSI_RESET);
            System.out.print(ANSI_RED + "#SYSTEM@INFO> Trying to restore all overridden code!" + ANSI_RESET);
            rewriteAllAfterError();
            System.out.print(ANSI_RED + "#SYSTEM@INFO> Restored all overriden code" + ANSI_RESET);
            throw e;
        }
    }
    private static boolean stillGenerating = true;
    public static File blockFile = new File("./src/main/java/net/bananashelp20/forgermod/registryInterpreter/regFileSources/blocks.willi");
    public static File itemFile = new File("./src/main/java/net/bananashelp20/forgermod/registryInterpreter/regFileSources/items.willi");
    public static File creativeTabFile = new File("./src/main/java/net/bananashelp20/forgermod/registryInterpreter/regFileSources/creativeTabs.willi");
    public static File upgradeList = new File("./src/main/java/net/bananashelp20/forgermod/registryInterpreter/regFileSources/itemUpgradeList.txt");
    public static File recipeFile = new File("./src/main/java/net/bananashelp20/forgermod/registryInterpreter/regFileSources/recipes.willi");
    public static File toolTierFile = new File("./src/main/java/net/bananashelp20/forgermod/registryInterpreter/regFileSources/toolTiers.willi");
    public static File modItemsFile = new File("./src/main/java/net/bananashelp20/forgermod/registryInterpreter/testRegistries/ModItems.java");
    public static File modCreativeModeTabsFile = new File("./src/main/java/net/bananashelp20/forgermod/registryInterpreter/testRegistries/ModCreativeModeTabs.java");
//    public static File modRegistry = new File("./src/main/java/net/bananashelp20/forgermod/registryInterpreter/testRegistries/TestRegistryClass.java");
    public static File modBlockFile = new File("./src/main/java/net/bananashelp20/forgermod/registryInterpreter/testRegistries/ModBlocks.java");

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    static ArrayList<InterpretedItem> items = getAllItems();
    static ArrayList<InterpretedBlock> blocks = getAllBlocks();
    static ArrayList<InterpretedRecipe> recipes = getAllRecipes();
    static ArrayList<InterpretedCreativeTab> creativeTabs = getAllCreativeTabs();
    static ArrayList<InterpretedToolTier> toolTiers = getAllToolTiers();

    static String unchangedModBlockFileContent = getContentFromFile(modBlockFile);
//    static String unchangedModRegistryContent = getContentFromFile(modRegistry);
    static String unchangedModItemsFileContent = getContentFromFile(modItemsFile);
    static String unchangedModCreativeModeTabsFileContent = getContentFromFile(modCreativeModeTabsFile);

    public static boolean generateCode() {
        if (!(modBlockFile.exists() && modBlockFile.canWrite() && modBlockFile.canRead()
                && modItemsFile.exists() && modItemsFile.canWrite() && modItemsFile.canRead()
                && modCreativeModeTabsFile.exists() && modCreativeModeTabsFile.canWrite() && modCreativeModeTabsFile.canRead()
//                && modRegistry.exists() && modRegistry.canWrite() && modRegistry.canRead()
                && blockFile.exists() && blockFile.canRead()
                && itemFile.exists() && itemFile.canRead()
                && creativeTabFile.exists() && creativeTabFile.canRead()
        )) {
            return false;
        }
        Scanner userHelper = new Scanner(System.in);
        String input;
        warning("****************************************************************************************************************************************\n" +
                "* Generating the code means OVERRIDING ALL CURRENT CODE that's been written to: all datagen files, ModItems, ModBlocks, RegistryClass, *\n* ModToolTiers and ModCreativeModeTabs." +
                "Other Files might also be affected, and there is no guarantee the code works as it should.      *\n* Please make sure to " + ANSI_RESET + ANSI_PURPLE + "//!PRESERVE " + ANSI_RESET +
                ANSI_YELLOW + "every important code line that shall not be overridden                                               *\n" +
                "* If you wish to continue anyways, type in " + ANSI_RESET + ANSI_GREEN + "\"!START\"" + ANSI_RESET + ANSI_YELLOW + ".                                            " +
                "                                       *\n" +
                "* If you want to stop without any code being generated, type in the command "+ ANSI_RESET + ANSI_RED + "\"!STOP\"" + ANSI_RESET + ANSI_YELLOW +
                "                                                    *\n" +
                "****************************************************************************************************************************************");
        while (!(input = userInputWithoutLineBreak(userHelper)).contains("!START")) {
            if (input.contains("!STOP")) {
                return true;
            }
        }
        System.out.println(ANSI_RED + "#SYSTEM@INFO> starting with generating phase" + ANSI_RESET);


        printRegistryFromList(toolTiers);
        System.out.print(ANSI_RED + "#SYSTEM@INFO> " + ANSI_RESET);
        success("Successfully generated tool tier objects");
//        writeToolTierCode();
//        success("Successfully wrote tool tier objects to files");
        printRegistryFromList(items);
        System.out.print(ANSI_RED + "#SYSTEM@INFO> " + ANSI_RESET);
        success("Successfully generated item objects");
//        writeItemCode();
//        success("Successfully wrote item objects to files");
        printRegistryFromList(blocks);
        System.out.print(ANSI_RED + "#SYSTEM@INFO> " + ANSI_RESET);
        success("Successfully generated block objects");
//        writeBlockCode();
//        success("Successfully wrote block tab objects to files");
        printRegistryFromList(creativeTabs);
        System.out.print(ANSI_RED + "#SYSTEM@INFO> " + ANSI_RESET);
        success("Successfully generated creative tab objects");
//        writeCreativeTabCode();
//        success("Successfully wrote creative tab objects to files");
        printRegistryFromList(recipes);
        System.out.print(ANSI_RED + "#SYSTEM@INFO> " + ANSI_RESET);
        success("Successfully generated recipe objects");
//        writeRecipeCode();
//        success("Successfully wrote recipe objects to files");
        System.out.println(ANSI_RED + "#SYSTEM@INFO> Successfully completed generating phase" + ANSI_RESET);
        System.out.println(ANSI_RED + "#SYSTEM@INFO> starting with writing phase" + ANSI_RESET);
        return true;
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

    public static ArrayList<InterpretedCreativeTab> getAllCreativeTabs() {
        ArrayList<InterpretedCreativeTab> interpretedToolTiers = new ArrayList<>();
        ArrayList<String> toolTierText = getContentFromFileAsList(creativeTabFile);
        ArrayList<ArrayList<String>> toolTierStringObjects = new ArrayList<>();
        InterpretedCreativeTab toolTierToAdd = null;
        int ctr = -1;

        for (int i = 0; i < toolTierText.size(); i++) {
            if (toolTierText.get(i).contains("{")) {
                toolTierStringObjects.add(new ArrayList<>());
                ctr++;
                for (int j = i; j < toolTierText.size() && !toolTierText.get(j).contains("}"); j++) {
                    toolTierStringObjects.get(ctr).add(toolTierText.get(j));
                    i = j;
                }
            }
        }

        for (int i = 0; i < toolTierStringObjects.size(); i++) {
            if (toolTierStringObjects.get(i).getFirst().contains("{simpleTab")) {
                toolTierToAdd = new InterpretedCreativeTab(toolTierStringObjects.get(i).get(1), toolTierStringObjects.get(i).get(2), toolTierStringObjects.get(i).get(3));
            }
            interpretedToolTiers.add(toolTierToAdd);
        }

        return interpretedToolTiers;
    }

    public static ArrayList<InterpretedToolTier> getAllToolTiers() {
        ArrayList<InterpretedToolTier> interpretedToolTiers = new ArrayList<>();
        ArrayList<String> toolTierText = getContentFromFileAsList(toolTierFile);
        ArrayList<ArrayList<String>> toolTierStringObjects = new ArrayList<>();
        InterpretedToolTier toolTierToAdd = null;
        int ctr = -1;

        for (int i = 0; i < toolTierText.size(); i++) {
            if (toolTierText.get(i).contains("{")) {
                toolTierStringObjects.add(new ArrayList<>());
                ctr++;
                for (int j = i; j < toolTierText.size() && !toolTierText.get(j).contains("}"); j++) {
                    toolTierStringObjects.get(ctr).add(toolTierText.get(j));
                    i = j;
                }
            }
        }

        for (int i = 0; i < toolTierStringObjects.size(); i++) {
            if (toolTierStringObjects.get(i).getFirst().contains("{normal")) {
                toolTierToAdd = new InterpretedToolTier(toolTierStringObjects.get(i).get(1), toolTierStringObjects.get(i).get(2), toolTierStringObjects.get(i).get(3));
            }
            interpretedToolTiers.add(toolTierToAdd);
        }

        return interpretedToolTiers;
    }

    public static ArrayList<InterpretedRecipe> getAllRecipes() {
        ArrayList<InterpretedRecipe> interpretedRecipes = new ArrayList<>();
        ArrayList<String> recipeText = getContentFromFileAsList(recipeFile);
        ArrayList<String> inputItems;
        ArrayList<ArrayList<String>> recipeStringObjects = new ArrayList<>();
        InterpretedRecipe recipeToAdd;
        int ctr = -1;
        String[] pattern;
        String[] temp;
        HashMap<Character, String> patternMeaning;

        for (int i = 0; i < recipeText.size(); i++) {
            if (recipeText.get(i).contains("{")) {
                recipeStringObjects.add(new ArrayList<>());
                ctr++;
                for (int j = i; j < recipeText.size() && !recipeText.get(j).contains("}"); j++) {
                    recipeStringObjects.get(ctr).add(recipeText.get(j));
                    i = j;
                }
            }
        }

        for (int i = 0; i < recipeStringObjects.size(); i++) {
            inputItems = new ArrayList<>();
            recipeToAdd = new InterpretedRecipe(new ArrayList<>());
            patternMeaning = new HashMap<>();
            pattern = new String[3];
            if (recipeStringObjects.get(i).getFirst().contains("{smelting")) {
                inputItems.addAll(getContentInBrackets(i, 4, recipeStringObjects));
                recipeToAdd = new InterpretedBlastingOrSmeltingRecipe(recipeStringObjects.get(i).get(1), inputItems, recipeStringObjects.get(i).get(2), true, i, recipeStringObjects.get(i).get(3));
            } else if (recipeStringObjects.get(i).contains("{blasting")) {
                inputItems.addAll(getContentInBrackets(i, 4, recipeStringObjects));
                recipeToAdd = new InterpretedBlastingOrSmeltingRecipe(recipeStringObjects.get(i).get(1), inputItems, recipeStringObjects.get(i).get(2), false, i, recipeStringObjects.get(i).get(3));
            } else if (recipeStringObjects.get(i).contains("{both")) {
                inputItems.addAll(getContentInBrackets(i, 4, recipeStringObjects));
                recipeToAdd = new InterpretedBlastingOrSmeltingRecipe(true, recipeStringObjects.get(i).get(1), inputItems, recipeStringObjects.get(i).get(2), i, recipeStringObjects.get(i).get(3), recipeStringObjects.get(i).get(4));
            } else if (recipeStringObjects.get(i).contains("{shapeless")) {
                inputItems.addAll(getContentInBrackets(i, 5, recipeStringObjects));
                recipeToAdd = new InterpretedShapelessRecipe(inputItems, recipeStringObjects.get(i).get(1), recipeStringObjects.get(i).get(2), recipeStringObjects.get(i).get(3), Integer.parseInt(recipeStringObjects.get(i).get(4)), i);
            } else if (recipeStringObjects.get(i).contains("{shaped")) {
                pattern[0] = recipeStringObjects.get(i).get(2);
                pattern[1] = recipeStringObjects.get(i).get(3);
                pattern[2] = recipeStringObjects.get(i).get(4);
                temp = recipeStringObjects.get(i).get(5).split("->");
                patternMeaning.put(temp[0].charAt(0), temp[1].trim());
                temp = recipeStringObjects.get(i).get(6).split("->");
                patternMeaning.put(temp[0].charAt(0), temp[1].trim());
                temp = recipeStringObjects.get(i).get(7).split("->");
                patternMeaning.put(temp[0].charAt(0), temp[1].trim());
                recipeToAdd = new InterpretedShapedRecipe(recipeStringObjects.get(i).get(1), pattern, patternMeaning,recipeStringObjects.get(i).get(8), recipeStringObjects.get(i).get(9), i);
            } else if (recipeStringObjects.get(i).contains("{custom")) {
                inputItems.addAll(getContentInBrackets(i, 2, recipeStringObjects));
                int x = 2;
                while (!recipeStringObjects.get(i).get(x).contains("]")) {
                    x++;
                }
                ArrayList<String> output = new ArrayList<>(getContentInBrackets(i, x + 1, recipeStringObjects));
                recipeToAdd = new InterpretedCustomRecipe(recipeStringObjects.get(i).get(1), inputItems, output, i);
            }
            interpretedRecipes.add(recipeToAdd);
        }

        return interpretedRecipes;
    }

    private static ArrayList<String> getContentInBrackets(int listIndex, int elementIndex, ArrayList<ArrayList<String>> stringObjects) {
        ArrayList<String> objects = new ArrayList<>();
        for (int i = elementIndex+1; i < stringObjects.get(listIndex).size() && !stringObjects.get(listIndex).get(i).contains("]"); i++) {
            objects.add(stringObjects.get(listIndex).get(i));
        }
        return objects;
    }

    public static ArrayList<InterpretedBlock> getAllBlocks() {
        ArrayList<InterpretedBlock> interpretedBlocks = new ArrayList<>();
        ArrayList<String> blockText = getContentFromFileAsList(blockFile);
        ArrayList<ArrayList<String>> blockStringObjects = new ArrayList<>();
        String dropOtherItem;
        boolean dropOther;
        int indexExpander;
        InterpretedBlock blockToAdd = new InterpretedBlock(new ArrayList<>());
        int ctr = -1;
        for (int i = 0; i < blockText.size(); i++) {
            if (blockText.get(i).contains("{")) {
                blockStringObjects.add(new ArrayList<>());
                ctr++;
                for (int j = i; j < blockText.size() && !blockText.get(j).contains("}"); j++) {
                    blockStringObjects.get(ctr).add(blockText.get(j));
                    i = j;
                }
            }
        }

//        interpretedBlocks.add(new InterpretedSimpleBlock("name", "p", "drop", "", "b", "type", "tab", "tab"));
//        interpretedBlocks.add(new InterpretedSimpleBlock("name2", "p", "drop", "", "b", "type", "tab", "tab"));

        for (int i = 0; i < blockStringObjects.size(); i++) {
            dropOther = false;
            indexExpander = 0;
            dropOtherItem = "";
            blockToAdd = null;
            if (blockStringObjects.get(i).getFirst().contains("{simple")) {
                if (blockStringObjects.get(i).get(4).contains("?")) {
                    dropOther = true;
                    indexExpander++;
                    dropOtherItem = blockStringObjects.get(i).get(4).substring(1);
                }
                blockToAdd = new InterpretedSimpleBlock(blockStringObjects.get(i).get(1), blockStringObjects.get(i).get(2), blockStringObjects.get(i).get(3), dropOtherItem, blockStringObjects.get(i).get(4 + indexExpander), blockStringObjects.get(i).get(5 + indexExpander), blockStringObjects.get(i).get(6 + indexExpander), blockStringObjects.get(i).get(7 + indexExpander));

            } else if (blockStringObjects.get(i).getFirst().contains("{special")) {
                if (blockStringObjects.get(i).get(5).contains("?")) {
                    dropOther = true;
                    indexExpander++;
                    dropOtherItem = blockStringObjects.get(i).get(4).substring(1);
                }
                blockToAdd = new InterpretedSpecialBlock(blockStringObjects.get(i).get(1), blockStringObjects.get(i).get(2),
                        blockStringObjects.get(i).get(3), blockStringObjects.get(i).get(4),
                        dropOtherItem, blockStringObjects.get(i).get(5 + indexExpander),
                        blockStringObjects.get(i).get(6 + indexExpander), blockStringObjects.get(i).get(7 + indexExpander),
                        blockStringObjects.get(i).get(8 + indexExpander));

            } else if (blockStringObjects.get(i).getFirst().contains("{complex")) {
                if (blockStringObjects.get(i).get(4).contains("?")) {
                    dropOther = true;
                    indexExpander++;
                    dropOtherItem = blockStringObjects.get(i).get(4).substring(1);
                }
                blockToAdd = new InterpretedComplexBlock(blockStringObjects.get(i).get(1), blockStringObjects.get(i).get(2),
                        blockStringObjects.get(i).get(3),
                        dropOtherItem, blockStringObjects.get(i).get(4 + indexExpander),
                        blockStringObjects.get(i).get(5 + indexExpander), blockStringObjects.get(i).get(6 + indexExpander), blockStringObjects.get(i).get(7 + indexExpander));
            }
            interpretedBlocks.add(blockToAdd);
        }
        return interpretedBlocks;
    }

    public static ArrayList<InterpretedItem> getAllItems() {
        ArrayList<InterpretedItem> items = new ArrayList<>();
        ArrayList<String> itemText = getContentFromFileAsList(itemFile);
        ArrayList<ArrayList<String>> itemStringObjects = new ArrayList<>();
        ArrayList<String> variants;
        ArrayList<String> properties;
        int ctr = -1;
        for (int i = 0; i < itemText.size(); i++) {
            if (itemText.get(i).contains("{")) {
                itemStringObjects.add(new ArrayList<>());
                ctr++;
                for (int j = i; j < itemText.size() && !itemText.get(j).contains("}"); j++) {
                    itemStringObjects.get(ctr).add(itemText.get(j));
                    i = j;
                }
            }
        }

        for (int j = 0; j < itemStringObjects.size(); j++) {
            if (itemStringObjects.get(j).getFirst().contains("{simpleItem")) {
                items.add(new InterpretedSimpleItem(itemStringObjects.get(j).get(1), itemStringObjects.get(j).get(2), itemStringObjects.get(j).get(3)));
            } else if (itemStringObjects.get(j).getFirst().contains("{specialItem")) {
                if (!itemStringObjects.get(j).get(3).contains("?[E") && itemStringObjects.get(j).get(3).contains("?")) {
                    items.add(new InterpretedSpecialItem(itemStringObjects.get(j).get(1), itemStringObjects.get(j).get(2), itemStringObjects.get(j).get(3).substring(1).trim(), itemStringObjects.get(j).get(4)));
                } else {
                    items.add(new InterpretedSpecialItem(itemStringObjects.get(j).get(1), itemStringObjects.get(j).get(2), itemStringObjects.get(j).get(3)));
                }
            } else if (itemStringObjects.get(j).getFirst().contains("{simpleSword")) {
                items.add(new InterpretedSwordItem(itemStringObjects.get(j).get(1), itemStringObjects.get(j).get(2), itemStringObjects.get(j).get(3), itemStringObjects.get(j).get(4), itemStringObjects.get(j).get(5), itemStringObjects.get(j).get(6)));
            } else if (itemStringObjects.get(j).getFirst().contains("{specialSword")) {
                properties = new ArrayList<>();
                for (int k = 0; k < itemStringObjects.get(j).size() && !itemStringObjects.get(j).get(k).contains(")"); k++) {
                    if (itemStringObjects.get(j).get(k).contains("[") && !itemStringObjects.get(j).get(k).contains("?[E")) {
                        for (int l = k+1; l < itemStringObjects.get(j).size() && !itemStringObjects.get(j).get(l).contains("]"); l++) {
                            properties.add(itemStringObjects.get(j).get(l));
                        }
                    }
                }
                items.add(new InterpretedSpecialSwordItem(itemStringObjects.get(j).get(1), itemStringObjects.get(j).get(2), itemStringObjects.get(j).get(3), itemStringObjects.get(j).get(4), properties, itemStringObjects.get(j).get(5), itemStringObjects.get(j).get(6)));
            } else if (itemStringObjects.get(j).getFirst().contains("{upgradableSword") || itemStringObjects.get(j).getFirst().contains("{upgradeableSword")) {
                variants = new ArrayList<>();
                properties = new ArrayList<>();
                properties.add(itemStringObjects.get(j).get(1));
                properties.add(itemStringObjects.get(j).get(2));
                properties.add(itemStringObjects.get(j).get(3));
                properties.add(itemStringObjects.get(j).get(4));
                properties.add(itemStringObjects.get(j).get(5));
                ctr = 7;
                for (int k = 7; k < itemStringObjects.get(j).size() && !itemStringObjects.get(j).get(k).contains("]"); k++) {
                    variants.add(itemStringObjects.get(j).get(k));
                    ctr++;
                }
                properties.add(itemStringObjects.get(j).get(ctr));
                items.add(new InterpretedItemWithUpgradedVariations(properties.getFirst(), properties.get(1), properties.get(2), properties.get(3), properties.get(4), variants, properties.get(5)));
            }
        }
        return items;
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

    public static String getPartWithoutComment(String s) {
        String[] splitText = s.split("#");
        for (int i = 0; i < splitText.length; i++) {
            if (!splitText[i].contains("//")) {
                return splitText[i];
            }
        }
        return "";
    }

    public static void printFileFromList(ArrayList<String> listedFile) {
        for (int i = 0; i < listedFile.size(); i++) {
            System.out.print(listedFile.get(i));
        }
    }

    public static ArrayList<String> getContentFromFileAsList(File file) {
        Scanner reader;
        ArrayList<String> fileContent = new ArrayList<>();
        try {
            reader = new Scanner(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        while (reader.hasNextLine()) {
            fileContent.add(reader.nextLine() + "\n");
        }

        clearContentFromUnneccesary(fileContent);
        return fileContent;
    }

    public static void clearContentFromUnneccesary(ArrayList<String> content) {
        for (int i = 0; i < content.size(); i++) {
            content.set(i, content.get(i).trim());
            if (content.get(i).contains("#")) {
                content.set(i, getPartWithoutComment(content.get(i)));
            }
            if (getPartWithoutComment(content.get(i)).trim().equals("")) {
                content.remove(i);
                i--;
            }
        }
    }

    static int getWritablePos(File file, String commentCommand) {
        Scanner reader;
        try {
            reader = new Scanner(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        String searcher;
        for (int i = 1; reader.hasNextLine(); i++) {
            searcher = reader.nextLine();
            if (searcher.trim().equals(commentCommand)) {
                reader.close();
                return i + 1;
            }
        }
        return -1;
    }

    static String getWholeFileContentTillGenerate(File file, String command) {
        String saved = "";
        try {
            Scanner reader = new Scanner(file);
            int startGeneratingAtLine = getWritablePos(file, command);
            for (int i = 1; i < startGeneratingAtLine && reader.hasNextLine(); i++) {
                saved += reader.nextLine() + "\n";
            }
            reader.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return saved;
    }

    static String getContentFromFile(File file) {
        Scanner reader;
        try {
            reader = new Scanner(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        String content = "";
        while (reader.hasNextLine()) {
            content += reader.nextLine() + "\n";
        }
        return content;
    }

    public static void rewriteAllAfterError() {
        try {
            FileWriter writer = new FileWriter(modBlockFile);
//            FileWriter writer1 = new FileWriter(modRegistry);
            FileWriter writer2 = new FileWriter(modItemsFile);
            FileWriter writer3 = new FileWriter(modCreativeModeTabsFile);
            writer.write(unchangedModBlockFileContent);
//            writer1.write(unchangedModRegistryContent);
            writer2.write(unchangedModItemsFileContent);
            writer3.write(unchangedModCreativeModeTabsFileContent);
            writer.close();
//            writer1.close();
            writer2.close();
            writer3.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ArrayList<String> getEnchantmentablesFromOptionalParameter(ArrayList<String> filecontent, String name) {
        ArrayList<ArrayList<String>> enchantingTagsForEachItem = new ArrayList<>();
        ArrayList<String> currItem;
        String currName = "";
        for (int i = 0; i < filecontent.size(); i++) {
            if (filecontent.get(i).contains("{")) {
                currName = filecontent.get(i+1).trim();
            }
            if (filecontent.get(i).contains("?[E")) {
                i++;
                currItem = new ArrayList<>();
                currItem.add(currName);
                while (i < filecontent.size() && !filecontent.get(i).contains("?]")) {
                    currItem.add(filecontent.get(i).trim().split("Enchantable:")[0].toUpperCase());
                    i++;
                }
                enchantingTagsForEachItem.add(currItem);
            }
        }
        for (int i = 0; i < enchantingTagsForEachItem.size(); i++) {
            if (enchantingTagsForEachItem.get(i).getFirst().equalsIgnoreCase(name)) {
                return enchantingTagsForEachItem.get(i);
            }
        }
        return new ArrayList<>();
    }
}