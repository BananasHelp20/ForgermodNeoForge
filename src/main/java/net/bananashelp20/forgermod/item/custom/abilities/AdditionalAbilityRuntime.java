package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.augmentation.AdditionalAbilities;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class AdditionalAbilityRuntime {
    private AdditionalAbilityRuntime() {}
    public static boolean activate(ServerPlayer player, ItemStack stack, String id) {
        if (AdditionalAbilities.FLAME_BREATH.equals(id)) return FlameBreathAbility.activate(player, stack);
        return false;
    }
    public static boolean active(ServerPlayer player, String id) { return false; }
    public static void cancel(ServerPlayer player, String id) {}
}
