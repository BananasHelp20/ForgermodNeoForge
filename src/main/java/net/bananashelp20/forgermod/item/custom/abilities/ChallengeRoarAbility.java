package net.bananashelp20.forgermod.item.custom.abilities;
import net.bananashelp20.forgermod.augmentation.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.memory.*;
import net.minecraft.world.entity.monster.warden.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
public final class ChallengeRoarAbility {
    private ChallengeRoarAbility() {}
    public static boolean activate(ServerPlayer player,ItemStack stack) {
        double range=4+2*Augmentations.level(stack,AdditionalAbilities.CHALLENGE_ROAR);
        for(var mob:player.level().getEntitiesOfClass(Mob.class,player.getBoundingBox().inflate(range),
                m->WeaponAbilityTargets.enemy(player,m) && player.distanceToSqr(m)<=range*range && player.hasLineOfSight(m) && m.canAttack(player))) {
            WeaponControlLocks.redirectTarget(mob,player);
            if(mob instanceof NeutralMob neutral) {neutral.setPersistentAngerTarget(player.getUUID());neutral.setRemainingPersistentAngerTime(600);}
            var brain=mob.getBrain();
            if(brain.checkMemory(MemoryModuleType.ANGRY_AT,MemoryStatus.REGISTERED))brain.setMemoryWithExpiry(MemoryModuleType.ANGRY_AT,player.getUUID(),600L);
            if(mob instanceof Warden warden && warden.canTargetEntity(player)) {
                if(warden.isNoAi())warden.getAngerManagement().increaseAnger(player,100);
                else warden.increaseAngerAt(player,100,false);
                warden.setAttackTarget(player);WardenAi.updateActivity(warden);
            } else if(brain.checkMemory(MemoryModuleType.ATTACK_TARGET,MemoryStatus.REGISTERED)) {
                brain.eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);brain.setMemory(MemoryModuleType.ATTACK_TARGET,player);
            }
        }
        player.serverLevel().sendParticles(ParticleTypes.SONIC_BOOM,player.getX(),player.getY()+1,player.getZ(),1,0,0,0,0);
        player.serverLevel().playSound(null,player.blockPosition(),SoundEvents.WARDEN_ROAR,player.getSoundSource(),.7F,1.2F);
        return true;
    }
}
