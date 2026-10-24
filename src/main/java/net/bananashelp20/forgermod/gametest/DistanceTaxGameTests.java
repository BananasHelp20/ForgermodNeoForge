package net.bananashelp20.forgermod.gametest;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.abilities.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.nbt.*;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.gametest.*;
import java.util.*;
@GameTestHolder(ForgerMod.MOD_ID) @PrefixGameTestTemplate(false)
public class DistanceTaxGameTests {
 private static ServerPlayer player(GameTestHelper test){var p=TestPlayers.survival(test);p.setNoGravity(true);p.setInvulnerable(true);p.setPos(test.getBounds().getCenter());return p;}
 private static Zombie enemy(GameTestHelper test,ServerPlayer player){
  var mob=EntityType.ZOMBIE.create(test.getLevel());mob.setNoAi(true);mob.setNoGravity(true);mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);mob.setHealth(1000);mob.getAttribute(Attributes.ARMOR).setBaseValue(0);mob.setItemSlot(EquipmentSlot.HEAD,new ItemStack(Items.CARVED_PUMPKIN));mob.setPos(player.position().add(0,0,1));return mob;
 }
 private static void cleanup(ServerPlayer player){DistanceTaxEvents.cancel(player.getUUID());WeaponAbilityNetwork.onLogout(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(player));player.server.getPlayerList().remove(player);}
 @GameTest(template="riftfang_test",timeoutTicks=250)
 public static void activationIsRequiredAndLogoutCancelsExistingMark(GameTestHelper test)throws Exception{
  var player=player(test);var fresh=new ItemStack(ModItems.CLAYMORE_OF_THE_VOID.get());player.setItemInHand(InteractionHand.MAIN_HAND,fresh);var unlearned=enemy(test,player);unlearned.hurt(player.damageSources().playerAttack(player),1);
  test.assertFalse(DistanceTaxEvents.activate(player,fresh),"Fresh weapon can arm");
  var stack=Augmentations.apply(fresh,RecommendedAbilities.DISTANCE_TAX);player.setItemInHand(InteractionHand.MAIN_HAND,stack);var unarmed=enemy(test,player);unarmed.hurt(player.damageSources().playerAttack(player),1);
  test.assertTrue(!DistanceTaxEvents.active(player) && !Augmentations.hasPassive(stack,RecommendedAbilities.DISTANCE_TAX) && Augmentations.ids(stack,true).contains(RecommendedAbilities.DISTANCE_TAX),"Distance Tax remains automatic/passive");
  AbilityTestPackets.use(player,0);var canceled=enemy(test,player);canceled.hurt(player.damageSources().playerAttack(player),1);player.setPos(canceled.position().add(20,0,0));DistanceTaxEvents.onLogout(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(player));
  test.runAfterDelay(201,()->{try{DistanceTaxEvents.onTick(new PlayerTickEvent.Post(player));test.assertTrue(unlearned.getHealth()==999 && unarmed.getHealth()==999 && canceled.getHealth()==999,"Unarmed hit marks or logout retains tax");test.succeed();}finally{unlearned.discard();unarmed.discard();canceled.discard();cleanup(player);}});
 }
 @GameTest(template="riftfang_test",timeoutTicks=450)
 public static void allVariantsAndRanksPayOnceAfterFirstHitDeadlineWithoutRefreshing(GameTestHelper test)throws Exception{
  var players=new ArrayList<ServerPlayer>();var targets=new ArrayList<Zombie>();var ranks=new ArrayList<Integer>();
  for(Item item:List.of(ModItems.CLAYMORE_OF_THE_VOID.get(),ModItems.CLAYMORE_OF_THE_VOID_RUBY.get(),ModItems.CLAYMORE_OF_THE_VOID_AMBER.get(),ModItems.CLAYMORE_OF_THE_VOID_AMETHYST.get(),ModItems.CLAYMORE_OF_THE_VOID_JADE.get()))for(int rank=1;rank<=4;rank++){
   var player=player(test);var stack=Augmentations.setLevel(new ItemStack(item),RecommendedAbilities.DISTANCE_TAX,rank);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
   AbilityTestPackets.use(player,0);var target=enemy(test,player);target.hurt(player.damageSources().playerAttack(player),1);player.setPos(target.position().add(20,0,0));players.add(player);targets.add(target);ranks.add(rank);
  }
  test.runAfterDelay(100,()->{for(int i=0;i<players.size();i++){targets.get(i).invulnerableTime=0;targets.get(i).hurt(players.get(i).damageSources().playerAttack(players.get(i)),1);}});
  test.runAfterDelay(199,()->{for(var p:players)DistanceTaxEvents.onTick(new PlayerTickEvent.Post(p));for(var target:targets)test.assertTrue(target.getHealth()==998,"Mark pays before ten seconds");});
  test.runAfterDelay(201,()->{for(int i=0;i<players.size();i++){DistanceTaxEvents.onTick(new PlayerTickEvent.Post(players.get(i)));test.assertTrue(Math.abs(targets.get(i).getHealth()-(998-DistanceTaxEvents.damage(20,ranks.get(i))))<.001,"Wrong rank conversion or later hit refreshes mark");}});
  test.runAfterDelay(403,()->{try{for(int i=0;i<players.size();i++){DistanceTaxEvents.onTick(new PlayerTickEvent.Post(players.get(i)));test.assertTrue(Math.abs(targets.get(i).getHealth()-(998-DistanceTaxEvents.damage(20,ranks.get(i))))<.001,"Detonation recursively marks target");}test.succeed();}finally{targets.forEach(Entity::discard);players.forEach(DistanceTaxGameTests::cleanup);}});
 }
 @GameTest(template="riftfang_test",timeoutTicks=250)
 public static void firstHitStaysMarkedCapRemainsFortyAndSwitchCancels(GameTestHelper test)throws Exception{
  var player=player(test);var stack=Augmentations.setLevel(new ItemStack(ModItems.CLAYMORE_OF_THE_VOID.get()),RecommendedAbilities.DISTANCE_TAX,4);player.setItemInHand(InteractionHand.MAIN_HAND,stack);var first=enemy(test,player);var later=enemy(test,player);
  test.getLevel().getChunk(first.blockPosition());test.getLevel().addFreshEntity(first);
  AbilityTestPackets.use(player,0);first.hurt(player.damageSources().playerAttack(player),1);later.hurt(player.damageSources().playerAttack(player),1);player.setPos(first.position().add(100,0,0));
  var switched=player(test);var switchedStack=Augmentations.apply(new ItemStack(ModItems.CLAYMORE_OF_THE_VOID.get()),RecommendedAbilities.DISTANCE_TAX);switched.setItemInHand(InteractionHand.MAIN_HAND,switchedStack);var canceled=enemy(test,switched);AbilityTestPackets.use(switched,0);canceled.hurt(switched.damageSources().playerAttack(switched),1);switched.setItemInHand(InteractionHand.MAIN_HAND,switchedStack.copy());DistanceTaxEvents.onTick(new PlayerTickEvent.Post(switched));
  test.runAfterDelay(201,()->{try{DistanceTaxEvents.onTick(new PlayerTickEvent.Post(player));DistanceTaxEvents.onTick(new PlayerTickEvent.Post(switched));test.assertTrue(first.getHealth()==959 && later.getHealth()==999 && canceled.getHealth()==999,"First target changes, damage exceeds cap or switching retains mark");test.succeed();}finally{first.discard();later.discard();canceled.discard();cleanup(player);cleanup(switched);}});
 }
 @GameTest(template="riftfang_test")
 public static void legacyTaxStaysLearnedInactiveUntilSlotIsFreed(GameTestHelper test){
  // Templates share the same migration logic and can legitimately hold three other actives.
  var stack=new ItemStack(ModItems.AUGMENT_TEMPLATE.get());var actives=Augmentations.catalog(stack).stream().filter(Augmentations.Ability::active).filter(a->!a.id().equals(RecommendedAbilities.DISTANCE_TAX)).limit(3).toList();test.assertTrue(actives.size()==3,"Migration fixture lacks three other actives");
  for(var ability:actives)stack=Augmentations.apply(stack,ability.id());
  var all=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag();var aug=all.getCompound("forgermod_augments");var levels=aug.getList("levels",Tag.TAG_COMPOUND);var legacy=new ListTag();var tax=new CompoundTag();tax.putString("id",RecommendedAbilities.DISTANCE_TAX);tax.putInt("level",4);legacy.add(tax);for(var level:levels)legacy.add(level.copy());aug.put("levels",legacy);all.put("forgermod_augments",aug);stack.set(DataComponents.CUSTOM_DATA,CustomData.of(all));
  var expected=actives.stream().map(Augmentations.Ability::id).toList();test.assertTrue(Augmentations.ids(stack,true).equals(expected) && Augmentations.level(stack,RecommendedAbilities.DISTANCE_TAX)==4 && Augmentations.inactiveActives(stack).contains(RecommendedAbilities.DISTANCE_TAX),"Migration displaces an existing active or loses learned tax");
  // Free an active slot in the legacy save, retaining every remaining rank.
  legacy.remove(1);aug.put("levels",legacy);all.put("forgermod_augments",aug);stack.set(DataComponents.CUSTOM_DATA,CustomData.of(all));
  test.assertTrue(Augmentations.ids(stack,true).contains(RecommendedAbilities.DISTANCE_TAX) && Augmentations.inactiveActives(stack).isEmpty() && Augmentations.level(stack,RecommendedAbilities.DISTANCE_TAX)==4,"Tax does not become usable when an active slot is freed");test.succeed();
 }
}
