package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.AdditionalAbilities;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.*;
import net.minecraft.world.level.ClipContext;
import net.minecraft.core.particles.ParticleTypes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import java.util.*;

@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class StaticBladeEvents {
    private static final class Blade {
        final ItemStack stack;final int selected;final ServerLevel level;long expires;
        Vec3 start,direction,right,up;float damage;double traveled;final Set<UUID> hit=new HashSet<>();
        boolean finished;Blade secondary;ItemStack pendingOffhand;long offhandUntil;
        Blade(ServerPlayer player,ItemStack stack) {this.stack=stack;selected=player.getInventory().selected;level=player.serverLevel();expires=level.getGameTime()+600;}
    }
    private static final Map<UUID,Blade> BLADES=new HashMap<>();
    private StaticBladeEvents() {}
    public static boolean activate(ServerPlayer player,ItemStack stack) {
        if(active(player)) return false;BLADES.put(player.getUUID(),new Blade(player,stack));return true;
    }
    public static boolean active(ServerPlayer player) {
        var blade=BLADES.get(player.getUUID());if(blade==null) return false;
        if(!player.isAlive() || player.isSpectator() || player.getMainHandItem()!=blade.stack || player.getInventory().selected!=blade.selected
            || player.serverLevel()!=blade.level || player.level().getGameTime()>=blade.expires || !Augmentations.ids(blade.stack,true).contains(AdditionalAbilities.STATIC_BLADE)) {
            cancel(player.getUUID());return false;
        }
        return true;
    }
    public static void cancel(UUID owner) {BLADES.remove(owner);}
    public static boolean release(ServerPlayer player) {
        if(WeaponControlLocks.frozen(player) || !active(player)) return false;var blade=BLADES.get(player.getUUID());if(blade.start!=null) return false;
        launch(player,blade);
        if(net.bananashelp20.forgermod.item.custom.attacks.dagger.DaggerItems.hasMatchingDaggers(player)) {
            blade.pendingOffhand=player.getOffhandItem();blade.offhandUntil=player.level().getGameTime()+10;
        }
        return true;
    }
    /** Only the paired offhand swing may consume the one extra echo reserved by the main swing. */
    public static boolean releaseOffhand(ServerPlayer player) {
        if(WeaponControlLocks.frozen(player) || !active(player)) return false;
        var blade=BLADES.get(player.getUUID());
        if(blade.pendingOffhand==null || player.level().getGameTime()>blade.offhandUntil
            || player.getOffhandItem()!=blade.pendingOffhand
            || !net.bananashelp20.forgermod.item.custom.attacks.dagger.DaggerItems.hasMatchingDaggers(player)) return false;
        blade.pendingOffhand=null;blade.secondary=new Blade(player,blade.stack);launch(player,blade.secondary);
        blade.expires=blade.secondary.expires;return true;
    }
    private static void launch(ServerPlayer player,Blade blade) {
        blade.expires=player.level().getGameTime()+21;
        blade.start=player.getEyePosition();blade.direction=player.getLookAngle();blade.right=blade.direction.cross(new Vec3(0,1,0));
        if(blade.right.lengthSqr()<.0001) blade.right=new Vec3(1,0,0);else blade.right=blade.right.normalize();
        blade.up=blade.right.cross(blade.direction).normalize();blade.damage=(float)player.getAttributeValue(Attributes.ATTACK_DAMAGE);
    }
    private static double extent(AABB box,Vec3 axis) {
        return (Math.abs(axis.x)*box.getXsize()+Math.abs(axis.y)*box.getYsize()+Math.abs(axis.z)*box.getZsize())*.5;
    }
    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post event) {
        if(!(event.getEntity() instanceof ServerPlayer player) || !active(player)) return;
        var blade=BLADES.get(player.getUUID());if(blade.start==null) return;
        if(blade.pendingOffhand!=null && (player.level().getGameTime()>blade.offhandUntil || player.getOffhandItem()!=blade.pendingOffhand
            || !net.bananashelp20.forgermod.item.custom.attacks.dagger.DaggerItems.hasMatchingDaggers(player))) blade.pendingOffhand=null;
        advance(player,blade);if(blade.secondary!=null) advance(player,blade.secondary);
        if(blade.finished && blade.pendingOffhand==null && (blade.secondary==null || blade.secondary.finished)) cancel(player.getUUID());
    }
    private static void advance(ServerPlayer player,Blade blade) {
        if(blade.finished) return;
        var from=blade.start.add(blade.direction.scale(blade.traveled));double step=Math.min(1,20-blade.traveled);boolean blocked=true;
        double furthestWall=0;
        // Any open sample lets the echo pass: only a fully occupied cross-section stops it.
        for(int x=-4;x<=4;x++) for(int y=-1;y<=1;y++) {
            var offset=blade.right.scale(Math.clamp(x*.5,-1.999,1.999)).add(blade.up.scale(y*.249));var a=from.add(offset);var b=a.add(blade.direction.scale(step));
            var wall=blade.level.clip(new ClipContext(a,b,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,player));
            if(wall.getType()==HitResult.Type.MISS) blocked=false;
            else furthestWall=Math.max(furthestWall,a.distanceTo(wall.getLocation()));
        }
        if(blocked) step=Math.min(step,furthestWall);
        var middle=from.add(blade.direction.scale(step*.5));
        for(var target:blade.level.getEntitiesOfClass(LivingEntity.class,AABB.ofSize(middle,7,7,7),e->WeaponAbilityTargets.enemy(player,e) && !blade.hit.contains(e.getUUID()))) {
            var box=target.getBoundingBox();var delta=box.getCenter().subtract(from);
            if(delta.dot(blade.direction)+extent(box,blade.direction)<0 || delta.dot(blade.direction)-extent(box,blade.direction)>step
                || Math.abs(delta.dot(blade.right))>2+extent(box,blade.right) || Math.abs(delta.dot(blade.up))>.25+extent(box,blade.up)) continue;
            blade.hit.add(target.getUUID());int immunity=target.invulnerableTime;target.invulnerableTime=0;
            try {target.hurt(player.damageSources().indirectMagic(player,player),blade.damage);}finally {target.invulnerableTime=Math.max(immunity,target.invulnerableTime);}
        }
        var front=from.add(blade.direction.scale(step));
        for(int x=-4;x<=4;x++) {
            var point=front.add(blade.right.scale(x*.5));blade.level.sendParticles(ParticleTypes.ELECTRIC_SPARK,point.x,point.y,point.z,2,.05,.12,.05,.01);
        }
        blade.traveled+=step;if(blocked || blade.traveled>=20) blade.finished=true;
    }
    @SubscribeEvent
    public static void attack(AttackEntityEvent event) {if(!event.isCanceled() && event.getEntity() instanceof ServerPlayer player) release(player);}
    @SubscribeEvent
    public static void block(PlayerInteractEvent.LeftClickBlock event) {if(!event.isCanceled() && event.getEntity() instanceof ServerPlayer player) release(player);}
    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {cancel(event.getEntity().getUUID());}
}
