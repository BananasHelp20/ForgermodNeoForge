package net.bananashelp20.forgermod.item.custom.abilities;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Counts successful mob hits; the twenty-first hit discharges the stored charge. */
public final class ChargeAttackState {
    private static final int CHARGING_HITS = 20;
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

    public void clear(UUID player) {
        hits.remove(player);
    }
}
