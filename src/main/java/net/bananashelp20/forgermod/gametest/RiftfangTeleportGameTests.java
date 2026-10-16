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
    private static final class RecordingConnection extends Connection {
        final java.util.List<net.minecraft.network.protocol.Packet<?>> packets=new java.util.ArrayList<>();
        RecordingConnection(){super(PacketFlow.SERVERBOUND);}
        @Override public void send(net.minecraft.network.protocol.Packet<?> packet,net.minecraft.network.PacketSendListener listener,boolean flush){packets.add(packet);}
    }
    private static final BlockPos START = new BlockPos(8, 3, 8);

    private static ServerPlayer player(GameTestHelper test, Item main, Item off, float pitch) {
        GameProfile profile = new GameProfile(UUID.randomUUID(), "riftfang-test");
        ServerPlayer player = new ServerPlayer(test.getLevel().getServer(), test.getLevel(), profile, ClientInformation.createDefault());
        // Real listener applies absolute teleports; FakePlayer's listener discards them.
        new ServerGamePacketListenerImpl(test.getLevel().getServer(), new RecordingConnection(),
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

    @GameTest(template="riftfang_test")
    public static void voidStepPacketLeavesClientMouseLookFree(GameTestHelper test) {
        for(int x=1;x<=19;x++)for(int y=3;y<=17;y++)for(int z=1;z<=19;z++)test.setBlock(new BlockPos(x,y,z),Blocks.AIR);
        for(Item item:new Item[]{ModItems.RIFTFANG_DAGGER.get(),ModItems.RIFTFANG_DAGGER_RUBY.get(),ModItems.RIFTFANG_DAGGER_AMBER.get(),ModItems.RIFTFANG_DAGGER_AMETHYST.get(),ModItems.RIFTFANG_DAGGER_JADE.get()}) {
            var player=player(test,item,null,-55);player.setYRot(23);Vec3 start=player.position();var look=player.getLookAngle();
            test.assertTrue(weapon(player).activateAbility(player,player.getMainHandItem(),WeaponAbilitySlot.PRIMARY),"Step did not activate");
            test.assertTrue(player.position().distanceTo(start.add(look.scale(10)))<.001 && player.getYRot()==23 && player.getXRot()==-55,"Server teleport changes direction, range or rotation");
            var connection=(RecordingConnection)player.connection.getConnection();
            var packet=connection.packets.stream().filter(p->p instanceof net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket).map(p->(net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket)p).findFirst().orElseThrow();
            test.assertTrue(packet.getRelativeArguments().equals(java.util.Set.of(net.minecraft.world.entity.RelativeMovement.Y_ROT,net.minecraft.world.entity.RelativeMovement.X_ROT)) && packet.getYRot()==0 && packet.getXRot()==0,"Teleport forces absolute camera rotation instead of preserving in-flight mouse input");
            // Minecraft adds these relative offsets to the client's current look, which can be newer than the server's.
            float newerYaw=127,newerPitch=-31;
            test.assertTrue(newerYaw+packet.getYRot()==127 && newerPitch+packet.getXRot()==-31,"Late teleport packet discards newer mouse look");
        }
        test.succeed();
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
        test.assertTrue(forward.position().distanceTo(start.add(0, 0, 5.2)) < .0001, "Teleport passed through wall or stopped too late");
        for (int x = 6; x <= 10; x++) {
            for (int z = 6; z <= 10; z++) test.setBlock(new BlockPos(x, 8, z), Blocks.STONE);
        }
        ServerPlayer upward = player(test, ModItems.RIFTFANG_DAGGER.get(), null, -90);
        start = upward.position();
        test.assertTrue(weapon(upward).activateAbility(upward, upward.getMainHandItem(), WeaponAbilitySlot.PRIMARY), "Short clear segment before ceiling failed");
        test.assertTrue(upward.position().distanceTo(start.add(0, 3.2, 0)) < .0001, "Player body passed through ceiling");
        test.succeed();
    }

    @GameTest(template = "riftfang_test")
    public static void shortClearPathsStopAtCeilingsAndWorldHeight(GameTestHelper test) {
        test.setBlock(START.above(2), Blocks.STONE);
        ServerPlayer blocked = player(test, ModItems.RIFTFANG_DAGGER.get(), null, -90);
        Vec3 start = blocked.position();
        test.assertTrue(weapon(blocked).activateAbility(blocked, blocked.getMainHandItem(), WeaponAbilitySlot.PRIMARY), "Short available path is rejected");
        test.assertTrue(Math.abs(blocked.getBoundingBox().maxY-test.absolutePos(START.above(2)).getY())<.0001, "Short step does not stop just before ceiling");
        blocked.setPos(blocked.getX(),test.absolutePos(START.above(2)).getY()-blocked.getBbHeight(),blocked.getZ());start=blocked.position();
        test.assertFalse(weapon(blocked).activateAbility(blocked,blocked.getMainHandItem(),WeaponAbilitySlot.PRIMARY),"Zero-distance path activates");
        test.assertTrue(blocked.position().equals(start),"Blocked step moves the player");
        ServerPlayer high = player(test, ModItems.RIFTFANG_DAGGER.get(), null, -90);
        high.setPos(high.getX(), test.getLevel().getMaxBuildHeight() - high.getBbHeight() - .05, high.getZ());
        test.assertTrue(weapon(high).activateAbility(high, high.getMainHandItem(), WeaponAbilitySlot.PRIMARY), "Available distance below world height is rejected");
        test.assertTrue(high.getBoundingBox().maxY<=test.getLevel().getMaxBuildHeight() && test.getLevel().getMaxBuildHeight()-high.getBoundingBox().maxY<.0001,"Step crosses world height or stops too early");
        test.succeed();
    }

    @GameTest(template="riftfang_test")
    public static void downwardAimThroughGrassContinuesAlongGround(GameTestHelper test) {
        for(int x=7;x<=9;x++)for(int z=8;z<=20;z++) {
            test.setBlock(new BlockPos(x,2,z),Blocks.GRASS_BLOCK);
            for(int y=3;y<=5;y++)test.setBlock(new BlockPos(x,y,z),Blocks.AIR);
        }
        test.setBlock(new BlockPos(8,3,9),Blocks.SHORT_GRASS);
        test.setBlock(new BlockPos(8,3,10),Blocks.TALL_GRASS);
        test.setBlock(new BlockPos(8,4,10),Blocks.TALL_GRASS.defaultBlockState().setValue(net.minecraft.world.level.block.DoublePlantBlock.HALF,net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER));
        for(var item:new Item[]{ModItems.RIFTFANG_DAGGER.get(),ModItems.RIFTFANG_DAGGER_RUBY.get(),ModItems.RIFTFANG_DAGGER_AMBER.get(),ModItems.RIFTFANG_DAGGER_AMETHYST.get(),ModItems.RIFTFANG_DAGGER_JADE.get()}) {
            var player=player(test,item,null,35);var start=player.position();
            test.assertTrue(weapon(player).activateAbility(player,player.getMainHandItem(),WeaponAbilitySlot.PRIMARY),"Looking down at grass rejects step");
            test.assertTrue(player.position().distanceTo(start.add(0,0,10))<.0001 && test.getLevel().noCollision(player,player.getBoundingBox()),"Grass obstructs step or downward look enters the ground");
        }
        test.succeed();
    }

    @GameTest(template="riftfang_test")
    public static void wallCloserThanHalfABlockStillAllowsAvailableDistance(GameTestHelper test) {
        for(int x=6;x<=10;x++)for(int y=2;y<=7;y++)test.setBlock(new BlockPos(x,y,9),Blocks.STONE);
        var player=player(test,ModItems.RIFTFANG_DAGGER.get(),null,0);var start=player.position();
        test.assertTrue(weapon(player).activateAbility(player,player.getMainHandItem(),WeaponAbilitySlot.PRIMARY),"Nearby wall rejects a nonzero clear path");
        test.assertTrue(Math.abs(player.getBoundingBox().maxZ-test.absolutePos(new BlockPos(8,3,9)).getZ())<.0001 && player.getZ()>start.z,"Step is not as far as possible before nearby wall");
        test.assertTrue(test.getLevel().noCollision(player,player.getBoundingBox()),"Nearby-wall step clips into wall");
        test.succeed();
    }
}
