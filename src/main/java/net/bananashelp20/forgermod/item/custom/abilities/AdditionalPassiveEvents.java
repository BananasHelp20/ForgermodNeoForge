package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.AdditionalAbilities;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class AdditionalPassiveEvents {
    private AdditionalPassiveEvents() {}
    @SubscribeEvent
    public static void onFallDamage(LivingIncomingDamageEvent event) {
        if(!event.isCanceled() && event.getAmount()>0 && event.getSource().is(DamageTypes.FALL)
                && event.getEntity() instanceof ServerPlayer player && player.isAlive() && !player.isSpectator()
                && Augmentations.hasPassive(player.getMainHandItem(),AdditionalAbilities.PILLOW_ARMOR))
            event.setAmount(event.getAmount()*.75F);
    }
    @SubscribeEvent
    public static void onDamage(LivingIncomingDamageEvent event) {
        if(event.isCanceled() || event.getAmount()<=0 || !event.getSource().is(DamageTypes.PLAYER_ATTACK)
                || !(event.getSource().getEntity() instanceof ServerPlayer player)
                || !player.isAlive() || player.isSpectator() || !WeaponAbilityTargets.enemy(player,event.getEntity())) return;
        var stack=player.getMainHandItem();
        long time=Math.floorMod(player.level().getDayTime(),24000L);
        if(Augmentations.hasPassive(stack,AdditionalAbilities.SLEEPWALKER) && player.level().dimensionType().hasSkyLight() && time>=13000 && time<23000)
            event.setAmount(event.getAmount()*1.1F);
        if(Augmentations.hasPassive(stack,AdditionalAbilities.CHARRED_EDGE) && event.getEntity().hasEffect(MobEffects.FIRE_RESISTANCE))
            event.setAmount(event.getAmount()*1.15F);
        if(Augmentations.hasPassive(stack,AdditionalAbilities.BACKSTAB)) {
            var fromTarget=player.position().subtract(event.getEntity().position()).multiply(1,0,1);
            var facing=net.minecraft.world.phys.Vec3.directionFromRotation(0,event.getEntity().getYRot());
            if(fromTarget.lengthSqr()>.0001 && fromTarget.normalize().dot(facing)<-.5)
                event.setAmount(event.getAmount()*1.2F);
        }
    }
}
