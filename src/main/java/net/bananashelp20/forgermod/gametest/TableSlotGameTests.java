package net.bananashelp20.forgermod.gametest;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.block.ModBlocks;
import net.bananashelp20.forgermod.block.entity.custom.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.recipe.*;
import net.bananashelp20.forgermod.screen.custom.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder(ForgerMod.MOD_ID) @PrefixGameTestTemplate(false)
public class TableSlotGameTests {
    @GameTest(template="riftfang_test")
    public static void infusionInputsRejectWrongItemsRouteShiftClicksAndStillCraft(GameTestHelper test){
        var pos=test.absolutePos(new BlockPos(1,2,1));test.getLevel().setBlockAndUpdate(pos,ModBlocks.INFUSION_TABLE.get().defaultBlockState());
        var table=(InfusionTableBlockEntity)test.getLevel().getBlockEntity(pos);var player=test.makeMockServerPlayerInLevel();
        try{
            var menu=new InfusionTableMenu(0,player.getInventory(),table,new SimpleContainerData(2));
            var recipes=test.getLevel().getRecipeManager().getAllRecipesFor(ModRecipes.INFUSION_TYPE.get());
            for(var holder:recipes){var recipe=holder.value();
                test.assertTrue(menu.getSlot(39).mayPlace(recipe.ingredient1().getItems()[0]),"Recipe gemstone rejected");
                test.assertTrue(menu.getSlot(37).mayPlace(recipe.ingredient2().getItems()[0]),"Recipe base rejected");
            }
            for(int slot=36;slot<=39;slot++)test.assertFalse(menu.getSlot(slot).mayPlace(new ItemStack(Items.DIRT)),"Wrong manual input accepted");
            test.assertFalse(menu.getSlot(39).mayPlace(new ItemStack(ModItems.SAPPHIRE_GEMSTONE.get())) || menu.getSlot(37).mayPlace(new ItemStack(Items.DIAMOND_SWORD)),"Unlisted recipe input accepted");
            var recipe=recipes.stream().map(holder->holder.value()).filter(r->!r.requiresJadeCompatible()).findFirst().orElseThrow();
            int[] destinations={39,37,38};
            for(int i=0;i<3;i++){var item=recipe.ingredient(i).getItems()[0].copy();var expected=item.getItem();player.getInventory().setItem(9,item);test.assertFalse(menu.quickMoveStack(player,0).isEmpty(),"Valid shift click rejected");test.assertTrue(menu.getSlot(destinations[i]).getItem().is(expected),"Input routed incorrectly");}
            for(int slot=0;slot<4;slot++)test.assertTrue(table.itemHandler.insertItem(slot,new ItemStack(Items.DIRT),false).is(Items.DIRT),"Handler accepted wrong input");
            for(int tick=0;tick<recipe.processingTime();tick++)table.tick(test.getLevel(),pos,table.getBlockState());
            test.assertTrue(menu.getSlot(36).getItem().is(recipe.output().getItem()),"Data-defined recipe failed to craft");
            test.assertTrue(table.itemHandler.getStackInSlot(2).isEmpty(),"Completed infusion did not consume its template");
            test.assertFalse(menu.quickMoveStack(player,36).isEmpty(),"Output extraction failed");
            test.assertTrue(menu.quickMoveStack(player,-1).isEmpty() && menu.quickMoveStack(player,40).isEmpty(),"Invalid slot accepted");
        }finally{player.server.getPlayerList().remove(player);}test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void forgeTemplatesAndOutputRemainRestrictedWithoutBreakingCrafting(GameTestHelper test){
        var pos=test.absolutePos(new BlockPos(1,2,1));test.getLevel().setBlockAndUpdate(pos,ModBlocks.FORGE.get().defaultBlockState());
        var table=(ForgeBlockEntity)test.getLevel().getBlockEntity(pos);var player=test.makeMockServerPlayerInLevel();
        try{
            var menu=new ForgeMenu(0,player.getInventory(),table,new SimpleContainerData(2));
            test.assertTrue(menu.getSlot(37).mayPlace(new ItemStack(ModItems.ANCIENT_UPGRADE_TEMPLATE.get())),"Recipe template rejected");
            test.assertFalse(menu.getSlot(37).mayPlace(new ItemStack(Items.DIRT)) || menu.getSlot(37).mayPlace(new ItemStack(Items.NETHERITE_UPGRADE_SMITHING_TEMPLATE)),"Template not used by any recipe accepted");
            var recipe=test.getLevel().getRecipeManager().getAllRecipesFor(ModRecipes.FORGE_TYPE.get()).stream().map(holder->holder.value()).filter(r->r.output().is(ModItems.ELECTRIUM_INGOT.get())).findFirst().orElseThrow();
            for(int slot=0;slot<3;slot++)table.itemStackHandler.setStackInSlot(slot,recipe.ingredient(slot).getItems()[0].copyWithCount(Math.max(1,recipe.cost(slot))));
            table.itemStackHandler.setStackInSlot(3,recipe.output().copyWithCount(3));
            test.assertFalse(menu.getSlot(36).mayPlace(new ItemStack(ModItems.ELECTRIUM_INGOT.get())),"Output accepts manual input");
            for(int tick=0;tick<recipe.processingTime();tick++)table.tick(test.getLevel(),pos,table.getBlockState());
            test.assertTrue(menu.getSlot(36).getItem().is(recipe.output().getItem()) && menu.getSlot(36).getItem().getCount()==4,"Output merge or data-defined crafting failed");
            test.assertFalse(menu.quickMoveStack(player,36).isEmpty(),"Output extraction failed");
        }finally{player.server.getPlayerList().remove(player);}test.succeed();
    }
}
