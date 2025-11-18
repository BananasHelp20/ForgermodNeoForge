package net.bananashelp20.forgermod.registries;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

public class RegistryInterpreterHelper {
    static File blockFile = new File("./src/main/java/net/bananashelp20/forgermod/registries/regFiles/blocks.txt");
    static File itemFile = new File("./src/main/java/net/bananashelp20/forgermod/registries/regFiles/items.txt");
    static File creativeTabFile = new File("./src/main/java/net/bananashelp20/forgermod/registries/regFiles/creativeTabs.txt");
    static File modItemsFile = new File("./src/main/java/net/bananashelp20/forgermod/item/ModItems.java");
    static File modCreativeModeTabsFile = new File("./src/main/java/net/bananashelp20/forgermod/CreativeModeTabs/ModCreativeModeTabs.java");
    static File modBlockFile = new File("./src/main/java/net/bananashelp20/forgermod/registries/test/ModBlocks.java");
    static File modRegistry = new File("./src/main/java/net/bananashelp20/forgermod/registries/RegistryClass.java");
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

    //helpermethod for datagen generation: generates The drops for the blocks
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

    //helpermethod for Blocktag generation: generates the Blocktags for the tools
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

    //gets the index of a String in a 2-dimensional list
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

    //helpermethod for Blocktag generation: generates the tools-blocktags
    public static String generateToolTags(String[] tags) {
        return "        tag(BlockTags.MINEABLE_WITH_PICKAXE)\n" +
                tags[0] + "        ;\n        tag(BlockTags.MINEABLE_WITH_AXE)\n" +
                tags[1] + "        ;\n        tag(BlockTags.MINEABLE_WITH_SHOVEL)\n" +
                tags[2] + "        ;\n        tag(BlockTags.MINEABLE_WITH_HOE)\n" +
                tags[3] + "        ;\n\n";
    }

