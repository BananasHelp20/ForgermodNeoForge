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
        for(int rank=1;rank<=5;rank++) {
            expect(true,state.arm(first,rank),"ranked activation");
            int count=ChargeAttackState.chargingHits(rank);
            for(int hit=1;hit<=count;hit++) {
                expectFloat(1+hit*ChargeAttackState.maximumBonus(rank)/count,state.damageMultiplier(first),"ranked multiplier");
                expect(false,state.recordMobHit(first),"not discharged before final charge");
            }
            expectFloat(1,state.damageMultiplier(first),"discharge is not ramped");
            expect(true,state.recordMobHit(first),"ranked discharge");
            expect(false,state.isArmed(first),"ranked charge consumed");
        }
        expectFloat(3,1+ChargeAttackState.maximumBonus(4),"maximum +200%");
        if(ChargeAttackState.chargingHits(4)!=15 || ChargeAttackState.lightningDamage(4)!=30)throw new AssertionError("Maximum must discharge on hit 16 for 30 lightning");
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
