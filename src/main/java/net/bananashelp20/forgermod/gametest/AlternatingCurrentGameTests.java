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
    private static void aim(net.minecraft.server.level.ServerPlayer player,net.minecraft.world.entity.LivingEntity target){
        var delta=target.getBoundingBox().getCenter().subtract(player.getEyePosition());player.setYRot((float)Math.toDegrees(Math.atan2(-delta.x,delta.z)));player.setXRot((float)-Math.toDegrees(Math.atan2(delta.y,Math.sqrt(delta.x*delta.x+delta.z*delta.z))));
    }
    private static void clear(GameTestHelper test,net.minecraft.server.level.ServerPlayer player){
        player.setNoGravity(true);player.setPos(Vec3.atBottomCenterOf(BlockPos.containing(test.getBounds().getCenter())));
        for(int x=-2;x<=5;x++)for(int y=0;y<=4;y++)for(int z=0;z<=12;z++)test.getLevel().setBlockAndUpdate(player.blockPosition().offset(x,y,z),Blocks.AIR.defaultBlockState());
    }
    @GameTest(template="riftfang_test")
    public static void everyVariantManuallyLinksAlliesAndDamagesAndStunsBoth(GameTestHelper test)throws Exception{
        var player=TestPlayers.survival(test);clear(test,player);var origin=player.position();
        var cow=EntityType.COW.create(test.getLevel());var wolf=EntityType.WOLF.create(test.getLevel());wolf.tame(player);
        for(var mob:java.util.List.of(cow,wolf)){mob.setNoGravity(true);mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);mob.getAttribute(Attributes.ARMOR).setBaseValue(0);test.getLevel().addFreshEntity(mob);}
        var team=player.getScoreboard().addPlayerTeam("ac_"+player.getUUID().toString().substring(0,8));player.getScoreboard().addPlayerToTeam(player.getScoreboardName(),team);player.getScoreboard().addPlayerToTeam(cow.getScoreboardName(),team);int checked=0;
        try{for(var entry:ModItems.ITEMS.getEntries())if(entry.get() instanceof ElectriumWeapon){
            checked++;for(boolean reverse:new boolean[]{false,true}){
                var first=reverse?wolf:cow;var second=reverse?cow:wolf;first.setPos(origin.add(0,0,4));second.setPos(origin.add(3,0,8));
                for(var mob:java.util.List.of(first,second)){mob.setHealth(100);mob.invulnerableTime=0;mob.setNoAi(false);}
                var stack=Augmentations.apply(new ItemStack(entry.get()),AdditionalAbilities.ALTERNATING_CURRENT);player.setItemInHand(InteractionHand.MAIN_HAND,stack);WeaponAbilityNetwork.onPlayerTick(new PlayerTickEvent.Pre(player));player.setData(WeaponCooldownAttachments.COOLDOWNS,java.util.Map.of());
                aim(player,first);AbilityTestPackets.use(player,0);test.assertTrue(AlternatingCurrentEvents.active(player) && !WeaponControlLocks.frozen(first) && !WeaponControlLocks.frozen(second),"First mark automatically selected/stunned another mob");
                aim(player,second);AbilityTestPackets.use(player,0);
                test.assertTrue(WeaponControlLocks.frozen(first) && WeaponControlLocks.frozen(second) && first.getHealth()==98 && second.getHealth()==98,"Current does not deal 2 HP and stun both allies");
                test.assertTrue(player.level().getEntitiesOfClass(net.minecraft.world.entity.LightningBolt.class,player.getBoundingBox().inflate(24)).isEmpty(),"Exchange spawns real lightning");
                test.assertTrue(AlternatingCurrentEvents.remainingTicks(player)==1200,"Link lifetime did not start when second target was selected");
                AlternatingCurrentEvents.cancel(player.getUUID());test.assertTrue(!WeaponControlLocks.frozen(first) && !WeaponControlLocks.frozen(second) && !first.isNoAi() && !second.isNoAi(),"Cancel leaks stun/AI locks");WeaponAbilityNetwork.onPlayerTick(new PlayerTickEvent.Pre(player));
            }
        }test.assertTrue(checked==15,"Skipped Electrium variants");}finally{AlternatingCurrentEvents.cancel(player.getUUID());player.getScoreboard().removePlayerTeam(team);cow.discard();wolf.discard();player.server.getPlayerList().remove(player);}test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void meleeNoLongerChoosesSecondTargetAndInvalidMarksPreserveFirst(GameTestHelper test){
        var player=TestPlayers.survival(test);clear(test,player);var origin=player.position();var first=EntityType.ZOMBIE.create(test.getLevel());var second=EntityType.ZOMBIE.create(test.getLevel());
        first.setPos(origin.add(0,0,4));second.setPos(origin.add(3,0,8));for(var mob:java.util.List.of(first,second)){mob.setNoAi(true);mob.setNoGravity(true);test.getLevel().addFreshEntity(mob);}
        var stack=Augmentations.apply(new ItemStack(ModItems.STATIC_DAGGER.get()),AdditionalAbilities.ALTERNATING_CURRENT);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
        try{
            aim(player,first);test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.ALTERNATING_CURRENT),"Cannot mark first");first.hurt(player.damageSources().playerAttack(player),1);
            test.assertFalse(WeaponControlLocks.frozen(second),"Melee automatically links a second enemy");
            test.assertFalse(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.ALTERNATING_CURRENT),"Same mob accepted twice");
            second.setPos(origin.add(25,0,0));aim(player,second);test.assertFalse(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.ALTERNATING_CURRENT),"Out of range mark accepted");
            second.setPos(origin.add(3,0,8));var wall=player.blockPosition().offset(2,1,6);test.getLevel().setBlockAndUpdate(wall,Blocks.STONE.defaultBlockState());aim(player,second);
            test.assertFalse(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.ALTERNATING_CURRENT),"Second mark ray pierces solid wall");test.getLevel().setBlockAndUpdate(wall,Blocks.AIR.defaultBlockState());
            test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.ALTERNATING_CURRENT),"Invalid marks destroyed first selection");
        }finally{AlternatingCurrentEvents.cancel(player.getUUID());first.discard();second.discard();player.server.getPlayerList().remove(player);}test.succeed();
    }
    @GameTest(template="riftfang_test",timeoutTicks=70)
    public static void eachPulseStunsBothForHalfASecondAndDealsTwoHp(GameTestHelper test){
        var player=TestPlayers.survival(test);clear(test,player);var origin=player.position();var first=EntityType.ZOMBIE.create(test.getLevel());var second=EntityType.ZOMBIE.create(test.getLevel());
        first.setPos(origin.add(0,0,4));second.setPos(origin.add(3,0,8));for(var mob:java.util.List.of(first,second)){mob.setNoGravity(true);mob.getAttribute(Attributes.ARMOR).setBaseValue(0);mob.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD,new ItemStack(Blocks.CARVED_PUMPKIN));test.getLevel().addFreshEntity(mob);}
        var stack=Augmentations.apply(new ItemStack(ModItems.STATIC_DAGGER.get()),AdditionalAbilities.ALTERNATING_CURRENT);player.setItemInHand(InteractionHand.MAIN_HAND,stack);aim(player,first);RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.ALTERNATING_CURRENT);aim(player,second);RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.ALTERNATING_CURRENT);
        test.runAfterDelay(9,()->test.assertTrue(WeaponControlLocks.frozen(first) && WeaponControlLocks.frozen(second),"Stuns end before half a second"));
        test.runAfterDelay(10,()->test.assertTrue(!WeaponControlLocks.frozen(first) && !WeaponControlLocks.frozen(second),"Stun exceeds half a second"));
        test.runAfterDelay(20,()->{AlternatingCurrentEvents.tick(new PlayerTickEvent.Post(player));test.assertTrue(WeaponControlLocks.frozen(first) && WeaponControlLocks.frozen(second) && first.getHealth()==16 && second.getHealth()==16,"Second pulse does not stun and damage both");});
        test.runAfterDelay(40,()->{try{AlternatingCurrentEvents.tick(new PlayerTickEvent.Post(player));test.assertTrue(first.getHealth()==14 && second.getHealth()==14,"Third pulse damage differs from 2 HP");test.succeed();}finally{AlternatingCurrentEvents.cancel(player.getUUID());first.discard();second.discard();player.server.getPlayerList().remove(player);}});
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
