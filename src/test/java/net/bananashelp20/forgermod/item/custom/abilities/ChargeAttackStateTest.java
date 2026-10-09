package net.bananashelp20.forgermod.item.custom.abilities;

import java.util.UUID;

public final class ChargeAttackStateTest {
    public static void main(String[] args) {
        ChargeAttackState state = new ChargeAttackState();
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();

        expect(false, state.recordMobHit(first), "inactive hit");
        expect(true, state.arm(first), "activation");
        expect(false, state.arm(first), "cannot rearm mid-charge");
        expect(false, state.recordMobHit(second), "another player cannot use charge");
        for (int hit = 1; hit <= 20; hit++) {
            expectFloat(1.0F + hit * 0.05F, state.damageMultiplier(first), "charging multiplier " + hit);
            expect(false, state.recordMobHit(first), "charging hit " + hit);
        }
        expectFloat(1.0F, state.damageMultiplier(first), "discharge hit is not ramped");
        expect(true, state.recordMobHit(first), "twenty-first hit discharges");
        expect(false, state.recordMobHit(first), "charge is consumed");
        expect(true, state.arm(first), "can rearm after discharge");
        state.clear(first);
        expect(false, state.recordMobHit(first), "logout clears charge");
    }

    private static void expect(boolean expected, boolean actual, String description) {
        if (expected != actual) throw new AssertionError(description + ": expected " + expected + ", got " + actual);
    }

    private static void expectFloat(float expected, float actual, String description) {
        if (Math.abs(expected - actual) > 0.0001F) {
            throw new AssertionError(description + ": expected " + expected + ", got " + actual);
        }
    }
}
