package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.ForgerMod;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import java.util.*;

/** Shared leases prevent overlapping sleep/stuns from restoring stale AI or gravity flags. */
@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class WeaponControlLocks {
    private static final class Lock {
        final Vec3 position;final boolean noGravity,noAi;final LivingEntity target;
        final Map<String,Long> leases=new HashMap<>();
        Lock(LivingEntity entity) {position=entity.position();noGravity=entity.isNoGravity();noAi=entity instanceof Mob mob && mob.isNoAi();target=entity instanceof Mob mob?mob.getTarget():null;}
    }
    private static final Map<LivingEntity,Lock> LOCKS=new HashMap<>();
    private WeaponControlLocks() {}
    private static String token(UUID owner,String id) {return owner+"|"+id;}
    public static void acquire(LivingEntity entity,UUID owner,String id,int ticks) {
        if(!entity.isAlive() || ticks<=0 || entity instanceof ServerPlayer player && player.isSpectator()) return;
        var lock=LOCKS.computeIfAbsent(entity,Lock::new);lock.leases.merge(token(owner,id),entity.level().getGameTime()+ticks,Math::max);hold(entity,lock,true);
    }
    private static void hold(LivingEntity entity,Lock lock,boolean force) {
        boolean moving=entity.getDeltaMovement().lengthSqr()>.000001 || entity.position().distanceToSqr(lock.position)>.0001;
        entity.setNoGravity(true);entity.setDeltaMovement(Vec3.ZERO);entity.stopUsingItem();entity.hasImpulse=true;
        if(entity instanceof Mob mob) {mob.setNoAi(true);mob.setTarget(null);}
        if(entity instanceof ServerPlayer player) {
            if(entity.position().distanceToSqr(lock.position)>.0001) player.connection.teleport(lock.position.x,lock.position.y,lock.position.z,player.getYRot(),player.getXRot(),Set.of(net.minecraft.world.entity.RelativeMovement.X_ROT,net.minecraft.world.entity.RelativeMovement.Y_ROT));
            if(force || moving) player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket(player));
        } else entity.setPos(lock.position);
    }
    private static void restore(LivingEntity entity,Lock lock) {
        LOCKS.remove(entity);entity.setNoGravity(lock.noGravity);
        if(entity instanceof Mob mob) {
            mob.setNoAi(lock.noAi);
            if(entity.isAlive() && lock.target!=null && lock.target.isAlive() && lock.target.level()==entity.level()) mob.setTarget(lock.target);
        }
    }
    public static boolean frozen(LivingEntity entity) {
        var lock=LOCKS.get(entity);if(lock==null) return false;
        lock.leases.entrySet().removeIf(e->e.getValue()<=entity.level().getGameTime());
        if(lock.leases.isEmpty() || !entity.isAlive() || entity.isRemoved()) {restore(entity,lock);return false;}return true;
    }
    public static void release(UUID owner,String id) {
        for(var entry:List.copyOf(LOCKS.entrySet())) {entry.getValue().leases.remove(token(owner,id));if(entry.getValue().leases.isEmpty()) restore(entry.getKey(),entry.getValue());}
    }
    @SubscribeEvent
    public static void tick(ServerTickEvent.Pre event) {for(var entity:List.copyOf(LOCKS.keySet())) if(frozen(entity)) hold(entity,LOCKS.get(entity),false);}
    @SubscribeEvent
    public static void afterTick(ServerTickEvent.Post event) {for(var entity:List.copyOf(LOCKS.keySet())) if(frozen(entity)) hold(entity,LOCKS.get(entity),false);}
    @SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.HIGHEST)
    public static void incomingAttack(net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent event) {
        if((event.getSource().is(net.minecraft.world.damagesource.DamageTypes.MOB_ATTACK) || event.getSource().is(net.minecraft.world.damagesource.DamageTypes.PLAYER_ATTACK))
            && event.getSource().getEntity() instanceof LivingEntity attacker && frozen(attacker)) event.setCanceled(true);
    }
    @SubscribeEvent
    public static void attack(AttackEntityEvent event) {if(frozen(event.getEntity())) event.setCanceled(true);}
    @SubscribeEvent
    public static void use(PlayerInteractEvent.RightClickItem event) {if(frozen(event.getEntity())) event.setCanceled(true);}
    @SubscribeEvent
    public static void travel(net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent event) {
        if(event.getEntity() instanceof LivingEntity entity) {var lock=LOCKS.get(entity);if(lock!=null) restore(entity,lock);}
    }
    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        String prefix=event.getEntity().getUUID()+"|";
        for(var entry:List.copyOf(LOCKS.entrySet())) {
            entry.getValue().leases.keySet().removeIf(key->key.startsWith(prefix));
            if(entry.getKey()==event.getEntity() || entry.getValue().leases.isEmpty()) restore(entry.getKey(),entry.getValue());
        }
    }
}
