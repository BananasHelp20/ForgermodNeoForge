package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.memory.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.warden.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import java.util.*;

/** Target leases last for the Strength window; super debuffs are a moving, independently owned aura. */
@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class ChallengeRoarAbility {
    private static final String ID=AdditionalAbilities.CHALLENGE_ROAR;
    private static final class Effect {
        final LivingEntity target;final Holder<MobEffect> type;
        MobEffectInstance owned,previous;long previousAt,deadline;int amplifier;
        Effect(LivingEntity target,Holder<MobEffect> type){this.target=target;this.type=type;}
        void apply(int remaining,int amp,long now){
            var current=target.getEffect(type);
            if(current!=null && current==owned && current.getAmplifier()==amp && !current.isInfiniteDuration() && current.getDuration()<=remaining+1)return;
            if(current!=null && (current.getAmplifier()>amp || current.getAmplifier()==amp && (current.isInfiniteDuration() || current.getDuration()>=remaining))){owned=null;previous=null;return;}
            previous=current==null?null:new MobEffectInstance(current);previousAt=now;
            target.addEffect(new MobEffectInstance(type,remaining,amp));owned=target.getEffect(type);deadline=now+remaining;amplifier=amp;
        }
        void release(long now){
            var current=target.getEffect(type);
            if(current==null || current!=owned || current.isInfiniteDuration() || current.getAmplifier()!=amplifier || current.getDuration()>Math.max(0,deadline-now)+1)return;
            target.removeEffect(type);
            if(previous!=null){int remaining=previous.isInfiniteDuration()?-1:(int)Math.max(0,previous.getDuration()-(now-previousAt));
                if(remaining!=0)target.addEffect(new MobEffectInstance(type,remaining,previous.getAmplifier(),previous.isAmbient(),previous.isVisible(),previous.showIcon()));}
        }
    }
    private static final class Roar {
        final ServerPlayer owner;final ItemStack stack;final int slot,range;final ServerLevel level;final long expires;final boolean intimidating;
        final Effect strength;final Map<UUID,Mob> targets=new HashMap<>();final Map<UUID,List<Effect>> aura=new HashMap<>();
        Roar(ServerPlayer player,ItemStack stack,int rank){
            owner=player;this.stack=stack;slot=player.getInventory().selected;level=player.serverLevel();range=ReviewedRanks.roarRange(rank);intimidating=rank>=5;
            expires=level.getGameTime()+JadeAugments.duration(stack,ID,ReviewedRanks.roarTicks(rank));strength=new Effect(player,MobEffects.DAMAGE_BOOST);
            strength.apply((int)(expires-level.getGameTime()),JadeAugments.amplifier(stack,ID,0),level.getGameTime());
        }
    }
    private static final Map<UUID,Roar> ROARS=new HashMap<>();
    private static final Map<UUID,UUID> OWNERS=new HashMap<>();
    private ChallengeRoarAbility(){}
    private static boolean hostile(ServerPlayer player,Mob mob){return mob.isAlive() && !mob.isRemoved() && mob instanceof Enemy && !(mob instanceof NeutralMob) && !WeaponAbilityTargets.ally(player,mob);}
    public static boolean activate(ServerPlayer player,ItemStack stack){
        if(active(player))return false;
        int rank=Augmentations.level(stack,ID);if(rank<=0 || stack!=player.getMainHandItem() || !player.isAlive() || player.isSpectator())return false;
        var roar=new Roar(player,stack,rank);ROARS.put(player.getUUID(),roar);
        for(var mob:roar.level.getEntitiesOfClass(Mob.class,player.getBoundingBox().inflate(roar.range),m->hostile(player,m) && player.distanceToSqr(m)<=roar.range*roar.range)){
            roar.targets.put(mob.getUUID(),mob);OWNERS.put(mob.getUUID(),player.getUUID());redirect(roar,mob,true);
        }
        tick(player);
        roar.level.sendParticles(ParticleTypes.SONIC_BOOM,player.getX(),player.getY()+1,player.getZ(),1,0,0,0,0);
        roar.level.playSound(null,player.blockPosition(),SoundEvents.WARDEN_ROAR,player.getSoundSource(),.7F,1.2F);return true;
    }
    public static boolean active(ServerPlayer player){
        var roar=ROARS.get(player.getUUID());if(roar==null)return false;
        if(player.isAlive() && !player.isSpectator() && player.serverLevel()==roar.level && player.getMainHandItem()==roar.stack
                && player.getInventory().selected==roar.slot && player.level().getGameTime()<roar.expires && player.hasEffect(MobEffects.DAMAGE_BOOST)
                && Augmentations.ids(roar.stack,true).contains(ID))return true;
        cancel(player.getUUID());return false;
    }
    public static void cancel(UUID owner){
        var roar=ROARS.remove(owner);if(roar==null)return;long now=roar.level.getGameTime();
        OWNERS.entrySet().removeIf(e->e.getValue().equals(owner));roar.strength.release(now);roar.aura.values().forEach(list->list.forEach(effect->effect.release(now)));
    }
    private static void redirect(Roar roar,Mob mob,boolean initial){
        if(DelusionEvents.suppressed(mob))return;
        WeaponControlLocks.redirectTarget(mob,roar.owner);if(WeaponControlLocks.frozen(mob))return;
        var brain=mob.getBrain();long remaining=Math.max(1,roar.expires-roar.level.getGameTime());
        if(brain.checkMemory(MemoryModuleType.ANGRY_AT,MemoryStatus.REGISTERED))brain.setMemoryWithExpiry(MemoryModuleType.ANGRY_AT,roar.owner.getUUID(),remaining);
        if(mob instanceof Warden warden && warden.canTargetEntity(roar.owner)){
            if(initial){if(warden.isNoAi())warden.getAngerManagement().increaseAnger(roar.owner,100);else warden.increaseAngerAt(roar.owner,100,false);}
            if(brain.getMemory(MemoryModuleType.ATTACK_TARGET).orElse(null)!=roar.owner){warden.setAttackTarget(roar.owner);WardenAi.updateActivity(warden);}
        }else if(brain.checkMemory(MemoryModuleType.ATTACK_TARGET,MemoryStatus.REGISTERED)){
            brain.eraseMemory(MemoryModuleType.CANT_REACH_WALK_TARGET_SINCE);brain.setMemory(MemoryModuleType.ATTACK_TARGET,roar.owner);
        }
    }
    public static void tick(ServerPlayer player){
        if(!active(player))return;var roar=ROARS.get(player.getUUID());long now=roar.level.getGameTime();
        roar.targets.values().removeIf(mob->{if(!hostile(player,mob) || mob.level()!=roar.level){OWNERS.remove(mob.getUUID(),player.getUUID());return true;}
            if(player.getUUID().equals(OWNERS.get(mob.getUUID())))redirect(roar,mob,false);return false;});
        if(!roar.intimidating)return;Set<UUID> present=new HashSet<>();
        for(var mob:roar.level.getEntitiesOfClass(Mob.class,player.getBoundingBox().inflate(roar.range),m->WeaponAbilityTargets.enemy(player,m) && player.distanceToSqr(m)<=roar.range*roar.range)){
            present.add(mob.getUUID());var effects=roar.aura.computeIfAbsent(mob.getUUID(),id->List.of(new Effect(mob,MobEffects.WEAKNESS),new Effect(mob,MobEffects.MOVEMENT_SLOWDOWN)));
            for(var effect:effects)effect.apply((int)(roar.expires-now),0,now);
        }
        roar.aura.entrySet().removeIf(entry->{if(present.contains(entry.getKey()))return false;entry.getValue().forEach(effect->effect.release(now));return true;});
    }
    private static Roar controlling(Mob mob){
        var owner=OWNERS.get(mob.getUUID());var roar=owner==null?null:ROARS.get(owner);
        return roar!=null && active(roar.owner) && mob.level()==roar.level?roar:null;
    }
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void target(LivingChangeTargetEvent event){
        if(event.getEntity() instanceof Mob mob && event.getNewAboutToBeSetTarget()!=null){var roar=controlling(mob);
            if(roar!=null && event.getNewAboutToBeSetTarget()!=roar.owner)event.setNewAboutToBeSetTarget(WeaponControlLocks.frozen(mob)?null:roar.owner);}
    }
    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void attack(LivingIncomingDamageEvent event){
        if(event.getSource().getEntity() instanceof Mob mob){var roar=controlling(mob);if(roar!=null && event.getEntity()!=roar.owner)event.setCanceled(true);}
    }
    @SubscribeEvent public static void tick(PlayerTickEvent.Post event){if(event.getEntity() instanceof ServerPlayer player)tick(player);}
    @SubscribeEvent public static void brainTarget(net.neoforged.neoforge.event.tick.EntityTickEvent.Post event){
        if(event.getEntity() instanceof Mob mob){var roar=controlling(mob);if(roar!=null)redirect(roar,mob,false);}
    }
    @SubscribeEvent public static void logout(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event){cancel(event.getEntity().getUUID());}
}
