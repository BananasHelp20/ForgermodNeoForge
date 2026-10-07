package net.bananashelp20.forgermod.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.ai.behavior.GoAndGiveItemsToTarget;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;

public class AncientSwordStandBlock extends HorizontalDirectionalBlock {
    public static final MapCodec<AncientSwordStandBlock> CODEC = simpleCodec(AncientSwordStandBlock::new);
    public VoxelShape facingNorthShapeFull() {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(0, 0, 0.15625, 1, 0.0625, 0.84375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.1875, 0.0625, 0.59375, 0.8125, 0.6875, 0.78125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.125, 0.6875, 0.59375, 0.3125, 1.4375, 0.78125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.6875, 0.6875, 0.59375, 0.875, 1.4375, 0.78125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.3125, 0.6875, 0.59375, 0.6875, 1.375, 0.78125), BooleanOp.OR);

        return shape;
    }
    public VoxelShape facingWestShapeFull() {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(-0.375, 0, -1.09375, 0.625, 0.0625, -0.4062499999999999), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-0.1875, 0.0625, -0.65625, 0.4375, 0.6875, -0.4687499999999999), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-0.25, 0.6875, -0.65625, -0.0625, 1.4375, -0.4687499999999999), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.3125, 0.6875, -0.65625, 0.5, 1.4375, -0.4687499999999999), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-0.0625, 0.6875, -0.65625, 0.3125, 1.375, -0.4687499999999999), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.0625, 1.375, -0.59375, 0.1875, 1.8125, -0.53125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0.1875, 1.625, -0.6875, 0.25, 1.6875, -0.53125), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(0, 1.625, -0.6875, 0.0625, 1.6875, -0.53125), BooleanOp.OR);

        return shape;
    }
    public VoxelShape facingSouthShapeFull() {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(-1.3125, 1.484375, -0.33125000000000004, -1.25, 1.546875, -0.30000000000000004), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-1.1875, 1.546875, -0.33125000000000004, -1.125, 1.609375, -0.30000000000000004), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-1.25, 1.546875, -0.33125000000000004, -1.1875, 1.609375, -0.30000000000000004), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-1.1875, 1.484375, -0.33125000000000004, -1.125, 1.546875, -0.30000000000000004), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-1.125, 1.484375, -0.33125000000000004, -1.0625, 1.546875, -0.30000000000000004), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-1.125, 1.421875, -0.33125000000000004, -1.0625, 1.484375, -0.30000000000000004), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-1.25, 1.484375, -0.33125000000000004, -1.1875, 1.546875, -0.30000000000000004), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-1.25, 1.421875, -0.33125000000000004, -1.1875, 1.484375, -0.30000000000000004), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-1.3125, 1.421875, -0.33125000000000004, -1.25, 1.484375, -0.30000000000000004), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-1.375, 1.421875, -0.33125000000000004, -1.3125, 1.484375, -0.30000000000000004), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-1.375, 1.359375, -0.33125000000000004, -1.3125, 1.421875, -0.30000000000000004), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-1.3125, 1.359375, -0.33125000000000004, -1.25, 1.421875, -0.30000000000000004), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-1.375, 1.296875, -0.33125000000000004, -1.3125, 1.359375, -0.30000000000000004), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-1.4375, 1.296875, -0.33125000000000004, -1.375, 1.359375, -0.30000000000000004), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-1.5, 1.296875, -0.33125000000000004, -1.4375, 1.359375, -0.30000000000000004), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-1.3125, 1.234375, -0.33125000000000004, -1.25, 1.296875, -0.30000000000000004), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-1.375, 1.234375, -0.33125000000000004, -1.3125, 1.296875, -0.30000000000000004), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-1.4375, 1.234375, -0.33125000000000004, -1.375, 1.296875, -0.30000000000000004), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-1.5, 1.234375, -0.33125000000000004, -1.4375, 1.296875, -0.30000000000000004), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-1.3125, 1.296875, -0.33125000000000004, -1.25, 1.359375, -0.30000000000000004), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-1.25, 1.171875, -0.33125000000000004, -1.1875, 1.234375, -0.30000000000000004), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-1.3125, 1.171875, -0.33125000000000004, -1.25, 1.234375, -0.30000000000000004), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-1.375, 1.171875, -0.33125000000000004, -1.3125, 1.234375, -0.30000000000000004), BooleanOp.OR);

        return shape;
    }
    public VoxelShape facingEastShapeFull() {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.join(shape, Shapes.box(-1.25, 0, 0.53125, -0.25, 0.0625, 1.21875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-1.0625, 0.0625, 0.96875, -0.4375, 0.6875, 1.15625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-1.125, 0.6875, 0.96875, -0.9375, 1.4375, 1.15625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-0.5625, 0.6875, 0.96875, -0.375, 1.4375, 1.15625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-0.9375, 0.6875, 0.96875, -0.5625, 1.375, 1.15625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-0.8125, 1.375, 1.03125, -0.6875, 1.8125, 1.09375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-0.6875, 1.625, 0.9375, -0.625, 1.6875, 1.09375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-0.875, 1.625, 0.9375, -0.8125, 1.6875, 1.09375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-0.375, 1.3125, 1.03125, -0.25, 1.4375, 1.09375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-0.375, 1.125, 1.03125, -0.25, 1.25, 1.09375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-0.375, 0.9375, 1.03125, -0.25, 1.0625, 1.09375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-0.375, 0.75, 1.03125, -0.25, 0.875, 1.09375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-1.125, 1.4375, 1.03125, -0.9375, 1.5625, 1.09375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-1.25, 1.3125, 1.03125, -1.125, 1.4375, 1.09375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-1.25, 1.125, 1.03125, -1.125, 1.25, 1.09375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-1.25, 0.9375, 1.03125, -1.125, 1.0625, 1.09375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-1.1875, 0.5, 1.03125, -1.0625, 0.625, 1.09375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-1.1875, 0.3125, 1.03125, -1.0625, 0.4375, 1.09375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-1.1875, 0.125, 1.03125, -1.0625, 0.25, 1.09375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-0.4375, 0.125, 1.03125, -0.3125, 0.25, 1.09375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-0.4375, 0.0625, 0.78125, -0.375, 0.1875, 0.90625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-1.125, 0.0625, 0.78125, -1.0625, 0.1875, 0.90625), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-0.4375, 0.0625, 0.59375, -0.375, 0.1875, 0.71875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-1.125, 0.0625, 0.59375, -1.0625, 0.1875, 0.71875), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-0.4375, 0.3125, 1.03125, -0.3125, 0.4375, 1.09375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-0.4375, 0.5, 1.03125, -0.3125, 0.625, 1.09375), BooleanOp.OR);
        shape = Shapes.join(shape, Shapes.box(-1.25, 0.75, 1.03125, -1.125, 0.875, 1.09375), BooleanOp.OR);

        return shape;
    }

    public AncientSwordStandBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FULL, Boolean.valueOf(true)));
    }
    public static final BooleanProperty FULL = BooleanProperty.create("full");

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { // full : empty
        switch (state.getValue(FACING)) {
            case SOUTH -> {
                return state.getValue(FULL) ? facingSouthShapeFull() : facingSouthShapeFull();
            }
            case EAST -> {
                return state.getValue(FULL) ? facingEastShapeFull() : facingEastShapeFull();
            }
            case WEST -> {
                return state.getValue(FULL) ? facingWestShapeFull() : facingWestShapeFull();
            }
            default -> {
                return state.getValue(FULL) ? facingNorthShapeFull() : facingNorthShapeFull();
            }
        }
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING, FULL);
    }
}