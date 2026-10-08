package net.bananashelp20.forgermod.item.custom.abilities;

import java.util.UUID;

public final class WindyDashStateTest {
    public static void main(String[] args) {
        WindyDashState state = new WindyDashState();
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        expect(null, state.nextStep(first), "inactive player");
        expect(false, state.start(first, 0, 0), "zero direction rejected");
        expect(true, state.start(first, 3, 4), "start dash");
        expect(false, state.start(first, 1, 0), "cannot restart mid-dash");
        expect(null, state.nextStep(second), "other player unaffected");
        for (int impulse = 1; impulse <= 6; impulse++) {
            WindyDashState.Step step = state.nextStep(first);
            if (step == null || Math.abs(step.x() - 0.6) > 0.0001 || Math.abs(step.z() - 0.8) > 0.0001) {
                throw new AssertionError("Dash direction changed on step " + impulse);
            }
        }
        expect(null, state.nextStep(first), "dash finished");
        expect(true, state.start(first, 1, 0), "can start after dash");
        state.clear(first);
        expect(null, state.nextStep(first), "logout cleanup");
    }

    private static void expect(Object expected, Object actual, String description) {
        if (expected == null ? actual != null : !expected.equals(actual)) {
            throw new AssertionError(description + ": expected " + expected + ", got " + actual);
        }
    }
}
