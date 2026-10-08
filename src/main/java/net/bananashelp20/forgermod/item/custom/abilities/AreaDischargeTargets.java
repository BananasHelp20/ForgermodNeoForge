package net.bananashelp20.forgermod.item.custom.abilities;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;

/** Chooses living hostile targets inside the actual spherical ability range. */
public final class AreaDischargeTargets {
    private AreaDischargeTargets() {}

    public static <T> List<T> select(Iterable<T> candidates, Predicate<T> hostile,
                                     ToDoubleFunction<T> distanceSquared, double radius) {
        List<T> selected = new ArrayList<>();
        double limit = radius * radius;
        for (T candidate : candidates) {
            if (hostile.test(candidate) && distanceSquared.applyAsDouble(candidate) <= limit) {
                selected.add(candidate);
            }
        }
        return selected;
    }
}
