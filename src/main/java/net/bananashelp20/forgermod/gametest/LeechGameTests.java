package net.bananashelp20.forgermod.gametest;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.VulnusiumWeapon;
import net.bananashelp20.forgermod.item.custom.WeaponAbilitySlot;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder(ForgerMod.MOD_ID) @PrefixGameTestTemplate(false)
public class LeechGameTests {
 private static final String LEECH="tooltips.forgermod.ability.leech";
 @GameTest(template="riftfang_test")
 public static void fiveVariantsLeechContinuouslyWithoutDamageBonusAtAllRanks(GameTestHelper test) {
  var player=TestPlayers.survival(test);player.setNoGravity(true);player.setPos(test.getBounds().getCenter());player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);
  var target=EntityType.COW.create(test.getLevel());target.setNoAi(true);target.setNoGravity(true);target.setPos(player.position().add(0,0,2));target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(10000);target.setHealth(10000);test.getLevel().addFreshEntity(target);
  try {
   for(var item:new net.minecraft.world.item.Item[]{ModItems.ASSASSIN_DAGGER.get(),ModItems.ASSASSIN_DAGGER_RUBY.get(),ModItems.ASSASSIN_DAGGER_AMBER.get(),ModItems.ASSASSIN_DAGGER_AMETHYST.get(),ModItems.ASSASSIN_DAGGER_JADE.get()}) {
    var stack=new ItemStack(item);player.setItemInHand(InteractionHand.MAIN_HAND,stack);player.setHealth(5);target.setHealth(target.getMaxHealth());target.invulnerableTime=0;target.hurt(player.damageSources().playerAttack(player),10);test.assertTrue(player.getHealth()==5,"Fresh dagger leeches");
    for(int rank=1;rank<=4;rank++) {
     stack=Augmentations.apply(stack,LEECH);player.setItemInHand(InteractionHand.MAIN_HAND,stack);for(int tick=0;tick<60;tick++)player.doTick();player.setHealth(5);
     test.assertTrue(Augmentations.hasPassive(stack,LEECH) && !Augmentations.ids(stack,true).contains(LEECH),"Leech did not migrate to passive");
     var weapon=(VulnusiumWeapon)item;test.assertFalse(weapon.activateAbility(player,stack,WeaponAbilitySlot.SECONDARY),"Leech can still be activated");
     target.setHealth(target.getMaxHealth());float before=target.getHealth();
     for(int hit=0;hit<20;hit++){target.invulnerableTime=0;test.assertTrue(target.hurt(player.damageSources().playerAttack(player),10),"Leech hit rejected");}
     test.assertTrue(Math.abs(before-target.getHealth()-200)<.01,"Leech still increases damage");
     test.assertTrue(Math.abs(player.getHealth()-(5+20*(1+.2F*(rank-1))))<.01,"Passive healing stops or has wrong rank rate");
     float health=player.getHealth();target.setInvulnerable(true);target.hurt(player.damageSources().playerAttack(player),10);target.setInvulnerable(false);test.assertTrue(player.getHealth()==health,"Rejected hit leeches");
    }
   }
   player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(net.minecraft.world.item.Items.IRON_SWORD));player.setHealth(5);target.invulnerableTime=0;target.hurt(player.damageSources().playerAttack(player),10);test.assertTrue(player.getHealth()==5,"Leech remains after unequipping");
  } finally {target.discard();player.server.getPlayerList().remove(player);}
  test.succeed();
 }
 @GameTest(template="riftfang_test")
 public static void oneMatchingOffhandDaggerProvidesLeechWithoutStacking(GameTestHelper test) {
  var player=TestPlayers.survival(test);player.setNoGravity(true);player.setPos(test.getBounds().getCenter());
  var main=Augmentations.apply(new ItemStack(ModItems.ASSASSIN_DAGGER.get()),Augmentations.DUAL_WIELD);
  var off=Augmentations.apply(new ItemStack(ModItems.ASSASSIN_DAGGER_RUBY.get()),LEECH);player.setItemInHand(InteractionHand.MAIN_HAND,main);player.setItemInHand(InteractionHand.OFF_HAND,off);for(int tick=0;tick<60;tick++)player.doTick();
  var target=EntityType.COW.create(test.getLevel());target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000);target.setHealth(1000);target.setPos(player.position().add(0,0,2));test.getLevel().addFreshEntity(target);
  try {
   player.setHealth(5);target.hurt(player.damageSources().playerAttack(player),10);test.assertTrue(player.getHealth()==6,"Matching offhand passive does not leech");
   main=Augmentations.apply(main,LEECH);player.setItemInHand(InteractionHand.MAIN_HAND,main);target.invulnerableTime=0;target.hurt(player.damageSources().playerAttack(player),10);test.assertTrue(player.getHealth()==7,"Two copies stack passive");
   player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModItems.ASSASSIN_DAGGER.get()));target.invulnerableTime=0;target.hurt(player.damageSources().playerAttack(player),10);test.assertTrue(player.getHealth()==7,"Unlearned Dual Wield shares passive");
  } finally {target.discard();player.server.getPlayerList().remove(player);}
  test.succeed();
 }
}
