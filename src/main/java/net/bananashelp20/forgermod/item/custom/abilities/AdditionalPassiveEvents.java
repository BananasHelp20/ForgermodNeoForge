package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.ForgerMod;
import net.minecraft.world.entity.LivingEntity;
import net.bananashelp20.forgermod.augmentation.AdditionalAbilities;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffects;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class AdditionalPassiveEvents {
    public static final int BACKSTAB_PERCENT=20;
    private AdditionalPassiveEvents() {}
    private static boolean fullyArmored(net.minecraft.world.entity.LivingEntity target) {
        for(var slot:java.util.List.of(net.minecraft.world.entity.EquipmentSlot.HEAD,net.minecraft.world.entity.EquipmentSlot.CHEST,net.minecraft.world.entity.EquipmentSlot.LEGS,net.minecraft.world.entity.EquipmentSlot.FEET)) {
            var stack=target.getItemBySlot(slot);
            if(!(stack.getItem() instanceof net.minecraft.world.item.ArmorItem armor) || armor.getEquipmentSlot()!=slot) return false;
        }
        return true;
    }
    @SubscribeEvent
    public static void onFallDamage(LivingIncomingDamageEvent event) {
        if(!event.isCanceled() && event.getAmount()>0 && event.getSource().is(DamageTypes.FALL)
                && PassiveActors.entity(event.getEntity()) instanceof LivingEntity player && player.isAlive() && !player.isSpectator()
                && PassiveActors.has(player,AdditionalAbilities.PILLOW_ARMOR))
            event.setAmount(event.getAmount()*.75F);
    }
    @SubscribeEvent
    public static void onDamage(LivingIncomingDamageEvent event) {
        if(event.isCanceled() || event.getAmount()<=0 || !PassiveActors.melee(event.getSource())
                || !(PassiveActors.entity(PassiveActors.attacker(event.getSource())) instanceof LivingEntity player)
                || !player.isAlive() || player.isSpectator()) return;
        boolean enemy=WeaponAbilityTargets.enemy(player,event.getEntity());
        var stack=player.getMainHandItem();
        if(enemy && PassiveActors.has(player,AdditionalAbilities.HEAVY_CURRENT) && fullyArmored(event.getEntity()))
            event.setAmount(event.getAmount()*1.15F);
        if(enemy && PassiveActors.has(player,AdditionalAbilities.CONDUCTIVE_EDGE) && event.getEntity().isInWaterOrRain())
            event.setAmount(event.getAmount()*1.2F);
        long time=Math.floorMod(player.level().getDayTime(),24000L);
        if(enemy && PassiveActors.has(player,AdditionalAbilities.SLEEPWALKER) && player.level().dimensionType().hasSkyLight() && time>=13000 && time<23000)
            event.setAmount(event.getAmount()*1.1F);
        if(enemy && PassiveActors.has(player,AdditionalAbilities.CHARRED_EDGE) && event.getEntity().hasEffect(MobEffects.FIRE_RESISTANCE))
            event.setAmount(event.getAmount()*1.15F);
        if(event.getEntity()!=player && PassiveActors.has(player,AdditionalAbilities.BACKSTAB)) {
            var fromTarget=player.position().subtract(event.getEntity().position()).multiply(1,0,1);
            var facing=net.minecraft.world.phys.Vec3.directionFromRotation(0,event.getEntity().getYRot());
            if(fromTarget.lengthSqr()>.0001 && fromTarget.normalize().dot(facing)<-.5)
                event.setAmount(event.getAmount()*(1+BACKSTAB_PERCENT/100F));
        }
    }
}
