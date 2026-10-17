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
import net.minecraft.world.phys.EntityHitResult;
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
    private record Flight(ItemStack stack,int selected,Snowball rock,long expires,int rank,float damage) {}
    private static final Map<UUID,Flight> FLIGHTS=new HashMap<>();
    private MagmaTossEvents() {}
    public static boolean activate(ServerPlayer player,ItemStack stack) {
        if(active(player)) return false;
        int rank=Augmentations.level(stack,AdditionalAbilities.MAGMA_TOSS);
        var rock=new Snowball(player.level(),player);rock.setItem(new ItemStack(Items.MAGMA_BLOCK));
        rock.shootFromRotation(player,player.getXRot(),player.getYRot(),0,1.4F,0);
        if(!player.level().addFreshEntity(rock)) return false;
        FLIGHTS.put(player.getUUID(),new Flight(stack,player.getInventory().selected,rock,
                player.level().getGameTime()+60,rank,(float)player.getAttributeValue(Attributes.ATTACK_DAMAGE)));
        return true;
    }
    public static boolean active(ServerPlayer player) {
        var flight=FLIGHTS.get(player.getUUID());if(flight==null) return false;
        if(!player.isAlive() || player.isSpectator() || player.getMainHandItem()!=flight.stack()
                || player.getInventory().selected!=flight.selected() || player.level()!=flight.rock().level()
                || !flight.rock().isAlive() || player.level().getGameTime()>=flight.expires()
                || !Augmentations.ids(flight.stack(),true).contains(AdditionalAbilities.MAGMA_TOSS)) {
            cancel(player.getUUID());return false;
        }
        return true;
    }
    public static void cancel(UUID owner) {var flight=FLIGHTS.remove(owner);if(flight!=null) flight.rock().discard();}
    @SubscribeEvent
    public static void impact(ProjectileImpactEvent event) {
        if(!(event.getProjectile() instanceof Snowball rock) || !(rock.getOwner() instanceof ServerPlayer player)) return;
        var flight=FLIGHTS.get(player.getUUID());if(flight==null || flight.rock()!=rock) return;
        event.setCanceled(true);
        if(!active(player)) return;
        if(event.getRayTraceResult() instanceof EntityHitResult hit
                && (!(hit.getEntity() instanceof LivingEntity living) || !WeaponAbilityTargets.enemy(player,living))) return;
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
        cancel(player.getUUID());
    }
    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post event) {
        if(event.getEntity() instanceof ServerPlayer player && active(player)) {
            var rock=FLIGHTS.get(player.getUUID()).rock();
            player.serverLevel().sendParticles(ParticleTypes.FLAME,rock.getX(),rock.getY(),rock.getZ(),2,.1,.1,.1,0);
        }
    }
    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {cancel(event.getEntity().getUUID());}
}
