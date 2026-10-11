package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.augmentation.AdditionalAbilities;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public final class DowndraftBellAbility {
    private DowndraftBellAbility() {}
    public static boolean activate(ServerPlayer player,ItemStack stack) {
        int rank=Augmentations.level(stack,AdditionalAbilities.DOWNDRAFT_BELL);double radius=2+2*rank;
        var targets=player.level().getEntitiesOfClass(LivingEntity.class,player.getBoundingBox().inflate(radius),e->WeaponAbilityTargets.enemy(player,e)
            && !e.onGround() && !e.isInWaterOrBubble() && !e.isInLava() && player.distanceToSqr(e)<=radius*radius && player.hasLineOfSight(e));
        if(targets.isEmpty()) return false;
        for(var target:targets) {
            var motion=target.getDeltaMovement();target.setDeltaMovement(motion.x,Math.min(motion.y,-.6-.2*rank),motion.z);target.hasImpulse=true;
            if(target instanceof ServerPlayer other) VerticalMotionNetwork.apply(other,-.6-.2*rank,true,false);
            player.serverLevel().sendParticles(net.minecraft.core.particles.ParticleTypes.CLOUD,target.getX(),target.getY()+1,target.getZ(),12,.4,.2,.4,.04);
        }
        player.swing(net.minecraft.world.InteractionHand.MAIN_HAND);return true;
    }
}
