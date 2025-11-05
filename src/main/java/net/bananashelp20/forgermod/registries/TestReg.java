package net.bananashelp20.forgermod.registries;

import net.bananashelp20.forgermod.block.custom.AncientSwordStandBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;

import static net.bananashelp20.forgermod.block.ModBlocks.*;

public class TestReg {
    public static void main(String[] args) {
        System.out.println();
    }

    //STARTGENERATING!

    //Simple Blocks
    public static final DeferredBlock<Block> NEWBLOCK = createSimpleBlock("newblock", 77f, 50, SoundType.HEAVY_CORE);
    public static final DeferredBlock<Block> 77F, 50, SOUNDTYPE.HEAVY_CORE = createSimpleBlock("77f, 50, soundtype.heavy_core", dropSelf);
    public static final DeferredBlock<Block> SAPHIRE_GEMSTONE = createSimpleBlock("saphire_gemstone", blockWithItem);
    public static final DeferredBlock<Block> 50F, 100, SOUNDTYPE.HEAVY_CORE, "TOOLTIPS.FORGERMOD.NEWSPECIALBLOCK.TOOLTIP" = createSimpleBlock("50f, 100, soundtype.heavy_core, "tooltips.forgermod.newspecialblock.tooltip"", dropSelf);
    public static final DeferredBlock<Block> NEW ANCIENTSWORDSTANDBLOCK(BLOCKBEHAVIOUR.PROPERTIES.OF().NOOCCLUSION().STRENGTH(45F, 50).SOUND(SOUNDTYPE.NETHERITE_BLOCK)) = createSimpleBlock("new ancientswordstandblock(blockbehaviour.properties.of().noocclusion().strength(45f, 50).sound(soundtype.netherite_block))", dropSelf);

    //Special Blocks
    public static final DeferredBlock<Block> NEWSPECIALBLOCK = createBlockWithDescription("newspecialblock", 50f, 100, SoundType.HEAVY_CORE, "tooltips.forgermod.newSpecialBlock.tooltip");
    public static final DeferredBlock<Block> COMPLEX { = newComplexBlock("complex {", new AncientSwordStandBlock(BlockBehaviour.Properties.of().noOcclusion().strength(45f, 50).sound(SoundType.NETHERITE_BLOCK)));
    //Complex Blocks

}