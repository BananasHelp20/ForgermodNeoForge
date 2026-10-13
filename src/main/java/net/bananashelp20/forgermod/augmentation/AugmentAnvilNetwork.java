package net.bananashelp20.forgermod.augmentation;

import net.bananashelp20.forgermod.ForgerMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

public final class AugmentAnvilNetwork {
    private AugmentAnvilNetwork() {}

    public record ChooseReplacementPayload(int menuId, String incoming, String replaced) implements CustomPacketPayload {
        public static final Type<ChooseReplacementPayload> TYPE = new Type<>(
                ResourceLocation.fromNamespaceAndPath(ForgerMod.MOD_ID, "choose_augment_replacement"));
        public static final StreamCodec<RegistryFriendlyByteBuf, ChooseReplacementPayload> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, ChooseReplacementPayload::menuId,
                ByteBufCodecs.STRING_UTF8, ChooseReplacementPayload::incoming,
                ByteBufCodecs.STRING_UTF8, ChooseReplacementPayload::replaced,
                ChooseReplacementPayload::new);
        @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(ChooseReplacementPayload.TYPE, ChooseReplacementPayload.STREAM_CODEC,
                (payload, context) -> {
                    var player = context.player();
                    if (player.containerMenu.containerId == payload.menuId())
                        AugmentAnvilEvents.select(player, payload.incoming(), payload.replaced());
                });
    }
}
