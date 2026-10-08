package net.bananashelp20.forgermod.item.custom.abilities;

/** Horizontal offset behind an entity's facing direction. */
public record BehindTargetOffset(double x, double z) {
    public static BehindTargetOffset fromLook(double lookX, double lookZ, double distance) {
        double length = Math.hypot(lookX, lookZ);
        if (length < 0.001 || distance <= 0) return null;
        return new BehindTargetOffset(-lookX / length * distance, -lookZ / length * distance);
    }
}
