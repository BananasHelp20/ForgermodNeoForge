package net.bananashelp20.forgermod.block.custom;

import com.mojang.serialization.MapCodec;
import net.bananashelp20.forgermod.block.entity.ModBlockEntities;
import net.bananashelp20.forgermod.block.entity.custom.AugmentationTableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class AugmentationTableBlock extends BaseEntityBlock {
    public static final MapCodec<AugmentationTableBlock> CODEC=simpleCodec(AugmentationTableBlock::new);
    private static final VoxelShape SHAPE = Shapes.or(
            box(0, 0, 0, 16, 2, 16), box(1, 2, 1, 15, 3, 15),
            box(2, 3, 2, 14, 12, 14),
            box(1, 3, 1, 3, 12, 3), box(13, 3, 1, 15, 12, 3),
            box(1, 3, 13, 3, 12, 15), box(13, 3, 13, 15, 12, 15),
            box(1, 12, 1, 15, 14, 15),
            box(2, 14, 2, 14, 14.5, 14),
            box(0.5, 14, 1, 3, 16, 15), box(13, 14, 1, 15.5, 16, 15),
            box(3, 14, 13, 13, 16, 15.5), box(3, 14, 0.5, 13, 15, 2));
    public AugmentationTableBlock(Properties properties) { super(properties); }
    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
    @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new AugmentationTableBlockEntity(pos,state); }
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }
    @Override protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
    @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof AugmentationTableBlockEntity table)
            ((net.minecraft.server.level.ServerPlayer)player).openMenu(table,pos);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
    @Override protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState replacement, boolean moving) {
        if (!level.isClientSide && !state.is(replacement.getBlock()) && level.getBlockEntity(pos) instanceof AugmentationTableBlockEntity table) table.drops();
        super.onRemove(state,level,pos,replacement,moving);
    }
    @Override public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return level.isClientSide ? null : createTickerHelper(type,ModBlockEntities.AUGMENTATION_TABLE_BE.get(),
                (world,pos,block,table)->table.tick());
    }
}
