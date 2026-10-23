package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.AdditionalAbilities;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.augmentation.JadeAugments;
import net.bananashelp20.forgermod.block.ModBlocks;
import net.bananashelp20.forgermod.block.entity.custom.InfusionTableBlockEntity;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.abilities.ChallengeRoarAbility;
import net.bananashelp20.forgermod.recipe.ModSpecialRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class JadeInfusionGameTests {
    @GameTest(template = "riftfang_test")
    public static void jadeRequiresLearnedEffectAndPreservesAugmentOnInfusion(GameTestHelper test) throws Exception {
        BlockPos pos = test.absolutePos(new BlockPos(1, 2, 1));
        test.getLevel().setBlockAndUpdate(pos, ModBlocks.INFUSION_TABLE.get().defaultBlockState());
        var table = (InfusionTableBlockEntity) test.getLevel().getBlockEntity(pos);
        var hasRecipe = InfusionTableBlockEntity.class.getDeclaredMethod("hasRecipe");
        hasRecipe.setAccessible(true);
        var craft = InfusionTableBlockEntity.class.getDeclaredMethod("craftItem", int.class);
        craft.setAccessible(true);

        ItemStack fresh = new ItemStack(ModItems.SHRIEKING_CLAYMORE.get());
        ItemStack nonEffect = Augmentations.apply(fresh.copy(), Augmentations.VERTICAL_STRIKE);
        test.assertFalse(JadeAugments.canInfuse(fresh), "Fresh weapon accepts Jade");
        test.assertFalse(JadeAugments.canInfuse(nonEffect), "Non-effect augment accepts Jade");
        table.itemHandler.setStackInSlot(0, new ItemStack(ModItems.JADE_GEMSTONE.get()));
        table.itemHandler.setStackInSlot(1, nonEffect);
        table.itemHandler.setStackInSlot(2, new ItemStack(ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()));
        test.assertFalse((boolean) hasRecipe.invoke(table), "Table accepts Jade without a status-effect augment");
        test.assertTrue(table.itemHandler.getStackInSlot(0).getCount() == 1 && ItemStack.isSameItemSameComponents(table.itemHandler.getStackInSlot(1), nonEffect),
                "Rejected infusion consumed or changed input: gemstone=" + table.itemHandler.getStackInSlot(0)
                        + ", gear=" + table.itemHandler.getStackInSlot(1));

        ItemStack learned = Augmentations.apply(fresh.copy(), AdditionalAbilities.CHALLENGE_ROAR);
        test.assertTrue(JadeAugments.canInfuse(learned), "Learned status-effect augment rejects Jade");
        table.itemHandler.setStackInSlot(1, learned);
        test.assertTrue((boolean) hasRecipe.invoke(table), "Table rejects learned status-effect augment");
        int recipe = -1;
        for (int i = 0; i < ModSpecialRecipes.INFUSION_TABLE_RECIPE_INPUTS.length; i++) {
            var inputs = ModSpecialRecipes.INFUSION_TABLE_RECIPE_INPUTS[i];
            if (inputs[0] == ModItems.JADE_GEMSTONE.get() && inputs[1] == ModItems.SHRIEKING_CLAYMORE.get()) recipe = i;
        }
        test.assertTrue(recipe >= 0, "Missing Jade infusion recipe");
        craft.invoke(table, recipe);
        ItemStack output = table.itemHandler.getStackInSlot(3);
        test.assertTrue(output.is(ModItems.SHRIEKING_CLAYMORE_JADE.get()), "Wrong Jade output");
        test.assertTrue(Augmentations.level(output, AdditionalAbilities.CHALLENGE_ROAR) == 1, "Infusion lost learned augment");
        test.assertTrue(JadeAugments.bonus(output, AdditionalAbilities.CHALLENGE_ROAR) == 1, "Infusion did not activate Jade bonus");

        ItemStack materialHit = Augmentations.apply(fresh.copy(), Augmentations.MATERIAL_HIT);
        test.assertTrue(JadeAugments.canInfuse(materialHit), "Learned Material Hit rejects Jade");
        test.succeed();
    }

    @GameTest(template = "riftfang_test")
    public static void jadeReinforcesOnlyLearnedStatusEffects(GameTestHelper test) {
        var player = TestPlayers.survival(test);
        try {
            ItemStack jade = Augmentations.apply(new ItemStack(ModItems.SHRIEKING_CLAYMORE_JADE.get()), AdditionalAbilities.CHALLENGE_ROAR);
            player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,jade);
            ChallengeRoarAbility.activate(player, jade);
            var strength = player.getEffect(MobEffects.DAMAGE_BOOST);
            test.assertTrue(strength != null && strength.getAmplifier() == 1 && strength.getDuration() == 220,
                    "Jade did not add one Strength level and one second");
            player.removeEffect(MobEffects.DAMAGE_BOOST);

            ItemStack plain = Augmentations.apply(new ItemStack(ModItems.SHRIEKING_CLAYMORE.get()), AdditionalAbilities.CHALLENGE_ROAR);
            player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND,plain);
            ChallengeRoarAbility.activate(player, plain);
            strength = player.getEffect(MobEffects.DAMAGE_BOOST);
            test.assertTrue(strength != null && strength.getAmplifier() == 0 && strength.getDuration() == 200,
                    "Uninfused status effect changed");
            ChallengeRoarAbility.cancel(player.getUUID());
        } finally {
            player.server.getPlayerList().remove(player);
        }
        test.succeed();
    }
}
