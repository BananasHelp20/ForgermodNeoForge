package net.bananashelp20.forgermod.block.custom;
import com.mojang.serialization.MapCodec;
import net.bananashelp20.forgermod.item.ModItems;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
public final class AncientAxeStandBlock extends AncientRelicDisplayBlock {
    public static final MapCodec<AncientAxeStandBlock> CODEC=simpleCodec(AncientAxeStandBlock::new);
    public AncientAxeStandBlock(Properties properties){super(properties,ModItems.RUSTY_AXE,true);}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
}
