package net.bananashelp20.forgermod.item.custom.abilities;

import java.util.function.DoublePredicate;

/** Refines the first obstructed swept segment to its farthest safe point. */
public final class VoidStepPath {
    public static final int STEPS = 40;
    public static final int PAIRED_STEPS = 80;
    public static final double STEP_DISTANCE = 0.25;
    private VoidStepPath() {}

    public static double clearFraction(DoublePredicate isSafe) {
        if (!isSafe.test(0)) return 0;
        if (isSafe.test(1)) return 1;
        double clear=0, blocked=1;
        for(int i=0;i<18;i++) {
            double middle=(clear+blocked)*.5;
            if(isSafe.test(middle)) clear=middle; else blocked=middle;
        }
        return clear;
    }
}
