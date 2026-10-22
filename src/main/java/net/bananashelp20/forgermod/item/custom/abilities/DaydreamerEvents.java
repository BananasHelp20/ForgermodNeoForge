package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.augmentation.AdditionalAbilities;
import net.bananashelp20.forgermod.augmentation.JadeAugments;
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
public final class DaydreamerEvents {
    private static final class Grant {
        MobEffectInstance owned,previous;
        Grant(MobEffectInstance previous) { this.previous=previous==null?null:copy(previous); }
    }
    private static final Map<UUID,Grant> GRANTS=new HashMap<>();
    private static final Set<UUID> INTERNAL=new HashSet<>();
    private DaydreamerEvents() {}
    private static MobEffectInstance copy(MobEffectInstance effect) {
        // The ordinary copy constructor drops hidden potion effects. Preserve the complete chain.
        return MobEffectInstance.load((net.minecraft.nbt.CompoundTag)effect.save());
    }
    private static boolean held(ItemStack stack,int rank) {
        return Augmentations.hasPassive(stack,AdditionalAbilities.DAYDREAMER)
                && Augmentations.level(stack,AdditionalAbilities.DAYDREAMER)>=rank;
    }
    private static boolean eligible(ServerPlayer player) {
        long time=Math.floorMod(player.level().getDayTime(),24000);
        return player.isAlive() && !player.isSpectator() && player.level().dimensionType().hasSkyLight()
                && (time<13000 || time>=23000) && held(player.getMainHandItem(),1);
    }
    private static void add(ServerPlayer player,MobEffectInstance effect) {
        INTERNAL.add(player.getUUID());
        try { player.addEffect(effect); } finally { INTERNAL.remove(player.getUUID()); }
    }
    private static void end(ServerPlayer player,Grant grant) {
        GRANTS.remove(player.getUUID());
        if(grant!=null && player.getEffect(MobEffects.JUMP)==grant.owned) {
            player.removeEffect(MobEffects.JUMP);
            if(player.isAlive() && grant.previous!=null) add(player,grant.previous);
        }
    }
    @SubscribeEvent
    public static void onTick(PlayerTickEvent.Pre event) {
        if(!(event.getEntity() instanceof ServerPlayer player)) return;
        Grant grant=GRANTS.get(player.getUUID());
        MobEffectInstance current=player.getEffect(MobEffects.JUMP);
        // Milk/removal or replacement ends ownership; do not resurrect a deliberately removed potion.
        if(grant!=null && current!=grant.owned) { GRANTS.remove(player.getUUID()); grant=null; }
        if(!eligible(player)) { end(player,grant); return; }
        var desired=JadeAugments.reinforce(player.getMainHandItem(),AdditionalAbilities.DAYDREAMER,
                new MobEffectInstance(MobEffects.JUMP,-1,1,false,false,true));
        // Reconcile gemstone changes and externally strengthened effects without retaining our old strength.
        if(grant!=null && (current==null || current.getAmplifier()!=desired.getAmplifier())) {
            end(player,grant);grant=null;current=player.getEffect(MobEffects.JUMP);
        }
        if(grant==null) {
            if(current!=null && (current.getAmplifier()>desired.getAmplifier()
                    || current.getAmplifier()==desired.getAmplifier() && current.isInfiniteDuration())) return;
            grant=new Grant(current); GRANTS.put(player.getUUID(),grant);
            add(player,desired);
            grant.owned=player.getEffect(MobEffects.JUMP);
        }
        // Track independently acquired effects while our infinite grant masks their ordinary timer.
        if(grant.previous!=null && !grant.previous.tick(player,()->{})) grant.previous=null;
    }
    @SubscribeEvent
    public static void onEffectAdded(MobEffectEvent.Added event) {
        if(!(event.getEntity() instanceof ServerPlayer player) || INTERNAL.contains(player.getUUID())
                || !event.getEffectInstance().getEffect().equals(MobEffects.JUMP)) return;
        Grant grant=GRANTS.get(player.getUUID()); if(grant==null) return;
        var effect=copy(event.getEffectInstance());
        if(grant.previous==null) grant.previous=effect; else grant.previous.update(effect);
    }
    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if(event.getEntity() instanceof ServerPlayer player) end(player,GRANTS.get(player.getUUID()));
    }
}
