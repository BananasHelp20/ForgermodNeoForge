package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.TaifuniteWeapon;
import net.bananashelp20.forgermod.item.custom.abilities.RecommendedAbilityRuntime;
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
public class DowndraftBellGameTests {
    @GameTest(template="riftfang_test")
    public static void allVariantsChangeOnlyAirborneEnemyVerticalVelocity(GameTestHelper test) {
        var player=TestPlayers.survival(test);var origin=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8)));player.setPos(origin);
        for(int x=5;x<=12;x++) for(int y=5;y<=12;y++) for(int z=5;z<=14;z++) test.setBlock(new BlockPos(x,y,z),Blocks.AIR);
        var air=EntityType.ZOMBIE.create(test.getLevel());var grounded=EntityType.ZOMBIE.create(test.getLevel());var neutral=EntityType.COW.create(test.getLevel());
        for(var mob:java.util.List.of(air,grounded)) {mob.setNoAi(true);mob.setNoGravity(true);test.getLevel().addFreshEntity(mob);}neutral.setNoAi(true);neutral.setNoGravity(true);test.getLevel().addFreshEntity(neutral);
        air.setPos(origin.add(0,2,2));grounded.setPos(origin.add(1,0,2));grounded.setOnGround(true);neutral.setPos(origin.add(1,2,1));neutral.setOnGround(false);var neutralMotion=new Vec3(.1,.7,.2);neutral.setDeltaMovement(neutralMotion);int count=0;
        try {
            for(var entry:ModItems.ITEMS.getEntries()) {
                var fresh=new ItemStack(entry.get());boolean eligible=entry.get() instanceof TaifuniteWeapon;
                test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(a->a.id().equals(AdditionalAbilities.DOWNDRAFT_BELL))==eligible,"Wrong Downdraft Bell assignment");
                if(!eligible) continue;count++;air.setOnGround(false);air.setDeltaMovement(.4,.7,.2);air.fallDistance=7;var position=air.position();var groundMotion=new Vec3(.1,0,.2);grounded.setDeltaMovement(groundMotion);player.setItemInHand(InteractionHand.MAIN_HAND,fresh);
                test.assertFalse(RecommendedAbilityRuntime.activate(player,fresh,AdditionalAbilities.DOWNDRAFT_BELL),"Fresh weapon forces descent");
                var stack=Augmentations.apply(fresh,AdditionalAbilities.DOWNDRAFT_BELL);player.setItemInHand(InteractionHand.MAIN_HAND,stack);test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.DOWNDRAFT_BELL),"Learned gust rejected");
                test.assertTrue(air.getDeltaMovement().distanceTo(new Vec3(.4,-.8,.2))<.0001 && air.position().equals(position) && air.fallDistance==7 && air.getHealth()==20,"Gust teleports, damages or alters fall distance/horizontal speed");
                test.assertTrue(grounded.getDeltaMovement().equals(groundMotion) && neutral.getDeltaMovement().equals(neutralMotion),"Grounded or neutral target affected");
                air.setDeltaMovement(.4,-2,.2);RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.DOWNDRAFT_BELL);test.assertTrue(air.getDeltaMovement().y==-2,"Gust slows an already faster descent");
            }
        } finally {air.discard();grounded.discard();neutral.discard();player.server.getPlayerList().remove(player);}
        var motion=new Vec3(.32,.7,.24);test.assertTrue(net.bananashelp20.forgermod.item.custom.abilities.VerticalMotionNetwork.adjusted(motion,-.8,true).equals(new Vec3(.32,-.8,.24)),"Client correction overwrites sprint momentum");
        test.assertTrue(net.bananashelp20.forgermod.item.custom.abilities.VerticalMotionNetwork.adjusted(new Vec3(.32,-2,.24),-.8,true).y==-2,"Client correction slows an already faster fall");
        test.assertTrue(count==15,"Skipped Downdraft Bell variants");test.succeed();
    }
}
