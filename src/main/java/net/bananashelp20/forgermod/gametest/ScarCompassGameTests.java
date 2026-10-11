package net.bananashelp20.forgermod.gametest;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.VulnusiumWeapon;
import net.bananashelp20.forgermod.item.custom.abilities.ScarCompassEvents;
import net.minecraft.gametest.framework.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.effect.*;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.gametest.*;
@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class ScarCompassGameTests {
    @GameTest(template="riftfang_test")
    public static void fifteenVariantsMoveGlowAndPreserveOtherOwnersAndPotions(GameTestHelper test) {
        var player=TestPlayers.survival(test);var second=TestPlayers.survival(test);var mob=EntityType.ZOMBIE.create(test.getLevel());mob.setNoAi(true);mob.getAttribute(Attributes.ARMOR).setBaseValue(0);var other=EntityType.ZOMBIE.create(test.getLevel());other.setNoAi(true);other.getAttribute(Attributes.ARMOR).setBaseValue(0);int count=0;
        try {for(var entry:ModItems.ITEMS.getEntries()) {
            var fresh=new ItemStack(entry.get());boolean eligible=entry.get() instanceof VulnusiumWeapon;
            test.assertTrue(Augmentations.pool(fresh).stream().anyMatch(a->a.id().equals(AdditionalAbilities.SCAR_COMPASS))==eligible,"Wrong Scar pool");if(!eligible) continue;count++;
            mob.setHealth(20);mob.invulnerableTime=0;player.setItemInHand(InteractionHand.MAIN_HAND,fresh);mob.hurt(player.damageSources().playerAttack(player),1);test.assertFalse(mob.hasEffect(MobEffects.GLOWING),"Fresh weapon glows");
            var stack=Augmentations.apply(fresh,AdditionalAbilities.SCAR_COMPASS);player.setItemInHand(InteractionHand.MAIN_HAND,stack);mob.addEffect(new MobEffectInstance(MobEffects.GLOWING,100,0));mob.invulnerableTime=0;mob.hurt(player.damageSources().playerAttack(player),1);
            test.assertTrue(mob.getEffect(MobEffects.GLOWING).getDuration()==12000,"Mark is not ten minutes");
            second.setItemInHand(InteractionHand.MAIN_HAND,stack.copy());mob.invulnerableTime=0;mob.hurt(second.damageSources().playerAttack(second),1);ScarCompassEvents.end(player.getUUID());test.assertTrue(mob.getEffect(MobEffects.GLOWING).getDuration()==12000,"One owner removes another mark");ScarCompassEvents.end(second.getUUID());test.assertTrue(mob.getEffect(MobEffects.GLOWING).getDuration()==100,"Prior potion not restored");mob.removeEffect(MobEffects.GLOWING);
            mob.invulnerableTime=0;mob.hurt(player.damageSources().playerAttack(player),1);other.setHealth(20);other.invulnerableTime=0;other.hurt(player.damageSources().playerAttack(player),1);test.assertFalse(mob.hasEffect(MobEffects.GLOWING),"Old target stays marked");test.assertTrue(other.hasEffect(MobEffects.GLOWING),"New target not marked");
            other.addEffect(new MobEffectInstance(MobEffects.GLOWING,80,0));player.setItemInHand(InteractionHand.MAIN_HAND,stack.copy());ScarCompassEvents.tick(new ServerTickEvent.Post(()->true,player.server));test.assertTrue(other.getEffect(MobEffects.GLOWING).getDuration()==80,"External glow lost on switch");other.removeEffect(MobEffects.GLOWING);
            mob.addEffect(new MobEffectInstance(MobEffects.GLOWING,60,0));mob.invulnerableTime=0;mob.hurt(player.damageSources().playerAttack(player),1);mob.removeEffect(MobEffects.GLOWING);mob.invulnerableTime=0;mob.hurt(player.damageSources().playerAttack(player),1);ScarCompassEvents.end(player.getUUID());test.assertFalse(mob.hasEffect(MobEffects.GLOWING),"Dispelled prior potion resurrected after a new hit");
        }} finally {ScarCompassEvents.end(player.getUUID());ScarCompassEvents.end(second.getUUID());mob.discard();other.discard();player.server.getPlayerList().remove(player);second.server.getPlayerList().remove(second);}
        test.assertTrue(count==15,"Skipped variants");test.succeed();
    }
}
