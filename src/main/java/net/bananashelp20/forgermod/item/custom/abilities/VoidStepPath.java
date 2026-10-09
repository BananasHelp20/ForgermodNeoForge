package net.bananashelp20.forgermod.item.custom.abilities;

import java.util.function.IntPredicate;

/** Chooses the farthest point on a clear, continuously checked teleport path. */
public final class VoidStepPath {
    public static final int STEPS = 40;
    public static final int PAIRED_STEPS = 80;
    public static final double STEP_DISTANCE = 0.25;

    private VoidStepPath() {}

    public static int furthestClearStep(IntPredicate isSafe) {
        return furthestClearStep(STEPS, isSafe);
    }

    public static int furthestClearStep(int maxSteps, IntPredicate isSafe) {
        int lastClear = 0;
        for (int step = 1; step <= maxSteps; step++) {
            if (!isSafe.test(step)) break;
            lastClear = step;
        }
        return lastClear;
    }
}
