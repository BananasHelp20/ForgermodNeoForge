package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.item.custom.WeaponAbilitySlot;

public final class WeaponCooldownKeyTest {
    public static void main(String[] args) {
        String daggerPrimary = WeaponCooldownKey.of(String.class, true, false, WeaponAbilitySlot.PRIMARY);
        expect(daggerPrimary, WeaponCooldownKey.of(String.class, true, false, WeaponAbilitySlot.PRIMARY),
                "same material, type, and slot share a deadline");
        reject(daggerPrimary, WeaponCooldownKey.of(String.class, true, false, WeaponAbilitySlot.SECONDARY),
                "ability slots remain separate");
        reject(daggerPrimary, WeaponCooldownKey.of(String.class, false, true, WeaponAbilitySlot.PRIMARY),
                "weapon types remain separate");
        reject(daggerPrimary, WeaponCooldownKey.of(Integer.class, true, false, WeaponAbilitySlot.PRIMARY),
                "materials remain separate");
    }

    private static void expect(String expected, String actual, String description) {
        if (!expected.equals(actual)) throw new AssertionError(description);
    }

    private static void reject(String unexpected, String actual, String description) {
        if (unexpected.equals(actual)) throw new AssertionError(description);
    }
}
