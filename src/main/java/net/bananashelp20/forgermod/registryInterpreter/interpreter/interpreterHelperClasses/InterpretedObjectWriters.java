package net.bananashelp20.forgermod.registryInterpreter.interpreter.interpreterHelperClasses;

import net.bananashelp20.forgermod.registryInterpreter.interpreter.RegistryInterpreter;
import net.bananashelp20.forgermod.registryInterpreter.interpreter.RegistryInterpreterHelperMethods;
import net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.recipes.special.InterpretedCustomRecipe;

import java.io.File;
import java.util.ArrayList;

import static net.bananashelp20.forgermod.registryInterpreter.interpreter.RegistryInterpreter.*;
import static net.bananashelp20.forgermod.registryInterpreter.interpreter.RegistryInterpreterHelperMethods.*;
import static net.bananashelp20.forgermod.registryInterpreter.interpreter.interpreterHelperClasses.FileIO.*;
import static net.bananashelp20.forgermod.registryInterpreter.interpreter.interpreterHelperClasses.InterpretedObjectGetters.*;

public class InterpretedObjectWriters {
    public static void writeItemModels() {
        String prevContent = getWholeFileContentTillGenerate(modItemModelProviderFile, "//!GENERATE MODELS");
        String newStuff = "";

        for (int i = 0; i < items.size(); i++) {
            newStuff += items.get(i).getItemModel() + "\n";
        }

        write(prevContent + "\n" + newStuff + "    }\n}", modItemModelProviderFile);
    }

    public static void writeItemTags() {
        String prevContent = getWholeFileContentTillGenerate(modItemTagProviderFile, "//!GENERATE ITEM_TAGS");
        String newStuff = "";
        ArrayList<ArrayList<String>> differentTags = getDifferentItemTags();
        ArrayList<String> tagContent;

        for (int i = 0; i < differentTags.size(); i++) {
            newStuff += "\n        tag(" + differentTags.get(i).getFirst() + differentTags.get(i).get(1) + ")\n        ;\n";
        }

        write(prevContent + "\n" + newStuff + "    }\n}", modItemTagProviderFile);
        tagContent = getContentFromFileAsListNonFormated(modItemTagProviderFile);
        ArrayList<ArrayList<String>> tagsForEachItem = RegistryInterpreter.getAllEnchantmentablesFromOptionalParameter(getContentFromFileAsList(itemFile, "#"));
        for (int i = 0; i < tagsForEachItem.size(); i++) {
            for (int j = 1; j < tagsForEachItem.get(i).size(); j++) {
                for (int k = 0; k < tagContent.size(); k++) {
                    if (tagContent.get(k).toUpperCase().contains(tagsForEachItem.get(i).get(j).toUpperCase())) {
                        for (int l = 0; l < items.size(); l++) {
                            if (items.get(l).getItemTagCode().toUpperCase().contains(tagsForEachItem.get(i).getFirst().toUpperCase())) {
                                tagContent.add(k+1, items.get(l).getItemTagCode());
                            }
                        }
                    }
                }
            }
        }
        write(getListAsString(tagContent), modItemTagProviderFile);
    }

    public static void writeItemsToTabRegistry() {
        ArrayList<String> prevContent = getContentFromFileAsList(modRegistry, "");

        for (int j = 0; j < items.size(); j++) {
            for (int k = 0; k < prevContent.size(); k++) {
                if (prevContent.get(k).toUpperCase().contains(items.get(j).getCreativeTab().toUpperCase()) && !prevContent.get(k).toUpperCase().contains("DisplayItem".toUpperCase()) && prevContent.get(k).toUpperCase().contains("ItemLike[]".toUpperCase())) {
                    prevContent.add(k+2, items.get(j).getCreativeTabCode());
                }
            }
        }
        write(getListAsString(prevContent), modRegistry);
    }

    public static void writeModTags() {
        String prevContent = getWholeFileContentTillGenerate(modTagsFile, "//!GENERATE MOD_TAGS");
        String newStuff = "";

        for (int i = 0; i < toolTiers.size(); i++) {
            newStuff += toolTiers.get(i).getTags() + "\n";
        }

        prevContent += "\n" + newStuff + "    }\n    public static class Items {\n" +
                "        private static TagKey<Item> createTag(String name) {\n" +
                "            return ItemTags.create(ResourceLocation.fromNamespaceAndPath(ForgerMod.MOD_ID, name));\n" +
                "        }\n" +
                "        //!GENERATE MOD_ITEM_TAGS\n" +
                "    }";
        write(prevContent + "\n}", modTagsFile);
        //writeModTagsForItems(); //moch i erst wenns soweit is
    }

    public static void writeRegistryMethods() {
        String prevContent = getWholeFileContentTillGenerate(modRegistry, "//!GENERATE METHODS");
        String newStuff = "";

        for (int i = 0; i < creativeTabs.size(); i++) {
            newStuff += creativeTabs.get(i).getRegistryMethods() + "\n";
        }

        write(prevContent + "\n" + newStuff + "}", modRegistry);
    }

    public static void writeBlockLoottables() {
        String prevContent = getWholeFileContentTillGenerate(modBlockLootTableProviderFile, "//!GENERATE DROPS");
        String newStuff = "";

        for (int i = 0; i < blocks.size(); i++) {
            newStuff += blocks.get(i).getLoottable() + "\n";
        }

        write(prevContent + "\n" + newStuff + "    }\n}", modBlockLootTableProviderFile);
    }

