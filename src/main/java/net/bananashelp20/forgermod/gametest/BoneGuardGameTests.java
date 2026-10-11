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
            player.setItemInHand(InteractionHand.MAIN_HAND,stack.copy());test.assertFalse(BoneGuardEvents.active(player),"Switch keeps guard");
        }} finally {BoneGuardEvents.cancel(player.getUUID());zombie.discard();player.server.getPlayerList().remove(player);}
        test.assertTrue(count==10,"Skipped variants");test.succeed();
    }
    @GameTest(template="riftfang_test",timeoutTicks=330)
    public static void expiresAfterFifteenSeconds(GameTestHelper test) {
        var player=TestPlayers.survival(test);player.setNoGravity(true);player.setPos(Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8))));
        var stack=Augmentations.apply(new ItemStack(ModItems.GHOST_AXE.get()),AdditionalAbilities.BONE_GUARD);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
        test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.BONE_GUARD),"Summon failed");
        test.runAfterDelay(301,()->{try {test.assertFalse(BoneGuardEvents.active(player),"Guard exceeds fifteen seconds");}finally{BoneGuardEvents.cancel(player.getUUID());player.server.getPlayerList().remove(player);}test.succeed();});
    }
}
