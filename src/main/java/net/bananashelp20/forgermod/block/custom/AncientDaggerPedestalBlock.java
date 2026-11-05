package net.bananashelp20.forgermod.block.custom;
import com.mojang.serialization.MapCodec;
import net.bananashelp20.forgermod.item.ModItems;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
public final class AncientDaggerPedestalBlock extends AncientRelicDisplayBlock {
    public static final MapCodec<AncientDaggerPedestalBlock> CODEC=simpleCodec(AncientDaggerPedestalBlock::new);
    public AncientDaggerPedestalBlock(Properties properties){super(properties,ModItems.RUSTY_DAGGER,false);}
    @Override protected MapCodec<? extends HorizontalDirectionalBlock> codec(){return CODEC;}
}
