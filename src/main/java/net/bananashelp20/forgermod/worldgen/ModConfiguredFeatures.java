package net.bananashelp20.forgermod.worldgen;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockMatchTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.neoforged.fml.common.Mod;

import java.util.List;

public class ModConfiguredFeatures {
    public static final ResourceKey<ConfiguredFeature<?,?>> OVERWORLD_JADE_ORE_KEY = registerKey("overworld_jade_ore");
    public static final ResourceKey<ConfiguredFeature<?,?>> NETHER_JADE_ORE_KEY = registerKey("nether_jade_ore");
    public static final ResourceKey<ConfiguredFeature<?,?>> END_JADE_ORE_KEY = registerKey("end_jade_ore");

    public static void boostrap(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        RuleTest stoneReplaceables = new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES);
        RuleTest deepslateReplaceables = new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES);
        RuleTest netherrackReplaceables = new BlockMatchTest(Blocks.NETHERRACK);
        RuleTest endstoneReplaceables = new BlockMatchTest(Blocks.END_STONE);
        RuleTest obsidianReplaceables = new BlockMatchTest(Blocks.OBSIDIAN);

        List<OreConfiguration.TargetBlockState> overworldJadeOres = List.of(
                OreConfiguration.target(stoneReplaceables, ModBlocks.JADE_STONE_ORE.get().defaultBlockState()),
                OreConfiguration.target(deepslateReplaceables, ModBlocks.JADE_DEEPSLATE_ORE.get().defaultBlockState())
        );

        List<OreConfiguration.TargetBlockState> endJadeOres = List.of(
                OreConfiguration.target(endstoneReplaceables, ModBlocks.JADE_END_ORE.get().defaultBlockState()),
                OreConfiguration.target(obsidianReplaceables, ModBlocks.JADE_OBSIDIAN_ORE.get().defaultBlockState())
        );

        register(context, OVERWORLD_JADE_ORE_KEY, Feature.ORE, new OreConfiguration(overworldJadeOres, 1));
        register(context, NETHER_JADE_ORE_KEY, Feature.ORE, new OreConfiguration(netherrackReplaceables, ModBlocks.JADE_NETHER_ORE.get().defaultBlockState(), 1));
        register(context, END_JADE_ORE_KEY, Feature.ORE, new OreConfiguration(endJadeOres, 1));
    }

    public static ResourceKey<ConfiguredFeature<?, ?>> registerKey(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, ResourceLocation.fromNamespaceAndPath(ForgerMod.MOD_ID, name));
    }

    private static <FC extends FeatureConfiguration, F extends Feature<FC>> void register(BootstrapContext<ConfiguredFeature<?,?>> context,
                                                                                         ResourceKey<ConfiguredFeature<?,?>> key, F feature, FC configuration) {
        context.register(key, new ConfiguredFeature<>(feature, configuration));
    }
}
