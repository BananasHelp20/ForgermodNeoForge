package net.bananashelp20.forgermod.gametest;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.MorsiumWeapon;
import net.bananashelp20.forgermod.item.custom.abilities.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder(ForgerMod.MOD_ID) @PrefixGameTestTemplate(false)
public class BoneGuardGameTests {
    @GameTest(template="riftfang_test")
    public static void allTenVariantsSummonOneFriendlyLootlessArcher(GameTestHelper test) {
        var player=TestPlayers.survival(test);var point=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8)));player.setPos(point);
        for(int x=4;x<=14;x++) for(int y=5;y<=8;y++) for(int z=4;z<=14;z++) test.setBlock(new BlockPos(x,y,z),Blocks.AIR);
        var zombie=EntityType.ZOMBIE.create(test.getLevel());zombie.setNoAi(true);zombie.setNoGravity(true);zombie.setPos(point.add(0,0,5));test.getLevel().addFreshEntity(zombie);
        int count=0;
        try {for(var entry:ModItems.ITEMS.getEntries()) {
            var fresh=new ItemStack(entry.get());boolean eligible=entry.get() instanceof MorsiumWeapon weapon && !weapon.isDagger();
            test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(a->a.id().equals(AdditionalAbilities.BONE_GUARD))==eligible,"Wrong Bone Guard pool");if(!eligible)continue;count++;
            player.setItemInHand(InteractionHand.MAIN_HAND,fresh);test.assertFalse(RecommendedAbilityRuntime.activate(player,fresh,AdditionalAbilities.BONE_GUARD),"Fresh weapon summons guard");
            var stack=Augmentations.apply(fresh,AdditionalAbilities.BONE_GUARD);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
            test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.BONE_GUARD),"Summon failed");
            var guards=player.level().getEntitiesOfClass(BoneGuardEvents.GuardSkeleton.class,player.getBoundingBox().inflate(3));test.assertTrue(guards.size()==1,"Wrong guard count");var guard=guards.getFirst();
            test.assertFalse(WeaponAbilityTargets.enemy(player,guard),"Summon is hostile to owner");test.assertFalse(guard.canAttack(player),"Summon attacks owner");
            test.assertTrue(!guard.shouldBeSaved() && !guard.shouldDropExperience(),"Guard persists or drops XP");
            BoneGuardEvents.tick(new PlayerTickEvent.Pre(player));test.assertTrue(guard.getTarget()==zombie,"Guard does not find enemy");
            guard.performRangedAttack(zombie,1);var arrows=player.level().getEntitiesOfClass(AbstractArrow.class,guard.getBoundingBox().inflate(2),a->a.getOwner()==player);test.assertTrue(arrows.size()==1,"Guard does not shoot actual arrow");
            var arrow=arrows.getFirst();test.assertTrue(arrow.getDeltaMovement().length()>1 && arrow.pickup==AbstractArrow.Pickup.DISALLOWED,"Arrow is stationary or collectible");
            var friendlyHit=new ProjectileImpactEvent(arrow,new EntityHitResult(player));BoneGuardEvents.impact(friendlyHit);test.assertTrue(friendlyHit.isCanceled() && arrow.isRemoved(),"Guard arrow injures owner");
            test.assertFalse(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.BONE_GUARD),"Duplicate guard allowed");
            var box=guard.getBoundingBox().inflate(2);int drops=player.level().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,box).size();
            guard.hurt(player.damageSources().playerAttack(player),100);test.assertTrue(player.level().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,box).size()==drops,"Guard drops bow or bones");BoneGuardEvents.cancel(player.getUUID());
            test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.BONE_GUARD),"Cannot summon replacement after death");
            player.setItemInHand(InteractionHand.MAIN_HAND,stack.copy());test.assertTrue(BoneGuardEvents.active(player),"Switch removes guard");BoneGuardEvents.cancel(player.getUUID());
        }} finally {BoneGuardEvents.cancel(player.getUUID());zombie.discard();player.server.getPlayerList().remove(player);}
        test.assertTrue(count==10,"Skipped variants");test.succeed();
    }
    @GameTest(template="riftfang_test",timeoutTicks=40)
    public static void oneMinuteGuardKeepsFightingAfterSwitchAndExpires(GameTestHelper test) throws Exception {
        var player=TestPlayers.survival(test);player.setNoGravity(true);player.setPos(Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8))));
        var stack=Augmentations.apply(new ItemStack(ModItems.GHOST_AXE.get()),AdditionalAbilities.BONE_GUARD);player.setItemInHand(InteractionHand.MAIN_HAND,stack);AbilityTestPackets.use(player,0);
        var field=BoneGuardEvents.class.getDeclaredField("GUARDS");field.setAccessible(true);@SuppressWarnings("unchecked") var guards=(java.util.Map<java.util.UUID,Object>)field.get(null);var guard=guards.get(player.getUUID());
        var expiry=guard.getClass().getDeclaredMethod("expires");expiry.setAccessible(true);test.assertTrue((Long)expiry.invoke(guard)==player.level().getGameTime()+1200,"Level I lifetime is not one minute");
        player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);WeaponAbilityNetwork.onPlayerTick(new PlayerTickEvent.Pre(player));test.assertTrue(BoneGuardEvents.active(player),"Equipped-session cancellation despawns guard");
        var parts=guard.getClass().getRecordComponents();var types=new Class<?>[parts.length];var values=new Object[parts.length];
        for(int i=0;i<parts.length;i++){types[i]=parts[i].getType();var accessor=parts[i].getAccessor();accessor.setAccessible(true);values[i]=parts[i].getName().equals("expires")?player.level().getGameTime()+10:accessor.invoke(guard);}
        var ctor=guard.getClass().getDeclaredConstructor(types);ctor.setAccessible(true);guards.put(player.getUUID(),ctor.newInstance(values));
        test.runAfterDelay(11,()->{try {test.assertFalse(BoneGuardEvents.active(player),"Expired persistent guard remains");test.succeed();}finally {BoneGuardEvents.cancel(player.getUUID());player.server.getPlayerList().remove(player);}});
    }
    @GameTest(template="riftfang_test")
    public static void tenVariantsTransformToSevenFriendlySkeletonsAtSuperIV(GameTestHelper test) {
        var player=TestPlayers.survival(test);var point=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8)));player.setPos(point);player.setNoGravity(true);
        for(int x=3;x<=14;x++)for(int y=5;y<=9;y++)for(int z=3;z<=14;z++)test.setBlock(new BlockPos(x,y,z),Blocks.AIR);
        var golem=EntityType.IRON_GOLEM.create(test.getLevel());golem.setNoAi(true);golem.setNoGravity(true);golem.setPos(point.add(0,0,5));test.getLevel().addFreshEntity(golem);
        var enemy=EntityType.ZOMBIE.create(test.getLevel());enemy.setNoAi(true);enemy.setNoGravity(true);enemy.setPos(point.add(4,0,4));test.getLevel().addFreshEntity(enemy);
        int count=0;
        try {for(var entry:ModItems.ITEMS.getEntries())if(entry.get() instanceof MorsiumWeapon w && !w.isDagger()) {
            count++;var stack=new ItemStack(entry.get());
            for(int rank=1;rank<=7;rank++) {
                stack=Augmentations.apply(stack,AdditionalAbilities.BONE_GUARD);player.setItemInHand(InteractionHand.MAIN_HAND,stack);test.assertTrue(BoneGuardEvents.activate(player,stack),"Guard group cannot activate");
                var mobs=player.level().getEntitiesOfClass(Mob.class,player.getBoundingBox().inflate(5),m->m instanceof BoneGuardEvents.FriendlyGuard);
                int expected=rank<4?1:rank==4?2:rank==5?3:rank==6?5:7;test.assertTrue(mobs.size()==expected,"Wrong super group count");
                for(var mob:mobs) {test.assertFalse(mob.canAttack(player) || mob.canAttack(golem),"Guard attacks owner/friendly golem");golem.setTarget(mob);test.assertTrue(golem.getTarget()==null,"Friendly golem targets guard");mob.setTarget(golem);test.assertTrue(mob.getTarget()==null,"Guard targets friendly golem");test.assertFalse(mob.shouldBeSaved(),"Temporary guard saves");}
                if(rank>=4)test.assertTrue(Augmentations.displayRank(stack,AdditionalAbilities.BONE_GUARD,rank)==rank-3 && Augmentations.abilityName(stack,AdditionalAbilities.BONE_GUARD,rank).getString().equals("Necromancer"),"Necromancer name/rank wrong");
                if(rank==7)for(var mob:mobs) {
                    if(mob instanceof BoneGuardEvents.GuardBogged bogged)test.assertFalse(bogged.readyForShearing(),"Temporary bogged can farm mushrooms");
                    if(mob instanceof net.minecraft.world.entity.monster.AbstractSkeleton archer && !(mob instanceof net.minecraft.world.entity.monster.WitherSkeleton)) {
                        archer.performRangedAttack(enemy,1);
                        var arrows=player.level().getEntitiesOfClass(AbstractArrow.class,mob.getBoundingBox().inflate(2),a->a.getOwner()==player && !a.isRemoved());
                        test.assertTrue(arrows.size()==1 && arrows.getFirst().pickup==AbstractArrow.Pickup.DISALLOWED,"Necromancer archer arrow ownership/pickup wrong");
                        var hit=new ProjectileImpactEvent(arrows.getFirst(),new EntityHitResult(golem));BoneGuardEvents.impact(hit);test.assertTrue(hit.isCanceled() && arrows.getFirst().isRemoved(),"Necromancer arrow hurts friendly golem");
                    }
                }
                if(rank==7)test.assertTrue(mobs.stream().filter(m->m.getType()==EntityType.BOGGED).count()==1 && mobs.stream().filter(m->m.getType()==EntityType.STRAY).count()==1 && mobs.stream().filter(m->m.getType()==EntityType.SKELETON).count()==2 && mobs.stream().filter(m->m.getType()==EntityType.WITHER_SKELETON).count()==3,"Super IV composition wrong");
                BoneGuardEvents.cancel(player.getUUID());
            }
            test.assertTrue(Augmentations.apply(stack,AdditionalAbilities.BONE_GUARD).isEmpty(),"Necromancer exceeds IV");
        }}finally {BoneGuardEvents.cancel(player.getUUID());golem.discard();enemy.discard();player.server.getPlayerList().remove(player);}
        test.assertTrue(count==10,"Skipped Bone Guard variants");test.succeed();
    }
}
