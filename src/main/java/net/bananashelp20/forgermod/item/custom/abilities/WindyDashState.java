package net.bananashelp20.forgermod.item.custom.abilities;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Six one-block movement impulses make a dash of up to six blocks. */
public final class WindyDashState {
    private static final int IMPULSES = 6;
    public record Step(double x, double z) {}
    private record Dash(Step step, int remaining) {}
    private final Map<UUID, Dash> dashes = new HashMap<>();

    public boolean start(UUID player, double x, double z) {
        if (x * x + z * z < 0.0001) return false;
        double length = Math.sqrt(x * x + z * z);
        return dashes.putIfAbsent(player, new Dash(new Step(x / length, z / length), IMPULSES)) == null;
    }

    public Step nextStep(UUID player) {
        Dash dash = dashes.get(player);
        if (dash == null) return null;
        if (dash.remaining == 1) dashes.remove(player);
        else dashes.put(player, new Dash(dash.step, dash.remaining - 1));
        return dash.step;
    }

    public void clear(UUID player) {
        dashes.remove(player);
    }

    public boolean isActive(UUID player) {
        return dashes.containsKey(player);
    }
}
