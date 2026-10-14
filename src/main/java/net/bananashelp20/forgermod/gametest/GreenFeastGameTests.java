package net.bananashelp20.forgermod.gametest;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.LushWeapon;
import net.bananashelp20.forgermod.item.custom.abilities.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class GreenFeastGameTests {
    @GameTest(template="riftfang_test")
    public static void fifteenVariantsConsumeOneWheatForHealingOrSaturation(GameTestHelper test) {
        var player=TestPlayers.survival(test);int count=0;
        try {for(var entry:ModItems.ITEMS.getEntries()) {
            var fresh=new ItemStack(entry.get());boolean eligible=entry.get() instanceof LushWeapon;
            test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(a->a.id().equals(AdditionalAbilities.GREEN_FEAST))==eligible,"Wrong Green Feast pool");if(!eligible) continue;count++;
            player.getFoodData().setFoodLevel(20);player.getFoodData().setSaturation(0);player.getInventory().setItem(10,new ItemStack(Items.WHEAT,2));player.setHealth(18);player.setItemInHand(InteractionHand.MAIN_HAND,fresh);
            test.assertFalse(RecommendedAbilityRuntime.activate(player,fresh,AdditionalAbilities.GREEN_FEAST),"Fresh weapon heals");
            var stack=Augmentations.apply(fresh,AdditionalAbilities.GREEN_FEAST);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
            test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.GREEN_FEAST) && player.getHealth()==20 && player.getFoodData().getSaturationLevel()==2 && player.getInventory().getItem(10).getCount()==1,"Healing/saturation or wheat cost wrong");
            player.getFoodData().setSaturation(20);test.assertFalse(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.GREEN_FEAST),"Full health and saturation spends wheat");test.assertTrue(player.getInventory().getItem(10).getCount()==1,"Rejected use consumes wheat");
            player.setHealth(19);test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.GREEN_FEAST) && player.getHealth()==20 && player.getInventory().getItem(10).isEmpty(),"Healing cap or final wheat wrong");
            player.setHealth(18);test.assertFalse(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.GREEN_FEAST),"Missing wheat heals");test.assertTrue(player.getHealth()==18,"Rejected use changes health");
            player.setHealth(player.getMaxHealth());player.getFoodData().setSaturation(19);player.getInventory().setItem(10,new ItemStack(Items.WHEAT));
            test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.GREEN_FEAST) && player.getFoodData().getSaturationLevel()==20 && player.getInventory().getItem(10).isEmpty(),"Full-health saturation recovery/cap/cost wrong");
        }} finally {player.server.getPlayerList().remove(player);}
        test.assertTrue(count==15,"Skipped variants");test.succeed();
    }
}
