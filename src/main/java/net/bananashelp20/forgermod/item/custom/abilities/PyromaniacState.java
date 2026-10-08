package net.bananashelp20.forgermod.item.custom.abilities;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/** One successful dagger hit may consume each activation. */
public final class PyromaniacState {
    private final Set<UUID> armedPlayers = new HashSet<>();

    public boolean arm(UUID playerId, boolean burning) {
        if (!burning || armedPlayers.contains(playerId)) return false;
        armedPlayers.add(playerId);
        return true;
    }

    public boolean consume(UUID playerId) {
        return armedPlayers.remove(playerId);
    }

    public boolean isArmed(UUID playerId) {
        return armedPlayers.contains(playerId);
    }

    public void clear(UUID playerId) {
        armedPlayers.remove(playerId);
    }
}
