package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.augmentation.RecommendedAbilities;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.abilities.EchoingSpeedEvents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class EchoingSpeedGameTests {
    @GameTest(template="riftfang_test")
    public static void allVariantsBuildOnSuccessfulSameEnemyHitsAndReset(GameTestHelper test) {
        var player=TestPlayers.survival(test); player.setNoGravity(true); player.setInvulnerable(true); player.setPos(test.getBounds().getCenter());
        for(Item item:new Item[]{ModItems.RIFTFANG_DAGGER.get(),ModItems.RIFTFANG_DAGGER_RUBY.get(),ModItems.RIFTFANG_DAGGER_AMBER.get(),
                ModItems.RIFTFANG_DAGGER_AMETHYST.get(),ModItems.RIFTFANG_DAGGER_JADE.get()}) {
            var fresh=new ItemStack(item); player.setItemInHand(InteractionHand.MAIN_HAND,fresh); player.doTick();
            double freshSpeed=player.getAttributeValue(Attributes.ATTACK_SPEED);
            var unlearnedTarget=EntityType.ZOMBIE.create(test.getLevel()); unlearnedTarget.hurt(player.damageSources().playerAttack(player),1);
            test.assertTrue(player.getAttributeValue(Attributes.ATTACK_SPEED)==freshSpeed,"Fresh dagger gains speed"); unlearnedTarget.discard();
            var stack=Augmentations.apply(fresh,RecommendedAbilities.ECHOING_SPEED); player.setItemInHand(InteractionHand.MAIN_HAND,stack); player.doTick();
            double baseline=player.getAttributeValue(Attributes.ATTACK_SPEED);
            var enemy=EntityType.ZOMBIE.create(test.getLevel()); enemy.setNoAi(true); enemy.getAttribute(Attributes.ARMOR).setBaseValue(0);
            for(int hit=1;hit<=8;hit++) {
                enemy.invulnerableTime=0; enemy.hurt(player.damageSources().playerAttack(player),1);
                test.assertTrue(Math.abs(player.getAttributeValue(Attributes.ATTACK_SPEED)-baseline*(1+Math.min(hit,4)*.05))<.001,"Wrong buildup or rank-I cap");
            }
            var other=EntityType.ZOMBIE.create(test.getLevel()); other.setNoAi(true); other.getAttribute(Attributes.ARMOR).setBaseValue(0);
            other.setInvulnerable(true); other.hurt(player.damageSources().playerAttack(player),1);
            test.assertTrue(Math.abs(player.getAttributeValue(Attributes.ATTACK_SPEED)-baseline*1.2)<.001,"Failed hit resets buildup");
            other.setInvulnerable(false); other.hurt(player.damageSources().playerAttack(player),1);
            test.assertTrue(Math.abs(player.getAttributeValue(Attributes.ATTACK_SPEED)-baseline*1.05)<.001,"Different enemy does not reset buildup");
            player.setItemInHand(InteractionHand.MAIN_HAND,stack.copy()); EchoingSpeedEvents.onTick(new PlayerTickEvent.Pre(player));
            test.assertTrue(Math.abs(player.getAttributeValue(Attributes.ATTACK_SPEED)-baseline)<.001,"Switch retains speed modifier");
            enemy.discard(); other.discard();
        }
        test.getLevel().getServer().getPlayerList().remove(player); test.succeed();
    }
    @GameTest(template="riftfang_test",timeoutTicks=200)
    public static void maxRankCapsAndExpiresWithoutLeakingModifier(GameTestHelper test) {
        var player=TestPlayers.survival(test); player.setNoGravity(true); player.setInvulnerable(true); player.setPos(test.getBounds().getCenter());
        player.getAttribute(Attributes.ATTACK_SPEED).addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(ForgerMod.MOD_ID,"test_other_speed"),.2,
                net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
        ItemStack stack=new ItemStack(ModItems.RIFTFANG_DAGGER.get()); for(int rank=0;rank<4;rank++) stack=Augmentations.apply(stack,RecommendedAbilities.ECHOING_SPEED);
        player.setItemInHand(InteractionHand.MAIN_HAND,stack); player.doTick(); double baseline=player.getAttributeValue(Attributes.ATTACK_SPEED);
        var enemy=EntityType.ZOMBIE.create(test.getLevel()); enemy.setNoAi(true); enemy.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000); enemy.setHealth(1000); enemy.getAttribute(Attributes.ARMOR).setBaseValue(0);
        for(int hit=0;hit<20;hit++) { enemy.invulnerableTime=0; enemy.hurt(player.damageSources().playerAttack(player),1); }
        test.assertTrue(Math.abs(player.getAttributeValue(Attributes.ATTACK_SPEED)-baseline*1.5)<.001,"Rank IV cap differs from 50%");
        test.runAfterDelay(161,()->{
            EchoingSpeedEvents.onTick(new PlayerTickEvent.Pre(player));
            test.assertTrue(Math.abs(player.getAttributeValue(Attributes.ATTACK_SPEED)-baseline)<.001,"Idle fight retains modifier");
            test.assertTrue(player.isAlive(),"Idle fixture died instead of timing out");
            enemy.invulnerableTime=0; enemy.hurt(player.damageSources().playerAttack(player),1);
            test.assertTrue(player.getAttributeValue(Attributes.ATTACK_SPEED)>baseline,"Buildup cannot resume");
            test.getLevel().getServer().getPlayerList().remove(player);
            test.assertTrue(Math.abs(player.getAttributeValue(Attributes.ATTACK_SPEED)-baseline)<.001,"Logout leaks modifier or removes unrelated speed");
            enemy.discard(); test.succeed();
        });
    }
}
