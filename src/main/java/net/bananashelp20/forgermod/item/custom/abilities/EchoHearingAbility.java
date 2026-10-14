package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.augmentation.RecommendedAbilities;
import net.minecraft.core.particles.VibrationParticleOption;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gameevent.EntityPositionSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.VanillaGameEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

/** Reveals an entity briefly whenever it emits a vibration within the moving listener's range. */
@EventBusSubscriber(modid = ForgerMod.MOD_ID)
public final class EchoHearingAbility {
    private record Listening(ServerLevel level, ItemStack stack, int slot, long expires, int radius) {}
    private record Glow(ServerLevel level, long expires) {}
    private static final Map<UUID, Listening> LISTENERS = new HashMap<>();
    private static final Map<Entity, Glow> NONLIVING_GLOWS = new HashMap<>();

    private EchoHearingAbility() {}

    public static boolean activate(ServerPlayer player, ItemStack stack) {
        if (LISTENERS.containsKey(player.getUUID())) return false;
        int rank = Math.clamp(Augmentations.level(stack, RecommendedAbilities.ECHO_HEARING), 1, 4);
        int radius = 24 + 8 * rank;
        int duration = 160 + 40 * rank;
        LISTENERS.put(player.getUUID(), new Listening(player.serverLevel(), stack,
                player.getInventory().selected, player.level().getGameTime() + duration, radius));
        return true;
    }

    public static boolean active(ServerPlayer player) {
        Listening listening = LISTENERS.get(player.getUUID());
        if (listening == null) return false;
        if (player.isAlive() && player.serverLevel() == listening.level()
                && player.level().getGameTime() < listening.expires()) return true;
        cancel(player.getUUID());
        return false;
    }

    public static void cancel(UUID playerId) {
        LISTENERS.remove(playerId);
    }

    @SubscribeEvent
    public static void onNoise(VanillaGameEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        var source = event.getCause();
        if (source instanceof Projectile projectile && projectile.getOwner() instanceof LivingEntity owner) source = owner;
        if (source == null || !source.isAlive()) return;
        Entity noisy = source;

        for (var entry : LISTENERS.entrySet()) {
            ServerPlayer listener = level.getServer().getPlayerList().getPlayer(entry.getKey());
            Listening state = entry.getValue();
            if (listener == null || listener == noisy || listener.serverLevel() != level
                    || !listener.isAlive() || listener.level().getGameTime() >= state.expires()
                    || listener.getMainHandItem() != state.stack()
                    || listener.getInventory().selected != state.slot()
                    || listener.distanceToSqr(noisy) > (double) state.radius() * state.radius()) continue;

            if (noisy instanceof LivingEntity living) {
                living.addEffect(new MobEffectInstance(MobEffects.GLOWING, 40, 0));
            } else {
                Glow previous = NONLIVING_GLOWS.get(noisy);
                if (previous != null) NONLIVING_GLOWS.put(noisy, new Glow(level, level.getGameTime() + 40));
                else if (!noisy.isCurrentlyGlowing()) {
                    noisy.setGlowingTag(true);
                    NONLIVING_GLOWS.put(noisy, new Glow(level, level.getGameTime() + 40));
                }
            }
            int travelTicks = Math.clamp((int) Math.ceil(listener.distanceTo(noisy) / 2.0), 5, 30);
            level.sendParticles(listener, new VibrationParticleOption(new EntityPositionSource(listener, 1.0F), travelTicks),
                    true, noisy.getX(), noisy.getY() + noisy.getBbHeight() / 2,
                    noisy.getZ(), 1, 0, 0, 0, 0);
        }
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        Iterator<Map.Entry<Entity, Glow>> iterator = NONLIVING_GLOWS.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            Glow glow = entry.getValue();
            if (glow.level() != level) continue;
            if (entry.getKey().isRemoved() || level.getGameTime() >= glow.expires()) {
                entry.getKey().setGlowingTag(false);
                iterator.remove();
            }
        }
    }

    @SubscribeEvent
    public static void onLeave(EntityLeaveLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (NONLIVING_GLOWS.remove(event.getEntity()) != null) event.getEntity().setGlowingTag(false);
    }
}
