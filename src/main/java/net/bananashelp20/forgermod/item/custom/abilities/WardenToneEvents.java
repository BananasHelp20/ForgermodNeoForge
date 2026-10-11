package net.bananashelp20.forgermod.item.custom.abilities;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.VanillaGameEvent;
@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class WardenToneEvents {
 private WardenToneEvents() {}
 @SubscribeEvent(priority=EventPriority.HIGHEST)
 public static void onVibration(VanillaGameEvent event) {
  if(event.getLevel().isClientSide || event.getVanillaEvent().is(GameEvent.ENTITY_DAMAGE)) return;
  Entity cause=event.getCause(); if(cause instanceof Projectile projectile) cause=projectile.getOwner();
  if(cause instanceof ServerPlayer player && player.isAlive() && !player.isSpectator() && player.level()==event.getLevel()
   && Augmentations.ids(player.getMainHandItem(),false).contains(RecommendedAbilities.WARDEN_TONE)) event.setCanceled(true);
 }
}
