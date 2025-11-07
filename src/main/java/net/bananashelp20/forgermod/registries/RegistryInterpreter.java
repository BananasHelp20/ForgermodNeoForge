package net.bananashelp20.forgermod.registries;

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
    static File modRegistry = new File("./src/main/java/net/bananashelp20/forgermod/registries/RegistryClass.java");
    static File testFile = new File("./src/main/java/net/bananashelp20/forgermod/registries/test/ModBlocks.java");
    static File modBlockLootTableProvider = new File("./src/main/java/net/bananashelp20/forgermod/registries/test/ModBlockLootTableProvider.java");
    static File modBlockStateProvider = new File("./src/main/java/net/bananashelp20/forgermod/registries/test/ModBlockStateProvider.java");
    static File modBlockTagProvider = new File("./src/main/java/net/bananashelp20/forgermod/registries/test/ModBlockTagProvider.java");

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
        RegistryInterpreterHelper.generateAndWriteBlocks(testFile, blockFile);
        RegistryInterpreterHelper.generateAndWriteBlockDatagen();
        return true;
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
