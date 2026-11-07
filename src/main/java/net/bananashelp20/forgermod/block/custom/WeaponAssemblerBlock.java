package net.bananashelp20.forgermod.block.custom;

import com.mojang.serialization.MapCodec;
import net.bananashelp20.forgermod.block.entity.custom.WeaponAssemblerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public final class WeaponAssemblerBlock extends BaseEntityBlock {
    public static final MapCodec<WeaponAssemblerBlock> CODEC=simpleCodec(WeaponAssemblerBlock::new);
    public WeaponAssemblerBlock(Properties properties){super(properties);}
    @Override protected MapCodec<? extends BaseEntityBlock> codec(){return CODEC;}
    @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){return new WeaponAssemblerBlockEntity(pos,state);}
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
