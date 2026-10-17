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
 private static final class GrowthConnection extends net.minecraft.network.Connection {
  final java.util.List<net.minecraft.network.protocol.game.ClientboundLevelEventPacket> growth=new java.util.ArrayList<>();
  GrowthConnection(){super(net.minecraft.network.protocol.PacketFlow.SERVERBOUND);new io.netty.channel.embedded.EmbeddedChannel(this);}
  @Override public void send(net.minecraft.network.protocol.Packet<?> packet,net.minecraft.network.PacketSendListener listener,boolean flush) {
   if(packet instanceof net.minecraft.network.protocol.game.ClientboundLevelEventPacket event && event.getType()==1505)growth.add(event);
  }
 }

 @GameTest(template="riftfang_test")
 public static void remoteDimensionDeathCannotFertilizeCurrentDimension(GameTestHelper test) {
  var player=TestPlayers.survival(test);player.setItemInHand(InteractionHand.MAIN_HAND,Augmentations.apply(new ItemStack(ModItems.OVERGROWN_CLAYMORE.get()),AdditionalAbilities.LUSH_JUNGLE));
  var pos=test.absolutePos(new BlockPos(8,5,8));test.getLevel().setBlockAndUpdate(pos.below(2),Blocks.FARMLAND.defaultBlockState());test.getLevel().setBlockAndUpdate(pos.below(),Blocks.WHEAT.defaultBlockState());
  var nether=player.server.getLevel(net.minecraft.world.level.Level.NETHER);test.assertTrue(nether!=null,"Nether unavailable");var remote=EntityType.ZOMBIE.create(nether);remote.setPos(Vec3.atBottomCenterOf(pos));var local=EntityType.ZOMBIE.create(test.getLevel());local.setPos(Vec3.atBottomCenterOf(pos));
  try {
   net.bananashelp20.forgermod.item.custom.abilities.LushJungleEvents.kill(new net.neoforged.neoforge.event.entity.living.LivingDeathEvent(remote,player.damageSources().playerAttack(player)));
   test.assertTrue(test.getLevel().getBlockState(pos.below()).getValue(CropBlock.AGE)==0,"Remote kill changes current dimension crop");
   net.bananashelp20.forgermod.item.custom.abilities.LushJungleEvents.kill(new net.neoforged.neoforge.event.entity.living.LivingDeathEvent(local,player.damageSources().playerAttack(player)));
   test.assertTrue(test.getLevel().getBlockState(pos.below()).getValue(CropBlock.AGE)>0,"Local kill no longer fertilizes");
  }finally {remote.discard();local.discard();player.server.getPlayerList().remove(player);}
  test.succeed();
 }
 @GameTest(template="riftfang_test")
 public static void fifteenVariantsBonemealBeneathEnemyKillOnly(GameTestHelper test) {
  var player=TestPlayers.survival(test);player.setNoGravity(true);var pos=test.absolutePos(new BlockPos(8,5,8));player.setPos(Vec3.atBottomCenterOf(pos.north(2)));int count=0;
  var previous=player.connection;var connection=new GrowthConnection();new net.minecraft.server.network.ServerGamePacketListenerImpl(player.server,connection,player,net.minecraft.server.network.CommonListenerCookie.createInitial(player.getGameProfile(),false));
  try {for(var entry:ModItems.ITEMS.getEntries()) {
   var fresh=new ItemStack(entry.get());boolean eligible=entry.get() instanceof LushWeapon;
   test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(a->a.id().equals(AdditionalAbilities.LUSH_JUNGLE))==eligible,"Wrong Lush Jungle assignment");if(!eligible)continue;count++;
   connection.growth.clear();test.getLevel().setBlockAndUpdate(pos.below(2),Blocks.FARMLAND.defaultBlockState());test.getLevel().setBlockAndUpdate(pos.below(),Blocks.WHEAT.defaultBlockState());
   player.setItemInHand(InteractionHand.MAIN_HAND,fresh);kill(test,player,pos,true);test.assertTrue(test.getLevel().getBlockState(pos.below()).getValue(CropBlock.AGE)==0,"Fresh weapon fertilizes");test.assertTrue(connection.growth.isEmpty(),"Fresh weapon emits growth particles");
   var stack=Augmentations.apply(fresh,AdditionalAbilities.LUSH_JUNGLE);player.setItemInHand(InteractionHand.MAIN_HAND,stack);kill(test,player,pos,true);
   test.assertTrue(test.getLevel().getBlockState(pos.below()).getValue(CropBlock.AGE)>0,"Killed enemy does not fertilize block beneath");
   test.assertTrue(connection.growth.size()==1 && connection.growth.getFirst().getData()==15 && connection.growth.getFirst().getPos().equals(pos.below()),"Successful bonemeal event does not request vanilla growth particles at the crop");
   test.getLevel().setBlockAndUpdate(pos.below(),Blocks.WHEAT.defaultBlockState());kill(test,player,pos,false);test.assertTrue(test.getLevel().getBlockState(pos.below()).getValue(CropBlock.AGE)==0,"Neutral kill fertilizes");
   test.getLevel().setBlockAndUpdate(pos.below(),Blocks.STONE.defaultBlockState());kill(test,player,pos,true);test.assertTrue(test.getLevel().getBlockState(pos.below()).is(Blocks.STONE),"Non-bonemealable terrain changed");
   test.getLevel().setBlockAndUpdate(pos.below(),Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE,7));kill(test,player,pos,true);test.assertTrue(test.getLevel().getBlockState(pos.below()).getValue(CropBlock.AGE)==7,"Mature crop changed");test.assertTrue(connection.growth.size()==1,"Failed/neutral bonemeal attempts emit extra particles");
  }}finally {player.connection=previous;player.server.getPlayerList().remove(player);}
  test.assertTrue(count==15,"Skipped Lush variants");test.succeed();
 }
 private static void kill(GameTestHelper test,net.minecraft.server.level.ServerPlayer player,BlockPos pos,boolean hostile) {
  var mob=hostile?EntityType.ZOMBIE.create(test.getLevel()):EntityType.COW.create(test.getLevel());mob.setNoAi(true);mob.setNoGravity(true);mob.setPos(Vec3.atBottomCenterOf(pos));test.getLevel().addFreshEntity(mob);
  try {mob.hurt(player.damageSources().playerAttack(player),100);}finally {mob.discard();}
 }
}
