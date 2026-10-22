package net.bananashelp20.forgermod.item.custom.abilities;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Eye of the Storm window, tracked separately for each player. */
public final class StormState {
    private static final long DURATION_TICKS = 100;
    private final Map<UUID, Long> activeUntil = new HashMap<>();

    public boolean activate(UUID player, long now) {
        return activate(player,now,(int)DURATION_TICKS);
    }
    public boolean activate(UUID player,long now,int duration) {
        if(duration<=0)return false;
        if (isActive(player, now)) return false;
        activeUntil.put(player, now + duration);
        return true;
    }
    public int remainingTicks(UUID player,long now) {
        return isActive(player,now)?(int)Math.min(Integer.MAX_VALUE,activeUntil.get(player)-now):0;
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
