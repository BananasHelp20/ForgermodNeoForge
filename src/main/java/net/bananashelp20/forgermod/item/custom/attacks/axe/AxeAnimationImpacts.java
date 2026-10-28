package net.bananashelp20.forgermod.item.custom.attacks.axe;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.RecommendedAbilities;
import net.bananashelp20.forgermod.item.custom.abilities.*;
import net.bananashelp20.forgermod.mixin.AttackStrengthAccess;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import java.util.*;

/** Server-owned animation impact deadlines, bound to the actual equipped stack. */
@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class AxeAnimationImpacts {
    private record Job(ServerPlayer player,ItemStack stack,int slot,ServerLevel level,long due,AxeAnimationData.Clip clip,Runnable action) {}
    private static final Map<UUID,Map<String,Job>> JOBS=new HashMap<>();
    private static final Map<UUID,Integer> SWINGS=new HashMap<>();
    private static final Map<UUID,Long> NEXT_SWING=new HashMap<>();
    private static final ThreadLocal<AxeAnimationData.Clip> IMPACT=new ThreadLocal<>();
    private AxeAnimationImpacts(){}
    public static boolean atImpact(AxeAnimationData.Clip clip){return IMPACT.get()==clip;}
    public static boolean pending(UUID owner,String channel){return JOBS.getOrDefault(owner,Map.of()).containsKey(channel);}
    public static boolean queue(ServerPlayer player,ItemStack stack,String channel,AxeAnimationData.Clip clip,Runnable action){
        if(stack!=player.getMainHandItem() || !player.isAlive() || pending(player.getUUID(),channel))return false;
        interrupt(player.getUUID());AxeHeavyNetwork.cancelPending(player);
        JOBS.computeIfAbsent(player.getUUID(),ignored->new HashMap<>()).put(channel,new Job(player,stack,player.getInventory().selected,player.serverLevel(),player.level().getGameTime()+clip.impactTicks(),clip,action));
        AxeAnimationNetwork.play(player,clip);return true;
    }
    public static void cancel(UUID owner,String channel){var jobs=JOBS.get(owner);if(jobs!=null){jobs.remove(channel);if(jobs.isEmpty())JOBS.remove(owner);if(jobs.values().stream().noneMatch(j->j.clip==AxeAnimationData.Clip.VERTICAL))AxeVerticalMotion.cancelIfWaiting(owner);}}
    public static void interrupt(UUID owner){
        var old=JOBS.remove(owner);
        var server=net.neoforged.neoforge.server.ServerLifecycleHooks.getCurrentServer();
        if(server!=null){var player=server.getPlayerList().getPlayer(owner);if(player!=null)AxeVerticalMotion.cancel(player);}
        if(old!=null && old.containsKey("crucible_slam"))CrucibleHookEvents.cancel(owner);
    }
    public static void cancel(UUID owner){AxeVerticalMotion.forget(owner);JOBS.remove(owner);SWINGS.remove(owner);NEXT_SWING.remove(owner);}
    private static boolean valid(Job job){var p=job.player;return !job.stack.isEmpty() && p.isAlive() && !p.isSpectator() && p.getMainHandItem()==job.stack
            && p.getInventory().selected==job.slot && p.serverLevel()==job.level && !WeaponControlLocks.frozen(p)
            && (job.clip!=AxeAnimationData.Clip.VERTICAL || p.getOffhandItem().isEmpty());}
    @SubscribeEvent public static void tick(PlayerTickEvent.Post event){
        if(!(event.getEntity() instanceof ServerPlayer player))return;var jobs=JOBS.get(player.getUUID());if(jobs==null)return;
        for(var entry:List.copyOf(jobs.entrySet())){
            var job=entry.getValue();if(!valid(job)){cancel(player.getUUID(),entry.getKey());continue;}
            if(job.clip==AxeAnimationData.Clip.VERTICAL?!AxeVerticalMotion.ready(player):player.level().getGameTime()<job.due)continue;
            cancel(player.getUUID(),entry.getKey());var previous=IMPACT.get();IMPACT.set(job.clip);
            try{job.action.run();}finally{if(previous==null)IMPACT.remove();else IMPACT.set(previous);}
        }
    }
    private static AxeAnimationData.Clip next(ServerPlayer player){int n=SWINGS.merge(player.getUUID(),1,Integer::sum);return n%2==1?AxeAnimationData.Clip.SWING:AxeAnimationData.Clip.SWING2;}
    public static void miss(ServerPlayer player){
        if(AxeVerticalMotion.airborne(player) || WindchargeEvents.active(player))return;
        if(player.level().getGameTime()<NEXT_SWING.getOrDefault(player.getUUID(),Long.MIN_VALUE))return;
        var clip=next(player);NEXT_SWING.put(player.getUUID(),player.level().getGameTime()+(long)Math.ceil(AxeAnimationData.playbackLength(clip)*20));
        queue(player,player.getMainHandItem(),"ordinary",clip,()->player.serverLevel().playSound(null,player.blockPosition(),net.minecraft.sounds.SoundEvents.PLAYER_ATTACK_SWEEP,player.getSoundSource(),.45F,.7F));
    }
    public static boolean reachable(ServerPlayer player,Entity target){
        if(!target.isAlive() || target.level()!=player.level() || !player.hasLineOfSight(target))return false;
        var eye=player.getEyePosition();var box=target.getBoundingBox();
        double x=Math.clamp(eye.x,box.minX,box.maxX),y=Math.clamp(eye.y,box.minY,box.maxY),z=Math.clamp(eye.z,box.minZ,box.maxZ);
        return eye.distanceToSqr(new net.minecraft.world.phys.Vec3(x,y,z))<=player.entityInteractionRange()*player.entityInteractionRange();
    }
    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void attack(AttackEntityEvent event){
        if(event.isCanceled() || event.getEntity() instanceof ServerPlayer p && (p.isSpectator() || WeaponControlLocks.frozen(p) || WindchargeEvents.active(p)) || IMPACT.get()!=null || !(event.getEntity() instanceof ServerPlayer player) || !AxeAnimationItems.isModAxe(player.getMainHandItem()))return;
        event.setCanceled(true);
        if(AxeVerticalMotion.airborne(player) || AxeHeavyNetwork.hasPending(player) || pending(player.getUUID(),"ordinary") || player.level().getGameTime()<NEXT_SWING.getOrDefault(player.getUUID(),Long.MIN_VALUE))return;
        var target=event.getTarget();var stack=player.getMainHandItem();var access=(AttackStrengthAccess)player;
        int charge=access.forgermod$getAttackStrengthTicker();float fall=player.fallDistance;boolean grounded=player.onGround(),sprinting=player.isSprinting();
        var clip=AxeItems.canVerticalStrike(stack) && EchoSalvageEvents.bonus(player)>0?AxeAnimationData.Clip.VERTICAL:next(player);
        if(clip==AxeAnimationData.Clip.VERTICAL)player.getPersistentData().putLong(AxeHeavyNetwork.ANIMATION_TAG,player.level().getGameTime());
        NEXT_SWING.put(player.getUUID(),player.level().getGameTime()+(long)Math.ceil(AxeAnimationData.playbackLength(clip)*20));
        player.resetAttackStrengthTicker();
        queue(player,stack,"ordinary",clip,()->{
            if(!reachable(player,target))return;
            int progress=access.forgermod$getAttackStrengthTicker();float currentFall=player.fallDistance;boolean currentGround=player.onGround(),currentSprint=player.isSprinting();
            access.forgermod$setAttackStrengthTicker(charge);player.fallDistance=fall;player.setOnGround(grounded);player.setSprinting(sprinting);
            try{player.attack(target);}finally{access.forgermod$setAttackStrengthTicker(progress);player.fallDistance=currentFall;player.setOnGround(currentGround);player.setSprinting(currentSprint);}
        });
    }
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent event){cancel(event.getEntity().getUUID());}
}
