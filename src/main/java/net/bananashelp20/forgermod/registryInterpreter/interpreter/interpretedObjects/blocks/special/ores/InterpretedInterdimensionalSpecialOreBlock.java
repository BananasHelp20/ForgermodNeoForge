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
    TitledTable<String> dimensions;
    TitledList<String> generationSteps; //per default: Decoration.UNDERGROUND_ORES
    TitledTable<String> ruleTests; //ruleTestVariableName, tagMatchTestType, test. e.g. stoneReplaceables, BlockMatchTest, stone_ore_replaceables
    TitledList<Integer> oreSizesForEachDimension;
    TitledTable<String> placements; //in format for each dimension: triangle/uniform, from (as written int between -64 and 256), to (as written int between -64 and 256)

    public InterpretedInterdimensionalSpecialOreBlock(String defaultName, ArrayList<String> blockNames, TitledTable<String> dimensions, TitledList<String> generationSteps, TitledTable<String> ruleTests, TitledList<Integer> oreSizesForEachDimension, TitledTable<String> placements) {
        this.blockNames = blockNames;
        this.defaultName = defaultName;
        this.dimensions = dimensions;
        this.generationSteps = generationSteps;
        this.ruleTests = ruleTests;
        this.oreSizesForEachDimension = oreSizesForEachDimension;
        this.placements = placements;
    }

    private ArrayList<String> placedKeys = getPlacedKeys(blockNames);
    private ArrayList<String> primaryKeys = getPrimaryKeys(blockNames);
    private ArrayList<String> addOres = getAddOres(blockNames);
    private String[] listNames = {"overworld" + defaultName + "s", "nether" + defaultName + "s", "end" + defaultName + "s"};

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
        for (int i = 0; i < dimensions.length() && i < blockNames.size(); i++) {
            s += "        List<OreConfiguration.TargetBlockState> " + listNames[i] + " = List.of(\n";
            for (int j = 0; j < blockNames.size() && j < dimensions.innerLength(i); j++) {
                s += "                OreConfiguration.target(" + ruleTests.getFromList(0, "replace-variable-name") + ", ModBlocks." + blockNames.get(i).toUpperCase() + ".get().defaultBlockState()),\n";
            }
            s = s.substring(0, s.length()-2) + "\n";
            s += "        );\n";
        }
        return s + "\n";
    }

    @Override
    public String getConfiguredFeatureRegistration() {
        String s = "";

        for (int i = 0; i < blockNames.size(); i++) {
            s += "        register(context, " + primaryKeys.getFirst() + ", Feature.ORE, new OreConfiguration(" + listNames[i] + ", " + oreSizesForEachDimension.get(0) + "));\n";
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
                    "                biomes.getOrThrow(BiomeTags.IS_" + dimensions.getTitle(i).toUpperCase() + "),\n" +
                    "                HolderSet.direct(placedFeatures.getOrThrow(ModPlacedFeatures." + placedKeys.get(i).toUpperCase() + ")),\n" +
                    "                GenerationStep." + generationSteps.get(i) + "\n" +
                    "        ));\n";

        }
        return s + "\n";
    }
}
