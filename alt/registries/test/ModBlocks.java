package net.bananashelp20.forgermod.registries.test;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.block.custom.AncientSwordStandBlock;
import net.bananashelp20.forgermod.item.ModItems;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.Supplier;

public class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ForgerMod.MOD_ID);

    public static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static DeferredBlock<Block> createSimpleBlock(String name, float destroyTime, int explosionResistance, SoundType sound) {
        return registerBlock(name, () -> new Block(BlockBehaviour.Properties.of()
                        .strength(destroyTime, explosionResistance)
                        .sound(sound)
                        .requiresCorrectToolForDrops()
                )
        );
    }

    public static DeferredBlock<Block> createBlockWithDescription(String name, float destroyTime, int explosionTime, SoundType sound, String descriptionName) {
        return registerBlock(name,
                () -> new Block(BlockBehaviour.Properties.of()
                        .strength(destroyTime, explosionTime)
                        .sound(sound)
                        .requiresCorrectToolForDrops()) {
                    @Override
                    public void appendHoverText(ItemStack pStack, Item.TooltipContext pContext, List<Component> pTooltipComponents, TooltipFlag pTooltipFlag) {
                        pTooltipComponents.add(Component.translatable(descriptionName));
                        super.appendHoverText(pStack, pContext, pTooltipComponents, pTooltipFlag);
                    }
                }
        );
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }

    //STARTGENERATING!

    //Simple Blocks
    public static final DeferredBlock<Block> NEWBLOCK = createSimpleBlock("newblock", 77f, 50, SoundType.HEAVY_CORE);
    public static final DeferredBlock<Block> NEWBLOCKADFDSF = createSimpleBlock("newblockadfdsf", 77f, 50, SoundType.HEAVY_CORE);
    public static final DeferredBlock<Block> NEWBLOCKASDJF = createSimpleBlock("newblockasdjf", 77f, 50, SoundType.HEAVY_CORE);
    public static final DeferredBlock<Block> NEWBLOCKABHASD = createSimpleBlock("newblockabhasd", 77f, 50, SoundType.HEAVY_CORE);
    public static final DeferredBlock<Block> NEWBLOCK3 = createSimpleBlock("newblock3", 77f, 50, SoundType.HEAVY_CORE);
    public static final DeferredBlock<Block> ABCBLOCK = createSimpleBlock("abcblock", 77f, 50, SoundType.HEAVY_CORE);
    public static final DeferredBlock<Block> NEWBLOCKADSF = createSimpleBlock("newblockadsf", 77f, 50, SoundType.HEAVY_CORE);
    public static final DeferredBlock<Block> NEWBLOCKADSFd = createSimpleBlock("newblockadsf", 77f, 50, SoundType.HEAVY_CORE);
    public static final DeferredBlock<Block> SIMPLEBLOCKTEST = createSimpleBlock("simpleblocktest", 77f, 50, SoundType.HEAVY_CORE);

    //Special Blocks
    public static final DeferredBlock<Block> NEWSPECIALBLOCK = createBlockWithDescription("newspecialblock", 50f, 100, SoundType.HEAVY_CORE, "tooltips.forgermod.newSpecialBlock.tooltip");
    public static final DeferredBlock<Block> SPECIALBLOCKTEST = createBlockWithDescription("specialblocktest", 50f, 100, SoundType.HEAVY_CORE, "tooltips.forgermod.newSpecialBlock.tooltip");

    //Complex Blocks
    public static final DeferredBlock<Block> NEWCOMPLEXBLOCK = registerBlock("newcomplexblock",
            () -> new AncientSwordStandBlock(BlockBehaviour.Properties.of().noOcclusion().strength(45f, 50).sound(SoundType.NETHERITE_BLOCK)));
    public static final DeferredBlock<Block> NEWCOMPLEXBLOCK2 = registerBlock("newcomplexblock2",
            () -> new AncientSwordStandBlock(BlockBehaviour.Properties.of().noOcclusion().strength(45f, 50).sound(SoundType.NETHERITE_BLOCK)));
    public static final DeferredBlock<Block> COMPLEXTEST = registerBlock("complextest",
            () -> new AncientSwordStandBlock(BlockBehaviour.Properties.of().noOcclusion().strength(45f, 50).sound(SoundType.NETHERITE_BLOCK)));

}