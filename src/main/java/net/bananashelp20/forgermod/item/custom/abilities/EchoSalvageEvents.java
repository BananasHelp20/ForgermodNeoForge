package net.bananashelp20.forgermod.item.custom.abilities;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.custom.attacks.axe.*;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import java.util.*;
@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class EchoSalvageEvents {
 private static final String STORAGE="forgermod_echo_salvage";
 private static final Map<UUID,Integer> VERTICAL=new HashMap<>();
 private static final Set<UUID> RELEASING=new HashSet<>();
 private static final class Hit {
  final ItemStack stack;final UUID target;final long tick;DamageSource source;float bonus;
  Hit(LivingEntity actor,LivingEntity target,ItemStack stack){this.stack=stack;this.target=target.getUUID();tick=actor.level().getGameTime();}
 }
 private static final Map<UUID,Hit> HITS=new HashMap<>();
 private EchoSalvageEvents(){}
 public static int capacity(int rank){return rank<=0?0:Math.clamp(rank,1,4)+1;}
 private static List<Float> stored(ItemStack stack){
  int capacity=capacity(Augmentations.level(stack,RecommendedAbilities.ECHO_SALVAGE));if(capacity==0)return List.of();
  var values=new ArrayList<Float>();var list=stack.getOrDefault(DataComponents.CUSTOM_DATA,CustomData.EMPTY).copyTag().getList(STORAGE,Tag.TAG_FLOAT);
  for(int i=0;i<list.size() && values.size()<capacity;i++){float value=list.getFloat(i);if(Float.isFinite(value) && value>0)values.add(value);}return values;
 }
 public static int storedCount(ItemStack stack){return stored(stack).size();}
 private static void write(LivingEntity actor,ItemStack stack,List<Float> values){
  CustomData.update(DataComponents.CUSTOM_DATA,stack,tag->{if(values.isEmpty())tag.remove(STORAGE);else{var list=new ListTag();for(float value:values)list.add(FloatTag.valueOf(value));tag.put(STORAGE,list);}});
  if(actor instanceof Mob mob)NecromancerEquipment.changed(mob);
 }
 public static float bonus(LivingEntity actor){
  if(!actor.isAlive() || actor.isSpectator())return 0;
  var stack=PassiveActors.weapon(actor,RecommendedAbilities.ECHO_SALVAGE);if(!Augmentations.hasPassive(stack,RecommendedAbilities.ECHO_SALVAGE))return 0;
  double sum=0;for(float value:stored(stack))sum+=value;return (float)Math.min(Float.MAX_VALUE,sum);
 }
 public static void vertical(LivingEntity actor,Runnable action){
  UUID id=actor.getUUID();VERTICAL.merge(id,1,Integer::sum);
  try{action.run();}finally{VERTICAL.computeIfPresent(id,(key,depth)->depth<=1?null:depth-1);}
 }
 private static boolean canRelease(LivingEntity actor){return actor.isAlive() && !actor.isSpectator() && !WeaponControlLocks.frozen(actor)
   && actor.getOffhandItem().isEmpty() && AxeItems.canVerticalStrike(actor.getMainHandItem()) && bonus(actor)>0;}
 /** Only completed solid misses charge; a blocked/rejected entity hit never becomes a stored miss. */
 public static void onImpact(LivingEntity actor,boolean hitAnything,boolean attemptedHit,boolean solidImpact,float strikeDamage){
  if(RELEASING.contains(actor.getUUID()))return;
  var stack=PassiveActors.weapon(actor,RecommendedAbilities.ECHO_SALVAGE);float bonus=bonus(actor);
  if(hitAnything){if(bonus>0){releaseSound(actor,storedCount(stack));write(actor,stack,List.of());spark(actor,8);}return;}
  if(attemptedHit || !solidImpact || !actor.isAlive() || actor.isSpectator() || !Augmentations.hasPassive(stack,RecommendedAbilities.ECHO_SALVAGE))return;
  var values=new ArrayList<>(stored(stack));float value=strikeDamage*.5F;
  if(values.size()>=capacity(Augmentations.level(stack,RecommendedAbilities.ECHO_SALVAGE)) || !Float.isFinite(value) || value<=0)return;
  values.add(value);write(actor,stack,values);spark(actor,4);
 }
 private static void spark(LivingEntity actor,int count){PassiveActors.level(actor).sendParticles(ParticleTypes.SCULK_CHARGE_POP,actor.getX(),actor.getY()+.6,actor.getZ(),count,.3,.3,.3,.03);}
 private static void releaseSound(LivingEntity actor,int strikes){
  // One cue per consumed bank, not per victim or stored strike; a fuller bank sounds deeper.
  PassiveActors.level(actor).playSound(null,actor.getX(),actor.getY()+.6,actor.getZ(),
    net.bananashelp20.forgermod.sound.ModSounds.ECHO_SALVAGE_RELEASE.get(),actor.getSoundSource(),1F,1.12F-.04F*Math.clamp(strikes,1,5));
 }
 @SubscribeEvent(priority=EventPriority.LOWEST,receiveCanceled=true)
 public static void attack(AttackEntityEvent event){
  if(!(event.getEntity() instanceof ServerPlayer actor))return;HITS.remove(actor.getUUID());
  if(!event.isCanceled() && canRelease(actor) && event.getTarget() instanceof LivingEntity target && !target.isInvulnerableTo(actor.damageSources().playerAttack(actor)))
   HITS.put(actor.getUUID(),new Hit(actor,target,actor.getMainHandItem()));
 }
 @SubscribeEvent(priority=EventPriority.HIGHEST)
 public static void incoming(LivingIncomingDamageEvent event){
  if(event.isCanceled() || event.getAmount()<=0 || !PassiveActors.melee(event.getSource()) || !(PassiveActors.entity(PassiveActors.attacker(event.getSource())) instanceof LivingEntity actor)
   || VERTICAL.containsKey(actor.getUUID()) || RELEASING.contains(actor.getUUID()) || !canRelease(actor) || !PassiveActors.canAffect(actor,event.getEntity()))return;
  var hit=HITS.get(actor.getUUID());
  if(actor instanceof Mob && (hit==null || hit.tick!=actor.level().getGameTime())){hit=new Hit(actor,event.getEntity(),actor.getMainHandItem());HITS.put(actor.getUUID(),hit);}
  if(hit==null || hit.tick!=actor.level().getGameTime() || hit.stack!=actor.getMainHandItem() || !hit.target.equals(event.getEntity().getUUID()) || hit.source!=null)return;
  hit.source=event.getSource();hit.bonus=bonus(actor);
  // Conversion supplies at least a full vertical strike, preserving a stronger native critical/enchantment hit.
  event.setAmount(Math.max(event.getAmount(),(float)actor.getAttributeValue(Attributes.ATTACK_DAMAGE))+hit.bonus);
 }
 @SubscribeEvent(priority=EventPriority.LOWEST)
 public static void landed(LivingDamageEvent.Post event){
  if(!(PassiveActors.entity(PassiveActors.attacker(event.getSource())) instanceof LivingEntity actor) || VERTICAL.containsKey(actor.getUUID()) || RELEASING.contains(actor.getUUID()))return;
  var hit=HITS.get(actor.getUUID());
  if(hit==null || hit.source!=event.getSource() || !hit.target.equals(event.getEntity().getUUID()) || hit.tick!=actor.level().getGameTime()
    || hit.stack!=actor.getMainHandItem() || hit.bonus<=0 || event.getNewDamage()+event.getReduction(net.neoforged.neoforge.common.damagesource.DamageContainer.Reduction.ABSORPTION)<=0)return;
  HITS.remove(actor.getUUID());releaseSound(actor,storedCount(hit.stack));write(actor,hit.stack,List.of());spark(actor,8);RELEASING.add(actor.getUUID());
  try{
   if(actor instanceof ServerPlayer player)AxeHeavyNetwork.echoVerticalStrike(player,hit.stack,event.getEntity(),hit.bonus);
   else if(actor instanceof Mob mob)NecromancerPassiveCombat.echoVerticalStrike(mob,hit.stack,event.getEntity(),hit.bonus);
  }finally{RELEASING.remove(actor.getUUID());}
 }
 @SubscribeEvent public static void onTick(PassiveActorTickEvent.Post event){
  if(!(event.getEntity() instanceof LivingEntity actor))return;var hit=HITS.get(actor.getUUID());if(hit!=null && (hit.tick!=actor.level().getGameTime() || !actor.isAlive() || actor.getMainHandItem()!=hit.stack))HITS.remove(actor.getUUID());
 }
 @SubscribeEvent public static void leave(net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent event){if(!event.getLevel().isClientSide)HITS.remove(event.getEntity().getUUID());}
 @SubscribeEvent public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event){HITS.remove(event.getEntity().getUUID());}
 public static void onTick(PlayerTickEvent.Post event){onTick(new PassiveActorTickEvent.Post(event.getEntity()));}
}
