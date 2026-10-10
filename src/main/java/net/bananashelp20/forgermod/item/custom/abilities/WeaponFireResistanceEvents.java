package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.augmentation.RecommendedAbilities;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class WeaponFireResistanceEvents {
    private static final class Grant {
        MobEffectInstance owned,previous;
        Grant(MobEffectInstance previous) { this.previous=previous==null?null:copy(previous); }
    }
    private static final Map<UUID,Grant> GRANTS=new HashMap<>();
    private static final Set<UUID> INTERNAL=new HashSet<>();
    private WeaponFireResistanceEvents() {}
    private static MobEffectInstance copy(MobEffectInstance effect) {
        // The ordinary copy constructor drops hidden potion effects. Preserve the complete chain.
        return MobEffectInstance.load((net.minecraft.nbt.CompoundTag)effect.save());
    }
    private static boolean held(ItemStack stack,int rank) {
        return Augmentations.hasPassive(stack,RecommendedAbilities.FIRE_RESISTANCE)
                && Augmentations.level(stack,RecommendedAbilities.FIRE_RESISTANCE)>=rank;
    }
    private static boolean eligible(ServerPlayer player) {
        return player.isAlive() && !player.isSpectator() && (held(player.getMainHandItem(),1) || held(player.getOffhandItem(),2));
    }
    private static void add(ServerPlayer player,MobEffectInstance effect) {
        INTERNAL.add(player.getUUID());
        try { player.addEffect(effect); } finally { INTERNAL.remove(player.getUUID()); }
    }
    private static void end(ServerPlayer player,Grant grant) {
        GRANTS.remove(player.getUUID());
        if(grant!=null && player.getEffect(MobEffects.FIRE_RESISTANCE)==grant.owned) {
            player.removeEffect(MobEffects.FIRE_RESISTANCE);
            if(player.isAlive() && grant.previous!=null) add(player,grant.previous);
        }
    }
    @SubscribeEvent
    public static void onTick(PlayerTickEvent.Pre event) {
        if(!(event.getEntity() instanceof ServerPlayer player)) return;
        Grant grant=GRANTS.get(player.getUUID());
        MobEffectInstance current=player.getEffect(MobEffects.FIRE_RESISTANCE);
        // Milk/removal or replacement ends ownership; do not resurrect a deliberately removed potion.
        if(grant!=null && current!=grant.owned) { GRANTS.remove(player.getUUID()); grant=null; }
        if(!eligible(player)) { end(player,grant); return; }
        if(grant==null) {
            if(current!=null && current.isInfiniteDuration()) return;
            grant=new Grant(current); GRANTS.put(player.getUUID(),grant);
        }
        if(current==null || !current.isInfiniteDuration()) {
            add(player,new MobEffectInstance(MobEffects.FIRE_RESISTANCE,-1,current==null?0:current.getAmplifier(),false,false,true));
            grant.owned=player.getEffect(MobEffects.FIRE_RESISTANCE);
        }
        // Track independently acquired effects while our infinite grant masks their ordinary timer.
        if(grant.previous!=null && !grant.previous.tick(player,()->{})) grant.previous=null;
    }
    @SubscribeEvent
    public static void onEffectAdded(MobEffectEvent.Added event) {
        if(!(event.getEntity() instanceof ServerPlayer player) || INTERNAL.contains(player.getUUID())
                || !event.getEffectInstance().getEffect().equals(MobEffects.FIRE_RESISTANCE)) return;
        Grant grant=GRANTS.get(player.getUUID()); if(grant==null) return;
        var effect=copy(event.getEffectInstance());
        if(grant.previous==null) grant.previous=effect; else grant.previous.update(effect);
    }
    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if(event.getEntity() instanceof ServerPlayer player) end(player,GRANTS.get(player.getUUID()));
    }
}
