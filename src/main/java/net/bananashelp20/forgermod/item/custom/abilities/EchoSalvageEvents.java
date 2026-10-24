package net.bananashelp20.forgermod.item.custom.abilities;
import net.bananashelp20.forgermod.ForgerMod;
import net.minecraft.world.entity.LivingEntity;
import net.bananashelp20.forgermod.augmentation.*;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import java.util.*;
@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class EchoSalvageEvents {
 private record Sound(ItemStack stack,int selected,ServerLevel level,long expires,float bonus) {}
 private static final Map<UUID,Sound> SOUNDS=new HashMap<>();
 private EchoSalvageEvents() {}
 public static float bonus(LivingEntity player) {
  var sound=SOUNDS.get(player.getUUID()); if(sound==null) return 0;
  if(!player.isAlive() || player.isSpectator() || PassiveActors.weapon(player,RecommendedAbilities.ECHO_SALVAGE)!=sound.stack || PassiveActors.selected(player)!=sound.selected || player.level()!=sound.level
   || sound.level.getGameTime()>=sound.expires || !Augmentations.ids(sound.stack,false).contains(RecommendedAbilities.ECHO_SALVAGE)) { SOUNDS.remove(player.getUUID()); return 0; }
  return sound.bonus;
 }
 /** Called only from the actual completed intrinsic vertical slam, never an ordinary swing. */
 public static void onImpact(LivingEntity player,boolean hitAnything,boolean hitEnemy,boolean solidImpact) {
  float stored=bonus(player);
  if(hitEnemy) { if(stored>0) PassiveActors.level(player).sendParticles(ParticleTypes.SCULK_CHARGE_POP,player.getX(),player.getY()+1,player.getZ(),8,.3,.3,.3,.03); SOUNDS.remove(player.getUUID()); return; }
  if(hitAnything || !solidImpact || !player.isAlive() || player.isSpectator()) return;
  var stack=PassiveActors.weapon(player,RecommendedAbilities.ECHO_SALVAGE); int rank=Augmentations.level(stack,RecommendedAbilities.ECHO_SALVAGE); if(rank<=0) return;
  float amount=(float)Math.min(2*rank,.5*player.getAttributeValue(Attributes.ATTACK_DAMAGE));
  SOUNDS.put(player.getUUID(),new Sound(stack,PassiveActors.selected(player),PassiveActors.level(player),player.level().getGameTime()+600,amount));
  PassiveActors.level(player).sendParticles(ParticleTypes.SCULK_CHARGE_POP,player.getX(),player.getY()+.5,player.getZ(),4,.2,.2,.2,.01);
 }
 @SubscribeEvent public static void onTick(PassiveActorTickEvent.Post event) { if(PassiveActors.entity(event.getEntity()) instanceof LivingEntity player) bonus(player); }
 @SubscribeEvent public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) { SOUNDS.remove(event.getEntity().getUUID()); }
    public static void onTick(PlayerTickEvent.Post event){onTick(new PassiveActorTickEvent.Post(event.getEntity()));}
}
