package net.bananashelp20.forgermod.item.custom.abilities;

public final class RevengeDamageTest {
    public static void main(String[] args) {
        for(int rank=1;rank<=5;rank++) {
            expect(1+6*rank/100F, RevengeDamage.multiplier(true,rank), "undead bonus at rank "+rank);
            expect(1.0F, RevengeDamage.multiplier(false,rank), "other target unaffected");
        }
        expect(1.06F,RevengeDamage.multiplier(true,-1),"minimum clamp");
        expect(1.3F,RevengeDamage.multiplier(true,6),"maximum clamp");
    }

    private static void expect(float expected, float actual, String description) {
        if (Float.compare(expected, actual) != 0) {
            throw new AssertionError(description + ": expected " + expected + ", got " + actual);
        }
    }
}
