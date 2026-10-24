package net.bananashelp20.forgermod.item.custom.abilities;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.core.particles.*;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import org.joml.Vector3f;
@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class RevengefulExitEvents {
    private static final ThreadLocal<Boolean> BURST=ThreadLocal.withInitial(()->false);
    private RevengefulExitEvents() {}
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void death(LivingDeathEvent event) {
        if(event.isCanceled() || BURST.get() || !(PassiveActors.entity(event.getEntity()) instanceof LivingEntity player) || player.isSpectator()
            || !Augmentations.hasPassive(PassiveActors.weapon(player,AdditionalAbilities.REVENGEFUL_EXIT),AdditionalAbilities.REVENGEFUL_EXIT)
            || !(PassiveActors.attacker(event.getSource()) instanceof LivingEntity killer) || !killer.isAlive() || killer.level()!=player.level() || !WeaponAbilityTargets.hostileTo(player,killer)) return;
        float damage=(float)player.getAttributeValue(Attributes.ATTACK_DAMAGE);var center=player.position();boolean killerIncluded=false;
        BURST.set(true);
        try {
            var source=player.damageSources().explosion(player,player);
            for(var target:PassiveActors.level(player).getEntitiesOfClass(LivingEntity.class,player.getBoundingBox().inflate(5),e->e!=player && e.isAlive() && !e.isSpectator() && PassiveActors.canAffect(player,e))) {
                if(target.position().distanceToSqr(center)>25 || !player.hasLineOfSight(target)) continue;
                // Combine both amounts in one hit so immunity frames cannot erase the additional killer damage.
                target.hurt(source,damage*(target==killer?6:1));if(target==killer) killerIncluded=true;
            }
            if(!killerIncluded) killer.hurt(source,damage*5);
            PassiveActors.level(player).sendParticles(ParticleTypes.EXPLOSION_EMITTER,center.x,center.y+.5,center.z,1,0,0,0,0);
            PassiveActors.level(player).sendParticles(new DustParticleOptions(new Vector3f(.65F,.02F,.03F),1.5F),center.x,center.y+.7,center.z,80,2,.6,2,.02);
            PassiveActors.level(player).playSound(null,center.x,center.y,center.z,SoundEvents.GENERIC_EXPLODE,player.getSoundSource(),1,1);
        } finally {BURST.set(false);}
    }
}
