package net.bananashelp20.forgermod.item.custom.abilities;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.animal.horse.SkeletonHorse;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.damagesource.DamageSource;
import java.util.List;

/** Real vanilla AI/entity types, with explicit summon ownership and no loot, XP, sun damage or persistence. */
public final class NecromancerUndead {
    public record Unit(Mob root,Mob equipment,List<Mob> members){}
    private NecromancerUndead(){}
    public static Unit create(int choice,ServerPlayer owner){
        Mob mob=switch(choice){
            case 0->new BoneGuardEvents.GuardSkeleton(owner.level(),owner);
            case 1->new GuardZombie(owner.level(),owner);
            case 2->new GuardZombieVillager(owner.level(),owner);
            case 3->new GuardHusk(owner.level(),owner);
            case 4->new GuardDrowned(owner.level(),owner);
            case 5->new BoneGuardEvents.GuardStray(owner.level(),owner);
            case 6->new BoneGuardEvents.GuardBogged(owner.level(),owner);
            case 7->new GuardZombifiedPiglin(owner.level(),owner);
            case 8->new GuardPhantom(owner.level(),owner);
            case 9->new BoneGuardEvents.GuardWitherSkeleton(owner.level(),owner);
            case 10->new GuardHorse(owner.level(),owner);
            case 11->new GuardZoglin(owner.level(),owner);
            default->throw new IllegalArgumentException("Unknown undead choice "+choice);
        };
        configure(mob);
        if(choice==10){var rider=new BoneGuardEvents.GuardSkeleton(owner.level(),owner);configure(rider);return new Unit(mob,rider,List.of(mob,rider));}
        return new Unit(mob,mob,List.of(mob));
    }
    public static void configure(Mob mob){mob.targetSelector.removeAllGoals(goal->true);mob.setPersistenceRequired();NecromancerCombat.configure(mob);}

    public static final class GuardZombie extends Zombie implements BoneGuardEvents.FriendlyGuard {
        private final ServerPlayer owner;
        GuardZombie(Level level,ServerPlayer owner){super(EntityType.ZOMBIE,level);this.owner=owner;}
        @Override public ServerPlayer owner(){return owner;}
        @Override protected boolean isSunBurnTick(){return false;}
        @Override protected boolean shouldDespawnInPeaceful(){return false;}
        @Override public boolean shouldBeSaved(){return false;}
        @Override public boolean canUsePortal(boolean allowPassengers){return false;}
        @Override public boolean canChangeDimensions(Level oldLevel,Level newLevel){return false;}
        @Override protected boolean shouldDropLoot(){return false;}
        @Override public boolean shouldDropExperience(){return false;}
        @Override protected void dropEquipment(){}
        @Override public boolean isAlliedTo(Entity target){return owner!=null && BoneGuardEvents.allied(owner,target) || super.isAlliedTo(target);}
        @Override public boolean canAttack(LivingEntity target){return owner!=null && BoneGuardEvents.enemy(owner,target) && super.canAttack(target);}
        @Override public ItemStack getProjectile(ItemStack weapon){return weapon.getItem() instanceof ProjectileWeaponItem?new ItemStack(Items.ARROW):super.getProjectile(weapon);}
        @Override protected void hurtArmor(DamageSource source,float damage){doHurtEquipment(source,damage,EquipmentSlot.FEET,EquipmentSlot.LEGS,EquipmentSlot.CHEST,EquipmentSlot.HEAD);NecromancerEquipment.changed(this);}
        @Override protected void hurtHelmet(DamageSource source,float damage){doHurtEquipment(source,damage,EquipmentSlot.HEAD);NecromancerEquipment.changed(this);}
        @Override public boolean doHurtTarget(Entity target){boolean hit=super.doHurtTarget(target);if(hit)NecromancerEquipment.wearWeapon(this);return hit;}
    }

