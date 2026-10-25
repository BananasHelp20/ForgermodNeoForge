package net.bananashelp20.forgermod.sound;

import net.bananashelp20.forgermod.ForgerMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/** One finite moving sound per visible electrical arc; no server tick jobs or persistent entities. */
public final class ElectricalTravel {
    public static final int TICKS=5;
    public static final int MAX_SOUND_TICKS=24;
    private ElectricalTravel(){}
    public record Payload(ResourceLocation dimension,Vec3 from,Vec3 to) implements CustomPacketPayload {
        public static final Type<Payload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(ForgerMod.MOD_ID,"electrical_travel"));
        public static final StreamCodec<RegistryFriendlyByteBuf,Payload> CODEC=StreamCodec.of((b,p)->{
            b.writeResourceLocation(p.dimension());b.writeDouble(p.from.x);b.writeDouble(p.from.y);b.writeDouble(p.from.z);b.writeDouble(p.to.x);b.writeDouble(p.to.y);b.writeDouble(p.to.z);
        },b->new Payload(b.readResourceLocation(),new Vec3(b.readDouble(),b.readDouble(),b.readDouble()),new Vec3(b.readDouble(),b.readDouble(),b.readDouble())));
        public Type<? extends CustomPacketPayload> type(){return TYPE;}
    }
    public static Vec3 position(Vec3 from,Vec3 to,int age){return from.lerp(to,Math.clamp(age/(double)TICKS,0,1));}
    public static void emit(ServerLevel level,Vec3 from,Vec3 to){
        var packet=new net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket(new Payload(level.dimension().location(),from,to));
        var delta=to.subtract(from);double length=delta.lengthSqr();
        for(var player:level.players()){
            double t=length<.000001?0:Math.clamp(player.getEyePosition().subtract(from).dot(delta)/length,0,1);
            if(player.getEyePosition().distanceToSqr(from.add(delta.scale(t)))>256)continue;
            if(player.connection!=null && player.connection.getConnection().channel()!=null && player.connection.hasChannel(Payload.TYPE))player.connection.send(packet);
        }
    }
    public static void register(RegisterPayloadHandlersEvent event){
        event.registrar("1").playToClient(Payload.TYPE,Payload.CODEC,(payload,context)->context.enqueueWork(()->net.bananashelp20.forgermod.client.ElectricalTravelSound.play(payload)));
    }
}
