package net.bananashelp20.forgermod.block.custom;

import net.minecraft.core.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.*;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;
import java.util.function.Supplier;

/** Baked ancient-style displays: the FULL state owns the one-time reward, including in dropped items. */
public abstract class AncientRelicDisplayBlock extends HorizontalDirectionalBlock {
    public static final BooleanProperty FULL = BooleanProperty.create("full");
    private final Supplier<? extends Item> reward;
    private final VoxelShape[] emptyShapes, fullShapes;
    protected AncientRelicDisplayBlock(Properties properties, Supplier<? extends Item> reward, boolean axe) {
        super(properties);this.reward=reward;
        VoxelShape empty=axe?Shapes.or(box(1,0,2,15,2,14),box(2,2,3,14,3,13),box(6,2,6,10,16,10),box(2,2,7,4,18,10),box(12,2,7,14,18,10),box(2,14,6,14,16,11))
                :Shapes.or(box(1,0,1,15,2,15),box(2,2,2,14,4,14),box(4,4,4,12,7,12),box(1,7,2,15,9,14),box(2,9,3,14,9.5,13));
        emptyShapes=rotateShapes(empty);
        fullShapes=rotateShapes(Shapes.or(empty,axe?box(1,4,6,12,27,10.25):box(2,9.5,5,14,10.25,11)));
        registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH).setValue(FULL,true));
    }
    private static VoxelShape[] rotateShapes(VoxelShape north){
        var shapes=new VoxelShape[4];shapes[Direction.NORTH.get2DDataValue()]=north.optimize();var current=north;
        for(var direction:new Direction[]{Direction.EAST,Direction.SOUTH,Direction.WEST}){
            VoxelShape[] rotated={Shapes.empty()};current.forAllBoxes((x0,y0,z0,x1,y1,z1)->rotated[0]=Shapes.or(rotated[0],Shapes.box(1-z1,y0,x0,1-z0,y1,x1)));
            current=rotated[0].optimize();shapes[direction.get2DDataValue()]=current;
        }return shapes;
    }
    @Override protected VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context){return (state.getValue(FULL)?fullShapes:emptyShapes)[state.getValue(FACING).get2DDataValue()];}
    @Override protected InteractionResult useWithoutItem(BlockState state,Level level,BlockPos pos,Player player,BlockHitResult hit){
        var current=level.getBlockState(pos);
        if(!current.is(this) || !current.getValue(FULL))return InteractionResult.PASS;
        if(!level.isClientSide){
            level.setBlock(pos,current.setValue(FULL,false),Block.UPDATE_ALL);
            var item=new ItemStack(reward.get());player.getInventory().add(item);if(!item.isEmpty())player.drop(item,false);
        }return InteractionResult.sidedSuccess(level.isClientSide);
    }
    @Override protected ItemInteractionResult useItemOn(ItemStack stack,BlockState state,Level level,BlockPos pos,Player player,InteractionHand hand,BlockHitResult hit){
        return useWithoutItem(state,level,pos,player,hit).consumesAction()?ItemInteractionResult.sidedSuccess(level.isClientSide):ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
    @Override public BlockState getStateForPlacement(BlockPlaceContext context){return defaultBlockState().setValue(FACING,context.getHorizontalDirection().getOpposite());}
    @Override protected BlockState rotate(BlockState state,Rotation rotation){return state.setValue(FACING,rotation.rotate(state.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState state,Mirror mirror){return state.rotate(mirror.getRotation(state.getValue(FACING)));}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){builder.add(FACING,FULL);}
}
