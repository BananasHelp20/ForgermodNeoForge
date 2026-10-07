package net.bananashelp20.forgermod.item.custom.attacks.dagger;

import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.SwordItemWithEffect;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public final class DaggerItems {
    private DaggerItems() {}

    public static boolean isDagger(ItemStack stack) {
        return stack.getItem() instanceof SwordItemWithEffect weapon && weapon.isDagger()
                || stack.is(ModItems.RUSTY_DAGGER);
    }

    public static boolean hasMatchingDaggers(Player player) {
        ItemStack mainhand = player.getMainHandItem();
        return isDagger(mainhand) && player.getOffhandItem().is(mainhand.getItem());
    }
}
