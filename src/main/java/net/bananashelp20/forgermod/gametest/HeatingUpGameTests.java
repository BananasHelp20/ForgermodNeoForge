package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.augmentation.RecommendedAbilities;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.abilities.HeatingUpEvents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class HeatingUpGameTests {
    @GameTest(template="riftfang_test")
    public static void allVariantsStoreRealMeleeDamageAndIgniteSweepOnce(GameTestHelper test) {
        for(Item item:new Item[]{ModItems.INFERNAL_CLAYMORE.get(),ModItems.INFERNAL_CLAYMORE_RUBY.get(),
                ModItems.INFERNAL_CLAYMORE_AMBER.get(),ModItems.INFERNAL_CLAYMORE_AMETHYST.get(),ModItems.INFERNAL_CLAYMORE_JADE.get()}) {
            var player=TestPlayers.survival(test); player.setNoGravity(true); player.setPos(test.getBounds().getCenter());
            var stack=Augmentations.apply(new ItemStack(item),RecommendedAbilities.HEATING_UP); player.setItemInHand(InteractionHand.MAIN_HAND,stack);
            var zombie=EntityType.ZOMBIE.create(test.getLevel()); zombie.setPos(player.position().add(0,0,2));
            test.assertTrue(player.hurt(player.damageSources().mobAttack(zombie),10),"Test melee damage failed");
            test.assertTrue(HeatingUpEvents.storedHeat(player)==5,"Melee damage did not store half as heat");
            player.invulnerableTime=0; var arrow=EntityType.ARROW.create(test.getLevel());
            player.hurt(player.damageSources().arrow(arrow,zombie),2);
            test.assertTrue(HeatingUpEvents.storedHeat(player)==5,"Projectile damage incorrectly becomes melee heat");
            var first=EntityType.COW.create(test.getLevel()); var swept=EntityType.COW.create(test.getLevel());
            first.hurt(player.damageSources().playerAttack(player),2); swept.hurt(player.damageSources().playerAttack(player),2);
            test.assertTrue(first.getRemainingFireTicks()==60 && swept.getRemainingFireTicks()==60,"Paid attack does not ignite every affected target");
            test.assertTrue(HeatingUpEvents.storedHeat(player)==0,"Sweep spends heat more than once");
            HeatingUpEvents.onAttack(new net.neoforged.neoforge.event.entity.player.AttackEntityEvent(player,first));
            first.clearFire(); first.invulnerableTime=0; first.hurt(player.damageSources().playerAttack(player),2);
            test.assertTrue(first.getRemainingFireTicks()<=0,"Separate same-tick attack bypasses heat cost");
            player.setRemainingFireTicks(100); HeatingUpEvents.onTick(new PlayerTickEvent.Pre(player));
            test.assertTrue(player.getRemainingFireTicks()==100,"Low-rank Heating Up extinguishes own fire");
            player.clearFire(); player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY); HeatingUpEvents.onTick(new PlayerTickEvent.Pre(player));
            test.getLevel().getServer().getPlayerList().remove(player);
        }
        test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void rankFourAbsorbsFireUnlessFullAndSwitchClearsHeat(GameTestHelper test) {
        var player=TestPlayers.survival(test); ItemStack stack=new ItemStack(ModItems.INFERNAL_CLAYMORE.get());
        for(int rank=0;rank<4;rank++) stack=Augmentations.apply(stack,RecommendedAbilities.HEATING_UP);
        player.setItemInHand(InteractionHand.MAIN_HAND,stack); player.setRemainingFireTicks(400);
        HeatingUpEvents.onTick(new PlayerTickEvent.Pre(player));
        test.assertTrue(HeatingUpEvents.storedHeat(player)==20 && player.getRemainingFireTicks()<=0,"Rank IV does not extinguish fire to fill capped heat");
        player.setRemainingFireTicks(100); HeatingUpEvents.onTick(new PlayerTickEvent.Pre(player));
        test.assertTrue(player.getRemainingFireTicks()==100,"Full heat capacity still extinguishes fire");
        var cow=EntityType.COW.create(test.getLevel()); cow.hurt(player.damageSources().playerAttack(player),2);
        test.assertTrue(cow.getRemainingFireTicks()==120 && HeatingUpEvents.storedHeat(player)==15,"Rank IV hit has wrong ignition or cost");
        HeatingUpEvents.onTick(new PlayerTickEvent.Pre(player));
        test.assertTrue(HeatingUpEvents.storedHeat(player)==20 && player.getRemainingFireTicks()<=0,"Burning is not absorbed once capacity becomes available");
        player.setItemInHand(InteractionHand.MAIN_HAND,stack.copy()); HeatingUpEvents.onTick(new PlayerTickEvent.Pre(player));
        test.assertTrue(HeatingUpEvents.storedHeat(player)==0,"Switching to identical copy retains heat");
        test.getLevel().getServer().getPlayerList().remove(player); test.succeed();
    }
}
