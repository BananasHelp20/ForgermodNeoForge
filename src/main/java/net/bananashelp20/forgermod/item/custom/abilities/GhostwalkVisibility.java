package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.ForgerMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import java.util.UUID;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.HashSet;

/** Explicit tracking synchronization: vanilla does not broadcast a player's potion list to observers. */
@EventBusSubscriber(modid = ForgerMod.MOD_ID)
public final class GhostwalkVisibility {
    private static final Map<UUID,Set<ServerPlayer>> TRACKERS = new HashMap<>();
    private GhostwalkVisibility() {}

    public record Payload(int entityId, UUID playerId, boolean hidden) implements CustomPacketPayload {
        public static final Type<Payload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(ForgerMod.MOD_ID,"ghostwalk_visibility"));
        public static final StreamCodec<RegistryFriendlyByteBuf,Payload> CODEC = StreamCodec.of(
                (buffer,value)->{buffer.writeVarInt(value.entityId());buffer.writeUUID(value.playerId());buffer.writeBoolean(value.hidden());},
                buffer->new Payload(buffer.readVarInt(),buffer.readUUID(),buffer.readBoolean()));
        @Override public Type<? extends CustomPacketPayload> type() {return TYPE;}
    }

    public static boolean hidden(Entity entity) {
        return entity instanceof Player && entity.getData(WeaponCooldownAttachments.GHOSTWALK_HIDDEN);
    }

    public static Payload snapshot(ServerPlayer player) {
        return new Payload(player.getId(),player.getUUID(),hidden(player));
    }

    public static void set(ServerPlayer player, boolean hidden) {
        if(hidden(player)==hidden)return;
        player.setData(WeaponCooldownAttachments.GHOSTWALK_HIDDEN,hidden);
        var payload=snapshot(player);
        send(player,payload);
        for(var observer:TRACKERS.getOrDefault(player.getUUID(),Set.of()))send(observer,payload);
    }

    private static void send(ServerPlayer observer, Payload payload) {
        if(NetworkRegistry.hasChannel(observer.connection,Payload.TYPE.id()))
            PacketDistributor.sendToPlayer(observer,payload);
    }

    /** Entity ids can be reused; never apply an old update to a different player. */
    public static void apply(Level level, Payload payload) {
        var entity=level.getEntity(payload.entityId());
        if(entity instanceof Player && entity.getUUID().equals(payload.playerId()))
            entity.setData(WeaponCooldownAttachments.GHOSTWALK_HIDDEN,payload.hidden());
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(Payload.TYPE,Payload.CODEC,
                (payload,context)->apply(context.player().level(),payload));
    }

    @SubscribeEvent
    public static void tracking(PlayerEvent.StartTracking event) {
        if(event.getEntity() instanceof ServerPlayer observer && event.getTarget() instanceof ServerPlayer target
                && NetworkRegistry.hasChannel(observer.connection,Payload.TYPE.id())) {
            TRACKERS.computeIfAbsent(target.getUUID(),id->new HashSet<>()).add(observer);
            send(observer,snapshot(target));
        }
    }

    @SubscribeEvent
    public static void stoppedTracking(PlayerEvent.StopTracking event) {
        var watchers=TRACKERS.get(event.getTarget().getUUID());
        if(watchers!=null) {
            watchers.remove(event.getEntity());
            if(watchers.isEmpty())TRACKERS.remove(event.getTarget().getUUID());
        }
    }

    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        TRACKERS.remove(event.getEntity().getUUID());
        TRACKERS.values().removeIf(watchers->{watchers.remove(event.getEntity());return watchers.isEmpty();});
    }
}
