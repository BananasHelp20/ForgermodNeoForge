package net.bananashelp20.forgermod.item.custom.abilities;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.*;
import net.minecraft.server.level.*;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.gameevent.*;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.VanillaGameEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import java.util.*;
@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class EchoPinEvents {
 private record Pin(ServerPlayer owner,ItemStack stack,int selected,ServerLevel level,LivingEntity target,long expires,int rank) {}
 private static final Map<UUID,Pin> PINS=new HashMap<>();
 private static final Map<UUID,Set<UUID>> TARGETS=new HashMap<>();
 private static final Set<Holder<GameEvent>> LOUD=Set.of(GameEvent.ENTITY_DAMAGE,GameEvent.ENTITY_ACTION,GameEvent.PROJECTILE_SHOOT,GameEvent.HIT_GROUND,
  GameEvent.SPLASH,GameEvent.EXPLODE,GameEvent.PRIME_FUSE,GameEvent.INSTRUMENT_PLAY,GameEvent.BLOCK_DESTROY,GameEvent.BLOCK_PLACE);
 private EchoPinEvents() {}
 public static boolean activate(ServerPlayer player,ItemStack stack) {
  if(active(player)) return false; int rank=Augmentations.level(stack,RecommendedAbilities.ECHO_PIN);
  var target=WeaponAbilityTargets.aimedEnemy(player,20+5*(rank-1)); if(target==null) return false;
  var pin=new Pin(player,stack,player.getInventory().selected,player.serverLevel(),target,player.level().getGameTime()+600,rank);
  PINS.put(player.getUUID(),pin); TARGETS.computeIfAbsent(target.getUUID(),ignored->new HashSet<>()).add(player.getUUID()); return true;
 }
 public static boolean active(ServerPlayer player) {
  var pin=PINS.get(player.getUUID()); if(pin==null) return false;
  if(!player.isAlive() || player.isSpectator() || player.getMainHandItem()!=pin.stack || player.getInventory().selected!=pin.selected || player.level()!=pin.level
   || pin.level.getGameTime()>=pin.expires || !pin.target.isAlive() || pin.target.level()!=pin.level || !WeaponAbilityTargets.enemy(player,pin.target)
   || !Augmentations.ids(pin.stack,true).contains(RecommendedAbilities.ECHO_PIN)) { cancel(player.getUUID()); return false; }
  return true;
 }
 public static void cancel(UUID owner) {
  var pin=PINS.remove(owner); if(pin==null) return; var owners=TARGETS.get(pin.target.getUUID());
  if(owners!=null) { owners.remove(owner); if(owners.isEmpty()) TARGETS.remove(pin.target.getUUID()); }
 }
 @SubscribeEvent(priority=EventPriority.LOWEST)
 public static void onSound(VanillaGameEvent event) {
  if(event.isCanceled() || event.getCause()==null || !LOUD.contains(event.getVanillaEvent())) return;
  var owners=TARGETS.get(event.getCause().getUUID()); if(owners==null) return;
  for(var owner:Set.copyOf(owners)) {
   var pin=PINS.get(owner); if(pin==null || pin.target!=event.getCause() || event.getLevel()!=pin.level || !active(pin.owner)) continue;
   var position=event.getEventPosition(); int travel=Math.clamp((int)Math.ceil(position.distanceTo(pin.owner.getEyePosition())),5,40);
   var pulse=new VibrationParticleOption(new EntityPositionSource(pin.owner,pin.owner.getEyeHeight()),travel);
   pin.level.sendParticles(pulse,position.x,position.y,position.z,1,0,0,0,0);
   pin.target.addEffect(new MobEffectInstance(MobEffects.GLOWING,20*(1+pin.rank),0)); cancel(owner);
  }
 }
 @SubscribeEvent public static void onTick(PlayerTickEvent.Post event) {
  if(!(event.getEntity() instanceof ServerPlayer player) || !active(player)) return;
  var pin=PINS.get(player.getUUID()); if(pin.level.getGameTime()%20==0) pin.level.sendParticles(ParticleTypes.ENCHANT,pin.target.getX(),pin.target.getY()+1,pin.target.getZ(),2,.1,.2,.1,0);
 }
 @SubscribeEvent public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) { cancel(event.getEntity().getUUID()); }
}
