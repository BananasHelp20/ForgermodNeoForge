package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.augmentation.AdditionalAbilities;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;

public final class FlameBreathAbility {
    private FlameBreathAbility() {}
    public static boolean activate(ServerPlayer player, ItemStack stack) {
        int rank = Augmentations.level(stack, AdditionalAbilities.FLAME_BREATH);
        var origin = player.getEyePosition(); var look = player.getLookAngle();
        double range = 3 + rank;
        for (LivingEntity target : player.level().getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().inflate(range), entity -> WeaponAbilityTargets.enemy(player, entity))) {
            var direction = target.getBoundingBox().getCenter().subtract(origin);
            if (direction.lengthSqr() > range * range || direction.normalize().dot(look) < Math.cos(Math.toRadians(35))
                    || !player.hasLineOfSight(target)) continue;
            if (target.hurt(player.damageSources().indirectMagic(player, player), (float)player.getAttributeValue(Attributes.ATTACK_DAMAGE)))
                target.setRemainingFireTicks(Math.max(target.getRemainingFireTicks(), (2 + rank) * 20));
        }
        for (double step = .5; step <= range; step += .5) {
            var point = origin.add(look.scale(step));
            if (player.level().clip(new ClipContext(origin, point, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player)).getType() != HitResult.Type.MISS) break;
            player.serverLevel().sendParticles(ParticleTypes.FLAME, point.x, point.y, point.z, 8, step * .12, step * .12, step * .12, .015);
        }
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1, .8F);
        return true;
    }
}
