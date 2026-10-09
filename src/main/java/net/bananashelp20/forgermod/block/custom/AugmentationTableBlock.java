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
    private static final VoxelShape SHAPE = Shapes.or(box(2, 0, 2, 14, 3, 14),
            box(5, 3, 5, 11, 13, 11), box(0, 13, 0, 16, 16, 16));
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
