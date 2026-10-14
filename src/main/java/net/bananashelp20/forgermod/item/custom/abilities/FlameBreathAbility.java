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
import net.minecraft.world.phys.Vec3;

public final class FlameBreathAbility {
    private FlameBreathAbility() {}
    public static boolean activate(ServerPlayer player, ItemStack stack) {
        int rank = Augmentations.level(stack, AdditionalAbilities.FLAME_BREATH);
        var origin = player.getEyePosition(); var look = player.getLookAngle();
        double range = 6 + rank;
        for (LivingEntity target : player.level().getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().inflate(range), entity -> WeaponAbilityTargets.enemy(player, entity))) {
            var direction = target.getBoundingBox().getCenter().subtract(origin);
            if (direction.lengthSqr() > range * range || direction.normalize().dot(look) < Math.cos(Math.toRadians(35))
                    || !player.hasLineOfSight(target)) continue;
            if (target.hurt(player.damageSources().indirectMagic(player, player), (float)player.getAttributeValue(Attributes.ATTACK_DAMAGE)))
                target.setRemainingFireTicks(Math.max(target.getRemainingFireTicks(), (2 + rank) * 20));
        }
        Vec3 right = look.cross(new Vec3(0, 1, 0));
        right = right.lengthSqr() < 1.0E-6 ? new Vec3(1, 0, 0) : right.normalize();
        Vec3 up = right.cross(look).normalize();
        for (double step = .5; step <= range; step += .5) {
            var point = origin.add(look.scale(step));
            if (player.level().clip(new ClipContext(origin, point, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player)).getType() == HitResult.Type.MISS)
                player.serverLevel().sendParticles(ParticleTypes.FLAME, point.x, point.y, point.z, 8, step * .1, step * .1, step * .1, .015);
            if (step % 1 != 0) continue;
            double coneRadius = Math.min(step * Math.tan(Math.toRadians(35)),
                    Math.sqrt(Math.max(0, range * range - step * step))) * .95;
            for (int angle = 0; angle < 8; angle++) {
                double radians = angle * Math.PI / 4;
                var edge = point.add(right.scale(Math.cos(radians) * coneRadius)).add(up.scale(Math.sin(radians) * coneRadius));
                if (edge.distanceToSqr(origin) > range * range || player.level().clip(new ClipContext(origin, edge,
                        ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player)).getType() != HitResult.Type.MISS) continue;
                player.serverLevel().sendParticles(ParticleTypes.FLAME, edge.x, edge.y, edge.z, 2, .1, .1, .1, .005);
            }
        }
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1, .8F);
        return true;
    }
}
