package net.bananashelp20.forgermod.registries;

import org.checkerframework.checker.units.qual.A;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class RegistryInterpreter {
    static File blockFile = new File("./src/main/java/net/bananashelp20/forgermod/registries/regFiles/blocks.txt");
    static File modBlocksFile = new File("./src/main/java/net/bananashelp20/forgermod/block/ModBlocks.java");
    static File itemFile = new File("./src/main/java/net/bananashelp20/forgermod/registries/regFiles/items.txt");
    static File creativeTabsFile = new File("./src/main/java/net/bananashelp20/forgermod/registries/regFiles/creativeTabs.txt");
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

    public static void main(String[] args) throws Exception {
        if (!generateCode()) {
            throw new FileNotFoundException(ANSI_RED + "Code could not be generated, an Error occurred" + ANSI_RESET);
        }
    }

    public static boolean generateCode() {
        if (!(modBlocksFile.exists() && modBlocksFile.canWrite() && modBlocksFile.canRead()
                && modItemsFile.exists() && modItemsFile.canWrite() && modItemsFile.canRead()
                && modCreativeModeTabsFile.exists() && modCreativeModeTabsFile.canWrite() && modCreativeModeTabsFile.canRead()
                && modRegistry.exists() && modRegistry.canWrite() && modRegistry.canRead()
                && blockFile.exists() && blockFile.canRead()
                && itemFile.exists() && itemFile.canRead()
                && creativeTabsFile.exists() && creativeTabsFile.canRead()
        )) {
            return false;
        }
        RegistryInterpreterHelper.generateAndWriteBlocks(modBlockFile, blockFile);
        RegistryInterpreterHelper.generateAndWriteBlockDatagen();
        generateAndWriteBlocksToCorrectCreativeTab();
        return true;
    }

    private static void generateAndWriteBlocksToCorrectCreativeTab() {
        ArrayList<String> registryFileContentList = getContentFromFileAsList(modRegistry);
        Scanner reader;
        ArrayList<ArrayList<String>> nameAndCreativeTab = new ArrayList<>();
        String line;
        registryFileContentList = clearFromOldEntriesOfType("ModBlocks.", registryFileContentList);
        try {
            reader = new Scanner(blockFile);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        int ctrForTabs = 0;
        while (reader.hasNextLine()) {
            line = reader.nextLine().trim();
            while (reader.hasNextLine() && !(line.equalsIgnoreCase("simple {") || line.equalsIgnoreCase("complex {") || line.equalsIgnoreCase("special {"))) {
                line = reader.nextLine().trim();
            }
            ctrForTabs = getContentForCreativeTabs(line.equalsIgnoreCase("special {"), nameAndCreativeTab, reader, ctrForTabs);
        }

        for (int i = 0; i < registryFileContentList.size(); i++) {
            if (registryFileContentList.get(i).contains("public static ItemLike[]")) {
                i++;
                if (registryFileContentList.get(i-1).contains("Ingredient")) i = checkAndCycleTabs(registryFileContentList, nameAndCreativeTab, i, "Ingredient", "ModBlocks.");
                if (registryFileContentList.get(i-1).contains("Weapon")) i = checkAndCycleTabs(registryFileContentList, nameAndCreativeTab, i, "Weapon", "ModBlocks.");
                if (registryFileContentList.get(i-1).contains("Item")) i = checkAndCycleTabs(registryFileContentList, nameAndCreativeTab, i, "Item", "ModBlocks.");
                if (registryFileContentList.get(i-1).contains("Miscellaneous")) i = checkAndCycleTabs(registryFileContentList, nameAndCreativeTab, i, "Miscellaneous", "ModBlocks.");
                if (registryFileContentList.get(i-1).contains("Block")) i = checkAndCycleTabs(registryFileContentList, nameAndCreativeTab, i, "Block", "ModBlocks.");
            }
        }

        try {
            FileWriter writer = new FileWriter(modRegistry);
            writer.write(listToString(registryFileContentList));
            writer.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        System.out.println(ANSI_GREEN + "Successfully wrote Blocks into Creative Mode Tabs" + ANSI_RESET);
    }

    public static String listToString(ArrayList<String> list) {
        String s = "";
        for (int i = 0; i < list.size(); i++) {
            s += list.get(i);
        }
        return s;
    }

    public static int checkAndCycleTabs(ArrayList<String> registry, ArrayList<ArrayList<String>> nameAndTab, int i, String tab, String type) {
        for (int j = 0; j < nameAndTab.size(); j++) {
            if (nameAndTab.get(j).get(1).contains(tab.toLowerCase())) {
                registry.add(i+1, "                " + type + nameAndTab.get(j).getFirst().toUpperCase() + ".get()," + "\n");
            }
        }
        return i;
    }

    public static int getContentForCreativeTabs(boolean special, ArrayList<ArrayList<String>> nameAndCreativeTab, Scanner reader, int ctr) {
        String name;
        String drops;
        name = reader.nextLine().trim();
        for (ctr += 0; reader.hasNextLine() && !name.equalsIgnoreCase("}"); ctr++) {
            reader.nextLine();
            if (special) reader.nextLine();
            drops = reader.nextLine().trim();
            reader.nextLine();
            reader.nextLine();
            if (drops.equalsIgnoreCase("dropOther")) reader.nextLine();
            reader.nextLine();
            nameAndCreativeTab.add(new ArrayList<>());
            nameAndCreativeTab.get(ctr).add(0, name);
            nameAndCreativeTab.get(ctr).add(1, reader.nextLine().trim());
            name = reader.nextLine().trim();
        }
        return ctr;
    }

    public static ArrayList<String> clearFromOldEntriesOfType(String type, ArrayList<String> file) {
        boolean foundGenerateCommand = false;
        boolean currentlyInRegister = false;
        for (int j = 0; j < 2; j++) {
            for (int i = 0; i < file.size(); i++) {
                if (file.get(i).contains("//STARTGENERATING!")) {
                    foundGenerateCommand = true;
                } else if (file.get(i).contains("return new ItemLike[] {") && foundGenerateCommand) {
                    currentlyInRegister = true;
                } else if (file.get(i).contains(type) && file.get(i).contains(".get()") && foundGenerateCommand && !file.get(i).contains("//PRESERVE")) {
                    file.remove(i);
                    i--;
                } else if (file.get(i).contains("};") && foundGenerateCommand) {
                    currentlyInRegister = false;
                }

                if (currentlyInRegister && file.get(i).trim().equals("")) {
                    file.remove(i);
                    i--;
                }
            }
        }
        return file;
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

    public static String generateCreativeModeTabs(File fileToWrite, File registryFile) {
        return "";
    }

    public static String generateSimpleItems(File fileToWrite, File registryFile) {
        return "";
    }

    public static String generateComplexItems(File fileToWrite, File registryFile) {
        return "";
    }

    public static String generateGemstoneItems() {
        return "";
    }

//    public static void reWriteAllAfterError() {
//        FileWriter writer = new FileWriter()
//    }

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
}
