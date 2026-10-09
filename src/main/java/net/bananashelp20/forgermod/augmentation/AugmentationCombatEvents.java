package net.bananashelp20.forgermod.augmentation;

import net.bananashelp20.forgermod.ForgerMod;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber(modid = ForgerMod.MOD_ID)
public final class AugmentationCombatEvents {
    private AugmentationCombatEvents() {}

    @SubscribeEvent
    public static void onDamage(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && !event.isCanceled()) {
            int rank = Augmentations.level(player.getMainHandItem(), Augmentations.GUARDED);
            if (rank > 0) event.setAmount(event.getAmount() * (1 - .05F * rank));
        }
    }
}
