package net.bananashelp20.forgermod.augmentation;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** Hunger points, not saturation; shared by gameplay and upgrade previews. */
public final class EchoAppetiteValues {
    private EchoAppetiteValues(){}
    public static int multiplier(int rank){return Math.clamp(rank,0,2);}
    public static int wardenFood(int rank){return 4*multiplier(rank);}
    public static int blockFood(BlockState state,int rank){
        int base=state.is(Blocks.SCULK)?2:state.is(Blocks.SCULK_VEIN)?1:state.is(Blocks.SCULK_SENSOR)?3
                :state.is(Blocks.CALIBRATED_SCULK_SENSOR)?4:state.is(Blocks.SCULK_CATALYST)?6:state.is(Blocks.SCULK_SHRIEKER)?4:0;
        return base*multiplier(rank);
    }
}
