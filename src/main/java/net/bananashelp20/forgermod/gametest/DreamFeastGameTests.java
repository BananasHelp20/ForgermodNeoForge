package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.SomniumWeapon;
import net.bananashelp20.forgermod.item.custom.abilities.RecommendedAbilityRuntime;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class DreamFeastGameTests {
    @GameTest(template="riftfang_test")
    public static void allClaymoresScaleFoodAndSaturationWithinCaps(GameTestHelper test) {
        var player=TestPlayers.survival(test);int count=0;
        try {
            for(var entry:ModItems.ITEMS.getEntries()) {
                var fresh=new ItemStack(entry.get());boolean eligible=entry.get() instanceof SomniumWeapon weapon && !weapon.isDagger() && !weapon.isAxe();
                test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(a->a.id().equals(AdditionalAbilities.DREAM_FEAST))==eligible,"Wrong Dream Feast assignment");
                if(!eligible) continue;count++;player.setItemInHand(InteractionHand.MAIN_HAND,fresh);player.getFoodData().setFoodLevel(5);player.getFoodData().setSaturation(2);
                test.assertFalse(RecommendedAbilityRuntime.activate(player,fresh,AdditionalAbilities.DREAM_FEAST),"Fresh claymore feeds");
                for(int rank=1;rank<=5;rank++) {
                var stack=Augmentations.setLevel(fresh,AdditionalAbilities.DREAM_FEAST,rank);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
                int points=2*rank;player.getFoodData().setFoodLevel(5);player.getFoodData().setSaturation(2);
                test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.DREAM_FEAST),"Learned feast rejected");
                test.assertTrue(player.getFoodData().getFoodLevel()==5+points && player.getFoodData().getSaturationLevel()==2+points,"Feast restores wrong food/saturation");
                player.getFoodData().setFoodLevel(19);test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.DREAM_FEAST),"Partial hunger rejected");
                test.assertTrue(player.getFoodData().getFoodLevel()==20,"Feast exceeds maximum hunger");
                player.getFoodData().setSaturation(19);
                test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.DREAM_FEAST) && player.getFoodData().getSaturationLevel()==20,"Full hunger cannot top up saturation or exceeds cap");
                test.assertFalse(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.DREAM_FEAST),"Full hunger and saturation wastes ability");
                player.getFoodData().setSaturation(0);test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.DREAM_FEAST) && player.getFoodData().getFoodLevel()==20 && player.getFoodData().getSaturationLevel()==points,"Full-hunger saturation recovery wrong");
                }
            }
        } finally {player.server.getPlayerList().remove(player);}
        test.assertTrue(count==5,"Skipped Dream Feast variants");test.succeed();
    }
}
