package net.bananashelp20.forgermod.item.custom.abilities;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.minecraft.server.level.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.damagesource.DamageTypes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import java.util.*;
@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class FinalStrikeEvents {
    private record Last(ItemStack stack,int slot,ServerLevel level,UUID target) {}
    private static final Map<UUID,Last> LAST=new HashMap<>();
    private FinalStrikeEvents() {}
    private static boolean ready(ServerPlayer player) {
        var last=LAST.get(player.getUUID());if(last==null) return false;
        if(!player.isAlive() || player.isSpectator() || player.getMainHandItem()!=last.stack || player.getInventory().selected!=last.slot || player.serverLevel()!=last.level || !Augmentations.hasPassive(last.stack,AdditionalAbilities.FINAL_STRIKE)) {LAST.remove(player.getUUID());return false;}return true;
    }
    @SubscribeEvent
    public static void damage(LivingIncomingDamageEvent event) {
        if(!event.isCanceled() && event.getAmount()>0 && event.getSource().is(DamageTypes.PLAYER_ATTACK) && event.getSource().getEntity() instanceof ServerPlayer player && ready(player)
            && LAST.get(player.getUUID()).target.equals(event.getEntity().getUUID()) && WeaponAbilityTargets.enemy(player,event.getEntity()) && event.getEntity().getHealth()<event.getEntity().getMaxHealth()*.1F)
            event.setAmount(event.getAmount()*1.5F);
    }
    @SubscribeEvent
    public static void hit(LivingDamageEvent.Post event) {
        if(event.getSource().is(DamageTypes.PLAYER_ATTACK) && event.getSource().getEntity() instanceof ServerPlayer player && player.isAlive() && !player.isSpectator()
            && Augmentations.hasPassive(player.getMainHandItem(),AdditionalAbilities.FINAL_STRIKE) && WeaponAbilityTargets.hostileTo(player,event.getEntity())
            && event.getNewDamage()+event.getReduction(DamageContainer.Reduction.ABSORPTION)>0)
            LAST.put(player.getUUID(),new Last(player.getMainHandItem(),player.getInventory().selected,player.serverLevel(),event.getEntity().getUUID()));
    }
    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post event) {if(event.getEntity() instanceof ServerPlayer player) ready(player);}
    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {LAST.remove(event.getEntity().getUUID());}
}
