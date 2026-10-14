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
    public static void windwalkerBudgetsDualPairsAndServerLift(GameTestHelper test) throws ReflectiveOperationException {
        var player=test.makeMockServerPlayerInLevel();
        net.neoforged.neoforge.network.registration.ChannelAttributes.getOrCreateAdHocChannels(player.connection.getConnection()).add(DoubleJumpNetwork.DoubleJumpAcceptedPayload.TYPE.id());
        player.setNoGravity(true);player.setOnGround(false);player.setSprinting(true);
        IPayloadContext context=(IPayloadContext)Proxy.newProxyInstance(IPayloadContext.class.getClassLoader(),new Class<?>[]{IPayloadContext.class},(proxy,method,args)->{if(method.getName().equals("player"))return player;throw new UnsupportedOperationException(method.getName());});
        var request=DoubleJumpNetwork.class.getDeclaredMethod("handle",DoubleJumpNetwork.DoubleJumpPayload.class,IPayloadContext.class);request.setAccessible(true);
        try {
            for(int[] config:new int[][]{{1,0,0,1},{4,0,0,2},{7,0,0,2},{1,1,1,2},{4,1,2,3},{4,7,2,4},{1,7,0,1}}) {
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
                test.assertTrue(Math.abs(lift-(.55+.05*Math.max(0,rank-4)))<.001,"Super lift progression wrong");
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
