package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** Explicitly learns the abilities needed by existing combat regressions; production gear starts empty. */
final class TestWeapons {
    private TestWeapons() {}
    static ItemStack ready(Item item) {
        ItemStack stack = new ItemStack(item);
        if (!Augmentations.eligible(stack)) return stack;
        var canonical = Augmentations.canonical(stack);
        stack = Augmentations.apply(stack, Augmentations.EMPOWERED_HIT);
        for (var slot : net.bananashelp20.forgermod.item.custom.WeaponAbilitySlot.values()) {
            String id = canonical.abilityDescriptionKey(slot);
            if (id != null) stack = Augmentations.apply(stack, id);
        }
        for (String id : canonical.passiveDescriptionKeys()) stack = Augmentations.apply(stack, id);
        return stack;
    }
}
