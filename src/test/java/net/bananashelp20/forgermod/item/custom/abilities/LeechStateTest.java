package net.bananashelp20.forgermod.item.custom.abilities;

import java.util.UUID;

public final class LeechStateTest {
    public static void main(String[] args) {
        LeechState state = new LeechState();
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        expect(false, state.hasCharge(first), "inactive");
        expect(true, state.arm(first), "arm ten hits");
        expect(false, state.arm(first), "cannot rearm mid-sequence");
        if (state.availableHealing(first, 100) != 10) throw new AssertionError("Ten HP cap");
        state.recordHealing(first, 4);
        if (state.availableHealing(first, 100) != 6) throw new AssertionError("Shared budget");
        state.recordHealing(first, 100);
        if (state.availableHealing(first, 1) != 0) throw new AssertionError("Exhausted budget");
        if (state.availableHealing(first, Float.NaN) != 0) throw new AssertionError("Nonfinite healing");
        expect(false, state.consume(second), "other player unaffected");
        for (int hit = 1; hit <= 10; hit++) {
            expect(true, state.hasCharge(first), "charge available for hit " + hit);
            expect(true, state.consume(first), "consume hit " + hit);
        }
        expect(false, state.consume(first), "eleventh hit uncharged");
        expect(true, state.arm(first), "can rearm after ten hits");
        if (state.availableHealing(first, 100) != 10) throw new AssertionError("New activation budget");
        state.clear(first);
        expect(false, state.hasCharge(first), "logout cleanup");
    }

    private static void expect(boolean expected, boolean actual, String description) {
        if (expected != actual) throw new AssertionError(description + ": expected " + expected + ", got " + actual);
    }
}
