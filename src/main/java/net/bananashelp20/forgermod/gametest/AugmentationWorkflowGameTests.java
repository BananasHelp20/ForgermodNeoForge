package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.AugmentationAnimation;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.block.ModBlocks;
import net.bananashelp20.forgermod.block.entity.custom.AugmentationTableBlockEntity;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.screen.custom.AugmentationTableMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class AugmentationWorkflowGameTests {
    private static AugmentationTableBlockEntity table(GameTestHelper test) {
        BlockPos pos=test.absolutePos(new BlockPos(1,2,1));
        test.getLevel().setBlockAndUpdate(pos,ModBlocks.AUGMENTATION_TABLE.get().defaultBlockState());
        return (AugmentationTableBlockEntity)test.getLevel().getBlockEntity(pos);
    }
    private static void fill(AugmentationTableBlockEntity table,boolean reversed) {
        table.inventory.setStackInSlot(0,new ItemStack(ModItems.DEATHWISPER_DAGGER_JADE.get()));
        table.inventory.setStackInSlot(1,new ItemStack(reversed?ModItems.SAPPHIRE_GEMSTONE.get():ModItems.GEMSTONE_UPGRADE_TEMPLATE.get(),3));
        table.inventory.setStackInSlot(2,new ItemStack(reversed?ModItems.GEMSTONE_UPGRADE_TEMPLATE.get():ModItems.SAPPHIRE_GEMSTONE.get(),3));
        table.tick();
    }
    private static void advance(AugmentationTableBlockEntity table,int phase) {
        int budget=10000;
        while(table.data.get(0)<phase && --budget>0) table.tick();
        if(budget==0 || table.data.get(0)!=phase) throw new AssertionError("Workflow failed to reach phase "+phase);
    }
    @GameTest(template="riftfang_test")
    public static void cancellationAtEveryPhaseAndEitherIngredientOrderIsLossless(GameTestHelper test) {
        var table=table(test); var player=test.makeMockServerPlayerInLevel(); player.setPos(table.getBlockPos().getCenter());
        for(boolean reversed:new boolean[]{false,true}) for(int phase=1;phase<=5;phase++) {
            fill(table,reversed); advance(table,phase);
            var menu=new AugmentationTableMenu(0,player.getInventory(),table,table.data);
            test.assertTrue(menu.getSlot(0).mayPickup(player),"Weapon locked before completion");
            test.assertTrue(table.inventory.getStackInSlot(1).getCount()==3 && table.inventory.getStackInSlot(2).getCount()==3,"Animation spent ingredients");
            boolean covered=phase>=AugmentationAnimation.EXPAND;
            test.assertTrue(menu.getSlot(1).isActive()!=covered && menu.getSlot(2).mayPickup(player)!=covered,"Covered slot remains hoverable/removable");
            test.assertTrue(table.inventory.extractItem(1,1,true).isEmpty()==covered,"Handler ignores side lock");
            if(covered) test.assertTrue(table.inventory.extractItem(1,1,false).isEmpty(),"Actual covered extraction succeeded");
            var saved=table.saveWithoutMetadata(test.getLevel().registryAccess());
            table.loadWithComponents(saved,test.getLevel().registryAccess());
            test.assertTrue(table.data.get(0)==phase && table.inventory.getStackInSlot(1).getCount()==3,"Reload lost animation/payment state");
            menu.clicked(0,0,ClickType.PICKUP,player);
            test.assertTrue(!menu.getCarried().isEmpty() && Augmentations.count(menu.getCarried())==0,"Cancelling changed/lost weapon");
            menu.setCarried(ItemStack.EMPTY);
            test.assertTrue(table.data.get(0)==0 && menu.getSlot(1).isActive() && menu.getSlot(2).mayPickup(player),"Cancellation did not immediately reset/unlock GUI");
            test.assertTrue(table.inventory.getStackInSlot(1).getCount()==3 && table.inventory.getStackInSlot(2).getCount()==3,"Cancellation lost/duplicated materials");
            test.assertFalse(table.choose(0),"Stale selection accepted after cancellation");
        }
        fill(table,true); advance(table,AugmentationAnimation.CHOOSE);
        var menu=new AugmentationTableMenu(0,player.getInventory(),table,table.data);
        test.assertTrue(!menu.quickMoveStack(player,0).isEmpty() && table.data.get(0)==0,"Shift-taking weapon does not cancel selection");
        test.getLevel().getServer().getPlayerList().remove(player); test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void geometryTimingPersistenceAndSingleTransactionalChoice(GameTestHelper test) {
        var table=table(test); fill(table,true);
        var player=test.makeMockServerPlayerInLevel(); player.setPos(table.getBlockPos().getCenter());
        var menu=new AugmentationTableMenu(0,player.getInventory(),table,table.data);
        test.assertTrue(menu.getSlot(0).x==80 && menu.getSlot(1).x==44 && menu.getSlot(2).x==117 && menu.getSlot(0).y==34
                && menu.getSlot(4).y==84 && menu.getSlot(31).y==142,"Slots do not match authored texture");
        for(int phase=1;phase<=4;phase++) {
            test.assertTrue(table.data.get(0)==phase,"Overlay skipped or reordered");
            int ticks=AugmentationAnimation.stageTicks(phase,table.data.get(2));
            for(int i=1;i<ticks;i++) table.tick();
            test.assertTrue(table.data.get(0)==phase,"Overlay completed too soon");
            test.assertFalse(menu.clickMenuButton(player,0),"Selected before last overlay completed");
            table.tick(); test.assertTrue(table.data.get(0)==phase+1,"Overlay did not complete on time");
        }
        var first=menu.offer(0); var second=menu.offer(1);
        var saved=table.saveWithoutMetadata(test.getLevel().registryAccess()); table.loadWithComponents(saved,test.getLevel().registryAccess());
        test.assertTrue(menu.offer(0).equals(first) && menu.offer(1).equals(second),"Reopen rerolled choices");
        test.assertTrue(menu.clickMenuButton(player,1),"Valid choice rejected");
        test.assertFalse(menu.clickMenuButton(player,0) || menu.clickMenuButton(player,1),"Second viewer/replayed choice consumed another pair");
        test.assertTrue(table.data.get(0)==AugmentationAnimation.COMPLETE && menu.selected()==1 && !menu.getSlot(0).isActive()
                && menu.getSlot(3).isActive() && !menu.getSlot(1).isActive(),"Completed visual state wrong");
        test.assertTrue(table.inventory.getStackInSlot(1).getCount()==2 && table.inventory.getStackInSlot(2).getCount()==2,"Wrong reversed ingredient spending");
        test.assertTrue(Augmentations.level(table.inventory.getStackInSlot(3),second.id())==1 && menu.offerRank(1)==0,"Selected rank/card mismatch");
        saved=table.saveWithoutMetadata(test.getLevel().registryAccess()); table.loadWithComponents(saved,test.getLevel().registryAccess());
        test.assertTrue(menu.selected()==1 && menu.offer(0).equals(first),"Completion lost unselected card after reload");
        menu.clicked(3,0,ClickType.PICKUP,player);
        test.assertTrue(!menu.getCarried().isEmpty() && menu.phase()==0 && menu.getSlot(1).isActive(),"Taking output did not reset");
        menu.setCarried(ItemStack.EMPTY); test.getLevel().getServer().getPlayerList().remove(player); test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void flexibleInputsShiftRoutingAndInvalidPairsNeverStart(GameTestHelper test) {
        var table=table(test); var player=test.makeMockServerPlayerInLevel();
        var menu=new AugmentationTableMenu(0,player.getInventory(),table,table.data);
        for(int side=1;side<=2;side++) {
            test.assertTrue(menu.getSlot(side).mayPlace(new ItemStack(ModItems.SAPPHIRE_GEMSTONE.get()))
                    && menu.getSlot(side).mayPlace(new ItemStack(ModItems.GEMSTONE_UPGRADE_TEMPLATE.get())),"Side slot has fixed ingredient role");
            test.assertFalse(menu.getSlot(side).mayPlace(new ItemStack(Items.DIAMOND)),"Invalid ingredient accepted");
        }
        test.assertFalse(menu.getSlot(3).mayPlace(new ItemStack(ModItems.DEATHWISPER_DAGGER.get())),"Output accepts insertion");
        player.getInventory().setItem(9,new ItemStack(ModItems.SAPPHIRE_GEMSTONE.get(),4));
        menu.quickMoveStack(player,4);
        player.getInventory().setItem(9,new ItemStack(ModItems.GEMSTONE_UPGRADE_TEMPLATE.get(),4));
        menu.quickMoveStack(player,4);
        test.assertTrue(table.inventory.getStackInSlot(1).is(ModItems.SAPPHIRE_GEMSTONE.get())
                && table.inventory.getStackInSlot(2).is(ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()),"Shift-click cannot fill reversed pair");
        table.inventory.setStackInSlot(0,new ItemStack(ModItems.DEATHWISPER_DAGGER.get()));
        table.inventory.setStackInSlot(2,new ItemStack(ModItems.SAPPHIRE_GEMSTONE.get())); table.tick();
        test.assertTrue(menu.phase()==0 && table.inventory.getStackInSlot(1).getCount()==4,"Two sapphires started crafting");
        table.inventory.setStackInSlot(1,new ItemStack(ModItems.GEMSTONE_UPGRADE_TEMPLATE.get(),4));
        table.inventory.setStackInSlot(2,new ItemStack(ModItems.GEMSTONE_UPGRADE_TEMPLATE.get())); table.tick();
        test.assertTrue(menu.phase()==0,"Two templates started crafting");
        table.inventory.setStackInSlot(2,new ItemStack(ModItems.SAPPHIRE_GEMSTONE.get())); table.tick();
        table.inventory.extractItem(1,1,false);
        test.assertTrue(menu.phase()==0 && table.inventory.getStackInSlot(1).getCount()==3,"Early ingredient removal does not cancel safely");
        test.getLevel().getServer().getPlayerList().remove(player); test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void legacyUpfrontPaymentIsReturnedExactlyOnce(GameTestHelper test) {
        var table=table(test);
        table.inventory.setStackInSlot(0,new ItemStack(ModItems.DEATHWISPER_DAGGER.get()));
        table.inventory.setStackInSlot(1,new ItemStack(ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()));
        table.inventory.setStackInSlot(2,new ItemStack(ModItems.SAPPHIRE_GEMSTONE.get()));
        var saved=table.saveWithoutMetadata(test.getLevel().registryAccess()); saved.remove("workflow"); saved.putInt("phase",2);
        table.loadWithComponents(saved,test.getLevel().registryAccess());
        // Persist once more before recovery: the legacy payment marker must survive this too.
        saved=table.saveWithoutMetadata(test.getLevel().registryAccess()); table.loadWithComponents(saved,test.getLevel().registryAccess()); table.tick();
        test.assertTrue(table.inventory.getStackInSlot(1).getCount()==2 && table.inventory.getStackInSlot(2).getCount()==2,"Old spent pair not returned");
        saved=table.saveWithoutMetadata(test.getLevel().registryAccess()); table.loadWithComponents(saved,test.getLevel().registryAccess()); table.tick();
        test.assertTrue(table.inventory.getStackInSlot(1).getCount()==2 && table.inventory.getStackInSlot(2).getCount()==2,"Old refund duplicated");
        test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void breakingDuringSelectionDropsUnspentInputsOnce(GameTestHelper test) {
        var table=table(test); fill(table,false); advance(table,AugmentationAnimation.CHOOSE);
        test.getLevel().setBlockAndUpdate(table.getBlockPos(),net.minecraft.world.level.block.Blocks.AIR.defaultBlockState());
        var drops=test.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,
                new net.minecraft.world.phys.AABB(table.getBlockPos()).inflate(2));
        int weapons=0,sapphires=0,templates=0;
        for(var drop:drops) {
            ItemStack item=drop.getItem();
            if(item.is(ModItems.DEATHWISPER_DAGGER_JADE.get())) { weapons+=item.getCount(); test.assertTrue(Augmentations.count(item)==0,"Breaking augmented unfinished weapon"); }
            if(item.is(ModItems.SAPPHIRE_GEMSTONE.get())) sapphires+=item.getCount();
            if(item.is(ModItems.GEMSTONE_UPGRADE_TEMPLATE.get())) templates+=item.getCount();
        }
        test.assertTrue(weapons==1 && sapphires==3 && templates==3,"Breaking lost/duplicated pending inputs");
        test.succeed();
    }
}
