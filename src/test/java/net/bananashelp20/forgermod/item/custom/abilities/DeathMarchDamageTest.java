package net.bananashelp20.forgermod.item.custom.abilities;

public final class DeathMarchDamageTest {
    public static void main(String[] args) {
        expect(1, DeathMarchDamage.multiplier(20, 20), "full health");
        expect(1.5F, DeathMarchDamage.multiplier(10, 20), "half health");
        expect(2, DeathMarchDamage.multiplier(0, 20), "zero health cap");
        expect(2, DeathMarchDamage.multiplier(-10, 20), "negative health cap");
        expect(1, DeathMarchDamage.multiplier(30, 20), "overhealth cap");
        expect(1, DeathMarchDamage.multiplier(1, 0), "invalid maximum health");
    }

    private static void expect(float expected, float actual, String description) {
        if (Float.compare(expected, actual) != 0) {
            throw new AssertionError(description + ": expected " + expected + ", got " + actual);
        }
    }
}
