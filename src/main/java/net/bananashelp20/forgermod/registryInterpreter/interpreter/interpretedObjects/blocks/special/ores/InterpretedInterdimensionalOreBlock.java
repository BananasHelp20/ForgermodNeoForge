package net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.blocks.special.ores;

import net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.blocks.InterpretedBlock;
import net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.blocks.InterpretedOre;

import java.util.ArrayList;

public class InterpretedInterdimensionalOreBlock implements InterpretedBlock, InterpretedOre {
    //once for every configured dimension (overworld with deepslate and stone, nether with netherrack, and end with endstone)
    String blockProperties;
    ArrayList<String> blockNames; //e.g. nether_jade_ore, overworld_jade_ore, end_jade_ore
    String defaultName; //in CamelCase e.g. JadeOre
    String dropMethod;
    String dropsItem;
    String modelMethod;
    String creationMethod;
    String toolTag;
    String typeTag;
    String creativeTab;
    ArrayList<String> dimensions;
    ArrayList<String> generationSteps; //per default: Decoration.UNDERGROUND_ORES
    ArrayList<ArrayList<String>> RuleTests; //ruleTestVariableName, tagMatchTestType, test. e.g. stoneReplaceables, BlockMatchTest, stone_ore_replaceables
    ArrayList<ArrayList<String>> oreSizesForEachDimension;
    ArrayList<ArrayList<String>> placements; //in format for each dimension: triangle/uniform, from (as written int between -64 and 256), to (as written int between -64 and 256)

    public InterpretedInterdimensionalOreBlock(String defaultName, ArrayList<String> blockNames, String creationMethod,
                                               String blockProperties, String dropMethod, String dropsItem, String modelMethod,
                                               String toolTag, String typeTag, String creativeTab, ArrayList<String> dimensions,
                                               ArrayList<String> generationSteps, ArrayList<ArrayList<String>> ruleTests,
                                               ArrayList<ArrayList<String>> oreSizesForEachDimension, ArrayList<ArrayList<String>> placements) {
        this.blockProperties = blockProperties;
        this.blockNames = blockNames;
        this.defaultName = defaultName;
        this.dropMethod = dropMethod;
        this.dropsItem = dropsItem;
        this.modelMethod = modelMethod;
        this.creationMethod = creationMethod;
        this.toolTag = toolTag;
        this.typeTag = typeTag;
        this.creativeTab = creativeTab;
        this.dimensions = dimensions;
        this.generationSteps = generationSteps;
        this.RuleTests = ruleTests;
        this.oreSizesForEachDimension = oreSizesForEachDimension;
        this.placements = placements;
    }

    @Override
    public String getTag() {
        return "";
    }

    @Override
    public String getLoottable() {
        return "";
    }

    @Override
    public String getBlockState() {
        return "";
    }

    @Override
    public String getCreativeTab() {
        return "";
    }

    @Override
    public String getTagTool() {
        return "";
    }

    @Override
    public String getTagType() {
        return "";
    }

    @Override
    public String getPlacedFeatureRegistration() {
        return "";
    }

    @Override
    public String getPlacedFeatureInitialisation() {
        return "";
    }

    @Override
    public String getConfigurationList() {
        return "";
    }

    @Override
    public String getConfiguredFeatureRegistration() {
        return "";
    }

    @Override
    public String getConfiguredFeatureInitialisation() {
        return "";
    }

    @Override
    public String getConfiguredFeatureRuleTests() {
        return "";
    }

    @Override
    public String getBiomeModifierInitialisation() {
        return "";
    }

    @Override
    public String getBiomeModifierRegistration() {
        return "";
    }
}
