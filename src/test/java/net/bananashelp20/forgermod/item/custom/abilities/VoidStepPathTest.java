package net.bananashelp20.forgermod.item.custom.abilities;

public final class VoidStepPathTest {
    public static void main(String[] args) {
        expect(1, VoidStepPath.clearFraction(t -> true), "clear segment");
        expect(0, VoidStepPath.clearFraction(t -> false), "starting inside obstruction");
        expect(.73, VoidStepPath.clearFraction(t -> t <= .73), "partial distance before wall");
        expect(.01, VoidStepPath.clearFraction(t -> t <= .01), "very short clear segment");
        expect(0, VoidStepPath.clearFraction(t -> t == 0), "flush against wall");
    }
    private static void expect(double expected, double actual, String name) {
        if (Math.abs(expected-actual)>1.0E-5 || actual>expected) throw new AssertionError(name+": "+actual);
    }
}
