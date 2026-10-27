package net.bananashelp20.forgermod.item.custom.abilities;

/** Missing target health scales damage up to the learned rank's bonus. */
public final class DeathMarchDamage {
    private DeathMarchDamage() {}

    public static int maximumBonusPercent(int rank) {return 50*Math.clamp(rank,1,5);}
    public static float multiplier(float health, float maxHealth) {return multiplier(health,maxHealth,1);}
    public static float multiplier(float health, float maxHealth,int rank) {
        if (maxHealth <= 0) return 1;
        float remainingFraction = Math.max(0, Math.min(1, health / maxHealth));
        return 1+(1-remainingFraction)*maximumBonusPercent(rank)/100F;
    }
}
