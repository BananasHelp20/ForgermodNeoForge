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
 private record Guard(List<Mob> skeletons,long expires,int rank,List<AbstractArrow> arrows,UUID loadoutId) {}
 private static final Map<UUID,Guard> GUARDS=new HashMap<>();
 private record Retaliation(Mob victim,LivingEntity guard) {}
 private static final Map<UUID,Retaliation> RETALIATION=new HashMap<>();
 private static final String SHOOTER="forgermod_bone_guard_shooter";
 private BoneGuardEvents() {}
 public interface FriendlyGuard {ServerPlayer owner();}
 public static boolean busy(UUID loadoutId) {
  if(loadoutId==null)return false;
  return GUARDS.values().stream().anyMatch(group->loadoutId.equals(group.loadoutId()) && group.skeletons().stream().anyMatch(mob->mob.isAlive() && !mob.isRemoved()));
 }
 public static void cancelWeapon(UUID loadoutId) {for(var entry:List.copyOf(GUARDS.entrySet()))if(Objects.equals(loadoutId,entry.getValue().loadoutId()))cancel(entry.getKey());}
 public static boolean activate(ServerPlayer player,ItemStack stack) {
  int rank=Augmentations.level(stack,AdditionalAbilities.BONE_GUARD);
  if(rank<=0 || !player.isAlive() || player.isSpectator() || stack!=player.getMainHandItem() || player.containerMenu instanceof net.bananashelp20.forgermod.screen.custom.NecromancerMenu)return false;
  var loadout=rank>=4?NecromancerLoadouts.get(player,stack):null;
  if(loadout!=null && (loadout.types.isEmpty() || loadout.used()>NecromancerCatalog.capacity(rank)))return false;
  if(loadout!=null && GUARDS.entrySet().stream().anyMatch(entry->!entry.getKey().equals(player.getUUID()) && loadout.id.equals(entry.getValue().loadoutId()) && entry.getValue().skeletons().stream().anyMatch(mob->mob.isAlive() && !mob.isRemoved())))return false;
  var units=new ArrayList<NecromancerUndead.Unit>();
  if(rank<4) {
   if(rank==3)units.add(NecromancerUndead.create(6,player));
   units.add(NecromancerUndead.create(0,player));if(rank==2)units.add(NecromancerUndead.create(0,player));
  }else for(int row=0;row<loadout.types.size();row++) {
   var unit=NecromancerUndead.create(loadout.types.get(row),player);units.add(unit);
   if(rank>=5)NecromancerEquipment.bind(unit.equipment(),loadout,row);
  }
  var old=GUARDS.get(player.getUUID());Set<Mob> replaced=old==null?Set.of():new HashSet<>(old.skeletons());
  var staged=new ArrayList<Mob>();
  for(var unit:units) {
   boolean clear=false;
   for(int attempt=0;attempt<64;attempt++) {
    double angle=(attempt%8)*Math.PI/4,radius=1.5+(attempt/8)*.9;
    unit.root().setPos(player.position().add(Math.cos(angle)*radius,0,Math.sin(angle)*radius));
    if(unit.root()!=unit.equipment())unit.equipment().setPos(unit.root().position().add(0,1.6,0));
    clear=true;
    for(var mob:unit.members()) {
     var box=mob.getBoundingBox();
     if(!player.level().getWorldBorder().isWithinBounds(box)
       || !player.serverLevel().hasChunksAt(net.minecraft.core.BlockPos.containing(box.minX,box.minY,box.minZ),net.minecraft.core.BlockPos.containing(box.maxX,box.maxY,box.maxZ))
       || player.level().getBlockCollisions(mob,box).iterator().hasNext()
       || !player.level().getEntities(mob,box,entity->!replaced.contains(entity) && !unit.members().contains(entity) && entity.isAlive() && !entity.isSpectator() && (entity instanceof LivingEntity || entity.canBeCollidedWith())).isEmpty()
       || staged.stream().anyMatch(other->other.getBoundingBox().intersects(box))) {clear=false;break;}
    }
    if(clear)break;
   }
   if(!clear) {for(var made:units)for(var mob:made.members()){NecromancerEquipment.release(mob);mob.discard();}return false;}
   staged.addAll(unit.members());
  }
  var spawned=new ArrayList<Mob>();
  for(var unit:units) {
   for(var mob:unit.members()) {
    if(!player.level().addFreshEntity(mob)) {for(var made:units)for(var member:made.members()){NecromancerEquipment.release(member);member.discard();}return false;}
    spawned.add(mob);
   }
   if(unit.root()!=unit.equipment())unit.equipment().startRiding(unit.root(),true);
  }
  // Keep the old army until the replacement was fully validated and successfully spawned.
  cancel(player.getUUID());
  GUARDS.put(player.getUUID(),new Guard(spawned,player.level().getGameTime()+12000,rank,new ArrayList<>(),loadout==null?null:loadout.id));return true;
 }
 public static boolean active(ServerPlayer player) {
  var guard=GUARDS.get(player.getUUID());if(guard==null)return false;guard.skeletons().removeIf(e->{if(!e.isAlive() || e.isRemoved()){NecromancerEquipment.release(e);return true;}return false;});
  if(!player.isAlive() || player.isSpectator() || guard.skeletons().isEmpty() || guard.skeletons().getFirst().level()!=player.level()
    || player.level().getGameTime()>=guard.expires()) {cancel(player.getUUID());return false;}
  return true;
 }
 public static void cancel(UUID owner) {RETALIATION.values().removeIf(r->r.guard() instanceof FriendlyGuard friendly && friendly.owner().getUUID().equals(owner));var guard=GUARDS.remove(owner);if(guard!=null){guard.skeletons().forEach(mob->{NecromancerEquipment.release(mob);mob.discard();});guard.arrows().forEach(Entity::discard);}}
 static boolean enemy(ServerPlayer owner,LivingEntity target) {
  return !(target instanceof FriendlyGuard) && !(target instanceof IronGolem && !WeaponAbilityTargets.hostileTo(owner,target)) && WeaponAbilityTargets.enemy(owner,target);
 }
 private static boolean tracked(LivingEntity entity) {
  if(!(entity instanceof FriendlyGuard friendly) || !entity.isAlive())return false;
  var group=GUARDS.get(friendly.owner().getUUID());return group!=null && group.skeletons().contains(entity);
 }
 private static final class GuardTargetGoal extends net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal<Mob> {
  GuardTargetGoal(Mob mob) {
   super(mob,Mob.class,10,true,false,target->tracked(target) && target instanceof FriendlyGuard friendly && enemy(friendly.owner(),mob));
  }
  @Override public boolean canUse() {
   if(mob.getTarget() instanceof Mob guard && tracked(guard) && targetConditions.test(mob,guard)) {target=guard;return true;}
   return super.canUse();
  }
 }
 @SubscribeEvent public static void join(net.neoforged.neoforge.event.entity.EntityJoinLevelEvent event) {
  if(!event.getLevel().isClientSide && event.getEntity() instanceof AbstractArrow arrow && arrow.getOwner() instanceof FriendlyGuard friendly)ownArrow(arrow,friendly.owner());
  if(event.getLevel().isClientSide || !(event.getEntity() instanceof Mob mob) || !(mob instanceof Enemy) || mob instanceof FriendlyGuard)return;
  if(mob.targetSelector.getAvailableGoals().stream().noneMatch(goal->goal.getGoal() instanceof GuardTargetGoal))
   mob.targetSelector.addGoal(1,new GuardTargetGoal(mob));
 }
 @SubscribeEvent public static void retaliate(net.neoforged.neoforge.event.entity.living.LivingDamageEvent.Post event) {
  if(!(event.getEntity() instanceof Mob victim) || !(victim.level() instanceof net.minecraft.server.level.ServerLevel level)
    || event.getNewDamage()+event.getReduction(net.neoforged.neoforge.common.damagesource.DamageContainer.Reduction.ABSORPTION)<=0)return;
  RETALIATION.remove(victim.getUUID());
  Entity attacker=event.getSource().getEntity();
  if(event.getSource().getDirectEntity() instanceof AbstractArrow arrow && arrow.getPersistentData().hasUUID(SHOOTER))
   attacker=level.getEntity(arrow.getPersistentData().getUUID(SHOOTER));
  if(attacker instanceof LivingEntity guard && tracked(guard) && guard instanceof FriendlyGuard friendly
    && enemy(friendly.owner(),victim) && victim.canAttack(guard)) {
   // Keep the player as projectile owner for loot/kill abilities, but retaliate against the actual reinforcement.
   victim.setTarget(guard);RETALIATION.put(victim.getUUID(),new Retaliation(victim,guard));
  }
 }
 @SubscribeEvent public static void retaliationTick(net.neoforged.neoforge.event.tick.EntityTickEvent.Pre event) {
  if(event.getEntity().level().isClientSide)return;
  var pending=RETALIATION.remove(event.getEntity().getUUID());if(pending==null)return;
  var victim=pending.victim();var guard=pending.guard();
  if(victim.isAlive() && tracked(guard) && guard instanceof FriendlyGuard friendly && enemy(friendly.owner(),victim) && victim.canAttack(guard)) {
   // LivingEntity.hurt stamps the projectile's player owner AFTER LivingDamageEvent.Post.
   // Correct its revenge memory before the next AI tick can retarget that player.
   victim.setLastHurtByMob(guard);victim.setTarget(guard);
  }
 }
 @SubscribeEvent public static void leave(net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent event) {
  if(event.getLevel().isClientSide)return;
  RETALIATION.remove(event.getEntity().getUUID());RETALIATION.values().removeIf(r->r.guard()==event.getEntity());
 }
 @SubscribeEvent public static void target(LivingChangeTargetEvent event) {
  // Some vanilla target goals cache the arrow's credited player before revenge memory is corrected.
  // Keep that stale selection on the living reinforcement; an actual player hit replaces the memory normally.
  if(event.getEntity() instanceof Mob mob && mob.getLastHurtByMob() instanceof LivingEntity guard
    && guard instanceof FriendlyGuard friendly && event.getNewAboutToBeSetTarget()==friendly.owner()
    && tracked(guard) && enemy(friendly.owner(),mob) && mob.canAttack(guard)) {
   double range=mob.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.FOLLOW_RANGE);
   if(mob.distanceToSqr(guard)<=range*range)event.setNewAboutToBeSetTarget(guard);
  }
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
 static boolean allied(ServerPlayer owner,Entity target) {return target==owner || target instanceof FriendlyGuard || target.isAlliedTo(owner) || target instanceof IronGolem golem && !WeaponAbilityTargets.hostileTo(owner,golem);}
 static AbstractArrow ownArrow(AbstractArrow arrow,ServerPlayer owner) {
  // Capture the real shooter before transferring kill credit; retain the original helper signature.
  if(arrow.getOwner() instanceof FriendlyGuard)arrow.getPersistentData().putUUID(SHOOTER,arrow.getOwner().getUUID());
  arrow.setOwner(owner);arrow.pickup=AbstractArrow.Pickup.DISALLOWED;var guard=GUARDS.get(owner.getUUID());if(guard!=null)guard.arrows().add(arrow);return arrow;
 }
 public static final class GuardSkeleton extends Skeleton implements FriendlyGuard {
  private final ServerPlayer owner;
  GuardSkeleton(Level level,ServerPlayer owner) {super(EntityType.SKELETON,level);this.owner=owner;targetSelector.removeAllGoals(g->true);configure(this,false);}
  @Override public ServerPlayer owner() {return owner;}
  @Override protected boolean isSunBurnTick() {return false;}
  @Override protected boolean shouldDespawnInPeaceful() {return false;}
  @Override public boolean shouldBeSaved() {return false;}
  @Override public boolean canUsePortal(boolean allowPassengers) {return false;}
  @Override public boolean canChangeDimensions(Level oldLevel,Level newLevel) {return false;}
  @Override protected boolean shouldDropLoot() {return false;}
  @Override public boolean shouldDropExperience() {return false;}
  @Override protected void dropEquipment() {}
  @Override public ItemStack getProjectile(ItemStack weapon) {return weapon.getItem() instanceof ProjectileWeaponItem?new ItemStack(Items.ARROW):super.getProjectile(weapon);}
  @Override protected void hurtArmor(net.minecraft.world.damagesource.DamageSource source,float damage) {doHurtEquipment(source,damage,EquipmentSlot.FEET,EquipmentSlot.LEGS,EquipmentSlot.CHEST,EquipmentSlot.HEAD);NecromancerEquipment.changed(this);}
  @Override protected void hurtHelmet(net.minecraft.world.damagesource.DamageSource source,float damage) {doHurtEquipment(source,damage,EquipmentSlot.HEAD);NecromancerEquipment.changed(this);}
  @Override public boolean doHurtTarget(Entity target) {boolean hit=super.doHurtTarget(target);if(hit)NecromancerEquipment.wearWeapon(this);return hit;}
  @Override public boolean isAlliedTo(Entity target) {return owner!=null && allied(owner,target) || super.isAlliedTo(target);}
  @Override public boolean canAttack(LivingEntity target) {return owner!=null && enemy(owner,target) && super.canAttack(target);}
  @Override protected AbstractArrow getArrow(ItemStack ammo,float velocity,ItemStack weapon) {var arrow=ownArrow(super.getArrow(ammo,velocity,weapon),owner);NecromancerEquipment.wearBow(this);return arrow;}
 }
 public static final class GuardBogged extends Bogged implements FriendlyGuard {
  private final ServerPlayer owner;
  GuardBogged(Level level,ServerPlayer owner) {super(EntityType.BOGGED,level);this.owner=owner;targetSelector.removeAllGoals(g->true);configure(this,false);}
  @Override public ServerPlayer owner() {return owner;}
  @Override protected boolean isSunBurnTick() {return false;}
  @Override protected boolean shouldDespawnInPeaceful() {return false;}
  @Override public boolean shouldBeSaved() {return false;}
  @Override public boolean canUsePortal(boolean allowPassengers) {return false;}
  @Override public boolean canChangeDimensions(Level oldLevel,Level newLevel) {return false;}
  @Override protected boolean shouldDropLoot() {return false;}
  @Override public boolean shouldDropExperience() {return false;}
  @Override protected void dropEquipment() {}
  @Override public ItemStack getProjectile(ItemStack weapon) {return weapon.getItem() instanceof ProjectileWeaponItem?new ItemStack(Items.ARROW):super.getProjectile(weapon);}
  @Override protected void hurtArmor(net.minecraft.world.damagesource.DamageSource source,float damage) {doHurtEquipment(source,damage,EquipmentSlot.FEET,EquipmentSlot.LEGS,EquipmentSlot.CHEST,EquipmentSlot.HEAD);NecromancerEquipment.changed(this);}
  @Override protected void hurtHelmet(net.minecraft.world.damagesource.DamageSource source,float damage) {doHurtEquipment(source,damage,EquipmentSlot.HEAD);NecromancerEquipment.changed(this);}
  @Override public boolean doHurtTarget(Entity target) {boolean hit=super.doHurtTarget(target);if(hit)NecromancerEquipment.wearWeapon(this);return hit;}
  @Override public boolean isAlliedTo(Entity target) {return owner!=null && allied(owner,target) || super.isAlliedTo(target);}
  @Override public boolean canAttack(LivingEntity target) {return owner!=null && enemy(owner,target) && super.canAttack(target);}
  @Override protected AbstractArrow getArrow(ItemStack ammo,float velocity,ItemStack weapon) {var arrow=ownArrow(super.getArrow(ammo,velocity,weapon),owner);NecromancerEquipment.wearBow(this);return arrow;}
  @Override public boolean readyForShearing() {return false;}
  @Override public void shear(net.minecraft.sounds.SoundSource source) {}
 }
 public static final class GuardStray extends Stray implements FriendlyGuard {
  private final ServerPlayer owner;
  GuardStray(Level level,ServerPlayer owner) {super(EntityType.STRAY,level);this.owner=owner;targetSelector.removeAllGoals(g->true);configure(this,false);}
  @Override public ServerPlayer owner() {return owner;}
  @Override protected boolean isSunBurnTick() {return false;}
  @Override protected boolean shouldDespawnInPeaceful() {return false;}
  @Override public boolean shouldBeSaved() {return false;}
  @Override public boolean canUsePortal(boolean allowPassengers) {return false;}
  @Override public boolean canChangeDimensions(Level oldLevel,Level newLevel) {return false;}
  @Override protected boolean shouldDropLoot() {return false;}
  @Override public boolean shouldDropExperience() {return false;}
  @Override protected void dropEquipment() {}
  @Override public ItemStack getProjectile(ItemStack weapon) {return weapon.getItem() instanceof ProjectileWeaponItem?new ItemStack(Items.ARROW):super.getProjectile(weapon);}
  @Override protected void hurtArmor(net.minecraft.world.damagesource.DamageSource source,float damage) {doHurtEquipment(source,damage,EquipmentSlot.FEET,EquipmentSlot.LEGS,EquipmentSlot.CHEST,EquipmentSlot.HEAD);NecromancerEquipment.changed(this);}
  @Override protected void hurtHelmet(net.minecraft.world.damagesource.DamageSource source,float damage) {doHurtEquipment(source,damage,EquipmentSlot.HEAD);NecromancerEquipment.changed(this);}
  @Override public boolean doHurtTarget(Entity target) {boolean hit=super.doHurtTarget(target);if(hit)NecromancerEquipment.wearWeapon(this);return hit;}
  @Override public boolean isAlliedTo(Entity target) {return owner!=null && allied(owner,target) || super.isAlliedTo(target);}
  @Override public boolean canAttack(LivingEntity target) {return owner!=null && enemy(owner,target) && super.canAttack(target);}
  @Override protected AbstractArrow getArrow(ItemStack ammo,float velocity,ItemStack weapon) {var arrow=ownArrow(super.getArrow(ammo,velocity,weapon),owner);NecromancerEquipment.wearBow(this);return arrow;}
 }
 public static final class GuardWitherSkeleton extends WitherSkeleton implements FriendlyGuard {
  private final ServerPlayer owner;
  GuardWitherSkeleton(Level level,ServerPlayer owner) {super(EntityType.WITHER_SKELETON,level);this.owner=owner;targetSelector.removeAllGoals(g->true);configure(this,true);}
  @Override public ServerPlayer owner() {return owner;}
  @Override protected boolean isSunBurnTick() {return false;}
  @Override protected boolean shouldDespawnInPeaceful() {return false;}
  @Override public boolean shouldBeSaved() {return false;}
  @Override public boolean canUsePortal(boolean allowPassengers) {return false;}
  @Override public boolean canChangeDimensions(Level oldLevel,Level newLevel) {return false;}
  @Override protected boolean shouldDropLoot() {return false;}
  @Override public boolean shouldDropExperience() {return false;}
  @Override protected void dropEquipment() {}
  @Override public ItemStack getProjectile(ItemStack weapon) {return weapon.getItem() instanceof ProjectileWeaponItem?new ItemStack(Items.ARROW):super.getProjectile(weapon);}
  @Override protected void hurtArmor(net.minecraft.world.damagesource.DamageSource source,float damage) {doHurtEquipment(source,damage,EquipmentSlot.FEET,EquipmentSlot.LEGS,EquipmentSlot.CHEST,EquipmentSlot.HEAD);NecromancerEquipment.changed(this);}
  @Override protected void hurtHelmet(net.minecraft.world.damagesource.DamageSource source,float damage) {doHurtEquipment(source,damage,EquipmentSlot.HEAD);NecromancerEquipment.changed(this);}
  @Override public boolean doHurtTarget(Entity target) {boolean hit=super.doHurtTarget(target);if(hit)NecromancerEquipment.wearWeapon(this);return hit;}
  @Override public boolean isAlliedTo(Entity target) {return owner!=null && allied(owner,target) || super.isAlliedTo(target);}
  @Override public boolean canAttack(LivingEntity target) {return owner!=null && enemy(owner,target) && super.canAttack(target);}
  @Override protected AbstractArrow getArrow(ItemStack ammo,float velocity,ItemStack weapon) {var arrow=ownArrow(super.getArrow(ammo,velocity,weapon),owner);NecromancerEquipment.wearBow(this);return arrow;}
 }
}
