package net.bananashelp20.forgermod.item.custom.abilities;

import java.util.UUID;

public final class SonicBoomStateTest {
    public static void main(String[] args) {
        SonicBoomState state = new SonicBoomState();
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        expect(false, state.isArmed(first), "inactive");
        expect(true, state.arm(first), "arm six hits");
        expect(false, state.arm(first), "cannot rearm mid-sequence");
        expect(false, state.consumeHit(second), "other player unaffected");
        for (int hit = 1; hit <= 6; hit++) {
            expect(true, state.isArmed(first), "charge for hit " + hit);
            expect(true, state.consumeHit(first), "consume hit " + hit);
        }
        expect(false, state.isArmed(first), "sixth hit ends effect");
        expect(false, state.consumeHit(first), "seventh hit uncharged");
        expect(true, state.arm(first), "can rearm");
        state.clear(first);
        expect(false, state.isArmed(first), "logout cleanup");
    }

    private static void expect(boolean expected, boolean actual, String description) {
        if (expected != actual) throw new AssertionError(description + ": expected " + expected + ", got " + actual);
    }
}
