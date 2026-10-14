package net.bananashelp20.forgermod.gametest;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.LushWeapon;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder(ForgerMod.MOD_ID) @PrefixGameTestTemplate(false)
public class LushJungleGameTests {
 @GameTest(template="riftfang_test")
 public static void fifteenVariantsBonemealBeneathEnemyKillOnly(GameTestHelper test) {
  var player=TestPlayers.survival(test);player.setNoGravity(true);var pos=test.absolutePos(new BlockPos(8,5,8));player.setPos(Vec3.atBottomCenterOf(pos.north(2)));int count=0;
  try {for(var entry:ModItems.ITEMS.getEntries()) {
   var fresh=new ItemStack(entry.get());boolean eligible=entry.get() instanceof LushWeapon;
   test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(a->a.id().equals(AdditionalAbilities.LUSH_JUNGLE))==eligible,"Wrong Lush Jungle assignment");if(!eligible)continue;count++;
   test.getLevel().setBlockAndUpdate(pos.below(2),Blocks.FARMLAND.defaultBlockState());test.getLevel().setBlockAndUpdate(pos.below(),Blocks.WHEAT.defaultBlockState());
   player.setItemInHand(InteractionHand.MAIN_HAND,fresh);kill(test,player,pos,true);test.assertTrue(test.getLevel().getBlockState(pos.below()).getValue(CropBlock.AGE)==0,"Fresh weapon fertilizes");
   var stack=Augmentations.apply(fresh,AdditionalAbilities.LUSH_JUNGLE);player.setItemInHand(InteractionHand.MAIN_HAND,stack);kill(test,player,pos,true);
   test.assertTrue(test.getLevel().getBlockState(pos.below()).getValue(CropBlock.AGE)>0,"Killed enemy does not fertilize block beneath");
   test.getLevel().setBlockAndUpdate(pos.below(),Blocks.WHEAT.defaultBlockState());kill(test,player,pos,false);test.assertTrue(test.getLevel().getBlockState(pos.below()).getValue(CropBlock.AGE)==0,"Neutral kill fertilizes");
   test.getLevel().setBlockAndUpdate(pos.below(),Blocks.STONE.defaultBlockState());kill(test,player,pos,true);test.assertTrue(test.getLevel().getBlockState(pos.below()).is(Blocks.STONE),"Non-bonemealable terrain changed");
   test.getLevel().setBlockAndUpdate(pos.below(),Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE,7));kill(test,player,pos,true);test.assertTrue(test.getLevel().getBlockState(pos.below()).getValue(CropBlock.AGE)==7,"Mature crop changed");
  }}finally {player.server.getPlayerList().remove(player);}
  test.assertTrue(count==15,"Skipped Lush variants");test.succeed();
 }
 private static void kill(GameTestHelper test,net.minecraft.server.level.ServerPlayer player,BlockPos pos,boolean hostile) {
  var mob=hostile?EntityType.ZOMBIE.create(test.getLevel()):EntityType.COW.create(test.getLevel());mob.setNoAi(true);mob.setNoGravity(true);mob.setPos(Vec3.atBottomCenterOf(pos));test.getLevel().addFreshEntity(mob);
  try {mob.hurt(player.damageSources().playerAttack(player),100);}finally {mob.discard();}
 }
}
