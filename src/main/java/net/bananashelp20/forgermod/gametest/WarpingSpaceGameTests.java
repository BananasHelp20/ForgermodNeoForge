package net.bananashelp20.forgermod.gametest;

import com.mojang.authlib.GameProfile;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.*;
import net.bananashelp20.forgermod.item.custom.abilities.*;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.*;
import net.minecraft.server.network.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;
import java.util.*;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class WarpingSpaceGameTests {
    private static final String ID=VoidStepRanges.ID;
    private static final class RecordingConnection extends Connection {
        RecordingConnection(){super(PacketFlow.SERVERBOUND);}
        @Override public void send(net.minecraft.network.protocol.Packet<?> packet,net.minecraft.network.PacketSendListener listener,boolean flush) {}
    }
    private static ServerPlayer player(GameTestHelper test) {
        var profile=new GameProfile(UUID.randomUUID(),"warp-test");
        var player=new ServerPlayer(test.getLevel().getServer(),test.getLevel(),profile,ClientInformation.createDefault());
        new ServerGamePacketListenerImpl(test.getLevel().getServer(),new RecordingConnection(),player,CommonListenerCookie.createInitial(profile,false));
        return player;
    }
    private static void aim(ServerPlayer player,Vec3 point) {
        var delta=point.subtract(player.getEyePosition());
        player.setYRot((float)Math.toDegrees(Math.atan2(-delta.x,delta.z)));
        player.setXRot((float)-Math.toDegrees(Math.atan2(delta.y,Math.sqrt(delta.x*delta.x+delta.z*delta.z))));
    }
    private static void equip(ServerPlayer player,net.minecraft.world.item.Item item,int rank,boolean paired) {
        player.setItemInHand(InteractionHand.MAIN_HAND,Augmentations.setLevel(TestWeapons.ready(item),ID,rank));
        player.setItemInHand(InteractionHand.OFF_HAND,paired?Augmentations.apply(new ItemStack(ModItems.RIFTFANG_DAGGER_RUBY.get()),ID):ItemStack.EMPTY);
    }
    @GameTest(template="riftfang_test",batch="warping_space_ranges")
    public static void everyVariantAndRankBlinksAtApprovedSingleAndDualRanges(GameTestHelper test) {
        var player=player(test);var start=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,90,8)));int count=0;
        for(var entry:ModItems.ITEMS.getEntries()) {
            if(!(entry.get() instanceof InanisiumWeapon weapon) || !weapon.isDagger()) continue;
            count++;
            for(int rank=1;rank<=7;rank++) for(boolean paired:new boolean[]{false,true}) {
                equip(player,entry.get(),rank,paired);player.setPos(start);player.setYRot(17);player.setXRot(-90);player.setShiftKeyDown(false);
                int[] ranges={5,10,15,20,25,30,40};int range=ranges[rank-1]*(paired?2:1);
                test.assertTrue(weapon.activateAbility(player,player.getMainHandItem(),WeaponAbilitySlot.PRIMARY),"Blink rejected at rank "+rank);
                test.assertTrue(Math.abs(player.getY()-start.y-range)<.001,"Wrong blink distance at rank "+rank+" paired="+paired);
                test.assertTrue(player.getYRot()==17 && player.getXRot()==-90,"Blink changes camera");
                var stack=player.getMainHandItem();
                test.assertTrue(Augmentations.maxRank(stack,ID)==7 && Augmentations.isSuper(stack,ID,rank)==(rank>=4),"Wrong super progression");
                test.assertTrue(Augmentations.displayRank(stack,ID,rank)==(rank>=4?rank-3:rank),"Wrong displayed super rank");
                test.assertTrue("tooltips.forgermod.ability.warping_space.name".equals(Augmentations.superName(stack,ID)),"Missing command/template super name");
                if(rank<7) {
                    var changes=AugmentUpgradeDescriptions.changes(stack,ID,rank);int nextRange=ranges[rank],nextCooldown=45-5*(rank-3);
                    test.assertTrue(changes.stream().anyMatch(c->c.property().equals("range") && ((Number)c.after()).intValue()==nextRange),"Blink preview has wrong next range");
                    if(rank>=4)test.assertTrue(changes.stream().anyMatch(c->c.property().equals("etherwarp_cooldown") && ((Number)c.after()).intValue()==nextCooldown),"Etherwarp cooldown preview incorrect");
                } else test.assertTrue(Augmentations.apply(stack,ID).isEmpty(),"Warping Space exceeds IV");
                for(boolean compact:new boolean[]{false,true}) {
                    var description=AbilityDescriptions.description(stack,ID,rank,compact).getString();
                    test.assertTrue(description.contains(ranges[rank-1]+" blocks"),"Description has wrong current blink range");
                    test.assertTrue(description.contains("Etherwarp")== (rank>=4),"Super information leaked or missing");
                }
            }
        }
        test.assertTrue(count==5,"Missed Riftfang variant");test.succeed();
    }
    @GameTest(template="riftfang_test",batch="warping_space_etherwarp")
    public static void etherwarpUsesVisibleBlockAndCapsDualRangeAtOneHundred(GameTestHelper test) {
        var player=player(test);var start=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,90,8)));
        for(int dx=0;dx<=112;dx+=16) {var pos=BlockPos.containing(start).offset(dx,0,0);test.getLevel().getChunk(pos.getX()>>4,pos.getZ()>>4);}
        for(var entry:ModItems.ITEMS.getEntries()) {
            if(!(entry.get() instanceof InanisiumWeapon weapon) || !weapon.isDagger()) continue;
            for(int rank=4;rank<=7;rank++) for(boolean paired:new boolean[]{false,true}) {
                equip(player,entry.get(),rank,paired);player.setPos(start);player.setShiftKeyDown(true);
                int expected=Math.min(100,(30+10*(rank-4))*(paired?2:1));
                var target=BlockPos.containing(start).offset(expected-1,0,0);
                var previous=test.getLevel().getBlockState(target);
                try {
                    test.getLevel().setBlockAndUpdate(target,Blocks.STONE.defaultBlockState());aim(player,Vec3.atCenterOf(target));
                    test.assertTrue(weapon.activateAbility(player,player.getMainHandItem(),WeaponAbilitySlot.PRIMARY),"Etherwarp rejected at rank "+rank+" paired="+paired);
                    test.assertTrue(player.position().distanceTo(Vec3.atBottomCenterOf(target.above()))<.001,"Etherwarp does not land on selected block");
                    player.setPos(start);test.getLevel().setBlockAndUpdate(target,previous);
                    var beyond=BlockPos.containing(start).offset(expected+1,0,0);var before=test.getLevel().getBlockState(beyond);
                    try {
                        test.getLevel().setBlockAndUpdate(beyond,Blocks.STONE.defaultBlockState());aim(player,Vec3.atCenterOf(beyond));
                        test.assertFalse(weapon.activateAbility(player,player.getMainHandItem(),WeaponAbilitySlot.PRIMARY),"Etherwarp exceeds range/cap");
                        test.assertTrue(player.position().equals(start),"Failed Etherwarp changes position");
                    } finally {test.getLevel().setBlockAndUpdate(beyond,before);}
                } finally {test.getLevel().setBlockAndUpdate(target,previous);}
            }
        }
        test.succeed();
    }
    @GameTest(template="riftfang_test",batch="warping_space_safety")
    public static void failedEtherwarpDoesNotMoveOrConsumeCooldown(GameTestHelper test) throws Exception {
        var player=player(test);var start=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,90,8)));
        var target=BlockPos.containing(start).offset(12,0,0);
        var before=test.getLevel().getBlockState(target);var ceiling=test.getLevel().getBlockState(target.above());
        try {
            equip(player,ModItems.RIFTFANG_DAGGER.get(),4,false);player.setPos(start);player.setShiftKeyDown(true);
            test.getLevel().setBlockAndUpdate(target,Blocks.STONE.defaultBlockState());
            test.getLevel().setBlockAndUpdate(target.above(),Blocks.STONE.defaultBlockState());aim(player,Vec3.atCenterOf(target));
            player.setData(WeaponCooldownAttachments.COOLDOWNS,Map.of());AbilityTestPackets.use(player,0);
            test.assertTrue(player.position().equals(start) && player.getData(WeaponCooldownAttachments.COOLDOWNS).isEmpty(),"Unsafe landing moves player or consumes cooldown");
            test.getLevel().setBlockAndUpdate(target,before);test.getLevel().setBlockAndUpdate(target.above(),ceiling);
            player.setXRot(-90);AbilityTestPackets.use(player,0);
            test.assertTrue(player.position().equals(start) && player.getData(WeaponCooldownAttachments.COOLDOWNS).isEmpty(),"Missing target falls back to blink or consumes cooldown");
            // Sneaking at a base rank retains ordinary blink, without granting Etherwarp early.
            equip(player,ModItems.RIFTFANG_DAGGER.get(),1,false);AbilityTestPackets.use(player,0);
            test.assertTrue(Math.abs(player.getY()-start.y-5)<.001,"Sneaking breaks base blink");
        } finally {test.getLevel().setBlockAndUpdate(target,before);test.getLevel().setBlockAndUpdate(target.above(),ceiling);}
        test.succeed();
    }
    @GameTest(template="riftfang_test",batch="warping_space_cooldowns")
    public static void etherwarpAndBlinkHaveIndependentActivationCooldowns(GameTestHelper test) throws Exception {
        var player=player(test);var start=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,90,8)));
        var target=BlockPos.containing(start).offset(12,0,0);var previous=test.getLevel().getBlockState(target);
        try {
            test.getLevel().setBlockAndUpdate(target,Blocks.STONE.defaultBlockState());
            for(int rank=4;rank<=7;rank++) {
                equip(player,ModItems.RIFTFANG_DAGGER.get(),rank,false);player.setPos(start);player.setShiftKeyDown(true);aim(player,Vec3.atCenterOf(target));
                player.setData(WeaponCooldownAttachments.COOLDOWNS,Map.of());long now=player.level().getGameTime();
                AbilityTestPackets.use(player,0);
                String key=WeaponCooldownKey.of("forgermod:dagger_of_the_void",WeaponAbilitySlot.PRIMARY);
                var cooldowns=player.getData(WeaponCooldownAttachments.COOLDOWNS);
                test.assertTrue(cooldowns.getOrDefault(key+":etherwarp",0L)==now+(45-5*(rank-4))*20,"Wrong Etherwarp cooldown or key: "+cooldowns);
                test.assertFalse(cooldowns.containsKey(key),"Etherwarp starts normal blink cooldown");
                player.setPos(start);AbilityTestPackets.use(player,0);test.assertTrue(player.position().equals(start),"Etherwarp can bypass cooldown");
                player.setShiftKeyDown(false);player.setXRot(-90);AbilityTestPackets.use(player,0);
                test.assertTrue(player.getY()>start.y+19,"Blink blocked by Etherwarp cooldown");
                test.assertTrue(player.getData(WeaponCooldownAttachments.COOLDOWNS).getOrDefault(key,0L)==now+140,"Wrong blink cooldown");
                player.setPos(start);player.setShiftKeyDown(true);aim(player,Vec3.atCenterOf(target));
                player.setData(WeaponCooldownAttachments.COOLDOWNS,Map.of(key,now+140));AbilityTestPackets.use(player,0);
                test.assertTrue(player.position().distanceTo(Vec3.atBottomCenterOf(target.above()))<.001,"Etherwarp blocked by normal blink cooldown");
            }
        } finally {test.getLevel().setBlockAndUpdate(target,previous);}
        test.succeed();
    }
}
