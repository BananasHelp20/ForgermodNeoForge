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
    private static final class Flight {
        final ItemStack stack;final int slot;final ServerLevel level;final long expires;final float bonusMultiplier;
        double peakY;float fall;boolean airborne,directHit;
        Flight(ServerPlayer player,ItemStack stack,int rank) {
            this.stack=stack;slot=player.getInventory().selected;level=player.serverLevel();expires=level.getGameTime()+200;
            bonusMultiplier=1+.1f*Math.clamp(rank-4,0,3);peakY=player.getY();
        }
        void sample(ServerPlayer player) {
            peakY=Math.max(peakY,player.getY());airborne|=!player.onGround();
            fall=Math.max(fall,Math.max(player.fallDistance,(float)Math.max(0,peakY-player.getY())));
        }
    }
    private record Strike(LivingEntity target,float distance,long tick) {}
    private static final Map<UUID,Flight> FLIGHTS=new HashMap<>();
    private static final Map<UUID,Strike> STRIKES=new HashMap<>();
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
        if(axe && rank>=4) FLIGHTS.put(player.getUUID(),new Flight(player,stack,rank));
        player.serverLevel().sendParticles(ParticleTypes.GUST,player.getX(),player.getY(),player.getZ(),8,.3,.2,.3,.02);return true;
    }
    public static boolean active(ServerPlayer player) {
        var flight=FLIGHTS.get(player.getUUID());if(flight==null) return false;
        if(!player.isAlive() || player.isSpectator() || player.getMainHandItem()!=flight.stack || player.getInventory().selected!=flight.slot || player.serverLevel()!=flight.level
            || player.level().getGameTime()>=flight.expires || !Augmentations.ids(flight.stack,true).contains(AdditionalAbilities.WINDCHARGE)) {cancel(player.getUUID());return false;}return true;
    }
    public static void cancel(UUID id) {FLIGHTS.remove(id);STRIKES.remove(id);}
    public static float attackBonus(net.minecraft.world.entity.Entity target,net.minecraft.world.damagesource.DamageSource source) {
        if(!(source.getEntity() instanceof ServerPlayer owner) || !source.is(DamageTypes.PLAYER_ATTACK) || !active(owner)
            || FLIGHTS.get(owner.getUUID()).directHit || owner.fallDistance<=1.5 || owner.getDeltaMovement().y>0
            || owner.isFallFlying() || !(target instanceof LivingEntity living) || !WeaponAbilityTargets.enemy(owner,living)) return 0;
        var flight=FLIGHTS.get(owner.getUUID());flight.sample(owner);
        STRIKES.put(owner.getUUID(),new Strike(living,owner.fallDistance,owner.level().getGameTime()));
        return fallBonus(owner.fallDistance)*flight.bonusMultiplier;
    }
    @SubscribeEvent
    public static void hit(LivingDamageEvent.Post event) {
        if(!event.getSource().is(DamageTypes.PLAYER_ATTACK) || !(event.getSource().getEntity() instanceof ServerPlayer owner) || !active(owner)) return;
        var strike=STRIKES.remove(owner.getUUID());
        if(strike==null || strike.tick()!=owner.level().getGameTime() || strike.target()!=event.getEntity() || event.getNewDamage()+event.getReduction(DamageContainer.Reduction.ABSORPTION)<=0) return;
        var flight=FLIGHTS.get(owner.getUUID());flight.directHit=true;flight.fall=Math.max(flight.fall,strike.distance());
        // Reuse the pinned vanilla mace hit motion, sound, smash particles and knockback; it does not damage the passed stack.
        net.minecraft.world.item.Items.MACE.hurtEnemy(new ItemStack(net.minecraft.world.item.Items.MACE),event.getEntity(),owner);
        owner.resetFallDistance();
        // Keep the flight alive: a successful axe hit must not consume the later ground impact.
    }
    @SubscribeEvent
    public static void landing(LivingFallEvent event) {
        if(event.getEntity() instanceof ServerPlayer player && active(player)) {
            var flight=FLIGHTS.get(player.getUUID());flight.sample(player);float distance=Math.max(event.getDistance(),flight.fall);
            if(distance>1.5 && !player.isFallFlying()) {smash(player,distance);event.setDamageMultiplier(0);player.resetFallDistance();}
            else cancel(player.getUUID());
        }
    }
    private static void smash(ServerPlayer player,float distance) {
        var flight=FLIGHTS.get(player.getUUID());var center=player.position();
        cancel(player.getUUID());float damage=(float)player.getAttributeValue(Attributes.ATTACK_DAMAGE)+fallBonus(distance)*flight.bonusMultiplier;
        for(var target:player.serverLevel().getEntitiesOfClass(LivingEntity.class,new AABB(center,center).inflate(5),e->WeaponAbilityTargets.enemy(player,e))) {
            if(target.position().distanceToSqr(center)>25 || !player.hasLineOfSight(target)) continue;
            int immunity=target.invulnerableTime;target.invulnerableTime=0;boolean hurt;
            try {hurt=target.hurt(player.damageSources().playerAttack(player),damage);}finally {target.invulnerableTime=Math.max(immunity,target.invulnerableTime);}
            if(!hurt) continue;
            var away=target.position().subtract(center);double resistance=1-target.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
            double power=Math.max(0,5-away.length())*.7*(distance>5?2:1)*resistance;
            var push=away.normalize().scale(power);target.push(push.x,.7*resistance,push.z);
            if(target instanceof ServerPlayer other) other.connection.send(new net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket(other));
        }
        player.serverLevel().levelEvent(2013,player.getOnPos(),750);
        player.serverLevel().sendParticles(ParticleTypes.GUST_EMITTER_SMALL,center.x,center.y+.1,center.z,1,0,0,0,0);
        for(int i=0;i<32;i++) {
            double angle=i*Math.PI/16,dx=Math.cos(angle),dz=Math.sin(angle);
            player.serverLevel().sendParticles(ParticleTypes.CLOUD,center.x+dx*.5,center.y+.15,center.z+dz*.5,0,dx*.8,.12,dz*.8,1);
        }
        player.serverLevel().playSound(null,center.x,center.y,center.z,SoundEvents.MACE_SMASH_GROUND_HEAVY,player.getSoundSource(),1.5f,.85f);
        player.swing(net.minecraft.world.InteractionHand.MAIN_HAND,true);player.resetFallDistance();
    }
    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post event) {
        if(!(event.getEntity() instanceof ServerPlayer player) || !active(player)) return;
        var flight=FLIGHTS.get(player.getUUID());flight.sample(player);
        if(player.isInWaterOrBubble() || player.isInLava() || player.onClimbable() || player.isFallFlying() || player.isPassenger()) {cancel(player.getUUID());return;}
        // The mace's impulse safety can suppress/shorten LivingFallEvent: still smash on actual contact.
        if(player.onGround() && player.getDeltaMovement().y<=0) {
            if(flight.airborne && flight.fall>1.5) smash(player,flight.fall);
            else cancel(player.getUUID());
        }
    }
    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {cancel(event.getEntity().getUUID());}
}
