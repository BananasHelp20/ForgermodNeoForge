package net.bananashelp20.forgermod.item.custom.abilities;

import java.util.UUID;

public final class NightmareCritStateTest {
    public static void main(String[] args) {
        NightmareCritState state = new NightmareCritState();
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();

        expect(false, state.onCritical(first), "disabled ability");
        expect(true, state.toggle(first), "enabled after first press");
        expect(false, state.onCritical(first), "first critical");
        expect(false, state.onCritical(first), "second critical");
        expect(true, state.onCritical(first), "third critical triggers");
        expect(false, state.onCritical(first), "sequence repeats from one");
        expect(false, state.onCritical(second), "other player remains disabled");
        expect(false, state.toggle(first), "second press disables");
        expect(true, state.toggle(first), "reenabling resets count");
        expect(false, state.onCritical(first), "fresh first critical");
        state.clear(first);
        expect(false, state.onCritical(first), "logout clears ability");
    }

    private static void expect(boolean expected, boolean actual, String caseName) {
        if (expected != actual) throw new AssertionError(caseName + ": expected " + expected + ", got " + actual);
    }
}
