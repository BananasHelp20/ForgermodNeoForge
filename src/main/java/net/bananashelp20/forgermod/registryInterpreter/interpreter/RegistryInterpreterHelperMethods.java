package net.bananashelp20.forgermod.registryInterpreter.interpreter;

import com.mojang.realmsclient.dto.BackupList;
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
import org.checkerframework.checker.units.qual.A;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;
import static net.bananashelp20.forgermod.registryInterpreter.interpreter.RegistryInterpreter.*;

public class RegistryInterpreterHelperMethods {

    public static File blockFile = new File("./src/main/java/net/bananashelp20/forgermod/registryInterpreter/regFileSources/blocks.willi");
    public static File itemFile = new File("./src/main/java/net/bananashelp20/forgermod/registryInterpreter/regFileSources/items.willi");
    public static File creativeTabFile = new File("./src/main/java/net/bananashelp20/forgermod/registryInterpreter/regFileSources/creativeTabs.willi");
    public static File upgradeList = new File("./src/main/java/net/bananashelp20/forgermod/registryInterpreter/regFileSources/itemUpgradeList.txt");
    public static File recipeFile = new File("./src/main/java/net/bananashelp20/forgermod/registryInterpreter/regFileSources/recipes.willi");
    public static File toolTierFile = new File("./src/main/java/net/bananashelp20/forgermod/registryInterpreter/regFileSources/toolTiers.willi");
    public static File modItemsFile = new File("./src/main/java/net/bananashelp20/forgermod/registryInterpreter/testRegistries/ModItems.java");
    public static File modBlockFile = new File("./src/main/java/net/bananashelp20/forgermod/registryInterpreter/testRegistries/ModBlocks.java");
    public static File modToolTiersFile = new File("./src/main/java/net/bananashelp20/forgermod/registryInterpreter/testRegistries/ModToolTiers.java");
    public static File modBlockLootTableProviderFile = new File("./src/main/java/net/bananashelp20/forgermod/registryInterpreter/testRegistries/ModBlockLootTableProvider.java");
    public static File modBlockStateProviderFile = new File("./src/main/java/net/bananashelp20/forgermod/registryInterpreter/testRegistries/ModBlockStateProvider.java");
    public static File modBlockTagProviderFile = new File("./src/main/java/net/bananashelp20/forgermod/registryInterpreter/testRegistries/ModBlockTagProvider.java");
    public static File modItemTagProviderFile = new File("./src/main/java/net/bananashelp20/forgermod/registryInterpreter/testRegistries/ModItemTagProvider.java");
    public static File modCreativeModeTabsFile = new File("./src/main/java/net/bananashelp20/forgermod/registryInterpreter/testRegistries/ModCreativeModeTabs.java");
    public static File modItemModelProviderFile = new File("./src/main/java/net/bananashelp20/forgermod/registryInterpreter/testRegistries/ModItemModelProvider.java");
    public static File modRecipeProviderFile = new File("./src/main/java/net/bananashelp20/forgermod/registryInterpreter/testRegistries/ModRecipeProvider.java");
    public static File modTabRegistry = new File("./src/main/java/net/bananashelp20/forgermod/registryInterpreter/testRegistries/RegistryClass.java");

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    static String unchangedModBlockFileContent = getContentFromFile(modBlockFile);
    static String unchangedModRegistryContent = getContentFromFile(modTabRegistry);
    static String unchangedModItemTagProviderContent = getContentFromFile(modItemTagProviderFile);
    static String unchangedModToolTiersFile = getContentFromFile(modToolTiersFile);
    static String unchangedModBlockStateProviderFile = getContentFromFile(modBlockStateProviderFile);
    static String unchangedModBlockLootTableProviderFile = getContentFromFile(modBlockLootTableProviderFile);
    static String unchangedModBlockTagProviderFile = getContentFromFile(modBlockTagProviderFile);
    static String unchangedModItemModelProviderFile = getContentFromFile(modItemModelProviderFile);
    static String unchangedModRecipeProviderFile = getContentFromFile(modRecipeProviderFile);
    static String unchangedModItemsFileContent = getContentFromFile(modItemsFile);
    static String unchangedModCreativeModeTabsFileContent = getContentFromFile(modCreativeModeTabsFile);

