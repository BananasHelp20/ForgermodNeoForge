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
        expect(false, state.consume(second), "other player unaffected");
        for (int hit = 1; hit <= 10; hit++) {
            expect(true, state.hasCharge(first), "charge available for hit " + hit);
            expect(true, state.consume(first), "consume hit " + hit);
        }
        expect(false, state.consume(first), "eleventh hit uncharged");
        expect(true, state.arm(first), "can rearm after ten hits");
        state.clear(first);
        expect(false, state.hasCharge(first), "logout cleanup");
    }

    private static void expect(boolean expected, boolean actual, String description) {
        if (expected != actual) throw new AssertionError(description + ": expected " + expected + ", got " + actual);
    }
}
