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
