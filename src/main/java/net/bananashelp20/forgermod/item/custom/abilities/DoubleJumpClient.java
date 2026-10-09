package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = ForgerMod.MOD_ID, value = Dist.CLIENT)
public final class DoubleJumpClient {
    private static boolean wasJumpDown;
    private static boolean wasGrounded;
    private static boolean jumpedFromGround;
    private DoubleJumpClient() {}

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            wasJumpDown = false;
            wasGrounded = false;
            jumpedFromGround = false;
            return;
        }
        boolean jumpDown = minecraft.options.keyJump.isDown();
        boolean grounded = minecraft.player.onGround();
        boolean taifuniteDagger = Augmentations.hasPassive(minecraft.player.getMainHandItem(), "tooltips.forgermod.passive.double_jump");
        if (!taifuniteDagger || minecraft.screen != null) {
            jumpedFromGround = false;
        } else if (grounded && !jumpDown) {
            jumpedFromGround = false;
        } else if (jumpDown && !wasJumpDown) {
            if (grounded || wasGrounded) jumpedFromGround = true;
            else if (jumpedFromGround) {
                PacketDistributor.sendToServer(new DoubleJumpNetwork.DoubleJumpPayload());
                jumpedFromGround = false;
            }
        }
        wasJumpDown = jumpDown;
        wasGrounded = grounded;
    }
}
