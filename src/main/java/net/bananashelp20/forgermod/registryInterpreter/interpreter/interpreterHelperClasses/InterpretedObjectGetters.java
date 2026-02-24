package net.bananashelp20.forgermod.registryInterpreter.interpreter.interpreterHelperClasses;

import net.bananashelp20.forgermod.registryInterpreter.interpreter.ListObjects.TitledList;
import net.bananashelp20.forgermod.registryInterpreter.interpreter.ListObjects.TitledTable;
import net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.blocks.InterpretedBlock;
import net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.blocks.InterpretedOre;
import net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.blocks.special.InterpretedComplexBlock;
import net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.blocks.special.InterpretedSimpleBlock;
import net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.blocks.special.InterpretedSpecialBlock;
import net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.blocks.special.ores.InterpretedInterdimensionalOreBlock;
import net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.blocks.special.ores.InterpretedInterdimensionalSpecialOreBlock;
import net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.creativeTabs.InterpretedCreativeTab;
import net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.items.InterpretedItem;
import net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.items.special.*;
import net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.recipes.InterpretedRecipe;
import net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.recipes.special.*;
import net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.toolTiers.InterpretedToolTier;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static net.bananashelp20.forgermod.registryInterpreter.interpreter.RegistryInterpreter.*;
import static net.bananashelp20.forgermod.registryInterpreter.interpreter.RegistryInterpreterHelperMethods.*;
import static net.bananashelp20.forgermod.registryInterpreter.interpreter.ListObjects.TitledTable.getArraylistFromBrackets;
import static net.bananashelp20.forgermod.registryInterpreter.interpreter.interpreterHelperClasses.FileIO.getContentFromFileAsList;
import static net.bananashelp20.forgermod.registryInterpreter.interpreter.interpreterHelperClasses.FileIO.getContentInBrackets;

public class InterpretedObjectGetters {
    public static ArrayList<InterpretedBlock> getAllBlocks() {
        ArrayList<InterpretedBlock> interpretedBlocks = new ArrayList<>();
        ArrayList<String> blockText = getContentFromFileAsList(blockFile, "#");
        ArrayList<ArrayList<String>> blockStringObjects = new ArrayList<>();
        String dropOtherItem;
        boolean dropOther;
        int indexExpander;
        InterpretedBlock blockToAdd;
        int ctr = -1;
        for (int i = 0; i < blockText.size(); i++) {
            if (blockText.get(i).contains("{") && !blockText.get(i).toUpperCase().contains("ORE")) {
                blockStringObjects.add(new ArrayList<>());
                ctr++;
                for (int j = i; j < blockText.size() && !blockText.get(j).contains("}"); j++) {
                    blockStringObjects.get(ctr).add(blockText.get(j));
                    i = j;
                }
            }
        }

//        interpretedBlocks.add(new InterpretedSimpleBlock("name", "p", "drop", "", "b", "type", "tab", "tab"));
//        interpretedBlocks.add(new InterpretedSimpleBlock("name2", "p", "drop", "", "b", "type", "tab", "tab"));

        for (int i = 0; i < blockStringObjects.size(); i++) {
            dropOther = false;
            indexExpander = 0;
            dropOtherItem = "";
            blockToAdd = null;
            if (blockStringObjects.get(i).getFirst().contains("{simple")) {
                if (blockStringObjects.get(i).get(4).contains("?")) {
                    dropOther = true;
                    indexExpander++;
                    dropOtherItem = blockStringObjects.get(i).get(4).substring(1);
                }
                blockToAdd = new InterpretedSimpleBlock(blockStringObjects.get(i).get(1), blockStringObjects.get(i).get(2), blockStringObjects.get(i).get(3), dropOtherItem, blockStringObjects.get(i).get(4 + indexExpander), blockStringObjects.get(i).get(5 + indexExpander), blockStringObjects.get(i).get(6 + indexExpander), blockStringObjects.get(i).get(7 + indexExpander));

            } else if (blockStringObjects.get(i).getFirst().contains("{special")) {
                if (blockStringObjects.get(i).get(5).contains("?")) {
                    dropOther = true;
                    indexExpander++;
                    dropOtherItem = blockStringObjects.get(i).get(4).substring(1);
                }
                blockToAdd = new InterpretedSpecialBlock(blockStringObjects.get(i).get(1), blockStringObjects.get(i).get(2),
                        blockStringObjects.get(i).get(3), blockStringObjects.get(i).get(4),
                        dropOtherItem, blockStringObjects.get(i).get(5 + indexExpander),
                        blockStringObjects.get(i).get(6 + indexExpander), blockStringObjects.get(i).get(7 + indexExpander),
                        blockStringObjects.get(i).get(8 + indexExpander));

            } else if (blockStringObjects.get(i).getFirst().contains("{complex")) {
                if (blockStringObjects.get(i).get(4).contains("?")) {
                    dropOther = true;
                    indexExpander++;
                    dropOtherItem = blockStringObjects.get(i).get(4).substring(1);
                }
                blockToAdd = new InterpretedComplexBlock(blockStringObjects.get(i).get(1), blockStringObjects.get(i).get(2),
                        blockStringObjects.get(i).get(3),
                        dropOtherItem, blockStringObjects.get(i).get(4 + indexExpander),
                        blockStringObjects.get(i).get(5 + indexExpander), blockStringObjects.get(i).get(6 + indexExpander), blockStringObjects.get(i).get(7 + indexExpander));
            }
            interpretedBlocks.add(blockToAdd);
        }

        interpretedBlocks.addAll(temporaryOreBlocks);
        return interpretedBlocks;
    }

