package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.augmentation.RecommendedAbilities;
import net.bananashelp20.forgermod.item.custom.IgnisiumWeapon;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** A terrain-safe fire burst with temporary protection shared by all Ignisium weapon shapes. */
public final class FlamebornAbility {
    private static final double RADIUS = 10.0;
    private record Active(ServerLevel level, long start, long expires, MobEffectInstance previous,
                          boolean appliedResistance, int previousFireTicks) {}
    private static final Map<UUID, Active> ACTIVE = new HashMap<>();
    private FlamebornAbility() {}

    public static boolean activate(ServerPlayer player, ItemStack stack) {
        if (!(stack.getItem() instanceof IgnisiumWeapon)
                || !Augmentations.ids(stack, true).contains(RecommendedAbilities.FLAMEBORN)
                || ACTIVE.containsKey(player.getUUID())) return false;
        int burnTicks = Augmentations.level(stack, RecommendedAbilities.FLAMEBORN) > 1 ? 140 : 100;
        MobEffectInstance current = player.getEffect(MobEffects.FIRE_RESISTANCE);
        MobEffectInstance previous = current == null ? null : new MobEffectInstance(current);
        boolean applied = player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 600));
        int previousFireTicks = player.getRemainingFireTicks();
        player.setRemainingFireTicks(Math.max(player.getRemainingFireTicks(), burnTicks));
        long now = player.level().getGameTime();
        ACTIVE.put(player.getUUID(), new Active(player.serverLevel(), now, now + 600,
                previous, applied, previousFireTicks));
        for (LivingEntity target : player.serverLevel().getEntitiesOfClass(LivingEntity.class,
                player.getBoundingBox().inflate(RADIUS),
                entity -> WeaponAbilityTargets.enemy(player, entity) && player.distanceToSqr(entity) <= RADIUS * RADIUS)) {
            target.setRemainingFireTicks(Math.max(target.getRemainingFireTicks(), burnTicks));
        }
        // Visual burst only: terrain and nearby allies are left untouched.
        for (int radius = 2; radius <= 10; radius += 2) {
            for (int point = 0; point < 24; point++) {
                double angle = 2 * Math.PI * point / 24;
                player.serverLevel().sendParticles(ParticleTypes.FLAME,
                        player.getX() + Math.cos(angle) * radius, player.getY() + .25,
                        player.getZ() + Math.sin(angle) * radius, 1, 0, .1, 0, .01);
            }
        }
        player.serverLevel().sendParticles(ParticleTypes.LARGE_SMOKE, player.getX(), player.getY() + 1,
                player.getZ(), 24, 1, .5, 1, .03);
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(),
                SoundSource.PLAYERS, .8f, 1.3f);
        return true;
    }

    public static boolean active(ServerPlayer player) {
        Active state = ACTIVE.get(player.getUUID());
        if (state == null) return false;
        if (player.isAlive() && player.serverLevel() == state.level() && player.level().getGameTime() < state.expires())
            return true;
        cancel(player);
        return false;
    }

    public static void cancel(ServerPlayer player) {
        Active state = ACTIVE.remove(player.getUUID());
        if (state == null) return;
        int elapsed = (int)Math.min(600, Math.max(0, player.level().getGameTime() - state.start()));
        if (state.appliedResistance()) {
            MobEffectInstance current = player.getEffect(MobEffects.FIRE_RESISTANCE);
            if (current != null && current.getAmplifier() == 0 && current.getDuration() <= 602 - elapsed) {
                player.removeEffect(MobEffects.FIRE_RESISTANCE);
                if (state.previous() != null && state.previous().getDuration() > elapsed)
                    player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE,
                            state.previous().getDuration() - elapsed, state.previous().getAmplifier()));
            }
        }
        if (player.getRemainingFireTicks() > state.previousFireTicks())
            player.setRemainingFireTicks(Math.max(0, state.previousFireTicks() - elapsed));
    }
}
