package net.bananashelp20.forgermod.item.custom.abilities;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** One extra jump is available after a normal jump, until landing. */
public final class DoubleJumpState {
    private final Set<UUID> ready = new HashSet<>();

    public void normalJump(UUID player) {
        ready.add(player);
    }

    public boolean consume(UUID player) {
        return ready.remove(player);
    }

    public void clear(UUID player) {
        ready.remove(player);
    }
}
