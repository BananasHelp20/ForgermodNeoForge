package net.bananashelp20.forgermod.gametest;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.SomniumWeapon;
import net.bananashelp20.forgermod.item.custom.WeaponAbilitySlot;
import net.bananashelp20.forgermod.item.custom.abilities.WeaponAbilityNetwork;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder(ForgerMod.MOD_ID) @PrefixGameTestTemplate(false)
public class AbsoluteNightmareGameTests {
 @GameTest(template="riftfang_test")
 public static void fiveVariantsApplyCorrectNightmareStrengthAndRankDuration(GameTestHelper test) throws Exception {
  for(var item:new net.minecraft.world.item.Item[]{ModItems.NIGHTMARE_DAGGER.get(),ModItems.NIGHTMARE_DAGGER_RUBY.get(),ModItems.NIGHTMARE_DAGGER_AMBER.get(),ModItems.NIGHTMARE_DAGGER_AMETHYST.get(),ModItems.NIGHTMARE_DAGGER_JADE.get()}) {
   for(int rank=1;rank<=4;rank++) {
   var player=TestPlayers.survival(test);player.setNoGravity(true);player.setPos(test.getBounds().getCenter());
   var fresh=new ItemStack(item);player.setItemInHand(InteractionHand.MAIN_HAND,fresh);var weapon=(SomniumWeapon)item;test.assertFalse(weapon.activateAbility(player,fresh,WeaponAbilitySlot.PRIMARY),"Fresh dagger arms Nightmare");
   var stack=fresh;for(int upgrade=0;upgrade<rank;upgrade++)stack=Augmentations.apply(stack,"tooltips.forgermod.ability.absolute_nightmare");player.setItemInHand(InteractionHand.MAIN_HAND,stack);AbilityTestPackets.use(player,0);
   try {for(int hit=0;hit<3;hit++) {
    for(int tick=0;tick<60;tick++)player.doTick();player.setOnGround(true);player.fallDistance=0;
    var victim=EntityType.COW.create(test.getLevel());victim.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(1000);victim.setHealth(1000);victim.setNoAi(true);victim.setNoGravity(true);victim.setPos(player.position().add(0,0,2));test.getLevel().addFreshEntity(victim);
    try {player.attack(victim);var weak=victim.getEffect(MobEffects.WEAKNESS);var dark=victim.getEffect(MobEffects.DARKNESS);
     int duration=rank==1?100:200;
     if(hit<2)test.assertTrue(weak!=null && weak.getAmplifier()==3 && weak.getDuration()==duration && dark!=null && dark.getAmplifier()==4 && dark.getDuration()==duration,"Nightmare effects are not Weakness IV/Darkness V for the correct rank duration");
     else test.assertTrue(weak==null && dark==null,"Third hit reuses Nightmare");
     test.assertTrue(!player.hasEffect(MobEffects.WEAKNESS) && !player.hasEffect(MobEffects.DARKNESS),"Nightmare debuffs attacker");
    }finally {victim.discard();}
   }}finally {WeaponAbilityNetwork.onLogout(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(player));SomniumWeapon.onLogout(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(player));player.server.getPlayerList().remove(player);}
  }
  }
  test.succeed();
 }
}
