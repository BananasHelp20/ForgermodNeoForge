package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.augmentation.RecommendedAbilities;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.event.entity.living.ArmorHurtEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;

@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class OverwhelmingSmashEvents {
    private static final class Hit {
        final LivingEntity owner; final ItemStack weapon; final DamageSource source; boolean armorProcessed;
        Hit(LivingEntity owner,DamageSource source) { this.owner=owner; weapon=owner.getMainHandItem(); this.source=source; }
    }
    // Incoming damage provides the source missing from NeoForge 21.1.93's ArmorHurtEvent.
    private static final Map<UUID,Hit> HITS=new HashMap<>();
    private static final Map<ItemStack,Double> FRACTIONS=new WeakHashMap<>();
    private OverwhelmingSmashEvents() {}
    public static int armorBonusPercent() { return 20; }
    private static boolean qualifies(LivingEntity owner,LivingEntity target) {
        return owner.isAlive() && !owner.isSpectator() && owner.level()==target.level() && WeaponAbilityTargets.enemy(owner,target)
                && Augmentations.hasPassive(owner.getMainHandItem(),RecommendedAbilities.OVERWHELMING_SMASH);
    }
    private static int extra(ItemStack armor,double normalWear) {
        double amount=FRACTIONS.getOrDefault(armor,0D)+Math.max(0,normalWear)*armorBonusPercent()/100D;
        int whole=(int)Math.floor(amount+1e-7); FRACTIONS.put(armor,Math.max(0,amount-whole)); return whole;
    }
    @SubscribeEvent(priority=EventPriority.LOWEST,receiveCanceled=true)
    public static void onIncoming(LivingIncomingDamageEvent event) {
        HITS.remove(event.getEntity().getUUID());
        if(!event.isCanceled() && event.getAmount()>0 && PassiveActors.melee(event.getSource())
                && PassiveActors.entity(PassiveActors.attacker(event.getSource())) instanceof LivingEntity player && qualifies(player,event.getEntity()))
            HITS.put(event.getEntity().getUUID(),new Hit(player,event.getSource()));
    }
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void onArmor(ArmorHurtEvent event) {
        if(event.getEntity().level().isClientSide) return;
        var hit=HITS.get(event.getEntity().getUUID());
        if(hit==null || hit.owner.getMainHandItem()!=hit.weapon || !qualifies(hit.owner,event.getEntity())) return;
        hit.armorProcessed=true;
        for(var entry:event.getArmorMap().entrySet()) {
            var slot=entry.getKey(); var armor=entry.getValue().armorItemStack; float wear=event.getNewDamage(slot);
            if(armor.getItem() instanceof ArmorItem && armor.isDamageableItem() && wear>0)
                event.setNewDamage(slot,wear+extra(armor,wear));
        }
    }
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void onDamage(LivingDamageEvent.Post event) {
        var hit=HITS.remove(event.getEntity().getUUID());
        if(hit==null || hit.armorProcessed || event.getNewDamage()<=0 || hit.source.is(DamageTypeTags.BYPASSES_ARMOR)
                || hit.owner.getMainHandItem()!=hit.weapon || !hit.owner.isAlive() || hit.owner.isSpectator()
                || hit.owner.level()!=event.getEntity().level() || event.getEntity().isAlliedTo(hit.owner)
                || !Augmentations.hasPassive(hit.weapon,RecommendedAbilities.OVERWHELMING_SMASH)) return;
        // Most mobs have no vanilla armor-wear hook. Apply the bonus using vanilla's wear baseline.
        int normal=Math.max(1,(int)(event.getOriginalDamage()/4));
        for(var slot:new EquipmentSlot[]{EquipmentSlot.HEAD,EquipmentSlot.CHEST,EquipmentSlot.LEGS,EquipmentSlot.FEET,EquipmentSlot.BODY}) {
            var armor=event.getEntity().getItemBySlot(slot);
            if(armor.getItem() instanceof ArmorItem && armor.isDamageableItem() && armor.canBeHurtBy(hit.source)) {
                int bonus=extra(armor,normal); if(bonus>0) armor.hurtAndBreak(bonus,event.getEntity(),slot);
            }
        }
    }
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void onShield(LivingShieldBlockEvent event) {
        var source=event.getDamageContainer().getSource(); var target=event.getEntity();
        if(!event.getBlocked() || event.getBlockedDamage()<=0 || !PassiveActors.melee(source)
                || !(PassiveActors.entity(source.getEntity()) instanceof LivingEntity player) || !qualifies(player,target)) return;
        var shield=target.getUseItem(); if(shield.isEmpty() || !shield.canPerformAction(ItemAbilities.SHIELD_BLOCK)) return;
        if (!(target.level() instanceof net.minecraft.server.level.ServerLevel level)) return;
        var slot=target.getUsedItemHand()==InteractionHand.MAIN_HAND?EquipmentSlot.MAINHAND:EquipmentSlot.OFFHAND;
        // Resolve the original hit normally, including armor and all hit modifiers, without a second damage event.
        event.setBlocked(false);
        event.setBlockedDamage(0);
        event.setShieldDamage(0);
        if (target instanceof net.minecraft.world.entity.player.Player blocker) blocker.disableShield();
        else {
            target.stopUsingItem();
            target.getPersistentData().putLong("forgermod_shield_disabled_until", level.getGameTime()+100);
            level.broadcastEntityEvent(target,(byte)30);
        }
        if (shield.isDamageableItem()) shield.hurtAndBreak(Math.max(1,(int)Math.ceil(shield.getMaxDamage()*.1)),target,slot);
        var burst=target.position().add(0,target.getBbHeight()*.6,0).add(player.position().subtract(target.position()).normalize().scale(.35));
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.CRIT,burst.x,burst.y,burst.z,24,.25,.3,.25,.3);
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.SWEEP_ATTACK,burst.x,burst.y,burst.z,1,0,0,0,0);
    }
    @SubscribeEvent
    public static void onTick(ServerTickEvent.Post event) { HITS.clear(); }
}
