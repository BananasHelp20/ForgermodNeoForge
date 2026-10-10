package net.bananashelp20.forgermod.gametest;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.custom.LushWeapon;
import net.bananashelp20.forgermod.item.custom.abilities.GraveVaultEvents;
import net.bananashelp20.forgermod.screen.custom.GraveVaultMenu;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.*;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.*;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder(ForgerMod.MOD_ID) @PrefixGameTestTemplate(false)
public class GraveVaultGameTests {
 @GameTest(template="riftfang_test")
 public static void allVariantsStoreNineSlotsPersistAndProtectWeapon(GameTestHelper test) {
  int count=0;
  for(var item:BuiltInRegistries.ITEM) if(item instanceof LushWeapon) {
   count++; var player=TestPlayers.survival(test); var stack=Augmentations.apply(new ItemStack(item),RecommendedAbilities.GRAVE_VAULT); player.setItemInHand(InteractionHand.MAIN_HAND,stack);
   var menu=new GraveVaultMenu(1,player.getInventory(),player.getInventory().selected);
   for(int slot=0;slot<9;slot++) { test.assertTrue(menu.getSlot(slot).mayPlace(new ItemStack(Items.POPPY)),"Flower rejected"); test.assertFalse(menu.getSlot(slot).mayPlace(new ItemStack(Items.DIRT)),"Nonflower accepted"); menu.storage.setItem(slot,new ItemStack(Items.POPPY,slot+1)); }
   var saved=ItemStack.parse(player.registryAccess(),stack.save(player.registryAccess())).orElseThrow(); var restored=GraveVaultEvents.read(saved,player.registryAccess());
   for(int slot=0;slot<9;slot++) test.assertTrue(restored.getItem(slot).is(Items.POPPY) && restored.getItem(slot).getCount()==slot+1,"Flower slots not persisted independently");
   test.assertTrue(Augmentations.level(saved,RecommendedAbilities.GRAVE_VAULT)==1 && Augmentations.apply(saved,RecommendedAbilities.GRAVE_VAULT).isEmpty(),"Storage overwrites augmentation or redundant upgrades");
   int weaponSlot=36+player.getInventory().selected;
   menu.clicked(weaponSlot,0,ClickType.PICKUP,player); test.assertTrue(player.getMainHandItem()==stack && menu.getCarried().isEmpty(),"Weapon pickup allows nesting");
   player.getInventory().setItem(9,new ItemStack(Items.DANDELION,7)); menu.quickMoveStack(player,9); test.assertTrue(menu.storage.getItem(0).is(Items.POPPY),"Full bag overwritten");
   menu.quickMoveStack(player,0); test.assertTrue(menu.storage.getItem(0).isEmpty() && GraveVaultEvents.read(stack,player.registryAccess()).getItem(0).isEmpty(),"Extraction not saved");
   menu.clicked(1,player.getInventory().selected,ClickType.SWAP,player); test.assertTrue(player.getMainHandItem()==stack,"Number key removes weapon");
   player.setItemInHand(InteractionHand.MAIN_HAND,stack.copy()); test.assertFalse(menu.stillValid(player),"Copied weapon remains bound");
   test.getLevel().getServer().getPlayerList().remove(player);
  }
  test.assertTrue(count==15,"Missing Lush variants"); test.succeed();
 }
 @GameTest(template="riftfang_test")
 public static void actualPlacementChecksSoilConsumesOneAndRespectsAdventure(GameTestHelper test) {
  var player=TestPlayers.survival(test); var stack=Augmentations.apply(new ItemStack(net.bananashelp20.forgermod.item.ModItems.OVERGROWN_CLAYMORE.get()),RecommendedAbilities.GRAVE_VAULT);
  player.setItemInHand(InteractionHand.MAIN_HAND,stack); var storage=new SimpleContainer(9); storage.setItem(0,new ItemStack(Items.POPPY,3)); GraveVaultEvents.write(stack,storage,player.registryAccess());
  var soil=test.absolutePos(new BlockPos(2,3,2)); player.setPos(Vec3.atBottomCenterOf(soil.south(2).above()));
  test.getLevel().setBlockAndUpdate(soil,Blocks.STONE.defaultBlockState()); test.getLevel().setBlockAndUpdate(soil.above(),Blocks.AIR.defaultBlockState());
  var hit=new BlockHitResult(Vec3.atCenterOf(soil).add(0,.5,0),Direction.UP,soil,false);
  GraveVaultEvents.onBlock(new PlayerInteractEvent.RightClickBlock(player,InteractionHand.MAIN_HAND,soil,hit));
  test.assertTrue(test.getLevel().getBlockState(soil.above()).isAir() && GraveVaultEvents.read(stack,player.registryAccess()).getItem(0).getCount()==3,"Invalid soil consumed flower");
  test.getLevel().setBlockAndUpdate(soil,Blocks.DIRT.defaultBlockState());
  var event=new PlayerInteractEvent.RightClickBlock(player,InteractionHand.MAIN_HAND,soil,hit); GraveVaultEvents.onBlock(event);
  test.assertTrue(event.isCanceled() && test.getLevel().getBlockState(soil.above()).is(Blocks.POPPY),"Suitable terrain placement failed");
  test.assertTrue(GraveVaultEvents.read(stack,player.registryAccess()).getItem(0).getCount()==2 && player.getMainHandItem()==stack,"Placement loses/duplicates flower or weapon");
  test.getLevel().setBlockAndUpdate(soil.above(),Blocks.AIR.defaultBlockState()); player.setGameMode(GameType.ADVENTURE);
  GraveVaultEvents.onBlock(new PlayerInteractEvent.RightClickBlock(player,InteractionHand.MAIN_HAND,soil,hit));
  test.assertTrue(test.getLevel().getBlockState(soil.above()).isAir() && GraveVaultEvents.read(stack,player.registryAccess()).getItem(0).getCount()==2,"Adventure restrictions bypassed");
  test.getLevel().getServer().getPlayerList().remove(player); test.succeed();
 }
}
