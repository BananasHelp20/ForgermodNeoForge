package net.bananashelp20.forgermod.item.custom.abilities;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Ten successful hits share a ten-health-point healing budget. */
public final class LeechState {
    private final Map<UUID, Integer> remaining = new HashMap<>();
    private final Map<UUID, Float> healed = new HashMap<>();

    public boolean arm(UUID player) {
        if (remaining.putIfAbsent(player, 10) != null) return false;
        healed.put(player, 0f);
        return true;
    }

    public float availableHealing(UUID player, float requested) {
        if (!hasCharge(player) || !Float.isFinite(requested)) return 0;
        return Math.max(0, Math.min(requested, 10 - healed.getOrDefault(player, 0f)));
    }

    public void recordHealing(UUID player, float restored) {
        if (hasCharge(player)) healed.put(player, healed.getOrDefault(player, 0f) + availableHealing(player, restored));
    }

    public boolean hasCharge(UUID player) {
        return remaining.containsKey(player);
    }

    public boolean consume(UUID player) {
        Integer count = remaining.get(player);
        if (count == null) return false;
        if (count == 1) clear(player);
        else remaining.put(player, count - 1);
        return true;
    }

    public void clear(UUID player) {
        remaining.remove(player);
        healed.remove(player);
    }
}
