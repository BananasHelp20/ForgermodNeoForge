package net.bananashelp20.forgermod.item.custom.abilities;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import java.util.EnumSet;

/** Native melee/skeleton AI stays intact; supplied ranged weapons also work on the other undead types. */
public final class NecromancerCombat {
    private NecromancerCombat(){}
    public static void configure(Mob mob){mob.goalSelector.addGoal(-2,new NecromancerPassiveCombat.Shield(mob));mob.goalSelector.addGoal(-1,new NecromancerPassiveCombat.Melee(mob));mob.goalSelector.addGoal(1,new Ranged(mob));mob.goalSelector.addGoal(3,new Follow(mob));}
    private static final class Follow extends Goal {
        final Mob mob;
        Follow(Mob mob){this.mob=mob;setFlags(EnumSet.of(Flag.MOVE));}
        @Override public boolean canUse(){return mob instanceof BoneGuardEvents.FriendlyGuard guard && guard.owner().isAlive() && guard.owner().level()==mob.level() && mob.getTarget()==null && !mob.isPassenger() && mob.distanceToSqr(guard.owner())>36;}
        @Override public boolean canContinueToUse(){return canUse();}
        @Override public void tick(){var owner=((BoneGuardEvents.FriendlyGuard)mob).owner();if(mob instanceof net.minecraft.world.entity.monster.Phantom)mob.getMoveControl().setWantedPosition(owner.getX(),owner.getY()+3,owner.getZ(),1.1);else mob.getNavigation().moveTo(owner,1.1);}
        @Override public void stop(){mob.getNavigation().stop();}
    }
    private static final class Ranged extends Goal {
        final Mob mob;int cooldown=20;
        Ranged(Mob mob){this.mob=mob;setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));}
        @Override public boolean canUse(){
            var item=mob.getMainHandItem().getItem();return mob.getTarget()!=null && mob.getTarget().isAlive()
                    && (item instanceof CrossbowItem || item instanceof TridentItem || item instanceof BowItem && !(mob instanceof AbstractSkeleton));
        }
        @Override public boolean canContinueToUse(){return canUse();}
        @Override public void tick(){
            var target=mob.getTarget();if(target==null)return;mob.getLookControl().setLookAt(target,30,30);
            if(mob.distanceToSqr(target)>225 || !mob.hasLineOfSight(target)){mob.getNavigation().moveTo(target,1);return;}
            mob.getNavigation().stop();if(--cooldown>0)return;var weapon=mob.getMainHandItem();var owner=((BoneGuardEvents.FriendlyGuard)mob).owner();
            if(weapon.getItem() instanceof CrossbowItem crossbow){
                crossbow.releaseUsing(weapon,mob.level(),mob,0);crossbow.performShooting(mob.level(),mob,InteractionHand.MAIN_HAND,weapon,3.15F,1,target);
                cooldown=Math.max(5,CrossbowItem.getChargeDuration(weapon,mob));NecromancerEquipment.changed(mob);
            }else{
                AbstractArrow arrow=weapon.getItem() instanceof TridentItem?new TemporaryTrident(mob,weapon.copy()):ProjectileUtil.getMobArrow(mob,new ItemStack(Items.ARROW),1,weapon);
                var delta=target.position().add(0,target.getBbHeight()*.333,0).subtract(arrow.position());
                arrow.shoot(delta.x,delta.y+Math.hypot(delta.x,delta.z)*.2,delta.z,1.6F,1);
                BoneGuardEvents.ownArrow(arrow,owner);mob.level().addFreshEntity(arrow);
                if(weapon.getItem() instanceof BowItem)NecromancerEquipment.wearBow(mob);else if(NecromancerEquipment.supplied(mob,EquipmentSlot.MAINHAND)){weapon.hurtAndBreak(1,mob,EquipmentSlot.MAINHAND);NecromancerEquipment.changed(mob);}
                cooldown=40;
            }
        }
    }
    private static final class TemporaryTrident extends ThrownTrident {
        TemporaryTrident(Mob owner,ItemStack stack){super(owner.level(),owner,stack);pickup=Pickup.DISALLOWED;}
        @Override public void playerTouch(Player player){}
        @Override protected boolean tryPickup(Player player){return false;}
        @Override public boolean shouldBeSaved(){return false;}
        @Override public boolean canUsePortal(boolean allowPassengers){return false;}
        @Override public boolean canChangeDimensions(Level oldLevel,Level newLevel){return false;}
        @Override public void tick(){if(tickCount>=200)discard();else super.tick();}
    }
    /** Zoglin's native unarmed attack bypasses weapon enchantments; equipped strikes use the native Mob formula. */
    public static boolean weaponStrike(Mob mob,Entity target){
        if(!(mob.level() instanceof ServerLevel level) || !(target instanceof LivingEntity living) || !BoneGuardEvents.enemy(((BoneGuardEvents.FriendlyGuard)mob).owner(),living))return false;
        var source=mob.damageSources().mobAttack(mob);var weapon=mob.getMainHandItem();
        float amount=EnchantmentHelper.modifyDamage(level,weapon,target,source,(float)mob.getAttributeValue(Attributes.ATTACK_DAMAGE));
        boolean hit=target.hurt(source,amount);
        if(hit){float knockback=EnchantmentHelper.modifyKnockback(level,weapon,target,source,(float)mob.getAttributeValue(Attributes.ATTACK_KNOCKBACK));if(knockback>0)living.knockback(knockback*.5,Math.sin(Math.toRadians(mob.getYRot())),-Math.cos(Math.toRadians(mob.getYRot())));EnchantmentHelper.doPostAttackEffects(level,target,source);mob.setLastHurtMob(target);}
        return hit;
    }
}
