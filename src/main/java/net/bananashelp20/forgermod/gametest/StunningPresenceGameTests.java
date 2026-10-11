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
public class StunningPresenceGameTests {
    @GameTest(template="riftfang_test")
    public static void fiveClaymoresPulseOnlyVisibleEnemiesAndCancelStun(GameTestHelper test) {
        var player=TestPlayers.survival(test);var origin=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8)));player.setPos(origin);
        for(int x=5;x<=18;x++) for(int y=5;y<=8;y++) for(int z=5;z<=12;z++) test.setBlock(new BlockPos(x,y,z),Blocks.AIR);
        var near=EntityType.ZOMBIE.create(test.getLevel());var far=EntityType.ZOMBIE.create(test.getLevel());var neutral=EntityType.COW.create(test.getLevel());
        for(var mob:java.util.List.of(near,far)) {mob.setNoGravity(true);mob.getAttribute(Attributes.ARMOR).setBaseValue(0);test.getLevel().addFreshEntity(mob);}near.setPos(origin.add(0,0,3));far.setPos(origin.add(9,0,0));neutral.setNoAi(true);neutral.setNoGravity(true);neutral.setPos(origin.add(0,0,2));test.getLevel().addFreshEntity(neutral);float neutralHealth=neutral.getHealth();int count=0;
        try {
            for(var entry:ModItems.ITEMS.getEntries()) {
                var fresh=new ItemStack(entry.get());boolean eligible=entry.get() instanceof ElectriumWeapon weapon && !weapon.isDagger() && !weapon.isAxe();
                test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(a->a.id().equals(AdditionalAbilities.STUNNING_PRESENCE))==eligible,"Wrong Stunning Presence assignment");
                if(!eligible) continue;count++;near.setHealth(20);far.setHealth(20);player.setItemInHand(InteractionHand.MAIN_HAND,fresh);test.assertFalse(RecommendedAbilityRuntime.activate(player,fresh,AdditionalAbilities.STUNNING_PRESENCE),"Fresh claymore pulses");
                var stack=Augmentations.apply(fresh,AdditionalAbilities.STUNNING_PRESENCE);player.setItemInHand(InteractionHand.MAIN_HAND,stack);test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.STUNNING_PRESENCE),"Learned aura rejected");
                test.assertTrue(near.getHealth()==19 && WeaponControlLocks.frozen(near) && far.getHealth()==20 && neutral.getHealth()==neutralHealth,"Pulse has wrong damage, radius or target filter");
                player.invulnerableTime=0;test.assertFalse(player.hurt(player.damageSources().mobAttack(near),2),"Frozen mob can still inflict a melee attack");
                test.assertTrue(StunningPresenceEvents.remainingTicks(player)==600,"Aura does not last 30 seconds");StunningPresenceEvents.tick(new PlayerTickEvent.Post(player));test.assertTrue(near.getHealth()==19,"Aura pulses more than once per interval");
                player.setItemInHand(InteractionHand.MAIN_HAND,stack.copy());test.assertFalse(StunningPresenceEvents.active(player),"Switch retains aura");test.assertFalse(WeaponControlLocks.frozen(near),"Switch leaves stun");
                for(int i=1;i<4;i++) stack=Augmentations.apply(stack,AdditionalAbilities.STUNNING_PRESENCE);player.setItemInHand(InteractionHand.MAIN_HAND,stack);near.setHealth(20);far.setHealth(20);test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.STUNNING_PRESENCE),"Rank IV aura rejected");test.assertTrue(far.getHealth()==19,"Rank IV does not reach ten blocks");StunningPresenceEvents.cancel(player.getUUID());
                test.setBlock(new BlockPos(8,6,10),Blocks.STONE);near.setHealth(20);player.setItemInHand(InteractionHand.MAIN_HAND,stack);RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.STUNNING_PRESENCE);test.assertTrue(near.getHealth()==20,"Aura attacks through wall");StunningPresenceEvents.cancel(player.getUUID());test.setBlock(new BlockPos(8,6,10),Blocks.AIR);
            }
        } finally {StunningPresenceEvents.cancel(player.getUUID());near.discard();far.discard();neutral.discard();player.server.getPlayerList().remove(player);}
        test.assertTrue(count==5,"Skipped Stunning Presence variants");test.succeed();
    }
    @GameTest(template="riftfang_test",timeoutTicks=100)
    public static void firstRankRepeatsEveryTwoSecondsAndStunLastsSixTicks(GameTestHelper test) {
        var player=TestPlayers.survival(test);var origin=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8)));player.setPos(origin);
        for(int y=5;y<=8;y++) for(int z=8;z<=14;z++) test.setBlock(new BlockPos(8,y,z),Blocks.AIR);
        var mob=EntityType.ZOMBIE.create(test.getLevel());mob.setNoAi(true);mob.setNoGravity(true);mob.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD,new ItemStack(Blocks.CARVED_PUMPKIN));mob.getAttribute(Attributes.ARMOR).setBaseValue(0);mob.setPos(origin.add(0,0,3));test.getLevel().addFreshEntity(mob);
        var stack=Augmentations.apply(new ItemStack(ModItems.CLAYMORE_OF_THUNDER.get()),AdditionalAbilities.STUNNING_PRESENCE);player.setItemInHand(InteractionHand.MAIN_HAND,stack);test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.STUNNING_PRESENCE),"Timed aura rejected");
        test.runAfterDelay(6,()->test.assertFalse(WeaponControlLocks.frozen(mob),"Stun lasts longer than 0.3 seconds"));
        test.runAfterDelay(20,()->{StunningPresenceEvents.tick(new PlayerTickEvent.Post(player));test.assertTrue(mob.getHealth()==19,"Rank I pulses before two seconds");});
        test.runAfterDelay(40,()->{try {StunningPresenceEvents.tick(new PlayerTickEvent.Post(player));test.assertTrue(mob.getHealth()==18 && WeaponControlLocks.frozen(mob),"Two-second pulse missing or wrong");test.succeed();}finally {StunningPresenceEvents.cancel(player.getUUID());mob.discard();player.server.getPlayerList().remove(player);}});
    }
}
