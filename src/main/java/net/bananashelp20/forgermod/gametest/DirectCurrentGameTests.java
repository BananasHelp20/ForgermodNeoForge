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
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class DirectCurrentGameTests {
    private static Zombie zombie(GameTestHelper test,Vec3 position) {
        var mob=EntityType.ZOMBIE.create(test.getLevel());mob.setNoGravity(true);mob.getAttribute(Attributes.ARMOR).setBaseValue(0);mob.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD,new ItemStack(Blocks.CARVED_PUMPKIN));mob.setPos(position);test.getLevel().addFreshEntity(mob);return mob;
    }
    @GameTest(template="riftfang_test",timeoutTicks=60)
    public static void standingBodyContactRepeatsEveryHalfSecondAndWallsStopIt(GameTestHelper test) {
        var player=TestPlayers.survival(test);var origin=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8)));player.setPos(origin);
        for(int x=7;x<=11;x++) for(int y=5;y<=12;y++) for(int z=8;z<=17;z++) test.setBlock(new BlockPos(x,y,z),Blocks.AIR);
        var a=zombie(test,origin.add(0,0,3));var b=zombie(test,origin.add(0,0,8));
        var standing=zombie(test,origin.add(.4,0,5));var outside=zombie(test,origin.add(1.2,0,5));var above=zombie(test,origin.add(0,4,5));
        var mobs=java.util.List.of(a,b,standing,outside,above);mobs.forEach(m->m.setNoAi(true));
        var stack=Augmentations.apply(new ItemStack(ModItems.STATIC_DAGGER.get()),AdditionalAbilities.DIRECT_CURRENT);
        player.setItemInHand(InteractionHand.MAIN_HAND,stack);
        test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.DIRECT_CURRENT),"Stationary circuit rejected");
        a.hurt(player.damageSources().playerAttack(player),1);b.hurt(player.damageSources().playerAttack(player),1);
        DirectCurrentEvents.tick(new PlayerTickEvent.Post(player));
        test.assertTrue(standing.getHealth()==16 && outside.getHealth()==20 && above.getHealth()==20,"Stationary body intersection, width or height is wrong");
        test.assertTrue(a.getHealth()==19 && b.getHealth()==19,"Stationary endpoint anchors take contact damage");
        DirectCurrentEvents.tick(new PlayerTickEvent.Post(player));test.assertTrue(standing.getHealth()==16,"Same-tick contact bypasses damage guard");
        test.runAfterDelay(9,()->{DirectCurrentEvents.tick(new PlayerTickEvent.Post(player));test.assertTrue(standing.getHealth()==16,"Contact repeats before half a second");});
        test.runAfterDelay(10,()->{DirectCurrentEvents.tick(new PlayerTickEvent.Post(player));test.assertTrue(standing.getHealth()==12,"Standing in current does not repeat damage after half a second");});
        test.runAfterDelay(20,()->{
            try {
                test.setBlock(new BlockPos(8,6,13),Blocks.STONE);DirectCurrentEvents.tick(new PlayerTickEvent.Post(player));
                test.assertTrue(standing.getHealth()==12,"Stationary current damages through an obstructing wall");
                DirectCurrentEvents.cancel(player.getUUID());test.setBlock(new BlockPos(8,6,13),Blocks.AIR);
                DirectCurrentEvents.tick(new PlayerTickEvent.Post(player));test.assertTrue(standing.getHealth()==12,"Canceled current still deals contact damage");test.succeed();
            } finally {DirectCurrentEvents.cancel(player.getUUID());mobs.forEach(net.minecraft.world.entity.Entity::discard);player.server.getPlayerList().remove(player);}
        });
    }
    @GameTest(template="riftfang_test")
    public static void allVariantsCreateTwoAnchorBarrierAndPreserveCrossingDamage(GameTestHelper test) {
        var player=TestPlayers.survival(test);var origin=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8)));player.setPos(origin);
        for(int x=5;x<=11;x++) for(int y=5;y<=8;y++) for(int z=8;z<=25;z++) test.setBlock(new BlockPos(x,y,z),Blocks.AIR);
        var a=zombie(test,origin.add(0,0,3));var b=zombie(test,origin.add(0,0,8));var c=zombie(test,origin.add(0,0,10));var crossing=zombie(test,origin.add(-1,0,5));int count=0;
        try {
            for(var entry:ModItems.ITEMS.getEntries()) {
                var fresh=new ItemStack(entry.get());boolean eligible=entry.get() instanceof ElectriumWeapon;
                test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(ability->ability.id().equals(AdditionalAbilities.DIRECT_CURRENT))==eligible,"Wrong Direct Current assignment");
                if(!eligible) continue;count++;for(var mob:java.util.List.of(a,b,c,crossing)) {mob.setHealth(20);mob.invulnerableTime=0;mob.setNoAi(false);}crossing.setPos(origin.add(-3,0,5));
                player.setItemInHand(InteractionHand.MAIN_HAND,fresh);test.assertFalse(RecommendedAbilityRuntime.activate(player,fresh,AdditionalAbilities.DIRECT_CURRENT),"Fresh weapon marks");
                var stack=Augmentations.apply(fresh,AdditionalAbilities.DIRECT_CURRENT);player.setItemInHand(InteractionHand.MAIN_HAND,stack);test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.DIRECT_CURRENT),"Learned circuit rejected");
                a.hurt(player.damageSources().playerAttack(player),1);c.setPos(origin.add(0,0,60));c.hurt(player.damageSources().playerAttack(player),1);test.assertTrue(DirectCurrentEvents.marked(player)==1,"A distant mark beyond 50 blocks is accepted");c.setPos(origin.add(0,0,10));c.invulnerableTime=0;test.assertFalse(WeaponControlLocks.frozen(a),"A single mark starts barrier stun");b.hurt(player.damageSources().playerAttack(player),1);
                test.assertTrue(DirectCurrentEvents.marked(player)==2 && DirectCurrentEvents.remainingTicks(player)==400 && WeaponControlLocks.frozen(a) && WeaponControlLocks.frozen(b),"Two anchors do not start 20-second stun");
                c.hurt(player.damageSources().playerAttack(player),1);test.assertTrue(DirectCurrentEvents.marked(player)==2 && !WeaponControlLocks.frozen(c),"Rank I exceeds two marks");
                crossing.setPos(origin.add(3,0,5));DirectCurrentEvents.tick(new PlayerTickEvent.Post(player));test.assertTrue(crossing.getHealth()==16,"Crossing does not deal two hearts");DirectCurrentEvents.tick(new PlayerTickEvent.Post(player));test.assertTrue(crossing.getHealth()==16,"Standing near barrier repeatedly deals damage");
                player.setItemInHand(InteractionHand.MAIN_HAND,stack.copy());test.assertFalse(DirectCurrentEvents.active(player),"Switch retains circuit");test.assertFalse(WeaponControlLocks.frozen(a) || WeaponControlLocks.frozen(b) || a.isNoAi() || b.isNoAi(),"Cancel leaves anchors frozen");
            }
        } finally {DirectCurrentEvents.cancel(player.getUUID());for(var mob:java.util.List.of(a,b,c,crossing)) mob.discard();player.server.getPlayerList().remove(player);}
        test.assertTrue(count==15,"Skipped Direct Current variants");test.succeed();
    }
    @GameTest(template="riftfang_test",timeoutTicks=100)
    public static void staticFieldHurtsEveryOccupantOncePerSecondAndSupportsFourAnchors(GameTestHelper test) {
        var player=TestPlayers.survival(test);player.setNoGravity(true);var origin=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8)));player.setPos(origin.add(1,0,2.5));
        for(int x=7;x<=16;x++) for(int y=5;y<=8;y++) for(int z=7;z<=16;z++) test.setBlock(new BlockPos(x,y,z),Blocks.AIR);
        var a=zombie(test,origin);var b=zombie(test,origin.add(6,0,0));var c=zombie(test,origin.add(0,0,6));var d=zombie(test,origin.add(6,0,6));var neutral=EntityType.COW.create(test.getLevel());neutral.setNoAi(true);neutral.setNoGravity(true);neutral.setPos(origin.add(2.5,0,1));test.getLevel().addFreshEntity(neutral);
        ItemStack stack=new ItemStack(ModItems.CLAYMORE_OF_THUNDER.get());for(int i=0;i<4;i++) stack=Augmentations.apply(stack,AdditionalAbilities.DIRECT_CURRENT);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
        var name=Augmentations.abilityName(stack,AdditionalAbilities.DIRECT_CURRENT,4);test.assertTrue(name.getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents text && text.getKey().equals("tooltips.forgermod.ability.static_field.name"),"Rank IV is not named Static Field");
        test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.DIRECT_CURRENT),"Static Field rejected");for(var mob:java.util.List.of(a,b,c,d)) mob.hurt(player.damageSources().playerAttack(player),1);
        test.assertTrue(DirectCurrentEvents.marked(player)==4 && DirectCurrentEvents.remainingTicks(player)==400,"Rank IV does not support four marks");float health=neutral.getHealth();
        test.runAfterDelay(20,()->{
            try {DirectCurrentEvents.tick(new PlayerTickEvent.Post(player));test.assertTrue(neutral.getHealth()==health-1 && player.getHealth()==19,"Field damage wrong: neutral="+neutral.getHealth()+" expected="+(health-1)+" player="+player.getHealth()+" active="+DirectCurrentEvents.active(player)+" remaining="+DirectCurrentEvents.remainingTicks(player)+" playerPos="+player.position());DirectCurrentEvents.tick(new PlayerTickEvent.Post(player));test.assertTrue(neutral.getHealth()==health-1,"Field damages more than once per second");test.succeed();}
            finally {DirectCurrentEvents.cancel(player.getUUID());for(var mob:java.util.List.of(a,b,c,d)) mob.discard();neutral.discard();player.server.getPlayerList().remove(player);}
        });
    }
    @GameTest(template="riftfang_test",timeoutTicks=100)
    public static void superStaticFieldIVDealsUpgradedDamage(GameTestHelper test) {
        var player=TestPlayers.survival(test);player.setNoGravity(true);var origin=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8)));player.setPos(origin.add(1,0,2.5));
        for(int x=7;x<=16;x++) for(int y=5;y<=8;y++) for(int z=7;z<=16;z++) test.setBlock(new BlockPos(x,y,z),Blocks.AIR);
        var a=zombie(test,origin);var b=zombie(test,origin.add(6,0,0));var c=zombie(test,origin.add(0,0,6));var d=zombie(test,origin.add(6,0,6));var neutral=EntityType.COW.create(test.getLevel());neutral.setNoAi(true);neutral.setNoGravity(true);neutral.setPos(origin.add(2.5,0,1));test.getLevel().addFreshEntity(neutral);
        ItemStack stack=new ItemStack(ModItems.CLAYMORE_OF_THUNDER.get());for(int i=0;i<7;i++) stack=Augmentations.apply(stack,AdditionalAbilities.DIRECT_CURRENT);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
        var name=Augmentations.abilityName(stack,AdditionalAbilities.DIRECT_CURRENT,4);test.assertTrue(name.getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents text && text.getKey().equals("tooltips.forgermod.ability.static_field.name"),"Rank IV is not named Static Field");
        test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.DIRECT_CURRENT),"Static Field rejected");for(var mob:java.util.List.of(a,b,c,d)) mob.hurt(player.damageSources().playerAttack(player),1);
        test.assertTrue(DirectCurrentEvents.marked(player)==4 && DirectCurrentEvents.remainingTicks(player)==400,"Rank IV does not support four marks");float health=neutral.getHealth();
        test.runAfterDelay(20,()->{
            try {DirectCurrentEvents.tick(new PlayerTickEvent.Post(player));test.assertTrue(neutral.getHealth()==health-1.75f && player.getHealth()==18.25f,"Field damage wrong: neutral="+neutral.getHealth()+" expected="+(health-1)+" player="+player.getHealth()+" active="+DirectCurrentEvents.active(player)+" remaining="+DirectCurrentEvents.remainingTicks(player)+" playerPos="+player.position());DirectCurrentEvents.tick(new PlayerTickEvent.Post(player));test.assertTrue(neutral.getHealth()==health-1.75f,"Field damages more than once per second");test.succeed();}
            finally {DirectCurrentEvents.cancel(player.getUUID());for(var mob:java.util.List.of(a,b,c,d)) mob.discard();neutral.discard();player.server.getPlayerList().remove(player);}
        });
    }
    @GameTest(template="riftfang_test")
    public static void geometryRejectsNonCrossingsAndHeightAndDegenerateTriangles(GameTestHelper test) {
        Vec3 a=new Vec3(0,0,0),b=new Vec3(0,0,6),c=new Vec3(6,0,0);
        test.assertTrue(DirectCurrentEvents.crosses(new Vec3(-1,0,3),new Vec3(1,0,3),a,b),"Real crossing rejected");
        test.assertFalse(DirectCurrentEvents.crosses(new Vec3(-1,0,8),new Vec3(1,0,8),a,b) || DirectCurrentEvents.crosses(new Vec3(-1,4,3),new Vec3(1,4,3),a,b),"Barrier extends beyond endpoints or height");
        test.assertTrue(DirectCurrentEvents.inside(new Vec3(1,0,1),a,b,c),"Triangle interior rejected");test.assertFalse(DirectCurrentEvents.inside(new Vec3(5,0,5),a,b,c) || DirectCurrentEvents.inside(new Vec3(0,0,1),a,b,new Vec3(0,0,3)),"Field includes exterior or degenerate triangle");test.succeed();
    }
}
