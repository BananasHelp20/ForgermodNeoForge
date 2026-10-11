package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.augmentation.AdditionalAbilities;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public final class AdditionalAbilityRuntime {
    private AdditionalAbilityRuntime() {}
    public static boolean activate(ServerPlayer player, ItemStack stack, String id) {
        if (AdditionalAbilities.FLAME_BREATH.equals(id)) return FlameBreathAbility.activate(player, stack);
        if (AdditionalAbilities.MAGMA_TOSS.equals(id)) return MagmaTossEvents.activate(player, stack);
        if (AdditionalAbilities.DISPLACE.equals(id)) return DisplaceAbility.activate(player, stack);
        if (AdditionalAbilities.VOID_PULSE.equals(id)) return VoidPulseAbility.activate(player, stack);
        if (AdditionalAbilities.GRAVITY_WELL.equals(id)) return GravityWellEvents.activate(player, stack);
        return false;
    }
    public static boolean active(ServerPlayer player, String id) { return AdditionalAbilities.MAGMA_TOSS.equals(id) && MagmaTossEvents.active(player)
            || AdditionalAbilities.GRAVITY_WELL.equals(id) && GravityWellEvents.active(player); }
    public static void cancel(ServerPlayer player, String id) { if (AdditionalAbilities.MAGMA_TOSS.equals(id)) MagmaTossEvents.cancel(player.getUUID()); if (AdditionalAbilities.GRAVITY_WELL.equals(id)) GravityWellEvents.cancel(player.getUUID()); }
}
