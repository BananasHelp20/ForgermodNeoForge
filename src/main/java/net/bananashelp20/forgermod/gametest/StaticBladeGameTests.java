package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.ElectriumWeapon;
import net.bananashelp20.forgermod.item.custom.abilities.*;
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
public class StaticBladeGameTests {
    @GameTest(template="riftfang_test")
    public static void blockedMainEchoKeepsPairedSwingButSwitchCancelsBothFlights(GameTestHelper test) {
        var player=TestPlayers.survival(test);var origin=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8)));
        player.setNoGravity(true);player.setPos(origin);player.setYRot(0);player.setXRot(0);
        for(int x=5;x<=11;x++) for(int y=5;y<=8;y++) for(int z=7;z<=15;z++) test.setBlock(new BlockPos(x,y,z),z==10?Blocks.STONE:Blocks.AIR);
        var main=Augmentations.apply(Augmentations.apply(new ItemStack(ModItems.STATIC_DAGGER.get()),Augmentations.DUAL_WIELD),AdditionalAbilities.STATIC_BLADE);
        var off=new ItemStack(ModItems.STATIC_DAGGER_RUBY.get());player.setItemInHand(InteractionHand.MAIN_HAND,main);player.setItemInHand(InteractionHand.OFF_HAND,off);
        try {
            test.assertTrue(StaticBladeEvents.activate(player,main) && StaticBladeEvents.release(player),"Paired blocked echo rejected");
            for(int tick=0;tick<4;tick++) StaticBladeEvents.tick(new PlayerTickEvent.Post(player));
            test.assertTrue(StaticBladeEvents.active(player),"Early wall hit cancels pending paired swing");
            test.assertTrue(StaticBladeEvents.releaseOffhand(player),"Wall hit prevents the offhand echo");
            test.assertFalse(StaticBladeEvents.releaseOffhand(player),"Offhand echo can be repeated");
            player.setItemInHand(InteractionHand.MAIN_HAND,main.copy());
            test.assertFalse(StaticBladeEvents.active(player) || StaticBladeEvents.releaseOffhand(player),"Switch preserves paired flight");
            player.setItemInHand(InteractionHand.MAIN_HAND,main);StaticBladeEvents.activate(player,main);StaticBladeEvents.release(player);
            player.setItemInHand(InteractionHand.OFF_HAND,off.copy());
            test.assertFalse(StaticBladeEvents.releaseOffhand(player),"Changing paired stack retains the reserved offhand echo");test.succeed();
        } finally {StaticBladeEvents.cancel(player.getUUID());player.server.getPlayerList().remove(player);}
    }
    @GameTest(template="riftfang_test")
    public static void everyVariantPassesCenterAndOffsetOneBlockHolesButStopsAtSealedWall(GameTestHelper test) {
        var player=TestPlayers.survival(test);var origin=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8)));player.setNoGravity(true);
        for(int x=5;x<=11;x++) for(int y=5;y<=8;y++) for(int z=7;z<=30;z++) test.setBlock(new BlockPos(x,y,z),Blocks.AIR);
        var target=EntityType.ZOMBIE.create(test.getLevel());target.setNoAi(true);target.setNoGravity(true);target.getAttribute(Attributes.ARMOR).setBaseValue(0);target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(50);test.getLevel().addFreshEntity(target);
        int count=0;
        try {
            for(var entry:ModItems.ITEMS.getEntries()) if(entry.get() instanceof ElectriumWeapon weapon && weapon.isDagger()) {
                count++;var stack=Augmentations.apply(new ItemStack(entry.get()),AdditionalAbilities.STATIC_BLADE);player.setItemInHand(InteractionHand.MAIN_HAND,stack);player.doTick();
                for(int mode=0;mode<4;mode++) {
                    for(int x=5;x<=11;x++) for(int y=5;y<=8;y++) test.setBlock(new BlockPos(x,y,14),mode==3?Blocks.AIR:Blocks.STONE);
                    if(mode<2) test.setBlock(new BlockPos(mode==0?8:7,6,14),Blocks.AIR);
                    if(mode==3) test.setBlock(new BlockPos(8,6,14),Blocks.STONE);
                    target.setPos(origin.add(0,0,10));target.setHealth(50);target.invulnerableTime=0;player.setPos(origin);player.setYRot(0);player.setXRot(0);
                    float damage=(float)player.getAttributeValue(Attributes.ATTACK_DAMAGE);
                    test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.STATIC_BLADE) && StaticBladeEvents.release(player),"Gap test could not launch echo");
                    for(int tick=0;tick<25;tick++) StaticBladeEvents.tick(new PlayerTickEvent.Post(player));
                    test.assertTrue(Math.abs(target.getHealth()-(mode==2?50:50-damage))<.001,"Wrong partial/full wall behavior in mode "+mode);
                    test.assertFalse(StaticBladeEvents.active(player),"Gap echo exceeds twenty-block range");
                }
            }
            test.assertTrue(count==5,"Skipped gap-test dagger variants");test.succeed();
        } finally {StaticBladeEvents.cancel(player.getUUID());target.discard();player.server.getPlayerList().remove(player);}
    }
    @GameTest(template="riftfang_test",timeoutTicks=80)
    public static void delayedDualWieldAirSwingEmitsExactlyOneAdditionalEchoFromFreshMixedGemOffhand(GameTestHelper test) throws Exception {
        var method=net.bananashelp20.forgermod.item.custom.attacks.dagger.DualWieldNetwork.class.getDeclaredMethod("handleAttack",
            net.bananashelp20.forgermod.item.custom.attacks.dagger.DualWieldNetwork.OffhandAttackPayload.class,net.neoforged.neoforge.network.handling.IPayloadContext.class);method.setAccessible(true);
        var players=new java.util.ArrayList<net.minecraft.server.level.ServerPlayer>();var targets=new java.util.ArrayList<net.minecraft.world.entity.monster.Zombie>();var damages=new java.util.ArrayList<Float>();
        for(int x=5;x<=36;x++) for(int y=5;y<=8;y++) for(int z=7;z<=32;z++) test.setBlock(new BlockPos(x,y,z),Blocks.AIR);
        int index=0;
        for(var entry:ModItems.ITEMS.getEntries()) if(entry.get() instanceof ElectriumWeapon weapon && weapon.isDagger()) {
            var player=TestPlayers.survival(test);players.add(player);player.setNoGravity(true);
            var origin=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8+6*index++,5,8)));
            var stack=Augmentations.apply(Augmentations.apply(new ItemStack(entry.get()),Augmentations.DUAL_WIELD),AdditionalAbilities.STATIC_BLADE);
            player.setItemInHand(InteractionHand.MAIN_HAND,stack);
            player.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(entry.get()==ModItems.STATIC_DAGGER_RUBY.get()?ModItems.STATIC_DAGGER.get():ModItems.STATIC_DAGGER_RUBY.get()));
            player.doTick();player.setPos(origin);player.setYRot(0);player.setXRot(0);damages.add((float)player.getAttributeValue(Attributes.ATTACK_DAMAGE));
            var target=EntityType.ZOMBIE.create(test.getLevel());target.setNoAi(true);target.setNoGravity(true);target.getAttribute(Attributes.ARMOR).setBaseValue(0);target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);target.setHealth(1000);target.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD,new ItemStack(Blocks.CARVED_PUMPKIN));target.setPos(origin.add(0,0,10));test.getLevel().addFreshEntity(target);targets.add(target);
            test.assertTrue(Augmentations.level(player.getOffhandItem(),AdditionalAbilities.STATIC_BLADE)==0,"Offhand fixture unexpectedly augmented");
            AbilityTestPackets.use(player,0);test.assertTrue(StaticBladeEvents.release(player),"Paired echo did not release");
        }
        test.assertTrue(players.size()==5,"Skipped paired dagger variants");
        for(int tick=1;tick<=26;tick++) {
            final int elapsed=tick;
            test.runAfterDelay(tick,()->{
                try {
                    for(int i=0;i<players.size();i++) {
                        var player=players.get(i);
                        if(elapsed==6 || elapsed==9) {
                            var type=net.neoforged.neoforge.network.handling.IPayloadContext.class;
                            var context=(net.neoforged.neoforge.network.handling.IPayloadContext)java.lang.reflect.Proxy.newProxyInstance(type.getClassLoader(),new Class<?>[]{type},(proxy,m,args)->{if(m.getName().equals("player"))return player;throw new UnsupportedOperationException(m.getName());});
                            method.invoke(null,new net.bananashelp20.forgermod.item.custom.attacks.dagger.DualWieldNetwork.OffhandAttackPayload(-1),context);
                        }
                        StaticBladeEvents.tick(new PlayerTickEvent.Post(player));
                        if(elapsed==26) {
                            test.assertTrue(Math.abs(targets.get(i).getHealth()-(1000-2*damages.get(i)))<.01,"Paired echo missing or duplicated");
                            test.assertFalse(StaticBladeEvents.active(player) || StaticBladeEvents.releaseOffhand(player),"Finished pair retains a third echo");
                        }
                    }
                    if(elapsed==26) test.succeed();
                } catch(Exception e) {throw new RuntimeException(e);}
                finally {if(elapsed==26) for(int i=0;i<players.size();i++) {var player=players.get(i);WeaponAbilityNetwork.onLogout(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(player));targets.get(i).discard();player.server.getPlayerList().remove(player);}}
            });
        }
    }
    @GameTest(template="riftfang_test")
    public static void fiveDaggersPierceWideEchoOnceAndStopAtWall(GameTestHelper test) {
        var player=TestPlayers.survival(test);player.setNoGravity(true);var origin=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8)));
        for(int x=5;x<=11;x++) for(int y=5;y<=8;y++) for(int z=7;z<=30;z++) test.setBlock(new BlockPos(x,y,z),Blocks.AIR);
        for(int x=5;x<=11;x++) for(int y=5;y<=8;y++) test.setBlock(new BlockPos(x,y,18),Blocks.STONE);
        var first=EntityType.ZOMBIE.create(test.getLevel());var second=EntityType.ZOMBIE.create(test.getLevel());var hidden=EntityType.ZOMBIE.create(test.getLevel());var neutral=EntityType.COW.create(test.getLevel());
        for(var mob:java.util.List.of(first,second,hidden)) {mob.setNoAi(true);mob.setNoGravity(true);mob.getAttribute(Attributes.ARMOR).setBaseValue(0);mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(50);test.getLevel().addFreshEntity(mob);}
        float neutralHealth=neutral.getHealth();neutral.setNoAi(true);neutral.setNoGravity(true);test.getLevel().addFreshEntity(neutral);neutral.setPos(origin.add(.3,0,4));int count=0;
        try {
            for(var entry:ModItems.ITEMS.getEntries()) {
                var fresh=new ItemStack(entry.get());boolean eligible=entry.get() instanceof ElectriumWeapon weapon && weapon.isDagger();
                test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(a->a.id().equals(AdditionalAbilities.STATIC_BLADE))==eligible,"Wrong Static Blade assignment");
                if(!eligible) continue;count++;player.setPos(origin);player.setYRot(0);player.setXRot(0);first.setPos(origin.add(1.5,0,3));second.setPos(origin.add(-1.5,0,7));hidden.setPos(origin.add(0,0,14));
                for(var mob:java.util.List.of(first,second,hidden)) {mob.setHealth(50);mob.invulnerableTime=0;}
                player.setItemInHand(InteractionHand.MAIN_HAND,fresh);test.assertFalse(RecommendedAbilityRuntime.activate(player,fresh,AdditionalAbilities.STATIC_BLADE),"Fresh dagger emits echo");
                var stack=Augmentations.apply(fresh,AdditionalAbilities.STATIC_BLADE);player.setItemInHand(InteractionHand.MAIN_HAND,stack);player.doTick();player.setPos(origin);float damage=(float)player.getAttributeValue(Attributes.ATTACK_DAMAGE);
                test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.STATIC_BLADE),"Learned echo rejected");test.assertTrue(StaticBladeEvents.release(player),"Swing fails to emit echo");test.assertFalse(StaticBladeEvents.release(player),"A second swing emits another echo");
                player.setYRot(90);for(int i=0;i<20;i++) StaticBladeEvents.tick(new PlayerTickEvent.Post(player));
                test.assertTrue(Math.abs(first.getHealth()-(50-damage))<.0001 && Math.abs(second.getHealth()-(50-damage))<.0001,"Wide echo fails to pierce both enemies exactly once");
                test.assertTrue(hidden.getHealth()==50 && neutral.getHealth()==neutralHealth,"Echo pierces wall or hurts neutral entity");test.assertFalse(StaticBladeEvents.active(player),"Wall does not finish echo");
                test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.STATIC_BLADE),"Rearm rejected");player.setItemInHand(InteractionHand.MAIN_HAND,stack.copy());test.assertFalse(StaticBladeEvents.release(player),"Switch retains armed echo");
            }
            for(int x=5;x<=11;x++) for(int y=5;y<=8;y++) test.setBlock(new BlockPos(x,y,18),Blocks.AIR);
            hidden.setPos(origin.add(0,0,23));player.setPos(origin);player.setYRot(0);player.setXRot(0);
            var outside=EntityType.ZOMBIE.create(test.getLevel());var above=EntityType.ZOMBIE.create(test.getLevel());
            for(var mob:java.util.List.of(outside,above)) {mob.setNoAi(true);mob.setNoGravity(true);test.getLevel().addFreshEntity(mob);}
            outside.setPos(origin.add(3,0,3));above.setPos(origin.add(0,3,4));
            try {
                var stack=player.getMainHandItem();test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.STATIC_BLADE),"Range test arm rejected");StaticBladeEvents.release(player);
                for(int i=0;i<25;i++) StaticBladeEvents.tick(new PlayerTickEvent.Post(player));
                test.assertTrue(hidden.getHealth()==50 && outside.getHealth()==20 && above.getHealth()==20,"Echo exceeds its range, width or height");test.assertFalse(StaticBladeEvents.active(player),"Echo outlasts its 20-block travel");
            } finally {outside.discard();above.discard();}
        } finally {StaticBladeEvents.cancel(player.getUUID());first.discard();second.discard();hidden.discard();neutral.discard();player.server.getPlayerList().remove(player);}
        test.assertTrue(count==5,"Skipped Static Blade variants");test.succeed();
    }
}
