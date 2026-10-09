package net.bananashelp20.forgermod.worldgen;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraft.world.level.levelgen.structure.templatesystem.LiquidSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import java.util.Optional;

/** Keeps vanilla city assembly intact, then adds exactly one non-overlapping grave. */
public final class AncientCityWithGraveStructure extends Structure {
    public static final MapCodec<AncientCityWithGraveStructure> CODEC = JigsawStructure.CODEC.xmap(
            AncientCityWithGraveStructure::new, structure -> structure.city);
    private final JigsawStructure city;

    public AncientCityWithGraveStructure(JigsawStructure city) {
        super(city.modifiableStructureInfo().getOriginalStructureInfo().structureSettings());
        this.city = city;
    }

    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        return city.findGenerationPoint(context).map(stub -> new GenerationStub(stub.position(), builder -> {
            stub.getPiecesBuilder().build().pieces().forEach(builder::addPiece);
            addGrave(builder, context.structureTemplateManager());
        }));
    }

    public static void addGrave(StructurePiecesBuilder builder, StructureTemplateManager manager) {
        if (builder.isEmpty()) return;
        var cityPieces = builder.build().pieces();
        var root = (PoolElementStructurePiece)cityPieces.getFirst();
        var rootBox = root.getBoundingBox();
        int x = (rootBox.minX() + rootBox.maxX()) / 2;
        int z = (rootBox.minZ() + rootBox.maxZ()) / 2;
        int y = root.getPosition().getY();
        var grave = StructurePoolElement.single("forgermod:ancient_grave", LiquidSettings.IGNORE_WATERLOGGING)
                .apply(StructureTemplatePool.Projection.RIGID);
        // Prefer a gap near the center. Check the COMPLETE assembled city's bounds.
        for (int radius = 28; radius <= 112; radius += 8) {
            BlockPos[] origins = {new BlockPos(x + radius, y, z - 10),
                    new BlockPos(x + 10, y, z + radius), new BlockPos(x - radius, y, z + 10),
                    new BlockPos(x - 10, y, z - radius)};
            Rotation[] rotations = {Rotation.NONE, Rotation.CLOCKWISE_90,
                    Rotation.CLOCKWISE_180, Rotation.COUNTERCLOCKWISE_90};
            for (int side = 0; side < origins.length; side++) {
                var box = grave.getBoundingBox(manager, origins[side], rotations[side]);
                if (builder.findCollisionPiece(box.inflatedBy(3)) == null) {
                    builder.addPiece(new PoolElementStructurePiece(manager, grave, origins[side],
                            grave.getGroundLevelDelta(), rotations[side], box, LiquidSettings.IGNORE_WATERLOGGING));
                    return;
                }
            }
        }
        // Dense layouts: just beyond the easternmost city piece is always clear.
        int edge = cityPieces.stream().mapToInt(piece -> piece.getBoundingBox().maxX()).max().orElseThrow();
        BlockPos origin = new BlockPos(edge + 4, y, z - 10);
        builder.addPiece(new PoolElementStructurePiece(manager, grave, origin, grave.getGroundLevelDelta(),
                Rotation.NONE, grave.getBoundingBox(manager, origin, Rotation.NONE), LiquidSettings.IGNORE_WATERLOGGING));
    }

    @Override
    public StructureType<?> type() { return ModStructures.ANCIENT_CITY.get(); }
}
