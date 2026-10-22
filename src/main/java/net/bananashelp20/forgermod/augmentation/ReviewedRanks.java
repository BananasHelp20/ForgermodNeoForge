package net.bananashelp20.forgermod.augmentation;

/** Explicit review caps also apply to templates and legacy stored weapons. */
public final class ReviewedRanks {
    private ReviewedRanks() {}
    public static int hardCap(String id) {
        if(id==null)return -1;
        return switch(id) {
            case "tooltips.forgermod.ability.deep_wound" -> 3;
            default -> -1;
        };
    }
    public static int cooldown(String id,int rank) {
        if(id==null)return -1;
        return switch(id) {
            case "tooltips.forgermod.ability.deep_wound" -> at(rank,120,75,30)*20;
            default -> -1;
        };
    }
    public static int at(int rank,int... values) {return values[Math.clamp(rank-1,0,values.length-1)];}
}
