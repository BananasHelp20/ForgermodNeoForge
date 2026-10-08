package net.bananashelp20.forgermod.item.custom.abilities;

import java.util.UUID;

public final class DaggerCriticalTrackerTest {
    public static void main(String[] args) {
        DaggerCriticalTracker tracker = new DaggerCriticalTracker();
        UUID player = UUID.randomUUID();
        Object dagger = new Object();
        Object other = new Object();

        tracker.mark(player, 17, 100, dagger);
        expect(false, tracker.consume(player, 18, 100, dagger), "different target");
        tracker.mark(player, 17, 100, dagger);
        expect(false, tracker.consume(player, 17, 101, dagger), "later attack tick");
        tracker.mark(player, 17, 100, dagger);
        expect(false, tracker.consume(player, 17, 100, other), "different dagger");
        tracker.mark(player, 17, 100, dagger);
        expect(true, tracker.consume(player, 17, 100, dagger), "matching successful critical");
        expect(false, tracker.consume(player, 17, 100, dagger), "one-time consumption");
        tracker.mark(player, 17, 100, dagger);
        tracker.clear(player);
        expect(false, tracker.consume(player, 17, 100, dagger), "logout cleanup");
    }

    private static void expect(boolean expected, boolean actual, String caseName) {
        if (expected != actual) throw new AssertionError(caseName + ": expected " + expected + ", got " + actual);
    }
}
