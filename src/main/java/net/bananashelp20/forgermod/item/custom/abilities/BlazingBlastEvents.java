package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.*;
import net.bananashelp20.forgermod.augmentation.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.projectile.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import java.util.UUID;

/** Vanilla entity types render these transient projectiles; server overrides enforce the terrain option. */
@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class BlazingBlastEvents {
    private interface BurstProjectile {UUID burst();float multiplier();}
    private BlazingBlastEvents(){}
    public static boolean isBurstProjectile(Entity entity){return entity instanceof BurstProjectile;}
    public static void fire(ServerPlayer owner,ItemStack stack){
        int rank=Augmentations.level(stack,RecommendedAbilities.EXPLOSION);if(rank<5)return;
        UUID burst=UUID.randomUUID();float multiplier=(float)ReviewedRanks.blastProjectileMultiplier(rank);
        for(int i=0;i<4;i++)spawn(owner,new SafeLarge(owner,Vec3.directionFromRotation(0,owner.getYRot()+90*i),burst,multiplier),Vec3.directionFromRotation(0,owner.getYRot()+90*i));
        for(int i=0;i<8;i++)spawn(owner,new SafeSmall(owner,Vec3.directionFromRotation(0,owner.getYRot()+22.5F+45*i),burst,multiplier),Vec3.directionFromRotation(0,owner.getYRot()+22.5F+45*i));
    }
    private static void spawn(ServerPlayer owner,Fireball projectile,Vec3 direction){
        projectile.setPos(owner.getEyePosition().add(direction.scale(1.2)));projectile.setDeltaMovement(direction.scale(.5));owner.serverLevel().addFreshEntity(projectile);
    }
    private static boolean sibling(Entity first,Entity second){return first instanceof BurstProjectile a && second instanceof BurstProjectile b && a.burst().equals(b.burst());}
    @SubscribeEvent public static void damage(LivingIncomingDamageEvent event){
        if(event.getSource().getDirectEntity() instanceof BurstProjectile projectile)event.setAmount(event.getAmount()*projectile.multiplier());
    }
    private static final class SafeLarge extends LargeFireball implements BurstProjectile {
        private final UUID burst;private final float multiplier;
        SafeLarge(ServerPlayer owner,Vec3 direction,UUID burst,float multiplier){super(owner.level(),owner,direction,1);this.burst=burst;this.multiplier=multiplier;}
        @Override public UUID burst(){return burst;}
        @Override public float multiplier(){return multiplier;}
        @Override public boolean shouldBeSaved(){return false;}
        @Override public boolean canUsePortal(boolean allowPassengers){return false;}
        @Override public boolean canChangeDimensions(Level oldLevel,Level newLevel){return false;}
        @Override protected boolean canHitEntity(Entity entity){return !sibling(this,entity) && super.canHitEntity(entity);}
        @Override public boolean hurt(DamageSource source,float amount){return !sibling(this,source.getDirectEntity()) && super.hurt(source,amount);}
        @Override public void tick(){if(tickCount>=200)discard();else super.tick();}
        @Override protected void onHit(HitResult result){
            if(Config.EXPLOSION_TERRAIN.get()){super.onHit(result);return;}
            if(!level().isClientSide){
                if(result instanceof EntityHitResult entity)onHitEntity(entity);
                level().explode(this,getX(),getY(),getZ(),1,false,Level.ExplosionInteraction.NONE);discard();
            }
        }
    }
    private static final class SafeSmall extends SmallFireball implements BurstProjectile {
        private final UUID burst;private final float multiplier;
        SafeSmall(ServerPlayer owner,Vec3 direction,UUID burst,float multiplier){super(owner.level(),owner,direction);this.burst=burst;this.multiplier=multiplier;}
        @Override public UUID burst(){return burst;}
        @Override public float multiplier(){return multiplier;}
        @Override public boolean shouldBeSaved(){return false;}
        @Override public boolean canUsePortal(boolean allowPassengers){return false;}
        @Override public boolean canChangeDimensions(Level oldLevel,Level newLevel){return false;}
        @Override protected boolean canHitEntity(Entity entity){return !sibling(this,entity) && super.canHitEntity(entity);}
        @Override protected void onHitBlock(BlockHitResult result){if(Config.EXPLOSION_TERRAIN.get())super.onHitBlock(result);}
        @Override public void tick(){if(tickCount>=200)discard();else super.tick();}
    }
}