    public static ArrayList<InterpretedOre> getAllOres() {
        ArrayList<InterpretedOre> interpretedOres = new ArrayList<>();
        ArrayList<InterpretedBlock> innerInterpretedBlocks = new ArrayList<>();
        ArrayList<String> blockText = getContentFromFileAsList(blockFile, "#"); //comment must be declared as # since it wouldn't work otherwise (with #// declared or //#)
        ArrayList<ArrayList<String>> oreStringObjects = new ArrayList<>();
        ArrayList<ArrayList<ArrayList<String>>> innerBlockStringObjects = new ArrayList<>(); //arraylist for each Ore / arraylist for each Block / property list
        String dropOtherItem;
        boolean dropOther;
        int indexExtender;
        InterpretedOre oreToAdd;
        InterpretedBlock innerBlockToAdd;
        int ctr = -1;

        for (int i = 0; i < blockText.size(); i++) {
            if (blockText.get(i).contains("{") && !blockText.get(i).contains("@")) {
                oreStringObjects.add(new ArrayList<>());
                innerBlockStringObjects.add(new ArrayList<>());
                ctr++;
                for (int j = i; j < blockText.size() && !(blockText.get(j).contains("}") && !blockText.get(j).contains("@")); j++) {
                    if (blockText.get(i).contains("@") && blockText.get(i).contains("{")) {
                        innerBlockStringObjects.get(ctr).add(new ArrayList<>());
                        for (int k = i; j < blockText.size() && !(blockText.get(j).contains("@}")); j++) {
                            innerBlockStringObjects.get(ctr).get(k).add(blockText.get(j));
                            j = k;
                        }
                        j++;
                    }
                    oreStringObjects.get(ctr).add(blockText.get(j));
                    i = j;
                }
            }
        }

        for (int i = 0; i < oreStringObjects.size(); i++) {
            for (int j = 0; j < innerBlockStringObjects.get(i).size(); j++) {
                dropOther = false;
                indexExtender = 0;
                dropOtherItem = "";
                innerBlockToAdd = null;
                if (innerBlockStringObjects.get(i).get(j).getFirst().contains("@simple")) {
                    if (innerBlockStringObjects.get(i).get(j).get(4).contains("?")) {
                        dropOther = true;
                        indexExtender++;
                        dropOtherItem = innerBlockStringObjects.get(i).get(j).get(4).substring(1);
                    }
                    innerBlockToAdd = new InterpretedSimpleBlock(innerBlockStringObjects.get(i).get(j).get(1), innerBlockStringObjects.get(i).get(j).get(2), innerBlockStringObjects.get(i).get(j).get(3), dropOtherItem, innerBlockStringObjects.get(i).get(j).get(4 + indexExtender), innerBlockStringObjects.get(i).get(j).get(5 + indexExtender), innerBlockStringObjects.get(i).get(j).get(6 + indexExtender), innerBlockStringObjects.get(i).get(j).get(7 + indexExtender));

                } else if (innerBlockStringObjects.get(i).get(j).getFirst().contains("@special")) {
                    if (innerBlockStringObjects.get(i).get(j).get(5).contains("?")) {
                        dropOther = true;
                        indexExtender++;
                        dropOtherItem = innerBlockStringObjects.get(i).get(j).get(4).substring(1);
                    }
                    innerBlockToAdd = new InterpretedSpecialBlock(innerBlockStringObjects.get(i).get(j).get(1), innerBlockStringObjects.get(i).get(j).get(2),
                            innerBlockStringObjects.get(i).get(j).get(3), innerBlockStringObjects.get(i).get(j).get(4),
                            dropOtherItem, innerBlockStringObjects.get(i).get(j).get(5 + indexExtender),
                            innerBlockStringObjects.get(i).get(j).get(6 + indexExtender), innerBlockStringObjects.get(i).get(j).get(7 + indexExtender),
                            innerBlockStringObjects.get(i).get(j).get(8 + indexExtender));

                } else if (innerBlockStringObjects.get(i).get(j).getFirst().contains("@complex")) {
                    if (innerBlockStringObjects.get(i).get(4).contains("?")) {
                        dropOther = true;
                        indexExtender++;
                        dropOtherItem = innerBlockStringObjects.get(i).get(j).get(4).substring(1);
                    }
                    innerBlockToAdd = new InterpretedComplexBlock(innerBlockStringObjects.get(i).get(j).get(1), innerBlockStringObjects.get(i).get(j).get(2),
                            innerBlockStringObjects.get(i).get(j).get(3),
                            dropOtherItem, innerBlockStringObjects.get(i).get(j).get(4 + indexExtender),
                            innerBlockStringObjects.get(i).get(j).get(5 + indexExtender), innerBlockStringObjects.get(i).get(j).get(6 + indexExtender), innerBlockStringObjects.get(i).get(j).get(7 + indexExtender));
                }
                innerInterpretedBlocks.add(innerBlockToAdd);
            }

            //getArraylistFromBrackets returns an Arraylist<Arraylist<String>> 0th argument is the wanted arraylist, 1st argument is a stringified indexExtender to keep track of the current position in the stringObjectList
            // and not get bamboozled and using the contend of the same brackets again and again //NA des moch i ned so: jetzt: letztes Argument is indexExpander
            oreToAdd = null;
            TitledList<String> blockNames = new TitledList<>("blockNames", "String");
            blockNames.parseFromStringList(oreStringObjects.get(i), 2);
            TitledList<String> dimensions = new TitledList<>("dimensions", "String");
            dimensions.parseFromStringList(oreStringObjects.get(i), blockNames.lastIndexExpander);
            TitledList<String> generationSteps = new TitledList<>("generationSteps", "String");
            generationSteps.parseFromStringList(oreStringObjects.get(i), dimensions.lastIndexExpander);
            TitledTable<String> ruleTests = new TitledTable<>((ArrayList<String>) Arrays.asList(new String[]{"replace-variable-name", "matchtest-class", "replaceable-block"}), (ArrayList<String>) Arrays.asList(new String[]{"String", "String", "String"}));
            ruleTests.parseFromStringList(oreStringObjects.get(i), generationSteps.lastIndexExpander);
            TitledList<Integer> oreSizesForEachDimension = new TitledList<>("oreSizesForEachDimension", "String");
            oreSizesForEachDimension.parseFromStringList(oreStringObjects.get(i), ruleTests.lastIndexExpander);
            TitledTable<String> placements = new TitledTable<>((ArrayList<String>) Arrays.asList(new String[]{"chance-method-name", "count", "placement-class", "placement-method", "anchor", "absoluteness", "height", "second-anchor", "second-absoluteness", "second-height"}), (ArrayList<String>) Arrays.asList(new String[]{"String", "int", "String", "String", "String", "String", "int", "String", "String", "int"}));
            placements.parseFromStringList(oreStringObjects.get(i), oreSizesForEachDimension.lastIndexExpander);

            if (oreStringObjects.get(i).getFirst().contains("{simpleOre")) {
                oreToAdd = new InterpretedInterdimensionalOreBlock(oreStringObjects.get(i).get(1), blockNames, dimensions, generationSteps, ruleTests, oreSizesForEachDimension, placements);
            } else if (oreStringObjects.get(i).getFirst().contains("{specialOre")) {
                oreToAdd = new InterpretedInterdimensionalSpecialOreBlock();
            }

            interpretedOres.add(oreToAdd);
        }

        temporaryOreBlocks = innerInterpretedBlocks;
        return interpretedOres;
    }

