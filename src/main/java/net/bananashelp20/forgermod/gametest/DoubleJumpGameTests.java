package net.bananashelp20.forgermod.gametest;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.item.ModItems;
import net.bananashelp20.forgermod.item.custom.abilities.DoubleJumpNetwork;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.lang.reflect.Proxy;

@GameTestHolder(ForgerMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class DoubleJumpGameTests {
    @GameTest(template="riftfang_test")
    public static void allVariantsShowApprovedCountsAndAlternateJumpAndHeightUpgrades(GameTestHelper test) {
        int count=0;int[] jumps={1,2,2,3,3,4};double[] lift={.55,.55,.60,.60,.70,.70};
        for(var entry:ModItems.ITEMS.getEntries()) {
            var fresh=new ItemStack(entry.get());
            if(net.bananashelp20.forgermod.augmentation.Augmentations.pool(fresh).stream().noneMatch(a->a.id().equals(net.bananashelp20.forgermod.augmentation.Augmentations.DOUBLE_JUMP)))continue;
            count++;var stack=fresh;String id=net.bananashelp20.forgermod.augmentation.Augmentations.DOUBLE_JUMP;
            for(int rank=1;rank<=6;rank++) {
                stack=net.bananashelp20.forgermod.augmentation.Augmentations.apply(stack,id);
                test.assertTrue(!stack.isEmpty() && net.bananashelp20.forgermod.augmentation.Augmentations.level(stack,id)==rank,"Upgrade refused or remigrated");
                test.assertTrue(net.bananashelp20.forgermod.augmentation.Augmentations.isSuper(stack,id,rank)==(rank>=3),"Wrong super start");
                test.assertTrue(net.bananashelp20.forgermod.augmentation.Augmentations.displayRank(stack,id,rank)==(rank<3?rank:rank-2),"Wrong displayed rank");
                for(boolean compact:new boolean[]{false,true}) {
                    var text=net.bananashelp20.forgermod.augmentation.AbilityDescriptions.description(stack,id,rank,compact);var white=new StringBuilder();
                    text.visit((style,part)->{if(style.getColor()!=null && style.getColor().getValue()==net.minecraft.ChatFormatting.WHITE.getColor())white.append(part);return java.util.Optional.<Void>empty();},net.minecraft.network.chat.Style.EMPTY);
                    String phrase=jumps[rank-1]+" more "+(rank==1?"jump":"jumps");
                    test.assertTrue(text.getString().equals("Gain the ability to do "+phrase+" in midair. Matching dual-wielded daggers combine their jumps.") && white.toString().equals(phrase),"Wrong jump wording/highlights");
                }
                if(rank<6) {
                    var changes=net.bananashelp20.forgermod.augmentation.AugmentUpgradeDescriptions.changes(stack,id,rank);
                    boolean countChanges=changes.stream().anyMatch(c->c.property().equals("air_jumps")),heightChanges=changes.stream().anyMatch(c->c.property().equals("jump_height"));
                    test.assertTrue(countChanges==(jumps[rank]!=jumps[rank-1]) && heightChanges==(lift[rank]!=lift[rank-1]),"Upgrade mixes count and height or misses a bonus");
                }
            }
            test.assertTrue(net.bananashelp20.forgermod.augmentation.Augmentations.apply(stack,id).isEmpty(),"Windwalker exceeds IV");
        }
        test.assertTrue(count==5,"Missed Dead Calm variant");test.succeed();
    }
    @GameTest(template="riftfang_test")
    public static void oldWeaponAndTemplateRanksMigrateOnceAndSurviveTransfer(GameTestHelper test) {
        String id=net.bananashelp20.forgermod.augmentation.Augmentations.DOUBLE_JUMP;
        for(var item:java.util.List.of(ModItems.DEAD_CALM_DAGGER.get(),ModItems.DEAD_CALM_DAGGER_JADE.get(),ModItems.AUGMENT_TEMPLATE.get(),ModItems.SUPER_AUGMENT_TEMPLATE.get())) {
            for(int oldRank=1;oldRank<=7;oldRank++) {
                var stack=new ItemStack(item);var root=new net.minecraft.nbt.CompoundTag();var data=new net.minecraft.nbt.CompoundTag();var levels=new net.minecraft.nbt.ListTag();var row=new net.minecraft.nbt.CompoundTag();
                row.putString("id",id);row.putInt("level",oldRank);levels.add(row);data.put("levels",levels);data.putInt("version",2);data.putInt("count",oldRank);root.put("forgermod_augments",data);root.putString("unrelated","preserved");
                stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,net.minecraft.world.item.component.CustomData.of(root));
                int expected=oldRank>=4?oldRank-1:Math.min(oldRank,2);
                test.assertTrue(net.bananashelp20.forgermod.augmentation.Augmentations.level(stack,id)==expected && net.bananashelp20.forgermod.augmentation.Augmentations.level(stack,id)==expected,"Legacy rank lost or repeatedly migrated");
                test.assertTrue(stack.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA).copyTag().equals(root),"Read-only migration mutates item");
                if(oldRank>=4)test.assertTrue(net.bananashelp20.forgermod.augmentation.Augmentations.displayRank(stack,id,expected)==oldRank-3,"Existing Windwalker level changed");
                if(expected<6) {
                    var upgraded=net.bananashelp20.forgermod.augmentation.Augmentations.apply(stack,id);
                    test.assertTrue(net.bananashelp20.forgermod.augmentation.Augmentations.level(upgraded,id)==expected+1,"Writing remigrates upgraded rank");
                    test.assertTrue(upgraded.get(net.minecraft.core.component.DataComponents.CUSTOM_DATA).copyTag().getString("unrelated").equals("preserved"),"Upgrade loses unrelated item data");
                }
                var merged=net.bananashelp20.forgermod.augmentation.Augmentations.merge(new ItemStack(ModItems.DEAD_CALM_DAGGER.get()),stack);
                test.assertTrue(!merged.isEmpty() && net.bananashelp20.forgermod.augmentation.Augmentations.level(merged,id)==expected,"Legacy transfer loses rank");
            }
        }
        test.succeed();
    }

    @GameTest(template="riftfang_test")
    public static void windwalkerBudgetsDualPairsAndServerLift(GameTestHelper test) throws ReflectiveOperationException {
        var player=test.makeMockServerPlayerInLevel();
        net.neoforged.neoforge.network.registration.ChannelAttributes.getOrCreateAdHocChannels(player.connection.getConnection()).add(DoubleJumpNetwork.DoubleJumpAcceptedPayload.TYPE.id());
        player.setNoGravity(true);player.setOnGround(false);player.setSprinting(true);
        IPayloadContext context=(IPayloadContext)Proxy.newProxyInstance(IPayloadContext.class.getClassLoader(),new Class<?>[]{IPayloadContext.class},(proxy,method,args)->{if(method.getName().equals("player"))return player;throw new UnsupportedOperationException(method.getName());});
        var request=DoubleJumpNetwork.class.getDeclaredMethod("handle",DoubleJumpNetwork.DoubleJumpPayload.class,IPayloadContext.class);request.setAccessible(true);
        try {
            for(int[] config:new int[][]{{1,0,0,1},{2,0,0,2},{3,0,0,2},{4,0,0,3},{5,0,0,3},{6,0,0,4},{1,1,1,2},{2,2,1,4},{1,6,2,5},{6,1,1,5},{3,5,2,5},{6,6,2,8},{0,6,2,4},{1,6,0,1}}) {
                var main=new ItemStack(ModItems.DEAD_CALM_DAGGER.get());var off=new ItemStack(ModItems.DEAD_CALM_DAGGER_RUBY.get());
                for(int i=0;i<config[0];i++)main=net.bananashelp20.forgermod.augmentation.Augmentations.apply(main,net.bananashelp20.forgermod.augmentation.Augmentations.DOUBLE_JUMP);
                for(int i=0;i<config[1];i++)off=net.bananashelp20.forgermod.augmentation.Augmentations.apply(off,net.bananashelp20.forgermod.augmentation.Augmentations.DOUBLE_JUMP);
                if(config[2]==1)main=net.bananashelp20.forgermod.augmentation.Augmentations.apply(main,net.bananashelp20.forgermod.augmentation.Augmentations.DUAL_WIELD);
                if(config[2]==2)off=net.bananashelp20.forgermod.augmentation.Augmentations.apply(off,net.bananashelp20.forgermod.augmentation.Augmentations.DUAL_WIELD);
                player.setItemInHand(InteractionHand.MAIN_HAND,main);player.setItemInHand(InteractionHand.OFF_HAND,off);
                test.assertTrue(DoubleJumpNetwork.airJumps(player)==config[3],"Incorrect learned pair jump count");
                DoubleJumpNetwork.onNormalJump(new LivingEvent.LivingJumpEvent(player));
                double lift=DoubleJumpNetwork.lift(player);
                int rank=config[2]>0?Math.max(config[0],config[1]):config[0];
                test.assertTrue(Math.abs(lift-(rank<3?.55:rank<5?.60:.70))<.001,"Super lift progression wrong");
                for(int i=0;i<config[3];i++) {
                    player.setDeltaMovement(.4,-.2,.35);request.invoke(null,new DoubleJumpNetwork.DoubleJumpPayload(),context);
                    test.assertTrue(Math.abs(player.getDeltaMovement().y-lift)<.001 && player.getDeltaMovement().x==.4 && player.getDeltaMovement().z==.35 && player.isSprinting(),"Earned jump rejected or sprint lost");
                }
                player.setDeltaMovement(.4,-.2,.35);request.invoke(null,new DoubleJumpNetwork.DoubleJumpPayload(),context);test.assertTrue(player.getDeltaMovement().y==-.2,"Extra unearned jump allowed");
                DoubleJumpNetwork.onNormalJump(new LivingEvent.LivingJumpEvent(player));player.setItemInHand(InteractionHand.MAIN_HAND,main.copy());request.invoke(null,new DoubleJumpNetwork.DoubleJumpPayload(),context);test.assertTrue(player.getDeltaMovement().y==-.2,"Switch preserves jump budget");
            }
            test.assertTrue(Math.abs(DoubleJumpNetwork.lift(player)-.55)<.001,"Unshared offhand grants super lift");
        } finally {DoubleJumpNetwork.onLogout(new net.neoforged.neoforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent(player));player.server.getPlayerList().remove(player);}
        test.succeed();
    }
    @GameTest(template = "riftfang_test")
    public static void acceptedJumpRetainsSprintMomentum(GameTestHelper test) throws ReflectiveOperationException {
        var player = test.makeMockServerPlayerInLevel();
        // Mock logins do not negotiate mod channels; allow the acknowledgement explicitly.
        net.neoforged.neoforge.network.registration.ChannelAttributes.getOrCreateAdHocChannels(player.connection.getConnection())
                .add(DoubleJumpNetwork.DoubleJumpAcceptedPayload.TYPE.id());
        player.setNoGravity(true);
        player.setPos(test.getBounds().getCenter());
        player.setOnGround(false);
        player.setItemInHand(InteractionHand.MAIN_HAND, TestWeapons.ready(ModItems.DEAD_CALM_DAGGER.get()));
        player.setSprinting(true);
        IPayloadContext context = (IPayloadContext)Proxy.newProxyInstance(IPayloadContext.class.getClassLoader(), new Class<?>[]{IPayloadContext.class},
                (proxy, method, args) -> { if (method.getName().equals("player")) return player; throw new UnsupportedOperationException(method.getName()); });
        var request = DoubleJumpNetwork.class.getDeclaredMethod("handle", DoubleJumpNetwork.DoubleJumpPayload.class, IPayloadContext.class);
        request.setAccessible(true);
        DoubleJumpNetwork.onNormalJump(new LivingEvent.LivingJumpEvent(player));
        player.setDeltaMovement(0, -.1, 0); // Server may not have the client's current horizontal speed.
        player.hurtMarked = false;
        request.invoke(null, new DoubleJumpNetwork.DoubleJumpPayload(), context);
        test.assertTrue(player.getDeltaMovement().y == .55 && !player.hurtMarked, "Jump sent a full velocity overwrite");
        player.setDeltaMovement(0, -.2, 0);
        request.invoke(null, new DoubleJumpNetwork.DoubleJumpPayload(), context);
        test.assertTrue(player.getDeltaMovement().y == -.2, "A second unearned air jump was accepted");
        var accepted = DoubleJumpNetwork.class.getDeclaredMethod("handleAccepted", DoubleJumpNetwork.DoubleJumpAcceptedPayload.class, IPayloadContext.class);
        accepted.setAccessible(true);
        for (Vec3 velocity : new Vec3[]{new Vec3(.4, -.2, .35), new Vec3(-.6, .1, 0), new Vec3(0, -.4, -.7)}) {
            player.setDeltaMovement(velocity);
            player.fallDistance = 10;
            accepted.invoke(null, new DoubleJumpNetwork.DoubleJumpAcceptedPayload(), context);
            Vec3 actual = player.getDeltaMovement();
            test.assertTrue(actual.x == velocity.x && actual.z == velocity.z && actual.y == .55,
                    "Accepted jump changed horizontal momentum");
            test.assertTrue(player.isSprinting() && player.fallDistance == 0, "Accepted jump canceled sprinting or failed to reset fall distance");
        }
        test.getLevel().getServer().getPlayerList().remove(player);
        test.succeed();
    }
}
