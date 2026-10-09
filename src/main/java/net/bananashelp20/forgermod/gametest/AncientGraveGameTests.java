package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.block.ModBlocks;
import net.bananashelp20.forgermod.block.custom.AncientSwordStandBlock;
import net.bananashelp20.forgermod.worldgen.AncientCityWithGraveStructure;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import java.util.HashSet;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class AncientGraveGameTests {
    @GameTest(template = "sonic_boom_test", timeoutTicks = 600)
    public static void assembledCitiesNeverOverlapGrave(GameTestHelper test) throws Exception {
        var level = test.getLevel();
        var city = level.registryAccess().registryOrThrow(Registries.STRUCTURE)
                .get(ResourceLocation.withDefaultNamespace("ancient_city"));
        test.assertTrue(city instanceof AncientCityWithGraveStructure, "Collision-aware city type not loaded");
        var field = AncientCityWithGraveStructure.class.getDeclaredField("city");
        field.setAccessible(true);
        var vanilla = (net.minecraft.world.level.levelgen.structure.structures.JigsawStructure)field.get(city);
        var generator = level.getChunkSource().getGenerator();
        var manager = level.getStructureManager();
        var variants = new HashSet<String>();
        var rotations = new HashSet<Rotation>();
        for (long sample = 0; sample < 48; sample++) {
            long seed = sample * 0x9E3779B97F4A7C15L;
            var chunk = new ChunkPos(0, 0);
            var context = new Structure.GenerationContext(level.registryAccess(), generator,
                    generator.getBiomeSource(), level.getChunkSource().randomState(), manager,
                    seed, chunk, level, biome -> true);
            var pieces = city.findValidGenerationPoint(context).orElseThrow().getPiecesBuilder().build().pieces();
            var vanillaContext = new Structure.GenerationContext(level.registryAccess(), generator,
                    generator.getBiomeSource(), level.getChunkSource().randomState(), manager,
                    seed, chunk, level, biome -> true);
            var original = vanilla.findGenerationPoint(vanillaContext).orElseThrow().getPiecesBuilder().build().pieces();
            test.assertTrue(pieces.size() == original.size() + 1, "Vanilla city pieces changed or grave duplicated");
            for (int i = 0; i < original.size(); i++) {
                test.assertTrue(original.get(i).getBoundingBox().equals(pieces.get(i).getBoundingBox()),
                        "Grave changed vanilla piece placement");
            }
            var root = (PoolElementStructurePiece)pieces.getFirst();
            variants.add(root.getElement().toString());
            rotations.add(root.getRotation());
            var grave = (PoolElementStructurePiece)pieces.getLast();
            test.assertTrue(grave.getElement().toString().contains("forgermod:ancient_grave"), "Grave missing");
            var bounds = grave.getBoundingBox();
            test.assertTrue((bounds.minX() >> 4) >= chunk.x - 8 && (bounds.maxX() >> 4) <= chunk.x + 8
                    && (bounds.minZ() >> 4) >= chunk.z - 8 && (bounds.maxZ() >> 4) <= chunk.z + 8,
                    "Grave extends beyond neighboring-chunk structure reference range");
            for (int i = 0; i < pieces.size() - 1; i++) {
                test.assertFalse(grave.getBoundingBox().inflatedBy(3).intersects(pieces.get(i).getBoundingBox()),
                        "Grave collides with assembled city piece at seed " + seed);
            }
        }
        test.assertTrue(variants.size() == 3 && rotations.size() == 4, "Seeds did not exercise all city centers and rotations");
        test.succeed();
    }

    @GameTest(template = "sonic_boom_test", timeoutTicks = 400)
    public static void gravePlacementRetainsFullStand(GameTestHelper test) {
        var level = test.getLevel();
        var manager = level.getStructureManager();
        var grave = net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement
                .single("forgermod:ancient_grave").apply(
                        net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool.Projection.RIGID);
        for (Rotation rotation : Rotation.values()) {
            BlockPos origin = test.absolutePos(new BlockPos(100 + rotation.ordinal() * 80, 16, 100));
            var box = grave.getBoundingBox(manager, origin, rotation);
            for (int x = box.minX() >> 4; x <= box.maxX() >> 4; x++) {
                for (int z = box.minZ() >> 4; z <= box.maxZ() >> 4; z++) level.getChunk(x, z);
            }
            test.assertTrue(grave.place(manager, level, level.structureManager(),
                    level.getChunkSource().getGenerator(), origin, origin, rotation, box,
                    RandomSource.create(1), LiquidSettings.IGNORE_WATERLOGGING, false), "Grave placement failed");
            BlockPos stand = origin.offset(StructureTemplate.calculateRelativePosition(
                    new StructurePlaceSettings().setRotation(rotation), new BlockPos(8, 5, 10)));
            var state = level.getBlockState(stand);
            test.assertTrue(state.is(ModBlocks.ANCIENT_SWORD_STAND.get()) && state.getValue(AncientSwordStandBlock.FULL),
                    "Generated grave stand missing or empty");
        }
        test.succeed();
    }
}
