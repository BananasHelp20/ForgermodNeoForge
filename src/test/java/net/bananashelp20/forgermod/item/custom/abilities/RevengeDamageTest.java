package net.bananashelp20.forgermod.item.custom.abilities;

public final class RevengeDamageTest {
    public static void main(String[] args) {
        expect(1.2F, RevengeDamage.multiplier(true), "undead bonus");
        expect(1.0F, RevengeDamage.multiplier(false), "other target unaffected");
    }

    private static void expect(float expected, float actual, String description) {
        if (Float.compare(expected, actual) != 0) {
            throw new AssertionError(description + ": expected " + expected + ", got " + actual);
        }
    }
}
