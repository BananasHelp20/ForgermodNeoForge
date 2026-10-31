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
 public static void killWhileVaultOpenUpdatesLiveSlotsWithoutDuplicatingFlower(GameTestHelper test) {
  var player=TestPlayers.survival(test);player.setNoGravity(true);var stack=Augmentations.apply(new ItemStack(net.bananashelp20.forgermod.item.ModItems.OVERGROWN_CLAYMORE.get()),RecommendedAbilities.GRAVE_VAULT);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
  var storage=new SimpleContainer(9);storage.setItem(0,new ItemStack(Items.POPPY,2));GraveVaultEvents.write(stack,storage,player.registryAccess());var menu=new GraveVaultMenu(1,player.getInventory(),player.getInventory().selected);player.containerMenu=menu;
  var pos=test.absolutePos(new BlockPos(8,5,8));test.getLevel().setBlockAndUpdate(pos.below(),Blocks.DIRT.defaultBlockState());test.getLevel().setBlockAndUpdate(pos,Blocks.AIR.defaultBlockState());
  try {
   kill(test,player,pos,true);test.assertTrue(menu.storage.getItem(0).getCount()==1 && GraveVaultEvents.read(stack,player.registryAccess()).getItem(0).getCount()==1,"Open menu retains flower already planted by kill");
   menu.quickMoveStack(player,0);test.assertTrue(menu.storage.getItem(0).isEmpty() && GraveVaultEvents.read(stack,player.registryAccess()).getItem(0).isEmpty() && player.getInventory().countItem(Items.POPPY)==1,"Live-menu extraction duplicates spent flower");
  }finally {player.containerMenu=player.inventoryMenu;player.server.getPlayerList().remove(player);}
  test.succeed();
 }
 @GameTest(template="riftfang_test")
 public static void fifteenVariantsPlantAtEnemyDeathWithoutWastingFlowers(GameTestHelper test) {
  var player=TestPlayers.survival(test);player.setNoGravity(true);var pos=test.absolutePos(new BlockPos(8,5,8));player.setPos(Vec3.atBottomCenterOf(pos.north(2)));int count=0;
  try {for(var item:BuiltInRegistries.ITEM)if(item instanceof LushWeapon) {
   count++;var stack=Augmentations.apply(new ItemStack(item),RecommendedAbilities.GRAVE_VAULT);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
   var storage=new SimpleContainer(9);storage.setItem(0,new ItemStack(Items.POPPY,2));GraveVaultEvents.write(stack,storage,player.registryAccess());
   test.getLevel().setBlockAndUpdate(pos.below(),Blocks.DIRT.defaultBlockState());test.getLevel().setBlockAndUpdate(pos,Blocks.AIR.defaultBlockState());
   kill(test,player,pos,true);test.assertTrue(test.getLevel().getBlockState(pos).is(Blocks.POPPY) && GraveVaultEvents.read(stack,player.registryAccess()).getItem(0).getCount()==1,"Enemy kill did not place/consume one flower at feet");
   var refill=GraveVaultEvents.read(stack,player.registryAccess());refill.setItem(0,new ItemStack(Items.POPPY,2));GraveVaultEvents.write(stack,refill,player.registryAccess());
   test.getLevel().setBlockAndUpdate(pos,Blocks.AIR.defaultBlockState());test.getLevel().setBlockAndUpdate(pos.below(),Blocks.FARMLAND.defaultBlockState());kill(test,player,pos,true,-.0625);
   test.assertTrue(test.getLevel().getBlockState(pos).is(Blocks.POPPY) && GraveVaultEvents.read(stack,player.registryAccess()).getItem(0).getCount()==1,"Fractional farmland standing height places flower in wrong block");
   test.getLevel().setBlockAndUpdate(pos,Blocks.AIR.defaultBlockState());test.getLevel().setBlockAndUpdate(pos.below(),Blocks.STONE.defaultBlockState());kill(test,player,pos,true);
   test.assertTrue(test.getLevel().getBlockState(pos).isAir() && GraveVaultEvents.read(stack,player.registryAccess()).getItem(0).getCount()==1,"Invalid soil consumes flower");
   test.getLevel().setBlockAndUpdate(pos.below(),Blocks.DIRT.defaultBlockState());kill(test,player,pos,false);test.assertTrue(test.getLevel().getBlockState(pos).isAir(),"Neutral kill plants flower");
   player.setGameMode(GameType.ADVENTURE);kill(test,player,pos,true);test.assertTrue(test.getLevel().getBlockState(pos).isAir() && GraveVaultEvents.read(stack,player.registryAccess()).getItem(0).getCount()==1,"Adventure kill bypasses placement permissions");player.setGameMode(GameType.SURVIVAL);
  }}finally {player.server.getPlayerList().remove(player);}
  test.assertTrue(count==15,"Missing Grave Vault variants");test.succeed();
 }
 private static void kill(GameTestHelper test,net.minecraft.server.level.ServerPlayer player,BlockPos pos,boolean hostile) {kill(test,player,pos,hostile,0);}
 private static void kill(GameTestHelper test,net.minecraft.server.level.ServerPlayer player,BlockPos pos,boolean hostile,double yOffset) {
  var mob=hostile?net.minecraft.world.entity.EntityType.ZOMBIE.create(test.getLevel()):net.minecraft.world.entity.EntityType.COW.create(test.getLevel());mob.setNoAi(true);mob.setNoGravity(true);mob.setPos(Vec3.atBottomCenterOf(pos).add(0,yOffset,0));test.getLevel().addFreshEntity(mob);
  try {mob.hurt(player.damageSources().playerAttack(player),100);}finally {mob.discard();}
 }
 @GameTest(template="riftfang_test")
 public static void allVariantsStoreNineSlotsPersistAndProtectWeapon(GameTestHelper test) {
  int count=0;
  for(var item:BuiltInRegistries.ITEM) if(item instanceof LushWeapon) {
   count++; var player=TestPlayers.survival(test); var stack=Augmentations.apply(new ItemStack(item),RecommendedAbilities.GRAVE_VAULT); player.setItemInHand(InteractionHand.MAIN_HAND,stack);
   var menu=new GraveVaultMenu(1,player.getInventory(),player.getInventory().selected);
   test.assertTrue(menu.slots.size()==45 && menu.getSlot(0).y==18 && menu.getSlot(9).y==49 && menu.getSlot(36).y==107,"Menu does not match vanilla one-row chest coordinates");
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
 public static void allVariantsNeverPlantOrConsumeFlowersOnOrdinaryBlockClicks(GameTestHelper test){
  var player=TestPlayers.survival(test);player.setNoGravity(true);player.setShiftKeyDown(false);
  var soil=test.absolutePos(new BlockPos(8,4,8));player.setPos(Vec3.atBottomCenterOf(soil.north(2).above()));
  var hit=new BlockHitResult(Vec3.atCenterOf(soil).add(0,.5,0),Direction.UP,soil,false);int count=0;
  try{for(var item:BuiltInRegistries.ITEM)if(item instanceof LushWeapon){
   count++;
   for(var mode:new GameType[]{GameType.SURVIVAL,GameType.CREATIVE})for(boolean open:new boolean[]{false,true}){
    player.containerMenu=player.inventoryMenu;player.setGameMode(mode);
    var stack=Augmentations.apply(new ItemStack(item),RecommendedAbilities.GRAVE_VAULT);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
    var storage=new SimpleContainer(9);storage.setItem(0,new ItemStack(Items.POPPY,3));GraveVaultEvents.write(stack,storage,player.registryAccess());
    var menu=open?new GraveVaultMenu(1,player.getInventory(),player.getInventory().selected):null;if(open)player.containerMenu=menu;
    test.getLevel().setBlockAndUpdate(soil,Blocks.DIRT.defaultBlockState());test.getLevel().setBlockAndUpdate(soil.above(),Blocks.AIR.defaultBlockState());
    var event=new PlayerInteractEvent.RightClickBlock(player,InteractionHand.MAIN_HAND,soil,hit);GraveVaultEvents.onBlock(event);
    test.assertTrue(!event.isCanceled() && test.getLevel().getBlockState(soil.above()).isAir(),"Normal block interaction plants a flower or is intercepted");
    test.assertTrue(GraveVaultEvents.read(stack,player.registryAccess()).getItem(0).getCount()==3 && (!open || menu.storage.getItem(0).getCount()==3) && player.getMainHandItem()==stack,"Normal click changes saved/live flower count or held weapon");
   }
  }}finally{player.containerMenu=player.inventoryMenu;player.server.getPlayerList().remove(player);}
  test.assertTrue(count==15,"Skipped Grave Vault variants");test.succeed();
 }
 @GameTest(template="riftfang_test")
 public static void approvedDescriptionIsGrayAndSneakingStillOpensVault(GameTestHelper test){
  var player=TestPlayers.survival(test);var stack=Augmentations.apply(new ItemStack(net.bananashelp20.forgermod.item.ModItems.OVERGROWN_CLAYMORE.get()),RecommendedAbilities.GRAVE_VAULT);player.setItemInHand(InteractionHand.MAIN_HAND,stack);player.setShiftKeyDown(true);
  // Advertise the real NeoForge menu channel on this simulated client connection.
  net.neoforged.neoforge.network.registration.ChannelAttributes.getOrCreateCommonChannels(player.connection.getConnection(),net.minecraft.network.ConnectionProtocol.PLAY)
      .add(net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("neoforge","advanced_open_screen"));
  try{
   for(boolean compact:new boolean[]{false,true}){
    var text=AbilityDescriptions.description(stack,RecommendedAbilities.GRAVE_VAULT,1,compact);
    test.assertTrue(text.getString().equals(compact?"Sneak-right-click: nine-slot flower vault. Enemy kills plant stored flowers.":"Sneak-right-click to open a nine-slot flower vault. Enemy kills plant a stored flower where the enemy stood, if possible."),"Wrong Grave Vault description");
    text.visit((style,part)->{test.assertTrue(style.getColor()!=null && style.getColor().getValue()==net.minecraft.ChatFormatting.GRAY.getColor(),"Unexpected highlight in Grave Vault description");return java.util.Optional.<Void>empty();},net.minecraft.network.chat.Style.EMPTY);
   }
   var air=new PlayerInteractEvent.RightClickItem(player,InteractionHand.MAIN_HAND);GraveVaultEvents.onAir(air);
   test.assertTrue(air.isCanceled() && player.containerMenu instanceof GraveVaultMenu,"Sneak-right-click air no longer opens vault");
   player.closeContainer();
   var pos=test.absolutePos(new BlockPos(8,4,8));var hit=new BlockHitResult(Vec3.atCenterOf(pos),Direction.UP,pos,false);
   var block=new PlayerInteractEvent.RightClickBlock(player,InteractionHand.MAIN_HAND,pos,hit);GraveVaultEvents.onBlock(block);
   test.assertTrue(block.isCanceled() && player.containerMenu instanceof GraveVaultMenu,"Sneak-right-click block no longer opens vault");
  }finally{player.closeContainer();player.server.getPlayerList().remove(player);}
  test.succeed();
 }
 @GameTest(template="riftfang_test")
 public static void finiteStockDepletesForKillsInSurvivalAndCreative(GameTestHelper test) {
  var player=TestPlayers.survival(test);player.setNoGravity(true);var soil=test.absolutePos(new BlockPos(8,4,8));player.setPos(Vec3.atBottomCenterOf(soil.north(2).above()));
  var hit=new BlockHitResult(Vec3.atCenterOf(soil).add(0,.5,0),Direction.UP,soil,false);
  try {
   for(var mode:new GameType[]{GameType.SURVIVAL,GameType.CREATIVE})for(boolean open:new boolean[]{false,true}) {
    player.containerMenu=player.inventoryMenu;player.setGameMode(mode);
    var weapon=Augmentations.apply(new ItemStack(net.bananashelp20.forgermod.item.ModItems.OVERGROWN_CLAYMORE.get()),RecommendedAbilities.GRAVE_VAULT);player.setItemInHand(InteractionHand.MAIN_HAND,weapon);
    var stored=new SimpleContainer(9);stored.setItem(0,new ItemStack(Items.POPPY,2));GraveVaultEvents.write(weapon,stored,player.registryAccess());
    var menu=open?new GraveVaultMenu(1,player.getInventory(),player.getInventory().selected):null;if(open)player.containerMenu=menu;
    test.getLevel().setBlockAndUpdate(soil,Blocks.DIRT.defaultBlockState());
    for(int placed=0;placed<3;placed++) {
     test.getLevel().setBlockAndUpdate(soil.above(),Blocks.AIR.defaultBlockState());
     kill(test,player,soil.above(),true);
     int remaining=Math.max(0,1-placed);
     test.assertTrue(GraveVaultEvents.read(weapon,player.registryAccess()).getItem(0).getCount()==remaining,"Saved flower count does not deplete exactly once: "+mode+" open="+open);
     if(open)test.assertTrue(menu.storage.getItem(0).getCount()==remaining,"Open vault count is stale");
     test.assertTrue(test.getLevel().getBlockState(soil.above()).is(Blocks.POPPY)==(placed<2),"Empty vault produces another flower");
    }
    if(open){menu.storage.setChanged();test.assertTrue(GraveVaultEvents.read(weapon,player.registryAccess()).getItem(0).isEmpty(),"Menu write restores spent flowers");}
    var restored=ItemStack.parse(player.registryAccess(),weapon.save(player.registryAccess())).orElseThrow();test.assertTrue(GraveVaultEvents.read(restored,player.registryAccess()).isEmpty(),"Save/load restores spent flowers");
   }
  }finally {player.containerMenu=player.inventoryMenu;player.server.getPlayerList().remove(player);}
  test.succeed();
 }

}
