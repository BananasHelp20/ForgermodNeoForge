package net.bananashelp20.forgermod.item.custom.abilities;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.minecraft.nbt.*;
import net.minecraft.server.level.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import java.util.*;
@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class GardenersEvents {
 private static final class Garden {
  final ServerPlayer owner; final ItemStack stack; final int selected; final ServerLevel level;
  long expires; Plant plant; PoisonCloud cloud;
  Garden(ServerPlayer p,ItemStack s) { owner=p; stack=s; selected=p.getInventory().selected; level=p.serverLevel(); expires=level.getGameTime()+1200; }
 }
 /** Vanilla display renderer presents a real plant model without placing terrain or dropping items. */
 public static final class Plant extends Display.BlockDisplay {
  Plant(Level level) { super(EntityType.BLOCK_DISPLAY,level); var tag=new CompoundTag(); tag.put("block_state",NbtUtils.writeBlockState(Blocks.AZALEA.defaultBlockState())); readAdditionalSaveData(tag); }
  @Override public boolean shouldBeSaved() { return false; }
 }
 public static final class PoisonCloud extends AreaEffectCloud {
  PoisonCloud(Level level,double x,double y,double z) { super(level,x,y,z); }
  @Override public boolean shouldBeSaved() { return false; }
 }
 private static final Map<UUID,Garden> GARDENS=new HashMap<>();
 private GardenersEvents() {}
 public static boolean activate(ServerPlayer player,ItemStack stack) { if(active(player)) return false; GARDENS.put(player.getUUID(),new Garden(player,stack)); return true; }
 public static boolean active(ServerPlayer player) {
  var g=GARDENS.get(player.getUUID()); if(g==null) return false;
  if(!player.isAlive() || player.isSpectator() || player.getMainHandItem()!=g.stack || player.getInventory().selected!=g.selected || player.level()!=g.level
    || g.level.getGameTime()>=g.expires || !Augmentations.ids(g.stack,true).contains(RecommendedAbilities.GARDENERS)
    || g.cloud!=null && (g.cloud.isRemoved() || g.plant.isRemoved())) { cancel(player.getUUID()); return false; }
  return true;
 }
 public static void cancel(UUID owner) { var g=GARDENS.remove(owner); if(g!=null && g.plant!=null) { g.plant.discard(); g.cloud.discard(); } }
 @SubscribeEvent(priority=EventPriority.LOWEST)
 public static void onDeath(LivingDeathEvent event) {
  if(event.isCanceled() || !(event.getSource().getEntity() instanceof ServerPlayer player) || !active(player)) return;
  var victim=event.getEntity();
  if(victim==player || victim.isAlliedTo(player) || !(victim instanceof Enemy || victim instanceof Mob mob && mob.getTarget()==player
    || victim instanceof Player other && player.server.isPvpAllowed() && player.canHarmPlayer(other))) return;
  var g=GARDENS.get(player.getUUID()); if(g.cloud!=null) return;
  var pos=victim.position(); g.plant=new Plant(g.level); g.plant.setPos(pos.x-.5,pos.y,pos.z-.5);
  g.cloud=new PoisonCloud(g.level,pos.x,pos.y,pos.z); g.cloud.setOwner(player); g.cloud.setWaitTime(0); g.cloud.setDuration(6000);
  g.cloud.setRadius(2F+.5F*(Augmentations.level(g.stack,RecommendedAbilities.GARDENERS)-1)); g.cloud.setRadiusOnUse(0); g.cloud.setRadiusPerTick(0);
  g.cloud.setDurationOnUse(0); g.cloud.addEffect(new MobEffectInstance(MobEffects.POISON,400,0));
  g.expires=g.level.getGameTime()+6000;
  g.level.addFreshEntity(g.plant); g.level.addFreshEntity(g.cloud);
 }
 @SubscribeEvent public static void onTick(PlayerTickEvent.Post event) { if(event.getEntity() instanceof ServerPlayer player) active(player); }
 @SubscribeEvent public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) { cancel(event.getEntity().getUUID()); }
}
