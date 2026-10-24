package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.AdditionalAbilities;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class MagmaTossEvents {
    private record Flight(ServerPlayer owner,Snowball rock,int rank,float damage) {}
    private static final Map<UUID,Flight> FLIGHTS=new HashMap<>();
    private MagmaTossEvents() {}
    private static final class Rock extends Snowball {
        Rock(ServerPlayer owner){super(owner.level(),owner);setItem(new ItemStack(Items.MAGMA_BLOCK));}
        @Override public boolean shouldBeSaved(){return false;}
        @Override public boolean canUsePortal(boolean passengers){return false;}
        @Override public boolean canChangeDimensions(net.minecraft.world.level.Level oldLevel,net.minecraft.world.level.Level newLevel){return false;}
        @Override public void remove(net.minecraft.world.entity.Entity.RemovalReason reason){FLIGHTS.remove(getUUID());super.remove(reason);}
    }
    public static boolean activate(ServerPlayer player,ItemStack stack){
        int rank=Augmentations.level(stack,AdditionalAbilities.MAGMA_TOSS);
        if(rank<=0 || stack!=player.getMainHandItem() || !player.isAlive() || player.isSpectator() || WeaponControlLocks.frozen(player))return false;
        var rock=new Rock(player);rock.shootFromRotation(player,player.getXRot(),player.getYRot(),0,1.4F,0);
        if(!player.level().addFreshEntity(rock))return false;
        FLIGHTS.put(rock.getUUID(),new Flight(player,rock,rank,(float)player.getAttributeValue(Attributes.ATTACK_DAMAGE)));return true;
    }
    public static boolean active(ServerPlayer player){
        if(!player.isAlive() || player.isSpectator()){cancel(player.getUUID());return false;}
        return FLIGHTS.values().stream().anyMatch(flight->flight.owner()==player && flight.rock().isAlive() && flight.rock().level()==player.level());
    }
    public static void cancel(UUID owner){
        for(var flight:java.util.List.copyOf(FLIGHTS.values()))if(flight.owner().getUUID().equals(owner)){FLIGHTS.remove(flight.rock().getUUID());flight.rock().discard();}
    }
    @SubscribeEvent
    public static void impact(ProjectileImpactEvent event) {
        var flight=FLIGHTS.get(event.getProjectile().getUUID());if(flight==null)return;
        var rock=flight.rock();var player=flight.owner();event.setCanceled(true);
        if(!player.isAlive() || rock.level()!=player.level()){rock.discard();return;}
        var point=event.getRayTraceResult().getLocation().subtract(rock.getDeltaMovement().normalize().scale(.05));
        double radius=1.5+.5*flight.rank();
        for(var target:player.level().getEntitiesOfClass(LivingEntity.class,new AABB(point,point).inflate(radius),
                entity->WeaponAbilityTargets.enemy(player,entity))) {
            if(target.getBoundingBox().getCenter().distanceToSqr(point)>radius*radius) continue;
            var obstruction=player.level().clip(new ClipContext(point,target.getBoundingBox().getCenter(),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,player));
            if(obstruction.getType()!=HitResult.Type.MISS) continue;
            if(target.hurt(player.damageSources().indirectMagic(player,player),flight.damage()))
                target.setRemainingFireTicks(Math.max(target.getRemainingFireTicks(),(2+flight.rank())*20));
        }
        player.serverLevel().sendParticles(ParticleTypes.FLAME,point.x,point.y,point.z,45,radius*.4,radius*.4,radius*.4,.06);
        player.serverLevel().sendParticles(ParticleTypes.EXPLOSION,point.x,point.y,point.z,1,0,0,0,0);
        player.serverLevel().playSound(null,point.x,point.y,point.z,net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE,net.minecraft.sounds.SoundSource.PLAYERS,.6F,1.05F);
        rock.discard();
    }
    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post event) {
        if(event.getEntity() instanceof ServerPlayer player){
            if(!player.isAlive() || player.isSpectator()){cancel(player.getUUID());return;}
            for(var flight:java.util.List.copyOf(FLIGHTS.values()))if(flight.owner()==player && flight.rock().isAlive()){
                if(flight.rock().level()!=player.level()){flight.rock().discard();continue;}
                var rock=flight.rock();player.serverLevel().sendParticles(ParticleTypes.FLAME,rock.getX(),rock.getY(),rock.getZ(),2,.1,.1,.1,0);
            }
        }
    }
    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {cancel(event.getEntity().getUUID());}
    @SubscribeEvent public static void dimension(PlayerEvent.PlayerChangedDimensionEvent event){cancel(event.getEntity().getUUID());}
}
