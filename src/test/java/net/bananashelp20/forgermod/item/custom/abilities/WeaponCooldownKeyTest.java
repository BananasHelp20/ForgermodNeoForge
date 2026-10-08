package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.item.custom.WeaponAbilitySlot;

public final class WeaponCooldownKeyTest {
    public static void main(String[] args) {
        String daggerPrimary = WeaponCooldownKey.of("forgermod:dagger_of_the_void", WeaponAbilitySlot.PRIMARY);
        expect(daggerPrimary, WeaponCooldownKey.of("forgermod:dagger_of_the_void", WeaponAbilitySlot.PRIMARY),
                "same registered weapon and slot share a deadline");
        reject(daggerPrimary, WeaponCooldownKey.of("forgermod:dagger_of_the_void", WeaponAbilitySlot.SECONDARY),
                "ability slots remain separate");
        reject(daggerPrimary, WeaponCooldownKey.of("forgermod:dagger_of_the_void_ruby", WeaponAbilitySlot.PRIMARY),
                "gemstone variants remain separate");
        reject(daggerPrimary, WeaponCooldownKey.of("forgermod:axe_of_the_void", WeaponAbilitySlot.PRIMARY),
                "weapon types remain separate");
    }

    private static void expect(String expected, String actual, String description) {
        if (!expected.equals(actual)) throw new AssertionError(description);
    }

    private static void reject(String unexpected, String actual, String description) {
        if (unexpected.equals(actual)) throw new AssertionError(description);
    }
}
