package net.bananashelp20.forgermod.item.custom.abilities;

import java.util.UUID;

public final class SonicBoomStateTest {
    public static void main(String[] args) {
        SonicBoomState state = new SonicBoomState();
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        expect(false, state.isArmed(first), "inactive");
        expect(true, state.arm(first), "arm six attacks");
        expect(false, state.arm(first), "cannot rearm mid-sequence");
        expect(false, state.consumeAttack(second), "other player unaffected");
        for (int hit = 1; hit <= 6; hit++) {
            expect(true, state.isArmed(first), "charge for attack " + hit);
            expect(true, state.consumeAttack(first), "consume attack " + hit);
        }
        expect(false, state.isArmed(first), "sixth attack ends effect");
        expect(false, state.consumeAttack(first), "seventh attack uncharged");
        expect(true, state.arm(first), "can rearm");
        state.clear(first);
        expect(false, state.isArmed(first), "logout cleanup");
    }

    private static void expect(boolean expected, boolean actual, String description) {
        if (expected != actual) throw new AssertionError(description + ": expected " + expected + ", got " + actual);
    }
}
