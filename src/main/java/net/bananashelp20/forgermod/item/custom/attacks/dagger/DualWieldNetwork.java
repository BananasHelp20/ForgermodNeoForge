package net.bananashelp20.forgermod.item.custom.attacks.dagger;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.item.custom.SwordItemWithEffect;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DualWieldNetwork {
    private static final Map<UUID, Long> LAST_ATTACK_TICK = new HashMap<>();

    public record OffhandAttackPayload(int targetId) implements CustomPacketPayload {
        public static final Type<OffhandAttackPayload> TYPE = new Type<>(
                ResourceLocation.fromNamespaceAndPath(ForgerMod.MOD_ID, "offhand_attack"));
        public static final StreamCodec<RegistryFriendlyByteBuf, OffhandAttackPayload> STREAM_CODEC =
                StreamCodec.composite(ByteBufCodecs.VAR_INT, OffhandAttackPayload::targetId, OffhandAttackPayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record OffhandSwingPayload(int playerId) implements CustomPacketPayload {
        public static final Type<OffhandSwingPayload> TYPE = new Type<>(
                ResourceLocation.fromNamespaceAndPath(ForgerMod.MOD_ID, "offhand_swing"));
        public static final StreamCodec<RegistryFriendlyByteBuf, OffhandSwingPayload> STREAM_CODEC =
                StreamCodec.composite(ByteBufCodecs.VAR_INT, OffhandSwingPayload::playerId, OffhandSwingPayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(
                OffhandAttackPayload.TYPE, OffhandAttackPayload.STREAM_CODEC, DualWieldNetwork::handleAttack);
        event.registrar("1").playToClient(
                OffhandSwingPayload.TYPE, OffhandSwingPayload.STREAM_CODEC, DualWieldNetwork::handleSwing);
    }

    private static void handleSwing(OffhandSwingPayload payload, IPayloadContext context) {
        Entity entity = context.player().level().getEntity(payload.playerId());
        if (entity instanceof Player otherPlayer && entity != context.player()) {
            otherPlayer.swinging = false;
            otherPlayer.swing(InteractionHand.OFF_HAND);
        }
    }

    private static void handleAttack(OffhandAttackPayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) return;

        ItemStack mainhand = player.getMainHandItem();
        ItemStack offhand = player.getOffhandItem();
        if (!DaggerItems.hasMatchingDaggers(player)) return;

        long now = player.level().getGameTime();
        long last = LAST_ATTACK_TICK.getOrDefault(player.getUUID(), Long.MIN_VALUE);
        if (last != Long.MIN_VALUE && now - last < 3) return;
        LAST_ATTACK_TICK.put(player.getUUID(), now);

        // An air swing still animates; it simply has no target to damage.
        player.swinging = false;
        player.swing(InteractionHand.OFF_HAND, true);
        PacketDistributor.sendToPlayersTrackingEntity(player, new OffhandSwingPayload(player.getId()));

        Entity entity = player.level().getEntity(payload.targetId());
        if (!(entity instanceof LivingEntity target) || target.isDeadOrDying()
                || entity == player || player.distanceToSqr(entity) > 25.0
                || !player.hasLineOfSight(entity)) return;

        Vec3 eye = player.getEyePosition();
        Vec3 reachEnd = eye.add(player.getLookAngle().scale(4.5));
        if (entity.getBoundingBox().inflate(0.3).clip(eye, reachEnd).isEmpty()) return;

        int previousInvulnerableTime = target.invulnerableTime;
        target.invulnerableTime = 0;
        float damage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        if (target.hurt(player.damageSources().playerAttack(player), damage)) {
            if (offhand.getItem() instanceof SwordItemWithEffect weapon) {
                weapon.applyMaterialEffect(target);
            }
            offhand.hurtAndBreak(1, player, EquipmentSlot.OFFHAND);
        } else {
            target.invulnerableTime = previousInvulnerableTime;
        }
    }
}
