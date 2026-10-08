package net.bananashelp20.forgermod.item.custom.abilities;

import java.util.UUID;

public final class RootingRootsStateTest {
    public static void main(String[] args) {
        RootingRootsState state = new RootingRootsState();
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        UUID owner = UUID.randomUUID();
        expect(null, state.anchor(first, 0), "not rooted");
        state.root(owner, first, 2, 4, 10);
        expect(new RootingRootsState.Anchor(2, 4), state.anchor(first, 209), "anchored until last tick");
        expect(null, state.anchor(second, 20), "other mob unaffected");
        expect(null, state.anchor(first, 210), "expires at ten seconds");
        state.root(owner, first, 3, 5, 220);
        state.root(owner, first, 6, 7, 240);
        expect(new RootingRootsState.Anchor(6, 7), state.anchor(first, 430), "reapplication replaces anchor");
        expect(true, state.hasRoots(owner, 430), "owner has an active root");
        state.clearOwner(owner);
        expect(null, state.anchor(first, 430), "switching weapons releases owner's roots");
        state.root(owner, first, 6, 7, 240);
        state.prune(440);
        expect(null, state.anchor(first, 440), "prune removes expired root");
    }

    private static void expect(Object expected, Object actual, String description) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError(description + ": expected " + expected + ", got " + actual);
        }
    }
}
