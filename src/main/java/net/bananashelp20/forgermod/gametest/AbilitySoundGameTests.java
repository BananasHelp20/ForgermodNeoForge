package net.bananashelp20.forgermod.gametest;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.abilities.*;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.*;
import net.minecraft.network.protocol.*;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.sounds.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.gametest.*;
import java.util.*;
@GameTestHolder(ForgerMod.MOD_ID) @PrefixGameTestTemplate(false)
public class AbilitySoundGameTests {
    private static final class Wire extends Connection {
        final List<ClientboundSoundPacket> sounds=new ArrayList<>();
        Wire(){super(PacketFlow.SERVERBOUND);new io.netty.channel.embedded.EmbeddedChannel(this);}
        @Override public void send(Packet<?> packet,PacketSendListener listener,boolean flush){if(packet instanceof ClientboundSoundPacket sound)sounds.add(sound);}
    }
    @GameTest(template="riftfang_test")
    public static void sonicallyChargedNativeCriticalPlaysOneImpactAfterAnOrdinaryHit(GameTestHelper test)throws Exception{
        int checked=0;
        for(var entry:ModItems.ITEMS.getEntries()){
            if(!(entry.get() instanceof net.bananashelp20.forgermod.item.custom.PulsiteWeapon weapon) || !weapon.isDagger())continue;checked++;
            var player=TestPlayers.survival(test);player.setNoGravity(true);player.setPos(test.getBounds().getCenter());
            var old=player.connection;var wire=new Wire();new net.minecraft.server.network.ServerGamePacketListenerImpl(player.server,wire,player,net.minecraft.server.network.CommonListenerCookie.createInitial(player.getGameProfile(),false));
            var target=net.minecraft.world.entity.EntityType.COW.create(test.getLevel());target.setNoAi(true);target.setNoGravity(true);target.setPos(player.position().add(0,0,2));target.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(1000);target.setHealth(1000);test.getLevel().addFreshEntity(target);
            java.util.function.LongSupplier impacts=()->wire.sounds.stream().filter(sound->sound.getSound().value()==SoundEvents.WARDEN_SONIC_BOOM).count();
            try{
                var stack=Augmentations.apply(new ItemStack(entry.get()),"tooltips.forgermod.ability.sonic_crit");player.setItemInHand(InteractionHand.MAIN_HAND,stack);for(int tick=0;tick<60;tick++)player.doTick();
                AbilityTestPackets.use(player,0);test.assertTrue(impacts.getAsLong()==0,"Arming plays charged-hit sound");
                player.setOnGround(true);player.fallDistance=0;player.setSprinting(false);player.attack(target);
                test.assertTrue(weapon.isAbilityActive(player,net.bananashelp20.forgermod.item.custom.WeaponAbilitySlot.PRIMARY) && impacts.getAsLong()==0,"Ordinary hit consumes or sounds charged crit");
                for(int tick=0;tick<60;tick++)player.doTick();target.invulnerableTime=0;player.setOnGround(false);player.fallDistance=1;player.attack(target);
                test.assertTrue(!weapon.isAbilityActive(player,net.bananashelp20.forgermod.item.custom.WeaponAbilitySlot.PRIMARY) && impacts.getAsLong()==1,"Native charged critical misses or duplicates impact sound");
                for(int tick=0;tick<60;tick++)player.doTick();target.invulnerableTime=0;player.setOnGround(false);player.fallDistance=1;player.attack(target);
                test.assertTrue(impacts.getAsLong()==1,"Uncharged critical replays sonic impact");
            }finally{WeaponAbilityNetwork.onLogout(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(player));net.bananashelp20.forgermod.item.custom.PulsiteWeapon.onLogout(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(player));DaggerCriticalEvents.onLogout(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(player));target.discard();player.connection=old;player.server.getPlayerList().remove(player);}
        }
        test.assertTrue(checked==5,"Skipped Pulsite dagger variants");test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void strengthenedBonesIsSilentUntilHostileMeleeKillGrantsProtection(GameTestHelper test)throws Exception{
        var player=TestPlayers.survival(test);player.setNoGravity(true);player.setPos(test.getBounds().getCenter());
        var old=player.connection;var wire=new Wire();new net.minecraft.server.network.ServerGamePacketListenerImpl(player.server,wire,player,net.minecraft.server.network.CommonListenerCookie.createInitial(player.getGameProfile(),false));
        var mobs=new java.util.ArrayList<net.minecraft.world.entity.Mob>();int checked=0;
        java.util.function.LongSupplier cues=()->wire.sounds.stream().filter(sound->sound.getSound().value()==net.bananashelp20.forgermod.sound.ModSounds.STRENGTHENED_BONES_HIT.get()).count();
        try{
            for(var entry:ModItems.ITEMS.getEntries()){
                if(!(entry.get() instanceof net.bananashelp20.forgermod.item.custom.MorsiumWeapon weapon) || !weapon.isDagger())continue;checked++;
                WeaponAbilityNetwork.onLogout(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(player));
                var stack=Augmentations.apply(new ItemStack(entry.get()),"tooltips.forgermod.ability.strengthened_bones");player.setItemInHand(InteractionHand.MAIN_HAND,stack);wire.sounds.clear();
                AbilityTestPackets.use(player,0);test.assertTrue(cues.getAsLong()==0,"Strengthened Bones plays sound when armed");
                for(int hit=0;hit<4;hit++){
                    var mob=net.minecraft.world.entity.EntityType.ZOMBIE.create(test.getLevel());mobs.add(mob);mob.setNoAi(true);mob.setNoGravity(true);mob.setPos(player.position().add(0,0,2));test.getLevel().addFreshEntity(mob);
                    if(hit==0)mob.hurt(player.damageSources().indirectMagic(player,player),100);
                    else {
                        mob.setHealth(hit==1?20:1);
                        for(int tick=0;tick<60;tick++)player.doTick();player.setOnGround(true);player.fallDistance=0;player.setSprinting(false);player.attack(mob);
                        test.assertTrue(mob.isAlive()==(hit==1),"Native attack fixture failed to produce the intended surviving/killing hit");
                    }
                    test.assertTrue(cues.getAsLong()==(hit<2?0:1),"Protection sound triggers on wrong hit or repeats");
                    if(hit==2) {
                        var cue=wire.sounds.stream().filter(sound->sound.getSound().value()==net.bananashelp20.forgermod.sound.ModSounds.STRENGTHENED_BONES_HIT.get()).findFirst().orElseThrow();
                        test.assertTrue(cue.getSource()==SoundSource.PLAYERS && cue.getVolume()==1F && cue.getPitch()==1F && Math.abs(cue.getZ()-(double)(float)mob.getZ())<.25,"Protection impact is too quiet or not placed at the killing hit");
                    }
                }
                player.setHealth(20);player.invulnerableTime=0;player.hurt(player.damageSources().generic(),4);test.assertTrue(player.getHealth()==20,"Audible kill does not grant invulnerability");
            }
            test.assertTrue(checked==5,"Skipped Morsium dagger variants");
        }finally{WeaponAbilityNetwork.onLogout(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(player));net.bananashelp20.forgermod.item.custom.MorsiumWeapon.onLogout(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(player));for(var mob:mobs)mob.discard();player.connection=old;player.server.getPlayerList().remove(player);}test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void allActiveVariantsAndRanksHaveExistingAudioOrBoundedRegisteredCues(GameTestHelper test){
        Set<String> ids=new HashSet<>();int checked=0;
        for(var entry:ModItems.ITEMS.getEntries()){
            var fresh=new ItemStack(entry.get());if(!Augmentations.eligible(fresh))continue;
            for(var ability:Augmentations.pool(fresh))if(ability.active()){
                for(int rank:new int[]{1,Augmentations.maxRank(fresh,ability.id())}){
                    var stack=fresh.copy();for(var prerequisite:Augmentations.prerequisites(stack,ability.id()))stack=Augmentations.setLevel(stack,prerequisite,1);
                    stack=Augmentations.setLevel(stack,ability.id(),rank);test.assertFalse(stack.isEmpty(),"Audio fixture cannot learn "+ability.id());
                    test.assertTrue(WeaponAbilitySounds.covered(stack,ability.id()),"Active lacks an audio decision: "+ability.id());
                    var clips=WeaponAbilitySounds.profile(stack,ability.id());test.assertTrue(clips.size()<=2,"Unbounded layered activation cue");
                    for(var clip:clips)test.assertTrue(BuiltInRegistries.SOUND_EVENT.containsKey(clip.sound().getLocation()) && clip.volume()>0 && clip.volume()<=1 && clip.pitch()>=.5 && clip.pitch()<=2,"Missing or unbounded sound event");
                    ids.add(ability.id());checked++;
                }
            }
        }
        test.assertTrue(ids.size()>=50 && checked>500,"Active audio catalog coverage incomplete");
        test.assertTrue(WeaponAbilitySounds.profile(new ItemStack(ModItems.STATIC_DAGGER.get()),AdditionalAbilities.THUNDERCLAP).isEmpty(),"Thunderclap dedicated audio was doubled");test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void onlySuccessfulActivationPlaysCueAndMagmaCollisionSoundsOnce(GameTestHelper test)throws Exception{
        var player=TestPlayers.survival(test);player.setNoGravity(true);player.setPos(test.getBounds().getCenter());player.setXRot(-90);
        var previous=player.connection;var wire=new Wire();new net.minecraft.server.network.ServerGamePacketListenerImpl(player.server,wire,player,net.minecraft.server.network.CommonListenerCookie.createInitial(player.getGameProfile(),false));
        var stack=Augmentations.apply(new ItemStack(ModItems.MOLTEN_AXE.get()),AdditionalAbilities.MAGMA_TOSS);player.setItemInHand(InteractionHand.MAIN_HAND,stack);player.doTick();wire.sounds.clear();
        try{
            AbilityTestPackets.use(player,0);var expected=WeaponAbilitySounds.profile(stack,AdditionalAbilities.MAGMA_TOSS);
            test.assertTrue(wire.sounds.size()==expected.size() && wire.sounds.stream().allMatch(packet->packet.getSource()==SoundSource.PLAYERS),"Accepted cast is silent or wrong sound category");
            for(var clip:expected)test.assertTrue(wire.sounds.stream().anyMatch(packet->packet.getSound().value()==clip.sound()),"Cast plays wrong material cue");
            wire.sounds.clear();AbilityTestPackets.use(player,0);test.assertTrue(wire.sounds.isEmpty(),"Cooldown failure plays activation audio");
            var rock=player.level().getEntitiesOfClass(Snowball.class,player.getBoundingBox().inflate(5),entity->entity.getOwner()==player).getFirst();var point=rock.position();
            var impact=new ProjectileImpactEvent(rock,new BlockHitResult(point,Direction.UP,BlockPos.containing(point),false));MagmaTossEvents.impact(impact);MagmaTossEvents.impact(impact);
            test.assertTrue(wire.sounds.size()==1 && wire.sounds.getFirst().getSound().value()==SoundEvents.GENERIC_EXPLODE.value(),"Magma impact silent or duplicated");
            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModItems.MOLTEN_AXE.get()));wire.sounds.clear();AbilityTestPackets.use(player,0);test.assertTrue(wire.sounds.isEmpty(),"Unlearned cast plays activation audio");
        }finally{MagmaTossEvents.cancel(player.getUUID());player.connection=previous;player.server.getPlayerList().remove(player);}test.succeed();
    }
}
