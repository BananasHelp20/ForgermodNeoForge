package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.AdditionalAbilities;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import java.util.*;

@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class DowndraftBellAbility {
    private record Gust(ServerPlayer owner,ItemStack stack,int selected,ServerLevel level,long expires,Set<Mob> targets) {}
    private record Step(double y,double downward) {}
    private static final Map<UUID,Gust> GUSTS=new HashMap<>();
    private static final Map<Mob,Map<UUID,Double>> FORCES=new HashMap<>();
    private static final Map<Mob,Step> STEPS=new HashMap<>();
    private DowndraftBellAbility() {}
    public static boolean activate(ServerPlayer player,ItemStack stack) {
        if(active(player)) return false;
        int rank=Augmentations.level(stack,AdditionalAbilities.DOWNDRAFT_BELL);if(rank<=0) return false;
        double radius=2+2*rank;var box=player.getBoundingBox();
        var column=new AABB(box.minX-radius,player.getY(),box.minZ-radius,box.maxX+radius,Double.MAX_VALUE,box.maxZ+radius);
        var targets=player.level().getEntitiesOfClass(Mob.class,column,e->e.isAlive() && e.getY()>player.getY() && !e.onGround()
            && !e.isInWaterOrBubble() && !e.isInLava() && Math.pow(e.getX()-player.getX(),2)+Math.pow(e.getZ()-player.getZ(),2)<=radius*radius);
        if(targets.isEmpty()) return false;
        var owner=player.getUUID();double downward=-2-.2*(rank-1);
        GUSTS.put(owner,new Gust(player,stack,player.getInventory().selected,player.serverLevel(),player.level().getGameTime()+200,new HashSet<>(targets)));
        for(var target:targets) {
            FORCES.computeIfAbsent(target,e->new HashMap<>()).put(owner,downward);
            push(target,downward);
            player.serverLevel().sendParticles(net.minecraft.core.particles.ParticleTypes.CLOUD,target.getX(),target.getY()+1,target.getZ(),12,.4,.2,.4,.04);
        }
        player.swing(net.minecraft.world.InteractionHand.MAIN_HAND);return true;
    }
    public static boolean active(ServerPlayer player) {
        var gust=GUSTS.get(player.getUUID());if(gust==null) return false;
        if(!player.isAlive() || player.isSpectator() || player.getMainHandItem()!=gust.stack() || player.getInventory().selected!=gust.selected()
            || player.serverLevel()!=gust.level() || player.level().getGameTime()>=gust.expires()
            || !Augmentations.ids(gust.stack(),true).contains(AdditionalAbilities.DOWNDRAFT_BELL)) {cancel(player.getUUID());return false;}
        return true;
    }
    public static void cancel(UUID owner) {
        var gust=GUSTS.remove(owner);if(gust==null) return;
        for(var target:gust.targets()) {
            var forces=FORCES.get(target);if(forces==null) continue;forces.remove(owner);
            if(forces.isEmpty()) {FORCES.remove(target);STEPS.remove(target);}
        }
    }
    private static double force(Mob target) {
        var forces=FORCES.get(target);if(forces==null) return 0;
        for(var owner:List.copyOf(forces.keySet())) {
            var gust=GUSTS.get(owner);
            if(gust==null || !active(gust.owner())) forces.remove(owner);
            else if(!target.isAlive() || target.isRemoved() || target.level()!=gust.level()) {forces.remove(owner);gust.targets().remove(target);}
        }
        if(forces.isEmpty()) {FORCES.remove(target);STEPS.remove(target);return 0;}
        return forces.values().stream().min(Double::compare).orElse(0.0);
    }
    private static void push(Mob target,double downward) {
        var motion=target.getDeltaMovement();target.setDeltaMovement(motion.x,Math.min(motion.y,downward),motion.z);target.hasImpulse=true;
    }
    @SubscribeEvent public static void before(EntityTickEvent.Pre event) {
        if(!(event.getEntity() instanceof Mob target) || target.level().isClientSide) return;
        double downward=force(target);if(downward==0 || WeaponControlLocks.frozen(target)) return;
        downward=Math.min(downward,target.getDeltaMovement().y);STEPS.put(target,new Step(target.getY(),downward));push(target,downward);
    }
    @SubscribeEvent public static void after(EntityTickEvent.Post event) {
        if(!(event.getEntity() instanceof Mob target) || target.level().isClientSide) return;
        double downward=force(target);var step=STEPS.remove(target);if(downward==0 || step==null || WeaponControlLocks.frozen(target)) return;
        // Flying AI may overwrite velocity during its tick. Complete only the missing descent with vanilla collision physics.
        double missing=step.y()+step.downward()-target.getY();
        if(missing<-.000001) target.move(MoverType.SELF,new Vec3(0,missing,0));
        push(target,downward);
        if(target.tickCount%20==0 && target.level() instanceof ServerLevel level)
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.CLOUD,target.getX(),target.getY()+1,target.getZ(),4,.25,.15,.25,.02);
    }
    @SubscribeEvent public static void tick(PlayerTickEvent.Post event) {if(event.getEntity() instanceof ServerPlayer player) active(player);}
    @SubscribeEvent public static void despawn(net.neoforged.neoforge.event.entity.living.MobDespawnEvent event) {
        if(event.getResult()==net.neoforged.neoforge.event.entity.living.MobDespawnEvent.Result.DEFAULT
            && event.getEntity().level().getDifficulty()!=net.minecraft.world.Difficulty.PEACEFUL && force(event.getEntity())<0)
            event.setResult(net.neoforged.neoforge.event.entity.living.MobDespawnEvent.Result.DENY);
    }
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {cancel(event.getEntity().getUUID());}
}
