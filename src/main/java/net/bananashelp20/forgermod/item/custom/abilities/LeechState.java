package net.bananashelp20.forgermod.item.custom.abilities;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Ten successful drains consume one Leech activation. */
public final class LeechState {
    private final Map<UUID, Integer> remaining = new HashMap<>();

    public boolean arm(UUID player) {
        return remaining.putIfAbsent(player, 10) == null;
    }

    public boolean hasCharge(UUID player) {
        return remaining.containsKey(player);
    }

    public boolean consume(UUID player) {
        Integer count = remaining.get(player);
        if (count == null) return false;
        if (count == 1) remaining.remove(player);
        else remaining.put(player, count - 1);
        return true;
    }

    public void clear(UUID player) {
        remaining.remove(player);
    }
}
