package net.bananashelp20.forgermod.gametest;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.custom.PulsiteWeapon;
import net.bananashelp20.forgermod.item.custom.abilities.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder(ForgerMod.MOD_ID) @PrefixGameTestTemplate(false)
public class EchoPinGameTests {
 private static ServerPlayer player(GameTestHelper test) {
  var player=TestPlayers.survival(test); player.setInvulnerable(true); player.setNoGravity(true); player.setPos(test.getBounds().getCenter()); player.setYRot(0); player.setXRot(0);
  for(int x=-2;x<=2;x++) for(int y=0;y<=3;y++) for(int z=0;z<=8;z++) test.getLevel().setBlockAndUpdate(player.blockPosition().offset(x,y,z),Blocks.AIR.defaultBlockState());
  return player;
 }
 private static net.minecraft.world.entity.monster.Zombie enemy(GameTestHelper test,ServerPlayer player) {
  var mob=EntityType.ZOMBIE.create(test.getLevel()); mob.setNoAi(true); mob.setNoGravity(true); mob.setItemSlot(EquipmentSlot.HEAD,new ItemStack(Items.CARVED_PUMPKIN)); mob.setPos(player.position().add(0,0,4)); test.getLevel().addFreshEntity(mob); return mob;
 }
 @GameTest(template="riftfang_test")
 public static void allVariantsRespondOnceToRealLoudEventsEvenBehindWalls(GameTestHelper test) throws Exception {
  int count=0;
  for(var item:BuiltInRegistries.ITEM) if(item instanceof PulsiteWeapon) {
   count++; var player=player(test); var fresh=new ItemStack(item); player.setItemInHand(InteractionHand.MAIN_HAND,fresh); var mob=enemy(test,player);
   test.assertFalse(RecommendedAbilityRuntime.activate(player,fresh,RecommendedAbilities.ECHO_PIN),"Fresh weapon activates");
   var stack=Augmentations.apply(fresh,RecommendedAbilities.ECHO_PIN); player.setItemInHand(InteractionHand.MAIN_HAND,stack); AbilityTestPackets.use(player,0);
   test.assertTrue(EchoPinEvents.active(player),"Pin not armed"); mob.gameEvent(GameEvent.STEP); player.gameEvent(GameEvent.ENTITY_ACTION);
   test.assertTrue(EchoPinEvents.active(player) && !mob.hasEffect(MobEffects.GLOWING),"Quiet/unrelated event triggers pin");
   for(int y=0;y<=2;y++) test.getLevel().setBlockAndUpdate(player.blockPosition().south(2).above(y),Blocks.STONE.defaultBlockState());
   mob.setPos(player.position().add(0,0,6)); test.assertFalse(player.hasLineOfSight(mob),"Fixture lacks wall"); mob.gameEvent(GameEvent.ENTITY_ACTION);
   test.assertTrue(!EchoPinEvents.active(player) && mob.getEffect(MobEffects.GLOWING)!=null && mob.getEffect(MobEffects.GLOWING).getDuration()==40,"Loud event lacks two-second reveal out of sight");
   mob.removeEffect(MobEffects.GLOWING); mob.gameEvent(GameEvent.ENTITY_ACTION); test.assertFalse(mob.hasEffect(MobEffects.GLOWING),"Pin triggers twice");
   WeaponAbilityNetwork.onPlayerTick(new PlayerTickEvent.Pre(player)); String key=BuiltInRegistries.ITEM.getKey(item)+":"+RecommendedAbilities.ECHO_PIN;
   test.assertTrue(player.getData(WeaponCooldownAttachments.COOLDOWNS).getOrDefault(key,0L)==player.level().getGameTime()+600,"Triggered pin misses cooldown");
   mob.discard(); test.getLevel().getServer().getPlayerList().remove(player);
  }
  test.assertTrue(count==15,"Missing Pulsite variants"); test.succeed();
 }
 @GameTest(template="riftfang_test",timeoutTicks=650)
 public static void expirySwitchAndWallsDoNotLeaveMarkers(GameTestHelper test) throws Exception {
  var player=player(test); var stack=Augmentations.apply(new ItemStack(net.bananashelp20.forgermod.item.ModItems.SHRIEKING_CLAYMORE.get()),RecommendedAbilities.ECHO_PIN);
  player.setItemInHand(InteractionHand.MAIN_HAND,stack); var mob=enemy(test,player);
  var wall=player.blockPosition().south(2).above(); test.getLevel().setBlockAndUpdate(wall,Blocks.STONE.defaultBlockState()); AbilityTestPackets.use(player,0); test.assertFalse(EchoPinEvents.active(player),"Wall permits initial marking");
  test.getLevel().setBlockAndUpdate(wall,Blocks.AIR.defaultBlockState()); AbilityTestPackets.use(player,0);
  test.runAfterDelay(599,()->test.assertTrue(EchoPinEvents.active(player),"Pin expires early"));
  test.runAfterDelay(601,()-> { WeaponAbilityNetwork.onPlayerTick(new PlayerTickEvent.Pre(player)); test.assertFalse(EchoPinEvents.active(player),"Pin fails to expire");
   mob.gameEvent(GameEvent.ENTITY_ACTION); test.assertFalse(mob.hasEffect(MobEffects.GLOWING),"Expired reverse index reveals");
   RecommendedAbilityRuntime.activate(player,stack,RecommendedAbilities.ECHO_PIN); player.setItemInHand(InteractionHand.MAIN_HAND,stack.copy()); EchoPinEvents.onTick(new PlayerTickEvent.Post(player));
   mob.gameEvent(GameEvent.ENTITY_ACTION); test.assertFalse(mob.hasEffect(MobEffects.GLOWING),"Switch retains pin");
   mob.discard(); test.getLevel().getServer().getPlayerList().remove(player); test.succeed(); });
 }
}
