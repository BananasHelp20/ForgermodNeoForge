package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.augmentation.RecommendedAbilities;
import net.bananashelp20.forgermod.item.ModItems;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class QuenchPointGameTests {
    @GameTest(template="riftfang_test",timeoutTicks=300)
    public static void maxRankCapsStoredFireAndExpires(GameTestHelper test) {
        var player=test.makeMockServerPlayerInLevel();
        ItemStack stack=new ItemStack(ModItems.EMBERFANG_DAGGER.get());
        for(int rank=0;rank<4;rank++) stack=Augmentations.apply(stack,RecommendedAbilities.QUENCH_POINT);
        player.setItemInHand(InteractionHand.MAIN_HAND,stack);
        var cow=EntityType.COW.create(test.getLevel());
        cow.setNoAi(true); cow.setNoGravity(true); cow.setPos(test.getBounds().getCenter());
        cow.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200); cow.setHealth(200);
        test.getLevel().addFreshEntity(cow); cow.setRemainingFireTicks(2000);
        cow.hurt(player.damageSources().playerAttack(player),10); cow.invulnerableTime=0;
        cow.hurt(player.damageSources().playerAttack(player),2);
        test.assertTrue(cow.getHealth()==175.5F,"Rank IV burn conversion is not capped at 12.5 damage");
        cow.setRemainingFireTicks(2000); cow.invulnerableTime=0;
        cow.hurt(player.damageSources().playerAttack(player),2);
        test.runAfterDelay(201,()->{
            cow.invulnerableTime=0; cow.hurt(player.damageSources().playerAttack(player),2);
            test.assertTrue(cow.getHealth()==171.5F,"Expired Quench Point charge still adds damage");
            cow.discard(); test.getLevel().getServer().getPlayerList().remove(player); test.succeed();
        });
    }
    @GameTest(template="riftfang_test")
    public static void allVariantsConvertFireOnNextHitOnly(GameTestHelper test) {
        for(Item item:new Item[]{ModItems.EMBERFANG_DAGGER.get(),ModItems.EMBERFANG_DAGGER_RUBY.get(),
                ModItems.EMBERFANG_DAGGER_AMBER.get(),ModItems.EMBERFANG_DAGGER_AMETHYST.get(),ModItems.EMBERFANG_DAGGER_JADE.get()}) {
            var player=test.makeMockServerPlayerInLevel();
            var stack=Augmentations.apply(new ItemStack(item),RecommendedAbilities.QUENCH_POINT);
            test.assertFalse(stack.isEmpty(),"Quench Point cannot be learned");
            player.setItemInHand(InteractionHand.MAIN_HAND,stack);
            var cow=EntityType.COW.create(test.getLevel());
            cow.setNoAi(true); cow.setNoGravity(true); cow.setPos(test.getBounds().getCenter());
            cow.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100); cow.setHealth(100);
            test.getLevel().addFreshEntity(cow); cow.setRemainingFireTicks(200);
            test.assertTrue(cow.hurt(player.damageSources().playerAttack(player),10),"Initial hit failed");
            test.assertTrue(cow.getHealth()==90 && cow.getRemainingFireTicks()<=0,"Initial hit did not quench without bonus");
            // Invulnerability rejects a weaker hit; it must not consume the stored fire.
            test.assertFalse(cow.hurt(player.damageSources().playerAttack(player),.1F),"Invulnerable hit unexpectedly succeeded");
            cow.invulnerableTime=0;
            test.assertTrue(cow.hurt(player.damageSources().playerAttack(player),2),"Charged hit failed");
            test.assertTrue(cow.getHealth()==83,"Next hit did not receive five damage of stored fire");
            cow.invulnerableTime=0; cow.hurt(player.damageSources().playerAttack(player),2);
            test.assertTrue(cow.getHealth()==81,"Stored fire applied more than once");
            cow.setRemainingFireTicks(200); cow.invulnerableTime=0; cow.hurt(player.damageSources().playerAttack(player),2);
            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(item));
            cow.invulnerableTime=0; cow.hurt(player.damageSources().playerAttack(player),2);
            test.assertTrue(cow.getHealth()==77,"Switching retained a stored fire bonus");
            cow.discard(); test.getLevel().getServer().getPlayerList().remove(player);
        }
        test.succeed();
    }
}
