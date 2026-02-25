package net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.blocks.special.ores;

import net.bananashelp20.forgermod.registryInterpreter.interpreter.ListObjects.TitledList;
import net.bananashelp20.forgermod.registryInterpreter.interpreter.ListObjects.TitledTable;
import net.bananashelp20.forgermod.registryInterpreter.interpreter.interpretedObjects.blocks.InterpretedOre;

import java.util.ArrayList;

public class InterpretedInterdimensionalOreBlock implements InterpretedOre {
    //once for every configured dimension (overworld with deepslate and stone, nether with netherrack, and end with endstone)
    ArrayList<String> blockNames; //e.g. nether_jade_ore, overworld_jade_ore, end_jade_ore
    String defaultName; //in CamelCase e.g. JadeOre
    TitledList<String> dimensions;
    TitledList<String> generationSteps; //per default: Decoration.UNDERGROUND_ORES
    TitledTable<String> ruleTests; //ruleTestVariableName, tagMatchTestType, test. e.g. stoneReplaceables, BlockMatchTest, stone_ore_replaceables
    TitledList<Integer> oreSizesForEachDimension;
    TitledTable<String> placements; //in format for each dimension: triangle/uniform, from (as written int between -64 and 256), to (as written int between -64 and 256)

    public InterpretedInterdimensionalOreBlock(String defaultName, ArrayList<String> blockNames, TitledList<String> dimensions, TitledList<String> generationSteps, TitledTable<String> ruleTests, TitledList<Integer> oreSizesForEachDimension, TitledTable<String> placements) {
        this.defaultName = defaultName;
        this.blockNames = blockNames;
        this.dimensions = dimensions;
        this.generationSteps = generationSteps;
        this.ruleTests = ruleTests;
        this.oreSizesForEachDimension = oreSizesForEachDimension;
        this.placements = placements;
    }

    private ArrayList<String> placedKeys = getPlacedKeys(blockNames);
    private ArrayList<String> primaryKeys = getPrimaryKeys(blockNames);
    private ArrayList<String> addOres = getAddOres(blockNames);

    @Override
    public String getPlacedFeatureRegistration() {
        String s = "";
        for (int i = 0; i < blockNames.size(); i++) {
            s += "        register(context, " + placedKeys.get(i) + ", configuredFeatures.getOrThrow(ModConfiguredFeatures." + primaryKeys.get(i) + "),\n"
                    + "                ModOrePlacement.commonOrePlacement(2, HeightRangePlacement." + placements.getFromList(i, "placement-method") + "("
                    + placements.getFromList(i, "anchor") + "." + placements.getFromList(i, "absoluteness") + "(" + placements.getFromList(i, "height") + "), "
                    + placements.getFromList(i, "second-anchor") + "." + placements.getFromList(i, "second-absoluteness") + "(" + placements.getFromList(i, "second-height")
                    + "))));\n";
        }
        return s + "\n";
    }

    @Override
    public String getPlacedFeatureInitialisation() {
        String s = "";
        for (int i = 0; i < blockNames.size(); i++) {
            s += "    public static final ResourceKey<PlacedFeature> " + placedKeys.get(i) + " = registerKey(\"" + placedKeys.get(i).substring(0, placedKeys.get(i).length()-4).toLowerCase() + "\");\n";
        }
        return s + "\n";
    }

    @Override
    public String getConfigurationList() {
        String s = "";
        if (dimensions.contains("overworld"))
            s += "        List<OreConfiguration.TargetBlockState> overworld" + defaultName + "s = List.of(\n" +
                    "                OreConfiguration.target(" + ruleTests.getFromList(0, "replace-variable-name") + ", ModBlocks." + blockNames.getFirst().toUpperCase() + ".get().defaultBlockState()),\n" +
                    "                OreConfiguration.target(" + ruleTests.getFromList(1, "replace-variable-name") + ", ModBlocks." + blockNames.get(1).toUpperCase() + ".get().defaultBlockState())\n" +
                    "        );\n\n";
        return s;
    }

    @Override
    public String getConfiguredFeatureRegistration() {
        String s = "";
        if (dimensions.contains("overworld")) {
            s += "        register(context, " + primaryKeys.getFirst() + ", Feature.ORE, new OreConfiguration(overworld" + defaultName + "s, " + oreSizesForEachDimension.get(0) + "));\n";
        }
        for (int i = (dimensions.contains("overworld") ? 2 : 0); i < blockNames.size(); i++) {
            s += "        register(context, " + primaryKeys.get(i) + ", Feature.ORE, new OreConfiguration(" + ruleTests.getFromList(i, "replace-variable-name") + ",\n" +
                    "                ModBlocks." + blockNames.get(i).toUpperCase() + ".get().defaultBlockState(), " + oreSizesForEachDimension.get(i) + "));\n";
        }
        return s + "\n";
    }

    @Override
    public String getConfiguredFeatureInitialisation() {
        String s = "";
        for (int i = 0; i < blockNames.size(); i++) {
            s += "    public static final ResourceKey<ConfiguredFeature<?,?>> " + primaryKeys.get(i).toUpperCase() + " = registerKey(\"" + blockNames.get(i).toLowerCase() + "\");";
        }
        return s + "\n";
    }

    @Override
    public String getConfiguredFeatureRuleTests() {
        String s = "";
        for (int i = 0; i < blockNames.size(); i++) {
            s += "        RuleTest " + ruleTests.getFromList(i, "replace-variable-name") + " = new " + ruleTests.getFromList(i, "matchtest-class") + "(" + ruleTests.getFromList(i, "replaceable-block") + ");\n";

        }
        return s + "\n";
    }

    @Override
    public String getBiomeModifierInitialisation() {
        String s = "";
        for (int i = 0; i < blockNames.size(); i++) {
            s += "    public static final ResourceKey<BiomeModifier> " + addOres.get(i).toUpperCase() + " = registerKey(\"" + addOres.get(i).toLowerCase() + "\");\n";

        }
        return s + "\n";
    }

    @Override
    public String getBiomeModifierRegistration() {
        String s = "";
        for (int i = 0; i < blockNames.size(); i++) {
            s += "        context.register(" + addOres.get(i).toUpperCase() + ", new BiomeModifiers.AddFeaturesBiomeModifier(\n" +
                    "                biomes.getOrThrow(BiomeTags.IS_" + ((String) dimensions.get(i)).toUpperCase() + "),\n" +
                    "                HolderSet.direct(placedFeatures.getOrThrow(ModPlacedFeatures." + placedKeys.get(i).toUpperCase() + ")),\n" +
                    "                GenerationStep." + generationSteps.get(i) + "\n" +
                    "        ));\n";

        }
        return s + "\n";
    }
}
