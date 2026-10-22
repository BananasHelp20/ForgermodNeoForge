package net.bananashelp20.forgermod.item.custom.abilities;

/** Pays half the combined available pool, spending absorption before real health. */
public final class SacrificeCost {
    public record Remaining(float health,float absorption) {}
    private SacrificeCost() {}
    public static Remaining pay(float health,float absorption) {
        float cost=(health+absorption)*.5f;
        float absorbed=Math.min(absorption,cost);
        return new Remaining(health-(cost-absorbed),absorption-absorbed);
    }
    public static int durationTicks(int rank) {return (10+5*Math.clamp(rank,1,5))*20;}
}
