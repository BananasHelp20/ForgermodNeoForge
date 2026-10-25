package net.bananashelp20.forgermod.item.custom.abilities;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Horizontal anchors expire with the poison: ten seconds, or eleven with Jade. */
public final class RootingRootsState {
    public static final int RANGE=10;
    public static final int BASE_DURATION_TICKS=200;
    public static int durationTicks(boolean jade) {return BASE_DURATION_TICKS+(jade?20:0);}
    public record Anchor(double x, double z) {}
    private record Root(UUID owner, Anchor anchor, long endTick) {}
    private final Map<UUID, Root> roots = new HashMap<>();

    public void root(UUID owner, UUID mob, double x, double z, long now) {
        root(owner,mob,x,z,now,false);
    }

    public void root(UUID owner, UUID mob, double x, double z, long now, boolean jade) {
        roots.put(mob, new Root(owner, new Anchor(x, z), now + durationTicks(jade)));
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

    public boolean rootedBy(UUID mob, UUID owner, long now) {
        Root root = roots.get(mob);
        return root != null && root.owner.equals(owner) && anchor(mob, now) != null;
    }

    public void clearTarget(UUID mob) { roots.remove(mob); }
}
