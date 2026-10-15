package net.bananashelp20.forgermod.item.custom.abilities;
import net.bananashelp20.forgermod.augmentation.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.particles.ParticleTypes;
public final class FeatherShelterAbility {
    private FeatherShelterAbility() {}
    public static boolean activate(ServerPlayer player,ItemStack stack) {
        double radius=2+2*Augmentations.level(stack,AdditionalAbilities.FEATHER_SHELTER);
        var effect = JadeAugments.reinforce(stack, AdditionalAbilities.FEATHER_SHELTER,
                new MobEffectInstance(MobEffects.SLOW_FALLING,200,0));
        player.addEffect(effect);
        for(var ally:player.serverLevel().getEntitiesOfClass(LivingEntity.class,player.getBoundingBox().inflate(radius),e->WeaponAbilityTargets.ally(player,e)))
            if(player.distanceToSqr(ally)<=radius*radius && player.hasLineOfSight(ally)) ally.addEffect(new MobEffectInstance(effect));
        player.serverLevel().sendParticles(ParticleTypes.CLOUD,player.getX(),player.getY()+1,player.getZ(),20,.7,.4,.7,.04);return true;
    }
}
