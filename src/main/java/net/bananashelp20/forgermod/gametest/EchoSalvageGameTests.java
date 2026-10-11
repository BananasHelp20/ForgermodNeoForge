package net.bananashelp20.forgermod.gametest;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.custom.PulsiteWeapon;
import net.bananashelp20.forgermod.item.custom.abilities.*;
import net.bananashelp20.forgermod.item.custom.attacks.axe.AxeHeavyNetwork;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder(ForgerMod.MOD_ID) @PrefixGameTestTemplate(false)
public class EchoSalvageGameTests {
 private static void slam(ServerPlayer player,BlockPos center) throws Exception {
  var pending=Class.forName(AxeHeavyNetwork.class.getName()+"$PendingSlam"); var constructor=pending.getDeclaredConstructor(ServerLevel.class,Item.class,long.class,BlockPos.class,float.class); constructor.setAccessible(true);
  var land=AxeHeavyNetwork.class.getDeclaredMethod("landSlam",ServerPlayer.class,pending); land.setAccessible(true);
  land.invoke(null,player,constructor.newInstance(player.serverLevel(),player.getMainHandItem().getItem(),player.level().getGameTime(),center,10F));
 }
 private static ServerPlayer player(GameTestHelper test,BlockPos center) {
  var player=TestPlayers.survival(test); player.setInvulnerable(true); player.setNoGravity(true); player.setPos(Vec3.atBottomCenterOf(center.north(2))); player.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(100);
  for(int x=-2;x<=2;x++) for(int y=0;y<=3;y++) for(int z=-3;z<=3;z++) test.getLevel().setBlockAndUpdate(center.offset(x,y,z),Blocks.AIR.defaultBlockState());
  test.getLevel().setBlockAndUpdate(center.below(),Blocks.STONE.defaultBlockState()); return player;
 }
 private static net.minecraft.world.entity.monster.Zombie enemy(GameTestHelper test,BlockPos center) {
  var mob=EntityType.ZOMBIE.create(test.getLevel()); mob.setNoAi(true); mob.setNoGravity(true); mob.setItemSlot(EquipmentSlot.HEAD,new ItemStack(Items.CARVED_PUMPKIN)); mob.getAttribute(Attributes.ARMOR).setBaseValue(0); mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000); mob.setHealth(1000); mob.setPos(Vec3.atBottomCenterOf(center)); test.getLevel().addFreshEntity(mob); return mob;
 }
 @GameTest(template="riftfang_test")
 public static void allAxeVariantsStoreSolidMissAndReleaseOnceOnRealSlam(GameTestHelper test) throws Exception {
  int count=0; var center=test.absolutePos(new BlockPos(8,5,8));
  for(var item:BuiltInRegistries.ITEM) if(item instanceof PulsiteWeapon weapon && weapon.isAxe()) {
   count++; var player=player(test,center); var fresh=new ItemStack(item); player.setItemInHand(InteractionHand.MAIN_HAND,fresh); slam(player,center); test.assertTrue(EchoSalvageEvents.bonus(player)==0,"Fresh axe stores echo");
   var stack=Augmentations.apply(fresh,RecommendedAbilities.ECHO_SALVAGE); player.setItemInHand(InteractionHand.MAIN_HAND,stack);
   test.getLevel().setBlockAndUpdate(center.below(),Blocks.AIR.defaultBlockState()); slam(player,center); test.assertTrue(EchoSalvageEvents.bonus(player)==0,"Empty air stores echo");
   test.getLevel().setBlockAndUpdate(center.below(),Blocks.STONE.defaultBlockState()); slam(player,center); slam(player,center); test.assertTrue(EchoSalvageEvents.bonus(player)==2,"Rank-I cap or no stacking broken");
   var mob=enemy(test,center); mob.setInvulnerable(true); slam(player,center); mob.setInvulnerable(false); test.assertTrue(EchoSalvageEvents.bonus(player)==2,"Rejected hit consumes echo");
   mob.hurt(player.damageSources().playerAttack(player),1); test.assertTrue(EchoSalvageEvents.bonus(player)==2,"Ordinary hit consumes echo");
   mob.invulnerableTime=0; slam(player,center); test.assertTrue(mob.getHealth()==987 && EchoSalvageEvents.bonus(player)==0,"Real vertical strike lacks exact one-time bonus");
   slam(player,center); test.assertTrue(mob.getHealth()==977,"Echo releases twice");
   mob.discard(); test.getLevel().getServer().getPlayerList().remove(player);
  }
  test.assertTrue(count==5,"Missing axe variants"); test.succeed();
 }
 @GameTest(template="riftfang_test",timeoutTicks=650)
 public static void rankCapCircularSwingSwitchAndExpiryRemainIndependent(GameTestHelper test) throws Exception {
  var center=test.absolutePos(new BlockPos(8,5,8)); var player=player(test,center); var stack=new ItemStack(net.bananashelp20.forgermod.item.ModItems.ECHOING_AXE.get());
  for(int i=0;i<4;i++) stack=Augmentations.apply(stack,RecommendedAbilities.ECHO_SALVAGE); stack=Augmentations.apply(stack,RecommendedAbilities.SWING_ATTACK); player.setItemInHand(InteractionHand.MAIN_HAND,stack); slam(player,center);
  test.assertTrue(EchoSalvageEvents.bonus(player)==8,"Rank-IV cap wrong"); var mob=enemy(test,center);
  RecommendedAbilityRuntime.activate(player,stack,RecommendedAbilities.SWING_ATTACK); test.assertTrue(EchoSalvageEvents.bonus(player)==8,"Circular swing releases echo");
  player.setItemInHand(InteractionHand.MAIN_HAND,stack.copy()); EchoSalvageEvents.onTick(new PlayerTickEvent.Post(player)); test.assertTrue(EchoSalvageEvents.bonus(player)==0,"Switch retains echo");
  mob.discard(); slam(player,center); test.assertTrue(EchoSalvageEvents.bonus(player)==8,"New stack cannot store echo");
  test.runAfterDelay(599,()->test.assertTrue(EchoSalvageEvents.bonus(player)==8,"Echo expires early"));
  test.runAfterDelay(601,()-> { EchoSalvageEvents.onTick(new PlayerTickEvent.Post(player)); test.assertTrue(EchoSalvageEvents.bonus(player)==0,"Echo fails to expire");
   test.getLevel().getServer().getPlayerList().remove(player); test.succeed(); });
 }
 @GameTest(template="riftfang_test")
 public static void neutralHitsDoNotGenerateOrSpendEchoAndBonusUsesWeaponDamage(GameTestHelper test) throws Exception {
  var center=test.absolutePos(new BlockPos(8,5,8)); var player=player(test,center); player.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(6);
  var stack=new ItemStack(net.bananashelp20.forgermod.item.ModItems.ECHOING_AXE.get()); for(int i=0;i<4;i++) stack=Augmentations.apply(stack,RecommendedAbilities.ECHO_SALVAGE); player.setItemInHand(InteractionHand.MAIN_HAND,stack);
  var cow=EntityType.COW.create(test.getLevel()); cow.setNoAi(true); cow.setNoGravity(true); cow.setPos(Vec3.atBottomCenterOf(center)); cow.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100); cow.setHealth(100); test.getLevel().addFreshEntity(cow);
  slam(player,center); test.assertTrue(cow.getHealth()==90 && EchoSalvageEvents.bonus(player)==0,"Successful neutral hit incorrectly stores an echo");
  cow.setPos(Vec3.atBottomCenterOf(center.east(5))); slam(player,center); test.assertTrue(EchoSalvageEvents.bonus(player)==3,"Bonus ignores actual weapon damage below cap");
  cow.setPos(Vec3.atBottomCenterOf(center)); cow.invulnerableTime=0; slam(player,center); test.assertTrue(cow.getHealth()==80 && EchoSalvageEvents.bonus(player)==3,"Neutral hit receives/spends enemy-only bonus");
  cow.discard(); var mob=enemy(test,center); slam(player,center); test.assertTrue(mob.getHealth()==987 && EchoSalvageEvents.bonus(player)==0,"Eligible enemy does not release stored half damage");
  mob.discard(); test.getLevel().getServer().getPlayerList().remove(player); test.succeed();
 }

}
