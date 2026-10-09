package net.bananashelp20.forgermod.block.entity.custom;

import net.bananashelp20.forgermod.block.custom.AncientSwordStandBlock;
import net.bananashelp20.forgermod.block.entity.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class AncientSwordStandBlockEntity extends BlockEntity {
    public static final int OPENING_TICKS = 35;
    public static final int LAST_FRAME = 18;
    private long openingStarted = -1;

    public AncientSwordStandBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ANCIENT_SWORD_STAND_BE.get(), pos, state);
    }

    public void beginOpening(long gameTime) {
        openingStarted = gameTime;
        setChanged();
    }

    public int remainingTicks(long gameTime) {
        return openingStarted < 0 ? 0 : (int) Math.max(0, Math.min(OPENING_TICKS,
                OPENING_TICKS - (gameTime - openingStarted)));
    }

    public int animationFrame(long gameTime, float partialTick) {
        if (openingStarted < 0) return LAST_FRAME;
        return (int) Math.max(0, Math.min(LAST_FRAME, (gameTime - openingStarted + partialTick) / 2));
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide && getBlockState().getValue(AncientSwordStandBlock.OPENING)) {
            level.scheduleTick(worldPosition, getBlockState().getBlock(), Math.max(1, remainingTicks(level.getGameTime())));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putLong("OpeningStarted", openingStarted);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        openingStarted = tag.contains("OpeningStarted") ? tag.getLong("OpeningStarted") : -1;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) { return saveWithoutMetadata(registries); }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() { return ClientboundBlockEntityDataPacket.create(this); }

}
