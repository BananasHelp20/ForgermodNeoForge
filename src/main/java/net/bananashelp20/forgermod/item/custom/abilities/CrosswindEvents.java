package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.AdditionalAbilities;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import java.util.*;

@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class CrosswindEvents {
    private record Wind(ItemStack stack,int selected,ServerLevel level,long expires) {}
    private static final Map<UUID,Wind> WINDS=new HashMap<>();
    private CrosswindEvents() {}
    public static boolean activate(ServerPlayer player,ItemStack stack) {if(active(player)) return false;WINDS.put(player.getUUID(),new Wind(stack,player.getInventory().selected,player.serverLevel(),player.level().getGameTime()+2400));return true;}
    public static boolean active(ServerPlayer player) {
        var wind=WINDS.get(player.getUUID());if(wind==null) return false;
        if(!player.isAlive() || player.isSpectator() || player.getMainHandItem()!=wind.stack() || player.getInventory().selected!=wind.selected() || player.serverLevel()!=wind.level()
            || player.level().getGameTime()>=wind.expires() || !Augmentations.ids(wind.stack(),true).contains(AdditionalAbilities.CROSSWIND)) {cancel(player.getUUID());return false;}return true;
    }
    public static long remainingTicks(ServerPlayer player) {return active(player)?WINDS.get(player.getUUID()).expires()-player.level().getGameTime():0;}
    public static void cancel(UUID owner) {WINDS.remove(owner);}
    public static boolean incoming(ServerPlayer player,Projectile projectile) {
        if(projectile.getOwner()==player || !projectile.isAlive()) return false;
        var motion=projectile.getDeltaMovement();var start=projectile.position();
        return motion.lengthSqr()>.0001 && motion.dot(player.getBoundingBox().getCenter().subtract(start))>0
            && player.getBoundingBox().inflate(.6).clip(start,start.add(motion.scale(8))).isPresent();
    }
    private static void deflect(ServerPlayer player,Projectile projectile) {
        var motion=projectile.getDeltaMovement();double speed=Math.max(.3,motion.length());
        var side=new Vec3(motion.z,0,-motion.x);if(side.lengthSqr()<.0001) side=new Vec3(1,0,0);else side=side.normalize();
        var random=player.getRandom();if(random.nextBoolean()) side=side.scale(-1);
        var away=projectile.position().subtract(player.getBoundingBox().getCenter());
        if(away.dot(side)<-.000001) side=side.scale(-1);
        var forward=motion.lengthSqr()>.000001?motion.normalize():away.normalize().scale(-1);
        if(forward.lengthSqr()<.000001) forward=new Vec3(0,0,1);
        var up=side.cross(forward).normalize();
        var lateral=side.add(forward.scale(-(.2+random.nextDouble()*.3)));
        var lift=up.scale((random.nextDouble()*2-1)*.35);
        if(lateral.add(lift).dot(away)<0) lift=lift.scale(-1);
        var next=lateral.add(lift).normalize().scale(speed);
        projectile.setDeltaMovement(next);projectile.hasImpulse=true;
        player.serverLevel().getChunkSource().broadcastAndSend(projectile,new net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket(projectile));
        player.serverLevel().sendParticles(net.minecraft.core.particles.ParticleTypes.CLOUD,projectile.getX(),projectile.getY(),projectile.getZ(),4,.2,.2,.2,.04);
    }
    public static void advance(ServerPlayer player) {
        if(!active(player)) return;
        for(var projectile:player.level().getEntitiesOfClass(Projectile.class,player.getBoundingBox().inflate(12),e->incoming(player,e))) deflect(player,projectile);
    }
    @SubscribeEvent
    public static void tick(ServerTickEvent.Pre event) {
        for(var id:List.copyOf(WINDS.keySet())) {var player=event.getServer().getPlayerList().getPlayer(id);if(player==null) cancel(id);else advance(player);}
    }
    @SubscribeEvent
    public static void impact(ProjectileImpactEvent event) {
        if(event.getRayTraceResult() instanceof EntityHitResult hit && hit.getEntity() instanceof ServerPlayer player && active(player) && event.getProjectile().getOwner()!=player) {
            event.setCanceled(true);deflect(player,event.getProjectile());
        }
    }
    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {cancel(event.getEntity().getUUID());}
}
