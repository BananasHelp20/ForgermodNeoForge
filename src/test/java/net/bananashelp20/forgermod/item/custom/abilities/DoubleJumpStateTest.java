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
        state.normalJump(first,8);
        for(int i=0;i<8;i++)expect(true,state.consume(first),"Eight-jump dual Windwalker budget");
        expect(false,state.consume(first),"Ninth air jump rejected");
        state.normalJump(first,99);
        for(int i=0;i<8;i++)expect(true,state.consume(first),"Budget clamped to eight");
        expect(false,state.consume(first),"Budget exceeds eight");
        state.normalJump(first,-1);expect(false,state.consume(first),"Negative budget grants jumps");
    }

    private static void expect(boolean expected, boolean actual, String description) {
        if (expected != actual) throw new AssertionError(description + ": expected " + expected + ", got " + actual);
    }
}
