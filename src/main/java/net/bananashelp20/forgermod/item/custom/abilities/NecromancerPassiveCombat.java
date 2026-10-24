package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.custom.*;
import net.bananashelp20.forgermod.item.custom.attacks.dagger.DaggerItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.*;
import net.minecraft.sounds.*;
import net.bananashelp20.forgermod.item.custom.attacks.axe.AxeItems;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.*;
import java.util.*;

/** Native mob hits with passive-only weapon hooks. No active-ability dispatcher is called. */
@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class NecromancerPassiveCombat {
    private record Attack(Mob mob,ItemStack weapon,boolean charged){}
    private static final ThreadLocal<Attack> CURRENT=new ThreadLocal<>();
    private static final Map<UUID,Long> LAST=new HashMap<>();
    private record Combo(ItemStack stack,long first,long last,int hits){}
    private record Slam(ItemStack stack,long due,BlockPos center,float damage){}
    private static final Map<UUID,Combo> COMBOS=new HashMap<>();
    private static final Map<UUID,Slam> SLAMS=new HashMap<>();
    private NecromancerPassiveCombat(){}
    public static int interval(Mob mob){return (int)Math.ceil(20/Math.max(.01,mob.getAttributeValue(Attributes.ATTACK_SPEED)));}
    public static ItemStack striking(Mob mob){var hit=CURRENT.get();return hit!=null && hit.mob()==mob?hit.weapon():mob.getMainHandItem();}
    public static float weaponDamage(Mob mob,ItemStack weapon){
        var attributes=new AttributeInstance(Attributes.ATTACK_DAMAGE,ignored->{});attributes.replaceFrom(mob.getAttribute(Attributes.ATTACK_DAMAGE));
        mob.getMainHandItem().forEachModifier(EquipmentSlot.MAINHAND,(attribute,modifier)->{if(attribute.equals(Attributes.ATTACK_DAMAGE))attributes.removeModifier(modifier.id());});
        weapon.forEachModifier(EquipmentSlot.MAINHAND,(attribute,modifier)->{if(attribute.equals(Attributes.ATTACK_DAMAGE))attributes.addOrUpdateTransientModifier(modifier);});return (float)attributes.getValue();
    }
    public static boolean attack(Mob mob,LivingEntity target){
        if(!(mob instanceof BoneGuardEvents.FriendlyGuard) || !Augmentations.eligible(mob.getMainHandItem()) || !WeaponAbilityTargets.enemy(mob,target) || WeaponControlLocks.frozen(mob) || !mob.isWithinMeleeAttackRange(target) || !mob.hasLineOfSight(target))return false;
        long now=mob.level().getGameTime();boolean charged=!LAST.containsKey(mob.getUUID()) || now-LAST.get(mob.getUUID())>=interval(mob);
        LAST.put(mob.getUUID(),now);var previous=CURRENT.get();var main=mob.getMainHandItem();
        CURRENT.set(new Attack(mob,main,charged));if(charged)AftershockEvents.prepare(mob);
        try{
            mob.swing(InteractionHand.MAIN_HAND);boolean landed=mob.doHurtTarget(target);
            if(mob.isAlive() && target.isAlive() && DaggerItems.hasMatchingDaggers(mob)){
                var off=mob.getOffhandItem();CURRENT.set(new Attack(mob,off,charged));int immunity=target.invulnerableTime;if(landed)target.invulnerableTime=0;
                var source=new DamageSource(mob.registryAccess().registryOrThrow(net.minecraft.core.registries.Registries.DAMAGE_TYPE).getHolderOrThrow(DamageTypes.MOB_ATTACK),mob){@Override public ItemStack getWeaponItem(){return off;}};
                float damage=EnchantmentHelper.modifyDamage(PassiveActors.level(mob),off,target,source,weaponDamage(mob,off));
                mob.swing(InteractionHand.OFF_HAND);
                if(target.hurt(source,damage)){
                    EnchantmentHelper.doPostAttackEffects(PassiveActors.level(mob),target,source);off.hurtAndBreak(1,mob,EquipmentSlot.OFFHAND);NecromancerEquipment.changed(mob);landed=true;
                }else target.invulnerableTime=Math.max(immunity,target.invulnerableTime);
            }
            if(charged && mob.isAlive() && mob.getMainHandItem()==main && AxeItems.canVerticalStrike(main))queueSlam(mob,main,now);
            return landed;
        }finally{if(previous==null)CURRENT.remove();else CURRENT.set(previous);}
    }
    @SubscribeEvent public static void critical(LivingIncomingDamageEvent event){
        var hit=CURRENT.get();if(!event.isCanceled() && event.getAmount()>0 && hit!=null && hit.charged() && event.getSource().getEntity()==hit.mob() && PassiveActors.melee(event.getSource()) && ResonanceCritEvents.qualifies(hit.mob(),event.getEntity())){
            event.setAmount(event.getAmount()*1.5F);var center=event.getEntity().getBoundingBox().getCenter();
            PassiveActors.level(hit.mob()).sendParticles(ParticleTypes.CRIT,center.x,center.y,center.z,8,.2,.3,.2,.05);
        }
    }
    @SubscribeEvent public static void material(LivingDamageEvent.Post event){
        if(event.getNewDamage()<=0 || !PassiveActors.melee(event.getSource()) || !(PassiveActors.attacker(event.getSource()) instanceof Mob mob) || !(mob instanceof BoneGuardEvents.FriendlyGuard))return;
        var stack=striking(mob);if(stack.getItem() instanceof SwordItemWithEffect weapon && weapon.hasMaterialEffect()){
            weapon.applyMaterialEffect(event.getEntity(),stack);
            if(weapon.isDagger() && weapon instanceof ElectriumWeapon)weapon.onDaggerHit(event.getEntity(),mob);
            NecromancerEquipment.changed(mob);
        }
    }
    private static BlockPos groundAhead(Mob mob){
        var direction=mob.getLookAngle().multiply(1,0,1).normalize();if(direction.lengthSqr()<.0001)return null;var point=mob.position().add(direction.scale(2));
        for(int y=mob.blockPosition().getY()+1;y>=mob.blockPosition().getY()-4;y--){var pos=BlockPos.containing(point.x,y,point.z);if(!mob.level().getBlockState(pos).getCollisionShape(mob.level(),pos).isEmpty())return pos.above();}return null;
    }
    private static void queueSlam(Mob mob,ItemStack stack,long now){
        var old=COMBOS.get(mob.getUUID());int hits=old!=null && old.stack()==stack && now-old.first()<=200 && now-old.last()>=interval(mob)?old.hits()+1:1;
        COMBOS.put(mob.getUUID(),new Combo(stack,hits==1?now:old.first(),now,hits));
        if(hits<3)return;COMBOS.remove(mob.getUUID());var ground=groundAhead(mob);if(ground!=null)SLAMS.put(mob.getUUID(),new Slam(stack,now+3,ground,(float)mob.getAttributeValue(Attributes.ATTACK_DAMAGE)));
    }
    @SubscribeEvent public static void slam(net.neoforged.neoforge.event.tick.EntityTickEvent.Post event){
        if(!(event.getEntity() instanceof Mob mob) || !(mob instanceof BoneGuardEvents.FriendlyGuard) || mob.level().isClientSide)return;
        var combo=COMBOS.get(mob.getUUID());if(combo!=null && (!mob.isAlive() || mob.getMainHandItem()!=combo.stack() || !AxeItems.canVerticalStrike(mob.getMainHandItem()) || mob.level().getGameTime()-combo.first()>200))COMBOS.remove(mob.getUUID());
        var slam=SLAMS.get(mob.getUUID());if(slam==null)return;
        if(!mob.isAlive() || mob.getMainHandItem()!=slam.stack() || !mob.getOffhandItem().isEmpty() || WeaponControlLocks.frozen(mob)){SLAMS.remove(mob.getUUID());return;}
        if(mob.level().getGameTime()<slam.due())return;SLAMS.remove(mob.getUUID());
        EchoSalvageEvents.vertical(mob,()->landSlam(mob,slam,null,EchoSalvageEvents.bonus(mob)));
    }
    public static void echoVerticalStrike(Mob mob,ItemStack stack,LivingEntity primary,float bonus){
        COMBOS.remove(mob.getUUID());SLAMS.remove(mob.getUUID());
        var slam=new Slam(stack,mob.level().getGameTime(),primary.blockPosition(),(float)mob.getAttributeValue(Attributes.ATTACK_DAMAGE));
        EchoSalvageEvents.vertical(mob,()->landSlam(mob,slam,primary,bonus));
    }
    private static void landSlam(Mob mob,Slam slam,LivingEntity skipped,float echo){
        var center=slam.center();var area=new AABB(center.getX()-1,center.getY(),center.getZ()-1,center.getX()+2,center.getY()+2.5,center.getZ()+2);
        boolean hitAnything=skipped!=null,attemptedHit=skipped!=null;var previous=CURRENT.get();CURRENT.set(new Attack(mob,slam.stack(),true));AftershockEvents.prepare(mob);
        try{for(var target:mob.level().getEntitiesOfClass(LivingEntity.class,area,t->t!=skipped && t.isAlive() && !t.isSpectator() && PassiveActors.canAffect(mob,t))){
            if(!mob.hasLineOfSight(target))continue;attemptedHit=true;int immunity=target.invulnerableTime;target.invulnerableTime=0;
            if(target.hurt(mob.damageSources().mobAttack(mob),slam.damage()+echo)){hitAnything=true;target.knockback(.5,center.getX()+.5-target.getX(),center.getZ()+.5-target.getZ());}
            else target.invulnerableTime=immunity;
        }}finally{if(previous==null)CURRENT.remove();else CURRENT.set(previous);}
        EchoSalvageEvents.onImpact(mob,hitAnything,attemptedHit,!mob.level().getBlockState(center.below()).getCollisionShape(mob.level(),center.below()).isEmpty(),slam.damage());
        mob.swing(InteractionHand.MAIN_HAND);for(int x=-1;x<=1;x++)for(int z=-1;z<=1;z++)PassiveActors.level(mob).sendParticles(ParticleTypes.CLOUD,center.getX()+x+.5,center.getY()+.1,center.getZ()+z+.5,5,.25,.05,.25,.02);
        PassiveActors.level(mob).playSound(null,center,SoundEvents.IRON_GOLEM_ATTACK,SoundSource.HOSTILE,1,.7F);
    }
    @SubscribeEvent public static void leave(net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent event){LAST.remove(event.getEntity().getUUID());COMBOS.remove(event.getEntity().getUUID());SLAMS.remove(event.getEntity().getUUID());}
    public static final class Shield extends Goal {
        private final Mob mob;private Entity threat;private ItemStack shield;private int remaining,ready;
        public Shield(Mob mob){this.mob=mob;setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));}
        @Override public boolean requiresUpdateEveryTick(){return true;}
        @Override public boolean canUse(){
            if(!(mob instanceof BoneGuardEvents.FriendlyGuard) || !mob.isAlive() || mob.tickCount<ready || WeaponControlLocks.frozen(mob)
                    || !mob.getOffhandItem().canPerformAction(net.neoforged.neoforge.common.ItemAbilities.SHIELD_BLOCK))return false;
            threat=null;
            for(var projectile:mob.level().getEntitiesOfClass(net.minecraft.world.entity.projectile.Projectile.class,mob.getBoundingBox().inflate(8))){
                if(projectile.getOwner() instanceof LivingEntity source && WeaponAbilityTargets.enemy(mob,source)
                        && mob.getBoundingBox().inflate(.5).clip(projectile.position(),projectile.position().add(projectile.getDeltaMovement().scale(10))).isPresent()){threat=projectile;break;}
            }
            if(threat==null && mob.getTarget()!=null && WeaponAbilityTargets.enemy(mob,mob.getTarget()) && mob.distanceToSqr(mob.getTarget())<=16 && mob.hasLineOfSight(mob.getTarget()))threat=mob.getTarget();
            return threat!=null;
        }
        @Override public void start(){remaining=12;shield=mob.getOffhandItem();mob.startUsingItem(InteractionHand.OFF_HAND);mob.getNavigation().stop();}
        @Override public boolean canContinueToUse(){return remaining>0 && mob.isAlive() && threat!=null && threat.isAlive() && mob.getOffhandItem()==shield && mob.isUsingItem() && mob.getUsedItemHand()==InteractionHand.OFF_HAND && !WeaponControlLocks.frozen(mob);}
        @Override public void tick(){remaining--;mob.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,threat.getEyePosition());mob.getNavigation().stop();}
        @Override public void stop(){if(mob.isUsingItem() && mob.getUsedItemHand()==InteractionHand.OFF_HAND)mob.stopUsingItem();ready=mob.tickCount+20;threat=null;}
    }
    public static final class Melee extends Goal {
        private final Mob mob;private int delay;
        public Melee(Mob mob){this.mob=mob;setFlags(EnumSet.of(Flag.MOVE,Flag.LOOK));}
        @Override public boolean requiresUpdateEveryTick(){return true;}
        @Override public boolean canUse(){return mob instanceof BoneGuardEvents.FriendlyGuard && Augmentations.eligible(mob.getMainHandItem()) && mob.getTarget()!=null && WeaponAbilityTargets.enemy(mob,mob.getTarget()) && !WeaponControlLocks.frozen(mob);}
        @Override public boolean canContinueToUse(){return canUse();}
        @Override public void tick(){
            if(delay>0)delay--;var target=mob.getTarget();if(target==null)return;
            mob.getLookControl().setLookAt(target,30,30);
            if(mob instanceof net.minecraft.world.entity.monster.Phantom)mob.getMoveControl().setWantedPosition(target.getX(),target.getY()+.5,target.getZ(),1);
            else mob.getNavigation().moveTo(target,1);
            if(delay==0 && (!LAST.containsKey(mob.getUUID()) || mob.level().getGameTime()-LAST.get(mob.getUUID())>=interval(mob)) && mob.isWithinMeleeAttackRange(target) && mob.hasLineOfSight(target)){attack(mob,target);delay=interval(mob);}
        }
        @Override public void stop(){mob.getNavigation().stop();}
    }
}
