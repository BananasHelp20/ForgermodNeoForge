package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.item.custom.SwordItemWithEffect;
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

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(
                UseAbilityPayload.TYPE, UseAbilityPayload.STREAM_CODEC, WeaponAbilityNetwork::handleAbility);
    }

    private static void handleAbility(UseAbilityPayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player) || player.isSpectator()) return;
        if (payload.slot() < 0 || payload.slot() >= WeaponAbilitySlot.values().length) return;

        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof SwordItemWithEffect weapon)
                || player.getCooldowns().isOnCooldown(stack.getItem())) return;

        WeaponAbilitySlot slot = WeaponAbilitySlot.values()[payload.slot()];
        if (weapon.activateAbility(player, stack, slot)) {
            int cooldown = weapon.abilityCooldownTicks(slot);
            if (cooldown > 0) player.getCooldowns().addCooldown(stack.getItem(), cooldown);
        }
    }
}
