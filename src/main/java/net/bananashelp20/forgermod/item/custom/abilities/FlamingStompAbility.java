package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.augmentation.RecommendedAbilities;
import net.bananashelp20.forgermod.item.custom.IgnisiumWeapon;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Holds one forced vertical strike until the next axe attack. */
public final class FlamingStompAbility {
    private static final Map<UUID, ItemStack> ARMED = new HashMap<>();
    private FlamingStompAbility() {}

    public static boolean arm(ServerPlayer player, ItemStack stack) {
        if (!(stack.getItem() instanceof IgnisiumWeapon weapon) || !weapon.isAxe()
                || !player.getOffhandItem().isEmpty() || ARMED.containsKey(player.getUUID())
                || !Augmentations.ids(stack, true).contains(RecommendedAbilities.FLAMING_STOMP)) return false;
        ARMED.put(player.getUUID(), stack);
        return true;
    }

    public static boolean active(ServerPlayer player) {
        return ARMED.containsKey(player.getUUID());
    }

    public static boolean consume(ServerPlayer player, ItemStack stack) {
        if (!active(player) || ARMED.get(player.getUUID()) != stack) return false;
        ARMED.remove(player.getUUID());
        return true;
    }

    public static void cancel(UUID player) {
        ARMED.remove(player);
    }
}
