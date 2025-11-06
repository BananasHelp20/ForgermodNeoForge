package net.bananashelp20.forgermod.registries;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;

public class RegistryInterpreterHelper {
    static File blockFile = new File("./src/main/java/net/bananashelp20/forgermod/registries/regFiles/blocks.txt");
    static File modBlocksFile = new File("./src/main/java/net/bananashelp20/forgermod/block/ModBlocks.java");
    static File itemFile = new File("./src/main/java/net/bananashelp20/forgermod/registries/regFiles/items.txt");
    static File creativeTabsFile = new File("./src/main/java/net/bananashelp20/forgermod/registries/regFiles/creativeTabs.txt");
    static File modItemsFile = new File("./src/main/java/net/bananashelp20/forgermod/item/ModItems.java");
    static File modCreativeModeTabsFile = new File("./src/main/java/net/bananashelp20/forgermod/CreativeModeTabs/ModCreativeModeTabs.java");
    static File modRegistry = new File("./src/main/java/net/bananashelp20/forgermod/registries/RegistryClass.java");
    static File testFile = new File("./src/main/java/net/bananashelp20/forgermod/registries/test/TestReg.java");
    static File modBlockLootTableProvider = new File("./src/main/java/net/bananashelp20/forgermod/registries/test/ModBlockLootTableProvider.java");
    static File modBlockStateProvider = new File("./src/main/java/net/bananashelp20/forgermod/registries/test/ModBlockStateProvider.java");
    static File modBlockTagProvider = new File("./src/main/java/net/bananashelp20/forgermod/registries/test/ModBlockTagProvider.java");

    static String modBlocksFileContent = getWholeFileContentTillGenerate(modBlocksFile);
    static String modItemsFileContent = getWholeFileContentTillGenerate(modItemsFile);
    static String modCreativeModeTabsFileContent = getWholeFileContentTillGenerate(modCreativeModeTabsFile);
    static String modRegistryContent = getWholeFileContentTillGenerate(modRegistry);
    static String testFileContent = getWholeFileContentTillGenerate(testFile);

    static String getWholeFileContentTillGenerate(File file) {
        String saved = "";
        Scanner reader = null;
        try {
            reader = new Scanner(file);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        int startGeneratingAtLine = getWritablePos(file, "//STARTGENERATING!");
        for (int i = 1; i < startGeneratingAtLine && reader.hasNextLine(); i++) {
            saved += reader.nextLine() + "\n";
        }
        reader.close();
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
        reader.close();
        return content;
    }

    public static void generateAndWriteBlocks(File fileToWrite, File regFile) {
        Scanner reader;
        try {
            reader = new Scanner(regFile);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        String generated = getWholeFileContentTillGenerate(fileToWrite); //des muss vor da writer deklaration sei, weil sonst file leer is.
        String line;
        for (int i = 0; reader.hasNextLine(); i++) {
            line = reader.nextLine().trim();
            if (line.equalsIgnoreCase("simple {")) {
                generated += "\n    //Simple Blocks\n";
                generated += generateSimpleBlocks(regFile, i+1);
            } else if (line.equalsIgnoreCase("special {")) {
                generated += "\n    //Special Blocks\n";
                generated += generateSpecialBlocks(regFile, i+1);
            } else if (line.equalsIgnoreCase("complex {")) {
                generated += "\n    //Complex Blocks\n";
                generated += generateComplexBlocks(regFile, i+1);
            }
        }
        try {
            FileWriter writer = new FileWriter(fileToWrite.getPath());
            writer.write(generated + "\n}");
            reader.close();
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String generateSpecialBlocks(File registryFile, int index) {
        Scanner reader2 = null;
        try {
            reader2 = new Scanner(registryFile);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        String output = "";
        String name = "";
        String properties = "";
        String dropOtherMethod = "";
        String dropOtherItem = "";
        String blockTagTool = "";
        String blockTagToolType = "";
        String method = "";
        String blockModel = "";
        for (int i = 0; reader2.hasNextLine() && i < index; i++) {
            reader2.nextLine();
        }
        for (int i = 0; reader2.hasNextLine(); i++) {
            name = reader2.nextLine().trim();
            if (name.equals("}")) {
                return output;
            }
            method = reader2.nextLine().trim();
            properties = reader2.nextLine().trim();
            dropOtherMethod = reader2.nextLine().trim();
            if (!dropOtherMethod.equals("dropSelf")) {
                dropOtherItem = reader2.nextLine().trim();
            }
            blockModel = reader2.nextLine().trim();
            blockTagTool = reader2.nextLine().trim();
            blockTagToolType = reader2.nextLine().trim();
            reader2.nextLine();
            output += "    public static final DeferredBlock<Block> " + name.toUpperCase() + " = " + method + "(\"" + name.toLowerCase() + "\", " + properties + ");\n";
        }
        reader2.close();
        return output + "}";
    }

    public static String generateSimpleBlocks(File registryFile, int index) {
        Scanner reader2;
        try {
            reader2 = new Scanner(registryFile);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        String output = "";
        String name = "";
        String properties = "";
        String dropOtherMethod = "";
        String dropOtherItem = "";
        String blockModel = "";
        String blockTagTool = "";
        String blockTagToolType = "";
        for (int i = 0; reader2.hasNextLine() && i < index; i++) {
            reader2.nextLine();
        }
        for (int i = 0; reader2.hasNextLine(); i++) {
            name = reader2.nextLine().trim();
            if (name.equals("}")) {
                return output;
            }
            if (reader2.hasNextLine()) {
                properties = reader2.nextLine().trim();
                dropOtherMethod = reader2.nextLine().trim();
                if (!dropOtherMethod.equals("dropSelf")) {
                    dropOtherItem = reader2.nextLine().trim();
                }
                blockModel = reader2.nextLine().trim();
                blockTagTool = reader2.nextLine().trim();
                blockTagToolType = reader2.nextLine().trim();
                reader2.nextLine().trim();
                output += "    public static final DeferredBlock<Block> " + name.toUpperCase() + " = createSimpleBlock(\"" + name.toLowerCase() + "\", " + properties + ");\n";
            }
        }
        reader2.close();
        return output;
    }

    public static String generateComplexBlocks(File registryFile, int index) {
        Scanner reader2;
        try {
            reader2 = new Scanner(registryFile);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        String output = "";
        String name = "";
        String properties = "";
        String dropOtherMethod = "";
        String dropOtherItem = "";
        String blockTagTool = "";
        String blockModel = "";
        String blockTagToolType = "";
        for (int i = 0; reader2.hasNextLine() && i < index; i++) {
            reader2.nextLine();
        }
        for (int i = 0; reader2.hasNextLine(); i++) {
            name = reader2.nextLine().trim();
            if (name.equals("}")) {
                return output;
            }
            if (reader2.hasNextLine()) {
                properties = reader2.nextLine().trim();
                dropOtherMethod = reader2.nextLine().trim();
                if (!dropOtherMethod.equals("dropSelf")) {
                    dropOtherItem = reader2.nextLine().trim();
                }
                blockModel = reader2.nextLine().trim();
                blockTagTool = reader2.nextLine().trim();
                blockTagToolType = reader2.nextLine().trim();
                reader2.nextLine().trim();
                output += "    public static final DeferredBlock<Block> " + name.toUpperCase() + " = registerBlock(\"" + name.toLowerCase() + "\",\n" +
                        "            () -> " + properties +
                        ");\n";
            }
        }
        reader2.close();
        return output;
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
        reader.close();
        return -1;
    }
}
