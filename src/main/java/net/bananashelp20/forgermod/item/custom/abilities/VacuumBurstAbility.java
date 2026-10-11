package net.bananashelp20.forgermod.item.custom.abilities;
import net.bananashelp20.forgermod.augmentation.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.particles.ParticleTypes;
public final class VacuumBurstAbility {
    private VacuumBurstAbility() {}
    public static boolean activate(ServerPlayer player,ItemStack stack) {
        double radius=4+2*Augmentations.level(stack,AdditionalAbilities.VACUUM_BURST);
        for(var entity:player.serverLevel().getEntities(player,player.getBoundingBox().inflate(radius),e->e instanceof ItemEntity || e instanceof ExperienceOrb)) {
            if(player.distanceToSqr(entity)>radius*radius || !player.hasLineOfSight(entity)) continue;
            var direction=player.position().add(0,.4,0).subtract(entity.position());
            entity.setDeltaMovement(direction.normalize().scale(.9));entity.hasImpulse=true;
        }
        player.serverLevel().sendParticles(ParticleTypes.CLOUD,player.getX(),player.getY()+.5,player.getZ(),24,.8,.3,.8,.08);return true;
    }
}
