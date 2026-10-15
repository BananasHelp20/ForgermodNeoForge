package net.bananashelp20.forgermod.gametest;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.VulnusiumWeapon;
import net.bananashelp20.forgermod.item.custom.abilities.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class BloodRushGameTests {
    @GameTest(template="riftfang_test")
    public static void tenVariantsPayRealHealthWithoutAllowingLethalCost(GameTestHelper test) {
        var player=TestPlayers.survival(test);int count=0;
        try {for(var entry:ModItems.ITEMS.getEntries()) {
            var fresh=new ItemStack(entry.get());boolean eligible=entry.get() instanceof VulnusiumWeapon w && !w.isAxe();
            test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(a->a.id().equals(AdditionalAbilities.BLOOD_RUSH))==eligible,"Wrong Blood Rush pool");if(!eligible) continue;count++;
            player.setHealth(20);player.removeEffect(MobEffects.MOVEMENT_SPEED);player.setItemInHand(InteractionHand.MAIN_HAND,fresh);test.assertFalse(RecommendedAbilityRuntime.activate(player,fresh,AdditionalAbilities.BLOOD_RUSH),"Fresh weapon grants Rush");
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(MobEffects.ABSORPTION,200,0));player.setAbsorptionAmount(4);
            var stack=Augmentations.apply(fresh,AdditionalAbilities.BLOOD_RUSH);player.setItemInHand(InteractionHand.MAIN_HAND,stack);test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.BLOOD_RUSH),"Rush refused");
            int jade=JadeAugments.isJade(stack)?1:0;
            test.assertTrue(player.getHealth()==18 && player.getAbsorptionAmount()==4 && player.getEffect(MobEffects.MOVEMENT_SPEED).getAmplifier()==1+jade && player.getEffect(MobEffects.MOVEMENT_SPEED).getDuration()==200+20*jade,"Cost or Speed duration wrong");
            player.setHealth(2);player.removeEffect(MobEffects.MOVEMENT_SPEED);test.assertFalse(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.BLOOD_RUSH),"Lethal cost permitted");test.assertTrue(player.getHealth()==2 && !player.hasEffect(MobEffects.MOVEMENT_SPEED),"Rejected use changes health or speed");
        }} finally {player.setHealth(20);player.server.getPlayerList().remove(player);}
        test.assertTrue(count==10,"Skipped variants");test.succeed();
    }
}
