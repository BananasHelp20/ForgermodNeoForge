package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.TaifuniteWeapon;
import net.bananashelp20.forgermod.item.custom.abilities.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class CrosswindGameTests {
    @GameTest(template="riftfang_test")
    public static void randomizedDeflectionsStaySidewaysAndAwayForHorizontalVerticalAndSlantedShots(GameTestHelper test) {
        var player=TestPlayers.survival(test);player.setPos(Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8))));
        var stack=Augmentations.apply(new ItemStack(ModItems.DEAD_CALM_DAGGER.get()),AdditionalAbilities.CROSSWIND);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
        var arrow=EntityType.ARROW.create(test.getLevel());test.getLevel().addFreshEntity(arrow);player.getRandom().setSeed(42);
        try {
            test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.CROSSWIND),"Random deflection activation rejected");
            boolean left=false,right=false;
            for(var direction:java.util.List.of(new Vec3(0,0,-1),new Vec3(0,-1,0),new Vec3(.5,-.2,-1).normalize())) {
                var variations=new java.util.HashSet<Vec3>();
                for(int shot=0;shot<64;shot++) {
                    var start=player.getBoundingBox().getCenter().subtract(direction.scale(5));arrow.setPos(start);arrow.setDeltaMovement(direction.scale(2));
                    test.assertTrue(CrosswindEvents.incoming(player,arrow),"Random shot fixture does not approach player");CrosswindEvents.advance(player);
                    var next=arrow.getDeltaMovement();variations.add(next);
                    test.assertTrue(Math.abs(next.length()-2)<.00001 && arrow.position().equals(start),"Random deflection changes speed or position");
                    test.assertTrue(!CrosswindEvents.incoming(player,arrow) && next.dot(direction)<0,"Random direction still approaches player");
                    var sideways=new Vec3(direction.z,0,-direction.x);if(sideways.lengthSqr()<.0001) sideways=new Vec3(1,0,0);else sideways=sideways.normalize();
                    test.assertTrue(Math.abs(next.dot(sideways))>1.6,"Random deflection loses its predominantly sideways direction");
                    if(direction.y==0) {left|=next.x<0;right|=next.x>0;}
                }
                test.assertTrue(variations.size()>30,"Deflection does not vary randomly");
            }
            test.assertTrue(left && right,"Head-on arrows always deflect to the same side");test.succeed();
        } finally {CrosswindEvents.cancel(player.getUUID());arrow.discard();player.server.getPlayerList().remove(player);}
    }
    @GameTest(template="riftfang_test")
    public static void allVariantsDeflectIncomingArrowsPreservingSpeedAndOwnShots(GameTestHelper test) {
        var player=TestPlayers.survival(test);var origin=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8)));player.setPos(origin);var shooter=EntityType.ZOMBIE.create(test.getLevel());var arrow=EntityType.ARROW.create(test.getLevel());test.getLevel().addFreshEntity(arrow);int count=0;
        try {
            for(var entry:ModItems.ITEMS.getEntries()) {
                var fresh=new ItemStack(entry.get());boolean eligible=entry.get() instanceof TaifuniteWeapon;
                test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(a->a.id().equals(AdditionalAbilities.CROSSWIND))==eligible,"Wrong Crosswind assignment");
                if(!eligible) continue;count++;arrow.setOwner(shooter);arrow.setPos(origin.add(0,1,5));arrow.setDeltaMovement(0,0,-2);player.setItemInHand(InteractionHand.MAIN_HAND,fresh);
                test.assertFalse(RecommendedAbilityRuntime.activate(player,fresh,AdditionalAbilities.CROSSWIND),"Fresh weapon deflects");
                var stack=Augmentations.apply(fresh,AdditionalAbilities.CROSSWIND);player.setItemInHand(InteractionHand.MAIN_HAND,stack);test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.CROSSWIND),"Learned shield rejected");test.assertTrue(CrosswindEvents.remainingTicks(player)==2400,"Crosswind does not last two minutes");
                test.assertTrue(CrosswindEvents.incoming(player,arrow),"Incoming arrow not detected");var position=arrow.position();CrosswindEvents.advance(player);
                test.assertTrue(!CrosswindEvents.incoming(player,arrow) && Math.abs(arrow.getDeltaMovement().length()-2)<.0001 && arrow.position().equals(position),"Deflection changes speed, teleports or still targets player");
                arrow.setOwner(player);arrow.setPos(origin.add(0,1,5));arrow.setDeltaMovement(0,0,-2);CrosswindEvents.advance(player);test.assertTrue(arrow.getDeltaMovement().equals(new Vec3(0,0,-2)),"Own shot is deflected");
                arrow.setOwner(shooter);var impact=new ProjectileImpactEvent(arrow,new EntityHitResult(player));CrosswindEvents.impact(impact);test.assertTrue(impact.isCanceled(),"Last-moment projectile hit is not blocked");
                player.setItemInHand(InteractionHand.MAIN_HAND,stack.copy());test.assertFalse(CrosswindEvents.active(player),"Switch retains Crosswind");impact=new ProjectileImpactEvent(arrow,new EntityHitResult(player));CrosswindEvents.impact(impact);test.assertFalse(impact.isCanceled(),"Cancelled Crosswind still blocks impacts");
            }
        } finally {CrosswindEvents.cancel(player.getUUID());arrow.discard();shooter.discard();player.server.getPlayerList().remove(player);}
        test.assertTrue(count==15,"Skipped Crosswind variants");test.succeed();
    }
    @GameTest(template="riftfang_test",timeoutTicks=100)
    public static void actualArrowFlightMissesProtectedPlayer(GameTestHelper test) {
        var player=TestPlayers.survival(test);player.setNoGravity(true);var origin=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8)));player.setPos(origin);
        for(int x=-12;x<=12;x++) for(int y=5;y<=8;y++) for(int z=7;z<=18;z++) test.setBlock(new BlockPos(x,y,z),net.minecraft.world.level.block.Blocks.AIR);
        var stack=Augmentations.apply(new ItemStack(ModItems.DEAD_CALM_DAGGER.get()),AdditionalAbilities.CROSSWIND);player.setItemInHand(InteractionHand.MAIN_HAND,stack);test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.CROSSWIND),"Flight protection rejected");
        var arrow=EntityType.ARROW.create(test.getLevel());arrow.setNoGravity(true);arrow.setPos(origin.add(0,1,8));arrow.setDeltaMovement(0,0,-2);test.getLevel().addFreshEntity(arrow);
        test.runAfterDelay(5,()->{try {test.assertTrue(player.getHealth()==20 && Math.abs(arrow.getX()-origin.x)>3,"Real projectile flight is not deflected away");test.succeed();}finally {CrosswindEvents.cancel(player.getUUID());arrow.discard();player.server.getPlayerList().remove(player);}});
    }
}
