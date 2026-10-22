package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.ForgerMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/** Viewer-owned outlines. Target entities and their shared glowing state are never changed. */
public final class EchoHearingHighlights {
    private static final UUID NONE = new UUID(0,0);
    private EchoHearingHighlights() {}

    public record Payload(UUID target, long expires, boolean clear) implements CustomPacketPayload {
        public static final Type<Payload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ForgerMod.MOD_ID,"echo_hearing_highlight"));
        public static final StreamCodec<RegistryFriendlyByteBuf,Payload> CODEC = StreamCodec.of(
                (buffer,value)->{buffer.writeUUID(value.target());buffer.writeLong(value.expires());buffer.writeBoolean(value.clear());},
                buffer->new Payload(buffer.readUUID(),buffer.readLong(),buffer.readBoolean()));
        @Override public Type<? extends CustomPacketPayload> type() {return TYPE;}
    }

    public static long remainingTicks(Player viewer, Entity target) {
        if(viewer==null || viewer==target || viewer.level()!=target.level() || !target.isAlive())return 0;
        return Math.max(0,viewer.getData(WeaponCooldownAttachments.ECHO_HIGHLIGHTS)
                .getOrDefault(target.getUUID(),0L)-viewer.level().getGameTime());
    }

    public static void apply(Player viewer, Payload payload) {
        if(payload.clear()) {viewer.setData(WeaponCooldownAttachments.ECHO_HIGHLIGHTS,Map.of());return;}
        var highlights=new HashMap<>(viewer.getData(WeaponCooldownAttachments.ECHO_HIGHLIGHTS));
        long now=viewer.level().getGameTime();
        highlights.values().removeIf(expires->expires<=now);
        if(payload.expires()>now)highlights.merge(payload.target(),payload.expires(),Math::max);
        viewer.setData(WeaponCooldownAttachments.ECHO_HIGHLIGHTS,Map.copyOf(highlights));
    }

    public static void reveal(ServerPlayer listener, Entity target, int ticks) {
        var payload=new Payload(target.getUUID(),listener.level().getGameTime()+ticks,false);
        apply(listener,payload);
        send(listener,payload);
    }

    public static void clear(ServerPlayer listener) {
        if(listener.getData(WeaponCooldownAttachments.ECHO_HIGHLIGHTS).isEmpty())return;
        var payload=new Payload(NONE,0,true);
        apply(listener,payload);
        send(listener,payload);
    }

    private static void send(ServerPlayer listener, Payload payload) {
        if(NetworkRegistry.hasChannel(listener.connection,Payload.TYPE.id()))
            PacketDistributor.sendToPlayer(listener,payload);
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(Payload.TYPE,Payload.CODEC,
                (payload,context)->apply(context.player(),payload));
    }
}
