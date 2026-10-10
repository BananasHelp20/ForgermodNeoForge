package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.augmentation.RecommendedAbilities;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import java.util.Set;

public final class DeathStareAbility {
    private DeathStareAbility() {}
    public static double range(int rank) { return switch(rank) { case 1->30; case 2->60; case 3->120; default->Double.POSITIVE_INFINITY; }; }
    public static Mob target(ServerPlayer player,double range) {
        var start=player.getEyePosition(); var look=player.getLookAngle();
        Mob closest=null; Vec3 hit=null; double nearest=range*range;
        // Iterate loaded entities, rather than requesting a world-sized spatial query/chunk load.
        for(var entity:player.serverLevel().getAllEntities()) {
            if(!(entity instanceof Mob mob) || !mob.isAlive() || mob.isAlliedTo(player)) continue;
            double distance=mob.getBoundingBox().getCenter().distanceTo(start)+mob.getBbHeight()+mob.getBbWidth();
            var intersection=mob.getBoundingBox().inflate(mob.getPickRadius()).clip(start,start.add(look.scale(Math.min(range,distance))));
            if(intersection.isPresent() && start.distanceToSqr(intersection.get())<nearest) {
                closest=mob; hit=intersection.get(); nearest=start.distanceToSqr(hit);
            }
        }
        if(closest==null || !loadedLine(player.serverLevel(),start,hit)) return null;
        var wall=player.level().clip(new ClipContext(start,hit,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,player));
        return wall.getType()==HitResult.Type.MISS?closest:null;
    }
    private static boolean loadedLine(ServerLevel level,Vec3 start,Vec3 end) {
        int x=Mth.floor(start.x)>>4,z=Mth.floor(start.z)>>4,ex=Mth.floor(end.x)>>4,ez=Mth.floor(end.z)>>4;
        double dx=end.x-start.x,dz=end.z-start.z;
        int sx=dx>0?1:dx<0?-1:0,sz=dz>0?1:dz<0?-1:0;
        double tx=sx==0?Double.POSITIVE_INFINITY:((sx>0?(x+1)*16.0:x*16.0)-start.x)/dx;
        double tz=sz==0?Double.POSITIVE_INFINITY:((sz>0?(z+1)*16.0:z*16.0)-start.z)/dz;
        while(true) {
            if(!level.getChunkSource().hasChunk(x,z)) return false;
            if(x==ex && z==ez) return true;
            if(tx<tz) { x+=sx; tx+=16/Math.abs(dx); }
            else if(tz<tx) { z+=sz; tz+=16/Math.abs(dz); }
            else { x+=sx; z+=sz; tx+=16/Math.abs(dx); tz+=16/Math.abs(dz); }
        }
    }
    private static boolean clear(ServerPlayer player,Vec3 destination) {
        var level=player.serverLevel(); var box=player.getBoundingBox().move(destination.subtract(player.position()));
        if(box.minY<level.getMinBuildHeight() || box.maxY>level.getMaxBuildHeight() || !level.getWorldBorder().isWithinBounds(box)) return false;
        for(int x=Mth.floor(box.minX)>>4;x<=(Mth.floor(Math.nextDown(box.maxX))>>4);x++)
            for(int z=Mth.floor(box.minZ)>>4;z<=(Mth.floor(Math.nextDown(box.maxZ))>>4);z++)
                if(!level.getChunkSource().hasChunk(x,z)) return false;
        return level.noCollision(player,box) && level.getFluidState(BlockPos.containing(destination)).isEmpty()
                && level.getFluidState(BlockPos.containing(destination.add(0,player.getBbHeight()-.1,0))).isEmpty();
    }
    public static boolean activate(ServerPlayer player,ItemStack stack) {
        if(player.isPassenger()) return false;
        Mob target=target(player,range(Augmentations.level(stack,RecommendedAbilities.DEATH_STARE))); if(target==null) return false;
        var approach=player.position().subtract(target.position()).multiply(1,0,1).normalize();
        if(approach.lengthSqr()<.1) approach=new Vec3(0,0,-1);
        double separation=(target.getBbWidth()+player.getBbWidth())/2+.5;
        for(int side=0;side<8;side++) {
            double angle=side*Math.PI/4;
            var direction=new Vec3(approach.x*Math.cos(angle)-approach.z*Math.sin(angle),0,approach.x*Math.sin(angle)+approach.z*Math.cos(angle));
            for(double height:new double[]{0,.5,1,-.5}) {
                var destination=target.position().add(direction.scale(separation)).add(0,height,0);
                if(!clear(player,destination)) continue;
                var departure=player.blockPosition();
                if(player.teleportTo(player.serverLevel(),destination.x,destination.y,destination.z,Set.of(),player.getYRot(),player.getXRot())) {
                    player.fallDistance=0;
                    player.serverLevel().playSound(null,departure,SoundEvents.ENDERMAN_TELEPORT,SoundSource.PLAYERS,.7F,1.2F);
                    player.serverLevel().playSound(null,player.blockPosition(),SoundEvents.ENDERMAN_TELEPORT,SoundSource.PLAYERS,.7F,1.2F);
                    return true;
                }
            }
        }
        return false;
    }
}