    public static void writeBlockTags() {
        String prevContent = getWholeFileContentTillGenerate(modBlockTagProviderFile, "//!GENERATE BLOCK_TAGS");
        String newStuff = "";
        ArrayList<ArrayList<String>> differentTags = getDifferentBlockTags();
        ArrayList<String> tagContent;

        for (int i = 0; i < differentTags.size(); i++) {
            newStuff += "        tag(" + differentTags.get(i).getFirst() + (differentTags.get(i).get(2).equals("type") ? "NEEDS_" + differentTags.get(i).get(1).toUpperCase() + "_TOOL" : "MINEABLE_WITH_" + differentTags.get(i).get(1).toUpperCase()) + ")\n        ;\n";
        }

        write(prevContent + "\n" + newStuff + "    }\n}", modBlockTagProviderFile);
        tagContent = getContentFromFileAsListNonFormated(modBlockTagProviderFile);
        for (int i = 0; i < tagContent.size(); i++) {
            if (tagContent.get(i).contains(" tag(") && !tagContent.get(i).contains("INCORRECT")) {
                if (tagContent.get(i).contains("MINEABLE")) {
                    for (int j = 0; j < blocks.size(); j++) {
                        if (tagContent.get(i).toUpperCase().contains(blocks.get(j).getTagTool().toUpperCase())) {
                            tagContent.add(i+1, blocks.get(j).getTag());
                        }
                    }
                } else {
                    for (int j = 0; j < blocks.size(); j++) {
                        if (tagContent.get(i).toUpperCase().contains(blocks.get(j).getTagType().toUpperCase())) {
                            tagContent.add(i+1, blocks.get(j).getTag());
                        }
                    }
                }
            }
        }
        write(getListAsString(tagContent), modBlockTagProviderFile);
    }

    public static void writeBlockStates() {
        String prevContent = getWholeFileContentTillGenerate(modBlockStateProviderFile, "//!GENERATE MODELS");
        String newStuff = "";

        for (int i = 0; i < blocks.size(); i++) {
            newStuff += blocks.get(i).getBlockState() + (blocks.get(i).getBlockState().isEmpty() ? "" : "\n");
        }

        write(prevContent + "\n" + newStuff + "    }\n}", modBlockStateProviderFile);
    }

    public static void writeCreativeTabCode(boolean allowed) {
        String prevContent = getWholeFileContentTillGenerate(modCreativeTabsFile, "//!GENERATE");
        String newStuff = "";

        for (int i = 0; i < creativeTabs.size(); i++) {
            newStuff += creativeTabs.get(i).toString() + "\n";
        }

        prevContent += "\n" + newStuff + "}";

        if (!allowed) return;
        write(prevContent, modCreativeTabsFile);
        writeRegistryMethods();
    }

    public static void writeBlockCode(boolean allowed) {
        String prevContent = getWholeFileContentTillGenerate(modBlocksFile, "//!GENERATE");
        String newStuff = "";

        for (int i = 0; i < blocks.size(); i++) {
            newStuff += blocks.get(i).toString() + "\n";
        }

        prevContent += "\n" + newStuff + "}";

        if (!allowed) return;
        write(prevContent, modBlocksFile);
        writeBlockLoottables();
        writeBlockTags(); //do liegts problem, wo denn a sunst...
        writeBlockStates();
//        writeOreCode();
    }

    public static void writeItemCode(boolean allowed) {
        String prevContent = getWholeFileContentTillGenerate(modItemsFile, "//!GENERATE");
        String newStuff = "";

        for (int i = 0; i < items.size(); i++) {
            newStuff += items.get(i).toString() + "\n";
        }

        prevContent += "\n" + newStuff + "}";

        if (!allowed) return;
        write(prevContent, modItemsFile);
        writeItemModels();
        writeItemsToTabRegistry();
        writeItemTags(); //do ligts problem
    }

    public static void writeRecipeCode(boolean allowed) {
        String prevContent = getWholeFileContentTillGenerate(modRecipeProviderFile, "//!GENERATE");
        String newStuff = "";
        ArrayList<Integer> relevantObjects = new ArrayList<>();
        ArrayList<File> relevantClasses = new ArrayList<>();
        for (int i = 0; i < recipes.size(); i++) {
            if (!(recipes.get(i) instanceof InterpretedCustomRecipe)) {
                newStuff += recipes.get(i).toString() + "\n";
            } else {
                relevantObjects.add(i);
                relevantClasses.add(((InterpretedCustomRecipe) recipes.get(i)).getRecipeClass());
            }
        }

        prevContent += "\n" + newStuff + "    }\n}";
        if (!allowed) return;
        write(prevContent, modRecipeProviderFile);
    }

    public static void writeToolTierCode(boolean allowed) {
        String prevContent = getWholeFileContentTillGenerate(modToolTiersFile, "//!GENERATE");
        String newStuff = "";
        for (int i = 0; i < toolTiers.size(); i++) {
            newStuff += toolTiers.get(i).getTierCode();
        }

        prevContent += "\n" + newStuff + "}";

        if (!allowed) return;
        write(prevContent, modToolTiersFile);
        writeModTags(); //do liegt a a problem, wo a sunst...
    }
}
