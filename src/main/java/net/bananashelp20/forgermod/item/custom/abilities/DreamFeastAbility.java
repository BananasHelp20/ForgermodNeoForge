package net.bananashelp20.forgermod.item.custom.abilities;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.particles.ParticleTypes;

public final class DreamFeastAbility {
    private DreamFeastAbility() {}
    public static boolean activate(ServerPlayer player) {
        if(!player.getFoodData().needsFood()) return false;
        player.getFoodData().eat(8,0F);
        player.serverLevel().sendParticles(ParticleTypes.HAPPY_VILLAGER,player.getX(),player.getY()+1,player.getZ(),12,.4,.4,.4,.03);
        return true;
    }
}
