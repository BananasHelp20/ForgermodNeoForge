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
        if (AdditionalAbilities.STUNNING_PRESENCE.equals(id)) return StunningPresenceEvents.activate(player,stack);
        if (AdditionalAbilities.DOWNDRAFT_BELL.equals(id)) return DowndraftBellAbility.activate(player,stack);
        if (AdditionalAbilities.CROSSWIND.equals(id)) return CrosswindEvents.activate(player,stack);
        if (AdditionalAbilities.UPWIND.equals(id)) return UpwindEvents.activate(player,stack);
        if (AdditionalAbilities.WINDCHARGE.equals(id)) return WindchargeEvents.activate(player,stack);
        if (AdditionalAbilities.VACUUM_BURST.equals(id)) return VacuumBurstAbility.activate(player,stack);
        if (AdditionalAbilities.FEATHER_SHELTER.equals(id)) return FeatherShelterAbility.activate(player,stack);
        if (AdditionalAbilities.POCKET_STORM.equals(id)) return PocketStormEvents.activate(player,stack);
        if (AdditionalAbilities.BLOOD_RUSH.equals(id)) return BloodRushAbility.activate(player);
        if (AdditionalAbilities.BLOOD_SACRAFICE.equals(id)) return BloodSacrificeEvents.activate(player,stack);
        if (AdditionalAbilities.GREEN_FEAST.equals(id)) return GreenFeastAbility.activate(player);
        if (AdditionalAbilities.GHOSTWALK.equals(id)) return GhostwalkEvents.activate(player,stack);
        return false;
    }
    public static boolean active(ServerPlayer player, String id) { return AdditionalAbilities.MAGMA_TOSS.equals(id) && MagmaTossEvents.active(player)
            || AdditionalAbilities.GRAVITY_WELL.equals(id) && GravityWellEvents.active(player)
            || AdditionalAbilities.LULLABY.equals(id) && LullabyEvents.active(player)
            || AdditionalAbilities.LIVE_WIRE.equals(id) && LiveWireEvents.active(player)
            || AdditionalAbilities.STATIC_BLADE.equals(id) && StaticBladeEvents.active(player)
            || AdditionalAbilities.ALTERNATING_CURRENT.equals(id) && AlternatingCurrentEvents.active(player)
            || AdditionalAbilities.DIRECT_CURRENT.equals(id) && DirectCurrentEvents.active(player)
            || AdditionalAbilities.STUNNING_PRESENCE.equals(id) && StunningPresenceEvents.active(player)
            || AdditionalAbilities.CROSSWIND.equals(id) && CrosswindEvents.active(player)
            || AdditionalAbilities.UPWIND.equals(id) && UpwindEvents.active(player)
            || AdditionalAbilities.WINDCHARGE.equals(id) && WindchargeEvents.active(player)
            || AdditionalAbilities.POCKET_STORM.equals(id) && PocketStormEvents.active(player)
            || AdditionalAbilities.BLOOD_SACRAFICE.equals(id) && BloodSacrificeEvents.active(player)
            || AdditionalAbilities.GHOSTWALK.equals(id) && GhostwalkEvents.active(player); }
    public static void cancel(ServerPlayer player, String id) { if (AdditionalAbilities.GHOSTWALK.equals(id)) GhostwalkEvents.cancel(player.getUUID()); if (AdditionalAbilities.BLOOD_SACRAFICE.equals(id)) BloodSacrificeEvents.cancel(player.getUUID()); if (AdditionalAbilities.POCKET_STORM.equals(id)) PocketStormEvents.cancel(player.getUUID()); if (AdditionalAbilities.WINDCHARGE.equals(id)) WindchargeEvents.cancel(player.getUUID()); if (AdditionalAbilities.UPWIND.equals(id)) UpwindEvents.cancel(player.getUUID()); if (AdditionalAbilities.CROSSWIND.equals(id)) CrosswindEvents.cancel(player.getUUID()); if (AdditionalAbilities.STUNNING_PRESENCE.equals(id)) StunningPresenceEvents.cancel(player.getUUID()); if (AdditionalAbilities.DIRECT_CURRENT.equals(id)) DirectCurrentEvents.cancel(player.getUUID()); if (AdditionalAbilities.ALTERNATING_CURRENT.equals(id)) AlternatingCurrentEvents.cancel(player.getUUID()); if (AdditionalAbilities.STATIC_BLADE.equals(id)) StaticBladeEvents.cancel(player.getUUID()); if (AdditionalAbilities.LIVE_WIRE.equals(id)) LiveWireEvents.cancel(player.getUUID()); if (AdditionalAbilities.LULLABY.equals(id)) LullabyEvents.cancel(player.getUUID()); if (AdditionalAbilities.MAGMA_TOSS.equals(id)) MagmaTossEvents.cancel(player.getUUID()); if (AdditionalAbilities.GRAVITY_WELL.equals(id)) GravityWellEvents.cancel(player.getUUID()); }
}
