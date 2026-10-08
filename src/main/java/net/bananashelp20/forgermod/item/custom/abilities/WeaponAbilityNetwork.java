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
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid = ForgerMod.MOD_ID)
public final class WeaponAbilityNetwork {
    private record CooldownKey(Class<?> material, boolean dagger, boolean axe, WeaponAbilitySlot slot) {}
    private static final Map<UUID, Map<CooldownKey, Long>> COOLDOWNS = new HashMap<>();
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
        if (!(stack.getItem() instanceof SwordItemWithEffect weapon)) return;

        WeaponAbilitySlot slot = WeaponAbilitySlot.values()[payload.slot()];
        // Gemstone variants of the same material and weapon type share one ability cooldown.
        CooldownKey key = new CooldownKey(weapon.getClass(), weapon.isDagger(), weapon.isAxe(), slot);
        long now = player.level().getGameTime();
        Map<CooldownKey, Long> playerCooldowns = COOLDOWNS.computeIfAbsent(player.getUUID(), unused -> new HashMap<>());
        if (now < playerCooldowns.getOrDefault(key, 0L)) return;
        if (weapon.activateAbility(player, stack, slot)) {
            int cooldown = weapon.abilityCooldownTicks(slot);
            if (cooldown > 0) playerCooldowns.put(key, now + cooldown);
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        COOLDOWNS.remove(event.getEntity().getUUID());
    }
}
