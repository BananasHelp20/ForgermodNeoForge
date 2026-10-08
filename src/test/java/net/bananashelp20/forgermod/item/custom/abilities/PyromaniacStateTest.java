package net.bananashelp20.forgermod.item.custom.abilities;

import java.util.UUID;

public final class PyromaniacStateTest {
    public static void main(String[] args) {
        PyromaniacState state = new PyromaniacState();
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();

        expect(false, state.arm(first, false), "activation requires burning");
        expect(false, state.consume(first), "failed activation does not arm a hit");
        expect(true, state.arm(first, true), "burning activation succeeds");
        expect(false, state.arm(first, true), "cannot waste cooldown by rearming");
        expect(false, state.consume(second), "other player cannot consume activation");
        expect(true, state.consume(first), "next hit consumes activation");
        expect(false, state.consume(first), "later hits do not trigger it again");

        state.arm(first, true);
        state.clear(first);
        expect(false, state.consume(first), "logout clears armed hit");
    }

    private static void expect(boolean expected, boolean actual, String caseName) {
        if (expected != actual) {
            throw new AssertionError(caseName + ": expected " + expected + ", got " + actual);
        }
    }
}