    public static ArrayList<InterpretedItem> getAllItems() {
        ArrayList<InterpretedItem> items = new ArrayList<>();
        ArrayList<String> itemText = getContentFromFileAsList(itemFile, "#");
        ArrayList<ArrayList<String>> itemStringObjects = new ArrayList<>();
        ArrayList<String> variants;
        ArrayList<String> properties;
        int ctr = -1;
        for (int i = 0; i < itemText.size(); i++) {
            if (itemText.get(i).contains("{")) {
                itemStringObjects.add(new ArrayList<>());
                ctr++;
                for (int j = i; j < itemText.size() && !itemText.get(j).contains("}"); j++) {
                    itemStringObjects.get(ctr).add(itemText.get(j));
                    i = j;
                }
            }
        }

        for (int j = 0; j < itemStringObjects.size(); j++) {
            if (itemStringObjects.get(j).getFirst().contains("{simpleItem")) {
                items.add(new InterpretedSimpleItem(itemStringObjects.get(j).get(1), itemStringObjects.get(j).get(2), itemStringObjects.get(j).get(3)));
            } else if (itemStringObjects.get(j).getFirst().contains("{specialItem")) {
                if (!itemStringObjects.get(j).get(3).contains("?[E") && itemStringObjects.get(j).get(3).contains("?")) {
                    items.add(new InterpretedSpecialItem(itemStringObjects.get(j).get(1), itemStringObjects.get(j).get(2), itemStringObjects.get(j).get(3).substring(1).trim(), itemStringObjects.get(j).get(4)));
                } else {
                    items.add(new InterpretedSpecialItem(itemStringObjects.get(j).get(1), itemStringObjects.get(j).get(2), itemStringObjects.get(j).get(3)));
                }
            } else if (itemStringObjects.get(j).getFirst().contains("{simpleSword")) {
                items.add(new InterpretedSwordItem(itemStringObjects.get(j).get(1), itemStringObjects.get(j).get(2), itemStringObjects.get(j).get(3), itemStringObjects.get(j).get(4), itemStringObjects.get(j).get(5), itemStringObjects.get(j).get(6)));
            } else if (itemStringObjects.get(j).getFirst().contains("{specialSword")) {
                properties = new ArrayList<>();
                for (int k = 0; k < itemStringObjects.get(j).size() && !itemStringObjects.get(j).get(k).contains(")"); k++) {
                    if (itemStringObjects.get(j).get(k).contains("[") && !itemStringObjects.get(j).get(k).contains("?[E")) {
                        for (int l = k+1; l < itemStringObjects.get(j).size() && !itemStringObjects.get(j).get(l).contains("]"); l++) {
                            properties.add(itemStringObjects.get(j).get(l));
                        }
                    }
                }
                items.add(new InterpretedSpecialSwordItem(itemStringObjects.get(j).get(1), itemStringObjects.get(j).get(2), itemStringObjects.get(j).get(3), itemStringObjects.get(j).get(4), properties, itemStringObjects.get(j).get(5), itemStringObjects.get(j).get(6)));
            } else if (itemStringObjects.get(j).getFirst().contains("{upgradableSword") || itemStringObjects.get(j).getFirst().contains("{upgradeableSword")) {
                variants = new ArrayList<>();
                properties = new ArrayList<>();
                properties.add(itemStringObjects.get(j).get(1));
                properties.add(itemStringObjects.get(j).get(2));
                properties.add(itemStringObjects.get(j).get(3));
                properties.add(itemStringObjects.get(j).get(4));
                properties.add(itemStringObjects.get(j).get(5));
                ctr = 7;
                for (int k = 7; k < itemStringObjects.get(j).size() && !itemStringObjects.get(j).get(k).contains("]"); k++) {
                    variants.add(itemStringObjects.get(j).get(k));
                    ctr++;
                }
                properties.add(itemStringObjects.get(j).get(ctr));
                items.add(new InterpretedItemWithUpgradedVariations(properties.getFirst(), properties.get(1), properties.get(2), properties.get(3), properties.get(4), variants, properties.get(5)));
            }
        }
        return items;
    }

