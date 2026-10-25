package net.bananashelp20.forgermod.item.custom.abilities;

import java.util.UUID;

public final class SonicCritStateTest {
    public static void main(String[] args) {
        SonicCritState state = new SonicCritState();
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        expect(false, state.isArmed(first), "inactive");
        expect(true, state.arm(first), "arm");
        expect(false, state.arm(first), "cannot rearm");
        expect(false, state.isArmed(second), "other player unaffected");
        expect(true, state.isArmed(first), "normal hit leaves charge");
        expect(true, state.consume(first), "critical consumes charge");
        expect(false, state.consume(first), "only one charged critical");
        expect(1.0F, SonicCritState.bonusDamage(20), "five percent base bonus");
        for(int rank=1;rank<=4;rank++)expect((float)rank,SonicCritState.bonusDamage(20,rank),"reviewed rank bonus");
        expect(4.0F,SonicCritState.bonusDamage(20,5),"extra template rank respects twenty percent cap");
        expect(4.0F,SonicCritState.bonusDamage(20,Integer.MAX_VALUE),"large rank respects cap");
        expect(0.0F, SonicCritState.bonusDamage(-1), "invalid health does not reverse damage");
        state.arm(first);
        state.clear(first);
        expect(false, state.isArmed(first), "logout cleanup");
    }

    private static void expect(boolean expected, boolean actual, String description) {
        if (expected != actual) throw new AssertionError(description + ": expected " + expected + ", got " + actual);
    }

    private static void expect(float expected, float actual, String description) {
        if (Float.compare(expected, actual) != 0) {
            throw new AssertionError(description + ": expected " + expected + ", got " + actual);
        }
    }
}
