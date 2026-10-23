package net.bananashelp20.forgermod.augmentation;

/** Explicit review caps also apply to templates and legacy stored weapons. */
public final class ReviewedRanks {
    private ReviewedRanks() {}
    public static int hardCap(String id) {
        if(id==null)return -1;
        return switch(id) {
            case "tooltips.forgermod.ability.eye_of_the_storm" -> 5;
            case "tooltips.forgermod.ability.ender_swing" -> 3;
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
            case "tooltips.forgermod.ability.area_discharge" -> at(rank,180,130,80,30,300,260,220,180)*20;
            case "tooltips.forgermod.ability.challenge_roar" -> at(rank,30,25,20,15,60,60,60,60)*20;
            case "tooltips.forgermod.ability.crucible_hook" -> at(rank,40,35,30,25,25,25,25,25)*20;
            case "tooltips.forgermod.ability.ender_swing" -> at(rank,90,75,60)*20;
            case "tooltips.forgermod.ability.just_a_dream" -> at(rank,60,30)*20;
            case "tooltips.forgermod.ability.dream_feast" -> at(rank,120,100,75,50,30)*20;
            case "tooltips.forgermod.ability.delusion" -> at(rank,120,100,80,60,40)*20;
            case "tooltips.forgermod.ability.deep_wound" -> at(rank,120,75,30)*20;
            default -> -1;
        };
    }
    public static int at(int rank,int... values) {return values[Math.clamp(rank-1,0,values.length-1)];}
    public static int roarRange(int rank) {return at(rank,16,20,24,28,35,40,45,50);}
    public static int grappleRange(int rank) {return at(rank,0,0,0,0,25,30,35,40);}
    public static int roarTicks(int rank) {return at(rank,10,10,10,10,20,24,27,30)*20;}
    public static int delusionTicks(int rank) {return at(rank,10,14,18,22,25)*20;}
    public static int foodPoints(int rank) {return 2*Math.clamp(rank,1,5);}
    public static int echoGlowTicks(int rank) {return at(rank,20,27,33,40);}
    public static double gravitySwingRadius(int rank) {return 2+1.5*(Math.clamp(rank,1,3)-1);}
    public static int gravitySwingHeight(int rank) {return at(rank,10,15,20);}
    public static double gravitySwingBonus(int rank) {return .5*Math.clamp(rank,1,3);}
    public static double stormRadius(int rank) {return switch(Math.clamp(rank,1,5)){case 1->5;case 2->7;case 3->9;case 4->10.5;default->12;};}
    public static int stormTicks(int rank) {return at(rank,5,9,13,16,20)*20;}
}
