package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.custom.SwordItemWithEffect;
import net.minecraft.server.level.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.phys.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import java.util.*;

@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class WindchargeEvents {
    private record Flight(ItemStack stack,int slot,ServerLevel level,long expires,float bonusMultiplier) {}
    private static final Map<UUID,Flight> FLIGHTS=new HashMap<>();
    private static final Map<UUID,LivingEntity> STRIKES=new HashMap<>();
    private WindchargeEvents() {}
    public static double launchVelocity(double height) {
        double low=0,high=4;
        for(int j=0;j<40;j++) {double v=(low+high)/2,y=0;for(double speed=v;speed>0;speed=(speed-.08)*.98) y+=speed;if(y<height) low=v;else high=v;}
        return (low+high)/2;
    }
    public static float fallBonus(float distance) {return distance<=3?4*distance:distance<=8?12+2*(distance-3):22+distance-8;}
    public static boolean activate(ServerPlayer player,ItemStack stack) {
        if(active(player) || player.isFallFlying() || player.isPassenger()) return false;
        int rank=Augmentations.level(stack,AdditionalAbilities.WINDCHARGE);boolean axe=stack.getItem() instanceof SwordItemWithEffect weapon && weapon.isAxe();
        double height=axe?new int[]{10,16,23,30}[Math.clamp(rank,1,4)-1]:5*Math.clamp(rank,1,5);
        VerticalMotionNetwork.apply(player,launchVelocity(height),false,true);
        if(axe && rank>=4) FLIGHTS.put(player.getUUID(),new Flight(stack,player.getInventory().selected,player.serverLevel(),player.level().getGameTime()+200,1+.1f*Math.clamp(rank-4,0,3)));
        player.serverLevel().sendParticles(ParticleTypes.GUST,player.getX(),player.getY(),player.getZ(),8,.3,.2,.3,.02);return true;
    }
    public static boolean active(ServerPlayer player) {
        var flight=FLIGHTS.get(player.getUUID());if(flight==null) return false;
        if(!player.isAlive() || player.isSpectator() || player.getMainHandItem()!=flight.stack || player.getInventory().selected!=flight.slot || player.serverLevel()!=flight.level
            || player.level().getGameTime()>=flight.expires || !Augmentations.ids(flight.stack,true).contains(AdditionalAbilities.WINDCHARGE)) {cancel(player.getUUID());return false;}return true;
    }
    public static void cancel(UUID id) {FLIGHTS.remove(id);STRIKES.remove(id);}
    @SubscribeEvent
    public static void damage(LivingIncomingDamageEvent event) {
        if(event.getSource().is(DamageTypes.PLAYER_ATTACK) && event.getSource().getEntity() instanceof ServerPlayer owner && active(owner)
            && owner.fallDistance>1.5 && !owner.isFallFlying() && WeaponAbilityTargets.enemy(owner,event.getEntity())) {STRIKES.put(owner.getUUID(),event.getEntity());event.setAmount(event.getAmount()+fallBonus(owner.fallDistance)*FLIGHTS.get(owner.getUUID()).bonusMultiplier);}
    }
    @SubscribeEvent
    public static void hit(LivingDamageEvent.Post event) {
        if(event.getSource().is(DamageTypes.PLAYER_ATTACK) && event.getSource().getEntity() instanceof ServerPlayer owner && active(owner)
            && STRIKES.remove(owner.getUUID())==event.getEntity() && owner.fallDistance>1.5 && event.getNewDamage()+event.getReduction(DamageContainer.Reduction.ABSORPTION)>0) {
            // The initial victim already received the bonus, including when the strike killed it.
            smash(owner,event.getEntity().position(),event.getEntity(),owner.fallDistance);
            owner.currentImpulseImpactPos=owner.position();owner.setIgnoreFallDamageFromCurrentImpulse(true);owner.resetFallDistance();
        }
    }
    @SubscribeEvent
    public static void landing(LivingFallEvent event) {
        if(event.getEntity() instanceof ServerPlayer player && active(player)) {
            if(event.getDistance()>1.5 && !player.isFallFlying()) {smash(player,player.position(),null,event.getDistance());event.setDamageMultiplier(0);player.resetFallDistance();}
            else cancel(player.getUUID());
        }
    }
    private static void smash(ServerPlayer player,Vec3 center,LivingEntity initial,float distance) {
        float bonusMultiplier=FLIGHTS.get(player.getUUID()).bonusMultiplier;
        cancel(player.getUUID());float damage=(float)player.getAttributeValue(Attributes.ATTACK_DAMAGE)+fallBonus(distance)*bonusMultiplier;
        for(var target:player.serverLevel().getEntitiesOfClass(LivingEntity.class,new AABB(center,center).inflate(2),e->e!=initial && WeaponAbilityTargets.enemy(player,e))) {
            if(target.position().distanceToSqr(center)>4 || !player.hasLineOfSight(target)) continue;
            target.hurt(player.damageSources().indirectMagic(player,player),damage);target.knockback(.8,center.x-target.getX(),center.z-target.getZ());
            if(target instanceof ServerPlayer other) other.connection.send(new net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket(other));
        }
        player.serverLevel().sendParticles(ParticleTypes.GUST_EMITTER_SMALL,center.x,center.y+.1,center.z,1,0,0,0,0);
        player.serverLevel().playSound(null,center.x,center.y,center.z,SoundEvents.MACE_SMASH_GROUND_HEAVY,player.getSoundSource(),1,1);
    }
    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post event) {
        if(event.getEntity() instanceof ServerPlayer player && active(player) && (player.isInWaterOrBubble() || player.isInLava() || player.onClimbable() || player.onGround() && player.getDeltaMovement().y<=0)) cancel(player.getUUID());
    }
    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {cancel(event.getEntity().getUUID());}
}
