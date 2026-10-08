package net.bananashelp20.forgermod.item.custom.abilities;

public final class BehindTargetOffsetTest {
    public static void main(String[] args) {
        BehindTargetOffset south = BehindTargetOffset.fromLook(0, 1, 2);
        expect(0, south.x(), "south-facing x");
        expect(-2, south.z(), "south-facing behind");

        BehindTargetOffset east = BehindTargetOffset.fromLook(4, 0, 1.5);
        expect(-1.5, east.x(), "normalizes look direction");
        expect(0, east.z(), "east-facing z");

        if (BehindTargetOffset.fromLook(0, 0, 2) != null) {
            throw new AssertionError("zero facing vector must not create a destination");
        }
    }

    private static void expect(double expected, double actual, String caseName) {
        if (Math.abs(expected - actual) > 0.000001) {
            throw new AssertionError(caseName + ": expected " + expected + ", got " + actual);
        }
    }
}