    public static ArrayList<InterpretedCreativeTab> getAllCreativeTabs() {
        ArrayList<InterpretedCreativeTab> interpretedToolTiers = new ArrayList<>();
        ArrayList<String> toolTierText = getContentFromFileAsList(creativeTabFile, "#");
        ArrayList<ArrayList<String>> toolTierStringObjects = new ArrayList<>();
        InterpretedCreativeTab toolTierToAdd = null;
        int ctr = -1;

        for (int i = 0; i < toolTierText.size(); i++) {
            if (toolTierText.get(i).contains("{")) {
                toolTierStringObjects.add(new ArrayList<>());
                ctr++;
                for (int j = i; j < toolTierText.size() && !toolTierText.get(j).contains("}"); j++) {
                    toolTierStringObjects.get(ctr).add(toolTierText.get(j));
                    i = j;
                }
            }
        }

        for (int i = 0; i < toolTierStringObjects.size(); i++) {
            if (toolTierStringObjects.get(i).getFirst().contains("{simpleTab")) {
                toolTierToAdd = new InterpretedCreativeTab(toolTierStringObjects.get(i).get(1), toolTierStringObjects.get(i).get(2), toolTierStringObjects.get(i).get(3));
            }
            interpretedToolTiers.add(toolTierToAdd);
        }

        return interpretedToolTiers;
    }

