package net.bananashelp20.forgermod.item.custom.abilities;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** One armed hit grants a five-second fall-damage reduction. */
public final class LucidDreamingState {
    private static final long DURATION_TICKS = 100;
    private final Set<UUID> armed = new HashSet<>();
    private final Map<UUID, Long> protectedUntil = new HashMap<>();

    public boolean arm(UUID player) {
        return armed.add(player);
    }

    public boolean consumeHit(UUID player, long now) {
        if (!armed.remove(player)) return false;
        protectedUntil.put(player, now + DURATION_TICKS);
        return true;
    }

    public boolean reducesFallDamage(UUID player, long now) {
        Long end = protectedUntil.get(player);
        if (end == null) return false;
        if (now < end) return true;
        protectedUntil.remove(player);
        return false;
    }

    public void clear(UUID player) {
        armed.remove(player);
        protectedUntil.remove(player);
    }
}
