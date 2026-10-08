package net.bananashelp20.forgermod.item.custom.abilities;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Tracks the Somnium dagger's enabled state and critical-hit sequence. */
public final class NightmareCritState {
    private final Map<UUID, Integer> criticalCounts = new HashMap<>();

    /** Returns true if the ability is enabled after toggling. */
    public boolean toggle(UUID player) {
        if (criticalCounts.remove(player) != null) return false;
        criticalCounts.put(player, 0);
        return true;
    }

    /** Returns true on every third successful critical hit while enabled. */
    public boolean onCritical(UUID player) {
        Integer count = criticalCounts.get(player);
        if (count == null) return false;
        int next = count + 1;
        criticalCounts.put(player, next == 3 ? 0 : next);
        return next == 3;
    }

    public void clear(UUID player) {
        criticalCounts.remove(player);
    }
}
