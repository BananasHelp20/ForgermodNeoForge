package net.bananashelp20.forgermod.item.custom.abilities;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.phys.Vec3;
import java.util.Set;

public final class JustADreamAbility {
    private JustADreamAbility() {}
    public static boolean activate(ServerPlayer player,ItemStack stack) {
        if(player.isPassenger() || player.isVehicle()) return false;
        // keepInventory=true also prevents spending a respawn-anchor charge in 1.21.1.
        var destination=player.findRespawnPositionAndUseSpawnBlock(true,DimensionTransition.DO_NOTHING);
        var level=destination.newLevel();var pos=destination.pos();
        var box=player.getBoundingBox().move(pos.subtract(player.position()));
        for(int x=Mth.floor(box.minX)>>4;x<=(Mth.floor(Math.nextDown(box.maxX))>>4);x++)
            for(int z=Mth.floor(box.minZ)>>4;z<=(Mth.floor(Math.nextDown(box.maxZ))>>4);z++) level.getChunk(x,z);
        if(!WeaponTeleportSafety.clear(player,level,pos,null)) return false;
        if(!player.teleportTo(level,pos.x,pos.y,pos.z,Set.of(),player.getYRot(),player.getXRot())) return false;
        player.fallDistance=0;player.setDeltaMovement(Vec3.ZERO);
        player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket(player));
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD,pos.x,pos.y+1,pos.z,25,.3,.5,.3,.03);
        return true;
    }
}
