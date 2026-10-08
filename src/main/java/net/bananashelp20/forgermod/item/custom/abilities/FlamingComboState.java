package net.bananashelp20.forgermod.item.custom.abilities;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Server-side active windows for the Ignisium dagger's Flaming Combo. */
public final class FlamingComboState {
    private static final long DURATION_TICKS = 200;
    private static final int FIRE_PER_HIT_TICKS = 20;
    private final Map<UUID, Long> endTicks = new HashMap<>();

    public void activate(UUID playerId, long now) {
        endTicks.put(playerId, now + DURATION_TICKS);
    }

    public int fireTicksAfterHit(UUID playerId, long now, int currentFireTicks) {
        Long end = endTicks.get(playerId);
        if (end == null) return currentFireTicks;
        if (now >= end) {
            endTicks.remove(playerId);
            return currentFireTicks;
        }
        return Math.max(0, currentFireTicks) + FIRE_PER_HIT_TICKS;
    }

    public void clear(UUID playerId) {
        endTicks.remove(playerId);
    }
}
