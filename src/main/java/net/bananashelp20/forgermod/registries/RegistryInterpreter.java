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
            generateAndWriteBlocks(TestFile, blockFile);
        } catch (Exception e) {
            System.err.println(e);
        }
        return true;
    }

    public static void generateAndWriteBlocks(File fileToWrite, File regFile) throws Exception {
        Scanner reader = new Scanner(regFile);
        FileWriter writer = new FileWriter(fileToWrite.getPath());
        String line;
        for (int i = 0; reader.hasNextLine(); i++) {
            line = reader.nextLine().trim();
            if (line.equalsIgnoreCase("simple {")) {
                System.out.println(generateSimpleBlocks(regFile, i+1));
            } else if (line.equalsIgnoreCase("special {")) {
                System.out.println(generateSpecialBlocks(regFile, i+1));
            } else if (line.equalsIgnoreCase("complex {")) {
                System.out.println(generateComplexBlocks(regFile, i+1));
            }
        }
        reader.close();
        writer.close();
    }

    private static String generateSpecialBlocks(File registryFile, int index) throws FileNotFoundException {
        Scanner reader2 = new Scanner(registryFile);
        String output = "";
        String name;
        String properties;
        String method;
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
                output += "    public static final DeferredBlock<Block> " + name.toUpperCase() + " = " + method + "(\"" + name.toLowerCase() + "\", " + properties + ");\n";
            }
        } catch (Exception e) {
            System.err.println("hüfe");;
        }

        return output;
    }

    public static String generateSimpleBlocks(File registryFile, int index) throws Exception {
        Scanner reader2 = new Scanner(registryFile);
        String output = "";
        String name;
        String properties;
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
                    output += "    public static final DeferredBlock<Block> " + name.toUpperCase() + " = createSimpleBlock(\"" + name.toLowerCase() + "\", " + properties + ");\n";
                }
            }
        } catch (Exception e) {
            System.err.println("hüfe");;
        }

        return output;
    }

    public static String generateComplexBlocks(File registryFile, int index) throws IOException {
        Scanner reader2 = new Scanner(registryFile);
        String output = "";
        String name;
        String properties;
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
                    output += "    public static final DeferredBlock<Block> " + name.toUpperCase() + " = registerBlock(\"" + name.toLowerCase() + "\",\n" +
                            "            () -> " + properties +
                            ";\n";
                }
            }
        } catch (Exception e) {
            System.err.println("hüfe");;
        }
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
