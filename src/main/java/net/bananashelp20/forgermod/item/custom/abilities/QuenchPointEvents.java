package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.augmentation.RecommendedAbilities;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class QuenchPointEvents {
    private record Charge(ItemStack stack,int selected,long expires,float damage) {}
    private static final Map<UUID,Charge> CHARGES=new HashMap<>();
    private QuenchPointEvents() {}
    private static boolean learned(ServerPlayer player) {
        return player.isAlive() && !player.isSpectator()
                && Augmentations.hasPassive(player.getMainHandItem(),RecommendedAbilities.QUENCH_POINT);
    }
    private static Charge charge(ServerPlayer player) {
        Charge charge=CHARGES.get(player.getUUID());
        if(charge!=null && (!learned(player) || player.getMainHandItem()!=charge.stack()
                || player.getInventory().selected!=charge.selected() || player.level().getGameTime()>=charge.expires())) {
            CHARGES.remove(player.getUUID()); return null;
        }
        return charge;
    }
    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if(event.isCanceled() || event.getAmount()<=0 || !event.getSource().is(DamageTypes.PLAYER_ATTACK)
                || !(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        Charge charge=charge(player);
        if(charge!=null) event.setAmount(event.getAmount()+charge.damage());
    }
    @SubscribeEvent
    public static void onDamageDealt(LivingDamageEvent.Post event) {
        if(event.getNewDamage()<=0 || !event.getSource().is(DamageTypes.PLAYER_ATTACK)
                || !(event.getSource().getEntity() instanceof ServerPlayer player) || !learned(player)) return;
        // Consume only after real damage: blocked/canceled hits do not spend the charge.
        CHARGES.remove(player.getUUID());
        int burning=event.getEntity().getRemainingFireTicks();
        if(burning<=0) return;
        event.getEntity().clearFire();
        int rank=Augmentations.level(player.getMainHandItem(),RecommendedAbilities.QUENCH_POINT);
        float damage=Math.min(burning,200)/20F*(.25F+.25F*rank);
        CHARGES.put(player.getUUID(),new Charge(player.getMainHandItem(),player.getInventory().selected,
                player.level().getGameTime()+200,damage));
    }
    @SubscribeEvent
    public static void onTick(PlayerTickEvent.Pre event) {
        if(event.getEntity() instanceof ServerPlayer player) charge(player);
    }
    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) { CHARGES.remove(event.getEntity().getUUID()); }
}
