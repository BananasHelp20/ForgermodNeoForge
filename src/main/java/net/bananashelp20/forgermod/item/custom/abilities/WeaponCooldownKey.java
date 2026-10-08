package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.item.custom.WeaponAbilitySlot;

/** Each registered weapon and ability slot has its own cooldown. */
public final class WeaponCooldownKey {
    private WeaponCooldownKey() {}

    public static String of(String itemId, WeaponAbilitySlot slot) {
        return itemId + ":" + slot.name();
    }
}
