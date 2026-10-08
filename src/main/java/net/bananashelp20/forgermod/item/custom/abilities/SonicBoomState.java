package net.bananashelp20.forgermod.item.custom.abilities;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Six dagger attacks, including air swings, consume one extended-reach activation. */
public final class SonicBoomState {
    private final Map<UUID, Integer> remaining = new HashMap<>();

    public boolean arm(UUID player) {
        return remaining.putIfAbsent(player, 6) == null;
    }

    public boolean isArmed(UUID player) {
        return remaining.containsKey(player);
    }

    public boolean consumeAttack(UUID player) {
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
