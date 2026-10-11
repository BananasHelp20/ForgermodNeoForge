package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.AdditionalAbilities;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import java.util.Map;
import java.util.WeakHashMap;

@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class VoidGripEvents {
    private record Hit(ServerPlayer player,ItemStack stack,long tick) {}
    private static final Map<LivingEntity,Hit> HITS=new WeakHashMap<>();
    private VoidGripEvents() {}
    @SubscribeEvent
    public static void incoming(LivingIncomingDamageEvent event) {
        if(event.getEntity().level().isClientSide) return;
        HITS.remove(event.getEntity());
        if(!event.isCanceled() && event.getAmount()>0 && event.getSource().is(DamageTypes.PLAYER_ATTACK)
                && event.getSource().getEntity() instanceof ServerPlayer player && player.isAlive() && !player.isSpectator()
                && WeaponAbilityTargets.enemy(player,event.getEntity())
                && Augmentations.hasPassive(player.getMainHandItem(),AdditionalAbilities.VOID_GRIP))
            HITS.put(event.getEntity(),new Hit(player,player.getMainHandItem(),player.level().getGameTime()));
    }
    @SubscribeEvent
    public static void knockback(LivingKnockBackEvent event) {
        var hit=HITS.get(event.getEntity());if(hit==null || event.isCanceled()) return;
        if(hit.player().level()!=event.getEntity().level() || hit.tick()!=event.getEntity().level().getGameTime()
                || hit.player().getMainHandItem()!=hit.stack() || !hit.player().isAlive()
                || !Augmentations.hasPassive(hit.stack(),AdditionalAbilities.VOID_GRIP)) return;
        event.setStrength(event.getStrength()*.75F);
    }
    @SubscribeEvent
    public static void endTick(ServerTickEvent.Post event) {HITS.clear();}
}
