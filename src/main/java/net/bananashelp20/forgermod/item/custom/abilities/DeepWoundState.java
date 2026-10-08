package net.bananashelp20.forgermod.item.custom.abilities;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** Keeps Deep Wound armed until a successful critical dagger hit. */
public final class DeepWoundState {
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
}
