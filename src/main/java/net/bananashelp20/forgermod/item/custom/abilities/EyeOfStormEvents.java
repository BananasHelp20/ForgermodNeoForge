package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import java.util.*;

/** A moving enemy-only storm owns its victims until the common storm/levitation deadline. */
@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class EyeOfStormEvents {
    public static final String ID="tooltips.forgermod.ability.eye_of_the_storm";
    private static final StormState WINDOWS=new StormState();
    private static final class Victim {
        final LivingEntity entity;
        MobEffectInstance owned,previous;
        long previousAt,deadline;
        int amplifier;
        Vec3 lastCenter;
        Victim(LivingEntity entity,Vec3 center){this.entity=entity;lastCenter=center;}
    }
    private static final class Storm {
        final ItemStack stack;final int slot;final ServerLevel level;final double radius;
        final Map<UUID,Victim> victims=new HashMap<>();
        Storm(ServerPlayer player,ItemStack stack,int rank){this.stack=stack;slot=player.getInventory().selected;level=player.serverLevel();radius=ReviewedRanks.stormRadius(rank);}
    }
    private static final Map<UUID,Storm> STORMS=new HashMap<>();
    private EyeOfStormEvents(){}
    public static boolean activate(ServerPlayer player,ItemStack stack){
        if(active(player))return false;
        int rank=Augmentations.level(stack,ID);
        if(rank<=0 || !WINDOWS.activate(player.getUUID(),player.level().getGameTime(),JadeAugments.duration(stack,ID,ReviewedRanks.stormTicks(rank))))return false;
        STORMS.put(player.getUUID(),new Storm(player,stack,rank));tick(player);return true;
    }
    public static boolean active(ServerPlayer player){
        var storm=STORMS.get(player.getUUID());if(storm==null)return false;
        if(player.isAlive() && !player.isSpectator() && player.serverLevel()==storm.level && player.getMainHandItem()==storm.stack
                && player.getInventory().selected==storm.slot && Augmentations.ids(storm.stack,true).contains(ID)
                && WINDOWS.isActive(player.getUUID(),player.level().getGameTime()))return true;
        cancel(player.getUUID());return false;
    }
    public static void cancel(UUID owner){
        var storm=STORMS.remove(owner);WINDOWS.clear(owner);if(storm==null)return;
        for(var victim:storm.victims.values())release(victim,storm.level.getGameTime());
    }
    private static void levitate(Storm storm,Victim victim,int remaining){
        var current=victim.entity.getEffect(MobEffects.LEVITATION);long now=storm.level.getGameTime();
        int amplifier=JadeAugments.amplifier(storm.stack,ID,0);
        if(current==victim.owned && current!=null && current.getDuration()!=-1 && current.getAmplifier()==amplifier && current.getDuration()<=remaining+1)return;
        // A stronger or longer independent effect remains its owner's effect.
        if(current!=null && (current.getAmplifier()>amplifier || current.getAmplifier()==amplifier && (current.getDuration()==-1 || current.getDuration()>=remaining))){victim.owned=null;victim.previous=null;return;}
        victim.previous=current==null?null:new MobEffectInstance(current);victim.previousAt=now;
        victim.entity.addEffect(new MobEffectInstance(MobEffects.LEVITATION,remaining,amplifier));
        victim.owned=victim.entity.getEffect(MobEffects.LEVITATION);victim.amplifier=amplifier;victim.deadline=now+remaining;
    }
    private static void release(Victim victim,long now){
        var current=victim.entity.getEffect(MobEffects.LEVITATION);
        if(current!=null && current==victim.owned && current.getDuration()!=-1 && current.getAmplifier()==victim.amplifier && current.getDuration()<=Math.max(0,victim.deadline-now)+1){
            victim.entity.removeEffect(MobEffects.LEVITATION);
            if(victim.previous!=null){
                int duration=victim.previous.getDuration()==-1?-1:(int)Math.max(0,victim.previous.getDuration()-(now-victim.previousAt));
                if(duration!=0)victim.entity.addEffect(new MobEffectInstance(MobEffects.LEVITATION,duration,victim.previous.getAmplifier(),victim.previous.isAmbient(),victim.previous.isVisible(),victim.previous.showIcon()));
            }
        }
    }
    private static boolean clear(Storm storm,LivingEntity target,Vec3 position){
        var box=target.getBoundingBox().move(position.subtract(target.position()));
        return box.minY>=storm.level.getMinBuildHeight() && box.maxY<=storm.level.getMaxBuildHeight()
                && storm.level.getWorldBorder().isWithinBounds(box)
                && storm.level.hasChunksAt(BlockPos.containing(box.minX,box.minY,box.minZ),BlockPos.containing(Math.nextDown(box.maxX),Math.nextDown(box.maxY),Math.nextDown(box.maxZ)))
                && !storm.level.getBlockCollisions(target,box).iterator().hasNext();
    }
    public static void tick(ServerPlayer player){
        if(!active(player))return;var storm=STORMS.get(player.getUUID());long now=storm.level.getGameTime();
        int remaining=WINDOWS.remainingTicks(player.getUUID(),now);
        for(var target:storm.level.getEntitiesOfClass(LivingEntity.class,player.getBoundingBox().inflate(storm.radius),
                e->WeaponAbilityTargets.enemy(player,e) && !e.isSpectator() && player.distanceToSqr(e)<=storm.radius*storm.radius))
            storm.victims.computeIfAbsent(target.getUUID(),id->new Victim(target,player.position()));
        storm.victims.values().removeIf(victim->{
            var target=victim.entity;
            if(target.isRemoved() || target.isSpectator() || target.level()!=storm.level || !WeaponAbilityTargets.enemy(player,target)){
                release(victim,now);return true;
            }
            target.stopRiding();target.ejectPassengers();
            Vec3 destination=target.position().add(player.position().subtract(victim.lastCenter));var relative=destination.subtract(player.position());
            if(relative.lengthSqr()>storm.radius*storm.radius)destination=player.position().add(relative.normalize().scale(Math.max(.5,storm.radius-.5)));
            if(destination.distanceToSqr(target.position())>.000001){
                if(!clear(storm,target,destination)){
                    // Search safe points inside the storm instead of placing victims into walls.
                    destination=null;
                    search:for(double height:new double[]{0,1,2,-1})for(double radius:new double[]{1,2,3})for(int side=0;side<8;side++){
                        var point=player.position().add(Math.cos(side*Math.PI/4)*radius,height,Math.sin(side*Math.PI/4)*radius);
                        if(point.distanceToSqr(player.position())<storm.radius*storm.radius && clear(storm,target,point)){destination=point;break search;}
                    }
                }
                if(destination!=null)target.teleportTo(storm.level,destination.x,destination.y,destination.z,Set.of(),target.getYRot(),target.getXRot());
            }
            levitate(storm,victim,remaining);
            victim.lastCenter=player.position();
            var flat=target.position().subtract(player.position()).multiply(1,0,1);if(flat.lengthSqr()<.01)flat=new Vec3(1,0,0);
            var outward=flat.normalize();var tangent=new Vec3(-outward.z,0,outward.x);
            target.setDeltaMovement(tangent.scale(.35).subtract(outward.scale(.08)).add(0,.16,0));target.hasImpulse=true;target.hurtMarked=true;
            if(target instanceof ServerPlayer other)other.connection.send(new net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket(other));
            if(now%5==0)storm.level.sendParticles(ParticleTypes.CLOUD,target.getX(),target.getY()+target.getBbHeight()*.5,target.getZ(),2,.2,.3,.2,.01);
            return false;
        });
    }
    @SubscribeEvent public static void travel(net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent event){
        if(event.getDimension().equals(event.getEntity().level().dimension()))return;
        for(var storm:STORMS.values()){
            var victim=storm.victims.remove(event.getEntity().getUUID());if(victim!=null)release(victim,storm.level.getGameTime());
        }
        if(event.getEntity() instanceof ServerPlayer player)cancel(player.getUUID());
    }
}
