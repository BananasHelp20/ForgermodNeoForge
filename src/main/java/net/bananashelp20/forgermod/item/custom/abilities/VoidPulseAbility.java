package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.augmentation.AdditionalAbilities;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.particles.ParticleTypes;

public final class VoidPulseAbility {
    private VoidPulseAbility() {}
    public static boolean activate(ServerPlayer player,ItemStack stack) {
        int rank=Augmentations.level(stack,AdditionalAbilities.VOID_PULSE);double radius=3+rank;
        for(var target:player.level().getEntitiesOfClass(LivingEntity.class,player.getBoundingBox().inflate(radius),e->WeaponAbilityTargets.enemy(player,e))) {
            if(player.distanceToSqr(target)>radius*radius || !player.hasLineOfSight(target)) continue;
            double dx=player.getX()-target.getX(),dz=player.getZ()-target.getZ();
            if(dx*dx+dz*dz<.0001) {dx=-player.getLookAngle().x;dz=-player.getLookAngle().z;if(dx*dx+dz*dz<.0001) dz=-1;}
            target.knockback(.4+.2*rank,dx,dz);
            if(target instanceof ServerPlayer other) other.connection.send(new net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket(other));
        }
        for(int i=0;i<32;i++) {
            double angle=i*Math.PI/16;
            player.serverLevel().sendParticles(ParticleTypes.PORTAL,player.getX()+Math.cos(angle)*radius,player.getY()+.5,
                    player.getZ()+Math.sin(angle)*radius,1,0,.1,0,.02);
        }
        return true;
    }
}
