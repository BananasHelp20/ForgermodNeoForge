package net.bananashelp20.forgermod.item.custom.abilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** Armed kills place five-second clouds in their source dimension. */
public final class PoisonCloudState<D> {
    public record Cloud(UUID owner, double x, double y, double z, long endTick) {}
    private final Set<UUID> armed = new HashSet<>();
    private final Map<D, List<Cloud>> clouds = new HashMap<>();

    public boolean arm(UUID player) {
        return armed.add(player);
    }

    public boolean createOnKill(UUID player, D dimension, double x, double y, double z, long now) {
        if (!armed.remove(player)) return false;
        clouds.computeIfAbsent(dimension, unused -> new ArrayList<>())
                .add(new Cloud(player, x, y, z, now + 100));
        return true;
    }

    public List<Cloud> active(D dimension, long now) {
        List<Cloud> entries = clouds.get(dimension);
        if (entries == null) return List.of();
        entries.removeIf(cloud -> now >= cloud.endTick);
        if (entries.isEmpty()) {
            clouds.remove(dimension);
            return List.of();
        }
        return List.copyOf(entries);
    }

    public void clearPlayer(UUID player) {
        armed.remove(player);
    }
}
