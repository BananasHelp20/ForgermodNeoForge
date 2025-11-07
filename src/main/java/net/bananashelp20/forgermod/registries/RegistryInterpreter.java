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
                && creativeTabsFile.exists() && creativeTabsFile.canRead()
        )) {
            return false;
        }
        RegistryInterpreterHelper.generateAndWriteBlocks(testFile, blockFile);
        System.out.println("wroteBlocks!");
        generateAndWriteBlockDatagen();
        return true;
    }

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
        System.out.println("wroteLoottables!");
        String generatedBlockStates = getWholeFileContentTillGenerate(modBlockStateProvider, "//generate MODELS!") + RegistryInterpreterHelper.generateBlockStates(data);
        System.out.println("wroteModels!");
        String generatedBlockTags = getWholeFileContentTillGenerate(modBlockTagProvider, "//generate TAGS!") + generateBlockToolTags(data);
        System.out.println("wroteTags!");
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

    public static ArrayList<ArrayList<String>> initTagsByType(ArrayList<ArrayList<String>> tagsByType, ArrayList<String> types) {
        Scanner tagReader2;
        try {
            tagReader2 = new Scanner(blockFile);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        String line = "";
        String line2 = "";
        String name = "";
        String drops = "";
        String currentTag = "";

        for (int i = 0; i < types.size(); i++) {
            tagsByType.add(new ArrayList<>());
            tagsByType.get(i).add(0, types.get(i));
        }

        for (int j = 0; tagReader2.hasNextLine() && !name.equals("}"); j++) {
            line = tagReader2.nextLine().trim();
            if (line.equalsIgnoreCase("simple {") || line.equalsIgnoreCase("complex {")) {
                while (tagReader2.hasNextLine() && !name.equalsIgnoreCase("}")) {
                    name = tagReader2.nextLine().trim();
                    if (!name.equalsIgnoreCase("}")) {
                        tagReader2.nextLine();
                        drops = tagReader2.nextLine().trim();
                        tagReader2.nextLine();
                        tagReader2.nextLine();
                        if (drops.equals("dropSelf") || drops.equals("dropWhenSilkTouch")) {
                            currentTag = tagReader2.nextLine().trim();
                            tagsByType.get(RegistryInterpreterHelper.getIndexByValue(currentTag, tagsByType)).add(name);
                            System.out.println("gotIndex!");
                        } else {
                            tagReader2.nextLine();
                            currentTag = tagReader2.nextLine().trim();
                            tagsByType.get(RegistryInterpreterHelper.getIndexByValue(currentTag, tagsByType)).add(name);
                            System.out.println("gotIndex!");
                        }
                    }
                }
            } else if (line.equalsIgnoreCase("special {")) {
                while (tagReader2.hasNextLine() && !name.equalsIgnoreCase("}")) {
                    name = tagReader2.nextLine().trim();
                    if (!name.equalsIgnoreCase("}")) {
                        tagReader2.nextLine();
                        tagReader2.nextLine();
                        drops = tagReader2.nextLine().trim();
                        tagReader2.nextLine();
                        tagReader2.nextLine();
                        if (drops.equals("dropSelf") || drops.equals("dropWhenSilkTouch")) {
                            currentTag = tagReader2.nextLine().trim();
                            tagsByType.get(RegistryInterpreterHelper.getIndexByValue(currentTag, tagsByType)).add(name);
                            System.out.println("gotIndex!");
                        } else {
                            tagReader2.nextLine();
                            currentTag = tagReader2.nextLine().trim();
                            tagsByType.get(RegistryInterpreterHelper.getIndexByValue(currentTag, tagsByType)).add(name);
                            System.out.println("gotIndex!");
                        }
                    }
                }
            }
        }

        return tagsByType;
    }

    public static String generateBlockToolTags(ArrayList<ArrayList<String>> data) {
        ArrayList<String> typeTags = initTypeTags();
        System.out.println("initTypeTags!");
        ArrayList<String> types = new ArrayList<>();
        ArrayList<ArrayList<String>> tagsByType = new ArrayList<>();
        System.out.println("initTagsbyType!");

        for (int i = 0; i < typeTags.size(); i++) {
            types.add(typeTags.get(i).substring(typeTags.get(i).indexOf("NEEDS_")+6, typeTags.get(i).indexOf("_TOOL")).toLowerCase());
        }

        tagsByType = initTagsByType(tagsByType, types);

        return RegistryInterpreterHelper.generateToolTags(RegistryInterpreterHelper.generateToolsForBlockTags(data)) + generateToolTypeTags(typeTags, tagsByType) + "\n    }\n}";
    }

    public static String generateToolTypeTags(ArrayList<String> types, ArrayList<ArrayList<String>> tagsByType) {
        String generated = "";
        for (int i = 0; i < types.size(); i++) {
            generated += "        " + types.get(i);
            for (int j = 0; j < tagsByType.size(); j++) {
                if (types.get(i).substring(types.get(i).indexOf("NEEDS_") + 6, types.get(i).indexOf("_TOOL")).equals(tagsByType.get(j).get(0))) {
                    for (int k = 0; k < tagsByType.get(j).size(); k++) {
                        generated += "                .add(ModBlocks." + tagsByType.get(j).get(k) + ".get())\n";
                    }
                }
            }
            generated += "\n        ;\n";
        }
        return generated;
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
