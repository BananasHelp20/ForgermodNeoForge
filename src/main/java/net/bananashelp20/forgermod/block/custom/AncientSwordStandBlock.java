package net.bananashelp20.forgermod.block.custom;

import com.mojang.serialization.MapCodec;
import net.bananashelp20.forgermod.block.entity.custom.AncientSwordStandBlockEntity;
import net.bananashelp20.forgermod.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class AncientSwordStandBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final MapCodec<AncientSwordStandBlock> CODEC = simpleCodec(AncientSwordStandBlock::new);
    public static final BooleanProperty FULL = BooleanProperty.create("full");
    public static final BooleanProperty OPENING = BooleanProperty.create("opening");
    // Four broad stand boxes, plus one sword box while full; cached for each facing.
    private static final VoxelShape[] EMPTY_SHAPES = shapes(false);
    private static final VoxelShape[] FULL_SHAPES = shapes(true);

    public AncientSwordStandBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH)
                .setValue(FULL, true).setValue(OPENING, false));
    }

    private static VoxelShape[] shapes(boolean full) {
        // The detailed Blockbench export has 162 boxes. Merge the lower and upper
        // ribs into broad envelopes, including their forward reach and the open pose.
        VoxelShape north = Shapes.or(Block.box(0, 0, 2.5, 16, 1, 13.5),
                Block.box(1, 1, 3, 15, 11, 14.5), Block.box(-0.5, 11, 3, 16.5, 25, 14.5),
                Block.box(6, 25, 9, 10, 29, 11.5));
        if (full) north = Shapes.or(north, Block.box(3, 3.5, 8.5, 14, 31.25, 9.5));
        VoxelShape[] result = new VoxelShape[4];
        result[Direction.NORTH.get2DDataValue()] = north.optimize();
        VoxelShape current = north;
        for (Direction direction : new Direction[]{Direction.EAST, Direction.SOUTH, Direction.WEST}) {
            VoxelShape[] rotated = {Shapes.empty()};
            current.forAllBoxes((x0, y0, z0, x1, y1, z1) -> rotated[0] = Shapes.or(rotated[0],
                    Shapes.box(1 - z1, y0, x0, 1 - z0, y1, x1)));
            current = rotated[0].optimize();
            result[direction.get2DDataValue()] = current;
        }
        return result;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return (state.getValue(FULL) ? FULL_SHAPES : EMPTY_SHAPES)[state.getValue(FACING).get2DDataValue()];
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                              Player player, BlockHitResult hit) {
        // Re-read: queued clicks and the other hand must not claim the sword twice.
        BlockState current = level.getBlockState(pos);
        if (!current.is(this) || !current.getValue(FULL)) return InteractionResult.PASS;
        if (!level.isClientSide) {
            if (!(level.getBlockEntity(pos) instanceof AncientSwordStandBlockEntity stand)) return InteractionResult.FAIL;
            stand.beginOpening(level.getGameTime());
            level.setBlock(pos, current.setValue(FULL, false).setValue(OPENING, true), Block.UPDATE_ALL);
            level.sendBlockUpdated(pos, current, level.getBlockState(pos), Block.UPDATE_CLIENTS);
            level.scheduleTick(pos, this, AncientSwordStandBlockEntity.OPENING_TICKS);
            ItemStack reward = new ItemStack(ModItems.RUSTY_CLAYMORE.get());
            player.getInventory().add(reward);
            if (!reward.isEmpty()) player.drop(reward, false);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                             Player player, InteractionHand hand, BlockHitResult hit) {
        InteractionResult result = useWithoutItem(state, level, pos, player, hit);
        return result.consumesAction() ? ItemInteractionResult.sidedSuccess(level.isClientSide)
                : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.getValue(OPENING)) return;
        if (level.getBlockEntity(pos) instanceof AncientSwordStandBlockEntity stand) {
            int remaining = stand.remainingTicks(level.getGameTime());
            if (remaining > 0) {
                level.scheduleTick(pos, this, remaining);
                return;
            }
        }
        level.setBlock(pos, state.setValue(OPENING, false), Block.UPDATE_ALL);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AncientSwordStandBlockEntity(pos, state);
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() { return CODEC; }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, FULL, OPENING);
    }
}
