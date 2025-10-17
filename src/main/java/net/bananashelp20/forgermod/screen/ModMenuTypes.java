package net.bananashelp20.forgermod.screen;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.screen.custom.ForgeMenu;
import net.bananashelp20.forgermod.screen.custom.InfusionTableMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, ForgerMod.MOD_ID);

    public static final Supplier<MenuType<ForgeMenu>> FORGE_MENU = registerMenuType("forge_menu", ForgeMenu::new);
    public static final Supplier<MenuType<InfusionTableMenu>> INFUSION_TABLE_MENU = registerMenuType("infusion_table_menu", InfusionTableMenu::new);

    private static <T extends AbstractContainerMenu>DeferredHolder<MenuType<?>, MenuType<T>> registerMenuType(String name, IContainerFactory<T> factory) {
        return MENUS.register(name, () -> IMenuTypeExtension.create(factory));
    }

    public static void register(IEventBus eventBus) {
        MENUS.register(eventBus);
    }
}
