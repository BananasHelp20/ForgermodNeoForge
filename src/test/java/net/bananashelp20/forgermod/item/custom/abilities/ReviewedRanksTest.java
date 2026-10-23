package net.bananashelp20.forgermod.item.custom.abilities;
import net.bananashelp20.forgermod.augmentation.ReviewedRanks;
public final class ReviewedRanksTest {
    public static void main(String[] args) {
        int[] explosionCooldowns={60,55,50,45,120,110,100,90};
        for(int rank=1;rank<=8;rank++)if(ReviewedRanks.cooldown("tooltips.forgermod.ability.explosion",rank)!=20*explosionCooldowns[rank-1])throw new AssertionError("Explosion/Blazing cooldown");
        for(int rank=5;rank<=8;rank++)if(Math.abs(ReviewedRanks.blastProjectileMultiplier(rank)-(1+.15*(rank-5)))>.00001)throw new AssertionError("Blazing damage progression");
        for(int rank=1;rank<=8;rank++){
            if(ReviewedRanks.grappleRange(rank)!=(rank<5?0:25+5*(rank-5)))throw new AssertionError("Grapple range");
            if(ReviewedRanks.cooldown("tooltips.forgermod.ability.crucible_hook",rank)!=(rank<5?45-rank*5:25)*20)throw new AssertionError("Hook/Grappler cooldown");
        }
        String roar="tooltips.forgermod.ability.challenge_roar";
        int[] ranges={16,20,24,28,35,40,45,50},durations={10,10,10,10,20,24,27,30},cooldowns={30,25,20,15,60,60,60,60};
        for(int i=0;i<8;i++)if(ReviewedRanks.roarRange(i+1)!=ranges[i] || ReviewedRanks.roarTicks(i+1)!=durations[i]*20 || ReviewedRanks.cooldown(roar,i+1)!=cooldowns[i]*20)throw new AssertionError("Roar progression");
        String area="tooltips.forgermod.ability.area_discharge";int[] areaCooldowns={180,130,80,30,300,260,220,180};
        for(int i=0;i<areaCooldowns.length;i++)if(ReviewedRanks.cooldown(area,i+1)!=areaCooldowns[i]*20)throw new AssertionError("Area/Zeus cooldown");
        if(ReviewedRanks.hardCap("tooltips.forgermod.ability.eye_of_the_storm")!=5 || ReviewedRanks.stormRadius(1)!=5 || ReviewedRanks.stormRadius(5)!=12
                || ReviewedRanks.stormTicks(1)!=100 || ReviewedRanks.stormTicks(5)!=400)throw new AssertionError("Eye of the Storm progression");
        String gravity="tooltips.forgermod.ability.ender_swing";
        if(ReviewedRanks.hardCap(gravity)!=3 || ReviewedRanks.gravitySwingRadius(1)!=2 || ReviewedRanks.gravitySwingRadius(2)!=3.5 || ReviewedRanks.gravitySwingRadius(3)!=5
                || ReviewedRanks.gravitySwingHeight(1)!=10 || ReviewedRanks.gravitySwingHeight(3)!=20 || ReviewedRanks.gravitySwingBonus(3)!=1.5
                || ReviewedRanks.cooldown(gravity,1)!=1800 || ReviewedRanks.cooldown(gravity,2)!=1500 || ReviewedRanks.cooldown(gravity,3)!=1200)throw new AssertionError("Gravity Swing progression");
        if(ReviewedRanks.echoGlowTicks(1)!=20 || ReviewedRanks.echoGlowTicks(2)!=27 || ReviewedRanks.echoGlowTicks(3)!=33 || ReviewedRanks.echoGlowTicks(4)!=40 || ReviewedRanks.echoGlowTicks(5)!=40)throw new AssertionError();
        String deep="tooltips.forgermod.ability.deep_wound";
        if(ReviewedRanks.hardCap(deep)!=3 || ReviewedRanks.cooldown(deep,1)!=2400 || ReviewedRanks.cooldown(deep,2)!=1500 || ReviewedRanks.cooldown(deep,3)!=600)throw new AssertionError();
        if(ReviewedRanks.cooldown(deep,99)!=600 || ReviewedRanks.hardCap("not_reviewed")!=-1)throw new AssertionError();
        String delusion="tooltips.forgermod.ability.delusion";
        if(ReviewedRanks.hardCap(delusion)!=5 || ReviewedRanks.delusionTicks(1)!=200 || ReviewedRanks.delusionTicks(5)!=500 || ReviewedRanks.cooldown(delusion,1)!=2400 || ReviewedRanks.cooldown(delusion,5)!=800)throw new AssertionError();
        if(ReviewedRanks.foodPoints(1)!=2 || ReviewedRanks.foodPoints(5)!=10 || ReviewedRanks.hardCap("tooltips.forgermod.ability.dream_feast")!=5 || ReviewedRanks.cooldown("tooltips.forgermod.ability.dream_feast",5)!=600)throw new AssertionError();
        String home="tooltips.forgermod.ability.just_a_dream";
        if(ReviewedRanks.hardCap(home)!=2 || ReviewedRanks.cooldown(home,1)!=1200 || ReviewedRanks.cooldown(home,2)!=600 || ReviewedRanks.cooldown(home,5)!=600)throw new AssertionError();
    }
}
