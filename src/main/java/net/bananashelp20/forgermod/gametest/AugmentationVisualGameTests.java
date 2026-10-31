package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.block.ModBlocks;
import net.bananashelp20.forgermod.block.entity.custom.AugmentationTableBlockEntity;
import net.bananashelp20.forgermod.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.item.*;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(ForgerMod.MOD_ID) @PrefixGameTestTemplate(false)
public final class AugmentationVisualGameTests {
    private static AugmentationTableBlockEntity table(GameTestHelper test){
        var pos=test.absolutePos(new BlockPos(1,2,1));test.getLevel().setBlockAndUpdate(pos,ModBlocks.AUGMENTATION_TABLE.get().defaultBlockState());
        return (AugmentationTableBlockEntity)test.getLevel().getBlockEntity(pos);
    }
    private static void fill(AugmentationTableBlockEntity table){
        table.inventory.setStackInSlot(0,new ItemStack(ModItems.STATIC_DAGGER_JADE.get()));
        table.inventory.setStackInSlot(1,new ItemStack(ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()));
        table.inventory.setStackInSlot(2,new ItemStack(ModItems.SAPPHIRE_GEMSTONE.get()));table.tick();
    }
    private static AugmentationTableBlockEntity mirror(GameTestHelper test,AugmentationTableBlockEntity table){
        var mirror=new AugmentationTableBlockEntity(table.getBlockPos(),table.getBlockState());
        // GUI slots share this block entity on the client; cosmetic packets must never reload or erase them.
        mirror.inventory.setStackInSlot(0,new ItemStack(ModItems.MOLTEN_AXE.get()));
        mirror.handleUpdateTag(table.getUpdateTag(test.getLevel().registryAccess()),test.getLevel().registryAccess());return mirror;
    }
    @GameTest(template="riftfang_test",batch="augmentation_visual_sync")
    public static void visualPacketsFollowCraftingWithoutLeakingOffersOrChangingInventory(GameTestHelper test){
        var table=table(test);fill(table);var mirror=mirror(test,table);var registries=test.getLevel().registryAccess();
        test.assertTrue(mirror.visuallyWorking() && ItemStack.matches(mirror.displayItem(),table.inventory.getStackInSlot(0)),"Initial chunk tag loses working animation or weapon");
        test.assertTrue(mirror.inventory.getStackInSlot(0).is(ModItems.MOLTEN_AXE.get()) && mirror.data.get(0)==0,"Cosmetic sync changes GUI slots or workflow");
        var tag=table.getUpdateTag(registries);
        test.assertTrue(tag.getAllKeys().size()==2 && !tag.contains("inventory") && !tag.contains("first"),"Visual sync leaks inventory/offers or per-tick progress");
        int budget=10000;while(table.data.get(0)<AugmentationAnimation.CHOOSE && --budget>0)table.tick();
        test.assertTrue(budget>0 && table.choose(0),"Crafting does not reach a legal selection");
        var output=table.inventory.getStackInSlot(AugmentationTableBlockEntity.OUTPUT).copy();
        var packet=table.getUpdatePacket();mirror.onDataPacket(null,packet,registries);
        test.assertTrue(!mirror.visuallyWorking() && ItemStack.matches(mirror.displayItem(),output) && Augmentations.count(output)>0,"Completion packet loses augmented output or keeps active rings");
        test.assertTrue(mirror.inventory.getStackInSlot(0).is(ModItems.MOLTEN_AXE.get()),"Completion packet replaces GUI inventory");
        table.inventory.extractItem(AugmentationTableBlockEntity.OUTPUT,1,false);
        mirror.onDataPacket(null,table.getUpdatePacket(),registries);
        test.assertTrue(mirror.displayItem().isEmpty() && !mirror.visuallyWorking(),"Taking output leaves a ghost weapon/active animation");
        fill(table);table.inventory.extractItem(AugmentationTableBlockEntity.GEAR,1,false);
        mirror.onDataPacket(null,table.getUpdatePacket(),registries);
        test.assertTrue(mirror.displayItem().isEmpty() && !mirror.visuallyWorking() && table.inventory.getStackInSlot(1).getCount()==1 && table.inventory.getStackInSlot(2).getCount()==1,"Cancel animation retains weapon or spends ingredients");
        test.succeed();
    }
    @GameTest(template="riftfang_test",batch="augmentation_visual_motion")
    public static void activitySmoothsRotationAndReloadedTablesProvideCurrentVisuals(GameTestHelper test){
        var table=table(test);fill(table);var registries=test.getLevel().registryAccess();
        var saved=table.saveWithoutMetadata(registries);
        var restored=new AugmentationTableBlockEntity(table.getBlockPos(),table.getBlockState());restored.loadWithComponents(saved,registries);
        var mirror=new AugmentationTableBlockEntity(table.getBlockPos(),table.getBlockState());mirror.handleUpdateTag(restored.getUpdateTag(registries),registries);
        test.assertTrue(mirror.visuallyWorking() && !mirror.displayItem().isEmpty(),"Reloaded table lacks initial visual snapshot");
        var idle=new AugmentationTableBlockEntity(table.getBlockPos(),table.getBlockState());
        for(int tick=0;tick<60;tick++)idle.clientAnimationTick();
        test.assertTrue(idle.visualActivity(1)==0 && idle.visualSpin(1)==0,"Resting rings still rotate or lift");
        mirror.clientAnimationTick();test.assertTrue(mirror.visualActivity(1)>0 && mirror.visualActivity(1)<1,"Activity animation snaps instantly");
        for(int tick=0;tick<25;tick++)mirror.clientAnimationTick();
        test.assertTrue(mirror.visualActivity(1)==1 && Float.isFinite(mirror.visualSpin(.5F)),"Active animation fails to ramp or yields invalid transform");
        for(int tick=0;tick<4000;tick++){
            float previous=mirror.visualSpin(1);mirror.clientAnimationTick();float next=mirror.visualSpin(1);
            for(float ratio:new float[]{1,-.85F,-1.3F,.2F})
                test.assertTrue(Math.abs(net.minecraft.util.Mth.wrapDegrees((next-previous)*ratio))<4,"Fractional ring/core rotation jumps at a wrap boundary");
        }
        var empty=new net.minecraft.nbt.CompoundTag();empty.putInt("visual_phase",0);mirror.handleUpdateTag(empty,registries);
        mirror.clientAnimationTick();test.assertTrue(mirror.visualActivity(1)>0 && mirror.visualActivity(1)<1 && mirror.displayItem().isEmpty(),"Cancellation fails to fade visual activity");
        for(int tick=0;tick<19;tick++)mirror.clientAnimationTick();
        test.assertTrue(mirror.visualActivity(1)>.45F && mirror.visualActivity(1)<.55F,"Rings do not descend over roughly two seconds");
        for(int tick=0;tick<1000;tick++)mirror.clientAnimationTick();
        test.assertTrue(mirror.visualActivity(1)==0 && Float.isFinite(mirror.visualSpin(.5F)),"Idle/wrapped rotation becomes invalid");
        float restingSpin=mirror.visualSpin(1);for(int tick=0;tick<60;tick++)mirror.clientAnimationTick();
        test.assertTrue(mirror.visualSpin(1)==restingSpin,"Rings keep rotating after settling");test.succeed();
    }
}
