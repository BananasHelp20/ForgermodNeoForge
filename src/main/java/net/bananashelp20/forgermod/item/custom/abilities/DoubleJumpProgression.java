package net.bananashelp20.forgermod.item.custom.abilities;

/** Double Jump I-II followed by Windwalker I-IV. */
public final class DoubleJumpProgression {
    public static final int MAX_AIR_JUMPS=8;
    private DoubleJumpProgression() {}
    public static int jumps(int rank) {
        if(rank<=0)return 0;
        return switch(Math.clamp(rank,1,6)) {case 1->1;case 2,3->2;case 4,5->3;default->4;};
    }
    /** Keep the original normal and maximum lift; only non-count upgrades improve lift. */
    public static double lift(int rank) {return rank<3?.55:rank<5?.60:.70;}
    public static int migrate(int oldRank) {return oldRank>=4?oldRank-1:Math.min(oldRank,2);}
}
