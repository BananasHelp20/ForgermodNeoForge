package net.bananashelp20.forgermod.CreativeModeTabs;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.RegistryClass;
import net.bananashelp20.forgermod.block.ModBlocks;
import net.bananashelp20.forgermod.item.ModItems;
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

    public static final Supplier<CreativeModeTab> FORGER_MOD_INGREDIENTS_TAB = CREATIVE_MODE_TABS.register("forger_mod_ingredients_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(RegistryClass.getDisplayItemForForgerIngredientsTab()))
                    .title(Component.translatable("creativetab.forgermod.forger_ingredients_tab"))
                    .displayItems((itemDisplayParameters, output) -> {
                        ItemLike[] ingredientTabRegister = RegistryClass.getIngredientTabRegister();
                        for (int i = 0; i < ingredientTabRegister.length; i++) {
                            output.accept(ingredientTabRegister[i]);
                        }
                    }).build());

    public static final Supplier<CreativeModeTab> FORGER_MOD_WEAPONS_TAB = CREATIVE_MODE_TABS.register("forger_mod_weapons_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(RegistryClass.getDisplayItemForForgerWeaponsTab()))
                    .title(Component.translatable("creativetab.forgermod.forger_weapons_tab"))
                    .displayItems((itemDisplayParameters, output) -> {
                        ItemLike[] weaponTabRegister = RegistryClass.getWeaponTabRegister();
                        for (int i = 0; i < weaponTabRegister.length; i++) {
                            output.accept(weaponTabRegister[i]);
                        }
                    }).build());

    public static final Supplier<CreativeModeTab> FORGER_MOD_ITEMS_TAB = CREATIVE_MODE_TABS.register("forger_mod_items_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(RegistryClass.getDisplayItemForForgerItemsTab()))
                    .title(Component.translatable("creativetab.forgermod.forger_items_tab"))
                    .displayItems((itemDisplayParameters, output) -> {
                        ItemLike[] itemTabRegister = RegistryClass.getItemTabRegister();
                        for (int i = 0; i < itemTabRegister.length; i++) {
                            output.accept(itemTabRegister[i]);
                        }
                    }).build());

    public static final Supplier<CreativeModeTab> FORGER_MOD_MISCELLANEOUS_TAB = CREATIVE_MODE_TABS.register("forger_mod_miscellaneous_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(RegistryClass.getDisplayItemForForgerMiscellaneousTab()))
                    .title(Component.translatable("creativetab.forgermod.forger_miscellaneous_tab"))
                    .displayItems((itemDisplayParameters, output) -> {
                        ItemLike[] miscellaneousTabRegister = RegistryClass.getMiscellaneousTabRegister();
                        for (int i = 0; i < miscellaneousTabRegister.length; i++) {
                            output.accept(miscellaneousTabRegister[i]);
                        }
                    }).build());

    public static final Supplier<CreativeModeTab> FORGER_MOD_BLOCKS_TAB = CREATIVE_MODE_TABS.register("forger_mod_blocks_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(RegistryClass.getDisplayItemForForgerBlocksTab()))
                    .title(Component.translatable("creativetab.forgermod.forger_blocks_tab"))
                    .displayItems((itemDisplayParameters, output) -> {
                        ItemLike[] blocksTabRegister = RegistryClass.getBlocksTabRegister();
                        for (int i = 0; i < blocksTabRegister.length; i++) {
                            output.accept(blocksTabRegister[i]);
                        }
                    }).build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}