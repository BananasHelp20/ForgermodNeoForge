package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.AdditionalAbilities;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
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
public final class SkullThrowEvents {
    private record Flight(ItemStack stack,int selected,WitherSkull rock,long expires,int rank,float damage) {}
    private static final Map<UUID,Flight> FLIGHTS=new HashMap<>();
    private SkullThrowEvents() {}
    public static boolean activate(ServerPlayer player,ItemStack stack) {
        if(active(player)) return false;
        int rank=Augmentations.level(stack,AdditionalAbilities.SKULL_THROW);
        var rock=new SafeSkull(player.level(),player);rock.setPos(player.getEyePosition().add(0,-.1,0));
        rock.setDeltaMovement(player.getLookAngle().scale(.6));rock.hasImpulse=true;rock.setDangerous(true);
        if(!player.level().addFreshEntity(rock)) return false;
        FLIGHTS.put(player.getUUID(),new Flight(stack,player.getInventory().selected,rock,
                player.level().getGameTime()+100,rank,(float)player.getAttributeValue(Attributes.ATTACK_DAMAGE)));
        return true;
    }
    public static boolean active(ServerPlayer player) {
        var flight=FLIGHTS.get(player.getUUID());if(flight==null) return false;
        if(!player.isAlive() || player.isSpectator() || player.getMainHandItem()!=flight.stack()
                || player.getInventory().selected!=flight.selected() || player.level()!=flight.rock().level()
                || !flight.rock().isAlive() || player.level().getGameTime()>=flight.expires()
                || !Augmentations.ids(flight.stack(),true).contains(AdditionalAbilities.SKULL_THROW)) {
            cancel(player.getUUID());return false;
        }
        return true;
    }
    public static void cancel(UUID owner) {var flight=FLIGHTS.remove(owner);if(flight!=null) flight.rock().discard();}
    @SubscribeEvent
    public static void impact(ProjectileImpactEvent event) {
        if(!(event.getProjectile() instanceof WitherSkull rock) || !(rock.getOwner() instanceof ServerPlayer player)) return;
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
                target.addEffect(new MobEffectInstance(MobEffects.WITHER,100,0),player);
        }
        player.serverLevel().sendParticles(ParticleTypes.SOUL,point.x,point.y,point.z,45,radius*.4,radius*.4,radius*.4,.06);
        player.serverLevel().sendParticles(ParticleTypes.EXPLOSION,point.x,point.y,point.z,1,0,0,0,0);
        cancel(player.getUUID());
    }
    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post event) {
        if(event.getEntity() instanceof ServerPlayer player && active(player)) {
            var rock=FLIGHTS.get(player.getUUID()).rock();
            player.serverLevel().sendParticles(ParticleTypes.SOUL,rock.getX(),rock.getY(),rock.getZ(),2,.1,.1,.1,0);
        }
    }
    private static final class SafeSkull extends WitherSkull {
        private final ServerPlayer owner;
        private SafeSkull(Level level,ServerPlayer owner) {super(EntityType.WITHER_SKULL,level);this.owner=owner;setOwner(owner);}
        @Override public boolean shouldBeSaved() {return false;}
        @Override protected boolean canHitEntity(Entity entity) {return super.canHitEntity(entity) && entity instanceof LivingEntity living && WeaponAbilityTargets.enemy(owner,living);}
        // Never run vanilla wither damage, healing or terrain-changing explosion logic.
        @Override protected void onHit(HitResult result) {discard();}
    }
    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {cancel(event.getEntity().getUUID());}
}
