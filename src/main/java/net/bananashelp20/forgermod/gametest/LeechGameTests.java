package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.VulnusiumWeapon;
import net.bananashelp20.forgermod.item.custom.WeaponAbilitySlot;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class LeechGameTests {
    @GameTest(template = "riftfang_test")
    public static void damageBonusAndHealingBudget(GameTestHelper test) {
        var player = test.makeMockServerPlayerInLevel();
        player.setNoGravity(true);
        player.setPos(test.getBounds().getCenter());
        player.getAttribute(Attributes.MAX_HEALTH).setBaseValue(40);
        player.setHealth(5);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.ASSASSIN_DAGGER.get()));
        VulnusiumWeapon weapon = (VulnusiumWeapon)player.getMainHandItem().getItem();
        weapon.activateAbility(player, player.getMainHandItem(), WeaponAbilitySlot.SECONDARY);
        for (int hit = 0; hit < 10; hit++) {
            var target = EntityType.COW.create(test.getLevel());
            target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(hit == 0 ? 100 : 10000);
            target.setHealth(target.getMaxHealth());
            target.setPos(player.position().add(0, 0, 2));
            test.getLevel().addFreshEntity(target);
            float before = target.getHealth();
            float damage = hit < 2 ? 10 : 100;
            test.assertTrue(target.hurt(player.damageSources().playerAttack(player), damage), "Leech hit failed");
            test.assertTrue(Math.abs(before - target.getHealth() - damage * 1.1f) < .001, "Damage depends on maximum health or is not 10% extra");
            test.assertTrue(player.getHealth() <= 15.001, "Activation healed more than ten HP");
            if (hit < 2) test.assertTrue(Math.abs(player.getHealth() - (6 + hit)) < .001, "Bonus healing differs by target maximum health");
            target.discard();
        }
        test.assertTrue(Math.abs(player.getHealth() - 15) < .001, "Healing budget was not ten HP");
        test.assertFalse(weapon.isAbilityActive(player, WeaponAbilitySlot.SECONDARY), "Ten successful hits did not consume activation");
        var target = EntityType.COW.create(test.getLevel());
        target.getAttribute(Attributes.MAX_HEALTH).setBaseValue(100);
        target.setHealth(100);
        target.hurt(player.damageSources().playerAttack(player), 10);
        test.assertTrue(target.getHealth() == 90 && player.getHealth() == 15, "Uncharged hit retained Leech");
        test.getLevel().getServer().getPlayerList().remove(player);
        test.succeed();
    }
}
