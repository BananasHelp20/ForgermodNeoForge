package net.bananashelp20.forgermod.item.custom.abilities;

import java.util.List;
import java.util.Comparator;
import java.util.Set;
import java.util.UUID;
import net.minecraft.util.RandomSource;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

/** Chooses the nearest unvisited hostile for each possible ten-block chain jump. */
public final class ChainLightningTargets {
    private ChainLightningTargets() {}

    public static boolean shouldContinue(RandomSource random) {
        return random.nextBoolean();
    }

    public static <T> List<T> select(Iterable<T> candidates, Predicate<T> eligible,
                                     ToDoubleFunction<T> distanceSquared) {
        List<T> nearby = AreaDischargeTargets.select(candidates, eligible, distanceSquared, 10);
        nearby.sort(Comparator.comparingDouble(distanceSquared));
        return nearby;
    }

    public static <T> T next(Iterable<T> candidates, Predicate<T> eligible,
                             ToDoubleFunction<T> distanceSquared, Set<UUID> visited,
                             java.util.function.Function<T, UUID> identity) {
        return select(candidates, candidate -> eligible.test(candidate) && !visited.contains(identity.apply(candidate)),
                distanceSquared).stream().findFirst().orElse(null);
    }
}
