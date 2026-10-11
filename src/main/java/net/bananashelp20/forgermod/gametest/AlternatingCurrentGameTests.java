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
public class AlternatingCurrentGameTests {
    @GameTest(template="riftfang_test")
    public static void allWeaponsLinkNearestEnemyWithoutAddedDamage(GameTestHelper test) {
        var player=TestPlayers.survival(test);var origin=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8)));player.setPos(origin);
        for(int y=5;y<=8;y++) for(int z=8;z<=40;z++) test.setBlock(new BlockPos(8,y,z),Blocks.AIR);
        var first=EntityType.ZOMBIE.create(test.getLevel());var second=EntityType.ZOMBIE.create(test.getLevel());
        for(var mob:java.util.List.of(first,second)) {mob.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD,new ItemStack(Blocks.CARVED_PUMPKIN));mob.setNoGravity(true);mob.getAttribute(Attributes.ARMOR).setBaseValue(0);test.getLevel().addFreshEntity(mob);}int count=0;
        try {
            for(var entry:ModItems.ITEMS.getEntries()) {
                var fresh=new ItemStack(entry.get());boolean eligible=entry.get() instanceof ElectriumWeapon;
                test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(a->a.id().equals(AdditionalAbilities.ALTERNATING_CURRENT))==eligible,"Wrong Alternating Current assignment");
                if(!eligible) continue;count++;first.setPos(origin.add(0,0,5));second.setPos(origin.add(0,0,10));first.setHealth(20);second.setHealth(20);first.invulnerableTime=0;first.setNoAi(false);second.setNoAi(false);
                player.setItemInHand(InteractionHand.MAIN_HAND,fresh);test.assertFalse(RecommendedAbilityRuntime.activate(player,fresh,AdditionalAbilities.ALTERNATING_CURRENT),"Fresh weapon links enemies");
                var stack=Augmentations.apply(fresh,AdditionalAbilities.ALTERNATING_CURRENT);player.setItemInHand(InteractionHand.MAIN_HAND,stack);test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.ALTERNATING_CURRENT),"Learned link rejected");
                first.hurt(player.damageSources().playerAttack(player),1);test.assertTrue(WeaponControlLocks.frozen(second) && !WeaponControlLocks.frozen(first),"First pulse does not stun second enemy");
                test.assertTrue(first.getHealth()==19 && second.getHealth()==20,"Stun adds damage");test.assertTrue(AlternatingCurrentEvents.remainingTicks(player)==1200,"Link does not last one minute");
                player.setItemInHand(InteractionHand.MAIN_HAND,stack.copy());test.assertFalse(AlternatingCurrentEvents.active(player),"Switch retains link");test.assertFalse(WeaponControlLocks.frozen(second) || second.isNoAi(),"Cancellation leaves stale stun AI");
            }
        } finally {AlternatingCurrentEvents.cancel(player.getUUID());first.discard();second.discard();player.server.getPlayerList().remove(player);}
        test.assertTrue(count==15,"Skipped Alternating Current variants");test.succeed();
    }
    @GameTest(template="riftfang_test",timeoutTicks=100)
    public static void pulsesAlternateAtOneSecondIntervalsAndExpireAfterTwoTicks(GameTestHelper test) {
        var player=TestPlayers.survival(test);var origin=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8)));player.setPos(origin);
        for(int y=5;y<=8;y++) for(int z=8;z<=24;z++) test.setBlock(new BlockPos(8,y,z),Blocks.AIR);
        var first=EntityType.ZOMBIE.create(test.getLevel());var second=EntityType.ZOMBIE.create(test.getLevel());
        for(var mob:java.util.List.of(first,second)) {mob.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD,new ItemStack(Blocks.CARVED_PUMPKIN));mob.setNoGravity(true);mob.getAttribute(Attributes.ARMOR).setBaseValue(0);test.getLevel().addFreshEntity(mob);}
        first.setPos(origin.add(0,0,5));second.setPos(origin.add(0,0,10));var stack=Augmentations.apply(new ItemStack(ModItems.STATIC_DAGGER.get()),AdditionalAbilities.ALTERNATING_CURRENT);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
        test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.ALTERNATING_CURRENT),"Timed link rejected");first.hurt(player.damageSources().playerAttack(player),1);
        test.runAfterDelay(2,()->test.assertFalse(WeaponControlLocks.frozen(second),"Stun lasts more than 0.1 seconds"));
        test.runAfterDelay(20,()->{AlternatingCurrentEvents.tick(new PlayerTickEvent.Post(player));test.assertTrue(WeaponControlLocks.frozen(first) && !WeaponControlLocks.frozen(second),"First one-second pulse does not switch to initial enemy");});
        test.runAfterDelay(40,()->{
            try {AlternatingCurrentEvents.tick(new PlayerTickEvent.Post(player));test.assertTrue(WeaponControlLocks.frozen(second) && !WeaponControlLocks.frozen(first),"Second pulse fails to alternate back");test.assertTrue(second.getHealth()==20,"Alternating pulses deal damage");test.succeed();}
            finally {AlternatingCurrentEvents.cancel(player.getUUID());first.discard();second.discard();player.server.getPlayerList().remove(player);}
        });
    }
    @GameTest(template="riftfang_test",timeoutTicks=100)
    public static void overlappingSleepAndStunRestoreAiOnlyAfterLastLease(GameTestHelper test) {
        var player=TestPlayers.survival(test);var origin=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8)));player.setPos(origin);player.setYRot(0);player.setXRot(0);
        for(int y=5;y<=8;y++) for(int z=8;z<=16;z++) test.setBlock(new BlockPos(8,y,z),Blocks.AIR);
        var mob=EntityType.ZOMBIE.create(test.getLevel());mob.setPos(origin.add(0,0,4));test.getLevel().addFreshEntity(mob);var stack=Augmentations.apply(new ItemStack(ModItems.DREAMBOUND_CLAYMORE.get()),AdditionalAbilities.LULLABY);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
        test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.LULLABY),"Overlap sleep rejected");WeaponControlLocks.acquire(mob,player.getUUID(),AdditionalAbilities.ALTERNATING_CURRENT,2);mob.hurt(player.damageSources().playerAttack(player),1);
        test.assertFalse(LullabyEvents.active(player),"Damage does not wake sleep lease");test.assertTrue(WeaponControlLocks.frozen(mob) && mob.isNoAi(),"Waking sleep prematurely restores overlapping stun");
        test.runAfterDelay(2,()->{try {test.assertFalse(WeaponControlLocks.frozen(mob) || mob.isNoAi() || mob.isNoGravity(),"Final stun expiry leaves stale AI/gravity");test.succeed();}finally {WeaponControlLocks.release(player.getUUID(),AdditionalAbilities.ALTERNATING_CURRENT);LullabyEvents.cancel(player.getUUID());mob.discard();player.server.getPlayerList().remove(player);}});
    }
    @GameTest(template="riftfang_test")
    public static void playerStunBlocksNewMeleeAndBladeEchoWithoutChangingFacing(GameTestHelper test) {
        var player=TestPlayers.survival(test);var mob=EntityType.ZOMBIE.create(test.getLevel());player.setYRot(37);player.setXRot(-22);
        var stack=Augmentations.apply(new ItemStack(ModItems.STATIC_DAGGER.get()),AdditionalAbilities.STATIC_BLADE);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
        try {
            test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.STATIC_BLADE),"Player echo arm rejected");
            WeaponControlLocks.acquire(player,player.getUUID(),AdditionalAbilities.ALTERNATING_CURRENT,2);player.attack(mob);
            test.assertTrue(mob.getHealth()==20,"Stunned player can attack");test.assertFalse(StaticBladeEvents.release(player),"Stunned player can release a new echo");
            test.assertTrue(player.getYRot()==37 && player.getXRot()==-22,"Stun forces camera rotation");test.succeed();
        } finally {WeaponControlLocks.release(player.getUUID(),AdditionalAbilities.ALTERNATING_CURRENT);StaticBladeEvents.cancel(player.getUUID());mob.discard();player.server.getPlayerList().remove(player);}
    }
}
