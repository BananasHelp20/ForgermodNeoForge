package net.bananashelp20.forgermod.item.custom.abilities;

import java.util.UUID;

public final class DoubleJumpStateTest {
    public static void main(String[] args) {
        DoubleJumpState state = new DoubleJumpState();
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        expect(false, state.consume(first), "cannot jump before normal jump");
        state.normalJump(first);
        expect(false, state.consume(second), "another player cannot use jump");
        expect(true, state.consume(first), "one extra jump");
        expect(false, state.consume(first), "third jump rejected");
        state.normalJump(first);
        state.clear(first);
        expect(false, state.consume(first), "landing or logout clears jump");
    }

    private static void expect(boolean expected, boolean actual, String description) {
        if (expected != actual) throw new AssertionError(description + ": expected " + expected + ", got " + actual);
    }
}
