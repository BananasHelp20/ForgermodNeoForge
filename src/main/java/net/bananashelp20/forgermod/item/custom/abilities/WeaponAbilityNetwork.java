package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.item.custom.SwordItemWithEffect;
import net.bananashelp20.forgermod.item.custom.WeaponAbilitySlot;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Map;
import java.util.HashMap;
import java.util.EnumSet;
import java.util.EnumMap;
import java.util.UUID;

@EventBusSubscriber(modid = ForgerMod.MOD_ID)
public final class WeaponAbilityNetwork {
    private record EquippedSession(ItemStack stack, String itemId, int selectedSlot, SwordItemWithEffect weapon,
                                   EnumSet<WeaponAbilitySlot> activeSlots,
                                   Map<WeaponAbilitySlot, WeaponAbilitySlot> sources,
                                   Map<WeaponAbilitySlot, Integer> cooldowns) {}
    private static final Map<UUID, EquippedSession> ACTIVE = new HashMap<>();
    private WeaponAbilityNetwork() {}

    public record UseAbilityPayload(int slot, int selectedSlot, String itemId) implements CustomPacketPayload {
        public static final Type<UseAbilityPayload> TYPE = new Type<>(
                ResourceLocation.fromNamespaceAndPath(ForgerMod.MOD_ID, "use_weapon_ability"));
        public static final StreamCodec<RegistryFriendlyByteBuf, UseAbilityPayload> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.VAR_INT, UseAbilityPayload::slot,
                        ByteBufCodecs.VAR_INT, UseAbilityPayload::selectedSlot,
                        ByteBufCodecs.STRING_UTF8, UseAbilityPayload::itemId,
                        UseAbilityPayload::new);

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
        updateSession(player);

        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof SwordItemWithEffect weapon)) return;
        if (payload.selectedSlot() != player.getInventory().selected
                || !payload.itemId().equals(itemId(stack))) return;

        WeaponAbilitySlot slot = WeaponAbilitySlot.values()[payload.slot()];
        String key = cooldownKey(stack, slot);
        long now = player.level().getGameTime();
        Map<String, Long> playerCooldowns = player.getData(WeaponCooldownAttachments.COOLDOWNS);
        long readyAt = playerCooldowns.getOrDefault(key, 0L);
        if (!player.getData(WeaponCooldownAttachments.DISABLED) && now < readyAt) {
            player.displayClientMessage(Component.translatable("message.forgermod.ability.cooldown",
                    (readyAt - now + 19) / 20), true);
            return;
        }
        if (Augmentations.activeId(stack, slot) == null) return;
        weapon = Augmentations.canonical(stack);
        WeaponAbilitySlot source = Augmentations.sourceSlot(stack, slot);
        if (weapon.activateAbility(player, stack, source)) {
            if (weapon.isAbilityActive(player, source)) {
                EquippedSession session = ACTIVE.get(player.getUUID());
                if (session == null) {
                    session = new EquippedSession(stack, itemId(stack), player.getInventory().selected, weapon,
                            EnumSet.noneOf(WeaponAbilitySlot.class), new EnumMap<>(WeaponAbilitySlot.class),
                            new EnumMap<>(WeaponAbilitySlot.class));
                    ACTIVE.put(player.getUUID(), session);
                }
                session.activeSlots().add(slot);
                session.sources().put(slot, source);
                session.cooldowns().put(slot, Augmentations.cooldown(stack, slot, weapon));
            } else {
                setCooldown(player, key, now + Augmentations.cooldown(stack, slot, weapon));
            }
        } else {
            player.displayClientMessage(Component.translatable("message.forgermod.ability.unavailable"), true);
        }
    }

    private static String cooldownKey(ItemStack stack, WeaponAbilitySlot slot) {
        return WeaponCooldownKey.of(itemId(stack), slot);
    }

    private static String itemId(ItemStack stack) {
        return BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
    }

    private static void setCooldown(ServerPlayer player, String key, long readyAt) {
        if (player.getData(WeaponCooldownAttachments.DISABLED)) return;
        Map<String, Long> updated = new HashMap<>(player.getData(WeaponCooldownAttachments.COOLDOWNS));
        updated.put(key, Math.max(updated.getOrDefault(key, 0L), readyAt));
        player.setData(WeaponCooldownAttachments.COOLDOWNS, Map.copyOf(updated));
    }

    private static void updateSession(ServerPlayer player) {
        EquippedSession session = ACTIVE.get(player.getUUID());
        if (session == null) return;
        long now = player.level().getGameTime();
        session.activeSlots().removeIf(slot -> {
            if (session.weapon().isAbilityActive(player, session.sources().get(slot))) return false;
            int cooldown = session.cooldowns().get(slot);
            if (cooldown > 0) setCooldown(player, WeaponCooldownKey.of(session.itemId(), slot), now + cooldown);
            return true;
        });
        if (session.activeSlots().isEmpty()) {
            ACTIVE.remove(player.getUUID());
            return;
        }
        if (player.isAlive() && !session.stack().isEmpty() && player.getMainHandItem() == session.stack()
                && player.getInventory().selected == session.selectedSlot()) return;

        cancelSession(player, session);
    }

    private static void cancelSession(ServerPlayer player, EquippedSession session) {
        long now = player.level().getGameTime();
        for (WeaponAbilitySlot slot : session.activeSlots()) {
            session.weapon().cancelAbility(player, session.sources().get(slot));
            int cooldown = session.cooldowns().get(slot);
            if (cooldown > 0) setCooldown(player, WeaponCooldownKey.of(session.itemId(), slot), now + cooldown);
        }
        ACTIVE.remove(player.getUUID());
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Pre event) {
        if (event.getEntity() instanceof ServerPlayer player) updateSession(player);
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            EquippedSession session = ACTIVE.get(player.getUUID());
            if (session != null) cancelSession(player, session);
        }
    }
}
