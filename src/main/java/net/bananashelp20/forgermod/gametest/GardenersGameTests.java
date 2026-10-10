package net.bananashelp20.forgermod.gametest;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.custom.LushWeapon;
import net.bananashelp20.forgermod.item.custom.abilities.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder(ForgerMod.MOD_ID) @PrefixGameTestTemplate(false)
public class GardenersGameTests {
 @GameTest(template="riftfang_test")
 public static void allVariantsPlantOncePoisonEveryoneAndCancelSafely(GameTestHelper test) throws Exception {
  int count=0;
  for(var item:BuiltInRegistries.ITEM) if(item instanceof LushWeapon) {
   count++; var player=TestPlayers.survival(test); player.setInvulnerable(true); player.setPos(test.getBounds().getCenter());
   var fresh=new ItemStack(item); player.setItemInHand(InteractionHand.MAIN_HAND,fresh);
   test.assertFalse(RecommendedAbilityRuntime.activate(player,fresh,RecommendedAbilities.GARDENERS),"Fresh weapon activates");
   var stack=Augmentations.apply(fresh,RecommendedAbilities.GARDENERS); player.setItemInHand(InteractionHand.MAIN_HAND,stack); AbilityTestPackets.use(player,0);
   var mob=EntityType.ZOMBIE.create(test.getLevel()); mob.setNoAi(true); mob.setPos(player.position()); test.getLevel().addFreshEntity(mob);
   mob.setInvulnerable(true); mob.hurt(player.damageSources().playerAttack(player),100); mob.setInvulnerable(false);
   test.assertTrue(test.getLevel().getEntitiesOfClass(GardenersEvents.Plant.class,player.getBoundingBox().inflate(3)).isEmpty(),"Failed kill plants");
   mob.hurt(player.damageSources().playerAttack(player),100);
   var clouds=test.getLevel().getEntitiesOfClass(GardenersEvents.PoisonCloud.class,player.getBoundingBox().inflate(3));
   var plants=test.getLevel().getEntitiesOfClass(GardenersEvents.Plant.class,player.getBoundingBox().inflate(3));
   test.assertTrue(clouds.size()==1 && plants.size()==1,"Kill does not create one garden"); var cloud=clouds.getFirst(); var plant=plants.getFirst();
   test.assertTrue(cloud.getDuration()==6000 && cloud.getRadius()==2 && !cloud.shouldBeSaved() && !plant.shouldBeSaved(),"Garden duration/radius/persistence wrong");
   var cow=EntityType.COW.create(test.getLevel()); cow.setNoAi(true); cow.setPos(player.position()); test.getLevel().addFreshEntity(cow);
   for(int tick=0;tick<5;tick++) cloud.tick();
   test.assertTrue(player.getEffect(MobEffects.POISON)!=null && cow.getEffect(MobEffects.POISON)!=null,"Caster or neutral entity excluded");
   test.assertTrue(cow.getEffect(MobEffects.POISON).getDuration()==400,"Poison shortened by vanilla potion scaling");
   player.setItemInHand(InteractionHand.MAIN_HAND,stack.copy()); WeaponAbilityNetwork.onPlayerTick(new PlayerTickEvent.Pre(player));
   test.assertTrue(cloud.isRemoved() && plant.isRemoved() && !GardenersEvents.active(player),"Switch retains garden");
   String key=BuiltInRegistries.ITEM.getKey(item)+":"+RecommendedAbilities.GARDENERS;
   test.assertTrue(player.getData(WeaponCooldownAttachments.COOLDOWNS).getOrDefault(key,0L)==player.level().getGameTime()+1200,"Switch misses cooldown");
   cow.discard(); mob.discard(); test.getLevel().getServer().getPlayerList().remove(player);
  }
  test.assertTrue(count==15,"Missing Lush variants"); test.succeed();
 }
 @GameTest(template="riftfang_test")
 public static void neutralKillDoesNotConsumeAndCloudExpiryRemovesPlant(GameTestHelper test) {
  var player=TestPlayers.survival(test); player.setInvulnerable(true); player.setPos(test.getBounds().getCenter());
  var stack=new ItemStack(net.bananashelp20.forgermod.item.ModItems.OVERGROWN_CLAYMORE.get());
  for(int i=0;i<4;i++) stack=Augmentations.apply(stack,RecommendedAbilities.GARDENERS);
  player.setItemInHand(InteractionHand.MAIN_HAND,stack); RecommendedAbilityRuntime.activate(player,stack,RecommendedAbilities.GARDENERS);
  var cow=EntityType.COW.create(test.getLevel()); cow.setPos(player.position()); test.getLevel().addFreshEntity(cow); cow.hurt(player.damageSources().playerAttack(player),100);
  test.assertTrue(test.getLevel().getEntitiesOfClass(GardenersEvents.Plant.class,player.getBoundingBox().inflate(4)).isEmpty() && GardenersEvents.active(player),"Neutral kill consumes charge");
  var mob=EntityType.ZOMBIE.create(test.getLevel()); mob.setNoAi(true); mob.setPos(player.position()); test.getLevel().addFreshEntity(mob); mob.hurt(player.damageSources().playerAttack(player),100);
  var cloud=test.getLevel().getEntitiesOfClass(GardenersEvents.PoisonCloud.class,player.getBoundingBox().inflate(4)).getFirst();
  var plant=test.getLevel().getEntitiesOfClass(GardenersEvents.Plant.class,player.getBoundingBox().inflate(4)).getFirst();
  test.assertTrue(cloud.getRadius()==3.5,"Rank-IV radius wrong"); cloud.tickCount=6000; cloud.tick(); GardenersEvents.onTick(new PlayerTickEvent.Post(player));
  test.assertTrue(cloud.isRemoved() && plant.isRemoved() && !GardenersEvents.active(player),"Five-minute expiry leaves plant");
  mob.discard(); cow.discard(); test.getLevel().getServer().getPlayerList().remove(player); test.succeed();
 }
}
