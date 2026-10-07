package net.bananashelp20.forgermod.item.custom.attacks.axe;

import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.SwordItemWithEffect;
import net.minecraft.world.item.ItemStack;

public final class AxeItems {
    private AxeItems() {}

    public static boolean isAxe(ItemStack stack) {
        return stack.getItem() instanceof SwordItemWithEffect weapon && weapon.isAxe()
                || stack.is(ModItems.CARBON_STEEL_AXE)
                || stack.is(ModItems.RUSTY_AXE);
    }
}
