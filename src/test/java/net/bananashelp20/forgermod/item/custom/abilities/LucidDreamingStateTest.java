package net.bananashelp20.forgermod.item.custom.abilities;

import java.util.UUID;

public final class LucidDreamingStateTest {
    public static void main(String[] args) {
        LucidDreamingState state = new LucidDreamingState();
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();

        expect(false, state.consumeHit(first, 100), "unarmed hit");
        expect(true, state.arm(first), "first activation");
        expect(false, state.arm(first), "already armed");
        expect(false, state.consumeHit(second, 100), "other player cannot consume it");
        expect(true, state.consumeHit(first, 100), "armed hit grants buff");
        expect(false, state.consumeHit(first, 101), "one hit per activation");
        expect(true, state.reducesFallDamage(first, 199), "last protected tick");
        expect(false, state.reducesFallDamage(first, 200), "exact expiry");
        state.arm(first);
        state.clear(first);
        expect(false, state.consumeHit(first, 201), "logout clears armed hit");
        expect(false, state.reducesFallDamage(first, 201), "logout clears buff");
    }

    private static void expect(boolean expected, boolean actual, String caseName) {
        if (expected != actual) throw new AssertionError(caseName + ": expected " + expected + ", got " + actual);
    }
}
