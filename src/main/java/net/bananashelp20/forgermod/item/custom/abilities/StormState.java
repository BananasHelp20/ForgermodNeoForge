package net.bananashelp20.forgermod.item.custom.abilities;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Five-second Eye of the Storm window, tracked separately for each player. */
public final class StormState {
    private static final long DURATION_TICKS = 100;
    private final Map<UUID, Long> activeUntil = new HashMap<>();

    public boolean activate(UUID player, long now) {
        if (isActive(player, now)) return false;
        activeUntil.put(player, now + DURATION_TICKS);
        return true;
    }

    public boolean isActive(UUID player, long now) {
        Long end = activeUntil.get(player);
        if (end == null) return false;
        if (now < end) return true;
        activeUntil.remove(player);
        return false;
    }

    public void clear(UUID player) {
        activeUntil.remove(player);
    }
}
