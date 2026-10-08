package net.bananashelp20.forgermod.item.custom.attacks.dagger;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.item.custom.PulsiteWeapon;
import net.bananashelp20.forgermod.item.custom.abilities.WeaponAbilityNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayDeque;
import java.util.Queue;

@EventBusSubscriber(modid = ForgerMod.MOD_ID, value = Dist.CLIENT)
public class DualWieldClient {
    private record PendingSwing(long dueTick, Item dagger) {}

    private static final Queue<PendingSwing> PENDING = new ArrayDeque<>();
    private static long lastQueuedTick = Long.MIN_VALUE;
    private static Level lastLevel;

    public static boolean hasMatchingDaggers(Player player) {
        return DaggerItems.hasMatchingDaggers(player);
    }

    @SubscribeEvent
    public static void onAttackInput(InputEvent.InteractionKeyMappingTriggered event) {
        if (!event.isAttack() || event.isCanceled()) return;

        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player != null && minecraft.level != null && minecraft.screen == null
                && minecraft.player.getMainHandItem().getItem() instanceof PulsiteWeapon weapon
                && weapon.isDagger() && minecraft.hitResult != null
                && minecraft.hitResult.getType() == HitResult.Type.MISS) {
            PacketDistributor.sendToServer(new WeaponAbilityNetwork.SonicAirSwingPayload());
        }
        if (minecraft.player == null || minecraft.level == null || !hasMatchingDaggers(minecraft.player)) return;
        if (minecraft.hitResult != null && minecraft.hitResult.getType() == HitResult.Type.BLOCK) return;

        if (lastLevel != minecraft.level) {
            PENDING.clear();
            lastQueuedTick = Long.MIN_VALUE;
            lastLevel = minecraft.level;
        }
        long now = minecraft.level.getGameTime();
        if (lastQueuedTick == Long.MIN_VALUE || now - lastQueuedTick >= 3) {
            PENDING.add(new PendingSwing(now + 2, minecraft.player.getMainHandItem().getItem()));
            lastQueuedTick = now;
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            PENDING.clear();
            lastLevel = null;
            lastQueuedTick = Long.MIN_VALUE;
            return;
        }

        long now = minecraft.level.getGameTime();
        while (!PENDING.isEmpty() && PENDING.peek().dueTick() <= now) {
            PendingSwing pending = PENDING.remove();
            if (!hasMatchingDaggers(minecraft.player)
                    || !minecraft.player.getMainHandItem().is(pending.dagger())) continue;

            // Vanilla tracks only one hand swing at a time. Restart it for the offhand.
            minecraft.player.swinging = false;
            minecraft.player.swing(InteractionHand.OFF_HAND);

            int targetId = minecraft.hitResult instanceof EntityHitResult hit
                    ? hit.getEntity().getId() : -1;
            PacketDistributor.sendToServer(new DualWieldNetwork.OffhandAttackPayload(targetId));
        }
    }
}
