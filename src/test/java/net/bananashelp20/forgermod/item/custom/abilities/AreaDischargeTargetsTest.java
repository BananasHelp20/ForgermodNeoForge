package net.bananashelp20.forgermod.item.custom.abilities;

import java.util.List;

public final class AreaDischargeTargetsTest {
    private record Candidate(boolean hostile, boolean alive, double distanceSquared) {}

    public static void main(String[] args) {
        Candidate center = new Candidate(true, true, 0);
        Candidate boundary = new Candidate(true, true, 25);
        Candidate beyond = new Candidate(true, true, 25.01);
        Candidate peaceful = new Candidate(false, true, 1);
        Candidate dead = new Candidate(true, false, 1);

        List<Candidate> chosen = AreaDischargeTargets.select(
                List.of(center, boundary, beyond, peaceful, dead),
                candidate -> candidate.hostile() && candidate.alive(),
                Candidate::distanceSquared, 5);
        if (!chosen.equals(List.of(center, boundary))) {
            throw new AssertionError("Expected only living hostile targets within five blocks: " + chosen);
        }
    }
}
