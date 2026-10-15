package net.bananashelp20.forgermod.CreativeModeTabs;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.block.ModBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ForgerMod.MOD_ID);

    private static boolean isGemstoneVariant(ItemLike item) {
        String id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item.asItem()).getPath();
        return id.endsWith("_ruby") || id.endsWith("_amber")
                || id.endsWith("_amethyst") || id.endsWith("_jade");
    }

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
    //!GENERATE

    public static final Supplier<CreativeModeTab> FORGER_MOD_INGREDIENTS_TAB = CREATIVE_MODE_TABS.register("forger_mod_ingredients_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(RegistryClass.getDisplayItemForForgerIngredientsTab()))
                    .title(Component.translatable("creativetab.forgermod.forger_ingredients_tab"))
                    .displayItems((itemDisplayParameters, output) -> {
                        ItemLike[] ingredientTabRegister = RegistryClass.getIngredientTabRegister();
                        for (int i = 0; i < ingredientTabRegister.length; i++) {
                            output.accept(ingredientTabRegister[i]);
                        }
                        output.accept(ModItems.RAW_METEORIC_IRON.get());
                        output.accept(ModItems.METEORIC_IRON_INGOT.get());
                        output.accept(ModItems.RAW_TITANIUM.get());
                        output.accept(ModItems.TITANIUM_INGOT.get());
                        output.accept(ModItems.RAW_TUNGSTEN.get());
                        output.accept(ModItems.TUNGSTEN_INGOT.get());
                        output.accept(ModItems.DOLOMITE.get());
                        output.accept(ModItems.MAGNESIUM.get());
                        output.accept(ModItems.TITANIUM_SPONGE.get());
                        output.accept(ModItems.TUNGSTEN_POWDER.get());
                        output.accept(ModItems.REINFORCED_IRON_BILLET.get());
                        output.accept(ModItems.DAMASK_BILLET.get());
                    }).build());

    public static final Supplier<CreativeModeTab> FORGER_MOD_WEAPONS_TAB = CREATIVE_MODE_TABS.register("forger_mod_weapons_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(RegistryClass.getDisplayItemForForgerWeaponsTab()))
                    .title(Component.translatable("creativetab.forgermod.forger_weapons_tab"))
                    .displayItems((itemDisplayParameters, output) -> {
                        ItemLike[] register = RegistryClass.getWeaponTabRegister();
                        for (int i = 0; i < register.length; i++) {
                            if (!isGemstoneVariant(register[i])) output.accept(register[i]);
                        }
                        for (var entry : ModItems.TOOL_SETS.entrySet()) {
                            var set = entry.getValue();
                            output.accept(set.pickaxe().get());
                            output.accept(set.axe().get());
                            output.accept(set.hoe().get());
                            if (!java.util.Set.of("scrap", "reinforced_iron", "steel", "damask").contains(entry.getKey())) {
                                output.accept(set.sword().get());
                            }
                        }
                    }).build()
    );

    public static final Supplier<CreativeModeTab> FORGER_MOD_ITEMS_TAB = CREATIVE_MODE_TABS.register("forger_mod_items_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(RegistryClass.getDisplayItemForForgerItemsTab()))
                    .title(Component.translatable("creativetab.forgermod.forger_items_tab"))
                    .displayItems((itemDisplayParameters, output) -> {
                        ItemLike[] register = RegistryClass.getItemTabRegister();
                        for (int i = 0; i < register.length; i++) {
                            if (!isGemstoneVariant(register[i])) output.accept(register[i]);
                        }
                    }).build()
    );

    public static final Supplier<CreativeModeTab> FORGER_MOD_MISCELLANEOUS_TAB = CREATIVE_MODE_TABS.register("forger_mod_miscellaneous_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(RegistryClass.getDisplayItemForForgerMiscellaneousTab()))
                    .title(Component.translatable("creativetab.forgermod.forger_miscellaneous_tab"))
                    .displayItems((itemDisplayParameters, output) -> {
                        ItemLike[] register = RegistryClass.getMiscellaneousTabRegister();
                        for (int i = 0; i < register.length; i++) {
                            if (!isGemstoneVariant(register[i])) output.accept(register[i]);
                        }
                    }).build()
    );

    public static final Supplier<CreativeModeTab> FORGER_MOD_BLOCKS_TAB = CREATIVE_MODE_TABS.register("forger_mod_blocks_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(RegistryClass.getDisplayItemForForgerBlocksTab()))
                    .title(Component.translatable("creativetab.forgermod.forger_blocks_tab"))
                    .displayItems((itemDisplayParameters, output) -> {
                        ItemLike[] register = RegistryClass.getBlocksTabRegister();
                        for (int i = 0; i < register.length; i++) {
                            if (!isGemstoneVariant(register[i])) output.accept(register[i]);
                        }
                        output.accept(ModBlocks.METEORIC_IRON_STONE_ORE.get());
                        output.accept(ModBlocks.METEORIC_IRON_END_ORE.get());
                        output.accept(ModBlocks.TITANIUM_STONE_ORE.get());
                        output.accept(ModBlocks.TITANIUM_DEEPSLATE_ORE.get());
                        output.accept(ModBlocks.TUNGSTEN_STONE_ORE.get());
                        output.accept(ModBlocks.TUNGSTEN_DEEPSLATE_ORE.get());
                        output.accept(ModBlocks.DOLOMITE_STONE_ORE.get());
                    }).build()
    );
}
