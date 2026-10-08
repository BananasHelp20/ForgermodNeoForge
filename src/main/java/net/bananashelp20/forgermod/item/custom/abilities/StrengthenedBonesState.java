package net.bananashelp20.forgermod.item.custom.abilities;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** One armed hostile kill grants ten seconds of invulnerability. */
public final class StrengthenedBonesState {
    private final Set<UUID> armed = new HashSet<>();
    private final Map<UUID, Long> protectedUntil = new HashMap<>();

    public boolean arm(UUID player, long now) {
        return !isProtected(player, now) && armed.add(player);
    }

    public boolean consumeKill(UUID player, long now) {
        if (!armed.remove(player)) return false;
        protectedUntil.put(player, now + 200);
        return true;
    }

    public boolean isProtected(UUID player, long now) {
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
