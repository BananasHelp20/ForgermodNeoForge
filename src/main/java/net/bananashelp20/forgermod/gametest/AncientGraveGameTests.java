package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.block.ModBlocks;
import net.bananashelp20.forgermod.block.custom.AncientSwordStandBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class AncientGraveGameTests {
    @GameTest(template = "sonic_boom_test", timeoutTicks = 400)
    public static void allCityStartsContainOneGrave(GameTestHelper test) {
        var level = test.getLevel();
        var pool = level.registryAccess().registryOrThrow(Registries.TEMPLATE_POOL)
                .get(ResourceLocation.fromNamespaceAndPath(ForgerMod.MOD_ID, "ancient_city_start"));
        test.assertTrue(pool != null, "Ancient Grave starting pool missing");
        var templates = pool.getShuffledTemplates(RandomSource.create(1));
        test.assertTrue(templates.size() == 3, "Vanilla center variants lost");
        var manager = level.getStructureManager();
        for (int index = 0; index < 4; index++) {
            var element = templates.get(index % 3);
            Rotation rotation = Rotation.values()[index];
            BlockPos origin = test.absolutePos(new BlockPos(100 + index * 80, 16, 100));
            var box = element.getBoundingBox(manager, origin, rotation);
            for (int x = box.minX() >> 4; x <= box.maxX() >> 4; x++) {
                for (int z = box.minZ() >> 4; z <= box.maxZ() >> 4; z++) level.getChunk(x, z);
            }
            test.assertTrue(element.place(manager, level, level.structureManager(),
                    level.getChunkSource().getGenerator(), origin, origin, rotation, box,
                    RandomSource.create(index), LiquidSettings.IGNORE_WATERLOGGING, false), "City start placement failed");
            BlockPos stand = origin.offset(StructureTemplate.calculateRelativePosition(
                    new StructurePlaceSettings().setRotation(rotation), new BlockPos(26, 5, 20)));
            var state = level.getBlockState(stand);
            test.assertTrue(state.is(ModBlocks.ANCIENT_SWORD_STAND.get()), "Mandatory grave missing after rotation " + rotation);
            test.assertTrue(state.getValue(AncientSwordStandBlock.FULL), "Generated grave sword stand is empty");
            int stands = 0;
            for (BlockPos pos : BlockPos.betweenClosed(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ())) {
                if (level.getBlockState(pos).is(ModBlocks.ANCIENT_SWORD_STAND.get())) stands++;
            }
            test.assertTrue(stands == 1, "City start contains " + stands + " stands instead of one");
        }
        test.succeed();
    }
}
