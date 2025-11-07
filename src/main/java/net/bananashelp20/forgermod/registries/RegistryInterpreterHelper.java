package net.bananashelp20.forgermod.registries;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
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

    public static String generateBlockLoottables(ArrayList<ArrayList<String>> data) {
        String generatedBlockLootTables = "";
        String itemType = "";
        int nameIndex = 0;
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).get(1).equals("dropSelf") || data.get(i).get(1).equals("dropWhenSilkTouch")) {
                generatedBlockLootTables += "        " + data.get(i).get(1) + "(ModBlocks." + data.get(i).get(0).toUpperCase() + ".get());\n";
            } else if (data.get(i).get(1).equals("dropOther")) {
                if (data.get(i).get(2).startsWith("mod")) {
                    if (data.get(i).get(2).charAt(4) == 'i') {
                        itemType = "ModItems.";
                        nameIndex = "item ".length() + data.get(i).get(2).indexOf("item");
                    } else {
                        itemType = "ModBlocks.";
                        nameIndex = "block ".length() + data.get(i).get(2).indexOf("block");
                    }
                } else if (data.get(i).get(2).startsWith("normal")) {
                    if (data.get(i).get(2).charAt(7) == 'i') {
                        itemType = "Items.";
                        nameIndex = "item ".length() + data.get(i).get(2).indexOf("item");
                    } else {
                        itemType = "blocks.";
                        nameIndex = "block ".length() + data.get(i).get(2).indexOf("block");
                    }
                }
                generatedBlockLootTables += "        dropOther(ModBlocks." + data.get(i).get(0).toUpperCase() + ".get(), " + itemType + data.get(i).get(2).substring(nameIndex).toUpperCase() + (data.get(i).get(2).startsWith("mod") ? ".get())" : "") + ";\n";
            }
        }
        return generatedBlockLootTables + "    }\n}";
    }

    public static String[] generateToolsForBlockTags(ArrayList<ArrayList<String>> data) {
        String pickaxeTags = "";
        String axeTags = "";
        String shovelTags = "";
        String hoeTags = "";
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).get(3).equalsIgnoreCase("pickaxe") || data.get(i).get(4).equals("pickaxe")) {
                pickaxeTags += "                .add(ModBlocks." + data.get(i).get(0).toUpperCase() + ".get())\n";
            } else if (data.get(i).get(3).equalsIgnoreCase("axe") || data.get(i).get(4).equals("axe")) {
                axeTags += "                .add(ModBlocks." + data.get(i).get(0).toUpperCase() + ".get())\n";
            } else if (data.get(i).get(3).equalsIgnoreCase("shovel") || data.get(i).get(4).equals("shovel")) {
                shovelTags += "                .add(ModBlocks." + data.get(i).get(0).toUpperCase() + ".get())\n";
            } else if (data.get(i).get(3).equalsIgnoreCase("hoe") || data.get(i).get(4).equals("hoe")) {
                hoeTags += "                .add(ModBlocks." + data.get(i).get(0).toUpperCase() + ".get())\n";
            }
        }

        return new String[] {pickaxeTags, axeTags, shovelTags, hoeTags};
    }

    public static int getIndexByValue(String searched, ArrayList<ArrayList<String>> tags) {
        if (tags.get(0) == null) {
            return 0;
        }
        for (int i = 0; i < tags.size(); i++) {
            if (tags.get(i).get(0).equalsIgnoreCase(searched)) {
                return i;
            }
        }
        System.err.println("DIDNT GET INDEX!!!");
        return -1;
    }

    public static String generateToolTags(String[] tags) {
        return "        tag(BlockTags.MINEABLE_WITH_PICKAXE)\n" +
                tags[0] + "        ;\n        tag(BlockTags.MINEABLE_WITH_AXE)\n" +
                tags[1] + "        ;\n        tag(BlockTags.MINEABLE_WITH_SHOVEL)\n" +
                tags[2] + "        ;\n        tag(BlockTags.MINEABLE_WITH_HOE)\n" +
                tags[3] + "        ;\n\n";
    }

    public static String generateBlockStates(ArrayList<ArrayList<String>> data) {
        String generatedBlockStates = "";
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).get(2).equals("blockWithItem") || data.get(i).get(3).equals("blockWithItem")) {
                generatedBlockStates += "        blockWithItem(ModBlocks." + data.get(i).get(0).toUpperCase() + ");\n";
            }
        }
        return generatedBlockStates + "    }\n}";
    }

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
