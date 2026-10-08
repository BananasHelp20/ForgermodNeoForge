package net.bananashelp20.forgermod.item.custom.abilities;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Horizontal anchors expire after ten seconds; vertical gravity remains free. */
public final class RootingRootsState {
    public record Anchor(double x, double z) {}
    private record Root(UUID owner, Anchor anchor, long endTick) {}
    private final Map<UUID, Root> roots = new HashMap<>();

    public void root(UUID owner, UUID mob, double x, double z, long now) {
        roots.put(mob, new Root(owner, new Anchor(x, z), now + 200));
    }

    public Anchor anchor(UUID mob, long now) {
        Root root = roots.get(mob);
        if (root == null) return null;
        if (now < root.endTick) return root.anchor;
        roots.remove(mob);
        return null;
    }

    public void prune(long now) {
        roots.values().removeIf(root -> now >= root.endTick);
    }

    public boolean hasRoots(UUID owner, long now) {
        prune(now);
        return roots.values().stream().anyMatch(root -> root.owner.equals(owner));
    }

    public void clearOwner(UUID owner) {
        roots.values().removeIf(root -> root.owner.equals(owner));
    }
}
