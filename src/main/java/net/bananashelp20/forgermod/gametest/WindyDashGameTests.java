package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.WeaponAbilitySlot;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class WindyDashGameTests {
    @GameTest(template = "riftfang_test")
    public static void launchStartsCooldownImmediatelyWithoutCancelingStorm(GameTestHelper test) throws Exception {
        var player = test.makeMockServerPlayerInLevel();
        ItemStack stack = TestWeapons.ready(ModItems.DEAD_CALM_DAGGER.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        var runtime = Augmentations.canonical(stack);
        var context = (net.neoforged.neoforge.network.handling.IPayloadContext)java.lang.reflect.Proxy.newProxyInstance(
                net.neoforged.neoforge.network.handling.IPayloadContext.class.getClassLoader(),
                new Class<?>[]{net.neoforged.neoforge.network.handling.IPayloadContext.class},
                (proxy, method, args) -> method.getName().equals("player") ? player : null);
        var handler = net.bananashelp20.forgermod.item.custom.abilities.WeaponAbilityNetwork.class.getDeclaredMethod("handleAbility",
                net.bananashelp20.forgermod.item.custom.abilities.WeaponAbilityNetwork.UseAbilityPayload.class,
                net.neoforged.neoforge.network.handling.IPayloadContext.class);
        handler.setAccessible(true);
        String id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
        for (int slot : new int[]{0, 1}) handler.invoke(null,
                new net.bananashelp20.forgermod.item.custom.abilities.WeaponAbilityNetwork.UseAbilityPayload(slot, player.getInventory().selected, id), context);
        String key = net.bananashelp20.forgermod.item.custom.abilities.WeaponCooldownKey.of(id, WeaponAbilitySlot.SECONDARY);
        var deadlines = player.getData(net.bananashelp20.forgermod.item.custom.abilities.WeaponCooldownAttachments.COOLDOWNS);
        test.assertTrue(deadlines.get(key) == player.level().getGameTime() + 100, "Launch did not start its five-second cooldown immediately");
        test.assertTrue(runtime.isAbilityActive(player, WeaponAbilitySlot.PRIMARY), "Dash canceled Eye of the Storm");
        player.setDeltaMovement(Vec3.ZERO);
        handler.invoke(null, new net.bananashelp20.forgermod.item.custom.abilities.WeaponAbilityNetwork.UseAbilityPayload(1, player.getInventory().selected, id), context);
        test.assertTrue(player.getDeltaMovement().equals(Vec3.ZERO), "Dash ignored its cooldown");
        net.bananashelp20.forgermod.item.custom.abilities.WeaponAbilityNetwork.onLogout(
                new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(player));
        test.getLevel().getServer().getPlayerList().remove(player); test.succeed();
    }

    @GameTest(template = "riftfang_test")
    public static void allVariantsLaunchInFullLookDirectionWithoutTeleporting(GameTestHelper test) {
        var player = test.makeMockServerPlayerInLevel();
        player.setPos(test.getBounds().getCenter());
        for (var entry : ModItems.ITEMS.getEntries()) {
            if (!(entry.get() instanceof net.bananashelp20.forgermod.item.custom.TaifuniteWeapon actual) || !actual.isDagger()) continue;
            ItemStack stack = new ItemStack(actual);
            var runtime = Augmentations.canonical(stack);
            stack = Augmentations.apply(stack, runtime.abilityDescriptionKey(WeaponAbilitySlot.SECONDARY));
            player.setItemInHand(InteractionHand.MAIN_HAND, stack);
            for (float pitch : new float[]{0, -45, -90, 45, 90}) {
                player.setXRot(pitch); player.setYRot(35);
                Vec3 start = player.position();
                player.setDeltaMovement(.2, -.3, .4); player.setSprinting(true);
                test.assertTrue(runtime.activateAbility(player, stack, WeaponAbilitySlot.SECONDARY), "Dash rejected for variant/pitch");
                test.assertTrue(player.position().equals(start), "Dash changed player position directly");
                Vec3 expected = player.getLookAngle().normalize().scale(1.2);
                test.assertTrue(player.getDeltaMovement().distanceToSqr(expected) < 1.0e-12, "Dash lost pitch/direction/speed");
                test.assertTrue(player.hasImpulse && player.hurtMarked && player.isSprinting(), "Dash motion/sprint flags missing");
                test.assertFalse(runtime.isAbilityActive(player, WeaponAbilitySlot.SECONDARY), "Instant launch left ongoing movement state");
                test.assertTrue(runtime.abilityCooldownTicks(WeaponAbilitySlot.SECONDARY) == 100, "Dash base cooldown changed");
            }
        }
        test.getLevel().getServer().getPlayerList().remove(player); test.succeed();
    }

    @GameTest(template = "riftfang_test")
    public static void normalCollisionStopsDashAndLaterTicksDoNotForceMovement(GameTestHelper test) {
        var player = test.makeMockServerPlayerInLevel();
        BlockPos pos = test.absolutePos(new BlockPos(2, 2, 2));
        player.setPos(pos.getX() + .5, pos.getY(), pos.getZ() + .5);
        player.setYRot(0); player.setXRot(0);
        for (int y = 0; y < 3; y++) test.getLevel().setBlockAndUpdate(pos.offset(0, y, 1), Blocks.STONE.defaultBlockState());
        ItemStack stack = TestWeapons.ready(ModItems.DEAD_CALM_DAGGER.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        var runtime = Augmentations.canonical(stack);
        Vec3 start = player.position();
        test.assertTrue(runtime.activateAbility(player, stack, WeaponAbilitySlot.SECONDARY), "Launch against wall rejected");
        player.move(MoverType.SELF, player.getDeltaMovement());
        test.assertTrue(player.getZ() - start.z < .21 && player.horizontalCollision, "Dash passed through wall");
        Vec3 stopped = player.position(); player.setDeltaMovement(Vec3.ZERO);
        for (int i = 0; i < 8; i++) net.bananashelp20.forgermod.item.custom.TaifuniteWeapon.onPlayerTick(
                new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(player));
        test.assertTrue(player.position().equals(stopped) && player.getDeltaMovement().equals(Vec3.ZERO), "Dash kept forcing movement on subsequent ticks");
        test.getLevel().getServer().getPlayerList().remove(player); test.succeed();
    }
}
