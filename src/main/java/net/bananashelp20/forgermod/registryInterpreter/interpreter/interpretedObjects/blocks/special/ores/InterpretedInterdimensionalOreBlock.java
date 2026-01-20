package net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.blocks.special.ores;

import net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.blocks.InterpretedBlock;
import net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.blocks.InterpretedOre;

import java.util.ArrayList;

public class InterpretedInterdimensionalOreBlock implements InterpretedOre {
    //once for every configured dimension (overworld with deepslate and stone, nether with netherrack, and end with endstone)
    ArrayList<String> blockNames; //e.g. nether_jade_ore, overworld_jade_ore, end_jade_ore
    String defaultName; //in CamelCase e.g. JadeOre
    ArrayList<String> dimensions;
    ArrayList<String> generationSteps; //per default: Decoration.UNDERGROUND_ORES
    ArrayList<ArrayList<String>> ruleTests; //ruleTestVariableName, tagMatchTestType, test. e.g. stoneReplaceables, BlockMatchTest, stone_ore_replaceables
    ArrayList<Integer> oreSizesForEachDimension;
    ArrayList<ArrayList<String>> placements; //in format for each dimension: triangle/uniform, from (as written int between -64 and 256), to (as written int between -64 and 256)

    public InterpretedInterdimensionalOreBlock(String defaultName, ArrayList<String> blockNames, ArrayList<String> dimensions, ArrayList<String> generationSteps, ArrayList<ArrayList<String>> ruleTests, ArrayList<Integer> oreSizesForEachDimension, ArrayList<ArrayList<String>> placements) {
        this.defaultName = defaultName;
        this.blockNames = blockNames;
        this.dimensions = dimensions;
        this.generationSteps = generationSteps;
        this.ruleTests = ruleTests;
        this.oreSizesForEachDimension = oreSizesForEachDimension;
        this.placements = placements;
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
