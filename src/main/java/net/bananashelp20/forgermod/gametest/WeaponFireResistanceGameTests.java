package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.augmentation.RecommendedAbilities;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.abilities.WeaponFireResistanceEvents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class WeaponFireResistanceGameTests {
    @GameTest(template="riftfang_test")
    public static void hiddenPotionSurvivesAndCuringDoesNotResurrectOldPotion(GameTestHelper test) {
        var player=TestPlayers.survival(test);
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE,100,0));
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE,10,1));
        var stack=Augmentations.apply(new ItemStack(ModItems.INFERNAL_CLAYMORE.get()),RecommendedAbilities.FIRE_RESISTANCE);
        player.setItemInHand(InteractionHand.MAIN_HAND,stack);
        for(int tick=0;tick<12;tick++) WeaponFireResistanceEvents.onTick(new PlayerTickEvent.Pre(player));
        player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY); WeaponFireResistanceEvents.onTick(new PlayerTickEvent.Pre(player));
        var effect=player.getEffect(MobEffects.FIRE_RESISTANCE);
        test.assertTrue(effect!=null && effect.getAmplifier()==0 && effect.getDuration()==88,"Hidden natural potion is lost or extended by held protection");
        player.setItemInHand(InteractionHand.MAIN_HAND,stack); WeaponFireResistanceEvents.onTick(new PlayerTickEvent.Pre(player));
        player.removeAllEffects(); WeaponFireResistanceEvents.onTick(new PlayerTickEvent.Pre(player));
        test.assertTrue(player.getEffect(MobEffects.FIRE_RESISTANCE).isInfiniteDuration(),"Held protection does not resume after curing");
        WeaponFireResistanceEvents.onLogout(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(player));
        test.assertFalse(player.hasEffect(MobEffects.FIRE_RESISTANCE),"Logout resurrects the cured potion or retains weapon protection");
        test.getLevel().getServer().getPlayerList().remove(player); test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void tenVariantsRespectHandsRankCapAndRealFireDamage(GameTestHelper test) {
        for(Item item:new Item[]{ModItems.INFERNAL_CLAYMORE.get(),ModItems.INFERNAL_CLAYMORE_RUBY.get(),ModItems.INFERNAL_CLAYMORE_AMBER.get(),
                ModItems.INFERNAL_CLAYMORE_AMETHYST.get(),ModItems.INFERNAL_CLAYMORE_JADE.get(),ModItems.MOLTEN_AXE.get(),ModItems.MOLTEN_AXE_RUBY.get(),
                ModItems.MOLTEN_AXE_AMBER.get(),ModItems.MOLTEN_AXE_AMETHYST.get(),ModItems.MOLTEN_AXE_JADE.get()}) {
            var player=TestPlayers.survival(test); var one=Augmentations.apply(new ItemStack(item),RecommendedAbilities.FIRE_RESISTANCE);
            player.setItemInHand(InteractionHand.OFF_HAND,one); WeaponFireResistanceEvents.onTick(new PlayerTickEvent.Pre(player));
            test.assertFalse(player.hasEffect(MobEffects.FIRE_RESISTANCE),"Level I works from offhand");
            player.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY); player.setItemInHand(InteractionHand.MAIN_HAND,one);
            WeaponFireResistanceEvents.onTick(new PlayerTickEvent.Pre(player));
            test.assertTrue(player.getEffect(MobEffects.FIRE_RESISTANCE).isInfiniteDuration(),"Main-hand level I has no infinite resistance");
            player.hurt(player.damageSources().inFire(),5); test.assertTrue(player.getHealth()==20,"Fire resistance does not prevent actual fire damage");
            player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY); WeaponFireResistanceEvents.onTick(new PlayerTickEvent.Pre(player));
            test.assertFalse(player.hasEffect(MobEffects.FIRE_RESISTANCE),"Weapon grants permanent resistance after removal");
            var two=Augmentations.apply(one,RecommendedAbilities.FIRE_RESISTANCE);
            test.assertTrue(Augmentations.level(two,RecommendedAbilities.FIRE_RESISTANCE)==2 && Augmentations.apply(two,RecommendedAbilities.FIRE_RESISTANCE).isEmpty(),"Fire Resistance exceeds level II");
            player.setItemInHand(InteractionHand.OFF_HAND,two); WeaponFireResistanceEvents.onTick(new PlayerTickEvent.Pre(player));
            test.assertTrue(player.getEffect(MobEffects.FIRE_RESISTANCE).isInfiniteDuration(),"Level II does not work in offhand");
            player.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY); WeaponFireResistanceEvents.onTick(new PlayerTickEvent.Pre(player));
            test.getLevel().getServer().getPlayerList().remove(player);
        }
        test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void independentPotionsArePreservedAndNotMadePermanent(GameTestHelper test) {
        var player=TestPlayers.survival(test); player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE,100,0));
        var stack=Augmentations.apply(new ItemStack(ModItems.INFERNAL_CLAYMORE.get()),RecommendedAbilities.FIRE_RESISTANCE);
        player.setItemInHand(InteractionHand.MAIN_HAND,stack); WeaponFireResistanceEvents.onTick(new PlayerTickEvent.Pre(player));
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE,200,1));
        WeaponFireResistanceEvents.onTick(new PlayerTickEvent.Pre(player));
        test.assertTrue(player.getEffect(MobEffects.FIRE_RESISTANCE).isInfiniteDuration(),"Stronger potion removes held resistance");
        player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY); WeaponFireResistanceEvents.onTick(new PlayerTickEvent.Pre(player));
        var effect=player.getEffect(MobEffects.FIRE_RESISTANCE);
        test.assertTrue(effect!=null && effect.getAmplifier()==1 && effect.getDuration()==199,"Independent potion is lost, extended or permanent");
        player.removeEffect(MobEffects.FIRE_RESISTANCE); player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE,-1,0));
        player.setItemInHand(InteractionHand.MAIN_HAND,stack); WeaponFireResistanceEvents.onTick(new PlayerTickEvent.Pre(player));
        player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY); WeaponFireResistanceEvents.onTick(new PlayerTickEvent.Pre(player));
        test.assertTrue(player.getEffect(MobEffects.FIRE_RESISTANCE).isInfiniteDuration(),"Unrelated infinite resistance was removed");
        test.getLevel().getServer().getPlayerList().remove(player); test.succeed();
    }
}
