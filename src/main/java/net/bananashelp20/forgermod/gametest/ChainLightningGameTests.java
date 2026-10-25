package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.ElectriumWeapon;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(ForgerMod.MOD_ID) @PrefixGameTestTemplate(false)
public class ChainLightningGameTests {
    private static final class ArcConnection extends net.minecraft.network.Connection {
        final java.util.List<net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket> arcs=new java.util.ArrayList<>();
        final java.util.List<net.bananashelp20.forgermod.sound.ElectricalTravel.Payload> travel=new java.util.ArrayList<>();
        ArcConnection(){super(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);new io.netty.channel.embedded.EmbeddedChannel(this);net.neoforged.neoforge.network.registration.ChannelAttributes.getOrCreateAdHocChannels(this).add(net.bananashelp20.forgermod.sound.ElectricalTravel.Payload.TYPE.id());}
        @Override public void send(net.minecraft.network.protocol.Packet<?> packet,net.minecraft.network.PacketSendListener listener,boolean flush){
            if(packet instanceof net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket custom && custom.payload() instanceof net.bananashelp20.forgermod.sound.ElectricalTravel.Payload hop)travel.add(hop);
            if(packet instanceof net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket particle
                    && particle.getParticle() instanceof net.bananashelp20.forgermod.particle.LightningArcParticles.Options)arcs.add(particle);
        }
    }
    @GameTest(template="riftfang_test")
    public static void fiveDaggersChainThroughPassivePetsFlyingMobsAndAlliedPlayers(GameTestHelper test) throws Exception {
        var player=TestPlayers.survival(test);var other=TestPlayers.survival(test);
        var previousConnection=player.connection;var wire=new ArcConnection();
        var previousOther=other.connection;var otherWire=new ArcConnection();new net.minecraft.server.network.ServerGamePacketListenerImpl(player.server,otherWire,other,net.minecraft.server.network.CommonListenerCookie.createInitial(other.getGameProfile(),false));
        new net.minecraft.server.network.ServerGamePacketListenerImpl(player.server,wire,player,net.minecraft.server.network.CommonListenerCookie.createInitial(player.getGameProfile(),false));
        var origin=test.getBounds().getCenter().add(0,20,0);player.setPos(origin);player.setNoGravity(true);
        var original=EntityType.ZOMBIE.create(test.getLevel());original.setPos(origin.add(0,0,2));original.setNoAi(true);test.getLevel().addFreshEntity(original);
        var cow=EntityType.COW.create(test.getLevel());var wolf=EntityType.WOLF.create(test.getLevel());wolf.tame(player);var bat=EntityType.BAT.create(test.getLevel());
        var targets=java.util.List.<LivingEntity>of(cow,wolf,bat,other);
        for(int i=0;i<targets.size();i++) {
            var target=targets.get(i);target.setPos(origin.add(0,0,2+9*(i+1)));target.setNoGravity(true);
            target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200);
            if(target instanceof Mob mob){mob.setNoAi(true);test.getLevel().addFreshEntity(mob);}
        }
        var team=player.getScoreboard().addPlayerTeam("chain_"+player.getUUID().toString().substring(0,8));
        for(var member:java.util.List.of(player,cow,other))player.getScoreboard().addPlayerToTeam(member.getScoreboardName(),team);
        boolean pvp=player.server.isPvpAllowed();player.server.setPvpAllowed(true);
        var method=ElectriumWeapon.class.getDeclaredMethod("chainLightning",LivingEntity.class,net.minecraft.server.level.ServerPlayer.class);method.setAccessible(true);
        long seed=0;
        for(;;seed++) {var rng=net.minecraft.util.RandomSource.create(seed);boolean matches=true;for(int i=0;i<4;i++)matches&=rng.nextBoolean();if(matches && !rng.nextBoolean())break;}
        int count=0;
        try {
            for(var entry:ModItems.ITEMS.getEntries()) {
                if(!(entry.get() instanceof ElectriumWeapon weapon) || !weapon.isDagger())continue;
                count++;player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(entry.get()));player.doTick();
                for(var target:targets){target.setHealth(200);target.invulnerableTime=0;}
                float before=player.getHealth();float expected=(float)player.getAttributeValue(Attributes.ATTACK_DAMAGE)*.5F;
                wire.arcs.clear();wire.travel.clear();otherWire.travel.clear();var bolts=player.level().getEntitiesOfClass(LightningBolt.class,player.getBoundingBox().inflate(60)).stream().map(Entity::getUUID).toList();
                player.getRandom().setSeed(seed);method.invoke(weapon,original,player);
                test.assertTrue(wire.arcs.size()==4,"A successful hop did not send one jagged lightning arc");
                var sounds=new java.util.ArrayList<>(wire.travel);sounds.addAll(otherWire.travel);
                test.assertTrue(sounds.size()==4 && sounds.stream().allMatch(net.bananashelp20.forgermod.sound.ElectricalTravel.Payload::chain),"Successful chains lack their individual traveling sound, or far observers receive extra paths");
                var soundBuffer=new net.minecraft.network.RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(),test.getLevel().registryAccess());
                try{net.bananashelp20.forgermod.sound.ElectricalTravel.Payload.CODEC.encode(soundBuffer,sounds.getFirst());test.assertTrue(net.bananashelp20.forgermod.sound.ElectricalTravel.Payload.CODEC.decode(soundBuffer).equals(sounds.getFirst()),"Chain audio profile is lost in packet codec");}finally{soundBuffer.release();}
                LivingEntity previous=original;
                for(int hop=0;hop<targets.size();hop++){
                    var expectedFrom=previous.getBoundingBox().getCenter();var expectedTo=targets.get(hop).getBoundingBox().getCenter();
                    test.assertTrue(sounds.stream().filter(sound->sound.from().equals(expectedFrom) && sound.to().equals(expectedTo)).count()==1,"Sound path differs from its actual chain hop");
                    var packet=wire.arcs.get(hop);var options=(net.bananashelp20.forgermod.particle.LightningArcParticles.Options)packet.getParticle();
                    var from=new net.minecraft.world.phys.Vec3(packet.getX(),packet.getY(),packet.getZ());
                    test.assertTrue(from.distanceTo(previous.getBoundingBox().getCenter())<.001 && from.add(options.delta()).distanceTo(targets.get(hop).getBoundingBox().getCenter())<.001
                            && options.owner()==-1 && options.ticks()==5,"Lightning arc endpoints/lifetime are incorrect");previous=targets.get(hop);
                }
                test.assertTrue(player.level().getEntitiesOfClass(LightningBolt.class,player.getBoundingBox().inflate(60)).stream().map(Entity::getUUID).toList().equals(bolts),"Exchange spawns actual vanilla lightning");
                for(var target:targets)test.assertTrue(Math.abs(target.getHealth()-(200-expected))<.001,"Chain misses or repeats allied/passive target: "+target.getType());
                test.assertTrue(player.getHealth()==before && original.getHealth()==original.getMaxHealth(),"Chain damages caster or revisits initial target");
                long noChain=0;while(net.minecraft.util.RandomSource.create(noChain).nextBoolean())noChain++;
                player.getRandom().setSeed(noChain);method.invoke(weapon,original,player);
                test.assertTrue(wire.travel.size()+otherWire.travel.size()==4,"Failed chain chance plays a traveling electrical sound");
            }
            test.assertTrue(count==5,"Skipped Electrium dagger variants");test.succeed();
        } finally {
            original.discard();cow.discard();wolf.discard();bat.discard();player.getScoreboard().removePlayerTeam(team);player.server.setPvpAllowed(pvp);
            player.connection=previousConnection;other.connection=previousOther;player.server.getPlayerList().remove(other);player.server.getPlayerList().remove(player);
        }
    }
}
