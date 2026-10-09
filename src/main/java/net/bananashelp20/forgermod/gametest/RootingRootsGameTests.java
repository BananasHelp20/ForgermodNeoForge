package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.LushWeapon;
import net.bananashelp20.forgermod.item.custom.WeaponAbilitySlot;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class RootingRootsGameTests {
    private static Creeper target(GameTestHelper test, Vec3 position) {
        Creeper target = EntityType.CREEPER.create(test.getLevel());
        target.setNoAi(true);
        target.setNoGravity(true);
        target.setPos(position);
        test.getLevel().addFreshEntity(target);
        return target;
    }

    @GameTest(template = "riftfang_test")
    public static void rootsReachTenBlocks(GameTestHelper test) {
        var player = test.makeMockServerPlayerInLevel();
        player.setNoGravity(true);
        Vec3 start = Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8, 8, 8)));
        player.setPos(start);
        player.setItemInHand(InteractionHand.MAIN_HAND, TestWeapons.ready(ModItems.LEAFCUTTER_DAGGER.get()));
        LushWeapon weapon = (LushWeapon)player.getMainHandItem().getItem();
        Creeper inside = target(test, start.add(0, 0, 9.5));
        Creeper edge = target(test, start.add(10, 0, 0));
        Creeper outside = target(test, start.add(10.25, 0, 0));
        test.assertTrue(weapon.activateAbility(player, player.getMainHandItem(), WeaponAbilitySlot.PRIMARY), "No roots activated at expanded range");
        test.assertTrue(inside.getEffect(MobEffects.POISON).getDuration() == 200 && edge.hasEffect(MobEffects.POISON), "Expanded targets were not poisoned");
        test.assertFalse(outside.hasEffect(MobEffects.POISON), "Root range exceeded ten blocks");
        inside.setPos(inside.position().add(1, 0, 0));
        test.runAfterDelay(3, () -> {
            test.assertTrue(Math.abs(inside.getX() - start.x) < .0001, "Expanded target was not anchored");
            test.assertTrue(weapon.abilityCooldownTicks(WeaponAbilitySlot.PRIMARY) == 1200, "Root cooldown changed");
            weapon.cancelAbility(player, WeaponAbilitySlot.PRIMARY);
            test.getLevel().getServer().getPlayerList().remove(player);
            test.succeed();
        });
    }
}
