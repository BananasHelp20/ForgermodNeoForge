package net.bananashelp20.forgermod.item.custom.abilities;

import java.util.List;

public final class ChainLightningTargetsTest {
    private record Candidate(String name, boolean eligible, double distanceSquared) {}

    public static void main(String[] args) {
        Candidate far = new Candidate("far", true, 100);
        Candidate near = new Candidate("near", true, 1);
        Candidate middle = new Candidate("middle", true, 25);
        Candidate outside = new Candidate("outside", true, 100.01);
        Candidate peaceful = new Candidate("peaceful", false, 0);
        List<Candidate> chosen = ChainLightningTargets.select(
                List.of(far, near, middle, outside, peaceful), Candidate::eligible, Candidate::distanceSquared);
        if (!chosen.equals(List.of(near, middle))) {
            throw new AssertionError("Expected two nearest eligible mobs: " + chosen);
        }
        if (!ChainLightningTargets.select(List.of(peaceful), Candidate::eligible, Candidate::distanceSquared).isEmpty()) {
            throw new AssertionError("Expected no targets when only peaceful mobs are nearby");
        }
    }
}
