package net.bananashelp20.forgermod.item.custom.abilities;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;

public final class SwingAttackAbility {
    private SwingAttackAbility() {}
    public static boolean activate(ServerPlayer player,ItemStack stack) {
        double range=player.entityInteractionRange();
        float damage=(float)player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        player.swing(InteractionHand.MAIN_HAND);
        for(var target:player.level().getEntitiesOfClass(LivingEntity.class,player.getBoundingBox().inflate(range),
                living->WeaponAbilityTargets.enemy(player,living) && player.distanceToSqr(living)<=range*range && player.hasLineOfSight(living))) {
            int immunity=target.invulnerableTime; target.invulnerableTime=0;
            if(target.hurt(player.damageSources().playerAttack(player),damage))
                stack.getItem().postHurtEnemy(stack,target,player);
            else target.invulnerableTime=immunity;
            if(stack.isEmpty()) break;
        }
        // Sparse circular sweep particles communicate the actual attack radius.
        for(int point=0;point<16;point++) {
            double angle=point*Math.PI/8;
            player.serverLevel().sendParticles(ParticleTypes.SWEEP_ATTACK,player.getX()+Math.cos(angle)*range,
                    player.getY()+1,player.getZ()+Math.sin(angle)*range,1,0,0,0,0);
        }
        player.serverLevel().playSound(null,player.blockPosition(),SoundEvents.PLAYER_ATTACK_SWEEP,SoundSource.PLAYERS,1,.8F);
        return true;
    }
}
