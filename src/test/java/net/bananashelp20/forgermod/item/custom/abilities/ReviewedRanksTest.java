package net.bananashelp20.forgermod.item.custom.abilities;
import net.bananashelp20.forgermod.augmentation.ReviewedRanks;
public final class ReviewedRanksTest {
    public static void main(String[] args) {
        String deep="tooltips.forgermod.ability.deep_wound";
        if(ReviewedRanks.hardCap(deep)!=3 || ReviewedRanks.cooldown(deep,1)!=2400 || ReviewedRanks.cooldown(deep,2)!=1500 || ReviewedRanks.cooldown(deep,3)!=600)throw new AssertionError();
        if(ReviewedRanks.cooldown(deep,99)!=600 || ReviewedRanks.hardCap("not_reviewed")!=-1)throw new AssertionError();
        String delusion="tooltips.forgermod.ability.delusion";
        if(ReviewedRanks.hardCap(delusion)!=5 || ReviewedRanks.delusionTicks(1)!=200 || ReviewedRanks.delusionTicks(5)!=500 || ReviewedRanks.cooldown(delusion,1)!=2400 || ReviewedRanks.cooldown(delusion,5)!=800)throw new AssertionError();
    }
}
