package net.bananashelp20.forgermod.item.custom.abilities;

import java.util.UUID;

public final class FlamingComboStateTest {
    public static void main(String[] args) {
        FlamingComboState state = new FlamingComboState();
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();

        expect(0, state.fireTicksAfterHit(first, 100, 0), "inactive hit");
        state.activate(first, 100);
        expect(20, state.fireTicksAfterHit(first, 100, 0), "first hit ignites");
        expect(40, state.fireTicksAfterHit(first, 101, 20), "following hit extends burn");
        expect(80, state.fireTicksAfterHit(first, 299, 60), "last active tick");
        expect(60, state.fireTicksAfterHit(first, 300, 60), "exact expiry");
        expect(0, state.fireTicksAfterHit(second, 150, 0), "separate player");

        state.activate(first, 400);
        state.clear(first);
        expect(0, state.fireTicksAfterHit(first, 401, 0), "logout clears window");
    }

    private static void expect(int expected, int actual, String caseName) {
        if (expected != actual) {
            throw new AssertionError(caseName + ": expected " + expected + ", got " + actual);
        }
    }
}
