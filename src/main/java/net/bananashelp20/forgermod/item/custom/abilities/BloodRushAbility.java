package net.bananashelp20.forgermod.item.custom.abilities;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.*;
public final class BloodRushAbility {
    private BloodRushAbility() {}
    public static boolean activate(ServerPlayer player) {
        if(player.getHealth()<=2) return false;
        player.setHealth(player.getHealth()-2);
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED,200,1));return true;
    }
}
