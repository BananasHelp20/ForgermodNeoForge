package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.AdditionalAbilities;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.InanisiumWeapon;
import net.bananashelp20.forgermod.item.custom.abilities.RecommendedAbilityRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class DisplaceGameTests {
    private static void aim(net.minecraft.server.level.ServerPlayer player,net.minecraft.world.entity.Entity target) {
        var direction=target.getBoundingBox().getCenter().subtract(player.getEyePosition());
        player.setYRot((float)Math.toDegrees(Math.atan2(-direction.x,direction.z)));
        player.setXRot((float)-Math.toDegrees(Math.atan2(direction.y,Math.hypot(direction.x,direction.z))));
    }
    @GameTest(template="riftfang_test",batch="displace_reviewed_targets")
    public static void passiveAlliedLivingAndDistantNonlivingTargetsUseReviewedEligibility(GameTestHelper test) {
        var player=TestPlayers.survival(test);player.setNoGravity(true);
        var from=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,5)));player.setPos(from);
        for(int x=-2;x<=2;x++)for(int y=0;y<4;y++)for(int z=-1;z<=26;z++)
            test.getLevel().setBlockAndUpdate(player.blockPosition().offset(x,y,z),Blocks.AIR.defaultBlockState());
        var team=player.getScoreboard().addPlayerTeam("swap_"+player.getUUID().toString().substring(0,8));
        player.getScoreboard().addPlayerToTeam(player.getScoreboardName(),team);
        var cow=EntityType.COW.create(test.getLevel());cow.setNoAi(true);cow.setNoGravity(true);cow.setPos(from.add(0,0,6));test.getLevel().addFreshEntity(cow);
        var ally=EntityType.ZOMBIE.create(test.getLevel());ally.setNoAi(true);ally.setNoGravity(true);ally.setPos(from.add(2,0,6));test.getLevel().addFreshEntity(ally);
        player.getScoreboard().addPlayerToTeam(ally.getScoreboardName(),team);
        var item=new net.minecraft.world.entity.item.ItemEntity(test.getLevel(),from.x,from.y,from.z+24,new ItemStack(net.minecraft.world.item.Items.DIAMOND));
        item.setNoGravity(true);item.setPickUpDelay(32767);test.getLevel().addFreshEntity(item);
        var arrow=EntityType.ARROW.create(test.getLevel());
        try {
            var stack=Augmentations.apply(new ItemStack(ModItems.RIFTFANG_DAGGER.get()),AdditionalAbilities.DISPLACE);
            player.setItemInHand(InteractionHand.MAIN_HAND,stack);
            aim(player,cow);test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.DISPLACE),"Passive living target rejected");
            player.setPos(from);cow.setPos(from.add(-2,0,6));aim(player,ally);
            test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.DISPLACE),"Allied living target rejected");
            player.setPos(from);ally.setPos(from.add(2,0,6));cow.setPos(from.add(-2,0,6));
            item.setPos(from.add(0,0,6));aim(player,item);test.assertFalse(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.DISPLACE),"Lower rank targets a nonliving entity");
            item.setPos(from.add(0,0,24));
            var max=Augmentations.setLevel(new ItemStack(ModItems.RIFTFANG_DAGGER.get()),AdditionalAbilities.DISPLACE,4);
            player.setItemInHand(InteractionHand.MAIN_HAND,max);aim(player,item);var destination=item.position();
            test.assertTrue(RecommendedAbilityRuntime.activate(player,max,AdditionalAbilities.DISPLACE),"Max rank cannot swap with a distant item");
            test.assertTrue(player.position().distanceToSqr(destination)<.0001 && item.position().distanceToSqr(from)<.0001,"Distant item swap moves only one participant");
            player.setPos(from);item.setPos(from.add(-2,0,4));arrow.setPos(from.add(0,1.5,24));arrow.setNoGravity(true);arrow.setOwner(player);test.getLevel().addFreshEntity(arrow);
            aim(player,arrow);var arrowPosition=arrow.position();
            test.assertTrue(RecommendedAbilityRuntime.activate(player,max,AdditionalAbilities.DISPLACE)
                    && player.position().distanceToSqr(arrowPosition)<.0001 && arrow.position().distanceToSqr(from)<.0001,"Maximum cannot swap a visible projectile");
            var text=net.bananashelp20.forgermod.augmentation.AbilityDescriptions.description(max,AdditionalAbilities.DISPLACE,4,false).getString();
            test.assertTrue(text.contains("visible entity") && !text.contains("10 blocks"),"Maximum description retains living/range restriction");
        } finally {cow.discard();ally.discard();item.discard();arrow.discard();player.getScoreboard().removePlayerTeam(team);player.server.getPlayerList().remove(player);}
        test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void fiveVariantsSwapOnlyWithClearDestinations(GameTestHelper test) {
        var player=TestPlayers.survival(test);player.setNoGravity(true);
        var from=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,5)));var to=from.add(0,0,6);
        for(int x=6;x<=10;x++) for(int y=5;y<=9;y++) for(int z=4;z<=14;z++) test.setBlock(new BlockPos(x,y,z),Blocks.AIR);
        var target=EntityType.ZOMBIE.create(test.getLevel());target.setNoAi(true);target.setNoGravity(true);target.setPos(to);test.getLevel().addFreshEntity(target);
        int checked=0;
        try {
            for(var entry:ModItems.ITEMS.getEntries()) {
                var fresh=new ItemStack(entry.get());
                boolean eligible=entry.get() instanceof InanisiumWeapon weapon && weapon.isDagger();
                test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(a->a.id().equals(AdditionalAbilities.DISPLACE))==eligible,"Wrong Displace assignment");
                if(!eligible) continue;checked++;player.setPos(from);target.setPos(to);player.setYRot(0);player.setXRot(0);
                player.setItemInHand(InteractionHand.MAIN_HAND,fresh);
                test.assertFalse(RecommendedAbilityRuntime.activate(player,fresh,AdditionalAbilities.DISPLACE),"Fresh dagger swaps");
                var stack=Augmentations.apply(fresh,AdditionalAbilities.DISPLACE);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
                player.fallDistance=12;target.fallDistance=12;
                test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.DISPLACE),"Clear swap rejected");
                test.assertTrue(player.position().distanceToSqr(to)<.0001 && target.position().distanceToSqr(from)<.0001,"Swap moves only one participant");
                test.assertTrue(player.fallDistance==0 && target.fallDistance==0,"Swap retains fall damage");
                player.setPos(from);target.setPos(to);player.setYRot(0);
                test.setBlock(new BlockPos(8,6,8),Blocks.STONE);
                test.assertFalse(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.DISPLACE),"Swap targets through wall");
                test.assertTrue(player.position().equals(from) && target.position().equals(to),"Rejected swap moves participant");
                test.setBlock(new BlockPos(8,6,8),Blocks.AIR);
                test.setBlock(new BlockPos(8,6,11),Blocks.STONE);
                test.assertFalse(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.DISPLACE),"Swap enters blocked body space");
                test.setBlock(new BlockPos(8,6,11),Blocks.AIR);
            }
        } finally {target.discard();player.server.getPlayerList().remove(player);}
        test.assertTrue(checked==5,"Skipped swap variants");test.succeed();
    }
}
