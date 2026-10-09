package net.bananashelp20.forgermod.gametest;

import com.mojang.authlib.GameProfile;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.InanisiumWeapon;
import net.bananashelp20.forgermod.item.custom.WeaponAbilitySlot;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class RiftfangTeleportGameTests {
    private static final BlockPos START = new BlockPos(8, 3, 8);

    private static ServerPlayer player(GameTestHelper test, Item main, Item off, float pitch) {
        GameProfile profile = new GameProfile(UUID.randomUUID(), "riftfang-test");
        ServerPlayer player = new ServerPlayer(test.getLevel().getServer(), test.getLevel(), profile, ClientInformation.createDefault());
        // Real listener applies absolute teleports; FakePlayer's listener discards them.
        new ServerGamePacketListenerImpl(test.getLevel().getServer(), new Connection(PacketFlow.SERVERBOUND),
                player, CommonListenerCookie.createInitial(profile, false));
        player.setPos(Vec3.atBottomCenterOf(test.absolutePos(START)));
        player.setYRot(0);
        player.setXRot(pitch);
        player.setItemInHand(InteractionHand.MAIN_HAND, TestWeapons.ready(main));
        player.setItemInHand(InteractionHand.OFF_HAND, off == null ? ItemStack.EMPTY : new ItemStack(off));
        // Tests explicitly load their clear test path, never depending on neighboring tests' chunks.
        for (int x = -1; x <= 2; x++) {
            for (int z = -1; z <= 2; z++) {
                BlockPos pos = test.absolutePos(START).offset(x * 16, 0, z * 16);
                test.getLevel().getChunk(pos.getX() >> 4, pos.getZ() >> 4);
            }
        }
        return player;
    }

    private static InanisiumWeapon weapon(ServerPlayer player) {
        return (InanisiumWeapon)player.getMainHandItem().getItem();
    }

    private static void expectDistance(GameTestHelper test, ServerPlayer player, double range) {
        Vec3 start = player.position();
        Vec3 expected = start.add(player.getLookAngle().normalize().scale(range));
        test.assertTrue(weapon(player).activateAbility(player, player.getMainHandItem(), WeaponAbilitySlot.PRIMARY), "Teleport failed on a clear path");
        test.assertTrue(player.position().distanceTo(expected) < .0001, "Teleport ignored pitch or used the wrong range: " + player.position() + " expected " + expected);
        test.assertTrue(test.getLevel().noCollision(player, player.getBoundingBox()), "Teleported into solid blocks");
    }

    @GameTest(template = "riftfang_test")
    public static void upwardAndDiagonalLook(GameTestHelper test) {
        expectDistance(test, player(test, ModItems.RIFTFANG_DAGGER.get(), null, -90), 10);
        expectDistance(test, player(test, ModItems.RIFTFANG_DAGGER.get(), null, -45), 10);
        ServerPlayer down = player(test, ModItems.RIFTFANG_DAGGER.get(), null, 90);
        down.setPos(down.position().add(0, 20, 0));
        expectDistance(test, down, 10);
        test.succeed();
    }

    @GameTest(template = "riftfang_test")
    public static void allRiftfangVariantsDoubleRange(GameTestHelper test) {
        Item[] variants = {ModItems.RIFTFANG_DAGGER.get(), ModItems.RIFTFANG_DAGGER_RUBY.get(),
                ModItems.RIFTFANG_DAGGER_AMBER.get(), ModItems.RIFTFANG_DAGGER_AMETHYST.get(), ModItems.RIFTFANG_DAGGER_JADE.get()};
        for (Item main : variants) {
            for (Item off : variants) {
                ServerPlayer player = player(test, main, off, -45);
                expectDistance(test, player, 20);
                test.assertTrue(weapon(player).abilityCooldownTicks(WeaponAbilitySlot.PRIMARY) == 140, "Primary cooldown is not seven seconds");
                test.assertTrue(weapon(player).abilityCooldownTicks(WeaponAbilitySlot.SECONDARY) == 2400, "Secondary cooldown changed");
            }
        }
        test.succeed();
    }

    @GameTest(template = "riftfang_test")
    public static void otherOffhandsDoNotDoubleRange(GameTestHelper test) {
        for (Item off : new Item[]{ModItems.EMBERFANG_DAGGER.get(), ModItems.CLAYMORE_OF_THE_VOID.get(),
                ModItems.NULLIFIED_AXE.get(), ModItems.DAMASK_KNIFE.get()}) {
            expectDistance(test, player(test, ModItems.RIFTFANG_DAGGER.get(), off, 0), 10);
        }
        test.succeed();
    }

    @GameTest(template = "riftfang_test")
    public static void wallsAndCeilingsStopTeleport(GameTestHelper test) {
        for (int x = 6; x <= 10; x++) {
            for (int y = 2; y <= 7; y++) test.setBlock(new BlockPos(x, y, 14), Blocks.STONE);
        }
        ServerPlayer forward = player(test, ModItems.RIFTFANG_DAGGER.get(), ModItems.RIFTFANG_DAGGER_JADE.get(), 0);
        Vec3 start = forward.position();
        test.assertTrue(weapon(forward).activateAbility(forward, forward.getMainHandItem(), WeaponAbilitySlot.PRIMARY), "Short clear segment before wall failed");
        test.assertTrue(forward.position().distanceTo(start.add(0, 0, 5)) < .0001, "Teleport passed through wall or stopped too late");
        for (int x = 6; x <= 10; x++) {
            for (int z = 6; z <= 10; z++) test.setBlock(new BlockPos(x, 8, z), Blocks.STONE);
        }
        ServerPlayer upward = player(test, ModItems.RIFTFANG_DAGGER.get(), null, -90);
        start = upward.position();
        test.assertTrue(weapon(upward).activateAbility(upward, upward.getMainHandItem(), WeaponAbilitySlot.PRIMARY), "Short clear segment before ceiling failed");
        test.assertTrue(upward.position().distanceTo(start.add(0, 3, 0)) < .0001, "Player body passed through ceiling");
        test.succeed();
    }

    @GameTest(template = "riftfang_test")
    public static void blockedAndWorldHeightDoNotTeleport(GameTestHelper test) {
        test.setBlock(START.above(2), Blocks.STONE);
        ServerPlayer blocked = player(test, ModItems.RIFTFANG_DAGGER.get(), null, -90);
        Vec3 start = blocked.position();
        test.assertFalse(weapon(blocked).activateAbility(blocked, blocked.getMainHandItem(), WeaponAbilitySlot.PRIMARY), "Too-short path activated");
        test.assertTrue(blocked.position().equals(start), "Blocked teleport moved the player");
        ServerPlayer high = player(test, ModItems.RIFTFANG_DAGGER.get(), null, -90);
        high.setPos(high.getX(), test.getLevel().getMaxBuildHeight() - high.getBbHeight() - .05, high.getZ());
        start = high.position();
        test.assertFalse(weapon(high).activateAbility(high, high.getMainHandItem(), WeaponAbilitySlot.PRIMARY), "Teleport escaped world height");
        test.assertTrue(high.position().equals(start), "Invalid-height teleport moved the player");
        test.succeed();
    }
}
