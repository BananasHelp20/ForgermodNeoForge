package net.bananashelp20.forgermod.registries;

import org.checkerframework.checker.units.qual.A;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Scanner;

public class RegistryInterpreter {
    static File blockFile = new File("./src/main/java/net/bananashelp20/forgermod/registries/regFiles/blocks.txt");
    static File modBlocksFile = new File("./src/main/java/net/bananashelp20/forgermod/block/ModBlocks.java");
    static File itemFile = new File("./src/main/java/net/bananashelp20/forgermod/registries/regFiles/items.txt");
    static File modItemsFile = new File("./src/main/java/net/bananashelp20/forgermod/item/ModItems.java");
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
        )) {
            return false;
        }
        try {
            generateAndWriteBlocks(testFile, blockFile);
            generateAndWriteBlockDatagen();
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

    public static String generateSimpleBlocks(File registryFile, int index) throws Exception {
        Scanner reader2 = new Scanner(registryFile);
        String output = "";
        String name = "";
        String properties = "";
        String dropOtherMethod = "";
        String dropOtherItem = "";
        String blockModel = "";
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
                    blockModel = reader2.nextLine().trim();
                    blockTagTool = reader2.nextLine().trim();
                    blockTagToolType = reader2.nextLine().trim();
                    reader2.nextLine().trim();
                    output += "    public static final DeferredBlock<Block> " + name.toUpperCase() + " = createSimpleBlock(\"" + name.toLowerCase() + "\", " + properties + ");\n";
                }
            }
        } catch (Exception e) {
            System.err.println("hüfe");
            throw e;
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
        String blockModel = "";
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
                    blockModel = reader2.nextLine().trim();
                    blockTagTool = reader2.nextLine().trim();
                    blockTagToolType = reader2.nextLine().trim();
                    reader2.nextLine().trim();
                    output += "    public static final DeferredBlock<Block> " + name.toUpperCase() + " = registerBlock(\"" + name.toLowerCase() + "\",\n" +
                            "            () -> " + properties +
                            ");\n";
                    reader2.close();
                }
            }
        } catch (Exception e) {
            System.err.println("hüfe:" + e);
            throw e;
        }
        return "";
    }

    public static void generateAndWriteBlockDatagen() throws IOException {
        String generatedBlockLootTables = getWholeFileContentTillGenerate(modBlockLootTableProvider);
        String generatedBlockStateProvider = getWholeFileContentTillGenerate(modBlockStateProvider);
        String generatedBlockTagProvider = getWholeFileContentTillGenerate(modBlockTagProvider);
        int writableLoottablePos = getWritablePos(modBlockLootTableProvider, "//generate DROPS!");
        int writableStatePos = getWritablePos(modBlockLootTableProvider, "//generate MODELS!");
        int writableTagPos = getWritablePos(modBlockLootTableProvider, "//generate TAGS!");
        Scanner reader = new Scanner(blockFile);
        FileWriter loottableWriter = new FileWriter(modBlockLootTableProvider.getPath());
        FileWriter stateWriter = new FileWriter(modBlockStateProvider.getPath());
        FileWriter tagWriter = new FileWriter(modBlockTagProvider.getPath());
        ArrayList<ArrayList<String>> data = new ArrayList<ArrayList<String>>();
        String dropOtherMethod;
        String dropOtherItem = "";
        String blockTagTool;
        String blockTagToolType;
        String blockModel;
        String creativeTab = "";
        String line = "";
        String name = "";
        for (int i = 0; reader.hasNextLine(); i++) {
            line = reader.nextLine().trim();
            if (line.equalsIgnoreCase("simple {")) {
                for (int j = 0; (name = reader.nextLine().trim()).equals("}"); j++) {
                    reader.nextLine();
                    dropOtherItem = "";
                    dropOtherMethod = reader.nextLine().trim();
                    if (!dropOtherMethod.equals("dropSelf")) {
                        dropOtherItem = reader.nextLine().trim();
                    }
                    blockModel = reader.nextLine().trim();
                    blockTagTool = reader.nextLine().trim();
                    blockTagToolType = reader.nextLine().trim();
                    creativeTab = reader.nextLine().trim();
                    data.add(new ArrayList<String>());
                    data.get(j).add(name);
                    data.get(j).add(dropOtherMethod);
                    if (!dropOtherItem.equals("")) data.get(j).add(dropOtherItem);
                    data.get(j).add(blockModel);
                    data.get(j).add(blockTagTool);
                    data.get(j).add(blockTagToolType);
                    data.get(j).add(creativeTab);
                }
            } else if (line.equalsIgnoreCase("special {")) {
                for (int j = 0; (name = reader.nextLine().trim()).equals("}"); j++) {
                    reader.nextLine();
                    reader.nextLine();
                    dropOtherItem = "";
                    dropOtherMethod = reader.nextLine().trim();
                    if (!dropOtherMethod.equals("dropSelf")) {
                        dropOtherItem = reader.nextLine().trim();
                    }
                    blockModel = reader.nextLine().trim();
                    blockTagTool = reader.nextLine().trim();
                    blockTagToolType = reader.nextLine().trim();
                    creativeTab = reader.nextLine().trim();
                    data.add(new ArrayList<String>());
                    data.get(j).add(name);
                    data.get(j).add(dropOtherMethod);
                    if (!dropOtherItem.equals("")) data.get(j).add(dropOtherItem);
                    data.get(j).add(blockModel);
                    data.get(j).add(blockTagTool);
                    data.get(j).add(blockTagToolType);
                    data.get(j).add(creativeTab);
                }
            } else if (line.equalsIgnoreCase("complex {")) {
                for (int j = 0; (name = reader.nextLine().trim()).equals("}"); j++) {
                    reader.nextLine();
                    dropOtherItem = "";
                    dropOtherMethod = reader.nextLine().trim();
                    if (!dropOtherMethod.equals("dropSelf")) {
                        dropOtherItem = reader.nextLine().trim();
                    }
                    blockModel = reader.nextLine().trim();
                    blockTagTool = reader.nextLine().trim();
                    blockTagToolType = reader.nextLine().trim();
                    creativeTab = reader.nextLine().trim();
                    data.add(new ArrayList<String>());
                    data.get(j).add(name);
                    data.get(j).add(dropOtherMethod);
                    if (!dropOtherItem.equals("")) data.get(j).add(dropOtherItem);
                    data.get(j).add(blockModel);
                    data.get(j).add(blockTagTool);
                    data.get(j).add(blockTagToolType);
                    data.get(j).add(creativeTab);

                }
            }
        }
        reader.close();
        generateDatagenBlockCode(data);
    }

    public static void  generateDatagenBlockCode(ArrayList<ArrayList<String>> data) throws IOException {
        String generatedBlockLootTables = getWholeFileContentTillGenerate(modBlockLootTableProvider);
        String generatedBlockStates = getWholeFileContentTillGenerate(modBlockStateProvider);
        String generatedBlockTags = getWholeFileContentTillGenerate(modBlockTagProvider);
        int writableLoottablePos = getWritablePos(modBlockLootTableProvider, "//generate DROPS!");
        int writableStatePos = getWritablePos(modBlockLootTableProvider, "//generate MODELS!");
        int writableTagPos = getWritablePos(modBlockLootTableProvider, "//generate TAGS!");

        int normalListSize = (data.get(0).get(2).equals("dropSelf") ? data.get(0).size() : data.get(0).size()-1);

        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).get(0).equals("dropSelf") || data.get(i).get(0).equals("dropWhenSilkTouch")) {
                generatedBlockLootTables += "        " + data.get(i).get(2) + "(ModBlocks." + data.get(i).get(0).toUpperCase() + ".get());\n";
            }
            if (data.get(i).get(0).equals("dropOther")) {
                generatedBlockLootTables += "        dropOther(ModBlocks." + data.get(i).get(0).toUpperCase() + ".get(), " + data.get(i).get(3).toUpperCase() + ".get());\n";
            }
        }
        generatedBlockLootTables += "    }\n}";

        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).get(3).equals("blockWithItem") || data.get(i).get(4).equals("blockWithItem")) {
                generatedBlockStates += "blockWithItem(ModBlocks." + data.get(i).get(0) +");\n";
            }
        }
        generatedBlockStates+= "    }\n}";

        String pickaxeTags = "";
        String axeTags = "";
        String shovelTags = "";
        String hoeTags = "";

        for (int i = 0; i < data.size(); i++) {
            if (data.get(i).get(4).equals("pickaxe") || data.get(i).get(5).equals("pickaxe")) {
                pickaxeTags += "                .add(ModBlocks." + data.get(i).get(0).toUpperCase() + ".get())\n";
            } else if (data.get(i).get(4).equals("axe") || data.get(i).get(5).equals("axe")) {
                axeTags += "                .add(ModBlocks." + data.get(i).get(0).toUpperCase() + ".get())\n";
            } else if (data.get(i).get(4).equals("shovel") || data.get(i).get(5).equals("shovel")) {
                shovelTags += "                .add(ModBlocks." + data.get(i).get(0).toUpperCase() + ".get())\n";
            } else if (data.get(i).get(4).equals("hoe") || data.get(i).get(5).equals("hoe")) {
                hoeTags += "                .add(ModBlocks." + data.get(i).get(0).toUpperCase() + ".get())\n";
            }
        }
        generatedBlockTags += generateToolTags(new String[] {pickaxeTags, axeTags, shovelTags, hoeTags});
        ArrayList<String> typeTags = new ArrayList<>();
        ArrayList<String> types = new ArrayList<>();
        ArrayList<ArrayList<String>> tagsByType = new ArrayList<ArrayList<String>>();
        Scanner tagReader = new Scanner(modBlockTagProvider);
        String current;
        for (int i = 0; tagReader.hasNextLine(); i++) {
            current = tagReader.nextLine().trim();
            if (current.contains("tag(") && current.contains("NEEDS_")) {
                typeTags.add(current);
            }
        }
        tagReader.close();

        for (int i = 0; i < typeTags.size(); i++) {
            types.add(typeTags.get(i).substring(25, typeTags.get(i).length() - 6).toLowerCase());
        }
        Scanner tagReader2 = new Scanner(blockFile);
        String tags = "";
        String line = "";
        String name = "";
        String drops = "";
        String currentTag = "";
        for (int i = 0; i < types.size(); i++) {
            tagsByType.get(i).set(0, types.get(i));
        }

        for (int j = 0; tagReader2.hasNextLine() && !name.equals("}"); j++) {
            line = tagReader2.nextLine().trim();
            if (line.equalsIgnoreCase("simple {") || line.equalsIgnoreCase("special {") || line.equalsIgnoreCase("complex {")) {
                tags += "\n    //" + line.substring(0, 6)+ " blocks\n";
                name = tagReader2.nextLine().trim();
                tagReader2.nextLine();
                drops = tagReader2.nextLine().trim();
                tagReader2.nextLine();
                if (drops.equals("dropSelf") || drops.equals("dropWhenSilkTouch")) {
                    currentTag = tagReader2.nextLine().trim();
                    tagsByType.get(getIndexByValue(currentTag, tagsByType)).set(j, name);
                } else {
                    tagReader2.nextLine();
                    currentTag = tagReader2.nextLine().trim();
                    tagsByType.get(getIndexByValue(currentTag, tagsByType)).set(j, name);
                }
            }
        }
        generatedBlockTags += generateToolTypeTags(typeTags, tagsByType);
        generatedBlockTags += "    }\n}";

        FileWriter loottableWriter = new FileWriter(modBlockLootTableProvider.getPath());
        FileWriter stateWriter = new FileWriter(modBlockStateProvider.getPath());
        FileWriter tagWriter = new FileWriter(modBlockTagProvider.getPath());

        loottableWriter.write(generatedBlockLootTables);
        stateWriter.write(generatedBlockStates);
        tagWriter.write(generatedBlockTags);

        loottableWriter.close();
        stateWriter.close();
        tagWriter.close();
    }

    public static int getIndexByValue(String searched, ArrayList<ArrayList<String>> tags) {
        for (int i = 0; i < tags.size(); i++) {
            if (tags.get(i).get(0).equals(searched)) {
                return i;
            }
        }
        return -1;
    }

    public static String generateToolTypeTags(ArrayList<String> types, ArrayList<ArrayList<String>> tagsByType) {
        String generated = "";
        for (int i = 0; i < types.size(); i++) {
            generated += "        " + types.get(i);
            for (int j = 0; j < tagsByType.size(); j++) {
                if (types.get(i).substring(26, types.get(i).length()-6).equals(tagsByType.get(j).get(0))) {
                    for (int k = 0; k < tagsByType.get(j).size(); k++) {
                        generated += "                .add(ModBlocks." + tagsByType.get(j).get(k) + ".get())\n";
                    }
                }
            }
            generated += "        ;\n";
        }
        return generated;
    }

    public static String generateToolTags(String[] tags) {
        return "        tag(BlockTags.MINEABLE_WITH_PICKAXE)\n" +
                tags[0] + "        ;\n        tag(BlockTags.MINEABLE_WITH_AXE)\n" +
                tags[1] + "        ;\n        tag(BlockTags.MINEABLE_WITH_SHOVEL)\n" +
                tags[2] + "        ;\n        tag(BlockTags.MINEABLE_WITH_HOE)\n" +
                tags[3] + "        ;\n\n";
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
