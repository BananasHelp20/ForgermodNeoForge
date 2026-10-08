package net.bananashelp20.forgermod.item.custom.abilities;

import java.util.UUID;

public final class StormStateTest {
    public static void main(String[] args) {
        StormState state = new StormState();
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        expect(false, state.isActive(first, 0), "inactive player");
        expect(true, state.activate(first, 10), "activate");
        expect(false, state.activate(first, 20), "cannot reactivate while active");
        expect(false, state.isActive(second, 20), "other player unaffected");
        expect(true, state.isActive(first, 109), "last active tick");
        expect(false, state.isActive(first, 110), "expires after five seconds");
        expect(true, state.activate(first, 110), "reactivate after expiry");
        state.clear(first);
        expect(false, state.isActive(first, 111), "logout cleanup");
    }

    private static void expect(boolean expected, boolean actual, String description) {
        if (expected != actual) throw new AssertionError(description + ": expected " + expected + ", got " + actual);
    }
}
