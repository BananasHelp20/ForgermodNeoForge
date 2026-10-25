package net.bananashelp20.forgermod.gametest;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.sound.ElectricalTravel;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.abilities.AlternatingCurrentEvents;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder(ForgerMod.MOD_ID) @PrefixGameTestTemplate(false)
public class ElectricalTravelGameTests {
    public static final class Wire extends net.minecraft.network.Connection {
        public final java.util.List<ElectricalTravel.Payload> arcs=new java.util.ArrayList<>();
        public Wire(){super(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);new io.netty.channel.embedded.EmbeddedChannel(this);net.neoforged.neoforge.network.registration.ChannelAttributes.getOrCreateAdHocChannels(this).add(ElectricalTravel.Payload.TYPE.id());}
        @Override public void send(net.minecraft.network.protocol.Packet<?> packet,net.minecraft.network.PacketSendListener listener,boolean flush){if(packet instanceof net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket custom && custom.payload() instanceof ElectricalTravel.Payload arc)arcs.add(arc);}
    }
    private static void aim(net.minecraft.server.level.ServerPlayer player,LivingEntity target){var d=target.getEyePosition().subtract(player.getEyePosition());player.setYRot((float)-Math.toDegrees(Math.atan2(d.x,d.z)));player.setXRot((float)-Math.toDegrees(Math.atan2(d.y,Math.hypot(d.x,d.z))));}
    @GameTest(template="riftfang_test",timeoutTicks=60)
    public static void alternatingJumpAudioReversesDirectionAndStopsAtWalls(GameTestHelper test){
        var player=TestPlayers.survival(test);player.setNoGravity(true);player.setPos(test.getBounds().getCenter());var origin=player.position();
        for(int x=-2;x<=6;x++)for(int y=0;y<=5;y++)for(int z=0;z<=10;z++)test.getLevel().setBlockAndUpdate(player.blockPosition().offset(x,y,z),Blocks.AIR.defaultBlockState());
        var first=EntityType.COW.create(test.getLevel());var second=EntityType.COW.create(test.getLevel());first.setPos(origin.add(0,0,4));second.setPos(origin.add(3,0,8));for(var mob:java.util.List.of(first,second)){mob.setNoAi(true);mob.setNoGravity(true);test.getLevel().addFreshEntity(mob);}
        var old=player.connection;var wire=new Wire();new net.minecraft.server.network.ServerGamePacketListenerImpl(player.server,wire,player,net.minecraft.server.network.CommonListenerCookie.createInitial(player.getGameProfile(),false));
        Runnable cleanup=()->{AlternatingCurrentEvents.cancel(player.getUUID());first.discard();second.discard();player.connection=old;player.server.getPlayerList().remove(player);};
        try{
            var stack=Augmentations.apply(new ItemStack(ModItems.STATIC_DAGGER.get()),AdditionalAbilities.ALTERNATING_CURRENT);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
            aim(player,first);test.assertTrue(AlternatingCurrentEvents.activate(player,stack) && wire.arcs.isEmpty(),"First mark makes a traveling current sound");
            aim(player,second);test.assertTrue(AlternatingCurrentEvents.activate(player,stack),"Cannot form alternating link");
            test.assertTrue(wire.arcs.size()==1 && wire.arcs.getFirst().from().equals(first.getEyePosition()) && wire.arcs.getFirst().to().equals(second.getEyePosition()),"First jump has no correctly directed audio");
            var arc=wire.arcs.getFirst();var buf=new net.minecraft.network.RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(),test.getLevel().registryAccess());
            try{ElectricalTravel.Payload.CODEC.encode(buf,arc);test.assertTrue(ElectricalTravel.Payload.CODEC.decode(buf).equals(arc),"Travel packet loses coordinates/dimension");}finally{buf.release();}
            test.assertTrue(ElectricalTravel.position(arc.from(),arc.to(),0).equals(arc.from()) && ElectricalTravel.position(arc.from(),arc.to(),5).equals(arc.to()) && ElectricalTravel.position(arc.from(),arc.to(),2).distanceTo(arc.from())<ElectricalTravel.position(arc.from(),arc.to(),3).distanceTo(arc.from()),"Sound path does not travel to the destination");
            test.runAfterDelay(20,()->{try{AlternatingCurrentEvents.tick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(player));test.assertTrue(wire.arcs.size()==2 && wire.arcs.getLast().from().equals(second.getEyePosition()) && wire.arcs.getLast().to().equals(first.getEyePosition()),"Next pulse audio fails to reverse: "+wire.arcs);var mid=net.minecraft.core.BlockPos.containing(first.getEyePosition().lerp(second.getEyePosition(),.5));for(int x=-1;x<=1;x++)for(int y=-1;y<=1;y++)test.getLevel().setBlockAndUpdate(mid.offset(x,y,0),Blocks.STONE.defaultBlockState());}catch(RuntimeException|Error e){cleanup.run();throw e;}});
            test.runAfterDelay(42,()->{try{test.assertTrue(wire.arcs.size()==2,"Blocked current plays travel sound");AlternatingCurrentEvents.cancel(player.getUUID());AlternatingCurrentEvents.tick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(player));test.assertTrue(wire.arcs.size()==2,"Canceled link plays current audio");test.succeed();}finally{cleanup.run();}});
        }catch(RuntimeException|Error e){cleanup.run();throw e;}
    }
}
