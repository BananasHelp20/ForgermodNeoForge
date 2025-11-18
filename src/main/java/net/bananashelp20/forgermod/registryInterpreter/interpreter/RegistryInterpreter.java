package net.bananashelp20.forgermod.registryInterpreter.interpreter;

import net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.blocks.InterpretedBlock;
import net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.creativeTabs.InterpretedCreativeTab;
import net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.items.InterpretedItem;
import net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.items.special.InterpretedSimpleItem;
import net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.recipes.InterpretedRecipe;
import net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.toolTiers.InterpretedToolTier;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class RegistryInterpreter {
    public static void main(String[] args) throws FileNotFoundException {
        try {
            if (!generateCode()) {
                rewriteAllAfterError();
                throw new FileNotFoundException(ANSI_RED + "Code could not be generated, an Error occurred" + ANSI_RESET);
            }
        } catch (Exception e) {
            rewriteAllAfterError();
            throw e;
        }
    }

    static File blockFile = new File("./src/main/java/net/bananashelp20/forgermod/registries/regFiles/blocks.txt");
    public static File itemFile = new File("./src/main/java/net/bananashelp20/forgermod/registries/regFiles/items.txt");
    static File creativeTabFile = new File("./src/main/java/net/bananashelp20/forgermod/registries/regFiles/creativeTabs.txt");
    static File modItemsFile = new File("./src/main/java/net/bananashelp20/forgermod/item/ModItems.java");
    static File modCreativeModeTabsFile = new File("./src/main/java/net/bananashelp20/forgermod/CreativeModeTabs/ModCreativeModeTabs.java");
    static File modRegistry = new File("./src/main/java/net/bananashelp20/forgermod/registries/TestRegistryClass.java");
    static File modBlockFile = new File("./src/main/java/net/bananashelp20/forgermod/registries/test/ModBlocks.java");

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    ArrayList<InterpretedItem> items = getAllItems();
//    ArrayList<InterpretedBlock> blocks = getAllBlocks();
//    ArrayList<InterpretedRecipe> recipes = getAllRecipes();
//    ArrayList<InterpretedCreativeTab> tabs = getAllCreativeTabs();
//    ArrayList<InterpretedToolTier> toolTiers = getAllToolTiers();

    static String unchangedModBlockFileContent = getContentFromFile(modBlockFile);
    static String unchangedModRegistryContent = getContentFromFile(modRegistry);
    static String unchangedModItemsFileContent = getContentFromFile(modItemsFile);
    static String unchangedModCreativeModeTabsFileContent = getContentFromFile(modCreativeModeTabsFile);

    public static  boolean generateCode() {
        if (!(modBlockFile.exists() && modBlockFile.canWrite() && modBlockFile.canRead()
                && modItemsFile.exists() && modItemsFile.canWrite() && modItemsFile.canRead()
                && modCreativeModeTabsFile.exists() && modCreativeModeTabsFile.canWrite() && modCreativeModeTabsFile.canRead()
                && modRegistry.exists() && modRegistry.canWrite() && modRegistry.canRead()
                && blockFile.exists() && blockFile.canRead()
                && itemFile.exists() && itemFile.canRead()
                && creativeTabFile.exists() && creativeTabFile.canRead()
        )) {
            return false;
        }

//        generateToolTiers();
//        generateAndWriteItemCode();
//        generateAndWriteBlockCode();
//        generateAndWriteCreativeTabs();
//        generateAndWriteRecipes();

        return true;
    }

    private ArrayList<InterpretedItem> getAllItems() {
        ArrayList<InterpretedItem> items = new ArrayList<>();
        ArrayList<String> itemText = getContentFromFileAsList(itemFile);
        Scanner reader;
        try {
            reader = new Scanner(itemFile);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        for (int i = 0; i < itemText.size(); i++) {
            if (itemText.get(i).contains("Simple {")) {
                for (int j = i+1; j < itemText.size() && !itemText.get(j).contains("}"); j+=2) {
                    items.add(new InterpretedSimpleItem(getPartWithoutComment(itemText.get(i)), getPartWithoutComment(itemText.get(i+1))));
                }
            } else if (itemText.get(i).contains("Special {")) {
                int j = i+1;
                while (j < itemText.size() && !itemText.get(j).contains("}")) {
                    items.add(new InterpretedSimpleItem(getPartWithoutComment(itemText.get(i)), getPartWithoutComment(itemText.get(i+1))));
                    if (getPartWithoutComment(itemText.get(i)).contains("?")) {
                        i+=3;
                    } else {
                        i+=2;
                    }
                }
            } else if (itemText.get(i).contains("Simple Sword {")) {

            } else if (itemText.get(i).contains("Special Sword {")) {

            } else if (itemText.get(i).contains("Upgradeable Sword {")) {

            }
        }
        
        return items;
    }

    private String getPartWithoutComment(String s) {
        String[] splitText = s.split("#");
        for (int i = 0; i < splitText.length; i++) {
            if (!splitText[i].contains("//")) {
                return splitText[i];
            }
        }
        return "NULL";
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
        return fileContent;
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
            FileWriter writer1 = new FileWriter(modRegistry);
            FileWriter writer2 = new FileWriter(modItemsFile);
            FileWriter writer3 = new FileWriter(modCreativeModeTabsFile);
            writer.write(unchangedModBlockFileContent);
            writer1.write(unchangedModRegistryContent);
            writer2.write(unchangedModItemsFileContent);
            writer3.write(unchangedModCreativeModeTabsFileContent);
            writer.close();
            writer1.close();
            writer2.close();
            writer3.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static ArrayList<ArrayList<String>> getEnchantmentablesFromOptionalParameter(ArrayList<String> filecontent, String name) {//specifisch für name ausgeben
        ArrayList<ArrayList<String>> enchantingTagsForEachItem = new ArrayList<>();
        ArrayList<String> currItem;
        for (int i = 0; i < filecontent.size(); i++) {
            if (filecontent.get(i).contains("?[E")) {
                i++;
                currItem = new ArrayList<>();
                while (i < filecontent.size() && !filecontent.get(i).contains("?]")) {
                    currItem.add(filecontent.get(i).trim().split("Enchantable:")[0].toUpperCase());
                    i++;
                }
                enchantingTagsForEachItem.add(currItem);
            }
        }

        return enchantingTagsForEachItem;
    }
}
