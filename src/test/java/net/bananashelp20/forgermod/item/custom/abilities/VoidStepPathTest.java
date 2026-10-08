package net.bananashelp20.forgermod.item.custom.abilities;

public final class VoidStepPathTest {
    public static void main(String[] args) {
        expect(20, VoidStepPath.furthestClearStep(step -> true), "full five blocks");
        expect(7, VoidStepPath.furthestClearStep(step -> step < 8), "stops before wall");
        expect(0, VoidStepPath.furthestClearStep(step -> false), "blocked at start");
        expect(1, VoidStepPath.furthestClearStep(step -> step == 1), "too short to activate");
    }

    private static void expect(int expected, int actual, String caseName) {
        if (expected != actual) throw new AssertionError(caseName + ": expected " + expected + ", got " + actual);
    }
}