    public static ArrayList<InterpretedBlock> getAllBlocks() {
        ArrayList<InterpretedBlock> interpretedBlocks = new ArrayList<>();
        ArrayList<String> blockText = getContentFromFileAsList(blockFile, "#");
        ArrayList<ArrayList<String>> blockStringObjects = new ArrayList<>();
        String dropOtherItem;
        boolean dropOther;
        int indexExpander;
        InterpretedBlock blockToAdd;
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
        ArrayList<String> itemText = getContentFromFileAsList(itemFile, "#");
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


    public static ArrayList<InterpretedCreativeTab> getAllCreativeTabs() {
        ArrayList<InterpretedCreativeTab> interpretedToolTiers = new ArrayList<>();
        ArrayList<String> toolTierText = getContentFromFileAsList(creativeTabFile, "#");
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
        ArrayList<String> toolTierText = getContentFromFileAsList(toolTierFile, "#");
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
        ArrayList<String> recipeText = getContentFromFileAsList(recipeFile, "#");
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
                inputItems.addAll(getContentInBrackets(i, 5, recipeStringObjects));
                recipeToAdd = new InterpretedBlastingOrSmeltingRecipe(true, recipeStringObjects.get(i).get(1), inputItems, recipeStringObjects.get(i).get(2), i, recipeStringObjects.get(i).get(3), recipeStringObjects.get(i).get(4));
            } else if (recipeStringObjects.get(i).contains("{shapeless")) {
                inputItems.addAll(getContentInBrackets(i, 5, recipeStringObjects));
                recipeToAdd = new InterpretedShapelessRecipe(inputItems, recipeStringObjects.get(i).get(1), recipeStringObjects.get(i).get(2), recipeStringObjects.get(i).get(3), Integer.parseInt(recipeStringObjects.get(i).get(4)), i);
            } else if (recipeStringObjects.get(i).contains("{shaped")) {
                pattern[0] = recipeStringObjects.get(i).get(2);
                pattern[1] = recipeStringObjects.get(i).get(3);
                pattern[2] = recipeStringObjects.get(i).get(4);
                int j = 0;
                for (j = 5; recipeStringObjects.get(i).get(j).contains("->"); j++) {
                    temp = recipeStringObjects.get(i).get(j).split("->");
                    patternMeaning.put(temp[0].charAt(0), temp[1].trim());
                }
                recipeToAdd = new InterpretedShapedRecipe(recipeStringObjects.get(i).get(1), pattern, patternMeaning,recipeStringObjects.get(i).get(j), recipeStringObjects.get(i).get(++j), i);
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

    public static void rewriteAllAfterError(boolean allowed) {
        try {
            if (!allowed) return; //only for safety reasons
            FileWriter[] writers = new FileWriter[11];
            (writers[0] = new FileWriter(modBlockFile)).write(unchangedModBlockFileContent);
            (writers[1] = new FileWriter(modTabRegistry)).write(unchangedModRegistryContent);
            (writers[2] = new FileWriter(modItemTagProviderFile)).write(unchangedModItemTagProviderContent);
            (writers[3] = new FileWriter(modToolTiersFile)).write(unchangedModToolTiersFile);
            (writers[4] = new FileWriter(modBlockStateProviderFile)).write(unchangedModBlockStateProviderFile);
            (writers[5] = new FileWriter(modBlockLootTableProviderFile)).write(unchangedModBlockLootTableProviderFile);
            (writers[6] = new FileWriter(modBlockTagProviderFile)).write(unchangedModBlockTagProviderFile);
            (writers[7] = new FileWriter(modItemModelProviderFile)).write(unchangedModItemModelProviderFile);
            (writers[8] = new FileWriter(modRecipeProviderFile)).write(unchangedModRecipeProviderFile);
            (writers[9] = new FileWriter(modItemsFile)).write(unchangedModItemsFileContent);
            (writers[10] = new FileWriter(modCreativeModeTabsFile)).write(unchangedModCreativeModeTabsFileContent);
            for (int i = 0; i < writers.length; i++) {
                writers[i].close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

    public static ArrayList<String> getContentFromFileAsList(File file, String comment) {
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

        clearContentFromUnneccesary(fileContent, comment);
        return fileContent;
    }


    public static ArrayList<String> getContentFromFileAsListNonFormated(File file) {
        Scanner reader;
        ArrayList<String> fileContent = new ArrayList<>();
        try {
            reader = new Scanner(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        while (reader.hasNextLine()) {
            fileContent.add(reader.nextLine());
        }

        return fileContent;
    }

    public static void clearContentFromUnneccesary(ArrayList<String> content, String comment) {
        for (int i = 0; i < content.size(); i++) {
            content.set(i, content.get(i).trim());
            if (!comment.isEmpty()) {
                if (content.get(i).contains(comment)) {
                    content.set(i, getPartWithoutComment(content.get(i)));
                }
            }
            if (content.get(i).trim().equals("")) {
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

    public static void writeItemModels() {
        String prevContent = getWholeFileContentTillGenerate(modItemModelProviderFile, "//!GENERATE MODELS");
        String newStuff = "";

        for (int i = 0; i < items.size(); i++) {
            newStuff += items.get(i).getItemModel() + "\n";
        }

        write(prevContent + "\n" + newStuff + "    }\n}", modItemModelProviderFile);
    }

    public static void writeItemTags() {
        String prevContent = getWholeFileContentTillGenerate(modItemTagProviderFile, "//!GENERATE ITEM_TAGS");
        String newStuff = "";
        ArrayList<ArrayList<String>> differentTags = getDifferentItemTags();
        ArrayList<String> tagContent;

        for (int i = 0; i < differentTags.size(); i++) {
            newStuff += "        tag(" + differentTags.get(i).getFirst() + differentTags.get(i).get(1) + ")\n        ;\n";
        }

        write(prevContent + "\n" + newStuff + "    }\n}", modItemTagProviderFile);
        tagContent = getContentFromFileAsListNonFormated(modItemTagProviderFile);
        for (int i = 0; i < items.size(); i++) {
            for (int k = 0; k < items.get(i).getTagsOfItem().size(); k++) {
                for (int j = 0; j < tagContent.size(); j++) {
                    if (tagContent.get(j).toUpperCase().contains(items.get(i).getTagsOfItem().get(k).toUpperCase())) {
                        tagContent.add(j+1, items.get(i).getItemTagCode());
                    }
                }
            }
        }
        write(getListAsString(tagContent), modItemTagProviderFile);
    }

    public static void writeItemsToTabRegistry() {
        ArrayList<String> prevContent = getContentFromFileAsList(modRegistry, "");

        for (int j = 0; j < items.size(); j++) {
            for (int k = 0; k < prevContent.size(); k++) {
                if (prevContent.get(k).toUpperCase().contains(items.get(j).getCreativeTab().toUpperCase()) && !prevContent.get(k).toUpperCase().contains("DisplayItem".toUpperCase()) && prevContent.get(k).toUpperCase().contains("ItemLike[]".toUpperCase())) {
                    prevContent.add(k+2, items.get(j).getCreativeTabCode());
                }
            }
        }
        write(getListAsString(prevContent), modRegistry);
    }

    public static void writeModTags() {
        String prevContent = getWholeFileContentTillGenerate(modTagsFile, "//!GENERATE MOD_TAGS");
        String newStuff = "";

        for (int i = 0; i < toolTiers.size(); i++) {
            newStuff += toolTiers.get(i).getTags() + "\n";
        }

        prevContent += "\n" + newStuff + "    public static class Items {\n" +
                "        private static TagKey<Item> createTag(String name) {\n" +
                "            return ItemTags.create(ResourceLocation.fromNamespaceAndPath(ForgerMod.MOD_ID, name));\n" +
                "        }\n" +
                "        //!GENERATE MOD_ITEM_TAGS\n" +
                "    }";
        write(prevContent + "    }\n}", modTagsFile);
        //writeModTagsForItems(); //moch i erst wenns soweit is
    }

    public static void writeRegistryMethods() {
        String prevContent = getWholeFileContentTillGenerate(modTabRegistry, "//!GENERATE METHODS");
        String newStuff = "";

        for (int i = 0; i < creativeTabs.size(); i++) {
            newStuff += creativeTabs.get(i).getRegistryMethods() + "\n";
        }

        write(prevContent + "\n" + newStuff + "}", modTabRegistry);
    }

    public static void writeBlockLoottables() {
        String prevContent = getWholeFileContentTillGenerate(modBlockLootTableProviderFile, "//!GENERATE DROPS");
        String newStuff = "";

        for (int i = 0; i < blocks.size(); i++) {
            newStuff += blocks.get(i).getLoottable() + "\n";
        }

        write(prevContent + "\n" + newStuff + "    }\n}", modBlockLootTableProviderFile);
    }

    public static void writeBlockTags() {
        String prevContent = getWholeFileContentTillGenerate(modBlockTagProviderFile, "//!GENERATE BLOCK_TAGS");
        String newStuff = "";
        ArrayList<ArrayList<String>> differentTags = getDifferentBlockTags();
        ArrayList<String> tagContent;

        for (int i = 0; i < differentTags.size(); i++) {
            newStuff += "        tag(" + differentTags.get(i).getFirst() + (differentTags.get(i).get(2).equals("type") ? "NEEDS_" + differentTags.get(i).get(1).toUpperCase() + "_TOOL" : "MINEABLE_WITH_" + differentTags.get(i).get(1).toUpperCase()) + ")\n        ;\n";
        }

        write(prevContent + "\n" + newStuff + "    }\n}", modBlockTagProviderFile);
        tagContent = getContentFromFileAsListNonFormated(modBlockTagProviderFile);
        for (int i = 0, j = 0, k = 0; i < tagContent.size() && j < blocks.size() && k < blocks.size(); i++) {
            if (tagContent.get(i).contains(" tag(") && tagContent.get(i).toUpperCase().contains(blocks.get(j).getTagTool().toUpperCase()) && !blocks.get(j).getTagTool().isEmpty()) {
                tagContent.add(i+1, blocks.get(j).getTag());
                j++;
            }
            if (tagContent.get(i).contains(" tag(") && tagContent.get(i).toUpperCase().contains(blocks.get(k).getTagType().toUpperCase()) && !tagContent.get(i).toUpperCase().contains("INCORRECT") && !blocks.get(k).getTagTool().isEmpty()) {
                tagContent.add(i+1, blocks.get(k).getTag());
                k++;
            }
        }
        write(getListAsString(tagContent), modBlockTagProviderFile);
    }

    public static void writeBlockStates() {
        String prevContent = getWholeFileContentTillGenerate(modBlockStateProviderFile, "//!GENERATE MODELS");
        String newStuff = "";

        for (int i = 0; i < blocks.size(); i++) {
            newStuff += blocks.get(i).getBlockState() + (blocks.get(i).getBlockState().isEmpty() ? "" : "\n");
        }

        write(prevContent + "\n" + newStuff + "    }\n}", modBlockStateProviderFile);
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

    public static ArrayList<ArrayList<String>> getDifferentBlockTags() {
        ArrayList<ArrayList<String>> differentTags = new ArrayList<>();
        ArrayList<String> tag;

        for (int i = 0; i < blocks.size(); i++) {
            tag = new ArrayList<>();
            if (blocks.get(i).getTagTool().toUpperCase().contains("shovel".toUpperCase())
                    || blocks.get(i).getTagTool().toUpperCase().contains("axe".toUpperCase())
                    || blocks.get(i).getTagTool().toUpperCase().contains("pickaxe".toUpperCase())
                    || blocks.get(i).getTagTool().toUpperCase().contains("hoe".toUpperCase())) {
                tag.add("BlockTags.");
            } else if (!blocks.get(i).getTagTool().isEmpty()) {
                tag.add("ModTags.Blocks.");
            }
            if (!blocks.get(i).getTagTool().isEmpty()) {
                tag.add(blocks.get(i).getTagTool());
                tag.add("tool");
                differentTags.add(tag);
            }
        }

        for (int i = 0; i < blocks.size(); i++) {
            tag = new ArrayList<>();
            if (blocks.get(i).getTagType().toUpperCase().contains("stone".toUpperCase())
                    || blocks.get(i).getTagType().toUpperCase().contains("diamond".toUpperCase())
                    || blocks.get(i).getTagType().toUpperCase().contains("iron".toUpperCase())) {
                tag.add("BlockTags.");
            } else if (!blocks.get(i).getTagType().isEmpty()) {
                tag.add("ModTags.Blocks.");
            }
            if (!blocks.get(i).getTagType().isEmpty()) {
                tag.add(blocks.get(i).getTagType());
                tag.add("type");
                differentTags.add(tag);
            }
        }
        removeDuplicatesFromTagList(differentTags);
        return differentTags;
    }

    public static ArrayList<ArrayList<String>> getDifferentItemTags() {
        ArrayList<ArrayList<String>> differentTags = new ArrayList<>(); //alles tags
        ArrayList<String> tag; //ein tag objekt (tag prefix, und tag selbst)

        for (int i = 0; i < items.size(); i++) {
            for (int j = 1; j < items.get(i).getTagsOfItem().size(); j++) {
                tag = new ArrayList<>();
                if (isPartOfItemTags(items.get(i).getTagsOfItem().get(j).toUpperCase(), true)) {
                    tag.add("ItemTags.");
                } else {
                    tag.add("ModTags.Items.");
                }
                tag.add(items.get(i).getTagsOfItem().get(j));
                differentTags.add(tag);
            }
        }
        removeDuplicatesFromTagList(differentTags);
        return differentTags;
    }

    public static void removeDuplicatesFromTagList(ArrayList<ArrayList<String>> tags) {
        for (int i = 0; i < tags.size(); i++) {
            removeDuplicatesOf(tags.get(i).get(1), tags, i);
        }
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

    public static void write(String content, File writeTo) {
        try {
            FileWriter modFileWriter = new FileWriter(writeTo);
            modFileWriter.write(content);
            modFileWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}