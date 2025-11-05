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
    static File testFile = new File("./src/main/java/net/bananashelp20/forgermod/registries/TestReg.java");
    static File testRegClassFile = new File("./src/main/java/net/bananashelp20/forgermod/registries/TestRegistryClass.java");
    static File modBlockLootTableProvider = new File("./src/main/java/net/bananashelp20/forgermod/registries/TestRegistryClass.java");
    static File modBlockStateProvider = new File("./src/main/java/net/bananashelp20/forgermod/registries/TestRegistryClass.java");
    static File modBlockTagProvider = new File("./src/main/java/net/bananashelp20/forgermod/registries/TestRegistryClass.java");


    static String modBlocksFileContent = getWholeFileContentTillGenerate(modBlocksFile);
    static String modItemsFileContent = getWholeFileContentTillGenerate(modItemsFile);
    static String modCreativeModeTabsFileContent = getWholeFileContentTillGenerate(modCreativeModeTabsFile);
    static String modRegistryContent = getWholeFileContentTillGenerate(modRegistry);
    static String testFileContent = getWholeFileContentTillGenerate(testFile);

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
            generateAndWriteBlocks(testFile, blockFile);
        } catch (Exception e) {
            System.err.println(e);
        }
        return true;
    }

    public static void generateAndWriteBlocks(File fileToWrite, File regFile) throws Exception {
        Scanner reader = new Scanner(regFile);
        String generated = getWholeFileContentTillGenerate(fileToWrite); //des muss vor da writer deklaration sei, weil sonst file leer is.
        FileWriter writer = new FileWriter(fileToWrite.getPath());
        String line;
        System.out.println(generated);
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
        writer.write(generated+ "\n}");
        reader.close();
        writer.close();
    }

    private static String generateSpecialBlocks(File registryFile, int index) throws FileNotFoundException {
        Scanner reader2 = new Scanner(registryFile);
        String output = "";
        String name = "";
        String properties = "";
        String dropOtherMethod = "";
        String dropOtherItem = "";
        String blockTagTool = "";
        String blockTagToolType = "";
        String method = "";
        try {
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
                blockTagTool = reader2.nextLine().trim();
                blockTagToolType = reader2.nextLine().trim();
                output += "    public static final DeferredBlock<Block> " + name.toUpperCase() + " = " + method + "(\"" + name.toLowerCase() + "\", " + properties + ");\n";
            }
            datagenBlockInit(dropOtherMethod, dropOtherItem, blockTagTool, blockTagToolType);
        } catch (Exception e) {
            System.err.println("hüfe");;
        }
        return output + "}";
    }

    public static String generateSimpleBlocks(File registryFile, int index) throws Exception {
        Scanner reader2 = new Scanner(registryFile);
        String output = "";
        String name = "";
        String properties = "";
        String dropOtherMethod = "";
        String dropOtherItem = "";
        String blockTagTool = "";
        String blockTagToolType = "";
        try {
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
                    blockTagTool = reader2.nextLine().trim();
                    blockTagToolType = reader2.nextLine().trim();
                    output += "    public static final DeferredBlock<Block> " + name.toUpperCase() + " = createSimpleBlock(\"" + name.toLowerCase() + "\", " + properties + ");\n";
                }
            }
            datagenBlockInit(dropOtherMethod, dropOtherItem, blockTagTool, blockTagToolType);
        } catch (Exception e) {
            System.err.println("hüfe");;
        }

        return output;
    }

    public static String generateComplexBlocks(File registryFile, int index) throws IOException {
        Scanner reader2 = new Scanner(registryFile);
        String output = "";
        String name = "";
        String properties = "";
        String dropOtherMethod = "";
        String dropOtherItem = "";
        String blockTagTool = "";
        String blockTagToolType = "";
        try {
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
                    blockTagTool = reader2.nextLine().trim();
                    blockTagToolType = reader2.nextLine().trim();
                    output += "    public static final DeferredBlock<Block> " + name.toUpperCase() + " = registerBlock(\"" + name.toLowerCase() + "\",\n" +
                            "            () -> " + properties +
                            ");\n";
                }
            }
            datagenBlockInit(dropOtherMethod, dropOtherItem, blockTagTool, blockTagToolType);
        } catch (Exception e) {
            System.err.println("hüfe");;
        }
        return "";
    }

    public static void datagenBlockInit(String dropOtherMethod, String dropOtherItem, String blockTagTool, String blockTagToolType) throws IOException {
        String generatedBlockLootTables = getWholeFileContentTillGenerate(modBlockLootTableProvider);
        String generatedBlockStateProvider = getWholeFileContentTillGenerate(modBlockStateProvider);
        String generatedBlockTagProvider = getWholeFileContentTillGenerate(modBlockTagProvider);
        FileWriter writer = new FileWriter(testRegClassFile.getPath());
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
//        FileWriter writer = new FileWriter();
//    }

    static int getWritablePos(File file, String commentCommand) throws FileNotFoundException {
        Scanner reader = new Scanner(file);
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

    static String getWholeFileContentTillGenerate(File file) {
        String saved = "";
        try {
            Scanner reader = new Scanner(file);
            int startGeneratingAtLine = getWritablePos(file, "//STARTGENERATING!");
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
