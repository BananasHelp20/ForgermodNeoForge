package net.bananashelp20.forgermod.item.custom.abilities;

import java.util.UUID;

public final class StrengthenedBonesStateTest {
    public static void main(String[] args) {
        StrengthenedBonesState state = new StrengthenedBonesState();
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        expect(false, state.consumeKill(first, 10), "unarmed kill");
        expect(true, state.arm(first, 10), "arm");
        expect(false, state.arm(first, 11), "cannot rearm");
        expect(false, state.consumeKill(second, 20), "other player unaffected");
        expect(true, state.consumeKill(first, 20), "kill grants protection");
        expect(true, state.isProtected(first, 219), "last protected tick");
        expect(false, state.arm(first, 30), "cannot arm while protected");
        expect(false, state.isProtected(first, 220), "protection expires");
        expect(true, state.arm(first, 220), "can rearm later");
        state.clear(first);
        expect(false, state.consumeKill(first, 221), "logout cleanup");
    }

    private static void expect(boolean expected, boolean actual, String description) {
        if (expected != actual) throw new AssertionError(description + ": expected " + expected + ", got " + actual);
    }
}
