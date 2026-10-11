package net.bananashelp20.forgermod.gametest;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.PulsiteWeapon;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder(ForgerMod.MOD_ID) @PrefixGameTestTemplate(false)
public class EchoAppetiteGameTests {
    @GameTest(template="riftfang_test")
    public static void allFifteenVariantsFeedOnOnlyActualSculkMining(GameTestHelper test) {
        var player=TestPlayers.survival(test);var pos=test.absolutePos(new BlockPos(8,5,8));int count=0;
        try {for(var entry:ModItems.ITEMS.getEntries()) {
            var fresh=new ItemStack(entry.get());boolean eligible=entry.get() instanceof PulsiteWeapon;
            test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(a->a.id().equals(AdditionalAbilities.ECHO_APPETITE))==eligible,"Wrong appetite pool");if(!eligible)continue;count++;
            player.setItemInHand(InteractionHand.MAIN_HAND,fresh);player.getFoodData().setFoodLevel(10);fresh.mineBlock(test.getLevel(),Blocks.SCULK.defaultBlockState(),pos,player);test.assertTrue(player.getFoodData().getFoodLevel()==10,"Fresh weapon feeds");
            var stack=Augmentations.apply(fresh,AdditionalAbilities.ECHO_APPETITE);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
            for(var sculk:java.util.List.of(Blocks.SCULK,Blocks.SCULK_VEIN,Blocks.SCULK_SENSOR,Blocks.CALIBRATED_SCULK_SENSOR,Blocks.SCULK_CATALYST,Blocks.SCULK_SHRIEKER)) {
                player.getFoodData().setFoodLevel(10);player.getFoodData().setSaturation(2);int wear=stack.getDamageValue();
                stack.mineBlock(test.getLevel(),sculk.defaultBlockState(),pos,player);
                test.assertTrue(player.getFoodData().getFoodLevel()==11 && player.getFoodData().getSaturationLevel()==2,"Wrong hunger/saturation");
                if(sculk.defaultBlockState().getDestroySpeed(test.getLevel(),pos)!=0)test.assertTrue(stack.getDamageValue()>wear,"Mining wear suppressed");
            }
            stack.mineBlock(test.getLevel(),Blocks.STONE.defaultBlockState(),pos,player);test.assertTrue(player.getFoodData().getFoodLevel()==11,"Stone grants hunger");
            player.getFoodData().setFoodLevel(20);stack.mineBlock(test.getLevel(),Blocks.SCULK.defaultBlockState(),pos,player);test.assertTrue(player.getFoodData().getFoodLevel()==20,"Food exceeds maximum");
            stack.setDamageValue(stack.getMaxDamage()-1);player.getFoodData().setFoodLevel(10);stack.mineBlock(test.getLevel(),Blocks.SCULK.defaultBlockState(),pos,player);test.assertTrue(stack.isEmpty() && player.getFoodData().getFoodLevel()==11,"Last-durability sculk hit loses benefit");
        }}finally {player.server.getPlayerList().remove(player);}
        test.assertTrue(count==15,"Skipped variants");test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void serverDestroyBlockActuallyRestoresHunger(GameTestHelper test) {
        var player=TestPlayers.survival(test);var pos=test.absolutePos(new BlockPos(8,5,8));
        try {test.getLevel().setBlockAndUpdate(pos,Blocks.SCULK.defaultBlockState());player.getFoodData().setFoodLevel(10);
            player.setItemInHand(InteractionHand.MAIN_HAND,Augmentations.apply(new ItemStack(ModItems.SHRIEKING_CLAYMORE.get()),AdditionalAbilities.ECHO_APPETITE));
            test.assertTrue(player.gameMode.destroyBlock(pos),"Actual sculk break failed");test.assertTrue(player.getFoodData().getFoodLevel()==11 && test.getLevel().getBlockState(pos).isAir(),"Actual block destruction does not feed");
        }finally {player.server.getPlayerList().remove(player);}test.succeed();
    }
}
