package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.SomniumWeapon;
import net.bananashelp20.forgermod.item.custom.abilities.DaydreamerEvents;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.*;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.gametest.*;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class DaydreamerGameTests {
    @GameTest(template="riftfang_test")
    public static void fiveAxesGrantDaytimeJumpAndPreservePotions(GameTestHelper test) {
        var player=TestPlayers.survival(test);long time=test.getLevel().getDayTime();int count=0;
        try {
            test.getLevel().setDayTime(6000);
            for(var entry:ModItems.ITEMS.getEntries()) {
                var fresh=new ItemStack(entry.get());boolean eligible=entry.get() instanceof SomniumWeapon weapon && weapon.isAxe();
                test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(a->a.id().equals(AdditionalAbilities.DAYDREAMER))==eligible,"Wrong Daydreamer assignment");
                if(!eligible) continue;count++;player.setItemInHand(InteractionHand.MAIN_HAND,fresh);DaydreamerEvents.onTick(new PlayerTickEvent.Pre(player));
                test.assertFalse(player.hasEffect(MobEffects.JUMP),"Fresh axe gives Jump Boost");
                var stack=Augmentations.apply(fresh,AdditionalAbilities.DAYDREAMER);player.setItemInHand(InteractionHand.MAIN_HAND,stack);DaydreamerEvents.onTick(new PlayerTickEvent.Pre(player));
                int expectedAmplifier=JadeAugments.isJade(stack)?1:0;
                test.assertTrue(player.getEffect(MobEffects.JUMP).getAmplifier()==expectedAmplifier && player.getEffect(MobEffects.JUMP).isInfiniteDuration(),"Missing daytime Jump Boost");
                test.getLevel().setDayTime(13000);DaydreamerEvents.onTick(new PlayerTickEvent.Pre(player));test.assertFalse(player.hasEffect(MobEffects.JUMP),"Night retains Jump Boost");test.getLevel().setDayTime(6000);
                player.addEffect(new MobEffectInstance(MobEffects.JUMP,200,0));DaydreamerEvents.onTick(new PlayerTickEvent.Pre(player));
                player.addEffect(new MobEffectInstance(MobEffects.JUMP,100,2));DaydreamerEvents.onTick(new PlayerTickEvent.Pre(player));
                player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);DaydreamerEvents.onTick(new PlayerTickEvent.Pre(player));
                test.assertTrue(player.getEffect(MobEffects.JUMP)!=null && player.getEffect(MobEffects.JUMP).getAmplifier()==2 && !player.getEffect(MobEffects.JUMP).isInfiniteDuration(),"Putting axe away destroys acquired potion");
                player.removeEffect(MobEffects.JUMP);
                player.setItemInHand(InteractionHand.OFF_HAND,stack);DaydreamerEvents.onTick(new PlayerTickEvent.Pre(player));test.assertFalse(player.hasEffect(MobEffects.JUMP),"Offhand grants main-hand passive");player.setItemInHand(InteractionHand.OFF_HAND,ItemStack.EMPTY);
            }
        } finally {player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);DaydreamerEvents.onTick(new PlayerTickEvent.Pre(player));test.getLevel().setDayTime(time);player.server.getPlayerList().remove(player);}
        test.assertTrue(count==5,"Skipped Daydreamer variants");test.succeed();
    }
}
