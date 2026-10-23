package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.ForgerMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
import java.util.*;

/** Transient visual link; native leash ownership and leash physics are untouched. */
@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class CrucibleHookRope {
    public static final int SEGMENTS=24;
    public record Link(int targetId,UUID targetUuid,long expires,Vec3 anchor) {
        public Link(int targetId,UUID targetUuid,long expires){this(targetId,targetUuid,expires,null);}
    }
    public static final Link NONE=new Link(-1,new UUID(0,0),0);
    private static final Map<UUID,Set<ServerPlayer>> TRACKERS=new HashMap<>();
    private static final Map<UUID,ServerPlayer> OWNERS=new HashMap<>();
    private CrucibleHookRope() {}
    public record Payload(int ownerId,UUID ownerUuid,Link link) implements CustomPacketPayload {
        public static final Type<Payload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(ForgerMod.MOD_ID,"crucible_hook_rope"));
        public static final StreamCodec<RegistryFriendlyByteBuf,Payload> CODEC=StreamCodec.of(
                (buffer,value)->{buffer.writeVarInt(value.ownerId());buffer.writeUUID(value.ownerUuid());buffer.writeVarInt(value.link().targetId());buffer.writeUUID(value.link().targetUuid());buffer.writeLong(value.link().expires());buffer.writeBoolean(value.link().anchor()!=null);if(value.link().anchor()!=null){buffer.writeDouble(value.link().anchor().x);buffer.writeDouble(value.link().anchor().y);buffer.writeDouble(value.link().anchor().z);}},
                buffer->{int owner=buffer.readVarInt();UUID uuid=buffer.readUUID();int target=buffer.readVarInt();UUID targetUuid=buffer.readUUID();long expires=buffer.readLong();Vec3 anchor=buffer.readBoolean()?new Vec3(buffer.readDouble(),buffer.readDouble(),buffer.readDouble()):null;return new Payload(owner,uuid,new Link(target,targetUuid,expires,anchor));});
        @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
    }
    public static Link link(Player owner){return owner.getData(WeaponCooldownAttachments.CRUCIBLE_HOOK_ROPE);}
    public static Payload snapshot(ServerPlayer owner){return new Payload(owner.getId(),owner.getUUID(),link(owner));}
    public static void begin(ServerPlayer owner,LivingEntity target,long expires) {
        OWNERS.put(owner.getUUID(),owner);
        owner.setData(WeaponCooldownAttachments.CRUCIBLE_HOOK_ROPE,new Link(target.getId(),target.getUUID(),expires));
        broadcast(owner);
    }
    public static void beginBlock(ServerPlayer owner,Vec3 anchor){
        OWNERS.put(owner.getUUID(),owner);owner.setData(WeaponCooldownAttachments.CRUCIBLE_HOOK_ROPE,new Link(-2,new UUID(0,0),Long.MAX_VALUE,anchor));broadcast(owner);
    }
    public static Vec3 blockAnchor(Player owner){
        var link=link(owner);return owner.isAlive() && !owner.isRemoved() && !owner.isSpectator() && owner.level().getGameTime()<link.expires() && link.targetId()==-2?link.anchor():null;
    }
    public static void end(UUID ownerId) {
        var owner=OWNERS.remove(ownerId);if(owner==null)return;
        owner.setData(WeaponCooldownAttachments.CRUCIBLE_HOOK_ROPE,NONE);broadcast(owner);
    }
    private static void broadcast(ServerPlayer owner) {
        var payload=snapshot(owner);send(owner,payload);
        for(var viewer:TRACKERS.getOrDefault(owner.getUUID(),Set.of()))if(viewer!=owner)send(viewer,payload);
    }
    private static void send(ServerPlayer viewer,Payload payload) {
        if(NetworkRegistry.hasChannel(viewer.connection,Payload.TYPE.id()))PacketDistributor.sendToPlayer(viewer,payload);
    }
    public static void apply(Level level,Payload payload) {
        var owner=level.getEntity(payload.ownerId());
        if(owner instanceof Player && owner.getUUID().equals(payload.ownerUuid()))owner.setData(WeaponCooldownAttachments.CRUCIBLE_HOOK_ROPE,payload.link());
    }
    public static LivingEntity target(Player owner) {
        var link=link(owner);
        if(!owner.isAlive() || owner.isRemoved() || owner.isSpectator() || link.targetId()<0 || owner.level().getGameTime()>=link.expires())return null;
        var target=owner.level().getEntity(link.targetId());
        return target instanceof LivingEntity living && living!=owner && living.isAlive() && !living.isRemoved()
                && living.getUUID().equals(link.targetUuid()) && owner.distanceToSqr(living)<=24*24?living:null;
    }
    /** Shared curve for renderer and geometry tests; the ends never drift from their attachment points. */
    public static Vec3 point(Vec3 start,Vec3 end,int index) {
        double t=Math.clamp(index,0,SEGMENTS)/(double)SEGMENTS;
        double sag=Math.min(.5,start.distanceTo(end)*.035);
        return start.lerp(end,t).add(0,-4*sag*t*(1-t),0);
    }
    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("2").playToClient(Payload.TYPE,Payload.CODEC,(payload,context)->apply(context.player().level(),payload));
    }
    @SubscribeEvent public static void tracking(PlayerEvent.StartTracking event) {
        if(event.getEntity() instanceof ServerPlayer viewer && event.getTarget() instanceof ServerPlayer owner
                && NetworkRegistry.hasChannel(viewer.connection,Payload.TYPE.id())) {
            TRACKERS.computeIfAbsent(owner.getUUID(),id->new HashSet<>()).add(viewer);send(viewer,snapshot(owner));
        }
    }
    @SubscribeEvent public static void stoppedTracking(PlayerEvent.StopTracking event) {
        var viewers=TRACKERS.get(event.getTarget().getUUID());
        if(viewers!=null){viewers.remove(event.getEntity());if(viewers.isEmpty())TRACKERS.remove(event.getTarget().getUUID());}
    }
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        end(event.getEntity().getUUID());TRACKERS.remove(event.getEntity().getUUID());
        TRACKERS.values().removeIf(viewers->{viewers.remove(event.getEntity());return viewers.isEmpty();});
    }
    @SubscribeEvent public static void dimension(PlayerEvent.PlayerChangedDimensionEvent event){end(event.getEntity().getUUID());}
}
