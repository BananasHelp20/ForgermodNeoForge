package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.AdditionalAbilities;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.particle.StunningPresenceParticles;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import java.util.*;

@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class StunningPresenceEvents {
    private static final double RADIUS=StunningPresenceParticles.RADIUS;
    private static final class Aura {
        final ItemStack stack;final int selected,rank;final ServerLevel level;final long expires;long nextPulse,nextVisual,pulseStarted;
        Aura(ServerPlayer player,ItemStack stack) {this.stack=stack;selected=player.getInventory().selected;rank=Augmentations.level(stack,AdditionalAbilities.STUNNING_PRESENCE);level=player.serverLevel();expires=level.getGameTime()+600;nextPulse=level.getGameTime();}
    }
    private static final Map<UUID,Aura> AURAS=new HashMap<>();
    private StunningPresenceEvents() {}
    public static boolean activate(ServerPlayer player,ItemStack stack) {if(active(player)) return false;AURAS.put(player.getUUID(),new Aura(player,stack));advance(player);return true;}
    public static boolean active(ServerPlayer player) {
        var aura=AURAS.get(player.getUUID());if(aura==null) return false;
        if(!player.isAlive() || player.isSpectator() || player.getMainHandItem()!=aura.stack || player.getInventory().selected!=aura.selected || player.serverLevel()!=aura.level
                || player.level().getGameTime()>=aura.expires || !Augmentations.ids(aura.stack,true).contains(AdditionalAbilities.STUNNING_PRESENCE)) {cancel(player.getUUID());return false;}return true;
    }
    public static long remainingTicks(ServerPlayer player) {return active(player)?AURAS.get(player.getUUID()).expires-player.level().getGameTime():0;}
    public static void cancel(UUID owner) {AURAS.remove(owner);WeaponControlLocks.release(owner,AdditionalAbilities.STUNNING_PRESENCE);}
    private static void advance(ServerPlayer player) {
        if(!active(player)) return;var aura=AURAS.get(player.getUUID());long now=player.level().getGameTime();
        if(now<aura.nextPulse) {animate(player,aura,now);return;}
        double radius=RADIUS;
        for(var target:aura.level.getEntitiesOfClass(LivingEntity.class,player.getBoundingBox().inflate(radius),e->WeaponAbilityTargets.enemy(player,e) && player.distanceToSqr(e)<=radius*radius && player.hasLineOfSight(e))) {
            int immunity=target.invulnerableTime;target.invulnerableTime=0;
            try {target.hurt(player.damageSources().indirectMagic(player,player),1);}finally {target.invulnerableTime=Math.max(immunity,target.invulnerableTime);}
            WeaponControlLocks.acquire(target,player.getUUID(),AdditionalAbilities.STUNNING_PRESENCE,10);
            aura.level.sendParticles(net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK,target.getX(),target.getY()+1,target.getZ(),8,.3,.4,.3,.03);
        }
        aura.nextPulse=now+Math.round(40-(aura.rank-1)*20F/3);
        aura.pulseStarted=now;
        animate(player,aura,now);
    }
    private static void animate(ServerPlayer player,Aura aura,long now) {
        if(now<aura.nextVisual) return;
        aura.nextVisual=now+StunningPresenceParticles.VISUAL_INTERVAL;
        int rank=Math.clamp(aura.rank,1,4);
        ring(player,aura,RADIUS,rank,now*.04*rank,true);
        long elapsed=now-aura.pulseStarted;
        if(elapsed<=10) ring(player,aura,RADIUS*Math.min(1,(elapsed+1)/10.0),rank,-now*.08,false);
    }
    private static void ring(ServerPlayer player,Aura aura,double radius,int rank,double rotation,boolean boundary) {
        var options=new StunningPresenceParticles.Options(player.getId(),rank,(float)radius,(float)(rotation%(2*Math.PI)),boundary);
        aura.level.sendParticles(options,player.getX(),player.getY(),player.getZ(),0,0,0,0,0);
    }
    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post event) {if(event.getEntity() instanceof ServerPlayer player) advance(player);}
    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {cancel(event.getEntity().getUUID());}
}
