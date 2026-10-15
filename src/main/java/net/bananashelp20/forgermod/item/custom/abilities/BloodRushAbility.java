package net.bananashelp20.forgermod.item.custom.abilities;
import net.bananashelp20.forgermod.augmentation.AdditionalAbilities;
import net.bananashelp20.forgermod.augmentation.JadeAugments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.*;
import net.minecraft.world.item.ItemStack;
public final class BloodRushAbility {
    private BloodRushAbility() {}
    public static boolean activate(ServerPlayer player, ItemStack stack) {
        if(player.getHealth()<=2) return false;
        player.setHealth(player.getHealth()-2);
        player.addEffect(JadeAugments.reinforce(stack, AdditionalAbilities.BLOOD_RUSH,
                new MobEffectInstance(MobEffects.MOVEMENT_SPEED,200,1)));return true;
    }
}
