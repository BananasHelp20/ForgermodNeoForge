package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import java.util.*;

@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class BoneGuardEvents {
    private record Guard(ItemStack stack,int slot,GuardSkeleton skeleton,long expires,int rank,List<AbstractArrow> arrows) {}
    private static final Map<UUID,Guard> GUARDS=new HashMap<>();
    private BoneGuardEvents() {}
    public static boolean activate(ServerPlayer player,ItemStack stack) {
        if(active(player)) return false;
        var skeleton=new GuardSkeleton(player.level(),player);
        var pos=player.position(); boolean clear=false;
        for(int i=0;i<8;i++) {
            double angle=i*Math.PI/4;
            skeleton.setPos(pos.add(Math.cos(angle)*1.5,0,Math.sin(angle)*1.5));
            if(player.level().getWorldBorder().isWithinBounds(skeleton.getBoundingBox()) && player.level().noCollision(skeleton)) {clear=true;break;}
        }
        if(!clear || !player.level().addFreshEntity(skeleton)) return false;
        GUARDS.put(player.getUUID(),new Guard(stack,player.getInventory().selected,skeleton,player.level().getGameTime()+300,
                Augmentations.level(stack,AdditionalAbilities.BONE_GUARD),new ArrayList<>()));
        return true;
    }
    public static boolean active(ServerPlayer player) {
        var guard=GUARDS.get(player.getUUID());if(guard==null) return false;
        if(!player.isAlive() || player.isSpectator() || player.getMainHandItem()!=guard.stack() || player.getInventory().selected!=guard.slot()
                || player.level()!=guard.skeleton().level() || !guard.skeleton().isAlive() || player.level().getGameTime()>=guard.expires()
                || !Augmentations.ids(guard.stack(),true).contains(AdditionalAbilities.BONE_GUARD)) {cancel(player.getUUID());return false;}
        return true;
    }
    public static void cancel(UUID owner) {
        var guard=GUARDS.remove(owner);if(guard!=null) {guard.skeleton().discard();guard.arrows().forEach(Entity::discard);}
    }
    @SubscribeEvent public static void tick(PlayerTickEvent.Pre event) {
        if(!(event.getEntity() instanceof ServerPlayer player) || !active(player)) return;
        var guard=GUARDS.get(player.getUUID());double range=6+2*guard.rank();
        guard.arrows().removeIf(Entity::isRemoved);
        var target=player.level().getEntitiesOfClass(LivingEntity.class,guard.skeleton().getBoundingBox().inflate(range),
                e->e!=guard.skeleton() && WeaponAbilityTargets.enemy(player,e) && guard.skeleton().hasLineOfSight(e))
                .stream().filter(e->guard.skeleton().distanceToSqr(e)<=range*range)
                .min(Comparator.comparingDouble(guard.skeleton()::distanceToSqr)).orElse(null);
        guard.skeleton().setTarget(target);
    }
    @SubscribeEvent public static void impact(ProjectileImpactEvent event) {
        if(!(event.getProjectile() instanceof AbstractArrow arrow) || !(arrow.getOwner() instanceof ServerPlayer player)) return;
        var guard=GUARDS.get(player.getUUID());if(guard==null || !guard.arrows().contains(arrow)) return;
        if(!active(player) || event.getRayTraceResult() instanceof EntityHitResult hit
                && (!(hit.getEntity() instanceof LivingEntity target) || !WeaponAbilityTargets.enemy(player,target))) {
            event.setCanceled(true);arrow.discard();
        }
    }
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {cancel(event.getEntity().getUUID());}
    public static final class GuardSkeleton extends Skeleton {
        private final ServerPlayer owner;
        private GuardSkeleton(Level level,ServerPlayer owner) {
            super(EntityType.SKELETON,level);this.owner=owner;
            targetSelector.removeAllGoals(g->true);
            setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.BOW));setPersistenceRequired();
        }
        @Override protected boolean isSunBurnTick() {return false;}
        @Override public boolean shouldBeSaved() {return false;}
        @Override protected boolean shouldDropLoot() {return false;}
        @Override public boolean shouldDropExperience() {return false;}
        @Override protected void dropEquipment() {}
        @Override public boolean isAlliedTo(Entity target) {return owner!=null && (target==owner || target.isAlliedTo(owner)) || super.isAlliedTo(target);}
        @Override public boolean canAttack(LivingEntity target) {return owner!=null && WeaponAbilityTargets.enemy(owner,target) && super.canAttack(target);}
        @Override protected AbstractArrow getArrow(ItemStack ammo,float velocity,ItemStack weapon) {
            var arrow=super.getArrow(ammo,velocity,weapon);arrow.setOwner(owner);arrow.pickup=AbstractArrow.Pickup.DISALLOWED;
            var guard=GUARDS.get(owner.getUUID());if(guard!=null) guard.arrows().add(arrow);
            return arrow;
        }
    }
}
