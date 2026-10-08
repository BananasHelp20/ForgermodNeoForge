package net.bananashelp20.forgermod.item.custom.abilities;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Two successful dagger hits consume one Absolute Nightmare activation. */
public final class NightmareHitState {
    private final Map<UUID, Integer> remainingHits = new HashMap<>();

    public boolean arm(UUID player) {
        if (remainingHits.containsKey(player)) return false;
        remainingHits.put(player, 2);
        return true;
    }

    public boolean consumeHit(UUID player) {
        Integer remaining = remainingHits.get(player);
        if (remaining == null) return false;
        if (remaining == 1) remainingHits.remove(player);
        else remainingHits.put(player, remaining - 1);
        return true;
    }

    public void clear(UUID player) {
        remainingHits.remove(player);
    }
}
