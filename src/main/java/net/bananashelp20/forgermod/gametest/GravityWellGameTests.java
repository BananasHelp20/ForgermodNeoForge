package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.AdditionalAbilities;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.InanisiumWeapon;
import net.bananashelp20.forgermod.item.custom.abilities.GravityWellEvents;
import net.bananashelp20.forgermod.item.custom.abilities.RecommendedAbilityRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class GravityWellGameTests {
    private static final class SoundWire extends net.minecraft.network.Connection {
        final java.util.List<net.minecraft.sounds.SoundEvent> sounds=new java.util.ArrayList<>();
        SoundWire(){super(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);new io.netty.channel.embedded.EmbeddedChannel(this);}
        @Override public void send(net.minecraft.network.protocol.Packet<?> packet,net.minecraft.network.PacketSendListener listener,boolean flush){
            if(packet instanceof net.minecraft.network.protocol.game.ClientboundSoundPacket sound && java.util.List.of(net.bananashelp20.forgermod.sound.ModSounds.VOID_PULSE_CHARGE.get(),net.minecraft.sounds.SoundEvents.MACE_SMASH_GROUND_HEAVY,net.minecraft.sounds.SoundEvents.ANVIL_LAND,net.minecraft.sounds.SoundEvents.ENDERMAN_TELEPORT).contains(sound.getSound().value()))sounds.add(sound.getSound().value());
        }
    }
    @GameTest(template="riftfang_test")
    public static void aimedTargetAloneIsPulledAndArrivalHitsTheVerticalStrikeArea(GameTestHelper test) {
        var player=TestPlayers.survival(test);player.setNoGravity(true);player.setPos(test.getBounds().getCenter());player.setYRot(0);player.setXRot(0);
        for(int x=-4;x<=4;x++)for(int y=0;y<4;y++)for(int z=0;z<=7;z++)
            test.getLevel().setBlockAndUpdate(player.blockPosition().offset(x,y,z),Blocks.AIR.defaultBlockState());
        var target=EntityType.ZOMBIE.create(test.getLevel());var side=EntityType.ZOMBIE.create(test.getLevel());var near=EntityType.ZOMBIE.create(test.getLevel());
        for(var mob:java.util.List.of(target,side,near)) {
            mob.setNoAi(true);mob.setNoGravity(true);mob.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(100);
            mob.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR).setBaseValue(0);mob.setHealth(100);test.getLevel().addFreshEntity(mob);
        }
        target.setPos(player.position().add(0,0,5));side.setPos(player.position().add(2,0,5));near.setPos(player.position().add(.8,0,2.6));
        var stack=Augmentations.apply(new ItemStack(ModItems.NULLIFIED_AXE.get()),AdditionalAbilities.GRAVITY_WELL);
        player.setItemInHand(InteractionHand.MAIN_HAND,stack);
        try {
            test.assertTrue(GravityWellEvents.activate(player,stack),"Aimed cast fails");
            test.assertTrue(target.getDeltaMovement().z<0 && side.getDeltaMovement().equals(Vec3.ZERO) && near.getDeltaMovement().equals(Vec3.ZERO),"Pull selects more than aimed target");
            target.setPos(player.position().add(0,0,2));GravityWellEvents.tick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(player));AxeAnimationTestDriver.finish(player);
            test.assertTrue(target.getHealth()<100 && near.getHealth()<100 && side.getHealth()==100,"Arrival is not a bounded 3x3 vertical area strike");
            test.assertFalse(GravityWellEvents.active(player),"Arrival does not finish pull");
        } finally {
            GravityWellEvents.cancel(player.getUUID());for(var mob:java.util.List.of(target,side,near))mob.discard();player.server.getPlayerList().remove(player);
        }
        test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void fiveAxesPullWithoutDamageThenSmashOnceAndCancelOnSwitch(GameTestHelper test) {
        var player=TestPlayers.survival(test);var origin=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8)));player.setPos(origin);player.setYRot(0);
        for(int x=5;x<=11;x++) for(int y=5;y<=8;y++) for(int z=6;z<=14;z++) test.setBlock(new BlockPos(x,y,z),Blocks.AIR);
        var mob=EntityType.ZOMBIE.create(test.getLevel());mob.setNoAi(true);mob.setNoGravity(true);mob.setPos(origin.add(0,0,5));test.getLevel().addFreshEntity(mob);
        int count=0;
        var old=player.connection;var wire=new SoundWire();new net.minecraft.server.network.ServerGamePacketListenerImpl(player.server,wire,player,net.minecraft.server.network.CommonListenerCookie.createInitial(player.getGameProfile(),false));
        try {
            for(var entry:ModItems.ITEMS.getEntries()) {
                var fresh=new ItemStack(entry.get());boolean eligible=entry.get() instanceof InanisiumWeapon weapon && weapon.isAxe();
                test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(a->a.id().equals(AdditionalAbilities.GRAVITY_WELL))==eligible,"Wrong Gravity Well assignment");
                if(!eligible) continue;count++;mob.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR).setBaseValue(0);mob.setHealth(20);mob.setPos(origin.add(0,0,5));mob.setDeltaMovement(Vec3.ZERO);player.setItemInHand(InteractionHand.MAIN_HAND,fresh);
                wire.sounds.clear();test.assertFalse(RecommendedAbilityRuntime.activate(player,fresh,AdditionalAbilities.GRAVITY_WELL),"Fresh axe pulls");
                var stack=Augmentations.apply(fresh,AdditionalAbilities.GRAVITY_WELL);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
                test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.GRAVITY_WELL),"Learned pull rejected");
                var pullSound=net.bananashelp20.forgermod.sound.ModSounds.VOID_PULSE_CHARGE.get();
                test.assertTrue(wire.sounds.equals(java.util.List.of(pullSound)),"Pull lacks reversed ender cue or plays strike early");
                test.assertTrue(mob.position().equals(origin.add(0,0,5)) && Math.abs(mob.getDeltaMovement().z+.6)<.0001,"Pull teleports or has wrong velocity");
                test.assertTrue(mob.getHealth()==20,"Pull itself deals damage");
                mob.setPos(origin.add(0,0,2.8));mob.setDeltaMovement(Vec3.ZERO);GravityWellEvents.tick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(player));AxeAnimationTestDriver.finish(player);
                test.assertTrue(mob.getHealth()==20 && GravityWellEvents.active(player),"Smash fires before reaching the fixed point");
                test.assertTrue(mob.getDeltaMovement().z<0,"Pull stops before arrival");
                player.setYRot(90);GravityWellEvents.tick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(player));AxeAnimationTestDriver.finish(player);
                test.assertTrue(Math.abs(mob.getDeltaMovement().x)<.0001,"Turning moves the captured destination");player.setYRot(0);
                player.setPos(origin.add(0,0,-2));mob.setPos(origin.add(0,0,2));GravityWellEvents.tick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(player));AxeAnimationTestDriver.finish(player);
                test.assertTrue(mob.getHealth()==20 && GravityWellEvents.active(player) && mob.getDeltaMovement().equals(Vec3.ZERO),"Moving away shifts destination or permits a smash outside reach");
                test.assertTrue(wire.sounds.equals(java.util.List.of(pullSound)),"Approach/out-of-reach arrival plays impact audio");
                player.setPos(origin);GravityWellEvents.tick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(player));AxeAnimationTestDriver.finish(player);
                test.assertFalse(GravityWellEvents.active(player),"Arrival does not finish visual smash");test.assertTrue(Math.abs(mob.getHealth()-(20-player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE)))<.0001,"Arrival smash does not deal weapon damage");
                var impactSounds=java.util.List.of(pullSound,net.minecraft.sounds.SoundEvents.MACE_SMASH_GROUND_HEAVY,net.minecraft.sounds.SoundEvents.ANVIL_LAND,net.minecraft.sounds.SoundEvents.ENDERMAN_TELEPORT);
                test.assertTrue(wire.sounds.equals(impactSounds),"Arrival loses native Vertical Strike or ender impact layer");
                float health=mob.getHealth();GravityWellEvents.tick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(player));AxeAnimationTestDriver.finish(player);test.assertTrue(mob.getHealth()==health && wire.sounds.equals(impactSounds),"Smash or impact sound repeats after arrival");
                mob.setPos(origin.add(0,0,5));test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.GRAVITY_WELL),"Cannot start switch test");
                player.setItemInHand(InteractionHand.MAIN_HAND,stack.copy());test.assertFalse(GravityWellEvents.active(player),"Switch retains pull state");GravityWellEvents.tick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(player));AxeAnimationTestDriver.finish(player);test.assertTrue(wire.sounds.size()==5 && wire.sounds.getLast()==pullSound,"Canceled pull plays ender smash audio");
            }
        } finally {GravityWellEvents.cancel(player.getUUID());mob.discard();player.connection=old;player.server.getPlayerList().remove(player);}
        test.assertTrue(count==5,"Skipped Gravity Well variants");test.succeed();
    }

    @GameTest(template="riftfang_test",timeoutTicks=80)
    public static void collisionPhysicsArrivesBeforeSmashAndBlockedPullNeverSmashes(GameTestHelper test) {
        var player=TestPlayers.survival(test);var origin=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8)));player.setPos(origin);player.setNoGravity(true);player.setYRot(0);
        for(int x=5;x<=11;x++)for(int y=5;y<=8;y++)for(int z=6;z<=14;z++)test.setBlock(new BlockPos(x,y,z),Blocks.AIR);
        var mob=EntityType.ZOMBIE.create(test.getLevel());mob.setNoAi(true);mob.setNoGravity(true);mob.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD,new ItemStack(net.minecraft.world.item.Items.CARVED_PUMPKIN));
        mob.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ARMOR).setBaseValue(0);mob.setPos(origin.add(0,0,5));test.getLevel().addFreshEntity(mob);
        var axe=ModItems.ITEMS.getEntries().stream().map(e->e.get()).filter(i->i instanceof InanisiumWeapon w && w.isAxe()).findFirst().orElseThrow();
        var stack=Augmentations.apply(new ItemStack(axe),AdditionalAbilities.GRAVITY_WELL);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
        GravityWellEvents.activate(player,stack);
        test.assertTrue(mob.getHealth()==20,"Initial pull damages target");
        boolean[] arrived={false};
        // Drive the inert fixture through vanilla collision movement and the player's ability tick.
        for(int tick=1;tick<60;tick++)test.runAfterDelay(tick,()->{
            mob.move(net.minecraft.world.entity.MoverType.SELF,mob.getDeltaMovement());
            GravityWellEvents.tick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(player));AxeAnimationTestDriver.finish(player);
            if(!arrived[0] && mob.getHealth()<20) {
                test.assertTrue(mob.position().distanceToSqr(origin.add(0,0,2))<=.16,"Smash damaged before arrival");arrived[0]=true;
            }
        });
        test.runAfterDelay(15,()->{
            test.assertTrue(arrived[0] && mob.getHealth()<20 && !GravityWellEvents.active(player),"Physics arrival does not smash and finish");
            mob.setHealth(20);mob.setPos(origin.add(0,0,5));mob.setDeltaMovement(Vec3.ZERO);GravityWellEvents.activate(player,stack);
            // An obstacle inserted after acquisition must stop the physics pull, without a remote smash.
            for(int x=5;x<=11;x++)for(int y=5;y<=8;y++)test.setBlock(new BlockPos(x,y,11),Blocks.STONE);
        });
        test.runAfterDelay(60,()->{try {
            test.assertTrue(mob.getHealth()==20 && !GravityWellEvents.active(player),"Blocked pull damages remotely or stays active");test.succeed();
        } finally {GravityWellEvents.cancel(player.getUUID());mob.discard();player.server.getPlayerList().remove(player);}});
    }
}
