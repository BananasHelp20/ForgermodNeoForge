package net.bananashelp20.forgermod.registries;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class RegistryInterpreter {
    static File blockFile = new File("./src/main/java/net/bananashelp20/forgermod/registries/regFiles/blocks.txt");
    static File modBlocksFile = new File("./src/main/java/net/bananashelp20/forgermod/block/ModBlocks.java");
    static File itemFile = new File("./src/main/java/net/bananashelp20/forgermod/registries/regFiles/items.txt");
    static File modItemsFile = new File("./src/main/java/net/bananashelp20/forgermod/item/ModItems.java");
    static File creativeTabFile = new File("./src/main/java/net/bananashelp20/forgermod/registries/regFiles/creativeTabs.txt");
    static File modCreativeModeTabsFile = new File("./src/main/java/net/bananashelp20/forgermod/CreativeModeTabs/ModCreativeModeTabs.java");
    static File modRegistry = new File("./src/main/java/net/bananashelp20/forgermod/registries/RegistryClass.java");
    static File TestFile = new File("./src/main/java/net/bananashelp20/forgermod/registries/TestReg.java");

    static String modBlocksFileContent = getWholeFileContentTillGenerate(modBlocksFile);
    static String modItemsFileContent = getWholeFileContentTillGenerate(modItemsFile);
    static String modCreativeModeTabsFileContent = getWholeFileContentTillGenerate(modCreativeModeTabsFile);
    static String modRegistryContent = getWholeFileContentTillGenerate(modRegistry);
    static String testFileContent = getWholeFileContentTillGenerate(TestFile);

    public static void main(String[] args) throws Exception {
        if (!generateCode()) {
            throw new FileNotFoundException("Code could not be generated, an Error occurred");
        }
    }

    public static boolean generateCode() {
        if (!(modBlocksFile.exists() && modBlocksFile.canWrite() && modBlocksFile.canRead()
                && modItemsFile.exists() && modItemsFile.canWrite() && modItemsFile.canRead()
                && modCreativeModeTabsFile.exists() && modCreativeModeTabsFile.canWrite() && modCreativeModeTabsFile.canRead()
                && modRegistry.exists() && modRegistry.canWrite() && modRegistry.canRead()
                && blockFile.exists() && blockFile.canRead()
                && itemFile.exists() && itemFile.canRead()
                && creativeTabFile.exists() && creativeTabFile.canRead()
        )) {
            return false;
        }
        try {
            generateSimpleBlocks(TestFile, blockFile);
        } catch (Exception e) {
            System.err.println(e);
        }
        return true;
    }

    public static String generateSimpleBlocks(File fileToWrite, File registryFile) throws Exception {
        Scanner reader = new Scanner(fileToWrite);
        FileWriter writer = new FileWriter(fileToWrite.getPath());
        return "";
    }

    public static String generateComplexBlocks(File fileToWrite, File registryFile) {
        return "";
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

    static int getWritablePos(File file) throws FileNotFoundException {
        Scanner reader = new Scanner(file);
        String searcher;
        for (int i = 1; reader.hasNextLine(); i++) {
            searcher = reader.nextLine();
            if (searcher.trim().equals("//STARTGENERATING")) {
                reader.close();
                return i + 1;
            }
        }
        return -1;
    }

    static String getWholeFileContentTillGenerate(File file) {
        String saved = "";
        try {
            Scanner reader = new Scanner(file);
            int startGeneratingAtLine = getWritablePos(file);
            for (int i = 1; i < startGeneratingAtLine && reader.hasNextLine(); i++) {
                saved += reader.nextLine() + "\n";
            }
            reader.close();
        } catch (Exception e) {
            System.err.println(e);
        }
        return saved;
    }

    static String getContentFromFile(File file) throws FileNotFoundException {
        Scanner reader = new Scanner(file);
        String content = "";
        while (reader.hasNextLine()) {
            content += reader.nextLine() + "\n";
        }
        return content;
    }
}
