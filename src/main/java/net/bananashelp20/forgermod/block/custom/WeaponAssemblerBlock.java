package net.bananashelp20.forgermod.block.custom;

import com.mojang.serialization.MapCodec;
import net.bananashelp20.forgermod.block.entity.custom.WeaponAssemblerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.*;

public final class WeaponAssemblerBlock extends BaseEntityBlock {
    public static final MapCodec<WeaponAssemblerBlock> CODEC=simpleCodec(WeaponAssemblerBlock::new);
    public static final DirectionProperty FACING=HorizontalDirectionalBlock.FACING;
    private static final VoxelShape BODY=Shapes.or(box(2,0,0,18,2,16),box(-.5,2,-.5,16.5,4,16.5),box(2,4,2,14,9,14));
    private static final VoxelShape OUTLINE=Shapes.or(BODY,box(-5,2,6,2,23,10),box(14,2,6,22,23,10),
            box(6,2,-5,10,23,2),box(6,2,14,10,23,22));
    private static final VoxelShape[] COLLISION=rotations(BODY),SHAPES=rotations(OUTLINE);
    public WeaponAssemblerBlock(Properties properties){super(properties);registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH));}
    private static VoxelShape[] rotations(VoxelShape source){
        var shapes=new VoxelShape[4];shapes[0]=source;
        for(int i=1;i<4;i++){
            final VoxelShape[] rotated={Shapes.empty()};
            shapes[i-1].forAllBoxes((x0,y0,z0,x1,y1,z1)->rotated[0]=Shapes.or(rotated[0],Shapes.box(1-z1,y0,x0,1-z0,y1,x1)));
            shapes[i]=rotated[0];
        }
        return shapes;
    }
    private static int turn(BlockState state){return switch(state.getValue(FACING)){case EAST->1;case SOUTH->2;case WEST->3;default->0;};}
    @Override protected VoxelShape getShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context){return SHAPES[turn(state)];}
    @Override protected VoxelShape getCollisionShape(BlockState state,BlockGetter level,BlockPos pos,CollisionContext context){return COLLISION[turn(state)];}
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block,BlockState> builder){builder.add(FACING);}
    @Override public BlockState getStateForPlacement(BlockPlaceContext context){return defaultBlockState().setValue(FACING,context.getHorizontalDirection().getOpposite());}
    @Override protected BlockState rotate(BlockState state,Rotation rotation){return state.setValue(FACING,rotation.rotate(state.getValue(FACING)));}
    @Override protected BlockState mirror(BlockState state,Mirror mirror){return rotate(state,mirror.getRotation(state.getValue(FACING)));}
    @Override protected MapCodec<? extends BaseEntityBlock> codec(){return CODEC;}
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new WeaponAssemblerBlockEntity(pos,state);}
    @Override public <T extends BlockEntity> net.minecraft.world.level.block.entity.BlockEntityTicker<T> getTicker(
            Level level,BlockState state,net.minecraft.world.level.block.entity.BlockEntityType<T> type){
        return level.isClientSide?createTickerHelper(type,
                net.bananashelp20.forgermod.block.entity.ModBlockEntities.WEAPON_ASSEMBLER_BE.get(),
                (world,pos,block,table)->table.clientAnimationTick()):null;
    }
    @Override protected RenderShape getRenderShape(BlockState state){return RenderShape.MODEL;}
    @Override protected InteractionResult useWithoutItem(BlockState state,Level level,BlockPos pos,Player player,BlockHitResult hit){
        if(!level.isClientSide && level.getBlockEntity(pos) instanceof WeaponAssemblerBlockEntity table)
            ((ServerPlayer)player).openMenu(table,pos);
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
    @Override protected void onRemove(BlockState state,Level level,BlockPos pos,BlockState replacement,boolean moving){
        if(!level.isClientSide && !state.is(replacement.getBlock()) && level.getBlockEntity(pos) instanceof WeaponAssemblerBlockEntity table)
            table.drops();
        super.onRemove(state,level,pos,replacement,moving);
    }
}
