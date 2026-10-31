package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.ForgerMod;
import net.minecraft.world.entity.LivingEntity;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.augmentation.RecommendedAbilities;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class NetherBornEvents {
    private NetherBornEvents() {}
    public static int bonusPercent(int rank) { return 6 * Math.clamp(rank, 1, 5); }
    @SubscribeEvent
    public static void onDamage(LivingIncomingDamageEvent event) {
        if(event.isCanceled() || event.getAmount()<=0 || !(PassiveActors.entity(PassiveActors.attacker(event.getSource())) instanceof LivingEntity player)
                || !player.isAlive() || player.isSpectator() || player.level().dimension()!=Level.NETHER
                || !Augmentations.hasPassive(player.getMainHandItem(),RecommendedAbilities.NETHER_BORN)
                || !(PassiveActors.melee(event.getSource()) || event.getSource().is(DamageTypeTags.IS_EXPLOSION))) return;
        event.setAmount(event.getAmount() * (1 + bonusPercent(Augmentations.level(player.getMainHandItem(), RecommendedAbilities.NETHER_BORN)) / 100F));
    }
}
