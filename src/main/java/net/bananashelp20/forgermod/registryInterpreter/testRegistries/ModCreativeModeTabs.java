package net.bananashelp20.forgermod.registryInterpreter.testRegistries;

import net.bananashelp20.forgermod.ForgerMod;
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

    public static void register(IEventBus eventBus) {
        CREATIVE_MODE_TABS.register(eventBus);
    }
    //!GENERATE

    public static final Supplier<CreativeModeTab> FORGER_MOD_ITEMS_TAB = CREATIVE_MODE_TABS.register("forger_mod_items_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(RegistryClass.getDisplayItemForForgerModItemsTab()))
                    .title(Component.translatable("creativetab.forgermod.forger_mod_items_tab"))
                    .displayItems((itemDisplayParameters, output) -> {
                        ItemLike[] register = RegistryClass.getForgerModItemsTabRegister();
                        for (int i = 0; i < register.length; i++) {
                            output.accept(register[i]);
                        }
                    }).build()
    );

}