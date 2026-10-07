package net.bananashelp20.forgermod.item.custom.attacks.axe;

import com.mojang.math.Axis;
import net.bananashelp20.forgermod.ForgerMod;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RenderHandEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = ForgerMod.MOD_ID, value = Dist.CLIENT)
public final class AxeHeavyClient {
    private AxeHeavyClient() {}

    @SubscribeEvent
    public static void onAttackInput(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isAttack() || event.isCanceled()) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.screen != null
                || !AxeItems.isAxe(minecraft.player.getMainHandItem())) return;
        if (minecraft.hitResult != null && minecraft.hitResult.getType() == HitResult.Type.BLOCK) return;

        PacketDistributor.sendToServer(new AxeHeavyNetwork.AxeAttackPayload(0));
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        if (event.getHand() != InteractionHand.MAIN_HAND
                || !AxeItems.isAxe(event.getItemStack())) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) return;

        long start = minecraft.player.getPersistentData().getLong(AxeHeavyNetwork.ANIMATION_TAG);
        float time = minecraft.level.getGameTime() - start + event.getPartialTick();
        if (start == 0 || time < 0 || time > 6) return;

        // Lift the axe, then bring it straight down as the server lands the slam.
        float angle = time < 2 ? -35.0F * time
                : time < 4 ? -70.0F + 80.0F * (time - 2)
                : 90.0F - 45.0F * (time - 4);
        event.getPoseStack().mulPose(Axis.XP.rotationDegrees(angle));
        event.getPoseStack().translate(0, Math.sin(Math.PI * time / 6.0) * 0.1, 0);
    }
}
