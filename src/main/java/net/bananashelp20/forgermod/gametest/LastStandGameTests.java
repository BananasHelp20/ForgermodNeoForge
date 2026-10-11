package net.bananashelp20.forgermod.gametest;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.MorsiumWeapon;
import net.bananashelp20.forgermod.item.custom.abilities.LastStandEvents;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class LastStandGameTests {
    @GameTest(template="riftfang_test")
    public static void fifteenVariantsSurviveOneFatalHitAndSharePersistentCooldown(GameTestHelper test) {
        var player=TestPlayers.survival(test);var clone=TestPlayers.survival(test);int count=0;
        try {for(var entry:ModItems.ITEMS.getEntries()) {
            var fresh=new ItemStack(entry.get());boolean eligible=entry.get() instanceof MorsiumWeapon;
            test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(a->a.id().equals(AdditionalAbilities.LAST_STAND))==eligible,"Wrong Last Stand pool");if(!eligible) continue;count++;
            player.setHealth(20);player.setItemInHand(InteractionHand.MAIN_HAND,fresh);player.getPersistentData().remove(LastStandEvents.COOLDOWN);
            var pre=new LivingDamageEvent.Pre(player,new DamageContainer(player.damageSources().generic(),100));LastStandEvents.damage(pre);test.assertTrue(pre.getNewDamage()==100,"Fresh weapon prevents death");
            var stack=Augmentations.apply(fresh,AdditionalAbilities.LAST_STAND);player.setItemInHand(InteractionHand.MAIN_HAND,stack);player.removeEffect(MobEffects.ABSORPTION);player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION,200,0));player.setAbsorptionAmount(4);
            pre=new LivingDamageEvent.Pre(player,new DamageContainer(player.damageSources().generic(),22));LastStandEvents.damage(pre);test.assertTrue(pre.getNewDamage()==22 && !player.getPersistentData().contains(LastStandEvents.COOLDOWN),"Nonfatal absorbed hit spends Last Stand");
            player.invulnerableTime=0;player.hurt(player.damageSources().generic(),100);test.assertTrue(player.isAlive() && player.getHealth()==1 && player.getAbsorptionAmount()==0,"Fatal hit not reduced to one HP after absorption");
            long deadline=player.getPersistentData().getLong(LastStandEvents.COOLDOWN);test.assertTrue(deadline==player.server.overworld().getGameTime()+2400,"Cooldown not two minutes");
            player.setHealth(20);player.setItemInHand(InteractionHand.MAIN_HAND,stack.copy());pre=new LivingDamageEvent.Pre(player,new DamageContainer(player.damageSources().generic(),100));LastStandEvents.damage(pre);test.assertTrue(pre.getNewDamage()==100,"New stack bypasses cooldown");
            LastStandEvents.clone(new PlayerEvent.Clone(clone,player,true));test.assertTrue(clone.getPersistentData().getLong(LastStandEvents.COOLDOWN)==deadline,"Respawn drops cooldown");
            player.getPersistentData().remove(LastStandEvents.COOLDOWN);player.setHealth(1);pre=new LivingDamageEvent.Pre(player,new DamageContainer(player.damageSources().generic(),2));LastStandEvents.damage(pre);test.assertTrue(pre.getNewDamage()==2,"One HP incorrectly protected");
            player.setHealth(20);player.getPersistentData().putLong(LastStandEvents.COOLDOWN,player.server.overworld().getGameTime());pre=new LivingDamageEvent.Pre(player,new DamageContainer(player.damageSources().generic(),100));LastStandEvents.damage(pre);test.assertTrue(pre.getNewDamage()==19,"Expired cooldown not usable");
        }} finally {player.server.getPlayerList().remove(player);clone.server.getPlayerList().remove(clone);}
        test.assertTrue(count==15,"Skipped variants");test.succeed();
    }
}
