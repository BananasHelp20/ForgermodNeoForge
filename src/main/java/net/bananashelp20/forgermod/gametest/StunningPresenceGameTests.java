package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.ElectriumWeapon;
import net.bananashelp20.forgermod.item.custom.abilities.*;
import net.bananashelp20.forgermod.particle.StunningPresenceParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class StunningPresenceGameTests {
    @GameTest(template="riftfang_test")
    public static void revisedDescriptionUsesCurrentWhiteRadiusAndPulse(GameTestHelper test) {
        int count=0;
        for(var entry:ModItems.ITEMS.getEntries()) {
            if(!(entry.get() instanceof ElectriumWeapon weapon) || weapon.isDagger() || weapon.isAxe()) continue;
            count++;
            for(int rank=1;rank<=4;rank++) for(boolean compact:new boolean[]{false,true}) {
                var text=AbilityDescriptions.description(new ItemStack(entry.get()),AdditionalAbilities.STUNNING_PRESENCE,rank,compact);
                var white=new StringBuilder();
                text.visit((style,part)->{if(style.getColor()!=null && style.getColor().getValue()==net.minecraft.ChatFormatting.WHITE.getColor())white.append(part);return java.util.Optional.<Void>empty();},net.minecraft.network.chat.Style.EMPTY);
                String pulse=new java.math.BigDecimal(Math.round(40-(rank-1)*20F/3)).divide(java.math.BigDecimal.valueOf(20)).stripTrailingZeros().toPlainString();
                test.assertTrue(text.getString().equals("Create an area (10 blocks radius) around you where you stun every entity for a short time every "+pulse+" seconds."),"Wrong current aura description: "+text.getString());
                test.assertTrue(white.toString().equals("10 blocks radius"+pulse+" seconds"),"Wrong aura highlights: "+white);
            }
        }
        test.assertTrue(count==5,"Skipped aura variants");test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void stunsAlliedPetsAndPlayersButNotCaster(GameTestHelper test) {
        var owner=TestPlayers.survival(test);var other=TestPlayers.survival(test);
        var origin=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8)));
        for(int x=6;x<=11;x++) for(int y=5;y<=8;y++) for(int z=6;z<=11;z++) test.setBlock(new BlockPos(x,y,z),Blocks.AIR);
        owner.setPos(origin);other.setPos(origin.add(2,0,0));
        var pet=EntityType.WOLF.create(test.getLevel());pet.tame(owner);pet.setNoAi(true);pet.setNoGravity(true);pet.setPos(origin.add(0,0,2));test.getLevel().addFreshEntity(pet);
        try {
            var stack=Augmentations.apply(new ItemStack(ModItems.CLAYMORE_OF_THUNDER.get()),AdditionalAbilities.STUNNING_PRESENCE);owner.setItemInHand(InteractionHand.MAIN_HAND,stack);
            float health=owner.getHealth();
            test.assertTrue(pet.isAlliedTo(owner),"Pet is not an ally");
            test.assertTrue(RecommendedAbilityRuntime.activate(owner,stack,AdditionalAbilities.STUNNING_PRESENCE),"All-living aura rejected");
            test.assertTrue(WeaponControlLocks.frozen(pet) && WeaponControlLocks.frozen(other),"Aura excludes allied pet or other player");
            test.assertTrue(!WeaponControlLocks.frozen(owner) && owner.getHealth()==health,"Aura affects its caster");
            StunningPresenceEvents.cancel(owner.getUUID());
            test.assertTrue(!WeaponControlLocks.frozen(pet) && !WeaponControlLocks.frozen(other),"Cancel leaves friendly targets stunned");
        } finally {StunningPresenceEvents.cancel(owner.getUUID());pet.discard();owner.server.getPlayerList().remove(other);owner.server.getPlayerList().remove(owner);}
        test.succeed();
    }
    private static final class RecordingConnection extends net.minecraft.network.Connection {
        final java.util.List<net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket> particles=new java.util.ArrayList<>();
        RecordingConnection(){super(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);}
        @Override public void send(net.minecraft.network.protocol.Packet<?> packet,net.minecraft.network.PacketSendListener listener,boolean flush) {
            if(packet instanceof net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket p) particles.add(p);
        }
    }
    @GameTest(template="riftfang_test")
    public static void electricalBoundaryIsVisibleWithoutTargetsAndScalesWithRank(GameTestHelper test) {
        var player=TestPlayers.survival(test);var connection=new RecordingConnection();
        var previous=player.connection;
        new net.minecraft.server.network.ServerGamePacketListenerImpl(test.getLevel().getServer(),connection,player,
            net.minecraft.server.network.CommonListenerCookie.createInitial(player.getGameProfile(),false));
        player.setPos(Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8))));
        try {
            var stack=new ItemStack(ModItems.CLAYMORE_OF_THUNDER.get());
            for(int rank=1;rank<=4;rank++) {
                stack=Augmentations.apply(stack,AdditionalAbilities.STUNNING_PRESENCE);
                player.setItemInHand(InteractionHand.MAIN_HAND,stack);connection.particles.clear();
                test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.STUNNING_PRESENCE),"Particle aura activation rejected");
                int boundary=0,inner=0,patterns=0;
                for(var packet:connection.particles) {
                    if(!(packet.getParticle() instanceof StunningPresenceParticles.Options options))continue;
                    patterns++;
                    test.assertTrue(options.owner()==player.getId() && options.rank()==rank && packet.getCount()==0,"Pattern loses owner/rank or uses random particle offsets");
                    if(options.boundary()) {
                        boundary=StunningPresenceParticles.points(options);
                        for(int i=0;i<boundary;i++) {
                            var point=StunningPresenceParticles.position(player.position(),options,i,0);
                            test.assertTrue(Math.abs(Math.hypot(point.x-player.getX(),point.z-player.getZ())-10)<.001,"Boundary misrepresents the actual ten-block area");
                            var next=StunningPresenceParticles.position(player.position(),options,(i+1)%boundary,0);
                            test.assertTrue(point.distanceTo(next)<.41,"Boundary has large gaps");
                        }
                    } else {
                        inner=StunningPresenceParticles.points(options);
                        test.assertTrue(StunningPresenceParticles.radiusAtAge(options,0)==1
                                && StunningPresenceParticles.radiusAtAge(options,5)==6
                                && StunningPresenceParticles.radiusAtAge(options,20)==10,"Pulse is not smooth or exceeds the affected area");
                    }
                    var buffer=new net.minecraft.network.RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(),test.getLevel().registryAccess());
                    try {
                        net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket.STREAM_CODEC.encode(buffer,packet);
                        var copy=net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket.STREAM_CODEC.decode(buffer);
                        test.assertTrue(copy.getParticle().equals(options),"Particle packet loses ring geometry");
                    } finally {buffer.release();}
                }
                test.assertTrue(boundary==160+32*(rank-1) && inner==60+12*(rank-1) && patterns==2,"Dense boundary/ripple missing or too many ring packets");
                test.assertTrue(StunningPresenceParticles.LIFETIME>StunningPresenceParticles.VISUAL_INTERVAL
                        && StunningPresenceParticles.LIFETIME<2*StunningPresenceParticles.VISUAL_INTERVAL
                        && 2*(boundary+inner)<=704,"Visuals flicker between updates or exceed the live-particle budget");
                StunningPresenceEvents.cancel(player.getUUID());connection.particles.clear();
                StunningPresenceEvents.tick(new PlayerTickEvent.Post(player));
                test.assertTrue(connection.particles.isEmpty(),"Canceled aura still emits particles");
            }
            test.succeed();
        } finally {StunningPresenceEvents.cancel(player.getUUID());player.connection=previous;player.server.getPlayerList().remove(player);}
    }
    @GameTest(template="riftfang_test")
    public static void fiveClaymoresPulseAllOtherVisibleLivingEntitiesAndCancelStun(GameTestHelper test) {
        var player=TestPlayers.survival(test);var origin=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8)));player.setPos(origin);
        for(int x=5;x<=20;x++) for(int y=5;y<=8;y++) for(int z=5;z<=12;z++) test.setBlock(new BlockPos(x,y,z),Blocks.AIR);
        var near=EntityType.ZOMBIE.create(test.getLevel());var far=EntityType.ZOMBIE.create(test.getLevel());var neutral=EntityType.COW.create(test.getLevel());
        for(var mob:java.util.List.of(near,far)) {mob.setNoGravity(true);mob.getAttribute(Attributes.ARMOR).setBaseValue(0);test.getLevel().addFreshEntity(mob);}near.setPos(origin.add(0,0,3));far.setPos(origin.add(11,0,0));neutral.setNoAi(true);neutral.setNoGravity(true);neutral.setPos(origin.add(0,0,2));test.getLevel().addFreshEntity(neutral);float neutralHealth=neutral.getHealth();int count=0;
        try {
            for(var entry:ModItems.ITEMS.getEntries()) {
                var fresh=new ItemStack(entry.get());boolean eligible=entry.get() instanceof ElectriumWeapon weapon && !weapon.isDagger() && !weapon.isAxe();
                test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(a->a.id().equals(AdditionalAbilities.STUNNING_PRESENCE))==eligible,"Wrong Stunning Presence assignment");
                if(!eligible) continue;count++;near.setHealth(20);far.setHealth(20);neutral.setHealth(neutralHealth);player.setItemInHand(InteractionHand.MAIN_HAND,fresh);test.assertFalse(RecommendedAbilityRuntime.activate(player,fresh,AdditionalAbilities.STUNNING_PRESENCE),"Fresh claymore pulses");
                var stack=Augmentations.apply(fresh,AdditionalAbilities.STUNNING_PRESENCE);player.setItemInHand(InteractionHand.MAIN_HAND,stack);test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.STUNNING_PRESENCE),"Learned aura rejected");
                test.assertTrue(near.getHealth()==19 && WeaponControlLocks.frozen(near) && far.getHealth()==20 && neutral.getHealth()==neutralHealth-1 && WeaponControlLocks.frozen(neutral) && !WeaponControlLocks.frozen(player),"Pulse has wrong damage, radius or target filter");
                player.invulnerableTime=0;test.assertFalse(player.hurt(player.damageSources().mobAttack(near),2),"Frozen mob can still inflict a melee attack");
                test.assertTrue(StunningPresenceEvents.remainingTicks(player)==600,"Aura does not last 30 seconds");StunningPresenceEvents.tick(new PlayerTickEvent.Post(player));test.assertTrue(near.getHealth()==19,"Aura pulses more than once per interval");
                player.setItemInHand(InteractionHand.MAIN_HAND,stack.copy());test.assertFalse(StunningPresenceEvents.active(player),"Switch retains aura");test.assertFalse(WeaponControlLocks.frozen(near),"Switch leaves stun");
                far.setPos(origin.add(9,0,0));near.setHealth(20);far.setHealth(20);player.setItemInHand(InteractionHand.MAIN_HAND,stack);test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.STUNNING_PRESENCE),"Level-I boundary activation rejected");test.assertTrue(far.getHealth()==19 && WeaponControlLocks.frozen(far),"Level I does not reach nine blocks");StunningPresenceEvents.cancel(player.getUUID());
                for(int i=1;i<4;i++) stack=Augmentations.apply(stack,AdditionalAbilities.STUNNING_PRESENCE);player.setItemInHand(InteractionHand.MAIN_HAND,stack);near.setHealth(20);far.setHealth(20);test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.STUNNING_PRESENCE),"Rank IV aura rejected");test.assertTrue(far.getHealth()==19,"Rank IV does not reach ten blocks");StunningPresenceEvents.cancel(player.getUUID());
                test.setBlock(new BlockPos(8,6,10),Blocks.STONE);near.setHealth(20);player.setItemInHand(InteractionHand.MAIN_HAND,stack);RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.STUNNING_PRESENCE);test.assertTrue(near.getHealth()==20,"Aura attacks through wall");StunningPresenceEvents.cancel(player.getUUID());test.setBlock(new BlockPos(8,6,10),Blocks.AIR);far.setPos(origin.add(11,0,0));
            }
        } finally {StunningPresenceEvents.cancel(player.getUUID());near.discard();far.discard();neutral.discard();player.server.getPlayerList().remove(player);}
        test.assertTrue(count==5,"Skipped Stunning Presence variants");test.succeed();
    }
    @GameTest(template="riftfang_test",timeoutTicks=100)
    public static void firstRankRepeatsEveryTwoSecondsAndStunLastsTenTicks(GameTestHelper test) {
        var player=TestPlayers.survival(test);var origin=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8)));player.setPos(origin);
        for(int y=5;y<=8;y++) for(int z=8;z<=14;z++) test.setBlock(new BlockPos(8,y,z),Blocks.AIR);
        var mob=EntityType.ZOMBIE.create(test.getLevel());mob.setNoAi(true);mob.setNoGravity(true);mob.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD,new ItemStack(Blocks.CARVED_PUMPKIN));mob.getAttribute(Attributes.ARMOR).setBaseValue(0);mob.setPos(origin.add(0,0,3));test.getLevel().addFreshEntity(mob);
        var stack=Augmentations.apply(new ItemStack(ModItems.CLAYMORE_OF_THUNDER.get()),AdditionalAbilities.STUNNING_PRESENCE);player.setItemInHand(InteractionHand.MAIN_HAND,stack);test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.STUNNING_PRESENCE),"Timed aura rejected");
        test.runAfterDelay(9,()->test.assertTrue(WeaponControlLocks.frozen(mob),"Level-I stun ends before 0.5 seconds"));
        test.runAfterDelay(10,()->test.assertFalse(WeaponControlLocks.frozen(mob),"Stun lasts longer than 0.5 seconds"));
        test.runAfterDelay(20,()->{StunningPresenceEvents.tick(new PlayerTickEvent.Post(player));test.assertTrue(mob.getHealth()==19,"Rank I pulses before two seconds");});
        test.runAfterDelay(40,()->{try {StunningPresenceEvents.tick(new PlayerTickEvent.Post(player));test.assertTrue(mob.getHealth()==18 && WeaponControlLocks.frozen(mob),"Two-second pulse missing or wrong");test.succeed();}finally {StunningPresenceEvents.cancel(player.getUUID());mob.discard();player.server.getPlayerList().remove(player);}});
    }
}
