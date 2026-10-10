package net.bananashelp20.forgermod.gametest;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.custom.SomniumWeapon;
import net.bananashelp20.forgermod.item.custom.abilities.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.tick.*;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder(ForgerMod.MOD_ID) @PrefixGameTestTemplate(false)
public class DelusionGameTests {
 @GameTest(template="riftfang_test")
 public static void allVariantsForgetAggressionRejectFailedHitsAndCancelOnSwitch(GameTestHelper test) throws Exception {
  int count=0;
  for(var item:BuiltInRegistries.ITEM) if(item instanceof SomniumWeapon) {
   count++; var player=TestPlayers.survival(test); player.setInvulnerable(true);
   var fresh=new ItemStack(item); player.setItemInHand(InteractionHand.MAIN_HAND,fresh);
   test.assertFalse(RecommendedAbilityRuntime.activate(player,fresh,RecommendedAbilities.DELUSION),"Fresh weapon learns Delusion");
   var stack=Augmentations.apply(fresh,RecommendedAbilities.DELUSION); player.setItemInHand(InteractionHand.MAIN_HAND,stack);
   var mob=EntityType.ZOMBIE.create(test.getLevel()); mob.setNoAi(true); mob.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD,new ItemStack(net.minecraft.world.item.Items.CARVED_PUMPKIN)); mob.getAttribute(Attributes.ARMOR).setBaseValue(0); mob.setPos(player.position()); test.getLevel().addFreshEntity(mob); mob.setTarget(player);
   AbilityTestPackets.use(player,0); mob.setInvulnerable(true); mob.hurt(player.damageSources().playerAttack(player),1); mob.setInvulnerable(false);
   test.assertTrue(mob.getTarget()==player,"Failed hit consumes charge");
   mob.hurt(player.damageSources().playerAttack(player),1);
   test.assertTrue(mob.getTarget()==null && DelusionEvents.active(player),"Hit does not clear aggression");
   mob.setTarget(player); test.assertTrue(mob.getTarget()==null,"Mob reacquires target while disoriented");
   DelusionEvents.onMobPost(new EntityTickEvent.Post(mob)); test.assertTrue(mob.getLastHurtByMob()==null,"Retaliation memory remains");
   player.setItemInHand(InteractionHand.MAIN_HAND,stack.copy()); WeaponAbilityNetwork.onPlayerTick(new PlayerTickEvent.Pre(player));
   mob.setTarget(player); test.assertTrue(mob.getTarget()==player && !DelusionEvents.active(player),"Switch retains suppression");
   String key=BuiltInRegistries.ITEM.getKey(item)+":"+RecommendedAbilities.DELUSION;
   test.assertTrue(player.getData(WeaponCooldownAttachments.COOLDOWNS).getOrDefault(key,0L)==player.level().getGameTime()+800,"Switch misses cooldown");
   mob.discard(); test.getLevel().getServer().getPlayerList().remove(player);
  }
  test.assertTrue(count==15,"Missing Somnium variants"); test.succeed();
 }
 @GameTest(template="riftfang_test",timeoutTicks=450)
 public static void suppressionEndsAfterTwentySeconds(GameTestHelper test) throws Exception {
  var player=TestPlayers.survival(test); player.setInvulnerable(true);
  var item=net.bananashelp20.forgermod.item.ModItems.DREAMBOUND_CLAYMORE.get();
  var stack=Augmentations.apply(new ItemStack(item),RecommendedAbilities.DELUSION); player.setItemInHand(InteractionHand.MAIN_HAND,stack);
  var mob=EntityType.ZOMBIE.create(test.getLevel()); mob.setNoAi(true); mob.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD,new ItemStack(net.minecraft.world.item.Items.CARVED_PUMPKIN)); mob.setNoGravity(true); mob.setPos(player.position()); test.getLevel().addFreshEntity(mob);
  AbilityTestPackets.use(player,0); mob.hurt(player.damageSources().playerAttack(player),1);
  test.runAfterDelay(399,()-> { test.assertTrue(DelusionEvents.active(player),"Suppression ended early"); mob.setTarget(player); test.assertTrue(mob.getTarget()==null,"Early reacquisition"); });
  test.runAfterDelay(401,()-> { WeaponAbilityNetwork.onPlayerTick(new PlayerTickEvent.Pre(player)); mob.setTarget(player);
   test.assertTrue(!DelusionEvents.active(player) && mob.getTarget()==player,"Suppression does not expire");
   mob.discard(); test.getLevel().getServer().getPlayerList().remove(player); test.succeed(); });
 }
}
