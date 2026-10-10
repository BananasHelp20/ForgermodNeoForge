package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.augmentation.RecommendedAbilities;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.event.entity.living.LivingShieldBlockEvent;

@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class ColdBellowsEvents {
    private ColdBellowsEvents() {}
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void onShieldBlock(LivingShieldBlockEvent event) {
        if(event.isCanceled() || !event.getBlocked() || event.getBlockedDamage()<=0
                || !(event.getEntity() instanceof ServerPlayer player) || !player.isAlive()
                || !player.getUseItem().canPerformAction(ItemAbilities.SHIELD_BLOCK)
                || !Augmentations.hasPassive(player.getMainHandItem(),RecommendedAbilities.COLD_BELLOWS)
                || !(event.getDamageSource().getEntity() instanceof LivingEntity attacker)
                || attacker==player || !attacker.isAlive() || attacker.isAlliedTo(player)) return;
        int rank=Augmentations.level(player.getMainHandItem(),RecommendedAbilities.COLD_BELLOWS);
        attacker.setRemainingFireTicks(Math.max(attacker.getRemainingFireTicks(),(2+rank)*20));
    }
}
