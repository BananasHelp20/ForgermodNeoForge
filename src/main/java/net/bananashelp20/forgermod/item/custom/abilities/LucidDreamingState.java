package net.bananashelp20.forgermod.item.custom.abilities;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** One armed kill grants the same protection window as its Speed buff. */
public final class LucidDreamingState {
    private static final int DURATION_TICKS = 600;
    private final Set<UUID> armed = new HashSet<>();
    private final Map<UUID, Long> protectedUntil = new HashMap<>();

    public boolean arm(UUID player) {
        return armed.add(player);
    }

    public boolean consumeKill(UUID player, long now) {
        return consumeKill(player,now,DURATION_TICKS);
    }

    public boolean consumeKill(UUID player,long now,int duration) {
        if (!armed.remove(player)) return false;
        protectedUntil.put(player, now + Math.max(1,duration));
        return true;
    }

    public boolean reducesFallDamage(UUID player, long now) {
        Long end = protectedUntil.get(player);
        if (end == null) return false;
        if (now < end) return true;
        protectedUntil.remove(player);
        return false;
    }

    public boolean isActive(UUID player, long now) {
        return armed.contains(player) || reducesFallDamage(player, now);
    }

    public void clear(UUID player) {
        armed.remove(player);
        protectedUntil.remove(player);
    }
}
