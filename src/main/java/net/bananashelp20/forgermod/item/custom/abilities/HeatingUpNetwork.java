package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.ForgerMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.NetworkRegistry;

/** Sends the server-owned Heating Up value to the wielder's HUD. */
public final class HeatingUpNetwork {
    private static float clientHeat;

    private HeatingUpNetwork() {}

    public record HeatPayload(float heat) implements CustomPacketPayload {
        public static final Type<HeatPayload> TYPE = new Type<>(
                ResourceLocation.fromNamespaceAndPath(ForgerMod.MOD_ID, "heating_up_heat"));
        public static final StreamCodec<RegistryFriendlyByteBuf, HeatPayload> CODEC = StreamCodec.composite(
                ByteBufCodecs.FLOAT, HeatPayload::heat, HeatPayload::new);

        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(HeatPayload.TYPE, HeatPayload.CODEC,
                (payload, context) -> clientHeat = Math.clamp(payload.heat(), 0, 20));
    }

    public static void send(ServerPlayer player, float heat) {
        if (NetworkRegistry.hasChannel(player.connection, HeatPayload.TYPE.id()))
            PacketDistributor.sendToPlayer(player, new HeatPayload(heat));
    }

    public static float clientHeat() { return clientHeat; }
    public static void clearClientHeat() { clientHeat = 0; }
}
