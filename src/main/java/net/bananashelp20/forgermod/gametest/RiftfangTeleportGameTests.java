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
        player.setItemInHand(InteractionHand.MAIN_HAND, net.bananashelp20.forgermod.augmentation.Augmentations.setLevel(TestWeapons.ready(main),"tooltips.forgermod.ability.void_step",2));
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
    public static void diagonalCornerOutsideActualBodySweepDoesNotStopBlink(GameTestHelper test){
        for(int x=7;x<=20;x++)for(int y=2;y<=6;y++)for(int z=7;z<=20;z++)test.setBlock(new BlockPos(x,y,z),Blocks.AIR);
        test.setBlock(new BlockPos(8,3,9),Blocks.STONE);test.setBlock(new BlockPos(8,4,9),Blocks.STONE);
        var player=player(test,ModItems.RIFTFANG_DAGGER.get(),null,0);player.setPos(Vec3.atLowerCornerOf(test.absolutePos(START)).add(.82,0,.18));player.setYRot(-45);var start=player.position();var expected=start.add(player.getLookAngle().normalize().scale(10));
        test.assertTrue(weapon(player).activateAbility(player,player.getMainHandItem(),WeaponAbilitySlot.PRIMARY),"Clear diagonal blink rejected");
        test.assertTrue(player.position().distanceTo(expected)<.0001 && test.getLevel().noCollision(player,player.getBoundingBox()),"Conservative broad-phase corner stops an actually clear diagonal blink");test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void sweptBodyDistinguishesCornerMissFromRealContact(GameTestHelper test){
        var box=new net.minecraft.world.phys.AABB(0,0,0,.6,1.8,.6);var step=new Vec3(.25,0,.25);
        test.assertTrue(net.bananashelp20.forgermod.item.custom.abilities.VoidStepPath.collisionFraction(box,step,new net.minecraft.world.phys.AABB(-.5,0,.74,.08,2,1))==1,"Diagonal broad-phase corner is mistaken for collision");
        double hit=net.bananashelp20.forgermod.item.custom.abilities.VoidStepPath.collisionFraction(box,step,new net.minecraft.world.phys.AABB(.8,0,.8,1,2,1));
        test.assertTrue(Math.abs(hit-.8)<.0001,"True diagonal contact misses its earliest safe distance");test.succeed();
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
                player.setItemInHand(InteractionHand.OFF_HAND, net.bananashelp20.forgermod.augmentation.Augmentations.apply(
                        new ItemStack(off),"tooltips.forgermod.ability.void_step"));
                expectDistance(test, player, 20);
                test.assertTrue(weapon(player).abilityCooldownTicks(WeaponAbilitySlot.PRIMARY) == 140, "Primary cooldown is not seven seconds");
                test.assertTrue(weapon(player).abilityCooldownTicks(WeaponAbilitySlot.SECONDARY) == 2400, "Secondary cooldown changed");
            }
        }
        test.succeed();
    }

    @GameTest(template="riftfang_test")
    public static void doubleRangeRequiresBothAugmentsAndDualWieldOnEitherHand(GameTestHelper test) {
        String id="tooltips.forgermod.ability.void_step";
        for(var item:new Item[]{ModItems.RIFTFANG_DAGGER.get(),ModItems.RIFTFANG_DAGGER_RUBY.get(),ModItems.RIFTFANG_DAGGER_AMBER.get(),ModItems.RIFTFANG_DAGGER_AMETHYST.get(),ModItems.RIFTFANG_DAGGER_JADE.get()}) {
            var offItem=item==ModItems.RIFTFANG_DAGGER_RUBY.get()?ModItems.RIFTFANG_DAGGER.get():ModItems.RIFTFANG_DAGGER_RUBY.get();
            for(int mode=0;mode<4;mode++) {
                var player=player(test,item,offItem,-45);
                var main=net.bananashelp20.forgermod.augmentation.Augmentations.apply(new ItemStack(item),id);
                var off=net.bananashelp20.forgermod.augmentation.Augmentations.apply(new ItemStack(offItem),id);
                if(mode==1 || mode==3)main=net.bananashelp20.forgermod.augmentation.Augmentations.apply(main,net.bananashelp20.forgermod.augmentation.Augmentations.DUAL_WIELD);
                if(mode==2)off=net.bananashelp20.forgermod.augmentation.Augmentations.apply(off,net.bananashelp20.forgermod.augmentation.Augmentations.DUAL_WIELD);
                if(mode==3)off=new ItemStack(offItem);
                player.setItemInHand(InteractionHand.MAIN_HAND,main);player.setItemInHand(InteractionHand.OFF_HAND,off);
                expectDistance(test,player,mode==1 || mode==2?10:5);
                for(boolean compact:new boolean[]{false,true}) {
                    var text=net.bananashelp20.forgermod.augmentation.AbilityDescriptions.description(main,id,1,compact);var white=new StringBuilder();
                    text.visit((style,part)->{if(style.getColor()!=null && style.getColor().getValue()==net.minecraft.ChatFormatting.WHITE.getColor())white.append(part);return java.util.Optional.<Void>empty();},net.minecraft.network.chat.Style.EMPTY);
                    test.assertTrue(text.getString().equals("On activation, teleport 5 blocks in the direction you look. Having this ability on both daggers in dual wield doubles the range.") && white.toString().equals("5 blocks"),"Void Step description does not show the approved base range and doubling rule");
                }
            }
            var missing=player(test,item,offItem,-45);missing.setItemInHand(InteractionHand.MAIN_HAND,net.bananashelp20.forgermod.augmentation.Augmentations.apply(new ItemStack(item),net.bananashelp20.forgermod.augmentation.Augmentations.DUAL_WIELD));missing.setItemInHand(InteractionHand.OFF_HAND,net.bananashelp20.forgermod.augmentation.Augmentations.apply(new ItemStack(offItem),id));
            var start=missing.position();test.assertFalse(weapon(missing).activateAbility(missing,missing.getMainHandItem(),WeaponAbilitySlot.PRIMARY),"Offhand augment incorrectly grants the missing main-hand ability");test.assertTrue(missing.position().equals(start),"Missing main-hand ability still teleports");
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
    public static void passableGrassDoesNotBlockBlinkAndDownwardAimLandsOnVisibleFloor(GameTestHelper test) {
        for(int x=7;x<=9;x++)for(int z=8;z<=20;z++) {
            test.setBlock(new BlockPos(x,2,z),Blocks.GRASS_BLOCK);
            for(int y=3;y<=5;y++)test.setBlock(new BlockPos(x,y,z),Blocks.AIR);
        }
        test.setBlock(new BlockPos(8,3,9),Blocks.SHORT_GRASS);
        test.setBlock(new BlockPos(8,3,10),Blocks.TALL_GRASS);
        test.setBlock(new BlockPos(8,4,10),Blocks.TALL_GRASS.defaultBlockState().setValue(net.minecraft.world.level.block.DoublePlantBlock.HALF,net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER));
        for(var item:new Item[]{ModItems.RIFTFANG_DAGGER.get(),ModItems.RIFTFANG_DAGGER_RUBY.get(),ModItems.RIFTFANG_DAGGER_AMBER.get(),ModItems.RIFTFANG_DAGGER_AMETHYST.get(),ModItems.RIFTFANG_DAGGER_JADE.get()}) {
            var player=player(test,item,null,0);var start=player.position();
            test.assertTrue(weapon(player).activateAbility(player,player.getMainHandItem(),WeaponAbilitySlot.PRIMARY),"Passable grass rejects level blink");
            test.assertTrue(player.position().distanceTo(start.add(0,0,10))<.0001 && test.getLevel().noCollision(player,player.getBoundingBox()),"Grass obstructs step or downward look enters the ground");
            player.setPos(start);player.setXRot(35);
            var eye=player.getEyePosition();var look=player.getLookAngle().normalize();
            var ground=eye.add(look.scale((start.y-eye.y)/look.y));
            test.assertTrue(weapon(player).activateAbility(player,player.getMainHandItem(),WeaponAbilitySlot.PRIMARY),"Downward blink rejects the visible floor beneath grass");
            test.assertTrue(player.position().distanceTo(ground)<.0001 && test.getLevel().noCollision(player,player.getBoundingBox()),"Downward blink does not land on the aimed floor surface: "+player.position()+" expected "+ground);
            player.setPos(start.add(0,2,0));var raised=player.position();
            test.assertTrue(weapon(player).activateAbility(player,player.getMainHandItem(),WeaponAbilitySlot.PRIMARY),"Clear downward path rejected");
            var raisedEye=raised.add(0,player.getEyeHeight(),0);double distance=(raisedEye.y-start.y)/-look.y;
            test.assertTrue(player.position().distanceTo(raisedEye.add(look.scale(distance)))<.0001 && test.getLevel().noCollision(player,player.getBoundingBox()),"Downward blink fails to land on its eye-ray floor target");
        }
        test.succeed();
    }

    private static void clearGroundedPath(GameTestHelper test) {
        for(int x=6;x<=10;x++)for(int z=7;z<=20;z++) {
            test.setBlock(new BlockPos(x,2,z),Blocks.GRASS_BLOCK);
            for(int y=3;y<=8;y++)test.setBlock(new BlockPos(x,y,z),Blocks.AIR);
        }
    }
    private static void aimAt(ServerPlayer player,Vec3 target) {
        var delta=target.subtract(player.getEyePosition());
        player.setYRot((float)Math.toDegrees(Math.atan2(-delta.x,delta.z)));
        player.setXRot((float)-Math.toDegrees(Math.atan2(delta.y,delta.horizontalDistance())));
    }
    @GameTest(template="riftfang_test")
    public static void nearbyBlockAndSlabTopsWorkThroughTallGrassForEveryVariant(GameTestHelper test) {
        clearGroundedPath(test);
        test.setBlock(new BlockPos(8,3,9),Blocks.TALL_GRASS);
        test.setBlock(new BlockPos(8,4,9),Blocks.TALL_GRASS.defaultBlockState().setValue(net.minecraft.world.level.block.DoublePlantBlock.HALF,net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER));
        for(var block:new net.minecraft.world.level.block.Block[]{Blocks.STONE,Blocks.STONE_SLAB}) {
            test.setBlock(new BlockPos(8,3,10),block);
            var target=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,3,10))).add(0,block==Blocks.STONE?1:.5,0);
            for(var item:new Item[]{ModItems.RIFTFANG_DAGGER.get(),ModItems.RIFTFANG_DAGGER_RUBY.get(),ModItems.RIFTFANG_DAGGER_AMBER.get(),ModItems.RIFTFANG_DAGGER_AMETHYST.get(),ModItems.RIFTFANG_DAGGER_JADE.get()}) {
                var player=player(test,item,null,0);aimAt(player,target);
                test.assertTrue(weapon(player).activateAbility(player,player.getMainHandItem(),WeaponAbilitySlot.PRIMARY),"Visible nearby block top rejects blink");
                test.assertTrue(player.position().distanceTo(target)<.001 && test.getLevel().noCollision(player,player.getBoundingBox()),"Blink fails to land on the actual full-block/slab top through tall grass");
            }
        }
        test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void grassAndFlowersBeforeWallGiveFarthestSafeGroundLanding(GameTestHelper test) {
        clearGroundedPath(test);
        test.setBlock(new BlockPos(8,3,9),Blocks.SHORT_GRASS);test.setBlock(new BlockPos(8,3,10),Blocks.DANDELION);
        test.setBlock(new BlockPos(8,3,11),Blocks.FERN);test.setBlock(new BlockPos(8,3,12),Blocks.POPPY);
        for(int x=6;x<=10;x++)for(int y=3;y<=7;y++)test.setBlock(new BlockPos(x,y,14),Blocks.STONE);
        var player=player(test,ModItems.RIFTFANG_DAGGER.get(),null,5);var start=player.position();
        test.assertTrue(weapon(player).activateAbility(player,player.getMainHandItem(),WeaponAbilitySlot.PRIMARY),"Grounded blink refuses the available path before a wall");
        test.assertTrue(player.getZ()>start.z && Math.abs(player.getY()-start.y)<.001 && Math.abs(player.getBoundingBox().maxZ-test.absolutePos(new BlockPos(8,3,14)).getZ())<.001,"Grass/flowers shorten the blink or it fails to stop at the nearest safe wall boundary");
        test.assertTrue(test.getLevel().noCollision(player,player.getBoundingBox()),"Shortened blink clips into a solid wall");test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void unsafeAimedTopFallsBackBeforeTheBlockInsteadOfClippingHead(GameTestHelper test) {
        clearGroundedPath(test);test.setBlock(new BlockPos(8,3,10),Blocks.STONE);test.setBlock(new BlockPos(8,5,10),Blocks.STONE);
        var player=player(test,ModItems.RIFTFANG_DAGGER.get(),null,0);var start=player.position();
        aimAt(player,Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,4,10))));
        test.assertTrue(weapon(player).activateAbility(player,player.getMainHandItem(),WeaponAbilitySlot.PRIMARY),"Unsafe top discards the shorter safe landing before the block");
        test.assertTrue(player.getZ()>start.z && Math.abs(player.getY()-start.y)<.001 && player.getBoundingBox().maxZ<=test.absolutePos(new BlockPos(8,3,10)).getZ()+.001,"Blink enters the one-block headroom gap or passes its obstruction");
        test.assertTrue(test.getLevel().noCollision(player,player.getBoundingBox()),"Aimed top suffocates the player");test.succeed();
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
