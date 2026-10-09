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
        return areMatchingDaggers(player.getMainHandItem(), player.getOffhandItem());
    }

    public static boolean areMatchingDaggers(ItemStack mainhand, ItemStack offhand) {
        if (!isDagger(mainhand) || !isDagger(offhand)) return false;
        if (mainhand.getItem() instanceof SwordItemWithEffect main
                && offhand.getItem() instanceof SwordItemWithEffect off) {
            // Each special material has its own shared Tier; gemstones do not change it.
            return main.getTier() == off.getTier();
        }
        // The ordinary Rusty Dagger has no gemstone variants or material weapon class.
        return mainhand.is(offhand.getItem());
    }
}
