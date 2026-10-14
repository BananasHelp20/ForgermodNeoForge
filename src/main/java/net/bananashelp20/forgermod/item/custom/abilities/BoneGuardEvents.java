package net.bananashelp20.forgermod.item.custom.abilities;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import java.util.*;
@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class BoneGuardEvents {
 private record Guard(List<Mob> skeletons,long expires,int rank,List<AbstractArrow> arrows) {}
 private static final Map<UUID,Guard> GUARDS=new HashMap<>();
 private BoneGuardEvents() {}
 public interface FriendlyGuard {ServerPlayer owner();}
 public static boolean activate(ServerPlayer player,ItemStack stack) {
  if(active(player))return false;
  int rank=Augmentations.level(stack,AdditionalAbilities.BONE_GUARD);if(rank<=0)return false;
  var mobs=new ArrayList<Mob>();
  if(rank<4)mobs.add(new GuardSkeleton(player.level(),player));
  else {
   mobs.add(new GuardSkeleton(player.level(),player));
   if(rank>=5)mobs.add(new GuardSkeleton(player.level(),player));
   if(rank>=6)mobs.add(new GuardBogged(player.level(),player));
   if(rank>=7)mobs.add(new GuardStray(player.level(),player));
   for(int i=0;i<(rank>=7?3:rank>=6?2:1);i++)mobs.add(new GuardWitherSkeleton(player.level(),player));
  }
  var spawned=new ArrayList<Mob>();
  for(var mob:mobs) {
   boolean clear=false;
   for(int i=0;i<32;i++) {
    double angle=(i%8)*Math.PI/4,radius=1.5+(i/8)*.75;mob.setPos(player.position().add(Math.cos(angle)*radius,0,Math.sin(angle)*radius));
    var box=mob.getBoundingBox();
    if(player.level().getWorldBorder().isWithinBounds(box) && player.serverLevel().hasChunksAt(mob.blockPosition(),mob.blockPosition()) && player.level().noCollision(mob) && spawned.stream().noneMatch(other->other.getBoundingBox().intersects(box))) {clear=true;break;}
   }
   if(!clear || !player.level().addFreshEntity(mob)) {spawned.forEach(Entity::discard);return false;}
   spawned.add(mob);
  }
  GUARDS.put(player.getUUID(),new Guard(spawned,player.level().getGameTime()+1200,rank,new ArrayList<>()));return true;
 }
 public static boolean active(ServerPlayer player) {
  var guard=GUARDS.get(player.getUUID());if(guard==null)return false;guard.skeletons().removeIf(e->!e.isAlive());
  if(!player.isAlive() || player.isSpectator() || guard.skeletons().isEmpty() || guard.skeletons().getFirst().level()!=player.level()
    || player.level().getGameTime()>=guard.expires()) {cancel(player.getUUID());return false;}
  return true;
 }
 public static void cancel(UUID owner) {var guard=GUARDS.remove(owner);if(guard!=null){guard.skeletons().forEach(Entity::discard);guard.arrows().forEach(Entity::discard);}}
 private static boolean enemy(ServerPlayer owner,LivingEntity target) {
  return !(target instanceof FriendlyGuard) && !(target instanceof IronGolem && !WeaponAbilityTargets.hostileTo(owner,target)) && WeaponAbilityTargets.enemy(owner,target);
 }
 @SubscribeEvent public static void target(LivingChangeTargetEvent event) {
  if(event.getEntity() instanceof IronGolem && event.getNewAboutToBeSetTarget() instanceof FriendlyGuard ally && !WeaponAbilityTargets.hostileTo(ally.owner(),event.getEntity()))event.setNewAboutToBeSetTarget(null);
  if(event.getEntity() instanceof FriendlyGuard ally && event.getNewAboutToBeSetTarget()!=null && !enemy(ally.owner(),event.getNewAboutToBeSetTarget()))event.setNewAboutToBeSetTarget(null);
 }
 @SubscribeEvent public static void tick(PlayerTickEvent.Pre event) {
  if(!(event.getEntity() instanceof ServerPlayer player) || !active(player))return;
  var guard=GUARDS.get(player.getUUID());double range=6+2*Math.min(4,guard.rank());guard.arrows().removeIf(Entity::isRemoved);
  for(var skeleton:guard.skeletons()) {
   var target=player.level().getEntitiesOfClass(LivingEntity.class,skeleton.getBoundingBox().inflate(range),e->enemy(player,e) && skeleton.hasLineOfSight(e))
    .stream().filter(e->skeleton.distanceToSqr(e)<=range*range).min(Comparator.comparingDouble(skeleton::distanceToSqr)).orElse(null);
   skeleton.setTarget(target);
  }
 }
 @SubscribeEvent public static void impact(ProjectileImpactEvent event) {
  if(!(event.getProjectile() instanceof AbstractArrow arrow) || !(arrow.getOwner() instanceof ServerPlayer player))return;
  var guard=GUARDS.get(player.getUUID());if(guard==null || !guard.arrows().contains(arrow))return;
  if(!active(player) || event.getRayTraceResult() instanceof EntityHitResult hit && (!(hit.getEntity() instanceof LivingEntity target) || !enemy(player,target))) {event.setCanceled(true);arrow.discard();}
 }
 @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {cancel(event.getEntity().getUUID());}
 @SubscribeEvent public static void travel(net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent event) {cancel(event.getEntity().getUUID());}
 private static void configure(AbstractSkeleton mob,boolean melee) {
  mob.setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(melee?Items.STONE_SWORD:Items.BOW));mob.setPersistenceRequired();
 }
 private static boolean allied(ServerPlayer owner,Entity target) {return target==owner || target instanceof FriendlyGuard || target.isAlliedTo(owner) || target instanceof IronGolem golem && !WeaponAbilityTargets.hostileTo(owner,golem);}
 private static AbstractArrow ownArrow(AbstractArrow arrow,ServerPlayer owner) {
  arrow.setOwner(owner);arrow.pickup=AbstractArrow.Pickup.DISALLOWED;var guard=GUARDS.get(owner.getUUID());if(guard!=null)guard.arrows().add(arrow);return arrow;
 }
 public static final class GuardSkeleton extends Skeleton implements FriendlyGuard {
  private final ServerPlayer owner;
  private GuardSkeleton(Level level,ServerPlayer owner) {super(EntityType.SKELETON,level);this.owner=owner;targetSelector.removeAllGoals(g->true);configure(this,false);}
  @Override public ServerPlayer owner() {return owner;}
  @Override protected boolean isSunBurnTick() {return false;}
  @Override protected boolean shouldDespawnInPeaceful() {return false;}
  @Override public boolean shouldBeSaved() {return false;}
  @Override protected boolean shouldDropLoot() {return false;}
  @Override public boolean shouldDropExperience() {return false;}
  @Override protected void dropEquipment() {}
  @Override public boolean isAlliedTo(Entity target) {return owner!=null && allied(owner,target) || super.isAlliedTo(target);}
  @Override public boolean canAttack(LivingEntity target) {return owner!=null && enemy(owner,target) && super.canAttack(target);}
  @Override protected AbstractArrow getArrow(ItemStack ammo,float velocity,ItemStack weapon) {return ownArrow(super.getArrow(ammo,velocity,weapon),owner);}
 }
 public static final class GuardBogged extends Bogged implements FriendlyGuard {
  private final ServerPlayer owner;
  private GuardBogged(Level level,ServerPlayer owner) {super(EntityType.BOGGED,level);this.owner=owner;targetSelector.removeAllGoals(g->true);configure(this,false);}
  @Override public ServerPlayer owner() {return owner;}
  @Override protected boolean isSunBurnTick() {return false;}
  @Override protected boolean shouldDespawnInPeaceful() {return false;}
  @Override public boolean shouldBeSaved() {return false;}
  @Override protected boolean shouldDropLoot() {return false;}
  @Override public boolean shouldDropExperience() {return false;}
  @Override protected void dropEquipment() {}
  @Override public boolean isAlliedTo(Entity target) {return owner!=null && allied(owner,target) || super.isAlliedTo(target);}
  @Override public boolean canAttack(LivingEntity target) {return owner!=null && enemy(owner,target) && super.canAttack(target);}
  @Override protected AbstractArrow getArrow(ItemStack ammo,float velocity,ItemStack weapon) {return ownArrow(super.getArrow(ammo,velocity,weapon),owner);}
  @Override public boolean readyForShearing() {return false;}
  @Override public void shear(net.minecraft.sounds.SoundSource source) {}
 }
 public static final class GuardStray extends Stray implements FriendlyGuard {
  private final ServerPlayer owner;
  private GuardStray(Level level,ServerPlayer owner) {super(EntityType.STRAY,level);this.owner=owner;targetSelector.removeAllGoals(g->true);configure(this,false);}
  @Override public ServerPlayer owner() {return owner;}
  @Override protected boolean isSunBurnTick() {return false;}
  @Override protected boolean shouldDespawnInPeaceful() {return false;}
  @Override public boolean shouldBeSaved() {return false;}
  @Override protected boolean shouldDropLoot() {return false;}
  @Override public boolean shouldDropExperience() {return false;}
  @Override protected void dropEquipment() {}
  @Override public boolean isAlliedTo(Entity target) {return owner!=null && allied(owner,target) || super.isAlliedTo(target);}
  @Override public boolean canAttack(LivingEntity target) {return owner!=null && enemy(owner,target) && super.canAttack(target);}
  @Override protected AbstractArrow getArrow(ItemStack ammo,float velocity,ItemStack weapon) {return ownArrow(super.getArrow(ammo,velocity,weapon),owner);}
 }
 public static final class GuardWitherSkeleton extends WitherSkeleton implements FriendlyGuard {
  private final ServerPlayer owner;
  private GuardWitherSkeleton(Level level,ServerPlayer owner) {super(EntityType.WITHER_SKELETON,level);this.owner=owner;targetSelector.removeAllGoals(g->true);configure(this,true);}
  @Override public ServerPlayer owner() {return owner;}
  @Override protected boolean isSunBurnTick() {return false;}
  @Override protected boolean shouldDespawnInPeaceful() {return false;}
  @Override public boolean shouldBeSaved() {return false;}
  @Override protected boolean shouldDropLoot() {return false;}
  @Override public boolean shouldDropExperience() {return false;}
  @Override protected void dropEquipment() {}
  @Override public boolean isAlliedTo(Entity target) {return owner!=null && allied(owner,target) || super.isAlliedTo(target);}
  @Override public boolean canAttack(LivingEntity target) {return owner!=null && enemy(owner,target) && super.canAttack(target);}
  @Override protected AbstractArrow getArrow(ItemStack ammo,float velocity,ItemStack weapon) {return ownArrow(super.getArrow(ammo,velocity,weapon),owner);}
 }
}
