package net.bananashelp20.forgermod.item.custom.abilities;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** One successful critical hit consumes the sonic charge. */
public final class SonicCritState {
    private final Set<UUID> armed = new HashSet<>();

    public boolean arm(UUID player) {
        return armed.add(player);
    }

    public boolean isArmed(UUID player) {
        return armed.contains(player);
    }

    public boolean consume(UUID player) {
        return armed.remove(player);
    }

    public void clear(UUID player) {
        armed.remove(player);
    }

    public static float bonusDamage(float targetMaxHealth) {
        return Math.max(0, targetMaxHealth) * 0.15F;
    }
}
