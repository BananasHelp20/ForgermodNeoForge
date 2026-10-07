package net.bananashelp20.forgermod.item.custom.attacks.axe;

import net.bananashelp20.forgermod.ForgerMod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@EventBusSubscriber(modid = ForgerMod.MOD_ID)
public final class AxeOffhandRestriction {
    private AxeOffhandRestriction() {}

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)
                || !AxeItems.isAxe(player.getMainHandItem())) return;

        ItemStack offhand = player.getOffhandItem();
        if (offhand.isEmpty()) return;

        ItemStack moving = offhand.copy();
        player.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        player.getInventory().add(moving);
        // Inventory.add may fit only part of the stack. Drop any remainder.
        if (!moving.isEmpty()) player.drop(moving, false);
    }

    private static boolean isBlocked(PlayerInteractEvent event) {
        Player player = event.getEntity();
        return event.getHand() == InteractionHand.OFF_HAND
                && AxeItems.isAxe(player.getMainHandItem());
    }

    @SubscribeEvent
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (isBlocked(event)) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (isBlocked(event)) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (isBlocked(event)) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        if (isBlocked(event)) event.setCanceled(true);
    }
}
