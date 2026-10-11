package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.ForgerMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class StaticBladeNetwork {
    private StaticBladeNetwork() {}
    public record SwingPayload() implements CustomPacketPayload {
        public static final Type<SwingPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(ForgerMod.MOD_ID,"static_blade_swing"));
        public static final StreamCodec<RegistryFriendlyByteBuf,SwingPayload> CODEC=StreamCodec.unit(new SwingPayload());
        public Type<? extends CustomPacketPayload> type() {return TYPE;}
    }
    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(SwingPayload.TYPE,SwingPayload.CODEC,(payload,context)->{
            if(context.player() instanceof ServerPlayer player) StaticBladeEvents.release(player);
        });
    }
}
