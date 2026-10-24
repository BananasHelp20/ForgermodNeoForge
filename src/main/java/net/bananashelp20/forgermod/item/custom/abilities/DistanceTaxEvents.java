package net.bananashelp20.forgermod.item.custom.abilities;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.*;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import java.util.*;
@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class DistanceTaxEvents {
 private static final TicketType<UUID> TICKET=TicketType.create("forgermod_distance_tax",UUID::compareTo,240);
 private static final class Mark {
  final ItemStack stack;final int selected,rank;final ServerLevel level;LivingEntity target;long due;ChunkPos chunk;
  Mark(ServerPlayer player,ItemStack stack){this.stack=stack;selected=player.getInventory().selected;rank=Augmentations.level(stack,RecommendedAbilities.DISTANCE_TAX);level=player.serverLevel();}
 }
 private static final Map<UUID,Mark> MARKS=new HashMap<>();
 private DistanceTaxEvents(){}
 public static float damage(double distance,int rank){return (float)Math.min(40,Math.max(0,distance)*(.25+.25*Math.clamp(rank,1,4)));}
 private static boolean target(ServerPlayer player,LivingEntity target){return target!=player && target.isAlive() && !target.isRemoved() && (target instanceof Mob || WeaponAbilityTargets.enemy(player,target));}
 public static boolean activate(ServerPlayer player,ItemStack stack){
  if(!player.isAlive() || player.isSpectator() || WeaponControlLocks.frozen(player) || stack!=player.getMainHandItem() || !Augmentations.ids(stack,true).contains(RecommendedAbilities.DISTANCE_TAX))return false;
  if(active(player))return MARKS.get(player.getUUID()).target==null;
  MARKS.put(player.getUUID(),new Mark(player,stack));return true;
 }
 public static boolean active(ServerPlayer player){
  var mark=MARKS.get(player.getUUID());if(mark==null)return false;
  if(player.isAlive() && !player.isSpectator() && player.serverLevel()==mark.level && player.getMainHandItem()==mark.stack && player.getInventory().selected==mark.selected
    && Augmentations.ids(mark.stack,true).contains(RecommendedAbilities.DISTANCE_TAX) && (mark.target==null || mark.target.level()==mark.level && target(player,mark.target)))return true;
  cancel(player.getUUID());return false;
 }
 public static void cancel(UUID owner){var mark=MARKS.remove(owner);if(mark!=null && mark.chunk!=null)mark.level.getChunkSource().removeRegionTicket(TICKET,mark.chunk,2,owner);}
 private static void track(UUID owner,Mark mark){
  var current=mark.target.chunkPosition();if(current.equals(mark.chunk))return;
  mark.level.getChunkSource().addRegionTicket(TICKET,current,2,owner);
  if(mark.chunk!=null)mark.level.getChunkSource().removeRegionTicket(TICKET,mark.chunk,2,owner);mark.chunk=current;
 }
 @SubscribeEvent public static void onHit(LivingDamageEvent.Post event){
  if(!event.getSource().is(DamageTypes.PLAYER_ATTACK) || !(event.getSource().getEntity() instanceof ServerPlayer player)
   || event.getNewDamage()+event.getReduction(net.neoforged.neoforge.common.damagesource.DamageContainer.Reduction.ABSORPTION)<=0 || !active(player))return;
  var mark=MARKS.get(player.getUUID());if(mark.target!=null || !target(player,event.getEntity()))return;
  mark.target=event.getEntity();mark.due=player.level().getGameTime()+200;track(player.getUUID(),mark);
 }
 @SubscribeEvent public static void onTick(PassiveActorTickEvent.Post event){
  if(!(event.getEntity() instanceof ServerPlayer player) || !active(player))return;var mark=MARKS.get(player.getUUID());if(mark.target==null)return;
  var target=mark.target;track(player.getUUID(),mark);
  if(player.level().getGameTime()>=mark.due){
   float amount=damage(player.distanceTo(target),mark.rank);cancel(player.getUUID());
   if(amount>0){int immunity=target.invulnerableTime;target.invulnerableTime=0;
    try{target.hurt(player.damageSources().indirectMagic(player,player),amount);}finally{target.invulnerableTime=Math.max(immunity,target.invulnerableTime);}
    player.serverLevel().sendParticles(ParticleTypes.PORTAL,target.getX(),target.getY()+1,target.getZ(),20,.3,.5,.3,.3);
   }
  }else if(player.level().getGameTime()%10==0)player.serverLevel().sendParticles(ParticleTypes.REVERSE_PORTAL,target.getX(),target.getY()+1,target.getZ(),3,.3,.5,.3,.01);
 }
 @SubscribeEvent public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event){cancel(event.getEntity().getUUID());}
 public static void onTick(PlayerTickEvent.Post event){onTick(new PassiveActorTickEvent.Post(event.getEntity()));}
}
