package net.bananashelp20.forgermod.item.custom.abilities;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Matches a critical-hit event with the successful item hit later in the same attack. */
public final class DaggerCriticalTracker {
    private record Pending(int targetId, long tick, Object dagger) {}
    private final Map<UUID, Pending> pending = new HashMap<>();

    public void mark(UUID player, int targetId, long tick, Object dagger) {
        pending.put(player, new Pending(targetId, tick, dagger));
    }

    public boolean consume(UUID player, int targetId, long tick, Object dagger) {
        Pending critical = pending.remove(player);
        return critical != null && critical.targetId() == targetId
                && critical.tick() == tick && critical.dagger() == dagger;
    }

    public void clear(UUID player) {
        pending.remove(player);
    }
}
