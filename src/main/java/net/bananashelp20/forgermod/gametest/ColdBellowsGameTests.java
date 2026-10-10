package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.augmentation.RecommendedAbilities;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.abilities.ColdBellowsEvents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class ColdBellowsGameTests {
    @GameTest(template="riftfang_test")
    public static void realRaisedShieldBlockAppliesRankFourFire(GameTestHelper test) {
        var player=TestPlayers.survival(test); player.setNoGravity(true); player.setPos(test.getBounds().getCenter()); player.setYRot(0);
        ItemStack stack=new ItemStack(ModItems.INFERNAL_CLAYMORE.get());
        for(int rank=0;rank<4;rank++) stack=Augmentations.apply(stack,RecommendedAbilities.COLD_BELLOWS);
        player.setItemInHand(InteractionHand.MAIN_HAND,stack); var shield=new ItemStack(Items.SHIELD);
        player.setItemInHand(InteractionHand.OFF_HAND,shield);
        player.startUsingItem(InteractionHand.OFF_HAND);
        for(int tick=0;tick<6;tick++) player.doTick(); // Shield needs five ticks to become blocking.
        var zombie=EntityType.ZOMBIE.create(test.getLevel()); zombie.setPos(player.position().add(0,0,2));
        player.hurt(player.damageSources().mobAttack(zombie),4);
        test.assertTrue(player.getHealth()==20 && shield.getDamageValue()>0,"Test attack was not blocked by the actual raised shield");
        test.assertTrue(zombie.getRemainingFireTicks()==120,"Actual shield damage pipeline does not apply rank IV Cold Bellows");
        test.getLevel().getServer().getPlayerList().remove(player); test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void allVariantsIgniteOnlySuccessfulBlocksIncludingArrowShooter(GameTestHelper test) {
        for(Item item:new Item[]{ModItems.INFERNAL_CLAYMORE.get(),ModItems.INFERNAL_CLAYMORE_RUBY.get(),
                ModItems.INFERNAL_CLAYMORE_AMBER.get(),ModItems.INFERNAL_CLAYMORE_AMETHYST.get(),ModItems.INFERNAL_CLAYMORE_JADE.get()}) {
            var player=test.makeMockServerPlayerInLevel(); player.setPos(test.getBounds().getCenter());
            player.setItemInHand(InteractionHand.MAIN_HAND,Augmentations.apply(new ItemStack(item),RecommendedAbilities.COLD_BELLOWS));
            player.setItemInHand(InteractionHand.OFF_HAND,new ItemStack(Items.SHIELD)); player.startUsingItem(InteractionHand.OFF_HAND);
            var zombie=EntityType.ZOMBIE.create(test.getLevel());
            var event=new LivingShieldBlockEvent(player,new DamageContainer(zombie.damageSources().mobAttack(zombie),4),true);
            ColdBellowsEvents.onShieldBlock(event);
            test.assertTrue(zombie.getRemainingFireTicks()==60,"Blocked melee attacker does not ignite");
            var skeleton=EntityType.SKELETON.create(test.getLevel()); var arrow=EntityType.ARROW.create(test.getLevel()); arrow.setOwner(skeleton);
            var shot=new LivingShieldBlockEvent(player,new DamageContainer(skeleton.damageSources().arrow(arrow,skeleton),4),true);
            ColdBellowsEvents.onShieldBlock(shot);
            test.assertTrue(skeleton.getRemainingFireTicks()==60 && arrow.getRemainingFireTicks()<=0,"Shield block ignites arrow instead of shooter");
            zombie.clearFire(); event.setBlocked(false); ColdBellowsEvents.onShieldBlock(event);
            test.assertTrue(zombie.getRemainingFireTicks()<=0,"Unblocked attack triggers Cold Bellows");
            event.setBlocked(true); event.setBlockedDamage(0); ColdBellowsEvents.onShieldBlock(event);
            test.assertTrue(zombie.getRemainingFireTicks()<=0,"Zero blocked damage triggers Cold Bellows");
            event.setBlockedDamage(4); event.setCanceled(true); ColdBellowsEvents.onShieldBlock(event);
            test.assertTrue(zombie.getRemainingFireTicks()<=0,"Canceled block triggers Cold Bellows");
            event.setCanceled(false); player.stopUsingItem(); ColdBellowsEvents.onShieldBlock(event);
            test.assertTrue(zombie.getRemainingFireTicks()<=0,"Cold Bellows triggers without a shield");
            player.startUsingItem(InteractionHand.OFF_HAND); player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(item));
            ColdBellowsEvents.onShieldBlock(event); test.assertTrue(zombie.getRemainingFireTicks()<=0,"Fresh weapon has Cold Bellows");
            test.getLevel().getServer().getPlayerList().remove(player);
        }
        test.succeed();
    }
}
