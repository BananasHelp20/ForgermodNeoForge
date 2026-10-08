package net.bananashelp20.forgermod.item.custom.abilities;

import java.util.UUID;

public final class DeepWoundStateTest {
    public static void main(String[] args) {
        DeepWoundState state = new DeepWoundState();
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        expect(false, state.isArmed(first), "inactive");
        expect(true, state.arm(first), "arm");
        expect(false, state.arm(first), "cannot rearm");
        expect(false, state.isArmed(second), "other player unaffected");
        expect(true, state.isArmed(first), "noncritical hits leave charge");
        expect(true, state.consume(first), "critical hit consumes charge");
        expect(false, state.consume(first), "only one critical hit");
        expect(true, state.arm(first), "rearm after use");
        state.clear(first);
        expect(false, state.isArmed(first), "logout cleanup");
    }

    private static void expect(boolean expected, boolean actual, String description) {
        if (expected != actual) throw new AssertionError(description + ": expected " + expected + ", got " + actual);
    }
}
