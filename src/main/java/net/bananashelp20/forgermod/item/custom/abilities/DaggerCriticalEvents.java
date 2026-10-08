package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.item.custom.SwordItemWithEffect;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

@EventBusSubscriber(modid = ForgerMod.MOD_ID)
public final class DaggerCriticalEvents {
    private static final DaggerCriticalTracker TRACKER = new DaggerCriticalTracker();

    private DaggerCriticalEvents() {}

    @SubscribeEvent
    public static void onCriticalHit(CriticalHitEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !(event.getTarget() instanceof LivingEntity target)
                || !event.isCriticalHit()) return;
        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof SwordItemWithEffect weapon) || !weapon.isDagger()) return;
        TRACKER.mark(player.getUUID(), target.getId(), player.level().getGameTime(), stack.getItem());
    }

    public static boolean consume(ServerPlayer player, LivingEntity target, ItemStack stack) {
        return TRACKER.consume(player.getUUID(), target.getId(), player.level().getGameTime(), stack.getItem());
    }

    public static boolean matches(ServerPlayer player, LivingEntity target, ItemStack stack) {
        return TRACKER.matches(player.getUUID(), target.getId(), player.level().getGameTime(), stack.getItem());
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        TRACKER.clear(event.getEntity().getUUID());
    }
}
