package net.bananashelp20.forgermod.item.custom.abilities;

import java.util.UUID;

public final class PoisonCloudStateTest {
    public static void main(String[] args) {
        PoisonCloudState<String> state = new PoisonCloudState<>();
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        expect(false, state.createOnKill(first, "overworld", 1, 2, 3, 10), "unarmed kill");
        expect(true, state.arm(first), "arm");
        expect(false, state.arm(first), "cannot rearm");
        expect(false, state.createOnKill(second, "overworld", 1, 2, 3, 10), "other player unaffected");
        expect(true, state.createOnKill(first, "overworld", 1, 2, 3, 10), "armed kill makes cloud");
        expect(true, state.isActive(first, 10), "cloud remains active after kill");
        expect(1, state.active("overworld", 109).size(), "cloud active through last tick");
        expect(0, state.active("nether", 109).size(), "dimension isolation");
        expect(0, state.active("overworld", 110).size(), "cloud expired");
        expect(false, state.createOnKill(first, "overworld", 1, 2, 3, 110), "charge consumed");
        expect(true, state.arm(first), "rearm");
        state.clearPlayer(first);
        expect(false, state.createOnKill(first, "overworld", 1, 2, 3, 110), "logout cleanup");
        expect(true, state.arm(first), "arm again");
        expect(true, state.createOnKill(first, "overworld", 1, 2, 3, 120), "second cloud");
        state.cancelPlayer(first);
        expect(false, state.isActive(first, 120), "switching weapons removes cloud");
    }

    private static void expect(Object expected, Object actual, String description) {
        if (!expected.equals(actual)) throw new AssertionError(description + ": expected " + expected + ", got " + actual);
    }
}
