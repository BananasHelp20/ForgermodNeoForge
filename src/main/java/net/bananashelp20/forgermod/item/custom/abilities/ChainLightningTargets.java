package net.bananashelp20.forgermod.item.custom.abilities;

import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

/** Picks up to two nearby hostile mobs, closest to the original target first. */
public final class ChainLightningTargets {
    private ChainLightningTargets() {}

    public static <T> List<T> select(Iterable<T> candidates, Predicate<T> eligible,
                                     ToDoubleFunction<T> distanceSquared) {
        List<T> nearby = AreaDischargeTargets.select(candidates, eligible, distanceSquared, 10);
        nearby.sort(Comparator.comparingDouble(distanceSquared));
        return nearby.subList(0, Math.min(2, nearby.size()));
    }
}
