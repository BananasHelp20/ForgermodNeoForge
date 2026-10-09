package net.bananashelp20.forgermod.item.custom.abilities;

import java.util.UUID;

public final class LucidDreamingStateTest {
    public static void main(String[] args) {
        LucidDreamingState state = new LucidDreamingState();
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();

        expect(false, state.consumeKill(first, 100), "unarmed kill");
        expect(true, state.arm(first), "first activation");
        expect(false, state.arm(first), "already armed");
        expect(false, state.consumeKill(second, 100), "other player cannot consume it");
        expect(true, state.consumeKill(first, 100), "armed kill grants buff");
        expect(false, state.consumeKill(first, 101), "one kill per activation");
        expect(true, state.reducesFallDamage(first, 399), "last protected tick");
        expect(false, state.reducesFallDamage(first, 400), "exact expiry");
        state.arm(first);
        state.clear(first);
        expect(false, state.consumeKill(first, 401), "logout clears armed kill");
        expect(false, state.reducesFallDamage(first, 401), "logout clears buff");
    }

    private static void expect(boolean expected, boolean actual, String caseName) {
        if (expected != actual) throw new AssertionError(caseName + ": expected " + expected + ", got " + actual);
    }
}
