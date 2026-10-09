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
    @GameTest(template = "riftfang_test")
    public static void acceptedJumpRetainsSprintMomentum(GameTestHelper test) throws ReflectiveOperationException {
        var player = test.makeMockServerPlayerInLevel();
        // Mock logins do not negotiate mod channels; allow the acknowledgement explicitly.
        net.neoforged.neoforge.network.registration.ChannelAttributes.getOrCreateAdHocChannels(player.connection.getConnection())
                .add(DoubleJumpNetwork.DoubleJumpAcceptedPayload.TYPE.id());
        player.setNoGravity(true);
        player.setPos(test.getBounds().getCenter());
        player.setOnGround(false);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(ModItems.DEAD_CALM_DAGGER.get()));
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
