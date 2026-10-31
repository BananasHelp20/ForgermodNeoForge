package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.AdditionalAbilities;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.damagesource.DamageTypes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import java.util.*;

@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class AlternatingCurrentEvents {
    private static final class Link {
        final ItemStack stack;final int selected;long expires,nextPulse;LivingEntity first,second;boolean pulseFirst=true;
        Link(ServerPlayer player,ItemStack stack) {this.stack=stack;selected=player.getInventory().selected;expires=player.level().getGameTime()+600;}
    }
    private static final Map<UUID,Link> LINKS=new HashMap<>();
    private AlternatingCurrentEvents() {}
    public static int damage(int rank) { return 1+Math.clamp(rank,1,5); }
    private static boolean validTarget(ServerPlayer player,LivingEntity target) {
        return target.isAlive() && !target.isRemoved()
                && (target instanceof Mob || WeaponAbilityTargets.enemy(player,target));
    }
    private static LivingEntity aimed(ServerPlayer player){
        var from=player.getEyePosition();var to=from.add(player.getLookAngle().scale(20));
        var wall=player.level().clip(new net.minecraft.world.level.ClipContext(from,to,net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,player));
        if(wall.getType()!=net.minecraft.world.phys.HitResult.Type.MISS)to=wall.getLocation();
        var hit=net.minecraft.world.entity.projectile.ProjectileUtil.getEntityHitResult(player,from,to,player.getBoundingBox().expandTowards(to.subtract(from)).inflate(1),
                entity->entity instanceof LivingEntity living && validTarget(player,living),from.distanceToSqr(to));
        return hit==null?null:(LivingEntity)hit.getEntity();
    }
    public static boolean activate(ServerPlayer player,ItemStack stack){
        if(!player.isAlive() || player.isSpectator() || WeaponControlLocks.frozen(player) || stack!=player.getMainHandItem() || Augmentations.level(stack,AdditionalAbilities.ALTERNATING_CURRENT)<=0)return false;
        active(player);var link=LINKS.get(player.getUUID());var target=aimed(player);if(target==null)return false;
        if(link==null){link=new Link(player,stack);link.first=target;LINKS.put(player.getUUID(),link);player.serverLevel().sendParticles(player,net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK,true,target.getX(),target.getEyeY()+.2,target.getZ(),6,.15,.15,.15,.02);return true;}
        if(link.second!=null || link.first==target || link.first.distanceToSqr(target)>400 || !link.first.hasLineOfSight(target))return false;
        link.second=target;link.expires=player.level().getGameTime()+1200;link.nextPulse=player.level().getGameTime()+20;pulse(player,link);return true;
    }
    public static boolean active(ServerPlayer player) {
        var link=LINKS.get(player.getUUID());if(link==null) return false;
        if(!player.isAlive() || player.isSpectator() || player.getMainHandItem()!=link.stack || player.getInventory().selected!=link.selected
            || player.level().getGameTime()>=link.expires || !Augmentations.ids(link.stack,true).contains(AdditionalAbilities.ALTERNATING_CURRENT)
            || link.first!=null && (!validTarget(player,link.first) || link.first.level()!=player.level())
            || link.second!=null && (!validTarget(player,link.second) || link.second.level()!=player.level() || link.first.distanceToSqr(link.second)>400)) {cancel(player.getUUID());return false;}
        return true;
    }
    public static long remainingTicks(ServerPlayer player) {return active(player)?LINKS.get(player.getUUID()).expires-player.level().getGameTime():0;}
    public static void cancel(UUID owner) {LINKS.remove(owner);WeaponControlLocks.release(owner,AdditionalAbilities.ALTERNATING_CURRENT);}
    private static void pulse(ServerPlayer player,Link link){
        if(!link.first.hasLineOfSight(link.second))return;
        for(var target:java.util.List.of(link.first,link.second)){
            int immunity=target.invulnerableTime;target.invulnerableTime=0;
            if(!target.hurt(player.damageSources().source(DamageTypes.LIGHTNING_BOLT,player),damage(Augmentations.level(link.stack,AdditionalAbilities.ALTERNATING_CURRENT))))target.invulnerableTime=immunity;
            WeaponControlLocks.acquire(target,player.getUUID(),AdditionalAbilities.ALTERNATING_CURRENT,10);
        }
        var from=link.pulseFirst?link.first:link.second;var to=link.pulseFirst?link.second:link.first;
        net.bananashelp20.forgermod.particle.LightningArcParticles.emit(player.serverLevel(),from.getEyePosition(),to.getEyePosition(),-1,5);
        net.bananashelp20.forgermod.sound.ElectricalTravel.emit(player.serverLevel(),from.getEyePosition(),to.getEyePosition());
        link.pulseFirst=!link.pulseFirst;
    }
    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post event) {
        if(!(event.getEntity() instanceof ServerPlayer player) || !active(player)) return;var link=LINKS.get(player.getUUID());if(link.second==null || player.level().getGameTime()<link.nextPulse) return;
        pulse(player,link);link.nextPulse=player.level().getGameTime()+20;
    }
    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {cancel(event.getEntity().getUUID());}
}
