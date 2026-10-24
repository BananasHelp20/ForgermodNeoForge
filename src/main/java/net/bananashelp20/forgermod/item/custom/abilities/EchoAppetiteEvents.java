package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.AdditionalAbilities;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.item.custom.PulsiteWeapon;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

/** Echo Appetite's Warden reward is credited once when the killing damage is accepted. */
@EventBusSubscriber(modid = ForgerMod.MOD_ID)
public final class EchoAppetiteEvents {
    private EchoAppetiteEvents() {}

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onWardenDeath(LivingDeathEvent event) {
        if (event.isCanceled() || event.getEntity().getType() != EntityType.WARDEN
                || !(PassiveActors.attacker(event.getSource()) instanceof ServerPlayer player)
                || !player.isAlive() || player.isSpectator()
                || !(player.getMainHandItem().getItem() instanceof PulsiteWeapon)
                || !Augmentations.hasPassive(player.getMainHandItem(), AdditionalAbilities.ECHO_APPETITE)) return;
        player.getFoodData().eat(4, 0);
    }
}
