package net.bananashelp20.forgermod.gametest;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.LushWeapon;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.*;
import net.minecraft.world.item.*;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.core.component.DataComponents;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class CompostGameTests {
    private static ItemStack rotten() {
        var food=new ItemStack(Items.ROTTEN_FLESH);var props=food.get(DataComponents.FOOD);
        food.set(DataComponents.FOOD,new FoodProperties(props.nutrition(),props.saturation(),false,props.eatSeconds(),props.usingConvertsTo(),java.util.List.of(new FoodProperties.PossibleEffect(()->new MobEffectInstance(MobEffects.HUNGER,600,0),1F))));return food;
    }
    @GameTest(template="riftfang_test")
    public static void fifteenVariantsBlockOnlyRottenFleshHungerInEitherHand(GameTestHelper test) {
        var player=TestPlayers.survival(test);int count=0;
        try {for(var entry:ModItems.ITEMS.getEntries()) {
            var fresh=new ItemStack(entry.get());boolean eligible=entry.get() instanceof LushWeapon;
            test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(a->a.id().equals(AdditionalAbilities.COMPOST))==eligible,"Wrong Compost pool");if(!eligible) continue;count++;
            player.removeEffect(MobEffects.HUNGER);player.setItemInHand(InteractionHand.MAIN_HAND,fresh);player.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);rotten().finishUsingItem(test.getLevel(),player);test.assertTrue(player.hasEffect(MobEffects.HUNGER),"Fresh weapon blocks Hunger");
            var stack=Augmentations.apply(fresh,AdditionalAbilities.COMPOST);player.setItemInHand(InteractionHand.MAIN_HAND,stack);player.removeEffect(MobEffects.HUNGER);player.getFoodData().setFoodLevel(10);var eaten=rotten();eaten.finishUsingItem(test.getLevel(),player);
            test.assertFalse(player.hasEffect(MobEffects.HUNGER),"Learned blade permits rotten-flesh Hunger");test.assertTrue(eaten.isEmpty() && player.getFoodData().getFoodLevel()==14,"Food consumption or nutrition changed");
            player.addEffect(new MobEffectInstance(MobEffects.HUNGER,100,0));rotten().finishUsingItem(test.getLevel(),player);test.assertTrue(player.getEffect(MobEffects.HUNGER).getDuration()==100,"Existing Hunger erased or extended");player.removeEffect(MobEffects.HUNGER);
            var other=new ItemStack(Items.APPLE);other.set(DataComponents.FOOD,rotten().get(DataComponents.FOOD));other.finishUsingItem(test.getLevel(),player);test.assertTrue(player.hasEffect(MobEffects.HUNGER),"Other food Hunger suppressed");player.removeEffect(MobEffects.HUNGER);
            player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);player.setItemInHand(InteractionHand.OFF_HAND,stack);rotten().finishUsingItem(test.getLevel(),player);test.assertFalse(player.hasEffect(MobEffects.HUNGER),"Offhand blade cannot protect main-hand food");
        }} finally {player.server.getPlayerList().remove(player);}
        test.assertTrue(count==15,"Skipped variants");test.succeed();
    }
}
