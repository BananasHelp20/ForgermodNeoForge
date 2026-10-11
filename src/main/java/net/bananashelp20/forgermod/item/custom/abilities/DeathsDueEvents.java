package net.bananashelp20.forgermod.item.custom.abilities;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingExperienceDropEvent;
@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class DeathsDueEvents {
    private DeathsDueEvents() {}
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void experience(LivingExperienceDropEvent event) {
        if(event.isCanceled() || !(event.getAttackingPlayer() instanceof ServerPlayer player) || !player.isAlive() || player.isSpectator()
                || !Augmentations.hasPassive(player.getMainHandItem(),AdditionalAbilities.DEATHS_DUE)
                || !WeaponAbilityTargets.hostileTo(player,event.getEntity())) return;
        int xp=Math.max(0,event.getDroppedExperience());
        event.setDroppedExperience((int)Math.min(Integer.MAX_VALUE,xp+(long)xp/4));
    }
}
