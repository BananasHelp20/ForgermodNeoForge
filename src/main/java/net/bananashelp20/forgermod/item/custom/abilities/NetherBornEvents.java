package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.augmentation.RecommendedAbilities;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class NetherBornEvents {
    private NetherBornEvents() {}
    @SubscribeEvent
    public static void onDamage(LivingIncomingDamageEvent event) {
        if(event.isCanceled() || event.getAmount()<=0 || !(event.getSource().getEntity() instanceof ServerPlayer player)
                || !player.isAlive() || player.isSpectator() || player.level().dimension()!=Level.NETHER
                || !Augmentations.hasPassive(player.getMainHandItem(),RecommendedAbilities.NETHER_BORN)
                || !(event.getSource().is(DamageTypes.PLAYER_ATTACK) || event.getSource().is(DamageTypeTags.IS_EXPLOSION))) return;
        event.setAmount(event.getAmount()*1.2F);
    }
}
