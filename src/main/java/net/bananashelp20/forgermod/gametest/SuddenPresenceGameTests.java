package net.bananashelp20.forgermod.gametest;

import com.mojang.authlib.GameProfile;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.InanisiumWeapon;
import net.bananashelp20.forgermod.item.custom.WeaponAbilitySlot;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.*;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.*;
import net.minecraft.server.network.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.*;
import java.util.UUID;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class SuddenPresenceGameTests {
    private static final String ID="tooltips.forgermod.ability.sudden_presence";
    private static final class RecordingConnection extends Connection {
        RecordingConnection(){super(PacketFlow.SERVERBOUND);}
        @Override public void send(net.minecraft.network.protocol.Packet<?> packet,net.minecraft.network.PacketSendListener listener,boolean flush) {}
    }

    @GameTest(template="riftfang_test",batch="review_sudden_presence")
    public static void sevenRanksUseCurrentRangeAndRespectTargetBoundary(GameTestHelper test) {
        var profile=new GameProfile(UUID.randomUUID(),"sudden-test");
        var player=new ServerPlayer(test.getLevel().getServer(),test.getLevel(),profile,ClientInformation.createDefault());
        new ServerGamePacketListenerImpl(test.getLevel().getServer(),new RecordingConnection(),player,CommonListenerCookie.createInitial(profile,false));
        var origin=Vec3.atBottomCenterOf(test.absolutePos(new BlockPos(8,5,8)));
        var target=EntityType.ZOMBIE.create(test.getLevel());
        target.setNoAi(true);target.setNoGravity(true);target.setYRot(0);test.getLevel().addFreshEntity(target);
        var neutral=EntityType.COW.create(test.getLevel());neutral.setNoAi(true);neutral.setNoGravity(true);neutral.setPos(origin.add(0,0,2));test.getLevel().addFreshEntity(neutral);
        var restored=new java.util.HashMap<BlockPos,net.minecraft.world.level.block.state.BlockState>();
        int count=0;
        try {
            for(var item:java.util.List.of(ModItems.AUGMENT_TEMPLATE.get(),ModItems.SUPER_AUGMENT_TEMPLATE.get())) {
                var blank=new ItemStack(item);
                test.assertTrue(Augmentations.maxRank(blank,ID)==7,"Template or weapon cap differs");
                var maximum=Augmentations.setLevel(blank,ID,7);
                test.assertTrue(!maximum.isEmpty() && Augmentations.level(maximum,ID)==7,"Command cannot grant VII");
                test.assertTrue(Augmentations.setLevel(blank,ID,8).isEmpty(),"Command can exceed VII");
                test.assertTrue(Augmentations.available(maximum).stream().noneMatch(a->a.id().equals(ID)),"Max-rank upgrade still offered");
            }

            // Preserve terrain even where the largest radius extends outside the test template.
            for(int x=8;x<=50;x++) for(int z=3;z<=13;z++) for(int y=4;y<=8;y++) {
                var pos=test.absolutePos(new BlockPos(x,y,z));restored.put(pos,test.getLevel().getBlockState(pos));
                test.getLevel().setBlockAndUpdate(pos,y==4?Blocks.STONE.defaultBlockState():Blocks.AIR.defaultBlockState());
            }
            for(var entry:ModItems.ITEMS.getEntries()) {
                if(!(entry.get() instanceof InanisiumWeapon weapon) || !weapon.isDagger()) continue;
                count++;var stack=new ItemStack(entry.get());
                test.assertTrue(Augmentations.level(Augmentations.setLevel(stack,ID,7),ID)==7 && Augmentations.setLevel(stack,ID,8).isEmpty(),"Wrong weapon command cap");
                test.assertTrue(Augmentations.maxRank(stack,ID)==7 && !Augmentations.hasSuperForm(stack,ID),"Wrong seven-level cap or unwanted super form");
                for(int rank=1;rank<=7;rank++) {
                    stack=Augmentations.apply(stack,ID);
                    test.assertTrue(Augmentations.level(stack,ID)==rank,"Cannot reach regular rank "+rank);
                    int range=10+5*(rank-1);
                    for(boolean compact:new boolean[]{false,true}) {
                        var text=AbilityDescriptions.description(stack,ID,rank,compact);var white=new StringBuilder();
                        text.visit((style,part)->{if(style.getColor()!=null && style.getColor().getValue()==net.minecraft.ChatFormatting.WHITE.getColor())white.append(part);return java.util.Optional.<Void>empty();},net.minecraft.network.chat.Style.EMPTY);
                        test.assertTrue(text.getString().equals("Teleport behind the nearest hostile mob within "+range+" blocks"),"Wrong range text at rank "+rank);
                        test.assertTrue(white.toString().equals(range+" blocks"),"Range highlight includes other prose");
                    }
                    var changes=AugmentUpgradeDescriptions.changes(stack,ID,rank);
                    test.assertTrue(rank==7?changes.isEmpty():changes.stream().anyMatch(c->c.property().equals("range") && ((Number)c.after()).intValue()==range+5),"Wrong range upgrade preview");
                    player.setItemInHand(InteractionHand.MAIN_HAND,stack);player.setPos(origin);
                    target.setPos(origin.add(range+.25,0,0));
                    test.assertFalse(weapon.activateAbility(player,stack,WeaponAbilitySlot.SECONDARY),"Targets beyond radius or neutral cow accepted");
                    test.assertTrue(player.position().distanceTo(origin)<.001,"Rejected teleport moved player");
                    target.setPos(origin.add(range,0,0));
                    test.assertTrue(weapon.activateAbility(player,stack,WeaponAbilitySlot.SECONDARY),"Boundary target rejected at rank "+rank);
                    test.assertTrue(player.position().distanceTo(target.position())<=3.1 && test.getLevel().noCollision(player,player.getBoundingBox()),"Unsafe landing or wrong destination");
                    test.assertTrue(weapon.abilityCooldownTicks(WeaponAbilitySlot.SECONDARY)==2400,"Cooldown changed");
                }
                test.assertTrue(Augmentations.apply(stack,ID).isEmpty(),"Rank exceeds VII");
            }
        } finally {
            target.discard();neutral.discard();
            restored.forEach((pos,state)->test.getLevel().setBlockAndUpdate(pos,state));
        }
        test.assertTrue(count==5,"Skipped Riftfang variant");test.succeed();
    }
}
