package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.LushWeapon;
import net.bananashelp20.forgermod.item.custom.abilities.*;
import net.bananashelp20.forgermod.sound.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(ForgerMod.MOD_ID) @PrefixGameTestTemplate(false)
public class RootingRootsSoundGameTests {
    private static final class Wire extends net.minecraft.network.Connection {
        final java.util.List<ClientboundSoundPacket> sounds=new java.util.ArrayList<>();
        Wire(){super(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);new io.netty.channel.embedded.EmbeddedChannel(this);}
        @Override public void send(net.minecraft.network.protocol.Packet<?> packet,net.minecraft.network.PacketSendListener listener,boolean flush) {
            if(packet instanceof ClientboundSoundPacket sound && java.util.Set.of(ModSounds.ROOTING_ROOTS_GROW.get(),ModSounds.ROOTING_ROOTS_HOLD.get(),ModSounds.ROOTING_ROOTS_RETREAT.get()).contains(sound.getSound().value()))sounds.add(sound);
        }
        long count(SoundEvent sound){return sounds.stream().filter(p->p.getSound().value()==sound).count();}
    }
    @GameTest(template="riftfang_test",timeoutTicks=230)
    public static void naturalExpiryPlaysGrowthCreaksAndRetreat(GameTestHelper test) throws Exception {lifecycle(test,false);}
    @GameTest(template="riftfang_test",timeoutTicks=130)
    public static void switchPlaysOneRetreatAndStopsCreaks(GameTestHelper test) throws Exception {lifecycle(test,true);}
    private static void lifecycle(GameTestHelper test,boolean cancel) throws Exception {
        var player=TestPlayers.survival(test);player.setNoGravity(true);
        var start=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8)));player.setPos(start);
        for(int x=6;x<=10;x++)for(int z=6;z<=12;z++){test.setBlock(new BlockPos(x,4,z),Blocks.STONE);for(int y=5;y<=8;y++)test.setBlock(new BlockPos(x,y,z),Blocks.AIR);}
        var mob=EntityType.CREEPER.create(test.getLevel());mob.setNoAi(true);mob.setNoGravity(true);mob.setPos(start.add(0,0,2));test.getLevel().addFreshEntity(mob);
        var stack=Augmentations.apply(new ItemStack(ModItems.LEAFCUTTER_DAGGER.get()),"tooltips.forgermod.ability.rooting_roots");player.setItemInHand(InteractionHand.MAIN_HAND,stack);
        var old=player.connection;var wire=new Wire();new net.minecraft.server.network.ServerGamePacketListenerImpl(player.server,wire,player,net.minecraft.server.network.CommonListenerCookie.createInitial(player.getGameProfile(),false));
        AbilityTestPackets.use(player,0);
        test.runAfterDelay(85,()->{
            test.assertTrue(wire.count(ModSounds.ROOTING_ROOTS_GROW.get())==1,"Growth sound repeats or is missing");
            test.assertTrue(wire.count(ModSounds.ROOTING_ROOTS_HOLD.get())>=1 && wire.count(ModSounds.ROOTING_ROOTS_HOLD.get())<=2,"Held roots lack bounded tension creaks");
            test.assertTrue(wire.count(ModSounds.ROOTING_ROOTS_RETREAT.get())==0,"Roots withdraw before release");
            test.assertTrue(wire.sounds.stream().allMatch(p->Math.abs(p.getZ()-(double)(float)mob.getZ())<.25 && p.getSource()==net.minecraft.sounds.SoundSource.PLAYERS),"Root audio is not spatially attached to the bound target");
            if(cancel){player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);WeaponAbilityNetwork.onPlayerTick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Pre(player));RootingRootsVisuals.retractOwner(player.getUUID());}
        });
        int end=cancel?87:202;
        test.runAfterDelay(end,()->{
            LushWeapon.onLevelTick(new net.neoforged.neoforge.event.tick.LevelTickEvent.Post(()->true,test.getLevel()));
            test.assertTrue(wire.count(ModSounds.ROOTING_ROOTS_RETREAT.get())==1,"Release must play exactly one retreat sound");
            int count=wire.sounds.size();
            test.runAfterDelay(15,()->{try{test.assertTrue(wire.sounds.size()==count,"Root audio continues after release");test.succeed();}
                finally{LushWeapon.onLogout(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(player));mob.discard();player.connection=old;player.server.getPlayerList().remove(player);}});
        });
    }
    @GameTest(template="riftfang_test")
    public static void overlappingRootsAreLimitedLocallyNotAcrossTheWorld(GameTestHelper test){
        var player=TestPlayers.survival(test);player.setPos(test.getBounds().getCenter());var old=player.connection;var wire=new Wire();new net.minecraft.server.network.ServerGamePacketListenerImpl(player.server,wire,player,net.minecraft.server.network.CommonListenerCookie.createInitial(player.getGameProfile(),false));
        try{
            var p=player.position();for(int i=0;i<20;i++)RootingRootsSounds.play(test.getLevel(),p.x,p.y,p.z,RootingRootsSounds.Phase.GROW);
            test.assertTrue(wire.sounds.size()==3,"Large groups stack unbounded root audio");
            double cell=Math.floor(p.x/16)*16;
            double adjacent=p.x-cell<8?cell-.25:cell+16.25;
            RootingRootsSounds.play(test.getLevel(),adjacent,p.y,p.z,RootingRootsSounds.Phase.GROW);
            test.assertTrue(wire.sounds.size()==4,"A separate area loses its root cue");
            test.succeed();
        }finally{player.connection=old;player.server.getPlayerList().remove(player);}
    }
}
