package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.SomniumWeapon;
import net.bananashelp20.forgermod.item.custom.abilities.*;
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
public class LullabyGameTests {
    @GameTest(template="riftfang_test")
    public static void allVariantsSleepPassiveAlliedFlyingAndNonMobLivingTargetsAndSlowFriendlyPlayers(GameTestHelper test) {
        var player=TestPlayers.survival(test);var other=TestPlayers.survival(test);
        var origin=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8)));player.setPos(origin);player.setYRot(0);player.setNoGravity(true);
        for(int x=6;x<=10;x++)for(int y=5;y<=9;y++)for(int z=7;z<=14;z++)test.setBlock(new BlockPos(x,y,z),Blocks.AIR);
        var cow=EntityType.COW.create(test.getLevel());var wolf=EntityType.WOLF.create(test.getLevel());wolf.tame(player);
        var bat=EntityType.BAT.create(test.getLevel());var stand=EntityType.ARMOR_STAND.create(test.getLevel());
        var targets=java.util.List.<net.minecraft.world.entity.LivingEntity>of(cow,wolf,bat,stand);
        for(var target:targets){target.setPos(origin.add(8,0,0));test.getLevel().addFreshEntity(target);}
        var team=player.getScoreboard().addPlayerTeam("lullaby_"+player.getUUID().toString().substring(0,8));
        for(var member:java.util.List.of(player,cow,other))player.getScoreboard().addPlayerToTeam(member.getScoreboardName(),team);
        boolean pvp=player.server.isPvpAllowed();player.server.setPvpAllowed(false);other.setPos(origin.add(8,0,2));int count=0;
        try {
            test.assertTrue(cow.isAlliedTo(player) && wolf.isAlliedTo(player) && other.isAlliedTo(player),"Allied fixtures are invalid");
            for(var entry:ModItems.ITEMS.getEntries()) {
                if(!(entry.get() instanceof SomniumWeapon weapon) || weapon.isAxe() || weapon.isDagger())continue;
                count++;var stack=Augmentations.apply(new ItemStack(entry.get()),AdditionalAbilities.LULLABY);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
                for(var target:targets) {
                    target.setPos(origin.add(0,0,4));target.setNoGravity(false);target.setDeltaMovement(.1,.1,.1);
                    if(target instanceof net.minecraft.world.entity.Mob mob)mob.setNoAi(false);
                    player.setXRot((float)Math.toDegrees(Math.atan2(player.getEyeY()-target.getBoundingBox().getCenter().y,4)));
                    test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.LULLABY),"Cannot sleep passive/allied/flying/nonmob living target: "+target.getType());
                    test.assertTrue(LullabyEvents.active(player) && WeaponControlLocks.frozen(target) && target.isNoGravity() && target.getDeltaMovement().equals(Vec3.ZERO),"Target not asleep");
                    LullabyEvents.cancel(player.getUUID());
                    test.assertTrue(!WeaponControlLocks.frozen(target) && !target.isNoGravity() && (!(target instanceof net.minecraft.world.entity.Mob mob) || !mob.isNoAi()),"Cancel fails to restore target flags");
                    target.setPos(origin.add(8,0,0));
                }
                for(var mode:java.util.List.of(net.minecraft.world.level.GameType.SURVIVAL,net.minecraft.world.level.GameType.CREATIVE)) {
                    other.setGameMode(mode);other.removeEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN);other.setPos(origin.add(0,0,4));player.setXRot(0);
                    test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.LULLABY),"Allied player rejected with PvP disabled: "+mode);
                    var slow=other.getEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN);boolean jade=weapon.gemstoneName().equals("jade");
                    test.assertTrue(slow!=null && slow.getAmplifier()==(jade?2:1) && slow.getDuration()==(jade?80:60) && !LullabyEvents.active(player),"Player substitute has wrong strength/duration or freezes the player");
                    other.setPos(origin.add(8,0,2));
                }
                other.setGameMode(net.minecraft.world.level.GameType.SPECTATOR);other.setPos(origin.add(0,0,4));
                test.assertFalse(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.LULLABY),"Spectator can be targeted");other.setPos(origin.add(8,0,2));
            }
            test.assertTrue(count==5,"Skipped a Lullaby variant");
            cow.setPos(origin.add(0,0,4));player.setXRot(0);test.setBlock(new BlockPos(8,6,10),Blocks.STONE);
            test.assertFalse(RecommendedAbilityRuntime.activate(player,player.getMainHandItem(),AdditionalAbilities.LULLABY),"All-target targeting pierces a wall");
            test.succeed();
        } finally {
            LullabyEvents.cancel(player.getUUID());player.server.setPvpAllowed(pvp);
            for(var target:targets)target.discard();player.getScoreboard().removePlayerTeam(team);
            for(var member:java.util.List.of(player,other))member.server.getPlayerList().remove(member);
        }
    }
    @GameTest(template="riftfang_test")
    public static void fiveClaymoresSleepWakeAndRestoreOriginalFlags(GameTestHelper test) {
        var player=TestPlayers.survival(test);var origin=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8)));player.setPos(origin);player.setYRot(0);player.setXRot(0);
        for(int x=6;x<=10;x++) for(int y=5;y<=8;y++) for(int z=7;z<=14;z++) test.setBlock(new BlockPos(x,y,z),Blocks.AIR);
        var mob=EntityType.ZOMBIE.create(test.getLevel());mob.setPos(origin.add(0,0,4));test.getLevel().addFreshEntity(mob);
        int count=0;
        try {
            for(var entry:ModItems.ITEMS.getEntries()) {
                var fresh=new ItemStack(entry.get());boolean eligible=entry.get() instanceof SomniumWeapon weapon && !weapon.isDagger() && !weapon.isAxe();
                test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(a->a.id().equals(AdditionalAbilities.LULLABY))==eligible,"Wrong Lullaby assignment");
                if(!eligible) continue;count++;mob.setHealth(20);mob.invulnerableTime=0;mob.setNoAi(false);mob.setNoGravity(false);mob.setTarget(player);player.setItemInHand(InteractionHand.MAIN_HAND,fresh);
                test.assertFalse(RecommendedAbilityRuntime.activate(player,fresh,AdditionalAbilities.LULLABY),"Fresh claymore sleeps");
                var stack=Augmentations.apply(fresh,AdditionalAbilities.LULLABY);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
                test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.LULLABY),"Learned sleep rejected");
                test.assertTrue(mob.isNoAi() && mob.isNoGravity() && mob.getTarget()==null && mob.getDeltaMovement().equals(Vec3.ZERO),"Mob is not asleep");
                test.assertTrue(mob.hurt(player.damageSources().playerAttack(player),1),"Wake hit rejected");
                test.assertFalse(LullabyEvents.active(player),"Damage does not wake mob");
                test.assertTrue(!mob.isNoAi() && !mob.isNoGravity() && mob.getTarget()==player,"Wake fails to restore mob state");
                mob.setNoAi(true);mob.setNoGravity(true);
                test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.LULLABY),"Second sleep rejected");
                player.setItemInHand(InteractionHand.MAIN_HAND,stack.copy());test.assertFalse(LullabyEvents.active(player),"Switch retains sleep");
                test.assertTrue(mob.isNoAi() && mob.isNoGravity(),"Cancellation overwrites original disabled AI/gravity");
            }
            mob.setPos(origin.add(5,0,0));var other=TestPlayers.survival(test);boolean pvp=player.server.isPvpAllowed();player.server.setPvpAllowed(true);
            try {
                other.setPos(origin.add(0,0,4));var stack=Augmentations.apply(new ItemStack(ModItems.DREAMBOUND_CLAYMORE.get()),AdditionalAbilities.LULLABY);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
                test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.LULLABY),"Player Slowness rejected");
                var slow=other.getEffect(net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN);
                test.assertTrue(slow!=null && slow.getAmplifier()==1 && slow.getDuration()==60,"Wrong player sleep substitute");
                test.assertFalse(LullabyEvents.active(player),"Player substitute incorrectly retains mob sleep state");
                test.setBlock(new BlockPos(8,6,10),Blocks.STONE);
                test.assertFalse(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.LULLABY),"Sleep targets through walls");
            } finally {player.server.setPvpAllowed(pvp);player.server.getPlayerList().remove(other);}
        } finally {LullabyEvents.cancel(player.getUUID());mob.discard();player.server.getPlayerList().remove(player);}
        test.assertTrue(count==5,"Skipped Lullaby variants");test.succeed();
    }
    @GameTest(template="riftfang_test",timeoutTicks=120)
    public static void threeSecondSleepExpiresAndRestoresAi(GameTestHelper test) {
        var player=TestPlayers.survival(test);var origin=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8)));player.setPos(origin);player.setYRot(0);player.setXRot(0);
        for(int y=5;y<=8;y++) for(int z=7;z<=14;z++) test.setBlock(new BlockPos(8,y,z),Blocks.AIR);
        var mob=EntityType.ZOMBIE.create(test.getLevel());mob.setPos(origin.add(0,0,4));test.getLevel().addFreshEntity(mob);
        var stack=Augmentations.apply(new ItemStack(ModItems.DREAMBOUND_CLAYMORE.get()),AdditionalAbilities.LULLABY);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
        test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.LULLABY),"Timed sleep rejected");
        test.runAfterDelay(60,()->{
            try {test.assertFalse(LullabyEvents.active(player),"Sleep outlasts three seconds");test.assertFalse(mob.isNoAi() || mob.isNoGravity(),"Expiry leaves mob disabled");test.succeed();}
            finally {LullabyEvents.cancel(player.getUUID());mob.discard();player.server.getPlayerList().remove(player);}
        });
    }
}