    public static final class GuardZombieVillager extends ZombieVillager implements BoneGuardEvents.FriendlyGuard {
        private final ServerPlayer owner;
        GuardZombieVillager(Level level,ServerPlayer owner){super(EntityType.ZOMBIE_VILLAGER,level);this.owner=owner;}
        @Override public ServerPlayer owner(){return owner;}
        @Override protected boolean isSunBurnTick(){return false;}
        @Override protected boolean shouldDespawnInPeaceful(){return false;}
        @Override public boolean shouldBeSaved(){return false;}
        @Override public boolean canUsePortal(boolean allowPassengers){return false;}
        @Override public boolean canChangeDimensions(Level oldLevel,Level newLevel){return false;}
        @Override protected boolean shouldDropLoot(){return false;}
        @Override public boolean shouldDropExperience(){return false;}
        @Override protected void dropEquipment(){}
        @Override public boolean isAlliedTo(Entity target){return owner!=null && BoneGuardEvents.allied(owner,target) || super.isAlliedTo(target);}
        @Override public boolean canAttack(LivingEntity target){return owner!=null && BoneGuardEvents.enemy(owner,target) && super.canAttack(target);}
        @Override public ItemStack getProjectile(ItemStack weapon){return weapon.getItem() instanceof ProjectileWeaponItem?new ItemStack(Items.ARROW):super.getProjectile(weapon);}
        @Override protected void hurtArmor(DamageSource source,float damage){doHurtEquipment(source,damage,EquipmentSlot.FEET,EquipmentSlot.LEGS,EquipmentSlot.CHEST,EquipmentSlot.HEAD);NecromancerEquipment.changed(this);}
        @Override protected void hurtHelmet(DamageSource source,float damage){doHurtEquipment(source,damage,EquipmentSlot.HEAD);NecromancerEquipment.changed(this);}
        @Override public boolean doHurtTarget(Entity target){boolean hit=super.doHurtTarget(target);if(hit)NecromancerEquipment.wearWeapon(this);return hit;}
    }

    public static final class GuardHusk extends Husk implements BoneGuardEvents.FriendlyGuard {
        private final ServerPlayer owner;
        GuardHusk(Level level,ServerPlayer owner){super(EntityType.HUSK,level);this.owner=owner;}
        @Override public ServerPlayer owner(){return owner;}
        @Override protected boolean isSunBurnTick(){return false;}
        @Override protected boolean shouldDespawnInPeaceful(){return false;}
        @Override public boolean shouldBeSaved(){return false;}
        @Override public boolean canUsePortal(boolean allowPassengers){return false;}
        @Override public boolean canChangeDimensions(Level oldLevel,Level newLevel){return false;}
        @Override protected boolean shouldDropLoot(){return false;}
        @Override public boolean shouldDropExperience(){return false;}
        @Override protected void dropEquipment(){}
        @Override public boolean isAlliedTo(Entity target){return owner!=null && BoneGuardEvents.allied(owner,target) || super.isAlliedTo(target);}
        @Override public boolean canAttack(LivingEntity target){return owner!=null && BoneGuardEvents.enemy(owner,target) && super.canAttack(target);}
        @Override public ItemStack getProjectile(ItemStack weapon){return weapon.getItem() instanceof ProjectileWeaponItem?new ItemStack(Items.ARROW):super.getProjectile(weapon);}
        @Override protected void hurtArmor(DamageSource source,float damage){doHurtEquipment(source,damage,EquipmentSlot.FEET,EquipmentSlot.LEGS,EquipmentSlot.CHEST,EquipmentSlot.HEAD);NecromancerEquipment.changed(this);}
        @Override protected void hurtHelmet(DamageSource source,float damage){doHurtEquipment(source,damage,EquipmentSlot.HEAD);NecromancerEquipment.changed(this);}
        @Override public boolean doHurtTarget(Entity target){boolean hit=super.doHurtTarget(target);if(hit)NecromancerEquipment.wearWeapon(this);return hit;}
    }

    public static final class GuardDrowned extends Drowned implements BoneGuardEvents.FriendlyGuard {
        private final ServerPlayer owner;
        GuardDrowned(Level level,ServerPlayer owner){super(EntityType.DROWNED,level);this.owner=owner;}
        @Override public ServerPlayer owner(){return owner;}
        @Override protected boolean isSunBurnTick(){return false;}
        @Override protected boolean shouldDespawnInPeaceful(){return false;}
        @Override public boolean shouldBeSaved(){return false;}
        @Override public boolean canUsePortal(boolean allowPassengers){return false;}
        @Override public boolean canChangeDimensions(Level oldLevel,Level newLevel){return false;}
        @Override protected boolean shouldDropLoot(){return false;}
        @Override public boolean shouldDropExperience(){return false;}
        @Override protected void dropEquipment(){}
        @Override public boolean isAlliedTo(Entity target){return owner!=null && BoneGuardEvents.allied(owner,target) || super.isAlliedTo(target);}
        @Override public boolean canAttack(LivingEntity target){return owner!=null && BoneGuardEvents.enemy(owner,target) && super.canAttack(target);}
        @Override public ItemStack getProjectile(ItemStack weapon){return weapon.getItem() instanceof ProjectileWeaponItem?new ItemStack(Items.ARROW):super.getProjectile(weapon);}
        @Override protected void hurtArmor(DamageSource source,float damage){doHurtEquipment(source,damage,EquipmentSlot.FEET,EquipmentSlot.LEGS,EquipmentSlot.CHEST,EquipmentSlot.HEAD);NecromancerEquipment.changed(this);}
        @Override protected void hurtHelmet(DamageSource source,float damage){doHurtEquipment(source,damage,EquipmentSlot.HEAD);NecromancerEquipment.changed(this);}
        @Override public boolean doHurtTarget(Entity target){boolean hit=super.doHurtTarget(target);if(hit)NecromancerEquipment.wearWeapon(this);return hit;}
    }

