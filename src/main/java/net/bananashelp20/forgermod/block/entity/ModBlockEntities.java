package net.bananashelp20.forgermod.block.entity;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.block.ModBlocks;
import net.bananashelp20.forgermod.block.entity.custom.ForgeBlockEntity;
import net.bananashelp20.forgermod.block.entity.custom.InfusionTableBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ForgerMod.MOD_ID);

    public static final Supplier<BlockEntityType<ForgeBlockEntity>> FORGE_BE =
            BLOCK_ENTITIES.register("forge_be", () -> BlockEntityType.Builder.of(
                    ForgeBlockEntity::new, ModBlocks.FORGE.get()).build(null));

    public static final Supplier<BlockEntityType<InfusionTableBlockEntity>> INFUSION_TABLE_BE =
            BLOCK_ENTITIES.register("infusion_table_be", () -> BlockEntityType.Builder.of(
                    InfusionTableBlockEntity::new, ModBlocks.INFUSION_TABLE.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