    //helpermethod for datagen generation: generates the blockstates
    public static String generateBlockStates(ArrayList<ArrayList<String>> data) {
        String generatedBlockStates = "";
        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).get(2).equals("blockWithItem") || data.get(i).get(3).equals("blockWithItem")) {
                generatedBlockStates += "        blockWithItem(ModBlocks." + data.get(i).get(0).toUpperCase() + ");\n";
            }
        }
        return generatedBlockStates + "    }\n}";
    }

    //generates the blocks themselfs from the blocks.txt
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
            System.out.println(ANSI_GREEN + "Successfully wrote Blocks into Block Class!" + ANSI_RESET);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //helpermethod for the block generation: generates the special blocks
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

    //helpermethod for the blockgeneration: generates the simple blocks
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

    //helpermethod for the blockgeneration: generates the complex blocks
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

    //generates the Datagen for the blocks
    public static void generateAndWriteBlockDatagen() {
        Scanner reader;
        try {
            reader = new Scanner(blockFile);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
        String line;
        String name;
        for (int i = 0; reader.hasNextLine(); i+=0) {
            line = reader.nextLine().trim();
            if (line.equalsIgnoreCase("simple {") || line.equalsIgnoreCase("complex {")) {
                while (!(name = reader.nextLine().trim()).equals("}")) {
                    String dropOtherItem;
                    String dropOtherMethod;
                    String blockModel;
                    String blockTagTool;
                    String blockTagToolType;
                    String creativeTab;
                    reader.nextLine();
                    dropOtherItem = "";
                    dropOtherMethod = reader.nextLine().trim();
                    if (dropOtherMethod.equals("dropOther")) {
                        dropOtherItem = reader.nextLine().trim();
                    }
                    blockModel = reader.nextLine().trim();
                    blockTagTool = reader.nextLine().trim();
                    blockTagToolType = reader.nextLine().trim();
                    creativeTab = reader.nextLine().trim();
                    data.add(new ArrayList<String>());
                    data.get(i).add(name);
                    data.get(i).add(dropOtherMethod);
                    if (!dropOtherItem.equals("")) data.get(i).add(dropOtherItem);
                    data.get(i).add(blockModel);
                    data.get(i).add(blockTagTool);
                    data.get(i).add(blockTagToolType);
                    data.get(i).add(creativeTab);
                    i++;
                }
            } else if (line.equalsIgnoreCase("special {")) {
                while (!(name = reader.nextLine().trim()).equals("}")) {
                    reader.nextLine();
                    String dropOtherItem;
                    String dropOtherMethod;
                    String blockModel;
                    String blockTagTool;
                    String blockTagToolType;
                    String creativeTab;
                    reader.nextLine();
                    dropOtherItem = "";
                    dropOtherMethod = reader.nextLine().trim();
                    if (dropOtherMethod.equals("dropOther")) {
                        dropOtherItem = reader.nextLine().trim();
                    }
                    blockModel = reader.nextLine().trim();
                    blockTagTool = reader.nextLine().trim();
                    blockTagToolType = reader.nextLine().trim();
                    creativeTab = reader.nextLine().trim();
                    data.add(new ArrayList<String>());
                    data.get(i).add(name);
                    data.get(i).add(dropOtherMethod);
                    if (!dropOtherItem.equals("")) data.get(i).add(dropOtherItem);
                    data.get(i).add(blockModel);
                    data.get(i).add(blockTagTool);
                    data.get(i).add(blockTagToolType);
                    data.get(i).add(creativeTab);
                    i++;
                }
            }
        }
        String generatedBlockLootTables = getWholeFileContentTillGenerate(modBlockLootTableProvider, "//generate DROPS!") + RegistryInterpreterHelper.generateBlockLoottables(data);
        System.out.println(ANSI_GREEN + "Successfully wrote BlockLoottables!" + ANSI_RESET);
        String generatedBlockStates = getWholeFileContentTillGenerate(modBlockStateProvider, "//generate MODELS!") + RegistryInterpreterHelper.generateBlockStates(data);
        System.out.println(ANSI_GREEN + "Successfully wrote BlockStates!" + ANSI_RESET);
        String generatedBlockTags = getWholeFileContentTillGenerate(modBlockTagProvider, "//generate TAGS!") + generateBlockToolTags(data);
        System.out.println(ANSI_GREEN + "Successfully wrote BlockTags!" + ANSI_RESET);
        reader.close();
        try {
            FileWriter loottableWriter = new FileWriter(modBlockLootTableProvider.getPath());
            loottableWriter.write(generatedBlockLootTables);
            loottableWriter.close();
            FileWriter stateWriter = new FileWriter(modBlockStateProvider.getPath());
            stateWriter.write(generatedBlockStates);
            stateWriter.close();
            FileWriter tagWriter = new FileWriter(modBlockTagProvider.getPath());
            tagWriter.write(generatedBlockTags);
            tagWriter.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    //helperemthod for generating the blocktags: initialises the typetags
    public static ArrayList<String> initTypeTags() {
        ArrayList<String> typeTags = new ArrayList<>();
        Scanner tagReader;
        try {
            tagReader = new Scanner(modBlockTagProvider);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        String current;
        while (tagReader.hasNextLine()) {
            current = tagReader.nextLine().trim();
            if (current.contains("tag(") && current.contains("NEEDS_")) {
                typeTags.add(current.substring(0, current.length()-1).trim() + ")");
            }
        }
        tagReader.close();
        return typeTags;
    }

    //helpermethod for tagsByType initialisation
    public static ArrayList<ArrayList<String>> getContentForTags(boolean special, ArrayList<ArrayList<String>> tagsByType, Scanner reader) {
        String name = "";
        String drops = "";
        String typeTag = "";
        name = reader.nextLine().trim();
        for (int i = 0; reader.hasNextLine() && !name.equalsIgnoreCase("}"); i++) {
            reader.nextLine();
            if (special) reader.nextLine();
            drops = reader.nextLine().trim();
            reader.nextLine();
            reader.nextLine();
            if (drops.equalsIgnoreCase("dropOther")) reader.nextLine();
            typeTag = reader.nextLine().trim();
            try {
                tagsByType.get(RegistryInterpreterHelper.getIndexByValue(typeTag.toLowerCase(), tagsByType)).add(name);
            } catch (Exception e) {
                System.err.println(e);
                System.out.println(ANSI_YELLOW + "\nINDEXING BY VALUE FAILED!" + ANSI_RESET);
            }
            reader.nextLine();
            name = reader.nextLine().trim();
        }
        return tagsByType;
    }

    //helpermethod for blocktag generation: initialises tagsByType
    public static ArrayList<ArrayList<String>> initTagsByType(ArrayList<ArrayList<String>> tagsByType, ArrayList<String> types) {
        Scanner reader;
        try {
            reader = new Scanner(blockFile);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        String line = "";

        for (int i = 0; i < types.size(); i++) {
            tagsByType.add(new ArrayList<>());
            tagsByType.get(i).add(0, types.get(i));
        }
        while (reader.hasNextLine()) {
            line = reader.nextLine().trim();
            while (reader.hasNextLine() && !(line.equalsIgnoreCase("simple {") || line.equalsIgnoreCase("complex {") || line.equalsIgnoreCase("special {"))) {
                line = reader.nextLine().trim();
            }
            tagsByType = getContentForTags(line.equalsIgnoreCase("special {"), tagsByType, reader);

        }
        return tagsByType;
    }

    //helpermethod for datagen generation: generates the blocktags
    public static String generateBlockToolTags(ArrayList<ArrayList<String>> data) {
        ArrayList<String> typeTags = initTypeTags();
        ArrayList<String> types = new ArrayList<>();
        ArrayList<ArrayList<String>> tagsByType = new ArrayList<>();

        for (int i = 0; i < typeTags.size(); i++) {
            types.add(typeTags.get(i).substring(typeTags.get(i).indexOf("NEEDS_")+6, typeTags.get(i).indexOf("_TOOL")).toLowerCase());
        }

        tagsByType = initTagsByType(tagsByType, types);

        return RegistryInterpreterHelper.generateToolTags(RegistryInterpreterHelper.generateToolsForBlockTags(data)) + generateToolTypeTags(typeTags, tagsByType) + "    }\n}";
    }

    public static String generateToolTypeTags(ArrayList<String> types, ArrayList<ArrayList<String>> tagsByType) {
        String generated = "";
        for (int i = 0; i < types.size(); i++) {
            generated += "        " + types.get(i) + "\n";
            for (int j = 0; j < tagsByType.size(); j++) {
                if (tagsByType.get(j).size() > 1 && types.get(i).substring(types.get(i).indexOf("NEEDS_") + 6, types.get(i).indexOf("_TOOL")).equalsIgnoreCase(tagsByType.get(j).get(0))) {
                    for (int k = 1; k < tagsByType.get(j).size(); k++) {
                        generated += "                .add(ModBlocks." + tagsByType.get(j).get(k).toUpperCase() + ".get())\n";
                    }
                }
            }
            if (i < types.size()-1) generated += "        ;\n\n";
        }
        return generated += "        ;\n";
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

    static String getWholeFileContentTillGenerate(File file) {
        return getWholeFileContentTillGenerate(file, "//STARTGENERATING!");
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

    public static void generateAndWriteBlocksToCorrectCreativeTab() {
        ArrayList<String> registryFileContentList = RegistryInterpreter.getContentFromFileAsList(modRegistry);
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
                if (registryFileContentList.get(i-1).contains("IngredientTab")) {
                    checkAndCycleTabs(registryFileContentList, nameAndCreativeTab, i, "Ingredient", "ModBlocks.");
                } else if (registryFileContentList.get(i-1).contains("WeaponTab")) {
                    checkAndCycleTabs(registryFileContentList, nameAndCreativeTab, i, "Weapon", "ModBlocks.");
                } else if (registryFileContentList.get(i-1).contains("ItemTab")) {
                    checkAndCycleTabs(registryFileContentList, nameAndCreativeTab, i, "Item", "ModBlocks.");
                } else if (registryFileContentList.get(i-1).contains("MiscellaneousTab")) {
                    checkAndCycleTabs(registryFileContentList, nameAndCreativeTab, i, "Miscellaneous", "ModBlocks.");
                } else if (registryFileContentList.get(i-1).contains("BlockTab")) {
                    checkAndCycleTabs(registryFileContentList, nameAndCreativeTab, i, "Block", "ModBlocks.");
                }
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

    public static void checkAndCycleTabs(ArrayList<String> registry, ArrayList<ArrayList<String>> nameAndTab, int i, String tab, String type) {
        for (int j = 0; j < nameAndTab.size(); j++) {
            if (nameAndTab.get(j).get(1).contains(tab.toLowerCase())) {
                registry.add(i+1, "                " + type + nameAndTab.get(j).get(0).toUpperCase() + ".get(),\n");
            }
        }
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
}