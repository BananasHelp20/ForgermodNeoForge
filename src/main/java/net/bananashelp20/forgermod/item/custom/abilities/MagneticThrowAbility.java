package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.augmentation.AdditionalAbilities;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.core.particles.ParticleTypes;

public final class MagneticThrowAbility {
    private MagneticThrowAbility() {}
    public static boolean activate(ServerPlayer player,ItemStack stack) {
        var target=WeaponAbilityTargets.aimedEnemy(player,player.entityInteractionRange());
        if(target==null) return false;
        target.setDeltaMovement(player.getLookAngle().scale(.9+.2*Augmentations.level(stack,AdditionalAbilities.MAGNETIC_THROW)));
        target.hasImpulse=true;
        if(target instanceof ServerPlayer other) other.connection.send(new ClientboundSetEntityMotionPacket(other));
        player.serverLevel().sendParticles(ParticleTypes.ELECTRIC_SPARK,target.getX(),target.getY()+1,target.getZ(),15,.3,.4,.3,.08);
        return true;
    }
}
