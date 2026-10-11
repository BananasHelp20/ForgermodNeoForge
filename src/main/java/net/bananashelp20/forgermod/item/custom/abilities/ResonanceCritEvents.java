package net.bananashelp20.forgermod.item.custom.abilities;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;
@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class ResonanceCritEvents {
    private ResonanceCritEvents() {}
    @SubscribeEvent(priority=EventPriority.HIGH)
    public static void critical(CriticalHitEvent event) {
        if(!(event.getEntity() instanceof ServerPlayer player) || !player.isAlive() || player.isSpectator()
                || player.getAttackStrengthScale(.5F)<=.9F || !Augmentations.hasPassive(player.getMainHandItem(),AdditionalAbilities.RESONANCE_CRIT)
                || !(event.getTarget() instanceof LivingEntity target) || !WeaponAbilityTargets.enemy(player,target)
                || target.onGround() || target.getDeltaMovement().y<=0 || target.isInWaterOrBubble() || target.isInLava()
                || target.onClimbable() || target.isFallFlying()) return;
        event.setCriticalHit(true);event.setDamageMultiplier(Math.max(1.5F,event.getDamageMultiplier()));
    }
}
