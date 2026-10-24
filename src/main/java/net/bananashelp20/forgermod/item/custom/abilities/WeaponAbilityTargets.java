package net.bananashelp20.forgermod.item.custom.abilities;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;

public final class WeaponAbilityTargets {
    private WeaponAbilityTargets() {}
    public static boolean enemy(ServerPlayer owner,LivingEntity target) {
        return target.isAlive() && hostileTo(owner,target);
    }
    public static boolean hostileTo(ServerPlayer owner,LivingEntity target) {
        return target!=owner && !target.isAlliedTo(owner) && (target instanceof Enemy
                || target instanceof Player player && owner.server.isPvpAllowed() && owner.canHarmPlayer(player)
                || target instanceof Mob mob && mob.getTarget()==owner);
    }
    public static boolean ally(ServerPlayer owner,LivingEntity target) {
        return target!=owner && target.isAlive() && (target.isAlliedTo(owner) || owner.isAlliedTo(target)
            || target instanceof net.minecraft.world.entity.TamableAnimal pet && pet.isOwnedBy(owner)
            || target instanceof Player player && (!owner.server.isPvpAllowed() || !owner.canHarmPlayer(player)));
    }
    public static boolean hostileTo(LivingEntity actor,LivingEntity target){
        if(actor instanceof ServerPlayer player)return hostileTo(player,target);
        if(!(actor instanceof BoneGuardEvents.FriendlyGuard guard))return false;
        return target!=actor && target!=guard.owner() && !(target instanceof BoneGuardEvents.FriendlyGuard)
                && !ally(guard.owner(),target) && (hostileTo(guard.owner(),target) || target instanceof Mob mob && mob.getTarget()==actor);
    }
    public static boolean enemy(LivingEntity actor,LivingEntity target){return target.isAlive() && hostileTo(actor,target);}
    public static boolean ally(LivingEntity actor,LivingEntity target){
        if(actor instanceof ServerPlayer player)return ally(player,target);
        if(!(actor instanceof BoneGuardEvents.FriendlyGuard guard))return false;
        return target!=actor && target.isAlive() && (target==guard.owner() || target instanceof BoneGuardEvents.FriendlyGuard other && other.owner()==guard.owner() || ally(guard.owner(),target));
    }
    public static LivingEntity aimedEnemy(ServerPlayer player,double range) {
        var start=player.getEyePosition(); var end=start.add(player.getLookAngle().scale(range));
        var wall=player.level().clip(new ClipContext(start,end,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,player));
        if(wall.getType()!=HitResult.Type.MISS) end=wall.getLocation();
        var hit=ProjectileUtil.getEntityHitResult(player,start,end,player.getBoundingBox().expandTowards(end.subtract(start)).inflate(1),
                target->target instanceof LivingEntity living && enemy(player,living),start.distanceToSqr(end));
        return hit==null?null:(LivingEntity)hit.getEntity();
    }

    /** Unbounded aim only scans entities already loaded and never traces through unloaded chunks. */
    public static net.minecraft.world.entity.Entity aimedLoadedEntity(ServerPlayer player,double range,
            java.util.function.Predicate<net.minecraft.world.entity.Entity> eligible) {
        var start=player.getEyePosition();var look=player.getLookAngle();
        net.minecraft.world.entity.Entity nearestEntity=null;net.minecraft.world.phys.Vec3 nearestHit=null;
        double nearest=range*range;
        for(var entity:player.serverLevel().getAllEntities()) {
            if(entity==player || !entity.isAlive() || entity.isRemoved() || entity.isSpectator() || !eligible.test(entity))continue;
            double reach=entity.getBoundingBox().getCenter().distanceTo(start)+entity.getBbWidth()+entity.getBbHeight();
            var hit=entity.getBoundingBox().inflate(entity.getPickRadius()).clip(start,start.add(look.scale(Math.min(range,reach))));
            if(hit.isPresent() && start.distanceToSqr(hit.get())<=nearest) {
                nearest=start.distanceToSqr(hit.get());nearestEntity=entity;nearestHit=hit.get();
            }
        }
        if(nearestEntity==null || !loadedLine(player.serverLevel(),start,nearestHit))return null;
        return player.level().clip(new ClipContext(start,nearestHit,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,player))
                .getType()==HitResult.Type.MISS?nearestEntity:null;
    }
    private static boolean loadedLine(net.minecraft.server.level.ServerLevel level,net.minecraft.world.phys.Vec3 start,net.minecraft.world.phys.Vec3 end) {
        int x=net.minecraft.util.Mth.floor(start.x)>>4,z=net.minecraft.util.Mth.floor(start.z)>>4;
        int ex=net.minecraft.util.Mth.floor(end.x)>>4,ez=net.minecraft.util.Mth.floor(end.z)>>4;
        double dx=end.x-start.x,dz=end.z-start.z;
        int sx=dx>0?1:dx<0?-1:0,sz=dz>0?1:dz<0?-1:0;
        double tx=sx==0?Double.POSITIVE_INFINITY:((sx>0?(x+1)*16.0:x*16.0)-start.x)/dx;
        double tz=sz==0?Double.POSITIVE_INFINITY:((sz>0?(z+1)*16.0:z*16.0)-start.z)/dz;
        while(true) {
            if(!level.getChunkSource().hasChunk(x,z))return false;
            if(x==ex && z==ez)return true;
            if(tx<tz){x+=sx;tx+=16/Math.abs(dx);}
            else if(tz<tx){z+=sz;tz+=16/Math.abs(dz);}
            else{x+=sx;z+=sz;tx+=16/Math.abs(dx);tz+=16/Math.abs(dz);}
        }
    }
}
