package net.bananashelp20.forgermod.item.custom.abilities;

public final class VoidStepPathTest {
    public static void main(String[] args) {
        expect(40, VoidStepPath.furthestClearStep(step -> true), "full ten blocks");
        expect(80, VoidStepPath.furthestClearStep(VoidStepPath.PAIRED_STEPS, step -> true), "paired twenty blocks");
        expect(47, VoidStepPath.furthestClearStep(VoidStepPath.PAIRED_STEPS, step -> step < 48), "paired path stops before wall");
        expect(0, VoidStepPath.furthestClearStep(0, step -> true), "zero-length path");
        expect(7, VoidStepPath.furthestClearStep(step -> step < 8), "stops before wall");
        expect(0, VoidStepPath.furthestClearStep(step -> false), "blocked at start");
        expect(1, VoidStepPath.furthestClearStep(step -> step == 1), "too short to activate");
    }

    private static void expect(int expected, int actual, String caseName) {
        if (expected != actual) throw new AssertionError(caseName + ": expected " + expected + ", got " + actual);
    }
}
