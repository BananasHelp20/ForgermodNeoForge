package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.augmentation.RecommendedAbilities;
import net.bananashelp20.forgermod.item.custom.InanisiumWeapon;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Set;

/** Immediately swings at close enemies and lifts victims through a clear vertical column. */
public final class EnderSwingAbility {
    private static final double RADIUS = 1.0;
    private static final int LIFT = 20;
    private EnderSwingAbility() {}

    public static boolean activate(ServerPlayer player, ItemStack stack) {
        if (!player.isAlive() || player.isSpectator() || WeaponControlLocks.frozen(player)
                || stack != player.getMainHandItem()
                || !(stack.getItem() instanceof InanisiumWeapon weapon) || !weapon.isAxe()
                || !player.getOffhandItem().isEmpty()
                || !Augmentations.ids(stack, true).contains(RecommendedAbilities.ENDER_SWING)) return false;
        strike(player, stack);
        return true;
    }

    private static void strike(ServerPlayer player, ItemStack stack) {
        float damage = (float)player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        if (Augmentations.level(stack, RecommendedAbilities.ENDER_SWING) > 1) damage *= 1.2f;
        player.swing(InteractionHand.MAIN_HAND);
        for (LivingEntity target : player.serverLevel().getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().inflate(RADIUS),
                living -> WeaponAbilityTargets.enemy(player, living) && player.distanceToSqr(living) <= RADIUS * RADIUS)) {
            int previousImmunity = target.invulnerableTime;
            target.invulnerableTime = 0;
            if (target.hurt(player.damageSources().playerAttack(player), damage)) {
                stack.getItem().postHurtEnemy(stack, target, player);
                if (target.isAlive() && clearLift(player, target)) lift(player, target);
            } else {
                target.invulnerableTime = previousImmunity;
            }
            if (stack.isEmpty()) break;
        }
        for (int point = 0; point < 16; point++) {
            double angle = 2 * Math.PI * point / 16;
            player.serverLevel().sendParticles(ParticleTypes.SWEEP_ATTACK,
                    player.getX() + Math.cos(angle) * RADIUS, player.getY() + 1,
                    player.getZ() + Math.sin(angle) * RADIUS, 1, 0, 0, 0, 0);
        }
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP,
                SoundSource.PLAYERS, 1, .7f);
    }

    private static boolean clearLift(ServerPlayer player, LivingEntity target) {
        if (target.isPassenger() || target.isVehicle()) return false;
        var level = player.serverLevel();
        AABB box = target.getBoundingBox();
        if (box.maxY + LIFT > level.getMaxBuildHeight()
                || !level.getChunkSource().hasChunk(target.blockPosition().getX() >> 4,
                        target.blockPosition().getZ() >> 4)) return false;
        for (int step = 1; step <= LIFT; step++) {
            AABB moved = box.move(0, step, 0);
            if (!level.getWorldBorder().isWithinBounds(moved) || !level.noCollision(target, moved)) return false;
        }
        return true;
    }

    private static void lift(ServerPlayer player, LivingEntity target) {
        Vec3 start = target.position();
        if (target instanceof ServerPlayer other) {
            if (!other.teleportTo(player.serverLevel(), start.x, start.y + LIFT, start.z,
                    Set.of(), other.getYRot(), other.getXRot())) return;
        } else {
            target.teleportTo(start.x, start.y + LIFT, start.z);
        }
        target.setDeltaMovement(Vec3.ZERO);
        target.fallDistance = 0;
        player.serverLevel().sendParticles(ParticleTypes.PORTAL, start.x, start.y + 1, start.z,
                24, .4, .8, .4, .1);
        player.serverLevel().sendParticles(ParticleTypes.PORTAL, target.getX(), target.getY() + 1, target.getZ(),
                24, .4, .8, .4, .1);
        player.serverLevel().playSound(null, target.blockPosition(), SoundEvents.ENDERMAN_TELEPORT,
                SoundSource.PLAYERS, .8f, 1.2f);
    }
}
