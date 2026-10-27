package net.bananashelp20.forgermod.item.custom.attacks.axe;

import net.bananashelp20.forgermod.ForgerMod;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(modid = ForgerMod.MOD_ID, value = Dist.CLIENT)
public final class AxeHeavyClient {
    private AxeHeavyClient() {}

    @SubscribeEvent
    public static void onAttackInput(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isAttack() || event.isCanceled()) return;
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.screen != null
                || (AxeItems.isAxe(minecraft.player.getMainHandItem()) && !minecraft.player.getOffhandItem().isEmpty())
                || !AxeAnimationItems.isModAxe(minecraft.player.getMainHandItem())) return;
        if (minecraft.hitResult != null && minecraft.hitResult.getType() == HitResult.Type.BLOCK) return;

        int attackKind = minecraft.hitResult == null || minecraft.hitResult.getType() == HitResult.Type.MISS ? 1 : 0;
        PacketDistributor.sendToServer(new AxeHeavyNetwork.AxeAttackPayload(attackKind));
    }

}
