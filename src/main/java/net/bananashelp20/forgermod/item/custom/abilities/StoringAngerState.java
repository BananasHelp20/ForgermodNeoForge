package net.bananashelp20.forgermod.item.custom.abilities;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Accumulates outgoing damage for ten seconds, then releases it once. */
public final class StoringAngerState {
    private record Charge(long readyTick, float storedDamage) {}
    private final Map<UUID, Charge> charges = new HashMap<>();

    public boolean arm(UUID player, long now) {
        return charges.putIfAbsent(player, new Charge(now + 200, 0)) == null;
    }

    public boolean storeIfCharging(UUID player, long now, float amount) {
        Charge charge = charges.get(player);
        if (charge == null || now >= charge.readyTick) return false;
        charges.put(player, new Charge(charge.readyTick, charge.storedDamage + Math.max(0, amount)));
        return true;
    }

    public float releaseOnDaggerHit(UUID player, long now) {
        Charge charge = charges.get(player);
        if (charge == null || now < charge.readyTick) return -1;
        charges.remove(player);
        return charge.storedDamage;
    }

    public void clear(UUID player) {
        charges.remove(player);
    }
}