    public static final class GuardZombifiedPiglin extends ZombifiedPiglin implements BoneGuardEvents.FriendlyGuard {
        private final ServerPlayer owner;
        GuardZombifiedPiglin(Level level,ServerPlayer owner){super(EntityType.ZOMBIFIED_PIGLIN,level);this.owner=owner;setItemSlot(EquipmentSlot.MAINHAND,new ItemStack(Items.GOLDEN_SWORD));}
        @Override public ServerPlayer owner(){return owner;}
        @Override protected boolean isSunBurnTick(){return false;}
        @Override protected boolean shouldDespawnInPeaceful(){return false;}
        @Override public boolean shouldBeSaved(){return false;}
        @Override public boolean canUsePortal(boolean allowPassengers){return false;}
        @Override public boolean canChangeDimensions(Level oldLevel,Level newLevel){return false;}
        @Override protected boolean shouldDropLoot(){return false;}
        @Override public boolean shouldDropExperience(){return false;}
        @Override protected void dropEquipment(){}
        @Override public boolean isAlliedTo(Entity target){return owner!=null && BoneGuardEvents.allied(owner,target) || super.isAlliedTo(target);}
        @Override public boolean canAttack(LivingEntity target){return owner!=null && BoneGuardEvents.enemy(owner,target) && super.canAttack(target);}
        @Override public ItemStack getProjectile(ItemStack weapon){return weapon.getItem() instanceof ProjectileWeaponItem?new ItemStack(Items.ARROW):super.getProjectile(weapon);}
        @Override protected void hurtArmor(DamageSource source,float damage){doHurtEquipment(source,damage,EquipmentSlot.FEET,EquipmentSlot.LEGS,EquipmentSlot.CHEST,EquipmentSlot.HEAD);NecromancerEquipment.changed(this);}
        @Override protected void hurtHelmet(DamageSource source,float damage){doHurtEquipment(source,damage,EquipmentSlot.HEAD);NecromancerEquipment.changed(this);}
        @Override public boolean doHurtTarget(Entity target){boolean hit=super.doHurtTarget(target);if(hit)NecromancerEquipment.wearWeapon(this);return hit;}
    }

    public static final class GuardPhantom extends Phantom implements BoneGuardEvents.FriendlyGuard {
        private final ServerPlayer owner;
        GuardPhantom(Level level,ServerPlayer owner){super(EntityType.PHANTOM,level);this.owner=owner;}
        @Override public ServerPlayer owner(){return owner;}
        @Override protected boolean isSunBurnTick(){return false;}
        @Override protected boolean shouldDespawnInPeaceful(){return false;}
        @Override public boolean shouldBeSaved(){return false;}
        @Override public boolean canUsePortal(boolean allowPassengers){return false;}
        @Override public boolean canChangeDimensions(Level oldLevel,Level newLevel){return false;}
        @Override protected boolean shouldDropLoot(){return false;}
        @Override public boolean shouldDropExperience(){return false;}
        @Override protected void dropEquipment(){}
        @Override public boolean isAlliedTo(Entity target){return owner!=null && BoneGuardEvents.allied(owner,target) || super.isAlliedTo(target);}
        @Override public boolean canAttack(LivingEntity target){return owner!=null && BoneGuardEvents.enemy(owner,target) && super.canAttack(target);}
        @Override public ItemStack getProjectile(ItemStack weapon){return weapon.getItem() instanceof ProjectileWeaponItem?new ItemStack(Items.ARROW):super.getProjectile(weapon);}
        @Override protected void hurtArmor(DamageSource source,float damage){doHurtEquipment(source,damage,EquipmentSlot.FEET,EquipmentSlot.LEGS,EquipmentSlot.CHEST,EquipmentSlot.HEAD);NecromancerEquipment.changed(this);}
        @Override protected void hurtHelmet(DamageSource source,float damage){doHurtEquipment(source,damage,EquipmentSlot.HEAD);NecromancerEquipment.changed(this);}
        @Override public boolean doHurtTarget(Entity target){boolean hit=super.doHurtTarget(target);if(hit)NecromancerEquipment.wearWeapon(this);return hit;}
    }

