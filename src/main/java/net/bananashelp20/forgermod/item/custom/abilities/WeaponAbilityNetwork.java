package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.item.custom.SwordItemWithEffect;
import net.bananashelp20.forgermod.item.custom.PulsiteWeapon;
import net.bananashelp20.forgermod.item.custom.WeaponAbilitySlot;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Map;
import java.util.HashMap;

public final class WeaponAbilityNetwork {
    private WeaponAbilityNetwork() {}

    public record UseAbilityPayload(int slot) implements CustomPacketPayload {
        public static final Type<UseAbilityPayload> TYPE = new Type<>(
                ResourceLocation.fromNamespaceAndPath(ForgerMod.MOD_ID, "use_weapon_ability"));
        public static final StreamCodec<RegistryFriendlyByteBuf, UseAbilityPayload> STREAM_CODEC =
                StreamCodec.composite(ByteBufCodecs.VAR_INT, UseAbilityPayload::slot, UseAbilityPayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record SonicAirSwingPayload() implements CustomPacketPayload {
        public static final Type<SonicAirSwingPayload> TYPE = new Type<>(
                ResourceLocation.fromNamespaceAndPath(ForgerMod.MOD_ID, "sonic_air_swing"));
        public static final StreamCodec<RegistryFriendlyByteBuf, SonicAirSwingPayload> STREAM_CODEC =
                StreamCodec.unit(new SonicAirSwingPayload());

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(
                UseAbilityPayload.TYPE, UseAbilityPayload.STREAM_CODEC, WeaponAbilityNetwork::handleAbility);
        event.registrar("1").playToServer(
                SonicAirSwingPayload.TYPE, SonicAirSwingPayload.STREAM_CODEC,
                (payload, context) -> {
                    if (context.player() instanceof ServerPlayer player) PulsiteWeapon.consumeSonicAirSwing(player);
                });
    }

    private static void handleAbility(UseAbilityPayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player) || player.isSpectator()) return;
        if (payload.slot() < 0 || payload.slot() >= WeaponAbilitySlot.values().length) return;

        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof SwordItemWithEffect weapon)) return;

        WeaponAbilitySlot slot = WeaponAbilitySlot.values()[payload.slot()];
        // Gemstone variants of the same material and weapon type share one ability cooldown.
        String key = WeaponCooldownKey.of(weapon.getClass(), weapon.isDagger(), weapon.isAxe(), slot);
        long now = player.level().getGameTime();
        Map<String, Long> playerCooldowns = player.getData(WeaponCooldownAttachments.COOLDOWNS);
        if (now < playerCooldowns.getOrDefault(key, 0L)) return;
        if (weapon.activateAbility(player, stack, slot)) {
            int cooldown = weapon.abilityCooldownTicks(slot);
            if (cooldown > 0) {
                Map<String, Long> updated = new HashMap<>(playerCooldowns);
                updated.put(key, now + cooldown);
                player.setData(WeaponCooldownAttachments.COOLDOWNS, Map.copyOf(updated));
            }
        }
    }
}
