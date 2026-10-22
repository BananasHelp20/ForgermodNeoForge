package net.bananashelp20.forgermod.item.custom.abilities;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.particles.ParticleTypes;

public final class DreamFeastAbility {
    private DreamFeastAbility() {}
    public static boolean activate(ServerPlayer player) {
        var food=player.getFoodData();
        if(!food.needsFood() && food.getSaturationLevel()>=food.getFoodLevel()) return false;
        int points=net.bananashelp20.forgermod.augmentation.ReviewedRanks.foodPoints(
                net.bananashelp20.forgermod.augmentation.Augmentations.level(player.getMainHandItem(),
                        net.bananashelp20.forgermod.augmentation.AdditionalAbilities.DREAM_FEAST));
        food.eat(points,.5F);
        player.serverLevel().sendParticles(ParticleTypes.HAPPY_VILLAGER,player.getX(),player.getY()+1,player.getZ(),12,.4,.4,.4,.03);
        return true;
    }
}