    public static final class GuardHorse extends SkeletonHorse implements BoneGuardEvents.FriendlyGuard {
        private final ServerPlayer owner;
        GuardHorse(Level level,ServerPlayer owner){super(EntityType.SKELETON_HORSE,level);this.owner=owner;}
        @Override public ServerPlayer owner(){return owner;}
        @Override protected boolean isSunBurnTick(){return false;}
        @Override protected boolean shouldDespawnInPeaceful(){return false;}
        @Override public boolean shouldBeSaved(){return false;}
        @Override public boolean canUsePortal(boolean allowPassengers){return false;}
        @Override public boolean canChangeDimensions(Level oldLevel,Level newLevel){return false;}
        @Override protected boolean shouldDropLoot(){return false;}
        @Override public boolean shouldDropExperience(){return false;}
        @Override protected void dropEquipment(){}
        @Override public boolean isAlliedTo(Entity target){return owner!=null && BoneGuardEvents.allied(owner,target) || super.isAlliedTo(target);}
        @Override public boolean canAttack(LivingEntity target){return owner!=null && BoneGuardEvents.enemy(owner,target) && super.canAttack(target);}
        @Override public ItemStack getProjectile(ItemStack weapon){return weapon.getItem() instanceof ProjectileWeaponItem?new ItemStack(Items.ARROW):super.getProjectile(weapon);}
        @Override protected void hurtArmor(DamageSource source,float damage){doHurtEquipment(source,damage,EquipmentSlot.FEET,EquipmentSlot.LEGS,EquipmentSlot.CHEST,EquipmentSlot.HEAD);NecromancerEquipment.changed(this);}
        @Override protected void hurtHelmet(DamageSource source,float damage){doHurtEquipment(source,damage,EquipmentSlot.HEAD);NecromancerEquipment.changed(this);}
        @Override public boolean doHurtTarget(Entity target){boolean hit=super.doHurtTarget(target);if(hit)NecromancerEquipment.wearWeapon(this);return hit;}
    }

    public static final class GuardZoglin extends Zoglin implements BoneGuardEvents.FriendlyGuard {
        private final ServerPlayer owner;
        GuardZoglin(Level level,ServerPlayer owner){super(EntityType.ZOGLIN,level);this.owner=owner;}
        @Override public ServerPlayer owner(){return owner;}
        @Override protected boolean isSunBurnTick(){return false;}
        @Override protected boolean shouldDespawnInPeaceful(){return false;}
        @Override public boolean shouldBeSaved(){return false;}
        @Override public boolean canUsePortal(boolean allowPassengers){return false;}
        @Override public boolean canChangeDimensions(Level oldLevel,Level newLevel){return false;}
        @Override protected boolean shouldDropLoot(){return false;}
        @Override public boolean shouldDropExperience(){return false;}
        @Override protected void dropEquipment(){}
        @Override public boolean isAlliedTo(Entity target){return owner!=null && BoneGuardEvents.allied(owner,target) || super.isAlliedTo(target);}
        @Override public boolean canAttack(LivingEntity target){return owner!=null && BoneGuardEvents.enemy(owner,target) && super.canAttack(target);}
        @Override public ItemStack getProjectile(ItemStack weapon){return weapon.getItem() instanceof ProjectileWeaponItem?new ItemStack(Items.ARROW):super.getProjectile(weapon);}
        @Override protected void hurtArmor(DamageSource source,float damage){doHurtEquipment(source,damage,EquipmentSlot.FEET,EquipmentSlot.LEGS,EquipmentSlot.CHEST,EquipmentSlot.HEAD);NecromancerEquipment.changed(this);}
        @Override protected void hurtHelmet(DamageSource source,float damage){doHurtEquipment(source,damage,EquipmentSlot.HEAD);NecromancerEquipment.changed(this);}
        @Override public boolean doHurtTarget(Entity target){boolean hit=!getMainHandItem().isEmpty()?NecromancerCombat.weaponStrike(this,target):super.doHurtTarget(target);if(hit)NecromancerEquipment.wearWeapon(this);return hit;}
    }
}
