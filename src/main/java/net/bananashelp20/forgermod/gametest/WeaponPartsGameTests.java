package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.item.ModItems;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.Arrays;

@GameTestHolder(ForgerMod.MOD_ID) @PrefixGameTestTemplate(false)
public class WeaponPartsGameTests {
    @GameTest(template="riftfang_test")
    public static void renamedBladeAndNewWeaponAssembly(GameTestHelper test) {
        test.assertTrue(BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("forgermod", "sharpened_blade"))
                == ModItems.CLAYMORE_BLADE.get(), "Old saved blade no longer resolves");
        test.assertTrue(new ItemStack(ModItems.AUGMENTED_HEAVY_CORE.get()).hasFoil(), "Core needs enchantments to shine");
        test.assertFalse(new ItemStack(net.minecraft.world.item.Items.HEAVY_CORE).hasFoil(), "Vanilla core was changed");
        var b=ModItems.AXE_BLADE.get();var c=ModItems.AUGMENTED_HEAVY_CORE.get();var h=ModItems.ADVANCED_HANDLE.get();
        check(test,"axe",3,3,ModItems.CARBON_STEEL_AXE.get(),b,c,b,null,h,null,null,h,null);
        check(test,"knife",1,3,ModItems.CARBON_STEEL_DAGGER.get(),ModItems.DAGGER_BLADE.get(),ModItems.CARBON_STEEL_CROSS_GUARD.get(),h);
        check(test,"claymore_from_simple_crafting",1,3,ModItems.CLAYMORE.get(),ModItems.CLAYMORE_BLADE.get(),ModItems.CARBON_STEEL_CROSS_GUARD.get(),h);
        check(test,"dagger_blade",1,2,ModItems.DAGGER_BLADE.get(),ModItems.DAMASK_INGOT.get(),ModItems.CARBON_STEEL_INGOT.get());
        check(test,"axe_blade",2,2,b,ModItems.DAMASK_INGOT.get(),ModItems.CARBON_STEEL_INGOT.get(),null,ModItems.CARBON_STEEL_INGOT.get());
        test.succeed();
    }

    @GameTest(template="riftfang_test")
    public static void coreAugmentsAutomaticallyAndConsumesExactlyOnePair(GameTestHelper test) {
        var pos=test.absolutePos(new net.minecraft.core.BlockPos(1,2,1));
        test.getLevel().setBlockAndUpdate(pos,net.bananashelp20.forgermod.block.ModBlocks.AUGMENTATION_TABLE.get().defaultBlockState());
        var table=(net.bananashelp20.forgermod.block.entity.custom.AugmentationTableBlockEntity)test.getLevel().getBlockEntity(pos);
        var player=TestPlayers.survival(test);
        try {
            player.setPos(pos.getCenter());
            var menu=new net.bananashelp20.forgermod.screen.custom.AugmentationTableMenu(1,player.getInventory(),table,table.data);
            player.getInventory().setItem(9,new ItemStack(net.minecraft.world.item.Items.HEAVY_CORE,2));
            test.assertFalse(menu.quickMoveStack(player,4).isEmpty(),"Shift-click rejects vanilla core");
            test.assertTrue(table.inventory.getStackInSlot(0).getCount()==1 && player.getInventory().getItem(9).getCount()==1,"Core stack limit not respected");
            // Both ingredient orders use the normal table payment and animation.
            for(boolean reversed:new boolean[]{false,true}) {
                if(table.inventory.getStackInSlot(0).isEmpty())table.inventory.setStackInSlot(0,new ItemStack(net.minecraft.world.item.Items.HEAVY_CORE));
                table.inventory.setStackInSlot(1,new ItemStack(reversed?ModItems.SAPPHIRE_GEMSTONE.get():ModItems.GEMSTONE_UPGRADE_TEMPLATE.get(),3));
                table.inventory.setStackInSlot(2,new ItemStack(reversed?ModItems.GEMSTONE_UPGRADE_TEMPLATE.get():ModItems.SAPPHIRE_GEMSTONE.get(),3));
                table.tick();
                test.assertTrue(table.data.get(0)==1 && table.data.get(3)==0 && table.data.get(4)==0,"Core did not start without offers");
                int budget=5000;
                while(table.data.get(0)!=6 && --budget>0) {
                    test.assertTrue(table.inventory.getStackInSlot(1).getCount()==3 && table.inventory.getStackInSlot(2).getCount()==3,"Early payment");
                    test.assertFalse(table.choose(0),"Core offers weapon augments");
                    if(table.data.get(0)==3 && table.data.get(1)==0) {
                        var saved=table.saveWithoutMetadata(test.getLevel().registryAccess());
                        table.loadWithComponents(saved,test.getLevel().registryAccess());
                    }
                    table.tick();
                }
                test.assertTrue(budget>0 && table.inventory.getStackInSlot(3).is(ModItems.AUGMENTED_HEAVY_CORE.get()),"Core never completes automatically");
                test.assertTrue(table.inventory.getStackInSlot(0).isEmpty() && table.inventory.getStackInSlot(1).getCount()==2 && table.inventory.getStackInSlot(2).getCount()==2,"Incorrect core payment");
                for(int i=0;i<30;i++)table.tick();
                test.assertTrue(table.inventory.getStackInSlot(3).getCount()==1 && table.inventory.getStackInSlot(1).getCount()==2,"Completed core repeats or duplicates");
                test.assertFalse(table.inventory.isItemValid(0,new ItemStack(ModItems.AUGMENTED_HEAVY_CORE.get())),"Finished core can be augmented again");
                table.inventory.extractItem(3,1,false);
                test.assertTrue(table.data.get(0)==0,"Taking core doesn't reset GUI");
            }
        } finally {player.server.getPlayerList().remove(player);}
        test.succeed();
    }

    @GameTest(template="riftfang_test")
    public static void unfinishedCoreCancellationNeverConsumesMaterials(GameTestHelper test) {
        var pos=test.absolutePos(new net.minecraft.core.BlockPos(1,2,1));
        test.getLevel().setBlockAndUpdate(pos,net.bananashelp20.forgermod.block.ModBlocks.AUGMENTATION_TABLE.get().defaultBlockState());
        var table=(net.bananashelp20.forgermod.block.entity.custom.AugmentationTableBlockEntity)test.getLevel().getBlockEntity(pos);
        for(int phase=1;phase<=4;phase++) {
            table.inventory.setStackInSlot(0,new ItemStack(net.minecraft.world.item.Items.HEAVY_CORE));
            table.tick();test.assertTrue(table.data.get(0)==0,"Starts without ingredients");
            table.inventory.setStackInSlot(1,new ItemStack(ModItems.SAPPHIRE_GEMSTONE.get()));
            table.inventory.setStackInSlot(2,new ItemStack(ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()));
            table.tick();int budget=5000;
            while(table.data.get(0)<phase && --budget>0)table.tick();
            test.assertTrue(budget>0,"Core phase not reached");
            test.assertTrue(table.inventory.extractItem(0,1,false).is(net.minecraft.world.item.Items.HEAVY_CORE),"Cannot cancel core");
            test.assertTrue(table.data.get(0)==0 && table.inventory.getStackInSlot(3).isEmpty(),"Cancellation produced result");
            test.assertTrue(table.inventory.extractItem(1,1,false).is(ModItems.SAPPHIRE_GEMSTONE.get()) && table.inventory.extractItem(2,1,false).is(ModItems.GEMSTONE_UPGRADE_TEMPLATE.get()),"Cancellation lost materials");
        }
        test.succeed();
    }

    private static void check(GameTestHelper test,String id,int width,int height,Item result,Item... items) {
        var input=CraftingInput.of(width,height,Arrays.stream(items).map(i->i==null?ItemStack.EMPTY:new ItemStack(i)).toList());
        var recipe=(CraftingRecipe)test.getLevel().getRecipeManager().byKey(ResourceLocation.fromNamespaceAndPath("forgermod",id)).orElseThrow().value();
        test.assertTrue(recipe.matches(input,test.getLevel()),"Recipe doesn't match: "+id);
        var output=recipe.assemble(input,test.getLevel().registryAccess());
        test.assertTrue(output.is(result)&&output.getCount()==1,"Wrong assembly output: "+id);
        // Every required component must actually be present.
        for(int i=0;i<input.size();i++) {
            if(input.getItem(i).isEmpty())continue;
            var missing=new java.util.ArrayList<ItemStack>();
            for(int j=0;j<input.size();j++)missing.add(i==j?ItemStack.EMPTY:input.getItem(j));
            test.assertFalse(recipe.matches(CraftingInput.of(width,height,missing),test.getLevel()),"Recipe ignores required slot "+i+": "+id);
        }
    }
}
