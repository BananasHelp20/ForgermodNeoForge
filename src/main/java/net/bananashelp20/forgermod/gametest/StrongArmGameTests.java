package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.augmentation.RecommendedAbilities;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.abilities.RecommendedAbilityRuntime;
import net.bananashelp20.forgermod.item.custom.abilities.StrongArmAbility;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class StrongArmGameTests {
    @GameTest(template="riftfang_test")
    public static void failedRollStartsCooldownButArmorlessAttemptDoesNot(GameTestHelper test) throws Exception {
        var player=TestPlayers.survival(test); player.setNoGravity(true); player.setPos(test.getBounds().getCenter()); player.setYRot(0); player.setXRot(0);
        for(int x=-1;x<=1;x++) for(int y=0;y<=2;y++) for(int z=0;z<=3;z++)
            test.getLevel().setBlockAndUpdate(player.blockPosition().offset(x,y,z),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
        var stack=Augmentations.apply(new ItemStack(ModItems.MOLTEN_AXE.get()),RecommendedAbilities.STRONG_ARM);
        player.setItemInHand(InteractionHand.MAIN_HAND,stack);
        var zombie=EntityType.ZOMBIE.create(test.getLevel()); zombie.setNoAi(true); zombie.setNoGravity(true); zombie.setPos(player.position().add(0,0,2)); test.getLevel().addFreshEntity(zombie);
        String key=net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem())+":"+RecommendedAbilities.STRONG_ARM;
        AbilityTestPackets.use(player,0);
        test.assertFalse(player.getData(net.bananashelp20.forgermod.item.custom.abilities.WeaponCooldownAttachments.COOLDOWNS).containsKey(key),"Armorless attempt starts cooldown");
        zombie.setItemSlot(EquipmentSlot.CHEST,new ItemStack(Items.DIAMOND_CHESTPLATE)); player.getRandom().setSeed(0);
        player.getRandom().setSeed(0);
        AbilityTestPackets.use(player,0);
        test.assertFalse(zombie.getItemBySlot(EquipmentSlot.CHEST).isEmpty(),"Failed roll fixture unexpectedly succeeds");
        test.assertTrue(player.getData(net.bananashelp20.forgermod.item.custom.abilities.WeaponCooldownAttachments.COOLDOWNS).getOrDefault(key,0L)==player.level().getGameTime()+1200,"Failed roll does not start full cooldown: "+player.getData(net.bananashelp20.forgermod.item.custom.abilities.WeaponCooldownAttachments.COOLDOWNS));
        zombie.discard(); test.getLevel().getServer().getPlayerList().remove(player); test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void allVariantsBreakExactlyOneArmorAtMaxRank(GameTestHelper test) {
        for(Item item:new Item[]{ModItems.MOLTEN_AXE.get(),ModItems.MOLTEN_AXE_RUBY.get(),ModItems.MOLTEN_AXE_AMBER.get(),
                ModItems.MOLTEN_AXE_AMETHYST.get(),ModItems.MOLTEN_AXE_JADE.get()}) {
            var player=TestPlayers.survival(test); player.setNoGravity(true); player.setPos(test.getBounds().getCenter()); player.setYRot(0); player.setXRot(0);
        for(int x=-1;x<=1;x++) for(int y=0;y<=2;y++) for(int z=0;z<=3;z++)
            test.getLevel().setBlockAndUpdate(player.blockPosition().offset(x,y,z),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
            ItemStack stack=new ItemStack(item);
            for(int rank=0;rank<4;rank++) stack=Augmentations.apply(stack,RecommendedAbilities.STRONG_ARM);
            player.setItemInHand(InteractionHand.MAIN_HAND,stack);
            var zombie=EntityType.ZOMBIE.create(test.getLevel()); zombie.setNoAi(true); zombie.setNoGravity(true); zombie.setPos(player.position().add(0,0,2));
            zombie.setItemSlot(EquipmentSlot.HEAD,new ItemStack(Items.DIAMOND_HELMET)); zombie.setItemSlot(EquipmentSlot.CHEST,new ItemStack(Items.DIAMOND_CHESTPLATE));
            zombie.setItemSlot(EquipmentSlot.LEGS,new ItemStack(Items.DIAMOND_LEGGINGS)); zombie.setItemSlot(EquipmentSlot.FEET,new ItemStack(Items.DIAMOND_BOOTS));
            test.getLevel().addFreshEntity(zombie);
            test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,RecommendedAbilities.STRONG_ARM),"Strong-arm cannot target armored enemy");
            int remaining=0; for(var armor:zombie.getArmorSlots()) if(!armor.isEmpty()) remaining++;
            test.assertTrue(remaining==3 && zombie.getHealth()==20,"Strong-arm does not break exactly one armor piece without health damage");
            zombie.discard(); test.getLevel().getServer().getPlayerList().remove(player);
        }
        test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void rankOneChanceHasSuccessAndFailureAndArmorlessTargetsFail(GameTestHelper test) {
        var player=TestPlayers.survival(test); player.setNoGravity(true); player.setPos(test.getBounds().getCenter()); player.setYRot(0); player.setXRot(0);
        for(int x=-1;x<=1;x++) for(int y=0;y<=2;y++) for(int z=0;z<=3;z++)
            test.getLevel().setBlockAndUpdate(player.blockPosition().offset(x,y,z),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
        var stack=Augmentations.apply(new ItemStack(ModItems.MOLTEN_AXE.get()),RecommendedAbilities.STRONG_ARM);
        player.setItemInHand(InteractionHand.MAIN_HAND,stack);
        var zombie=EntityType.ZOMBIE.create(test.getLevel()); zombie.setNoAi(true); zombie.setNoGravity(true); zombie.setPos(player.position().add(0,0,2)); test.getLevel().addFreshEntity(zombie);
        test.assertFalse(RecommendedAbilityRuntime.activate(player,stack,RecommendedAbilities.STRONG_ARM),"Armorless enemy consumes ability");
        int successes=0,failures=0;
        for(int seed=0;seed<100;seed++) {
            long randomSeed=seed*7919L;
            zombie.setItemSlot(EquipmentSlot.CHEST,new ItemStack(Items.DIAMOND_CHESTPLATE)); player.getRandom().setSeed(randomSeed);
            boolean expected=net.minecraft.util.RandomSource.create(randomSeed).nextFloat()<.3F;
            test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,RecommendedAbilities.STRONG_ARM),"Valid armor roll rejected");
            boolean broken=zombie.getItemBySlot(EquipmentSlot.CHEST).isEmpty();
            test.assertTrue(broken==expected,"Rank I probability differs from 30%");
            if(broken) successes++; else failures++;
        }
        test.assertTrue(successes>0 && failures>0,"Rank I does not permit both roll outcomes");
        test.assertTrue(StrongArmAbility.chance(2)==.55F && StrongArmAbility.chance(3)==.8F && StrongArmAbility.chance(4)==1,"Upgrade chance progression wrong");
        zombie.discard(); test.getLevel().getServer().getPlayerList().remove(player); test.succeed();
    }
}