    public static ArrayList<InterpretedToolTier> getAllToolTiers() {
        ArrayList<InterpretedToolTier> interpretedToolTiers = new ArrayList<>();
        ArrayList<String> toolTierText = getContentFromFileAsList(toolTierFile, "#");
        ArrayList<ArrayList<String>> toolTierStringObjects = new ArrayList<>();
        InterpretedToolTier toolTierToAdd = null;
        int ctr = -1;

        for (int i = 0; i < toolTierText.size(); i++) {
            if (toolTierText.get(i).contains("{")) {
                toolTierStringObjects.add(new ArrayList<>());
                ctr++;
                for (int j = i; j < toolTierText.size() && !toolTierText.get(j).contains("}"); j++) {
                    toolTierStringObjects.get(ctr).add(toolTierText.get(j));
                    i = j;
                }
            }
        }

        for (int i = 0; i < toolTierStringObjects.size(); i++) {
            if (toolTierStringObjects.get(i).getFirst().contains("{normal")) {
                toolTierToAdd = new InterpretedToolTier(toolTierStringObjects.get(i).get(1), toolTierStringObjects.get(i).get(2), toolTierStringObjects.get(i).get(3));
            }
            interpretedToolTiers.add(toolTierToAdd);
        }

        return interpretedToolTiers;
    }

