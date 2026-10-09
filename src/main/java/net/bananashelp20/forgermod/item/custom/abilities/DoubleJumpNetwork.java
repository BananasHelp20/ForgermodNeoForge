package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.PacketDistributor;
import net.minecraft.network.codec.StreamCodec;

@EventBusSubscriber(modid = ForgerMod.MOD_ID)
public final class DoubleJumpNetwork {
    private static final DoubleJumpState STATE = new DoubleJumpState();
    private DoubleJumpNetwork() {}

    public record DoubleJumpPayload() implements CustomPacketPayload {
        public static final Type<DoubleJumpPayload> TYPE = new Type<>(
                ResourceLocation.fromNamespaceAndPath(ForgerMod.MOD_ID, "double_jump"));
        public static final StreamCodec<RegistryFriendlyByteBuf, DoubleJumpPayload> STREAM_CODEC =
                StreamCodec.unit(new DoubleJumpPayload());

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(
                DoubleJumpPayload.TYPE, DoubleJumpPayload.STREAM_CODEC, DoubleJumpNetwork::handle);
        event.registrar("1").playToClient(
                DoubleJumpAcceptedPayload.TYPE, DoubleJumpAcceptedPayload.STREAM_CODEC, DoubleJumpNetwork::handleAccepted);
    }

    public record DoubleJumpAcceptedPayload() implements CustomPacketPayload {
        public static final Type<DoubleJumpAcceptedPayload> TYPE = new Type<>(
                ResourceLocation.fromNamespaceAndPath(ForgerMod.MOD_ID, "double_jump_accepted"));
        public static final StreamCodec<RegistryFriendlyByteBuf, DoubleJumpAcceptedPayload> STREAM_CODEC =
                StreamCodec.unit(new DoubleJumpAcceptedPayload());

        @Override
        public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    private static void handleAccepted(DoubleJumpAcceptedPayload payload, IPayloadContext context) {
        boost(context.player());
    }

    private static void boost(Player player) {
        Vec3 velocity = player.getDeltaMovement();
        player.setDeltaMovement(velocity.x, 0.55 + .05 * (Augmentations.level(player.getMainHandItem(), "tooltips.forgermod.passive.double_jump") - 1), velocity.z);
        player.hasImpulse = true;
        player.fallDistance = 0;
    }

    private static void handle(DoubleJumpPayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player) || player.onGround()
                || player.isSpectator() || !player.isAlive()
                || !Augmentations.hasPassive(player.getMainHandItem(), "tooltips.forgermod.passive.double_jump") || !STATE.consume(player.getUUID())) return;
        boost(player);
        // A full server motion packet overwrites the client's current sprint momentum.
        // Acknowledge only the lift, preserving the client's horizontal movement.
        PacketDistributor.sendToPlayer(player, new DoubleJumpAcceptedPayload());
        player.serverLevel().sendParticles(ParticleTypes.CLOUD,
                player.getX(), player.getY(), player.getZ(), 12, 0.35, 0.1, 0.35, 0.05);
    }

    @SubscribeEvent
    public static void onNormalJump(LivingEvent.LivingJumpEvent event) {
        if (event.getEntity() instanceof ServerPlayer player
                && Augmentations.hasPassive(player.getMainHandItem(), "tooltips.forgermod.passive.double_jump")) STATE.normalJump(player.getUUID());
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity() instanceof ServerPlayer player
                && (!player.isAlive() || player.onGround() && player.getDeltaMovement().y <= 0
                || !Augmentations.hasPassive(player.getMainHandItem(), "tooltips.forgermod.passive.double_jump"))) {
            STATE.clear(player.getUUID());
        }
    }

    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        STATE.clear(event.getEntity().getUUID());
    }
}
