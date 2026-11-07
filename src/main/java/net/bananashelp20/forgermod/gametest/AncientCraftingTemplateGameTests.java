package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.block.ModBlocks;
import net.bananashelp20.forgermod.block.entity.custom.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.AncientCraftingTemplateItem;
import net.bananashelp20.forgermod.screen.custom.WeaponAssemblerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public final class AncientCraftingTemplateGameTests {
    @GameTest(template="riftfang_test")
    public static void specializationsAreExclusiveAndNeverTransferToWeaponAugmentTemplates(GameTestHelper test){
        var blank=new ItemStack(ModItems.ANCIENT_UPGRADE_TEMPLATE.get(),3);
        blank.set(DataComponents.CUSTOM_NAME,Component.literal("Ancient design"));
        test.assertTrue(Augmentations.available(blank).size()==3 && !blank.hasFoil(),"Blank template choices/foil incorrect");
        for(var choice:AncientCraftingTemplateItem.SPECIALIZATIONS){
            var filled=Augmentations.apply(blank,choice.id());
            test.assertTrue(filled.getCount()==1 && blank.getCount()==3 && AncientCraftingTemplateItem.specialization(blank).isEmpty(),"Specialization mutated input");
            test.assertTrue(filled.hasFoil() && filled.getHoverName().getString().equals("Ancient design"),"Specialization lost name/foil");
            test.assertTrue(Augmentations.learned(filled).equals(java.util.Map.of(choice.id(),1)) && Augmentations.available(filled).isEmpty(),"Template can specialize twice");
            var saved=filled.save(test.getLevel().registryAccess());
            var restored=ItemStack.parse(test.getLevel().registryAccess(),saved).orElseThrow();
            test.assertTrue(AncientCraftingTemplateItem.specialization(restored).equals(choice.id()),"Specialization did not persist");
            for(var other:AncientCraftingTemplateItem.SPECIALIZATIONS){
                test.assertTrue(Augmentations.apply(filled,other.id()).isEmpty(),"Respecialization allowed");
                for(var item:java.util.List.of(ModItems.AUGMENT_TEMPLATE.get(),ModItems.SUPER_AUGMENT_TEMPLATE.get(),ModItems.EMBERFANG_DAGGER.get())){
                    var target=new ItemStack(item);
                    test.assertTrue(Augmentations.apply(target,other.id()).isEmpty() && Augmentations.setLevel(target,other.id(),1).isEmpty(),"Crafting augment leaked to weapon/template");
                    test.assertTrue(Augmentations.merge(target,filled).isEmpty(),"Crafting augment transferred through anvil");
                }
            }
            test.assertTrue(Augmentations.extractFirst(filled,new ItemStack(ModItems.AUGMENT_TEMPLATE.get()))==null,"Crafting specialization extracted");
        }
        test.assertTrue(test.getLevel().getRecipeManager().byKey(ResourceLocation.fromNamespaceAndPath("forgermod","ancient_upgrade_template_from_multiplication_crafting")).isEmpty(),"Duplication recipe still registered");
        test.assertTrue(test.getLevel().getRecipeManager().byKey(ResourceLocation.fromNamespaceAndPath("forgermod","ancient_upgrade_template_from_primal_crafting")).isPresent(),"Original blank-template recipe removed");
        test.succeed();
    }

    @GameTest(template="riftfang_test")
    public static void tableSpecializesOneTemplateForNormalCostAndStops(GameTestHelper test){
        BlockPos pos=test.absolutePos(new BlockPos(1,2,1));
        test.getLevel().setBlockAndUpdate(pos,ModBlocks.AUGMENTATION_TABLE.get().defaultBlockState());
        var table=(AugmentationTableBlockEntity)test.getLevel().getBlockEntity(pos);
        table.inventory.insertItem(0,new ItemStack(ModItems.ANCIENT_UPGRADE_TEMPLATE.get(),2),false);
        table.inventory.setStackInSlot(1,new ItemStack(ModItems.SAPPHIRE_GEMSTONE.get(),2));
        table.inventory.setStackInSlot(2,new ItemStack(ModItems.GEMSTONE_UPGRADE_TEMPLATE.get(),2));
        int budget=1000;
        while(table.data.get(0)!=AugmentationAnimation.CHOOSE && --budget>0)table.tick();
        test.assertTrue(budget>0,"Template did not reach selection");
        String first=AncientCraftingTemplateItem.SPECIALIZATIONS.get(table.data.get(3)-1).id();
        String second=AncientCraftingTemplateItem.SPECIALIZATIONS.get(table.data.get(4)-1).id();
        test.assertFalse(first.equals(second),"Duplicate specializations offered");
        var saved=table.saveWithoutMetadata(test.getLevel().registryAccess());table.loadWithComponents(saved,test.getLevel().registryAccess());
        test.assertTrue(table.choose(0) && !table.choose(1),"Selection failed or replay accepted");
        for(int i=0;i<20;i++)table.tick();
        test.assertTrue(table.data.get(0)==AugmentationAnimation.COMPLETE && AncientCraftingTemplateItem.specialization(table.inventory.getStackInSlot(3)).equals(first),"Completed template restarted");
        test.assertTrue(table.inventory.getStackInSlot(0).isEmpty() && table.inventory.getStackInSlot(1).getCount()==1 && table.inventory.getStackInSlot(2).getCount()==1,"Wrong subject or costs consumed");
        test.succeed();
    }

    @GameTest(template="riftfang_test")
    public static void assemblerValidatesShiftClickPersistsAndDropsExactlyOneTemplate(GameTestHelper test){
        BlockPos pos=test.absolutePos(new BlockPos(1,2,1));
        test.getLevel().setBlockAndUpdate(pos,ModBlocks.WEAPON_ASSEMBLER.get().defaultBlockState());
        var table=(WeaponAssemblerBlockEntity)test.getLevel().getBlockEntity(pos);
        var player=test.makeMockServerPlayerInLevel();player.setPos(pos.getCenter());
        var menu=new WeaponAssemblerMenu(0,player.getInventory(),table);
        test.assertTrue(menu.stillValid(player) && menu.specialization().isEmpty(),"Assembler initial screen invalid");
        for(var item:java.util.List.of(ModItems.ANCIENT_UPGRADE_TEMPLATE.get(),ModItems.AUGMENT_TEMPLATE.get(),ModItems.SUPER_AUGMENT_TEMPLATE.get()))
            test.assertFalse(menu.getSlot(0).mayPlace(new ItemStack(item)),"Assembler accepted unspecialized/weapon template");
        var filled=Augmentations.apply(new ItemStack(ModItems.ANCIENT_UPGRADE_TEMPLATE.get()),"crafting.forgermod.dagger");
        player.getInventory().setItem(9,filled.copyWithCount(2));menu.quickMoveStack(player,1);
        test.assertTrue(menu.specialization().equals("crafting.forgermod.dagger") && player.getInventory().getItem(9).getCount()==1,"Shift-click lost extra template or chose wrong screen");
        var saved=table.saveWithoutMetadata(test.getLevel().registryAccess());table.loadWithComponents(saved,test.getLevel().registryAccess());
        test.assertTrue(ItemStack.matches(filled,table.inventory.getStackInSlot(0)),"Assembler reload lost template");
        test.getLevel().setBlockAndUpdate(pos,Blocks.AIR.defaultBlockState());
        var drops=test.getLevel().getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class,new net.minecraft.world.phys.AABB(pos).inflate(2));
        int count=drops.stream().filter(drop->ItemStack.isSameItemSameComponents(filled,drop.getItem())).mapToInt(drop->drop.getItem().getCount()).sum();
        test.assertTrue(count==1 && !menu.stillValid(player),"Breaking lost/duplicated template or kept menu valid");
        test.getLevel().getServer().getPlayerList().remove(player);test.succeed();
    }
}
