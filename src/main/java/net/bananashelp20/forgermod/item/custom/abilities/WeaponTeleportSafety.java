package net.bananashelp20.forgermod.item.custom.abilities;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

public final class WeaponTeleportSafety {
    private WeaponTeleportSafety() {}
    public static boolean clear(Entity entity,ServerLevel level,Vec3 destination,Entity ignored) {
        var box=entity.getBoundingBox().move(destination.subtract(entity.position()));
        if(box.minY<level.getMinBuildHeight() || box.maxY>level.getMaxBuildHeight() || !level.getWorldBorder().isWithinBounds(box)) return false;
        for(int x=Mth.floor(box.minX)>>4;x<=(Mth.floor(Math.nextDown(box.maxX))>>4);x++)
            for(int z=Mth.floor(box.minZ)>>4;z<=(Mth.floor(Math.nextDown(box.maxZ))>>4);z++)
                if(!level.getChunkSource().hasChunk(x,z)) return false;
        if(level.getBlockCollisions(entity,box).iterator().hasNext()
                || !level.getEntities(entity,box,e->e!=ignored && e.isAlive() && !e.isSpectator()
                        && (e instanceof LivingEntity || e.canBeCollidedWith())).isEmpty()) return false;
        return level.getFluidState(BlockPos.containing(destination)).isEmpty()
                && level.getFluidState(BlockPos.containing(destination.add(0,entity.getBbHeight()-.1,0))).isEmpty();
    }
}
