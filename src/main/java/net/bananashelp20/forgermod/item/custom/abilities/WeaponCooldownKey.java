package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.item.custom.WeaponAbilitySlot;

/** Stable key shared by gemstone variants of the same material and weapon type. */
public final class WeaponCooldownKey {
    private WeaponCooldownKey() {}

    public static String of(Class<?> material, boolean dagger, boolean axe, WeaponAbilitySlot slot) {
        String type = dagger ? "dagger" : axe ? "axe" : "claymore";
        return material.getName() + ":" + type + ":" + slot.name();
    }
}
