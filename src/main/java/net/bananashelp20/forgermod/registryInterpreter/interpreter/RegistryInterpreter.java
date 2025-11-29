package net.bananashelp20.forgermod.registryInterpreter.interpreter;

import net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.blocks.InterpretedBlock;
import net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.creativeTabs.InterpretedCreativeTab;
import net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.items.InterpretedItem;
import net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.items.special.*;
import net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.recipes.InterpretedRecipe;
import net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.recipes.special.InterpretedCustomRecipe;
import net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.toolTiers.InterpretedToolTier;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import static net.bananashelp20.forgermod.registryInterpreter.interpreter.RegistryInterpreterHelperMethods.*;

public class RegistryInterpreter {
    public static void main(String[] args) throws FileNotFoundException {
        try {
            if (!generateCode()) {
                System.out.println(ANSI_RED + "\n#SYSTEM@INFO> CRITICAL: Interpreter was interrupted during " + (stillGenerating ? "generating" : "writing") + " phase, because an " + ANSI_RESET + ANSI_BLACK + " ERROR " + ANSI_RESET + ANSI_RED + " occured" + ANSI_RESET);
                System.out.println(ANSI_RED + "#SYSTEM@INFO> Trying to restore all overridden code!" + ANSI_RESET);
                rewriteAllAfterError(false);
                System.out.println(ANSI_RED + "#SYSTEM@INFO> Restored all overriden code!" + ANSI_RESET);
                throw new FileNotFoundException(ANSI_RED + "Code could not be generated, an Error occurred" + ANSI_RESET);
            }
            System.out.println(ANSI_RED + "#SYSTEM@INFO> successfully finished program without any problems!" + ANSI_RESET);
        } catch (Exception e) {
            System.out.print(ANSI_RED + "#SYSTEM@INFO> CRITICAL: Interpreter was interrupted during " + (stillGenerating ? "generating" : "writing") + " phase!" + ANSI_RESET);
            System.out.print(ANSI_RED + "#SYSTEM@INFO> Trying to restore all overridden code!" + ANSI_RESET);
            rewriteAllAfterError(false);
            System.out.print(ANSI_RED + "#SYSTEM@INFO> Restored all overriden code" + ANSI_RESET);
            throw e;
        }
    }

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
    public static File modRegistry = new File("./src/main/java/net/bananashelp20/forgermod/registryInterpreter/testRegistries/RegistryClass.java");

    private static boolean stillGenerating = true;
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
    static String unchangedModRegistryContent = getContentFromFile(modRegistry);
    static String unchangedModItemTagProviderContent = getContentFromFile(modItemTagProviderFile);
    static String unchangedModToolTiersFile = getContentFromFile(modToolTiersFile);
    static String unchangedModBlockStateProviderFile = getContentFromFile(modBlockStateProviderFile);
    static String unchangedModBlockLootTableProviderFile = getContentFromFile(modBlockLootTableProviderFile);
    static String unchangedModBlockTagProviderFile = getContentFromFile(modBlockTagProviderFile);
    static String unchangedModItemModelProviderFile = getContentFromFile(modItemModelProviderFile);
    static String unchangedModRecipeProviderFile = getContentFromFile(modRecipeProviderFile);
    static String unchangedModItemsFileContent = getContentFromFile(modItemsFile);
    static String unchangedModCreativeModeTabsFileContent = getContentFromFile(modCreativeModeTabsFile);

