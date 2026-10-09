package net.bananashelp20.forgermod.worldgen;

import net.bananashelp20.forgermod.ForgerMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.function.Supplier;

public final class ModStructures {
    public static final DeferredRegister<StructureType<?>> TYPES =
            DeferredRegister.create(Registries.STRUCTURE_TYPE, ForgerMod.MOD_ID);
    public static final Supplier<StructureType<AncientCityWithGraveStructure>> ANCIENT_CITY =
            TYPES.register("ancient_city_with_grave", () -> () -> AncientCityWithGraveStructure.CODEC);
    private ModStructures() {}
}
