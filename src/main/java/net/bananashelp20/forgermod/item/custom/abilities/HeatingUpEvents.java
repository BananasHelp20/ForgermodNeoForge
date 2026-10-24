package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.ForgerMod;
import net.minecraft.world.entity.LivingEntity;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.augmentation.RecommendedAbilities;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class HeatingUpEvents {
    private record Recovery(ItemStack stack, int selected, int ticks) {}
    private static final Map<UUID,Recovery> RECOVERY=new HashMap<>();
    private HeatingUpEvents() {}
    private static boolean equipped(LivingEntity player) {
        return player.isAlive() && !player.isSpectator()
                && Augmentations.hasPassive(PassiveActors.weapon(player,RecommendedAbilities.HEATING_UP),RecommendedAbilities.HEATING_UP);
    }
    /** Heal one HP per interval; rank V from existing super templates remains supported. */
    public static int healingInterval(int rank) { return 40-5*(Math.clamp(rank,1,5)-1); }

    @SubscribeEvent
    public static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if(PassiveActors.entity(event.getEntity()) instanceof LivingEntity player && equipped(player) && !player.isInLava()
                && event.getSource().is(DamageTypeTags.IS_FIRE) && !event.getSource().is(DamageTypes.LAVA))
            event.setCanceled(true);
    }
    @SubscribeEvent
    public static void onTick(PassiveActorTickEvent.Pre event) {
        if(!(PassiveActors.entity(event.getEntity()) instanceof LivingEntity player)) return;
        if(!equipped(player) || player.isInLava() || player.getRemainingFireTicks()<=0
                || player.getHealth()>=player.getMaxHealth()) {
            RECOVERY.remove(player.getUUID()); return;
        }
        var stack=PassiveActors.weapon(player,RecommendedAbilities.HEATING_UP);
        var previous=RECOVERY.get(player.getUUID());
        int elapsed=previous!=null && previous.stack()==stack && previous.selected()==PassiveActors.selected(player)
                ?previous.ticks()+1:1;
        if(elapsed>=healingInterval(Augmentations.level(stack,RecommendedAbilities.HEATING_UP))) {
            player.heal(1); elapsed=0;
        }
        RECOVERY.put(player.getUUID(),new Recovery(stack,PassiveActors.selected(player),elapsed));
    }
    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) { RECOVERY.remove(event.getEntity().getUUID()); }
    public static void onTick(PlayerTickEvent.Pre event){onTick(new PassiveActorTickEvent.Pre(event.getEntity()));}
}