    public static boolean generateCode() {
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
        Scanner userHelper = new Scanner(System.in);
        String input;
        warning("****************************************************************************************************************************************\n" +
                "* Generating the code means OVERRIDING ALL CURRENT CODE that's been written to: all datagen files, ModItems, ModBlocks, RegistryClass, *\n* ModToolTiers and ModCreativeModeTabs." +
                "Other Files might also be affected, and there is no guarantee the code works as it should.      *\n* Please make sure to " + ANSI_RESET + ANSI_PURPLE + "//!PRESERVE " + ANSI_RESET +
                ANSI_YELLOW + "every important code line that shall not be overridden                                               *\n" +
                "* If you wish to continue anyways, type in " + ANSI_RESET + ANSI_GREEN + "\"!START\"" + ANSI_RESET + ANSI_YELLOW + ".                                            " +
                "                                       *\n" +
                "* If you want to stop without any code being generated, type in the command "+ ANSI_RESET + ANSI_RED + "\"!STOP\"." + ANSI_RESET + ANSI_YELLOW +
                "                                                   *\n" +
                "****************************************************************************************************************************************");
        while (!(input = userInputWithoutLineBreak(userHelper)).contains("!START")) {
            if (input.contains("!STOP")) {
                return true;
            }
        }
        System.out.println(ANSI_RED + "#SYSTEM@INFO> starting with generating phase\n" + ANSI_RESET);


        printRegistryFromList(toolTiers);
        System.out.print(ANSI_RED + "#SYSTEM@INFO[GEN_PHASE]> " + ANSI_RESET);
        success("Successfully generated tool tier objects\n");
        printRegistryFromList(items);
        System.out.print(ANSI_RED + "#SYSTEM@INFO[GEN_PHASE]> " + ANSI_RESET);
        success("Successfully generated item objects\n");
        printRegistryFromList(blocks);
        System.out.print(ANSI_RED + "#SYSTEM@INFO[GEN_PHASE]> " + ANSI_RESET);
        success("Successfully generated block objects\n");
        printRegistryFromList(creativeTabs);
        System.out.print(ANSI_RED + "#SYSTEM@INFO[GEN_PHASE]> " + ANSI_RESET);
        success("Successfully generated creative tab objects\n");
        printRegistryFromList(recipes);
        System.out.print(ANSI_RED + "#SYSTEM@INFO[GEN_PHASE]> " + ANSI_RESET);
        success("Successfully generated recipe objects");
        System.out.println(ANSI_RED + "#SYSTEM@INFO> Successfully completed generating phase" + ANSI_RESET);
        warning("****************************************************************************************************************************************\n" +
                "* Do really want to proceed with writing the code to the files? Be aware that bugs are still very possible, and code is never perfect  *\n" +
                "* If you wish to continue, type in " + ANSI_RESET + ANSI_GREEN + "\"!RESUME\"" + ANSI_RESET + ANSI_YELLOW + ".                                            " +
                "                                              *\n" +
                "* If you want to stop without any code being written, type in the command "+ ANSI_RESET + ANSI_RED + "\"!STOP\"." + ANSI_RESET + ANSI_YELLOW +
                "                                                     *\n" +
                "****************************************************************************************************************************************");
        while (!(input = userInputWithoutLineBreak(userHelper)).contains("!RESUME")) {
            if (input.contains("!STOP")) {
                System.out.println(ANSI_RED + "#SYSTEM@INFO> stopping program..." + ANSI_RESET);
                return true;
            }
        }
        System.out.println(ANSI_RED + "#SYSTEM@INFO> resuming program..." + ANSI_RESET);
        System.out.println(ANSI_RED + "#SYSTEM@INFO> starting with writing phase" + ANSI_RESET);

        //TODO !PRESERVE geht nu ned, ds musst nu mochn
        writeToolTierCode(false); //WORKS! (jo vatrau ma des geht wirkli, wenns nd geht host wos augstöt)
        success("Successfully wrote tool tier objects to files");
        writeItemCode(false);
        success("Successfully wrote item objects to files");
//        writeBlockCode();
//        success("Successfully wrote block tab objects to files");
//        writeCreativeTabCode();
//        success("Successfully wrote creative tab objects to files");
        writeRecipeCode(false); //WORKS! (jo vatrau ma des geht wirkli, wenns nd geht host wos augstöt)
        success("Successfully wrote recipe objects to files");
        return true;
    }

    private static void writeItemCode(boolean allowed) {
        String prevContent = getWholeFileContentTillGenerate(modItemsFile, "//!GENERATE");
        String newStuff = "";

        for (int i = 0; i < items.size(); i++) {
            newStuff += items.get(i).toString() + "\n";
        }

        prevContent += "\n" + newStuff + "    }\n}";
        System.out.println(prevContent);
        if (!allowed) return;
        try {
            FileWriter modItemWriter = new FileWriter(modItemsFile);
            modItemWriter.write(prevContent);
            modItemWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void writeRecipeCode(boolean allowed) {
        String prevContent = getWholeFileContentTillGenerate(modRecipeProviderFile, "//!GENERATE");
        String newStuff = "";
        ArrayList<Integer> relevantObjects = new ArrayList<>();
        ArrayList<File> relevantClasses = new ArrayList<>();
        for (int i = 0; i < recipes.size(); i++) {
            if (!(recipes.get(i) instanceof InterpretedCustomRecipe)) {
                newStuff += recipes.get(i).toString() + "\n";
            } else {
                relevantObjects.add(i);
                relevantClasses.add(((InterpretedCustomRecipe) recipes.get(i)).getRecipeClass());
            }
        }

        prevContent += "\n" + newStuff + "    }\n}";
        System.out.println(prevContent);
        if (!allowed) return;
        try {
            FileWriter recipeWriter = new FileWriter(modRecipeProviderFile);
            recipeWriter.write(prevContent);
            recipeWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void writeToolTierCode(boolean allowed) {
        String prevContent = getWholeFileContentTillGenerate(modToolTiersFile, "//!GENERATE");
        String newStuff = "";
        for (int i = 0; i < toolTiers.size(); i++) {
            newStuff += toolTiers.get(i).getTierCode();
        }

        prevContent += "\n" + newStuff + "}";

        if (!allowed) return;
        try {
            FileWriter toolTierWriter = new FileWriter(modToolTiersFile);
            toolTierWriter.write(prevContent);
            toolTierWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
            (writers[1] = new FileWriter(modRegistry)).write(unchangedModRegistryContent);
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