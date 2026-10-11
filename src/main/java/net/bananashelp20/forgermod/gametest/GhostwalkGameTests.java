package net.bananashelp20.forgermod.gametest;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.MorsiumWeapon;
import net.bananashelp20.forgermod.item.custom.abilities.*;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class GhostwalkGameTests {
    @GameTest(template="riftfang_test")
    public static void fifteenVariantsEndOnAirSwingAndPreserveExternalInvisibility(GameTestHelper test) {
        var player=TestPlayers.survival(test);int count=0;
        try {for(var entry:ModItems.ITEMS.getEntries()) {
            var fresh=new ItemStack(entry.get());boolean eligible=entry.get() instanceof MorsiumWeapon;
            test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(a->a.id().equals(AdditionalAbilities.GHOSTWALK))==eligible,"Wrong Ghostwalk pool");if(!eligible) continue;count++;
            player.removeEffect(MobEffects.INVISIBILITY);player.setItemInHand(InteractionHand.MAIN_HAND,fresh);test.assertFalse(RecommendedAbilityRuntime.activate(player,fresh,AdditionalAbilities.GHOSTWALK),"Fresh weapon vanishes");
            var stack=Augmentations.apply(fresh,AdditionalAbilities.GHOSTWALK);player.setItemInHand(InteractionHand.MAIN_HAND,stack);
            test.assertTrue(RecommendedAbilityRuntime.activate(player,stack,AdditionalAbilities.GHOSTWALK) && player.getEffect(MobEffects.INVISIBILITY).getDuration()==200,"Ghostwalk duration wrong");GhostwalkEvents.swing(player);test.assertFalse(GhostwalkEvents.active(player) || player.hasEffect(MobEffects.INVISIBILITY),"Air swing keeps invisibility");
            player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY,100,0));GhostwalkEvents.activate(player,stack);GhostwalkEvents.tick(new PlayerTickEvent.Pre(player));GhostwalkEvents.swing(player);test.assertTrue(player.getEffect(MobEffects.INVISIBILITY).getDuration()==99,"Prior invisibility lost or timer paused");player.removeEffect(MobEffects.INVISIBILITY);
            GhostwalkEvents.activate(player,stack);player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY,80,0));player.setItemInHand(InteractionHand.MAIN_HAND,stack.copy());test.assertFalse(GhostwalkEvents.active(player),"Switch retains Ghostwalk");test.assertTrue(player.getEffect(MobEffects.INVISIBILITY).getDuration()==80,"Newly applied potion lost on switch");player.removeEffect(MobEffects.INVISIBILITY);
            var current=player.getMainHandItem();player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY,70,0));GhostwalkEvents.activate(player,current);GhostwalkEvents.expired(new net.neoforged.neoforge.event.entity.living.MobEffectEvent.Expired(player,player.getEffect(MobEffects.INVISIBILITY)));player.removeEffect(MobEffects.INVISIBILITY);test.assertFalse(GhostwalkEvents.active(player),"Natural potion expiry retains state");test.assertTrue(player.getEffect(MobEffects.INVISIBILITY).getDuration()==70,"Natural expiry loses saved potion");player.removeEffect(MobEffects.INVISIBILITY);
            GhostwalkEvents.activate(player,current);player.removeEffect(MobEffects.INVISIBILITY);test.assertFalse(GhostwalkEvents.active(player) || player.hasEffect(MobEffects.INVISIBILITY),"Dispel resurrects invisibility");
        }} finally {GhostwalkEvents.cancel(player.getUUID());player.server.getPlayerList().remove(player);}
        test.assertTrue(count==15,"Skipped variants");test.succeed();
    }
    @GameTest(template="riftfang_test",timeoutTicks=230)
    public static void naturalExpiryEndsStateAndRestoresLongerPotion(GameTestHelper test) {
        var player=TestPlayers.survival(test);var stack=Augmentations.apply(new ItemStack(ModItems.HOLLOW_CLAYMORE.get()),AdditionalAbilities.GHOSTWALK);player.setItemInHand(InteractionHand.MAIN_HAND,stack);player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY,600,0));GhostwalkEvents.activate(player,stack);
        test.runAfterDelay(201,()->{try {test.assertFalse(GhostwalkEvents.active(player),"Ghostwalk survives ten seconds");test.assertTrue(player.hasEffect(MobEffects.INVISIBILITY),"Longer potion lost on expiry");} finally {GhostwalkEvents.cancel(player.getUUID());player.server.getPlayerList().remove(player);}test.succeed();});
    }
}
