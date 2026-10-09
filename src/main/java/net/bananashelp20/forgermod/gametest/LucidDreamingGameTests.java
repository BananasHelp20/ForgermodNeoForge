package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.SomniumWeapon;
import net.bananashelp20.forgermod.item.custom.WeaponAbilitySlot;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class LucidDreamingGameTests {
    @GameTest(template = "riftfang_test")
    public static void enemyKillGrantsSeparateTimedBuffs(GameTestHelper test) {
        var player = test.makeMockServerPlayerInLevel();
        player.setNoGravity(true);
        player.setPos(test.getBounds().getCenter());
        player.setItemInHand(InteractionHand.MAIN_HAND, TestWeapons.ready(ModItems.NIGHTMARE_DAGGER.get()));
        SomniumWeapon weapon = (SomniumWeapon)player.getMainHandItem().getItem();
        weapon.activateAbility(player, player.getMainHandItem(), WeaponAbilitySlot.SECONDARY);
        var zombie = EntityType.ZOMBIE.create(test.getLevel());
        zombie.setPos(player.position().add(0, 0, 2));
        zombie.setNoAi(true);
        test.getLevel().addFreshEntity(zombie);
        weapon.onDaggerHit(zombie, player);
        test.assertFalse(player.hasEffect(MobEffects.MOVEMENT_SPEED), "Enemy hit consumed the kill charge");
        var cow = EntityType.COW.create(test.getLevel());
        cow.hurt(player.damageSources().playerAttack(player), 100);
        test.assertFalse(player.hasEffect(MobEffects.MOVEMENT_SPEED), "Passive kill triggered Lucid Dreaming");
        zombie.hurt(player.damageSources().playerAttack(player), 100);
        var speed = player.getEffect(MobEffects.MOVEMENT_SPEED);
        test.assertTrue(speed != null && speed.getAmplifier() == 1 && speed.getDuration() == 600, "Kill did not grant thirty seconds of Speed II");
        test.assertFalse(weapon.activateAbility(player, player.getMainHandItem(), WeaponAbilitySlot.SECONDARY), "Active protection could be restarted");
        var fall = new LivingFallEvent(player, 10, 1);
        SomniumWeapon.onFall(fall);
        test.assertTrue(fall.getDamageMultiplier() == .5f, "Kill did not halve fall damage");
        test.assertTrue(weapon.abilityCooldownTicks(WeaponAbilitySlot.SECONDARY) == 300, "Cooldown is not fifteen seconds");
        weapon.cancelAbility(player, WeaponAbilitySlot.SECONDARY);
        var canceledFall = new LivingFallEvent(player, 10, 1);
        SomniumWeapon.onFall(canceledFall);
        test.assertTrue(canceledFall.getDamageMultiplier() == 1 && !player.hasEffect(MobEffects.MOVEMENT_SPEED), "Cancel failed to remove ongoing buffs");
        test.getLevel().getServer().getPlayerList().remove(player);
        test.succeed();
    }
}
