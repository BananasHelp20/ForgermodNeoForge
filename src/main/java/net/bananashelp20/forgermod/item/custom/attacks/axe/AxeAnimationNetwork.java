package net.bananashelp20.forgermod.item.custom.attacks.axe;
import net.bananashelp20.forgermod.ForgerMod;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.*;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.NetworkRegistry;
public final class AxeAnimationNetwork {
    public static final String SEQUENCE="forgermod.axe_clip_sequence",CLIP="forgermod.axe_clip",START="forgermod.axe_clip_start",PHASE="forgermod.axe_clip_phase";
    private AxeAnimationNetwork(){}
    public record Payload(int entity,int clip,long sequence,int phase) implements CustomPacketPayload {
        public static final Type<Payload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(ForgerMod.MOD_ID,"axe_authored_animation"));
        public static final StreamCodec<RegistryFriendlyByteBuf,Payload> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.VAR_INT,Payload::entity,ByteBufCodecs.VAR_INT,Payload::clip,ByteBufCodecs.VAR_LONG,Payload::sequence,ByteBufCodecs.VAR_INT,Payload::phase,Payload::new);
        @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
    }
    public static void register(RegisterPayloadHandlersEvent event){event.registrar("2").playToClient(Payload.TYPE,Payload.STREAM_CODEC,(packet,context)->{
        if(packet.phase()<0 || packet.phase()>2 || packet.clip()<0 || packet.clip()>=AxeAnimationData.Clip.values().length)return;
        if(context.player().level().getEntity(packet.entity()) instanceof Player player)mark(player,AxeAnimationData.Clip.values()[packet.clip()],packet.sequence(),packet.phase());
    });}
    private static void mark(Player player,AxeAnimationData.Clip clip,long sequence,int phase){var tag=player.getPersistentData();tag.putInt(CLIP,clip.ordinal());tag.putLong(START,player.level().getGameTime());tag.putLong(SEQUENCE,sequence);tag.putInt(PHASE,phase);}
    public static void play(ServerPlayer player,AxeAnimationData.Clip clip){
        if(clip==AxeAnimationData.Clip.VERTICAL)AxeVerticalMotion.begin(player);
        send(player,clip,0);
    }
    public static void verticalPhase(ServerPlayer player,int phase){send(player,AxeAnimationData.Clip.VERTICAL,phase);}
    private static void send(ServerPlayer player,AxeAnimationData.Clip clip,int phase){
        long sequence=player.getPersistentData().getLong(SEQUENCE)+1;mark(player,clip,sequence,phase);
        var viewers=new java.util.HashSet<>(player.serverLevel().getChunkSource().chunkMap.getPlayers(player.chunkPosition(),false));viewers.add(player);
        for(var viewer:viewers)if(NetworkRegistry.hasChannel(viewer.connection,Payload.TYPE.id()))PacketDistributor.sendToPlayer(viewer,new Payload(player.getId(),clip.ordinal(),sequence,phase));
    }
}
