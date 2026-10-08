package net.bananashelp20.forgermod.item.custom.abilities;

import java.util.UUID;

public final class StoringAngerStateTest {
    public static void main(String[] args) {
        StoringAngerState state = new StoringAngerState();
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        expect(false, state.storeIfCharging(first, 10, 4), "inactive player");
        expect(true, state.arm(first, 10), "arm");
        expect(false, state.arm(first, 11), "cannot rearm");
        expect(true, state.storeIfCharging(first, 20, 4), "first held hit");
        expect(true, state.storeIfCharging(first, 209, 6), "last held hit");
        expect(false, state.storeIfCharging(second, 20, 3), "other player unaffected");
        expect(-1F, state.releaseOnDaggerHit(first, 209), "too early to release");
        expect(false, state.storeIfCharging(first, 210, 3), "charging ends after ten seconds");
        expect(10F, state.releaseOnDaggerHit(first, 210), "release all stored damage");
        expect(-1F, state.releaseOnDaggerHit(first, 211), "release only once");
        expect(true, state.arm(first, 220), "can rearm");
        state.clear(first);
        expect(-1F, state.releaseOnDaggerHit(first, 500), "logout cleanup");
    }

    private static void expect(boolean expected, boolean actual, String description) {
        if (expected != actual) throw new AssertionError(description + ": expected " + expected + ", got " + actual);
    }

    private static void expect(float expected, float actual, String description) {
        if (Float.compare(expected, actual) != 0) {
            throw new AssertionError(description + ": expected " + expected + ", got " + actual);
        }
    }
}
