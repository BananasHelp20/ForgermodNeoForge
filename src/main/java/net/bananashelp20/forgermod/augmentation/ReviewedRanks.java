package net.bananashelp20.forgermod.augmentation;

/** Explicit review caps also apply to templates and legacy stored weapons. */
public final class ReviewedRanks {
    private ReviewedRanks() {}
    public static int hardCap(String id) {
        if(id==null)return -1;
        return switch(id) {
            case "tooltips.forgermod.ability.just_a_dream" -> 2;
            case "tooltips.forgermod.ability.dream_feast" -> 5;
            case "tooltips.forgermod.ability.delusion" -> 5;
            case "tooltips.forgermod.ability.deep_wound" -> 3;
            default -> -1;
        };
    }
    public static int cooldown(String id,int rank) {
        if(id==null)return -1;
        return switch(id) {
            case "tooltips.forgermod.ability.just_a_dream" -> at(rank,60,30)*20;
            case "tooltips.forgermod.ability.dream_feast" -> at(rank,120,100,75,50,30)*20;
            case "tooltips.forgermod.ability.delusion" -> at(rank,120,100,80,60,40)*20;
            case "tooltips.forgermod.ability.deep_wound" -> at(rank,120,75,30)*20;
            default -> -1;
        };
    }
    public static int at(int rank,int... values) {return values[Math.clamp(rank-1,0,values.length-1)];}
    public static int delusionTicks(int rank) {return at(rank,10,14,18,22,25)*20;}
    public static int foodPoints(int rank) {return 2*Math.clamp(rank,1,5);}
    public static int echoGlowTicks(int rank) {return at(rank,20,27,33,40);}
}
