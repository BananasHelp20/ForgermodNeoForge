package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.block.ModBlocks;
import net.bananashelp20.forgermod.block.entity.custom.ForgeBlockEntity;
import net.bananashelp20.forgermod.block.entity.custom.InfusionTableBlockEntity;
import net.bananashelp20.forgermod.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.ItemStackHandler;

@GameTestHolder(ForgerMod.MOD_ID) @PrefixGameTestTemplate(false)
public class CraftingTemplateGameTests {
    @GameTest(template="riftfang_test")
    public static void infusionConsumesOneTemplateOnlyOnCompletion(GameTestHelper test) {
        var pos=test.absolutePos(new BlockPos(1,2,1));
        test.getLevel().setBlockAndUpdate(pos,ModBlocks.INFUSION_TABLE.get().defaultBlockState());
        var table=(InfusionTableBlockEntity)test.getLevel().getBlockEntity(pos);
        var inventory=table.itemHandler;
        inventory.setStackInSlot(0,new ItemStack(ModItems.RUBY_GEMSTONE.get(),3));
        inventory.setStackInSlot(1,new ItemStack(ModItems.SHRIEKING_CLAYMORE.get()));
        inventory.setStackInSlot(2,new ItemStack(ModItems.GEMSTONE_UPGRADE_TEMPLATE.get(),2));
        checkWorkflow(test,inventory,()->table.tick(test.getLevel(),pos,table.getBlockState()),
                ModItems.SHRIEKING_CLAYMORE_RUBY.get().getDefaultInstance());
        test.succeed();
    }

    @GameTest(template="riftfang_test")
    public static void forgeConsumesOneTemplateOnlyOnCompletion(GameTestHelper test) {
        var pos=test.absolutePos(new BlockPos(1,2,1));
        test.getLevel().setBlockAndUpdate(pos,ModBlocks.FORGE.get().defaultBlockState());
        var forge=(ForgeBlockEntity)test.getLevel().getBlockEntity(pos);
        var inventory=forge.itemStackHandler;
        inventory.setStackInSlot(0,new ItemStack(ModItems.ELECTRIUM_SHARD.get(),12));
        inventory.setStackInSlot(1,new ItemStack(ModItems.CARBON_STEEL_INGOT.get(),3));
        inventory.setStackInSlot(2,new ItemStack(ModItems.ANCIENT_UPGRADE_TEMPLATE.get(),2));
        checkWorkflow(test,inventory,()->forge.tick(test.getLevel(),pos,forge.getBlockState()),
                ModItems.ELECTRIUM_INGOT.get().getDefaultInstance());
        test.succeed();
    }

    private static void checkWorkflow(GameTestHelper test,ItemStackHandler inventory,Runnable tick,ItemStack expected) {
        // A blocked output and an interrupted craft cannot charge the template.
        inventory.setStackInSlot(3,new ItemStack(Items.DIAMOND));
        for(int i=0;i<400;i++)tick.run();
        test.assertTrue(inventory.getStackInSlot(2).getCount()==2,"Blocked craft consumed template");
        inventory.setStackInSlot(3,ItemStack.EMPTY);
        for(int i=0;i<20;i++)tick.run();
        test.assertTrue(inventory.getStackInSlot(2).getCount()==2,"Unfinished craft consumed template");
        var removed=inventory.extractItem(1,64,false);
        for(int i=0;i<400;i++)tick.run();
        test.assertTrue(inventory.getStackInSlot(2).getCount()==2 && inventory.getStackInSlot(3).isEmpty(),"Interrupted craft spent template or produced output");
        inventory.setStackInSlot(1,removed);
        for(int craft=0;craft<2;craft++) {
            int budget=1000;
            while(inventory.getStackInSlot(3).isEmpty() && --budget>0)tick.run();
            test.assertTrue(budget>0 && inventory.getStackInSlot(3).is(expected.getItem()),"Craft did not complete");
            test.assertTrue(inventory.getStackInSlot(2).getCount()==1-craft,"Craft didn't consume exactly one template");
            inventory.extractItem(3,64,false);
            if(craft==0 && inventory.getStackInSlot(1).isEmpty())inventory.setStackInSlot(1,removed.copy());
        }
        if(inventory.getStackInSlot(1).isEmpty())inventory.setStackInSlot(1,removed.copy());
        for(int i=0;i<400;i++)tick.run();
        test.assertTrue(inventory.getStackInSlot(3).isEmpty(),"Craft continued after templates ran out");
    }
}
