package net.bananashelp20.forgermod.CreativeModeTabs;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.block.ModBlocks;
import net.bananashelp20.forgermod.item.ModItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModCreativeModeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ForgerMod.MOD_ID);

    public static final Supplier<CreativeModeTab> FORGER_MOD_INGREDIENTS_TAB = CREATIVE_MODE_TABS.register("forger_mod_ingredients_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.INANISIUM_INGOT.get()))
                    .title(Component.translatable("creativetab.forgermod.forger_ingredients_tab"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ModBlocks.REINFORCED_IRON_BLOCK.get());
                        output.accept(ModItems.REINFORCED_IRON_INGOT.get());

                        output.accept(ModBlocks.SCRAP_IRON_BLOCK.get());
                        output.accept(ModItems.SCRAP_IRON_INGOT.get());

                        output.accept(ModBlocks.DAMASK_BLOCK.get());
                        output.accept(ModItems.DAMASK_INGOT.get());

                        output.accept(ModBlocks.SCRAP_BLOCK.get());
                        output.accept(ModItems.SCRAP_INGOT.get());

                        output.accept(ModBlocks.INANISIUM_BLOCK.get());
                        output.accept(ModItems.INANISIUM_INGOT.get());
                        output.accept(ModItems.INANISIUM_SHARD.get());

                        output.accept(ModBlocks.IGNISIUM_BLOCK.get());
                        output.accept(ModItems.IGNISIUM_INGOT.get());
                        output.accept(ModItems.IGNISIUM_SHARD.get());

                        output.accept(ModBlocks.PULSITE_BLOCK.get());
                        output.accept(ModItems.PULSITE_INGOT.get());
                        output.accept(ModItems.PULSITE_SHARD.get());

                        output.accept(ModBlocks.SOMNIUM_BLOCK.get());
                        output.accept(ModItems.SOMNIUM_INGOT.get());
                        output.accept(ModItems.SOMNIUM_SHARD.get());

                        output.accept(ModBlocks.VULNUSIUM_BLOCK.get());
                        output.accept(ModItems.VULNUSIUM_INGOT.get());
                        output.accept(ModItems.VULNUSIUM_SHARD.get());

                        output.accept(ModBlocks.MORSIUM_BLOCK.get());
                        output.accept(ModItems.MORSIUM_INGOT.get());
                        output.accept(ModItems.MORSIUM_SHARD.get());

                        output.accept(ModBlocks.OVERGROWN_BLOCK.get());
                        output.accept(ModItems.LUSH_INGOT.get());
                        output.accept(ModItems.LUSH_SHARD.get());

                        output.accept(ModBlocks.ELECTRIUM_BLOCK.get());
                        output.accept(ModItems.ELECTRIUM_INGOT.get());
                        output.accept(ModItems.ELECTRIUM_SHARD.get());

                        output.accept(ModBlocks.TAIFUNITE_BLOCK.get());
                        output.accept(ModItems.TAIFUNITE_INGOT.get());
                        output.accept(ModItems.TAIFUNITE_SHARD.get());

                        output.accept(ModBlocks.CARBON_STEEL_BLOCK.get());
                        output.accept(ModItems.CARBON_STEEL_INGOT.get());
                        output.accept(ModItems.UNREFINED_CARBON_STEEL.get());

                        output.accept(ModBlocks.STEEL_BLOCK.get());
                        output.accept(ModItems.STEEL_INGOT.get());
                        output.accept(ModItems.UNREFINED_STEEL.get());

                        output.accept(ModBlocks.DEVELOPIUM_BLOCK.get());
                        output.accept(ModItems.DEVELOPIUM_INGOT.get());

                        output.accept(ModItems.RUBY_GEMSTONE.get());
                        output.accept(ModItems.AMBER_GEMSTONE.get());
                        output.accept(ModItems.AMETHYST_GEMSTONE.get());
                        output.accept(ModItems.JADE_GEMSTONE.get());

                    }).build());

    public static final Supplier<CreativeModeTab> FORGER_MOD_WEAPONS_TAB = CREATIVE_MODE_TABS.register("forger_mod_weapons_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.CLAYMORE.get()))
                    .title(Component.translatable("creativetab.forgermod.forger_weapons_tab"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ModItems.SCRAP_IRON_SWORD.get());
                        output.accept(ModItems.REINFORCED_IRON_SWORD.get());
                        output.accept(ModItems.SCRAP_SWORD.get());
                        output.accept(ModItems.DAMASK_KNIFE.get());
                        output.accept(ModItems.DAMASK_SWORD.get());
                        output.accept(ModItems.STEEL_SWORD.get());
                        output.accept(ModItems.CLAYMORE.get());
                        output.accept(ModItems.RUSTY_CLAYMORE.get());
                        output.accept(ModItems.STUMPFL_BAT.get());

                        output.accept(ModItems.INFERNAL_CLAYMORE.get());
                        output.accept(ModItems.INFERNAL_CLAYMORE_RUBY.get());
                        output.accept(ModItems.INFERNAL_CLAYMORE_AMBER.get());
                        output.accept(ModItems.INFERNAL_CLAYMORE_AMETHYST.get());

                        output.accept(ModItems.CLAYMORE_OF_THE_VOID.get());
                        output.accept(ModItems.CLAYMORE_OF_THE_VOID_RUBY.get());
                        output.accept(ModItems.CLAYMORE_OF_THE_VOID_AMBER.get());
                        output.accept(ModItems.CLAYMORE_OF_THE_VOID_AMETHYST.get());

                        output.accept(ModItems.OVERGROWN_CLAYMORE.get());
                        output.accept(ModItems.OVERGROWN_CLAYMORE_RUBY.get());
                        output.accept(ModItems.OVERGROWN_CLAYMORE_AMBER.get());
                        output.accept(ModItems.OVERGROWN_CLAYMORE_AMETHYST.get());

                        output.accept(ModItems.HOLLOW_CLAYMORE.get());
                        output.accept(ModItems.HOLLOW_CLAYMORE_RUBY.get());
                        output.accept(ModItems.HOLLOW_CLAYMORE_AMBER.get());
                        output.accept(ModItems.HOLLOW_CLAYMORE_AMETHYST.get());

                        output.accept(ModItems.CURSEBLOOD_CLAYMORE.get());
                        output.accept(ModItems.CURSEBLOOD_CLAYMORE_RUBY.get());
                        output.accept(ModItems.CURSEBLOOD_CLAYMORE_AMBER.get());
                        output.accept(ModItems.CURSEBLOOD_CLAYMORE_AMETHYST.get());

                        output.accept(ModItems.DREAMBOUND_CLAYMORE.get());
                        output.accept(ModItems.DREAMBOUND_CLAYMORE_RUBY.get());
                        output.accept(ModItems.DREAMBOUND_CLAYMORE_AMBER.get());
                        output.accept(ModItems.DREAMBOUND_CLAYMORE_AMETHYST.get());

                        output.accept(ModItems.SHRIEKING_CLAYMORE.get());
                        output.accept(ModItems.SHRIEKING_CLAYMORE_RUBY.get());
                        output.accept(ModItems.SHRIEKING_CLAYMORE_AMBER.get());
                        output.accept(ModItems.SHRIEKING_CLAYMORE_AMETHYST.get());

                        output.accept(ModItems.CLAYMORE_OF_THUNDER.get());
                        output.accept(ModItems.CLAYMORE_OF_THUNDER_RUBY.get());
                        output.accept(ModItems.CLAYMORE_OF_THUNDER_AMBER.get());
                        output.accept(ModItems.CLAYMORE_OF_THUNDER_AMETHYST.get());

                        output.accept(ModItems.STORMING_CLAYMORE.get());
                        output.accept(ModItems.STORMING_CLAYMORE_RUBY.get());
                        output.accept(ModItems.STORMING_CLAYMORE_AMBER.get());
                        output.accept(ModItems.STORMING_CLAYMORE_AMETHYST.get());

                    }).build());

    public static final Supplier<CreativeModeTab> FORGER_MOD_ITEMS_TAB = CREATIVE_MODE_TABS.register("forger_mod_items_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.ADVANCED_HANDLE.get()))
                    .title(Component.translatable("creativetab.forgermod.forger_items_tab"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ModItems.CARBON_STEEL_CROSS_GUARD.get());
                        output.accept(ModItems.SHARPENED_BLADE.get());
                        output.accept(ModItems.HANDLE.get());
                        output.accept(ModItems.ADVANCED_HANDLE.get());
                        output.accept(ModItems.ANCIENT_UPGRADE_TEMPLATE.get());
                        output.accept(ModItems.GEMSTONE_UPGRADE_TEMPLATE.get());


                    }).build());

    public static final Supplier<CreativeModeTab> FORGER_MOD_MISCELLANEOUS_TAB = CREATIVE_MODE_TABS.register("forger_mod_miscellaneous_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModBlocks.ANCIENT_SWORD_STAND.get()))
                    .title(Component.translatable("creativetab.forgermod.forger_miscellaneous_tab"))
                    .displayItems((itemDisplayParameters, output) -> {
                        output.accept(ModBlocks.ANCIENT_SWORD_STAND.get());
                        output.accept(ModBlocks.FORGE.get());
                        output.accept(ModBlocks.INFUSION_TABLE.get());
                    }).build());

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
}
