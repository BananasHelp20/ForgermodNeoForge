package net.bananashelp20.forgermod.item.custom.abilities;

/** Rank-scaled melee damage bonus against undead. */
public final class RevengeDamage {
    private RevengeDamage() {}
    public static int bonusPercent(int rank) { return 6 * Math.clamp(rank,1,5); }
    public static float multiplier(boolean undead,int rank) {
        return undead ? 1 + bonusPercent(rank)/100F : 1;
    }
}
