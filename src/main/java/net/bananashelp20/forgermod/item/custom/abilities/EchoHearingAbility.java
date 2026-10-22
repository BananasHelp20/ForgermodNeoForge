package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.augmentation.RecommendedAbilities;
import net.bananashelp20.forgermod.augmentation.JadeAugments;
import net.minecraft.core.particles.VibrationParticleOption;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gameevent.EntityPositionSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.VanillaGameEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Reveals an entity briefly whenever it emits a vibration within the moving listener's range. */
@EventBusSubscriber(modid = ForgerMod.MOD_ID)
public final class EchoHearingAbility {
    private record Listening(ServerPlayer player, ServerLevel level, ItemStack stack, int slot, long expires, int radius) {}
    private static final Map<UUID, Listening> LISTENERS = new HashMap<>();

    private EchoHearingAbility() {}

    public static boolean activate(ServerPlayer player, ItemStack stack) {
        if (LISTENERS.containsKey(player.getUUID())) return false;
        int rank = Math.clamp(Augmentations.level(stack, RecommendedAbilities.ECHO_HEARING), 1, 4);
        int radius = 24 + 8 * rank;
        int duration = 160 + 40 * rank;
        LISTENERS.put(player.getUUID(), new Listening(player, player.serverLevel(), stack,
                player.getInventory().selected, player.level().getGameTime() + duration, radius));
        return true;
    }

    public static boolean active(ServerPlayer player) {
        Listening listening = LISTENERS.get(player.getUUID());
        if (listening == null) return false;
        if (player.isAlive() && player.serverLevel() == listening.level()
                && player.level().getGameTime() < listening.expires()
                && player.getMainHandItem() == listening.stack()
                && player.getInventory().selected == listening.slot()
                && Augmentations.ids(listening.stack(),true).contains(RecommendedAbilities.ECHO_HEARING)) return true;
        cancel(player.getUUID());
        return false;
    }

    public static void cancel(UUID playerId) {
        Listening state = LISTENERS.remove(playerId);
        if (state != null) EchoHearingHighlights.clear(state.player());
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

            int glowTicks=JadeAugments.duration(state.stack(),RecommendedAbilities.ECHO_HEARING,
                    net.bananashelp20.forgermod.augmentation.ReviewedRanks.echoGlowTicks(
                            Augmentations.level(state.stack(),RecommendedAbilities.ECHO_HEARING)));
            EchoHearingHighlights.reveal(listener,noisy,glowTicks);
            int travelTicks = Math.clamp((int) Math.ceil(listener.distanceTo(noisy) / 2.0), 5, 30);
            level.sendParticles(listener, new VibrationParticleOption(new EntityPositionSource(listener, 1.0F), travelTicks),
                    true, noisy.getX(), noisy.getY() + noisy.getBbHeight() / 2,
                    noisy.getZ(), 1, 0, 0, 0, 0);
        }
    }

    @SubscribeEvent
    public static void logout(net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent event) {
        cancel(event.getEntity().getUUID());
    }

    @SubscribeEvent
    public static void travel(net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent event) {
        if(event.getEntity() instanceof ServerPlayer player)cancel(player.getUUID());
    }
}
