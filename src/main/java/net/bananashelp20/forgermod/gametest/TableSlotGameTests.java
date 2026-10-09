package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.block.ModBlocks;
import net.bananashelp20.forgermod.block.entity.custom.ForgeBlockEntity;
import net.bananashelp20.forgermod.block.entity.custom.InfusionTableBlockEntity;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.recipe.ModSpecialRecipes;
import net.bananashelp20.forgermod.screen.custom.ForgeMenu;
import net.bananashelp20.forgermod.screen.custom.InfusionTableMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class TableSlotGameTests {
    @GameTest(template = "riftfang_test")
    public static void infusionInputsRejectWrongItemsRouteShiftClicksAndStillCraft(GameTestHelper test) throws Exception {
        BlockPos pos = test.absolutePos(new BlockPos(1, 2, 1));
        test.getLevel().setBlockAndUpdate(pos, ModBlocks.INFUSION_TABLE.get().defaultBlockState());
        var table = (InfusionTableBlockEntity)test.getLevel().getBlockEntity(pos);
        var player = test.makeMockServerPlayerInLevel();
        var menu = new InfusionTableMenu(0, player.getInventory(), table, new SimpleContainerData(2));
        for (var recipe : ModSpecialRecipes.INFUSION_TABLE_RECIPE_INPUTS) {
            test.assertTrue(menu.getSlot(39).mayPlace(new ItemStack(recipe[0])), "Gemstone rejected");
            test.assertTrue(menu.getSlot(37).mayPlace(new ItemStack(recipe[1])), "Infusible gear rejected");
        }
        test.assertTrue(menu.getSlot(38).mayPlace(new ItemStack(ModItems.GEMSTONE_UPGRADE_TEMPLATE.get())), "Gemstone template rejected");
        for (int slot = 36; slot <= 39; slot++) test.assertFalse(menu.getSlot(slot).mayPlace(new ItemStack(Items.DIRT)), "Wrong manual input accepted");
        test.assertFalse(menu.getSlot(36).mayPlace(new ItemStack(ModItems.RUBY_GEMSTONE.get())), "Output accepts gemstones");
        test.assertFalse(menu.getSlot(39).mayPlace(new ItemStack(ModItems.SAPPHIRE_GEMSTONE.get())), "Non-infusion sapphire accepted");
        test.assertFalse(menu.getSlot(37).mayPlace(new ItemStack(Items.DIAMOND_SWORD)), "Uninfusible gear accepted");
        test.assertFalse(menu.getSlot(38).mayPlace(new ItemStack(ModItems.ANCIENT_UPGRADE_TEMPLATE.get())), "Wrong template accepted");
        var recipe = ModSpecialRecipes.INFUSION_TABLE_RECIPE_INPUTS[0];
        int[] destinations = {39, 37, 38};
        for (int i = 0; i < 3; i++) {
            player.getInventory().setItem(9, new ItemStack(recipe[i]));
            test.assertFalse(menu.quickMoveStack(player, 0).isEmpty(), "Valid shift-click failed");
            test.assertTrue(menu.getSlot(destinations[i]).getItem().is(recipe[i]), "Shift-click chose wrong slot");
        }
        player.getInventory().setItem(9, new ItemStack(Items.DIRT));
        test.assertTrue(menu.quickMoveStack(player, 0).isEmpty() && menu.getSlot(36).getItem().isEmpty(), "Invalid shift-click entered table");
        for (int slot = 0; slot < 4; slot++) test.assertTrue(table.itemHandler.insertItem(slot, new ItemStack(Items.DIRT), false).is(Items.DIRT), "Handler bypass accepted invalid input");
        var craft = InfusionTableBlockEntity.class.getDeclaredMethod("craftItem", int.class); craft.setAccessible(true); craft.invoke(table, 0);
        test.assertTrue(menu.getSlot(36).getItem().is(ModSpecialRecipes.INFUSION_TABLE_RECIPE_OUTPUTS[0].getItem()), "Output restriction blocked crafting");
        test.assertFalse(menu.quickMoveStack(player, 36).isEmpty(), "Output cannot be extracted");
        test.assertTrue(menu.quickMoveStack(player, -1).isEmpty() && menu.quickMoveStack(player, 40).isEmpty(), "Invalid slot index accepted");
        test.getLevel().getServer().getPlayerList().remove(player); test.succeed();
    }

    @GameTest(template = "riftfang_test")
    public static void forgeTemplatesAndOutputRemainRestrictedWithoutBreakingCrafting(GameTestHelper test) throws Exception {
        BlockPos pos = test.absolutePos(new BlockPos(1, 2, 1));
        test.getLevel().setBlockAndUpdate(pos, ModBlocks.FORGE.get().defaultBlockState());
        var table = (ForgeBlockEntity)test.getLevel().getBlockEntity(pos);
        var player = test.makeMockServerPlayerInLevel();
        var menu = new ForgeMenu(0, player.getInventory(), table, new SimpleContainerData(2));
        for (var item : new net.minecraft.world.item.Item[]{ModItems.ANCIENT_UPGRADE_TEMPLATE.get(), ModItems.GEMSTONE_UPGRADE_TEMPLATE.get(),
                Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE, Items.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE}) {
            test.assertTrue(menu.getSlot(37).mayPlace(new ItemStack(item)), "Upgrade/smithing template rejected");
            player.getInventory().setItem(9, new ItemStack(item));
            menu.quickMoveStack(player, 0);
            test.assertTrue(menu.getSlot(37).getItem().is(item), "Template shift-click chose a material slot");
            table.itemStackHandler.setStackInSlot(2, ItemStack.EMPTY);
        }
        test.assertFalse(menu.getSlot(37).mayPlace(new ItemStack(Items.DIRT)), "Forge template accepts ordinary items");
        test.assertFalse(menu.getSlot(36).mayPlace(new ItemStack(ModItems.ELECTRIUM_INGOT.get())), "Forge output accepts manual input");
        test.assertTrue(table.itemStackHandler.insertItem(3, new ItemStack(Items.DIRT), false).is(Items.DIRT), "Handler bypass entered forge output");
        var recipe = ModSpecialRecipes.FORGE_RECIPE_INPUTS[0];
        table.itemStackHandler.setStackInSlot(0, new ItemStack(recipe[0], 4));
        table.itemStackHandler.setStackInSlot(1, new ItemStack(recipe[1]));
        table.itemStackHandler.setStackInSlot(2, new ItemStack(recipe[2]));
        var output = ModSpecialRecipes.FORGE_RECIPE_OUTPUTS[0];
        table.itemStackHandler.setStackInSlot(3, output.copyWithCount(3));
        var craft = ForgeBlockEntity.class.getDeclaredMethod("craftItem", int.class); craft.setAccessible(true); craft.invoke(table, 0);
        test.assertTrue(menu.getSlot(36).getItem().is(output.getItem()) && menu.getSlot(36).getItem().getCount() == 4, "Output restriction blocked or overwrote crafting");
        test.assertFalse(menu.quickMoveStack(player, 36).isEmpty(), "Forge output cannot be extracted");
        test.assertTrue(menu.quickMoveStack(player, -1).isEmpty() && menu.quickMoveStack(player, 40).isEmpty(), "Invalid forge slot index accepted");
        test.getLevel().getServer().getPlayerList().remove(player); test.succeed();
    }
}
