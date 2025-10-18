package net.bananashelp20.forgermod.block;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.block.custom.AncientSwordStandBlock;
import net.bananashelp20.forgermod.block.custom.ForgeBlock;
import net.bananashelp20.forgermod.block.custom.InfusionTableBlock;
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

    public static DeferredBlock<Block> createSpecialBlock(String name, float destroyTime, int explosionResistance, SoundType sound) {
        return registerBlock(name,
                () -> new AncientSwordStandBlock(BlockBehaviour.Properties.of()
                        .noOcclusion()
                        .strength(destroyTime, explosionResistance)
                        .sound(sound)
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

    //simple blocks
    public static final DeferredBlock<Block> DAMASK_BLOCK = createSimpleBlock("damask_block", 77f, 50, SoundType.HEAVY_CORE);
    public static final DeferredBlock<Block> PULSITE_BLOCK = createSimpleBlock("pulsite_block", 77f, 50, SoundType.HEAVY_CORE);
    public static final DeferredBlock<Block> SCRAP_BLOCK = createSimpleBlock("scrap_block", 60f, 50, SoundType.NETHERITE_BLOCK);
    public static final DeferredBlock<Block> INANISIUM_BLOCK = createSimpleBlock("inanisium_block", 77f, 50, SoundType.AMETHYST);
    public static final DeferredBlock<Block> IGNISIUM_BLOCK = createSimpleBlock("ignisium_block", 77f, 50, SoundType.GILDED_BLACKSTONE);
    public static final DeferredBlock<Block> REINFORCED_IRON_BLOCK = createSimpleBlock("reinforced_iron_block", 55f, 50, SoundType.METAL);
    public static final DeferredBlock<Block> SCRAP_IRON_BLOCK = createSimpleBlock("scrap_iron_block", 45f, 50, SoundType.METAL);
    public static final DeferredBlock<Block> OVERGROWN_BLOCK = createSimpleBlock("overgrown_block", 45f, 50, SoundType.METAL);
    public static final DeferredBlock<Block> MORSIUM_BLOCK = createSimpleBlock("morsium_block", 45f, 50, SoundType.METAL);
    public static final DeferredBlock<Block> SOMNIUM_BLOCK = createSimpleBlock("somnium_block", 45f, 50, SoundType.METAL);
    public static final DeferredBlock<Block> VULNUSIUM_BLOCK = createSimpleBlock("vulnusium_block", 45f, 50, SoundType.METAL);
    public static final DeferredBlock<Block> CARBON_STEEL_BLOCK = createSimpleBlock("carbon_steel_block", 45f, 50, SoundType.METAL);
    public static final DeferredBlock<Block> STEEL_BLOCK = createSimpleBlock("steel_block", 45f, 50, SoundType.METAL);
    public static final DeferredBlock<Block> ELECTRIUM_BLOCK = createSimpleBlock("electrium_block", 45f, 50, SoundType.METAL);
    public static final DeferredBlock<Block> TAIFUNITE_BLOCK = createSimpleBlock("taifunite_block", 45f, 50, SoundType.METAL);

    public static final DeferredBlock<Block> DEVELOPIUM_BLOCK = createBlockWithDescription("developium_block", 50f, 100, SoundType.HEAVY_CORE, "tooltips.forgermod.developium_block.tooltip");

    //non-block blocks and special blocks
    public static final DeferredBlock<Block> ANCIENT_SWORD_STAND = registerBlock("ancient_sword_stand",
                () -> new AncientSwordStandBlock(BlockBehaviour.Properties.of()
                        .noOcclusion()
                        .strength(45f, 50)
                        .sound(SoundType.NETHERITE_BLOCK)
                )
    );

    public static final DeferredBlock<Block> FORGE = registerBlock("forge",
            () -> new ForgeBlock(BlockBehaviour.Properties.of()
                    .strength(45f, 50)
                    .sound(SoundType.NETHERITE_BLOCK)
                    .lightLevel(state -> state.getValue(ForgeBlock.LIT) ? 10 : 0)
            )
    );

    public static final DeferredBlock<Block> INFUSION_TABLE = registerBlock("infusion_table",
            () -> new InfusionTableBlock(BlockBehaviour.Properties.of()
                    .strength(45f, 50)
                    .sound(SoundType.NETHERITE_BLOCK)
            )
    );

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }

}