    public static ArrayList<InterpretedRecipe> getAllRecipes() {
        ArrayList<InterpretedRecipe> interpretedRecipes = new ArrayList<>();
        ArrayList<String> recipeText = getContentFromFileAsList(recipeFile, "#");
        ArrayList<String> inputItems;
        ArrayList<ArrayList<String>> recipeStringObjects = new ArrayList<>();
        InterpretedRecipe recipeToAdd;
        int ctr = -1;
        String[] pattern;
        String[] temp;
        HashMap<Character, String> patternMeaning;

        for (int i = 0; i < recipeText.size(); i++) {
            if (recipeText.get(i).contains("{")) {
                recipeStringObjects.add(new ArrayList<>());
                ctr++;
                if (recipeText.get(i).contains("custom") && recipeText.get(i+2).contains("!GEMSTONE_INFUSION_RECIPE")) {
                    i++;
                    gemstoneInfusionRecipe(recipeStringObjects, recipeText, ctr, i, "ruby");
                    recipeStringObjects.add(new ArrayList<>());
                    gemstoneInfusionRecipe(recipeStringObjects, recipeText, ++ctr, i, "amethyst");
                    recipeStringObjects.add(new ArrayList<>());
                    gemstoneInfusionRecipe(recipeStringObjects, recipeText, ++ctr, i, "amber");
                    recipeStringObjects.add(new ArrayList<>());
                    gemstoneInfusionRecipe(recipeStringObjects, recipeText, ++ctr, i, "jade");
                    i += 2;
                } else {
                    for (int j = i; j < recipeText.size() && !recipeText.get(j).contains("}"); j++) {
                        recipeStringObjects.get(ctr).add(recipeText.get(j));
                        i = j;
                    }
                }
            }
        }

        for (int i = 0; i < recipeStringObjects.size(); i++) {
            inputItems = new ArrayList<>();
            recipeToAdd = new InterpretedRecipe(new ArrayList<>());
            patternMeaning = new HashMap<>();
            pattern = new String[3];
            if (recipeStringObjects.get(i).getFirst().contains("{smelting")) {
                inputItems.addAll(getContentInBrackets(i, 4, recipeStringObjects));
                recipeToAdd = new InterpretedBlastingOrSmeltingRecipe(recipeStringObjects.get(i).get(1), inputItems, recipeStringObjects.get(i).get(2), true, i, recipeStringObjects.get(i).get(3));
            } else if (recipeStringObjects.get(i).contains("{blasting")) {
                inputItems.addAll(getContentInBrackets(i, 4, recipeStringObjects));
                recipeToAdd = new InterpretedBlastingOrSmeltingRecipe(recipeStringObjects.get(i).get(1), inputItems, recipeStringObjects.get(i).get(2), false, i, recipeStringObjects.get(i).get(3));
            } else if (recipeStringObjects.get(i).contains("{both")) {
                inputItems.addAll(getContentInBrackets(i, 5, recipeStringObjects));
                recipeToAdd = new InterpretedBlastingOrSmeltingRecipe(true, recipeStringObjects.get(i).get(1), inputItems, recipeStringObjects.get(i).get(2), i, recipeStringObjects.get(i).get(3), recipeStringObjects.get(i).get(4));
            } else if (recipeStringObjects.get(i).contains("{shapeless")) {
                inputItems.addAll(getContentInBrackets(i, 5, recipeStringObjects));
                recipeToAdd = new InterpretedShapelessRecipe(inputItems, recipeStringObjects.get(i).get(1), recipeStringObjects.get(i).get(2), recipeStringObjects.get(i).get(3), Integer.parseInt(recipeStringObjects.get(i).get(4)), i);
            } else if (recipeStringObjects.get(i).contains("{shaped")) {
                pattern[0] = recipeStringObjects.get(i).get(2);
                pattern[1] = recipeStringObjects.get(i).get(3);
                pattern[2] = recipeStringObjects.get(i).get(4);
                int j = 0;
                for (j = 5; recipeStringObjects.get(i).get(j).contains("->"); j++) {
                    temp = recipeStringObjects.get(i).get(j).split("->");
                    patternMeaning.put(temp[0].charAt(0), temp[1].trim());
                }
                recipeToAdd = new InterpretedShapedRecipe(recipeStringObjects.get(i).get(1), pattern, patternMeaning,recipeStringObjects.get(i).get(j), recipeStringObjects.get(i).get(++j), i);
            } else if (recipeStringObjects.get(i).contains("{custom")) {
                inputItems.addAll(getContentInBrackets(i, 2, recipeStringObjects));
                int x = 2;
                while (!recipeStringObjects.get(i).get(x).contains("]")) {
                    x++;
                }
                ArrayList<String> output = new ArrayList<>(getContentInBrackets(i, x + 1, recipeStringObjects));
                recipeToAdd = new InterpretedCustomRecipe(recipeStringObjects.get(i).get(1), inputItems, output, i);
            } else if (recipeStringObjects.get(i).contains("{conversion")) {
                pattern = recipeStringObjects.get(i).get(2).split(" ");
                recipeToAdd = new InterpretedConversionRecipe(recipeStringObjects.get(i).get(1), pattern[0] + " " + pattern[1], pattern[2], pattern[3] + " " + pattern[4], i);
            }
            interpretedRecipes.add(recipeToAdd);
        }

        return interpretedRecipes;
    }

