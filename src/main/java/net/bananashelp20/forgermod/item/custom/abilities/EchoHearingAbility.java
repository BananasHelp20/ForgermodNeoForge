package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.augmentation.RecommendedAbilities;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/** One immediate reveal pulse. Nonliving entities need a timed glowing tag because mob effects only apply to living entities. */
@EventBusSubscriber(modid = ForgerMod.MOD_ID)
public final class EchoHearingAbility {
    private record Glow(ServerLevel level, long expires) {}
    private static final Map<Entity, Glow> NONLIVING_GLOWS = new HashMap<>();

    private EchoHearingAbility() {}

    public static boolean activate(ServerPlayer player, ItemStack stack) {
        int rank = Math.clamp(Augmentations.level(stack, RecommendedAbilities.ECHO_HEARING), 1, 4);
        int radius = 24 + 8 * rank;
        int duration = 160 + 40 * rank;
        ServerLevel level = player.serverLevel();
        AABB area = AABB.ofSize(player.position(), 2 * radius, 2 * radius, 2 * radius);
        double rangeSquared = (double) radius * radius;

        for (Entity target : level.getEntitiesOfClass(Entity.class, area,
                entity -> !entity.isRemoved() && entity.distanceToSqr(player) <= rangeSquared)) {
            if (target instanceof LivingEntity living) {
                living.addEffect(new MobEffectInstance(MobEffects.GLOWING, duration, 0));
            } else {
                Glow existing = NONLIVING_GLOWS.get(target);
                if (existing != null) {
                    NONLIVING_GLOWS.put(target, new Glow(level, Math.max(existing.expires, level.getGameTime() + duration)));
                } else if (!target.isCurrentlyGlowing()) {
                    target.setGlowingTag(true);
                    NONLIVING_GLOWS.put(target, new Glow(level, level.getGameTime() + duration));
                }
            }
        }
        return true;
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        Iterator<Map.Entry<Entity, Glow>> iterator = NONLIVING_GLOWS.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            Glow glow = entry.getValue();
            if (glow.level != level) continue;
            if (entry.getKey().isRemoved() || level.getGameTime() >= glow.expires) {
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
