package net.bananashelp20.forgermod.item.custom.abilities;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Counts successful mob hits; the twenty-first hit discharges the stored charge. */
public final class ChargeAttackState {
    private static final int CHARGING_HITS = 20;
    private static final float DAMAGE_STEP = 0.05F;
    private final Map<UUID, Integer> hits = new HashMap<>();

    public boolean arm(UUID player) {
        return hits.putIfAbsent(player, 0) == null;
    }

    public boolean recordMobHit(UUID player) {
        Integer count = hits.get(player);
        if (count == null) return false;
        if (count == CHARGING_HITS) {
            hits.remove(player);
            return true;
        }
        hits.put(player, count + 1);
        return false;
    }

    /** Returns the multiplier for the next successful hit while this charge is armed. */
    public float damageMultiplier(UUID player) {
        Integer count = hits.get(player);
        if (count == null || count >= CHARGING_HITS) return 1.0F;
        return 1.0F + (count + 1) * DAMAGE_STEP;
    }

    public boolean isArmed(UUID player) {
        return hits.containsKey(player);
    }

    public void clear(UUID player) {
        hits.remove(player);
    }
}