    public static ArrayList<ArrayList<String>> getDifferentBlockTags() {
        ArrayList<ArrayList<String>> differentTags = new ArrayList<>();
        ArrayList<String> tag;

        for (int i = 0; i < blocks.size(); i++) {
            tag = new ArrayList<>();
            if (blocks.get(i).getTagTool().toUpperCase().contains("shovel".toUpperCase())
                    || blocks.get(i).getTagTool().toUpperCase().contains("axe".toUpperCase())
                    || blocks.get(i).getTagTool().toUpperCase().contains("pickaxe".toUpperCase())
                    || blocks.get(i).getTagTool().toUpperCase().contains("hoe".toUpperCase())) {
                tag.add("BlockTags.");
            } else if (!blocks.get(i).getTagTool().isEmpty()) {
                tag.add("ModTags.Blocks.");
            }
            if (!blocks.get(i).getTagTool().isEmpty()) {
                tag.add(blocks.get(i).getTagTool());
                tag.add("tool");
                differentTags.add(tag);
            }
        }

        for (int i = 0; i < blocks.size(); i++) {
            tag = new ArrayList<>();
            if (blocks.get(i).getTagType().toUpperCase().contains("stone".toUpperCase())
                    || blocks.get(i).getTagType().toUpperCase().contains("diamond".toUpperCase())
                    || blocks.get(i).getTagType().toUpperCase().contains("iron".toUpperCase())) {
                tag.add("BlockTags.");
            } else if (!blocks.get(i).getTagType().isEmpty()) {
                tag.add("ModTags.Blocks.");
            }
            if (!blocks.get(i).getTagType().isEmpty()) {
                tag.add(blocks.get(i).getTagType());
                tag.add("type");
                differentTags.add(tag);
            }
        }
        removeDuplicatesFromTagList(differentTags);
        return differentTags;
    }

    public static ArrayList<ArrayList<String>> getDifferentItemTags() {
        ArrayList<ArrayList<String>> differentTags = new ArrayList<>(); //alles tags
        ArrayList<String> tag; //ein tag objekt (tag prefix, und tag selbst)

        for (int i = 0; i < items.size(); i++) {
            for (int j = 1; j < items.get(i).getTagsOfItem().size(); j++) {
                tag = new ArrayList<>();
                if (isPartOfItemTags(items.get(i).getTagsOfItem().get(j).toUpperCase(), true)) {
                    tag.add("ItemTags.");
                } else {
                    tag.add("ModTags.Items.");
                }
                tag.add(items.get(i).getTagsOfItem().get(j));
                differentTags.add(tag);
            }
        }
        removeDuplicatesFromTagList(differentTags);
        return differentTags;
    }

}
