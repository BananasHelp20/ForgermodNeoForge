package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.item.custom.InanisiumWeapon;
import net.bananashelp20.forgermod.item.custom.attacks.dagger.DaggerItems;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

/** Three base ranks followed by Warping Space I-IV. */
public final class VoidStepRanges {
    public static final String ID="tooltips.forgermod.ability.void_step";
    private VoidStepRanges() {}
    public static int blink(int rank) {
        return switch(Math.clamp(rank,1,7)) {
            case 1->5;case 2->10;case 3->15;case 4->20;case 5->25;case 6->30;default->40;
        };
    }
    public static int etherwarp(int rank) {return rank<4?0:30+10*(Math.clamp(rank,4,7)-4);}
    public static int blink(int rank,boolean paired) {return blink(rank)*(paired?2:1);}
    public static int etherwarp(int rank,boolean paired) {return Math.min(100,etherwarp(rank)*(paired?2:1));}
    public static int etherwarpCooldown(int rank) {return (45-5*(Math.clamp(rank,4,7)-4))*20;}
    public static boolean paired(ServerPlayer player) {
        return player.getMainHandItem().getItem() instanceof InanisiumWeapon main && main.isDagger()
                && player.getOffhandItem().getItem() instanceof InanisiumWeapon off && off.isDagger()
                && DaggerItems.hasMatchingDaggers(player)
                && Augmentations.level(player.getMainHandItem(),ID)>0
                && Augmentations.level(player.getOffhandItem(),ID)>0;
    }
    public static boolean etherwarpCast(ServerPlayer player,ItemStack stack) {
        return Augmentations.level(stack,ID)>=4 && player.isShiftKeyDown();
    }
}
