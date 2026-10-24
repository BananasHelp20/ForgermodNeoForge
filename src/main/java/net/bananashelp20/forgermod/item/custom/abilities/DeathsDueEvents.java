package net.bananashelp20.forgermod.item.custom.abilities;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class DeathsDueEvents {
    private DeathsDueEvents(){}
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void experience(LivingExperienceDropEvent event){
        if(event.isCanceled())return;
        var recorded=PassiveActors.killingActor(event.getEntity());var source=event.getEntity().getLastDamageSource();
        var actor=recorded!=null?recorded:source==null?event.getAttackingPlayer():PassiveActors.attacker(source);
        if(!(PassiveActors.entity(actor) instanceof LivingEntity wielder) || !WeaponAbilityTargets.hostileTo(wielder,event.getEntity()))return;
        boolean reinforcement=wielder instanceof BoneGuardEvents.FriendlyGuard;
        if(!reinforcement && (!wielder.isAlive() || wielder.isSpectator()))return;
        long xp=Math.max(0,event.getDroppedExperience());
        if(PassiveActors.has(wielder,AdditionalAbilities.DEATHS_DUE))xp+=xp/4;
        if(reinforcement)xp/=2;
        event.setDroppedExperience((int)Math.min(Integer.MAX_VALUE,xp));
    }
}
