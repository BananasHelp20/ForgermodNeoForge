package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.AdditionalAbilities;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.damagesource.DamageTypes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class QuickRechargeEvents {
    private record Critical(ItemStack stack,Entity target,long tick) {}
    private static final Map<UUID,Critical> PENDING=new HashMap<>();
    private static final Map<UUID,Long> READY=new HashMap<>();
    private QuickRechargeEvents() {}
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void critical(CriticalHitEvent event) {
        if(!(event.getEntity() instanceof ServerPlayer player)) return;
        PENDING.remove(player.getUUID());
        if(player.isAlive() && !player.isSpectator() && event.isCriticalHit() && player.getAttackStrengthScale(.5F)>.9F
                && Augmentations.hasPassive(player.getMainHandItem(),AdditionalAbilities.QUICK_RECHARGE))
            PENDING.put(player.getUUID(),new Critical(player.getMainHandItem(),event.getTarget(),player.level().getGameTime()));
    }
    @SubscribeEvent
    public static void landed(LivingDamageEvent.Post event) {
        if(!event.getSource().is(DamageTypes.PLAYER_ATTACK) || !(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        var critical=PENDING.get(player.getUUID());
        if(critical==null || critical.target()!=event.getEntity()) return;
        PENDING.remove(player.getUUID());long now=player.level().getGameTime();
        if(critical.stack()!=player.getMainHandItem() || critical.tick()!=now || !player.isAlive() || player.isSpectator()
                || event.getNewDamage()+event.getReduction(DamageContainer.Reduction.ABSORPTION)<=0
                || !Augmentations.hasPassive(critical.stack(),AdditionalAbilities.QUICK_RECHARGE)
                || now<READY.getOrDefault(player.getUUID(),0L)) return;
        // Successful hits count even if lethal, so do not require the victim to remain alive here.
        if(WeaponAbilityNetwork.reducePrimaryCooldown(player,critical.stack(),20)) READY.put(player.getUUID(),now+40);
    }
    @SubscribeEvent
    public static void clearPending(ServerTickEvent.Post event) {PENDING.clear();}
    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {PENDING.remove(event.getEntity().getUUID());READY.remove(event.getEntity().getUUID());}
}
