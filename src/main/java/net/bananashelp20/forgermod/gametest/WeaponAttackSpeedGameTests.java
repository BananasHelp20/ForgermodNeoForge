package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.ModSpecialRegistry;
import net.bananashelp20.forgermod.item.custom.SwordItemWithEffect;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class WeaponAttackSpeedGameTests {
    private static Player player(GameTestHelper test) {
        Player player = test.makeMockPlayer(GameType.CREATIVE);
        player.setNoGravity(true);
        player.setPos(test.getBounds().getCenter());
        return player;
    }

    private static double equip(Player player, Item item) {
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(item));
        player.tick(); // Apply real equipment attribute changes, including removal of the previous weapon.
        return player.getAttributeValue(Attributes.ATTACK_SPEED);
    }

    @GameTest(template = "empty")
    public static void everyWeaponRecharges(GameTestHelper test) {
        Player player = player(test);
        int checked = 0;
        for (var entry : ModItems.ITEMS.getEntries()) {
            if (!(entry.get() instanceof SwordItem)) continue;
            double speed = equip(player, entry.get());
            test.assertTrue(Double.isFinite(speed) && speed > 0, "Nonpositive attack speed: " + entry.getId() + " = " + speed);
            float delay = player.getCurrentItemAttackStrengthDelay();
            test.assertTrue(Float.isFinite(delay) && delay > 0 && delay < 100, "Invalid recharge time: " + entry.getId());
            player.resetAttackStrengthTicker();
            test.assertTrue(player.getAttackStrengthScale(0) == 0, "Attack did not reset");
            for (int i = 0; i < Math.ceil(delay) + 1; i++) player.tick();
            test.assertTrue(player.getAttackStrengthScale(0) > .999f, "Attack never recovered: " + entry.getId());
            if (entry.get() instanceof SwordItemWithEffect weapon) {
                float expected = weapon.isAxe() ? .9f : weapon.isDagger() ? 3f : 1.6f;
                if (entry.getId().getPath().endsWith("_amethyst")) expected += .4f;
                test.assertTrue(Math.abs(speed - expected) < .00001, "Wrong type/gemstone speed: " + entry.getId());
            }
            checked++;
        }
        test.assertTrue(checked >= 140, "Weapon check skipped registered variants");
        test.succeed();
    }

    @GameTest(template = "empty")
    public static void ordinaryAndRustySpeedOrdering(GameTestHelper test) {
        Player player = player(test);
        double axe = equip(player, ModItems.CARBON_STEEL_AXE.get());
        double claymore = equip(player, ModItems.CLAYMORE.get());
        double dagger = equip(player, ModItems.CARBON_STEEL_DAGGER.get());
        test.assertTrue(axe < claymore && claymore < dagger, "Ordinary weapons do not follow axe < claymore < dagger");
        double rustyAxe = equip(player, ModItems.RUSTY_AXE.get());
        double rustyClaymore = equip(player, ModItems.RUSTY_CLAYMORE.get());
        double rustyDagger = equip(player, ModItems.RUSTY_DAGGER.get());
        test.assertTrue(rustyAxe < rustyClaymore && rustyClaymore < rustyDagger, "Rusty weapons do not follow axe < claymore < dagger");
        test.assertTrue(Math.abs(axe - .9) < .00001 && Math.abs(rustyAxe - .6) < .00001, "Ordinary axes did not receive usable speeds");
        test.assertTrue(Math.abs(equip(player, ModItems.STEEL_SWORD.get()) - 1.6) < .00001, "Ordinary sword speed changed");
        test.assertTrue(Math.abs(equip(player, ModItems.STUMPFL_BAT.get()) - 3) < .00001, "Creative bat speed changed");
        test.succeed();
    }

    @GameTest(template = "empty")
    public static void invalidSpeedsAreRejected(GameTestHelper test) {
        for (float value : new float[]{0, -1, Float.NaN, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY}) {
            boolean rejected = false;
            try { ModSpecialRegistry.attackSpeedModifier(value); }
            catch (IllegalArgumentException expected) { rejected = true; }
            test.assertTrue(rejected, "Invalid configured attack speed was accepted: " + value);
        }
        test.succeed();
    }
}
