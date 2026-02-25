package net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.blocks.special.ores;

import net.bananashelp20.forgermod.registryInterpreter.interpreter.ListObjects.TitledList;
import net.bananashelp20.forgermod.registryInterpreter.interpreter.ListObjects.TitledTable;
import net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.blocks.InterpretedBlock;
import net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.blocks.InterpretedOre;

import java.util.ArrayList;

public class InterpretedInterdimensionalSpecialOreBlock implements InterpretedOre {
    //appears more than one time in one/two/all dimensions
    ArrayList<String> blockNames; //e.g. nether_jade_ore, overworld_jade_ore, end_jade_ore
    String defaultName; //in CamelCase e.g. JadeOre
    TitledList<String> dimensions;
    TitledList<String> generationSteps; //per default: Decoration.UNDERGROUND_ORES
    TitledTable<String> ruleTests; //ruleTestVariableName, tagMatchTestType, test. e.g. stoneReplaceables, BlockMatchTest, stone_ore_replaceables
    TitledList<Integer> oreSizesForEachDimension;
    TitledTable<String> placements; //in format for each dimension: triangle/uniform, from (as written int between -64 and 256), to (as written int between -64 and 256)
    ArrayList<InterpretedBlock> oreBlocks;

    public InterpretedInterdimensionalSpecialOreBlock(String defaultName, ArrayList<String> blockNames, TitledList<String> dimensions, TitledList<String> generationSteps, TitledTable<String> ruleTests, TitledList<Integer> oreSizesForEachDimension, TitledTable<String> placements, ArrayList<InterpretedBlock> oreBlocks) {
        this.blockNames = blockNames;
        this.defaultName = defaultName;
        this.dimensions = dimensions;
        this.generationSteps = generationSteps;
        this.ruleTests = ruleTests;
        this.oreSizesForEachDimension = oreSizesForEachDimension;
        this.placements = placements;
        this.oreBlocks = oreBlocks;
    }

    private ArrayList<String> placedKeys = getPlacedKeys(blockNames);
    private ArrayList<String> primaryKeys = getPrimaryKeys(blockNames);
    private ArrayList<String> addOre = getAddOres(blockNames);

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
