package net.bananashelp20.forgermod.item.custom.abilities;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Horizontal anchors expire after ten seconds; vertical gravity remains free. */
public final class RootingRootsState {
    public record Anchor(double x, double z) {}
    private record Root(Anchor anchor, long endTick) {}
    private final Map<UUID, Root> roots = new HashMap<>();

    public void root(UUID mob, double x, double z, long now) {
        roots.put(mob, new Root(new Anchor(x, z), now + 200));
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
}
