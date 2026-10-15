package net.bananashelp20.forgermod.worldgen;

import net.bananashelp20.forgermod.ForgerMod;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class ModBiomeModifiers {
    public static final ResourceKey<BiomeModifier> ADD_METEORIC_OVERWORLD = registerKey("add_meteoric_iron_overworld");
    public static final ResourceKey<BiomeModifier> ADD_METEORIC_END = registerKey("add_meteoric_iron_end");
    public static final ResourceKey<BiomeModifier> ADD_TITANIUM_ORE = registerKey("add_titanium_ore");
    public static final ResourceKey<BiomeModifier> ADD_TUNGSTEN_ORE = registerKey("add_tungsten_ore");
    public static final ResourceKey<BiomeModifier> ADD_DOLOMITE_ORE = registerKey("add_dolomite_ore");
    public static final ResourceKey<BiomeModifier> ADD_JADE_ORE = registerKey("add_jade_ore");
    public static final ResourceKey<BiomeModifier> ADD_NETHER_JADE_ORE = registerKey("add_nether_jade_ore");
    public static final ResourceKey<BiomeModifier> ADD_END_JADE_ORE = registerKey("add_end_jade_ore");

    public static final ResourceKey<BiomeModifier> ADD_RUBY_ORE = registerKey("add_ruby_ore");
    public static final ResourceKey<BiomeModifier> ADD_NETHER_RUBY_ORE = registerKey("add_nether_ruby_ore");
    public static final ResourceKey<BiomeModifier> ADD_END_RUBY_ORE = registerKey("add_end_ruby_ore");

    public static final ResourceKey<BiomeModifier> ADD_AMETHYST_ORE = registerKey("add_amethyst_ore");
    public static final ResourceKey<BiomeModifier> ADD_NETHER_AMETHYST_ORE = registerKey("add_nether_amethyst_ore");
    public static final ResourceKey<BiomeModifier> ADD_END_AMETHYST_ORE = registerKey("add_end_amethyst_ore");

    public static final ResourceKey<BiomeModifier> ADD_AMBER_ORE = registerKey("add_amber_ore");
    public static final ResourceKey<BiomeModifier> ADD_NETHER_AMBER_ORE = registerKey("add_nether_amber_ore");
    public static final ResourceKey<BiomeModifier> ADD_END_AMBER_ORE = registerKey("add_end_amber_ore");

    public static final ResourceKey<BiomeModifier> ADD_SHARDIUM_ORE = registerKey("add_shardium_ore");
    public static final ResourceKey<BiomeModifier> ADD_NETHER_SHARDIUM_ORE = registerKey("add_nether_shardium_ore");
    public static final ResourceKey<BiomeModifier> ADD_END_SHARDIUM_ORE = registerKey("add_end_shardium_ore");


    public static void bootstrap(BootstrapContext<BiomeModifier> context) {
        var placedFeatures = context.lookup(Registries.PLACED_FEATURE);
        var biomes = context.lookup(Registries.BIOME);
        addOre(context, ADD_METEORIC_OVERWORLD, biomes.getOrThrow(BiomeTags.IS_OVERWORLD), placedFeatures.getOrThrow(ModPlacedFeatures.METEORIC_OVERWORLD));
        addOre(context, ADD_METEORIC_END, biomes.getOrThrow(BiomeTags.IS_END), placedFeatures.getOrThrow(ModPlacedFeatures.METEORIC_END));
        addOre(context, ADD_TITANIUM_ORE, biomes.getOrThrow(BiomeTags.IS_OVERWORLD), placedFeatures.getOrThrow(ModPlacedFeatures.TITANIUM_ORE));
        addOre(context, ADD_TUNGSTEN_ORE, biomes.getOrThrow(BiomeTags.IS_OVERWORLD), placedFeatures.getOrThrow(ModPlacedFeatures.TUNGSTEN_ORE));
        addOre(context, ADD_DOLOMITE_ORE, biomes.getOrThrow(BiomeTags.IS_OVERWORLD), placedFeatures.getOrThrow(ModPlacedFeatures.DOLOMITE_ORE));

        context.register(ADD_JADE_ORE, new BiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                HolderSet.direct(placedFeatures.getOrThrow(ModPlacedFeatures.JADE_ORE_PLACED_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES
        ));
        context.register(ADD_NETHER_JADE_ORE, new BiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(BiomeTags.IS_NETHER),
                HolderSet.direct(placedFeatures.getOrThrow(ModPlacedFeatures.NETHER_JADE_ORE_PLACED_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES
        ));
        context.register(ADD_END_JADE_ORE, new BiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(BiomeTags.IS_END),
                HolderSet.direct(placedFeatures.getOrThrow(ModPlacedFeatures.END_JADE_ORE_PLACED_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES
        ));

        context.register(ADD_RUBY_ORE, new BiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                HolderSet.direct(placedFeatures.getOrThrow(ModPlacedFeatures.RUBY_ORE_PLACED_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES
        ));
        context.register(ADD_NETHER_RUBY_ORE, new BiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(BiomeTags.IS_NETHER),
                HolderSet.direct(placedFeatures.getOrThrow(ModPlacedFeatures.NETHER_RUBY_ORE_PLACED_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES
        ));
        context.register(ADD_END_RUBY_ORE, new BiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(BiomeTags.IS_END),
                HolderSet.direct(placedFeatures.getOrThrow(ModPlacedFeatures.END_RUBY_ORE_PLACED_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES
        ));

        context.register(ADD_AMETHYST_ORE, new BiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                HolderSet.direct(placedFeatures.getOrThrow(ModPlacedFeatures.AMETHYST_ORE_PLACED_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES
        ));
        context.register(ADD_NETHER_AMETHYST_ORE, new BiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(BiomeTags.IS_NETHER),
                HolderSet.direct(placedFeatures.getOrThrow(ModPlacedFeatures.NETHER_AMETHYST_ORE_PLACED_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES
        ));
        context.register(ADD_END_AMETHYST_ORE, new BiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(BiomeTags.IS_END),
                HolderSet.direct(placedFeatures.getOrThrow(ModPlacedFeatures.END_AMETHYST_ORE_PLACED_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES
        ));

        context.register(ADD_AMBER_ORE, new BiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                HolderSet.direct(placedFeatures.getOrThrow(ModPlacedFeatures.AMBER_ORE_PLACED_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES
        ));
        context.register(ADD_NETHER_AMBER_ORE, new BiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(BiomeTags.IS_NETHER),
                HolderSet.direct(placedFeatures.getOrThrow(ModPlacedFeatures.NETHER_AMBER_ORE_PLACED_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES
        ));
        context.register(ADD_END_AMBER_ORE, new BiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(BiomeTags.IS_END),
                HolderSet.direct(placedFeatures.getOrThrow(ModPlacedFeatures.END_AMBER_ORE_PLACED_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES
        ));

        context.register(ADD_SHARDIUM_ORE, new BiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(BiomeTags.IS_OVERWORLD),
                HolderSet.direct(placedFeatures.getOrThrow(ModPlacedFeatures.SHARDIUM_ORE_PLACED_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES
        ));
        context.register(ADD_NETHER_SHARDIUM_ORE, new BiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(BiomeTags.IS_NETHER),
                HolderSet.direct(placedFeatures.getOrThrow(ModPlacedFeatures.NETHER_SHARDIUM_ORE_PLACED_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES
        ));
        context.register(ADD_END_SHARDIUM_ORE, new BiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(BiomeTags.IS_END),
                HolderSet.direct(placedFeatures.getOrThrow(ModPlacedFeatures.END_SHARDIUM_ORE_PLACED_KEY)),
                GenerationStep.Decoration.UNDERGROUND_ORES
        ));
    }

    private static ResourceKey<BiomeModifier> registerKey(String name) {
        return ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS, ResourceLocation.fromNamespaceAndPath(ForgerMod.MOD_ID, name));
    }

    private static void addOre(BootstrapContext<BiomeModifier> context, ResourceKey<BiomeModifier> key,
                               net.minecraft.core.HolderSet.Named<net.minecraft.world.level.biome.Biome> biomes,
                               net.minecraft.core.Holder<net.minecraft.world.level.levelgen.placement.PlacedFeature> feature) {
        context.register(key, new BiomeModifiers.AddFeaturesBiomeModifier(biomes, HolderSet.direct(feature),
                GenerationStep.Decoration.UNDERGROUND_ORES));
    }
}
