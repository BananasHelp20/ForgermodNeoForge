package net.bananashelp20.forgermod.item.custom.abilities;

public final class DeathMarchDamageTest {
    public static void main(String[] args) {
        for(int rank=1;rank<=5;rank++) {
            float bonus=.5F*rank;
            expect(1,DeathMarchDamage.multiplier(20,20,rank),"full health");
            expect(1+bonus/2,DeathMarchDamage.multiplier(10,20,rank),"half health");
            expect(1+bonus,DeathMarchDamage.multiplier(0,20,rank),"zero health cap");
            expect(1+bonus,DeathMarchDamage.multiplier(-10,20,rank),"negative health cap");
            expect(1,DeathMarchDamage.multiplier(30,20,rank),"overhealth cap");
            expect(1,DeathMarchDamage.multiplier(1,0,rank),"invalid maximum health");
        }
        expect(1.5F,DeathMarchDamage.multiplier(0,20,0),"minimum rank");
        expect(3.5F,DeathMarchDamage.multiplier(0,20,99),"maximum rank");
        expect(1.25F,DeathMarchDamage.multiplier(10,20),"default rank I");
    }

    private static void expect(float expected, float actual, String description) {
        if (Float.compare(expected, actual) != 0) {
            throw new AssertionError(description + ": expected " + expected + ", got " + actual);
        }
    }
}
