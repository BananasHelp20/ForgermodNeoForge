package net.bananashelp20.forgermod.item.custom.abilities;

/** At full health damage is normal; near zero health it approaches double. */
public final class DeathMarchDamage {
    private DeathMarchDamage() {}

    public static float multiplier(float health, float maxHealth) {
        if (maxHealth <= 0) return 1;
        float remainingFraction = Math.max(0, Math.min(1, health / maxHealth));
        return 2 - remainingFraction;
    }
}
