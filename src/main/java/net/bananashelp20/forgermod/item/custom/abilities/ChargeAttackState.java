package net.bananashelp20.forgermod.item.custom.abilities;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Counts successful mob hits using the rank captured when the charge is armed. */
public final class ChargeAttackState {
    private record Charge(int rank,int hits) {}
    private final Map<UUID, Charge> hits = new HashMap<>();

    public static int chargingHits(int rank) {return new int[]{20,18,17,15}[Math.clamp(rank-1,0,3)];}
    public static float maximumBonus(int rank) {return 1F+(Math.clamp(rank,1,4)-1)/3F;}
    public static float lightningDamage(int rank) {return new int[]{20,23,27,30}[Math.clamp(rank-1,0,3)];}
    public int rank(UUID player) {var charge=hits.get(player);return charge==null?0:charge.rank();}

    public boolean arm(UUID player) {
        return arm(player,1);
    }
    public boolean arm(UUID player,int rank) {
        return hits.putIfAbsent(player, new Charge(Math.clamp(rank,1,4),0)) == null;
    }

    public boolean recordMobHit(UUID player) {
        Charge charge = hits.get(player);
        if (charge == null) return false;
        if (charge.hits() == chargingHits(charge.rank())) {
            hits.remove(player);
            return true;
        }
        hits.put(player, new Charge(charge.rank(),charge.hits()+1));
        return false;
    }

    /** Returns the multiplier for the next successful hit while this charge is armed. */
    public float damageMultiplier(UUID player) {
        Charge charge = hits.get(player);
        if (charge == null || charge.hits() >= chargingHits(charge.rank())) return 1.0F;
        return 1.0F + (charge.hits()+1)*maximumBonus(charge.rank())/chargingHits(charge.rank());
    }

    public boolean isArmed(UUID player) {
        return hits.containsKey(player);
    }

    public void clear(UUID player) {
        hits.remove(player);
    }
}
