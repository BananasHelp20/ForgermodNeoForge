package net.bananashelp20.forgermod.worldgen;

import net.bananashelp20.forgermod.ForgerMod;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;

import java.util.List;

public class ModPlacedFeatures {
    public static final ResourceKey<PlacedFeature> OVERWORLD_JADE_ORE_PLACED_KEY = registerKey("overworld_jade_ore_placed");
    public static final ResourceKey<PlacedFeature> NETHER_JADE_ORE_PLACED_KEY = registerKey("nether_jade_ore_placed");
    public static final ResourceKey<PlacedFeature> END_JADE_ORE_PLACED_KEY = registerKey("end_jade_ore_placed");


    public static void bootsrap(BootstrapContext<PlacedFeature> context) {
        var configuredFeatures = context.lookup(Registries.CONFIGURED_FEATURE);

        register(context, OVERWORLD_JADE_ORE_PLACED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.OVERWORLD_JADE_ORE_KEY),
                ModOrePlacement.commonOrePlacement(12, HeightRangePlacement.triangle(VerticalAnchor.absolute(-64), VerticalAnchor.absolute(80))));

        register(context, NETHER_JADE_ORE_PLACED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.NETHER_JADE_ORE_KEY),
                ModOrePlacement.commonOrePlacement(9, HeightRangePlacement.uniform(VerticalAnchor.absolute(-64), VerticalAnchor.absolute(80))));

        register(context, END_JADE_ORE_PLACED_KEY, configuredFeatures.getOrThrow(ModConfiguredFeatures.END_JADE_ORE_KEY),
                ModOrePlacement.commonOrePlacement(12, HeightRangePlacement.uniform(VerticalAnchor.absolute(-64), VerticalAnchor.absolute(80))));
    }

    public static ResourceKey<PlacedFeature> registerKey(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, ResourceLocation.fromNamespaceAndPath(ForgerMod.MOD_ID, name));
    }

    private static void register(BootstrapContext<PlacedFeature> context, ResourceKey<PlacedFeature> key, Holder<ConfiguredFeature<?, ?>> configuration, List<PlacementModifier> modifiers) {
        context.register(key, new PlacedFeature(configuration, modifiers));
    }
}
