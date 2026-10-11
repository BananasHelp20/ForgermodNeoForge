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
        if (AdditionalAbilities.JUST_A_DREAM.equals(id)) return JustADreamAbility.activate(player, stack);
        if (AdditionalAbilities.LULLABY.equals(id)) return LullabyEvents.activate(player, stack);
        if (AdditionalAbilities.DREAM_FEAST.equals(id)) return DreamFeastAbility.activate(player);
        if (AdditionalAbilities.MAGNETIC_THROW.equals(id)) return MagneticThrowAbility.activate(player,stack);
        if (AdditionalAbilities.LIVE_WIRE.equals(id)) return LiveWireEvents.activate(player,stack);
        if (AdditionalAbilities.STATIC_BLADE.equals(id)) return StaticBladeEvents.activate(player,stack);
        if (AdditionalAbilities.ALTERNATING_CURRENT.equals(id)) return AlternatingCurrentEvents.activate(player,stack);
        if (AdditionalAbilities.DIRECT_CURRENT.equals(id)) return DirectCurrentEvents.activate(player,stack);
        return false;
    }
    public static boolean active(ServerPlayer player, String id) { return AdditionalAbilities.MAGMA_TOSS.equals(id) && MagmaTossEvents.active(player)
            || AdditionalAbilities.GRAVITY_WELL.equals(id) && GravityWellEvents.active(player)
            || AdditionalAbilities.LULLABY.equals(id) && LullabyEvents.active(player)
            || AdditionalAbilities.LIVE_WIRE.equals(id) && LiveWireEvents.active(player)
            || AdditionalAbilities.STATIC_BLADE.equals(id) && StaticBladeEvents.active(player)
            || AdditionalAbilities.ALTERNATING_CURRENT.equals(id) && AlternatingCurrentEvents.active(player)
            || AdditionalAbilities.DIRECT_CURRENT.equals(id) && DirectCurrentEvents.active(player); }
    public static void cancel(ServerPlayer player, String id) { if (AdditionalAbilities.DIRECT_CURRENT.equals(id)) DirectCurrentEvents.cancel(player.getUUID()); if (AdditionalAbilities.ALTERNATING_CURRENT.equals(id)) AlternatingCurrentEvents.cancel(player.getUUID()); if (AdditionalAbilities.STATIC_BLADE.equals(id)) StaticBladeEvents.cancel(player.getUUID()); if (AdditionalAbilities.LIVE_WIRE.equals(id)) LiveWireEvents.cancel(player.getUUID()); if (AdditionalAbilities.LULLABY.equals(id)) LullabyEvents.cancel(player.getUUID()); if (AdditionalAbilities.MAGMA_TOSS.equals(id)) MagmaTossEvents.cancel(player.getUUID()); if (AdditionalAbilities.GRAVITY_WELL.equals(id)) GravityWellEvents.cancel(player.getUUID()); }
}
