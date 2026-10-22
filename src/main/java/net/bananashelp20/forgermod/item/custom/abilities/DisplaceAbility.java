package net.bananashelp20.forgermod.item.custom.abilities;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import java.util.Set;

public final class DisplaceAbility {
    private DisplaceAbility() {}
    public static boolean activate(ServerPlayer player,ItemStack stack) {
        if(player.isPassenger() || player.isVehicle()) return false;
        boolean maximum=net.bananashelp20.forgermod.augmentation.Augmentations.level(stack,
                net.bananashelp20.forgermod.augmentation.AdditionalAbilities.DISPLACE)>=4;
        var target=WeaponAbilityTargets.aimedLoadedEntity(player,maximum?Double.POSITIVE_INFINITY:10,
                entity->(maximum || entity instanceof net.minecraft.world.entity.LivingEntity) && !entity.isInvisibleTo(player));
        if(target==null || target.isPassenger() || target.isVehicle()) return false;
        var from=player.position();var to=target.position();var level=player.serverLevel();
        if(!WeaponTeleportSafety.clear(player,level,to,target) || !WeaponTeleportSafety.clear(target,level,from,player)) return false;
        if(!target.teleportTo(level,from.x,from.y,from.z,Set.of(),target.getYRot(),target.getXRot())) return false;
        if(!player.teleportTo(level,to.x,to.y,to.z,Set.of(),player.getYRot(),player.getXRot())) {
            target.teleportTo(level,to.x,to.y,to.z,Set.of(),target.getYRot(),target.getXRot());return false;
        }
        player.fallDistance=0;target.fallDistance=0;player.setDeltaMovement(Vec3.ZERO);target.setDeltaMovement(Vec3.ZERO);
        if(target instanceof ServerPlayer other) other.connection.send(new net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket(other));
        player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket(player));
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.PORTAL,from.x,from.y+1,from.z,20,.3,.7,.3,.1);
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.PORTAL,to.x,to.y+1,to.z,20,.3,.7,.3,.1);
        return true;
    }
}
