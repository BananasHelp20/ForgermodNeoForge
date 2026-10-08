package net.bananashelp20.forgermod.item.custom.abilities;

import java.util.UUID;

public final class NightmareHitStateTest {
    public static void main(String[] args) {
        NightmareHitState state = new NightmareHitState();
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();

        expect(false, state.consumeHit(first), "inactive hit");
        expect(true, state.arm(first), "activation arms two hits");
        expect(false, state.arm(first), "cannot rearm while charges remain");
        expect(false, state.consumeHit(second), "other player has no charges");
        expect(true, state.consumeHit(first), "first charged hit");
        expect(true, state.consumeHit(first), "second charged hit");
        expect(false, state.consumeHit(first), "third hit has no charge");
        expect(true, state.arm(first), "can rearm after charges are spent");
        state.clear(first);
        expect(false, state.consumeHit(first), "logout clears charges");
    }

    private static void expect(boolean expected, boolean actual, String caseName) {
        if (expected != actual) throw new AssertionError(caseName + ": expected " + expected + ", got " + actual);
    }
}
