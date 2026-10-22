package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.TaifuniteWeapon;
import net.bananashelp20.forgermod.item.custom.abilities.RecommendedAbilityRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class DowndraftBellGameTests {
    @GameTest(template="riftfang_test")
    public static void movingRadiusAcquiresNewFlyersAndReleasesLeaversIncludingElytra(GameTestHelper test) throws Exception {
        var owner=TestPlayers.survival(test);var flyer=TestPlayers.survival(test);
        var origin=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,8,8)));
        owner.setPos(origin);owner.setNoGravity(true);flyer.setNoGravity(true);
        var mob=EntityType.BAT.create(test.getLevel());mob.setNoAi(true);mob.setNoGravity(true);
        mob.setPos(origin.add(6,30,0));test.getLevel().addFreshEntity(mob);
        flyer.setPos(origin.add(6,20,0));flyer.startFallFlying();
        var stack=Augmentations.apply(new ItemStack(ModItems.DEAD_CALM_DAGGER.get()),AdditionalAbilities.DOWNDRAFT_BELL);
        owner.setItemInHand(InteractionHand.MAIN_HAND,stack);
        try {
            test.assertTrue(net.bananashelp20.forgermod.item.custom.abilities.DowndraftBellAbility.activate(owner,stack),"Cannot prepare an empty downdraft area for incoming flyers");
            mob.setPos(origin.add(4,30,0));mob.setDeltaMovement(.3,1,.2);
            flyer.setPos(origin.add(3,20,0));flyer.setDeltaMovement(.5,1,.25);
            net.bananashelp20.forgermod.item.custom.abilities.DowndraftBellAbility.tick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(owner));
            test.assertTrue(mob.getDeltaMovement().equals(new Vec3(.3,-2,.2)),"Entering passive flyer is not caught at the radius boundary");
            test.assertTrue(flyer.getDeltaMovement().equals(new Vec3(.5,-2,.25)) && flyer.isFallFlying(),"Elytra flyer missed, horizontal speed changed or flight cancelled");
            flyer.setDeltaMovement(.5,1,.25);
            net.bananashelp20.forgermod.item.custom.abilities.DowndraftBellAbility.tick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(flyer));
            test.assertTrue(flyer.getDeltaMovement().y==-2,"Elytra client movement can override the sustained force");
            flyer.stopFallFlying();flyer.getAbilities().flying=true;flyer.setDeltaMovement(0,1,0);
            net.bananashelp20.forgermod.item.custom.abilities.DowndraftBellAbility.tick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(flyer));
            test.assertTrue(flyer.getDeltaMovement().y==-2 && flyer.getAbilities().flying,"Other player flight is unaffected or its flight mode is removed");
            flyer.getAbilities().flying=false;flyer.setDeltaMovement(0,1,0);
            net.bananashelp20.forgermod.item.custom.abilities.DowndraftBellAbility.tick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(flyer));
            test.assertTrue(flyer.getDeltaMovement().y==1,"Ordinary nonflying player remains trapped");
            flyer.startFallFlying();
            net.bananashelp20.forgermod.item.custom.abilities.DowndraftBellAbility.tick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(owner));
            mob.setPos(origin.add(4.1,30,0));mob.setDeltaMovement(0,1,0);
            net.bananashelp20.forgermod.item.custom.abilities.DowndraftBellAbility.before(new net.neoforged.neoforge.event.tick.EntityTickEvent.Pre(mob));
            flyer.setPos(origin.add(4.1,20,0));flyer.setDeltaMovement(0,1,0);
            net.bananashelp20.forgermod.item.custom.abilities.DowndraftBellAbility.tick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(flyer));
            test.assertTrue(mob.getDeltaMovement().y==1 && flyer.getDeltaMovement().y==1,"Leaving the radius does not release flight immediately");
            owner.setPos(origin.add(3,0,0));
            net.bananashelp20.forgermod.item.custom.abilities.DowndraftBellAbility.tick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(owner));
            test.assertTrue(mob.getDeltaMovement().y==-2 && flyer.getDeltaMovement().y==-2,"Moving area fails to reacquire flying targets");
            net.bananashelp20.forgermod.item.custom.abilities.DowndraftBellAbility.cancel(owner.getUUID());
            mob.setDeltaMovement(0,1,0);flyer.setDeltaMovement(0,1,0);
            net.bananashelp20.forgermod.item.custom.abilities.DowndraftBellAbility.before(new net.neoforged.neoforge.event.tick.EntityTickEvent.Pre(mob));
            net.bananashelp20.forgermod.item.custom.abilities.DowndraftBellAbility.tick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(flyer));
            test.assertTrue(mob.getDeltaMovement().y==1 && flyer.getDeltaMovement().y==1,"Cancellation leaves a force on an acquired flyer");
            test.succeed();
        } finally {
            net.bananashelp20.forgermod.item.custom.abilities.DowndraftBellAbility.cancel(owner.getUUID());mob.discard();
            owner.server.getPlayerList().remove(owner);flyer.server.getPlayerList().remove(flyer);
        }
    }
    @GameTest(template="riftfang_test",timeoutTicks=240)
    public static void sustainedDowndraftGroundsHighFlyingMobsAndExpiresAfterTenSeconds(GameTestHelper test) throws Exception {
        var player=TestPlayers.survival(test);player.setNoGravity(true);var origin=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8)));player.setPos(origin);
        for(int x=3;x<=14;x++) for(int z=3;z<=14;z++) {test.setBlock(new BlockPos(x,4,z),Blocks.STONE);for(int y=5;y<=210;y++)test.setBlock(new BlockPos(x,y,z),Blocks.AIR);}
        var low=EntityType.GHAST.create(test.getLevel());var high=EntityType.GHAST.create(test.getLevel());var outside=EntityType.GHAST.create(test.getLevel());var aboveWorld=EntityType.BAT.create(test.getLevel());
        for(var mob:java.util.List.of(low,high,outside,aboveWorld)) {mob.setNoAi(true);mob.setNoGravity(true);}
        low.setPos(origin.add(-2,100,0));high.setPos(origin.add(2,200,0));outside.setPos(origin.add(8,200,0));aboveWorld.setPos(origin.x,test.getLevel().getMaxBuildHeight()+50,origin.z);
        for(var mob:java.util.List.of(low,high,outside,aboveWorld)) test.getLevel().addFreshEntity(mob);
        var stack=Augmentations.apply(new ItemStack(ModItems.DEAD_CALM_DAGGER.get()),AdditionalAbilities.DOWNDRAFT_BELL);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
        net.bananashelp20.forgermod.gametest.AbilityTestPackets.use(player,0);
        test.assertTrue(net.bananashelp20.forgermod.item.custom.abilities.DowndraftBellAbility.active(player),"Sustained downdraft did not activate");
        test.assertTrue(low.getDeltaMovement().y==-2 && high.getDeltaMovement().y==-2 && aboveWorld.getDeltaMovement().y==-2,"Vertical targeting has a height cap");
        test.assertTrue(outside.getDeltaMovement().equals(Vec3.ZERO),"Horizontal range is no longer bounded");
        var despawn=new net.neoforged.neoforge.event.entity.living.MobDespawnEvent(high,test.getLevel());
        net.bananashelp20.forgermod.item.custom.abilities.DowndraftBellAbility.despawn(despawn);
        test.assertTrue(despawn.getResult()==net.neoforged.neoforge.event.entity.living.MobDespawnEvent.Result.DENY && !high.isPersistenceRequired(),"High target despawns or receives permanent persistence");
        despawn.setResult(net.neoforged.neoforge.event.entity.living.MobDespawnEvent.Result.ALLOW);
        net.bananashelp20.forgermod.item.custom.abilities.DowndraftBellAbility.despawn(despawn);
        test.assertTrue(despawn.getResult()==net.neoforged.neoforge.event.entity.living.MobDespawnEvent.Result.ALLOW,"Explicit external despawn decision is overridden");
        test.runAfterDelay(10,()->test.assertTrue(low.getY()<=origin.y+85 && high.getY()<=origin.y+185,"No-gravity flight not descending: low="+low.getY()+" high="+high.getY()+" start="+origin.y+" ticks="+high.tickCount+" active="+net.bananashelp20.forgermod.item.custom.abilities.DowndraftBellAbility.active(player)));
        test.runAfterDelay(130,()->{
            test.assertTrue(Math.abs(low.getY()-origin.y)<.01 && Math.abs(high.getY()-origin.y)<.01 && low.onGround() && high.onGround(),"High flyers not grounded: low="+low.position()+" high="+high.position()+" groundY="+origin.y+" flags="+low.onGround()+","+high.onGround()+" ticks="+high.tickCount);
            net.bananashelp20.forgermod.item.custom.abilities.DowndraftBellAbility.before(new net.neoforged.neoforge.event.tick.EntityTickEvent.Pre(high));
            high.setDeltaMovement(0,1,0);high.move(net.minecraft.world.entity.MoverType.SELF,new Vec3(0,1,0));
            net.bananashelp20.forgermod.item.custom.abilities.DowndraftBellAbility.after(new net.neoforged.neoforge.event.tick.EntityTickEvent.Post(high));
            test.assertTrue(Math.abs(high.getY()-origin.y)<.01,"Flying AI can override the grounded downdraft");
        });
        test.runAfterDelay(199,()->test.assertTrue(net.bananashelp20.forgermod.item.custom.abilities.DowndraftBellAbility.active(player),"Downdraft ends before ten seconds"));
        test.runAfterDelay(200,()->{
            try {
                test.assertFalse(net.bananashelp20.forgermod.item.custom.abilities.DowndraftBellAbility.active(player),"Downdraft exceeds ten seconds");
                var ended=new net.neoforged.neoforge.event.entity.living.MobDespawnEvent(high,test.getLevel());net.bananashelp20.forgermod.item.custom.abilities.DowndraftBellAbility.despawn(ended);
                test.assertTrue(ended.getResult()==net.neoforged.neoforge.event.entity.living.MobDespawnEvent.Result.DEFAULT && !high.isPersistenceRequired(),"Expired force leaves despawn protection");
                high.setDeltaMovement(0,1,0);net.bananashelp20.forgermod.item.custom.abilities.DowndraftBellAbility.before(new net.neoforged.neoforge.event.tick.EntityTickEvent.Pre(high));
                test.assertTrue(high.getDeltaMovement().y==1,"Expired force still overrides flight");
                net.bananashelp20.forgermod.item.custom.abilities.WeaponAbilityNetwork.onPlayerTick(new net.neoforged.neoforge.event.tick.PlayerTickEvent.Pre(player));
                var key=net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem())+":"+AdditionalAbilities.DOWNDRAFT_BELL;
                test.assertTrue(java.util.Objects.equals(player.getData(net.bananashelp20.forgermod.item.custom.abilities.WeaponCooldownAttachments.COOLDOWNS).get(key),player.level().getGameTime()+600),"Downdraft cooldown does not start after its sustained effect");test.succeed();
            } finally {net.bananashelp20.forgermod.item.custom.abilities.WeaponAbilityNetwork.onLogout(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(player));for(var mob:java.util.List.of(low,high,outside,aboveWorld))mob.discard();player.server.getPlayerList().remove(player);}
        });
    }
    @GameTest(template="riftfang_test")
    public static void allVariantsForceHighAirborneMobsIncludingPassiveMobs(GameTestHelper test) {
        var player=TestPlayers.survival(test);var origin=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8)));player.setPos(origin);
        for(int x=5;x<=12;x++) for(int y=5;y<=12;y++) for(int z=5;z<=14;z++) test.setBlock(new BlockPos(x,y,z),Blocks.AIR);
        var air=EntityType.ZOMBIE.create(test.getLevel());var grounded=EntityType.ZOMBIE.create(test.getLevel());var neutral=EntityType.COW.create(test.getLevel());
        for(var mob:java.util.List.of(air,grounded)) {mob.setNoAi(true);mob.setNoGravity(true);test.getLevel().addFreshEntity(mob);}neutral.setNoAi(true);neutral.setNoGravity(true);test.getLevel().addFreshEntity(neutral);
        air.setPos(origin.add(0,2,2));grounded.setPos(origin.add(1,0,2));grounded.setOnGround(true);neutral.setPos(origin.add(1,2,1));neutral.setOnGround(false);var neutralMotion=new Vec3(.1,.7,.2);neutral.setDeltaMovement(neutralMotion);int count=0;
        try {
            for(var entry:ModItems.ITEMS.getEntries()) {
                var fresh=new ItemStack(entry.get());boolean eligible=entry.get() instanceof TaifuniteWeapon;
                test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(a->a.id().equals(AdditionalAbilities.DOWNDRAFT_BELL))==eligible,"Wrong Downdraft Bell assignment");
                if(!eligible) continue;count++;air.setPos(origin.add(0,count%2==0?200:100,2));air.setOnGround(false);air.setDeltaMovement(.4,.7,.2);air.fallDistance=7;var position=air.position();var groundMotion=new Vec3(.1,0,.2);grounded.setDeltaMovement(groundMotion);player.setItemInHand(InteractionHand.MAIN_HAND,fresh);
                test.assertFalse(RecommendedAbilityRuntime.activate(player,fresh,AdditionalAbilities.DOWNDRAFT_BELL),"Fresh weapon forces descent");
                var stack=Augmentations.apply(fresh,AdditionalAbilities.DOWNDRAFT_BELL);player.setItemInHand(InteractionHand.MAIN_HAND,stack);test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.DOWNDRAFT_BELL),"Learned gust rejected");
                test.assertTrue(air.getDeltaMovement().distanceTo(new Vec3(.4,-2,.2))<.0001 && air.position().equals(position) && air.fallDistance==7 && air.getHealth()==20,"Gust teleports, damages or alters fall distance/horizontal speed");
                test.assertTrue(grounded.getDeltaMovement().equals(groundMotion) && neutral.getDeltaMovement().distanceTo(new Vec3(.1,-2,.2))<.0001,"Grounded target affected or passive airborne mob missed");
                air.setDeltaMovement(.4,-3,.2);net.bananashelp20.forgermod.item.custom.abilities.DowndraftBellAbility.before(new net.neoforged.neoforge.event.tick.EntityTickEvent.Pre(air));test.assertTrue(air.getDeltaMovement().y==-3,"Gust slows an already faster descent");
                player.setItemInHand(InteractionHand.MAIN_HAND,stack.copy());test.assertFalse(net.bananashelp20.forgermod.item.custom.abilities.DowndraftBellAbility.active(player),"Switch retains sustained downdraft");
            }
        } finally {net.bananashelp20.forgermod.item.custom.abilities.DowndraftBellAbility.cancel(player.getUUID());air.discard();grounded.discard();neutral.discard();player.server.getPlayerList().remove(player);}
        var motion=new Vec3(.32,.7,.24);test.assertTrue(net.bananashelp20.forgermod.item.custom.abilities.VerticalMotionNetwork.adjusted(motion,-.8,true).equals(new Vec3(.32,-.8,.24)),"Client correction overwrites sprint momentum");
        test.assertTrue(net.bananashelp20.forgermod.item.custom.abilities.VerticalMotionNetwork.adjusted(new Vec3(.32,-2,.24),-.8,true).y==-2,"Client correction slows an already faster fall");
        test.assertTrue(count==15,"Skipped Downdraft Bell variants");test.succeed();
    }
}
