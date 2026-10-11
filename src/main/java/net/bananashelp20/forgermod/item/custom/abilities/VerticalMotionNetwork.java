package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.ForgerMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.registration.NetworkRegistry;

/** A vertical impulse must preserve the client's current sprint momentum. */
public final class VerticalMotionNetwork {
    private VerticalMotionNetwork() {}
    public record VerticalPayload(double velocity,boolean downward,boolean resetFall) implements CustomPacketPayload {
        public static final Type<VerticalPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(ForgerMod.MOD_ID,"weapon_vertical_motion"));
        public static final StreamCodec<RegistryFriendlyByteBuf,VerticalPayload> CODEC=StreamCodec.composite(
            ByteBufCodecs.DOUBLE,VerticalPayload::velocity,ByteBufCodecs.BOOL,VerticalPayload::downward,ByteBufCodecs.BOOL,VerticalPayload::resetFall,VerticalPayload::new);
        public Type<? extends CustomPacketPayload> type() {return TYPE;}
    }
    public static Vec3 adjusted(Vec3 motion,double velocity,boolean downward) {return new Vec3(motion.x,downward?Math.min(motion.y,velocity):velocity,motion.z);}
    public static void apply(ServerPlayer player,double velocity,boolean downward,boolean resetFall) {
        player.setDeltaMovement(adjusted(player.getDeltaMovement(),velocity,downward));player.hasImpulse=true;if(resetFall) player.fallDistance=0;
        if(NetworkRegistry.hasChannel(player.connection,VerticalPayload.TYPE.id())) PacketDistributor.sendToPlayer(player,new VerticalPayload(velocity,downward,resetFall));
        else player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket(player));
    }
    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(VerticalPayload.TYPE,VerticalPayload.CODEC,(payload,context)->{
            var player=context.player();player.setDeltaMovement(adjusted(player.getDeltaMovement(),payload.velocity(),payload.downward()));player.hasImpulse=true;if(payload.resetFall()) player.fallDistance=0;
        });
    }
}
