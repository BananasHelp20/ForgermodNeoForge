package net.bananashelp20.forgermod.gametest;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.TaifuniteWeapon;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class HeadwindGameTests {
    @GameTest(template="riftfang_test")
    public static void onlyLearnedGroundSprintExhaustionIsHalved(GameTestHelper test) {
        var player=TestPlayers.survival(test);player.setPos(Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8))));player.setOnGround(true);player.setSprinting(true);int count=0;
        try {for(var entry:ModItems.ITEMS.getEntries()) {
            var fresh=new ItemStack(entry.get());boolean eligible=entry.get() instanceof TaifuniteWeapon;
            test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(a->a.id().equals(AdditionalAbilities.HEADWIND))==eligible,"Wrong Headwind pool");if(!eligible) continue;count++;
            player.setItemInHand(InteractionHand.MAIN_HAND,fresh);player.getFoodData().setExhaustion(0);player.checkMovementStatistics(10,0,0);test.assertTrue(Math.abs(player.getFoodData().getExhaustionLevel()-1)<.0001,"Fresh weapon discounts sprint");
            var stack=Augmentations.apply(fresh,AdditionalAbilities.HEADWIND);player.setItemInHand(InteractionHand.MAIN_HAND,stack);player.getFoodData().setExhaustion(0);player.checkMovementStatistics(10,0,0);
            test.assertTrue(Math.abs(player.getFoodData().getExhaustionLevel()-.5)<.0001,"Sprint branch not halved");
            player.getFoodData().setExhaustion(0);player.causeFoodExhaustion(.3F);test.assertTrue(Math.abs(player.getFoodData().getExhaustionLevel()-.3)<.0001,"Other exhaustion discounted");
            player.getFoodData().setExhaustion(0);player.jumpFromGround();test.assertTrue(Math.abs(player.getFoodData().getExhaustionLevel()-.2)<.0001,"Sprint-jump exhaustion discounted");player.setOnGround(true);
            player.setSwimming(true);player.getFoodData().setExhaustion(0);player.checkMovementStatistics(10,0,0);test.assertTrue(Math.abs(player.getFoodData().getExhaustionLevel()-.1)<.0001,"Swimming exhaustion discounted");player.setSwimming(false);
            player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);player.setItemInHand(InteractionHand.OFF_HAND,stack);player.getFoodData().setExhaustion(0);player.checkMovementStatistics(10,0,0);test.assertTrue(Math.abs(player.getFoodData().getExhaustionLevel()-1)<.0001,"Offhand discounts sprint");
        }} finally {player.server.getPlayerList().remove(player);}
        test.assertTrue(count==15,"Skipped variants");test.succeed();
    }
}
