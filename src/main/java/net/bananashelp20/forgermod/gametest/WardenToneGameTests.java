package net.bananashelp20.forgermod.gametest;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.custom.PulsiteWeapon;
import net.bananashelp20.forgermod.item.custom.abilities.WardenToneEvents;
import net.minecraft.core.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.*;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.VanillaGameEvent;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder(ForgerMod.MOD_ID) @PrefixGameTestTemplate(false)
public class WardenToneGameTests {
 @GameTest(template="riftfang_test")
 public static void allVariantsSuppressOnlyLearnedOwnerVibrations(GameTestHelper test) {
  int count=0;
  for(var item:BuiltInRegistries.ITEM) if(item instanceof PulsiteWeapon) {
   count++; var player=TestPlayers.survival(test); var fresh=new ItemStack(item); player.setItemInHand(InteractionHand.MAIN_HAND,fresh);
   var step=new VanillaGameEvent(test.getLevel(),GameEvent.STEP,player.position(),GameEvent.Context.of(player)); NeoForge.EVENT_BUS.post(step); test.assertFalse(step.isCanceled(),"Fresh weapon silences player");
   var stack=Augmentations.apply(fresh,RecommendedAbilities.WARDEN_TONE); player.setItemInHand(InteractionHand.MAIN_HAND,stack);
   for(var event:java.util.List.of(GameEvent.STEP,GameEvent.SWIM,GameEvent.BLOCK_PLACE,GameEvent.PROJECTILE_SHOOT,GameEvent.ENTITY_ACTION)) {
    var sound=new VanillaGameEvent(test.getLevel(),event,player.position(),GameEvent.Context.of(player)); NeoForge.EVENT_BUS.post(sound); test.assertTrue(sound.isCanceled(),"Learned owner event detectable");
   }
   var damage=new VanillaGameEvent(test.getLevel(),GameEvent.ENTITY_DAMAGE,player.position(),GameEvent.Context.of(player)); NeoForge.EVENT_BUS.post(damage); test.assertFalse(damage.isCanceled(),"Damage made silent");
   var arrow=new Arrow(net.minecraft.world.entity.EntityType.ARROW,test.getLevel()); arrow.setOwner(player);
   var projectile=new VanillaGameEvent(test.getLevel(),GameEvent.PROJECTILE_LAND,player.position(),GameEvent.Context.of(arrow)); NeoForge.EVENT_BUS.post(projectile); test.assertTrue(projectile.isCanceled(),"Owned projectile remains detectable");
   var other=new VanillaGameEvent(test.getLevel(),GameEvent.STEP,player.position(),GameEvent.Context.of((net.minecraft.world.entity.Entity)null)); NeoForge.EVENT_BUS.post(other); test.assertFalse(other.isCanceled(),"Unrelated event canceled");
   var cow=net.minecraft.world.entity.EntityType.COW.create(test.getLevel());
   var foreign=new VanillaGameEvent(test.getLevel(),GameEvent.STEP,player.position(),GameEvent.Context.of(cow)); NeoForge.EVENT_BUS.post(foreign); test.assertFalse(foreign.isCanceled(),"Another entity made silent");
   player.setItemInHand(InteractionHand.MAIN_HAND,fresh); var switched=new VanillaGameEvent(test.getLevel(),GameEvent.STEP,player.position(),GameEvent.Context.of(player)); NeoForge.EVENT_BUS.post(switched); test.assertFalse(switched.isCanceled(),"Switch leaves silence");
   test.assertTrue(Augmentations.apply(stack,RecommendedAbilities.WARDEN_TONE).isEmpty(),"Redundant passive upgrade");
   test.getLevel().getServer().getPlayerList().remove(player);
  }
  test.assertTrue(count==15,"Missing Pulsite variants"); test.succeed();
 }
 @GameTest(template="riftfang_test",timeoutTicks=100)
 public static void realSculkSensorIgnoresActionsButDetectsActualDamage(GameTestHelper test) {
  var player=TestPlayers.survival(test); player.setNoGravity(true); player.setInvulnerable(true);
  var pos=test.absolutePos(new BlockPos(8,5,8));
  for(int x=-4;x<=4;x++) for(int y=0;y<=3;y++) for(int z=-4;z<=4;z++) test.getLevel().setBlockAndUpdate(pos.offset(x,y,z),Blocks.AIR.defaultBlockState());
  test.getLevel().setBlockAndUpdate(pos.below(),Blocks.STONE.defaultBlockState()); test.getLevel().setBlockAndUpdate(pos,Blocks.SCULK_SENSOR.defaultBlockState());
  player.setPos(net.minecraft.world.phys.Vec3.atBottomCenterOf(pos.east(3)));
  var stack=Augmentations.apply(new ItemStack(net.bananashelp20.forgermod.item.ModItems.WARDENS_NEEDLE.get()),RecommendedAbilities.WARDEN_TONE); player.setItemInHand(InteractionHand.MAIN_HAND,stack);
  test.runAfterDelay(2,()->player.gameEvent(GameEvent.STEP));
  test.runAfterDelay(10,()-> {
   test.assertTrue(test.getLevel().getBlockState(pos).getValue(SculkSensorBlock.POWER)==0,"Sensor detects muted footsteps");
   player.setInvulnerable(false); player.hurt(player.damageSources().generic(),1); player.setInvulnerable(true);
  });
  test.runAfterDelay(20,()-> {
   test.assertTrue(test.getLevel().getBlockState(pos).getValue(SculkSensorBlock.POWER)>0,"Sensor fails to detect actual damage");
   test.getLevel().getServer().getPlayerList().remove(player); test.succeed();
  });
 }
}
