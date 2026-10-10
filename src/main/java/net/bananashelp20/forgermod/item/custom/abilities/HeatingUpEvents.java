package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.augmentation.RecommendedAbilities;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class HeatingUpEvents {
    private static final float CAPACITY=20,COST=5;
    private static final class Heat {
        final ItemStack stack; final int selected;
        float amount; long ignitionTick=Long.MIN_VALUE;
        Heat(ServerPlayer player) { stack=player.getMainHandItem(); selected=player.getInventory().selected; }
    }
    private static final Map<UUID,Heat> HEAT=new HashMap<>();
    private HeatingUpEvents() {}
    private static Heat heat(ServerPlayer player,boolean create) {
        Heat heat=HEAT.get(player.getUUID());
        if(!player.isAlive() || player.isSpectator() || !Augmentations.hasPassive(player.getMainHandItem(),RecommendedAbilities.HEATING_UP)) {
            HEAT.remove(player.getUUID()); return null;
        }
        if(heat!=null && (heat.stack!=player.getMainHandItem() || heat.selected!=player.getInventory().selected)) {
            HEAT.remove(player.getUUID()); heat=null;
        }
        if(heat==null && create) { heat=new Heat(player); HEAT.put(player.getUUID(),heat); }
        return heat;
    }
    public static float storedHeat(ServerPlayer player) { Heat heat=heat(player,false); return heat==null?0:heat.amount; }
    private static boolean melee(DamageSource source) {
        return source.is(DamageTypes.PLAYER_ATTACK) || source.is(DamageTypes.MOB_ATTACK)
                || source.is(DamageTypes.MOB_ATTACK_NO_AGGRO) || source.is(DamageTypes.STING);
    }
    @SubscribeEvent
    public static void onDamage(LivingDamageEvent.Post event) {
        if(event.getNewDamage()<=0 || !melee(event.getSource())) return;
        if(event.getEntity() instanceof ServerPlayer victim) {
            Heat heat=heat(victim,true);
            if(heat!=null) heat.amount=Math.min(CAPACITY,heat.amount+event.getNewDamage()*.5F);
        }
        if(!(event.getSource().getEntity() instanceof ServerPlayer attacker) || event.getEntity()==attacker
                || event.getEntity().isAlliedTo(attacker)) return;
        Heat heat=heat(attacker,false); if(heat==null) return;
        long now=attacker.level().getGameTime();
        // Vanilla applies a swing and its sweeping hits in the same tick. Pay once for that attack.
        if(heat.ignitionTick!=now) {
            if(heat.amount<COST) return;
            heat.amount-=COST; heat.ignitionTick=now;
        }
        int rank=Augmentations.level(attacker.getMainHandItem(),RecommendedAbilities.HEATING_UP);
        if(!event.getEntity().isDeadOrDying()) event.getEntity().setRemainingFireTicks(
                Math.max(event.getEntity().getRemainingFireTicks(),(2+rank)*20));
    }
    @SubscribeEvent
    public static void onAttack(AttackEntityEvent event) {
        if(event.getEntity() instanceof ServerPlayer player) {
            Heat heat=heat(player,false);
            if(heat!=null) heat.ignitionTick=Long.MIN_VALUE;
        }
    }
    @SubscribeEvent
    public static void onTick(PlayerTickEvent.Pre event) {
        if(!(event.getEntity() instanceof ServerPlayer player)) return;
        Heat heat=heat(player,false);
        if(Augmentations.level(player.getMainHandItem(),RecommendedAbilities.HEATING_UP)!=4
                || player.getRemainingFireTicks()<=0) return;
        if(heat==null) heat=heat(player,true);
        if(heat==null || heat.amount>=CAPACITY) return;
        heat.amount=Math.min(CAPACITY,heat.amount+player.getRemainingFireTicks()/20F); player.clearFire();
    }
    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) { HEAT.remove(event.getEntity().getUUID()); }
}
