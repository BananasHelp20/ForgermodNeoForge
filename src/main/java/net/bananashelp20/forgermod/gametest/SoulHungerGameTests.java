package net.bananashelp20.forgermod.gametest;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.MorsiumWeapon;
import net.bananashelp20.forgermod.item.custom.abilities.SoulHungerEvents;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.effect.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class SoulHungerGameTests {
    @GameTest(template="riftfang_test", timeoutTicks=460)
    public static void switchGraceExpiresOnlyOwnedHeartsAndReturningRestartsGrace(GameTestHelper test) {
        var expires=TestPlayers.survival(test);var returns=TestPlayers.survival(test);
        var enemy=EntityType.ZOMBIE.create(test.getLevel());enemy.setHealth(0);
        var stack=Augmentations.apply(new ItemStack(ModItems.HOLLOW_CLAYMORE.get()),AdditionalAbilities.SOUL_HUNGER);
        for(var player:java.util.List.of(expires,returns)) {
            player.setNoGravity(true);player.setPos(test.getBounds().getCenter());
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION,1200,1));
            player.setItemInHand(InteractionHand.MAIN_HAND,stack.copy());
            for(int i=0;i<3;i++)SoulHungerEvents.kill(new LivingDeathEvent(enemy,player.damageSources().playerAttack(player)));
            test.assertTrue(player.getAbsorptionAmount()==14,"Fixture does not have six earned HP plus eight potion HP");
            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(net.minecraft.world.item.Items.IRON_SWORD));
            SoulHungerEvents.tick(new PlayerTickEvent.Post(player));
        }
        test.runAfterDelay(100,()->{
            expires.invulnerableTime=0;expires.hurt(expires.damageSources().generic(),2);
            test.assertTrue(expires.getAbsorptionAmount()==12,"Grace hearts cannot absorb damage");
        });
        test.runAfterDelay(199,()->{
            SoulHungerEvents.tick(new PlayerTickEvent.Post(expires));
            test.assertTrue(expires.getAbsorptionAmount()==12 && returns.getAbsorptionAmount()==14,"Hearts expired before ten seconds");
            returns.setItemInHand(InteractionHand.MAIN_HAND,stack.copy());
            SoulHungerEvents.tick(new PlayerTickEvent.Post(returns));
        });
        test.runAfterDelay(200,()->{
            SoulHungerEvents.tick(new PlayerTickEvent.Post(expires));
            test.assertTrue(expires.getAbsorptionAmount()==8,"Expiry removes potion absorption or retains earned hearts");
            expires.setItemInHand(InteractionHand.MAIN_HAND,stack.copy());
            SoulHungerEvents.tick(new PlayerTickEvent.Post(expires));
            test.assertTrue(expires.getAbsorptionAmount()==8,"Returning after expiry restores expired hearts");
            SoulHungerEvents.kill(new LivingDeathEvent(enemy,expires.damageSources().playerAttack(expires)));
            test.assertTrue(expires.getAbsorptionAmount()==10,"Cannot earn new hearts after expiry");
        });
        test.runAfterDelay(210,()->{
            test.assertTrue(returns.getAbsorptionAmount()==14,"Returning before expiry does not cancel removal");
            returns.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);
            SoulHungerEvents.tick(new PlayerTickEvent.Post(returns));
        });
        test.runAfterDelay(409,()->{
            SoulHungerEvents.tick(new PlayerTickEvent.Post(returns));
            test.assertTrue(returns.getAbsorptionAmount()==14,"Second switch reuses the first deadline");
        });
        test.runAfterDelay(410,()->{
            try {
                SoulHungerEvents.tick(new PlayerTickEvent.Post(returns));
                test.assertTrue(returns.getAbsorptionAmount()==8,"Second grace period never expires");
                test.succeed();
            } finally {
                for(var player:java.util.List.of(expires,returns)) {SoulHungerEvents.end(player);player.server.getPlayerList().remove(player);}
                enemy.discard();
            }
        });
    }

    @GameTest(template="riftfang_test")
    public static void matchingOffhandSoulHungerCountsAsEquipped(GameTestHelper test) {
        var player=TestPlayers.survival(test);var enemy=EntityType.ZOMBIE.create(test.getLevel());enemy.setHealth(0);
        try {
            var off=Augmentations.apply(new ItemStack(ModItems.DEATHWISPER_DAGGER_JADE.get()),AdditionalAbilities.SOUL_HUNGER);
            off=Augmentations.apply(off,Augmentations.DUAL_WIELD);
            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModItems.DEATHWISPER_DAGGER.get()));
            player.setItemInHand(InteractionHand.OFF_HAND,off);
            SoulHungerEvents.kill(new LivingDeathEvent(enemy,player.damageSources().playerAttack(player)));
            test.assertTrue(player.getAbsorptionAmount()==2,"Matching offhand passive is not active");
            SoulHungerEvents.tick(new PlayerTickEvent.Post(player));
            test.assertTrue(player.getAbsorptionAmount()==2,"Matching offhand hearts are removed");
            player.setItemInHand(InteractionHand.MAIN_HAND,new ItemStack(ModItems.DEAD_CALM_DAGGER.get()));
            SoulHungerEvents.kill(new LivingDeathEvent(enemy,player.damageSources().playerAttack(player)));
            test.assertTrue(player.getAbsorptionAmount()==2,"Mismatched offhand still grants hearts");
            player.setItemInHand(InteractionHand.MAIN_HAND,
                    Augmentations.apply(new ItemStack(ModItems.AUGMENT_TEMPLATE.get()),AdditionalAbilities.SOUL_HUNGER));
            SoulHungerEvents.kill(new LivingDeathEvent(enemy,player.damageSources().playerAttack(player)));
            test.assertTrue(player.getAbsorptionAmount()==2,"Stored template ability counts as an equipped passive");
        } finally {SoulHungerEvents.end(player);enemy.discard();player.server.getPlayerList().remove(player);}
        test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void fifteenVariantsKeepEarnedHeartsAndPotionHeartsOnSwitch(GameTestHelper test) {
        var player=TestPlayers.survival(test);var enemy=EntityType.ZOMBIE.create(test.getLevel());enemy.setHealth(0);int count=0;
        try {for(var entry:ModItems.ITEMS.getEntries()) {
            var fresh=new ItemStack(entry.get());boolean eligible=entry.get() instanceof MorsiumWeapon;
            test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(a->a.id().equals(AdditionalAbilities.SOUL_HUNGER))==eligible,"Wrong Soul Hunger pool");if(!eligible) continue;count++;
            SoulHungerEvents.end(player);player.setItemInHand(InteractionHand.MAIN_HAND,fresh);player.removeEffect(MobEffects.ABSORPTION);player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION,600,0));player.setAbsorptionAmount(4);
            var kill=new LivingDeathEvent(enemy,player.damageSources().playerAttack(player));SoulHungerEvents.kill(kill);test.assertTrue(player.getAbsorptionAmount()==4,"Fresh weapon grants absorption");
            var stack=Augmentations.apply(fresh,AdditionalAbilities.SOUL_HUNGER);player.setItemInHand(InteractionHand.MAIN_HAND,stack);kill.setCanceled(true);SoulHungerEvents.kill(kill);test.assertTrue(player.getAbsorptionAmount()==4,"Canceled death grants hearts");kill.setCanceled(false);SoulHungerEvents.kill(kill);test.assertTrue(player.getAbsorptionAmount()==6,"First kill does not add two HP");for(int i=0;i<3;i++) SoulHungerEvents.kill(kill);test.assertTrue(player.getAbsorptionAmount()==10,"Passive exceeds six owned HP");
            player.invulnerableTime=0;player.hurt(player.damageSources().generic(),2);test.assertTrue(player.getAbsorptionAmount()==8,"Absorbed damage wrong");SoulHungerEvents.kill(kill);test.assertTrue(player.getAbsorptionAmount()==10,"Consumed hearts not replenished");
            player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION,600,1));SoulHungerEvents.tick(new PlayerTickEvent.Post(player));test.assertTrue(player.getAbsorptionAmount()==14,"Potion does not coexist with six owned hearts");
            player.setItemInHand(InteractionHand.MAIN_HAND,ItemStack.EMPTY);SoulHungerEvents.tick(new PlayerTickEvent.Post(player));test.assertTrue(player.getAbsorptionAmount()==14,"Putting weapon away removes earned or potion hearts");
            player.invulnerableTime=0;player.hurt(player.damageSources().generic(),2);test.assertTrue(player.getAbsorptionAmount()==12,"Unequipped earned hearts cannot absorb damage");
            SoulHungerEvents.kill(kill);test.assertTrue(player.getAbsorptionAmount()==12,"Unequipped weapon still earns absorption");
            player.setItemInHand(InteractionHand.MAIN_HAND,stack.copy());SoulHungerEvents.kill(kill);test.assertTrue(player.getAbsorptionAmount()==14,"Reequipping resets or fails to refill remaining owned hearts");
        }} finally {SoulHungerEvents.end(player);enemy.discard();player.server.getPlayerList().remove(player);}
        test.assertTrue(count==15,"Skipped variants");test.succeed();
    }
}
