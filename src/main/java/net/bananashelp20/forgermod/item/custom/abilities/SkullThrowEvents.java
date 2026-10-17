package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.AdditionalAbilities;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.augmentation.JadeAugments;
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
    public static final double MAX_DISTANCE=500;
    public static final double LAUNCH_SPEED=3.5;
    private static final net.minecraft.server.level.TicketType<UUID> FLIGHT_TICKET=
            net.minecraft.server.level.TicketType.create("forgermod_skull_throw",UUID::compareTo,40);
    private record Flight(ItemStack stack,int selected,WitherSkull rock,int rank,float damage) {}
    private static final Map<UUID,Flight> FLIGHTS=new HashMap<>();
    private SkullThrowEvents() {}
    public static boolean activate(ServerPlayer player,ItemStack stack) {
        if(active(player)) return false;
        int rank=Augmentations.level(stack,AdditionalAbilities.SKULL_THROW);
        var rock=new SafeSkull(player.level(),player);rock.setPos(player.getEyePosition().add(0,-.1,0));
        // Ordinary skull inertia keeps native server/client physics matched and avoids blue-skull braking.
        rock.setDeltaMovement(player.getLookAngle().scale(LAUNCH_SPEED));rock.hasImpulse=true;
        if(!player.level().addFreshEntity(rock)) return false;
        FLIGHTS.put(player.getUUID(),new Flight(stack,player.getInventory().selected,rock,
                rank,(float)player.getAttributeValue(Attributes.ATTACK_DAMAGE)));
        return true;
    }
    public static boolean active(ServerPlayer player) {
        var flight=FLIGHTS.get(player.getUUID());if(flight==null) return false;
        if(!player.isAlive() || player.isSpectator() || player.getMainHandItem()!=flight.stack()
                || player.getInventory().selected!=flight.selected() || player.level()!=flight.rock().level()
                || !flight.rock().isAlive()
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
        var point=event.getRayTraceResult().getLocation().subtract(rock.getDeltaMovement().normalize().scale(.05));
        burst(player,rock,point);
    }
    private static void burst(ServerPlayer player,WitherSkull rock,net.minecraft.world.phys.Vec3 point) {
        var flight=FLIGHTS.get(player.getUUID());
        if(flight==null || flight.rock()!=rock || !active(player)) return;
        double radius=1.5+.5*flight.rank();
        for(var target:player.level().getEntitiesOfClass(LivingEntity.class,new AABB(point,point).inflate(radius),
                entity->WeaponAbilityTargets.enemy(player,entity))) {
            if(target.getBoundingBox().getCenter().distanceToSqr(point)>radius*radius) continue;
            var obstruction=player.level().clip(new ClipContext(point,target.getBoundingBox().getCenter(),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,player));
            if(obstruction.getType()!=HitResult.Type.MISS) continue;
            if(target.hurt(player.damageSources().indirectMagic(player,player),flight.damage()))
                target.addEffect(JadeAugments.reinforce(flight.stack(),AdditionalAbilities.SKULL_THROW,
                        new MobEffectInstance(MobEffects.WITHER,100,0)),player);
        }
        player.serverLevel().sendParticles(ParticleTypes.SOUL,point.x,point.y,point.z,45,radius*.4,radius*.4,radius*.4,.06);
        player.serverLevel().sendParticles(new net.minecraft.core.particles.BlockParticleOption(ParticleTypes.BLOCK,
                net.minecraft.world.level.block.Blocks.BONE_BLOCK.defaultBlockState()),point.x,point.y,point.z,36,.18,.18,.18,.18);
        player.serverLevel().sendParticles(ParticleTypes.EXPLOSION,point.x,point.y,point.z,1,0,0,0,0);
        player.serverLevel().playSound(null,point.x,point.y,point.z,net.minecraft.sounds.SoundEvents.SKELETON_DEATH,
                net.minecraft.sounds.SoundSource.PLAYERS,1, .8F);
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
        private double traveled;
        private final java.util.Set<net.minecraft.world.level.ChunkPos> tickets=new java.util.HashSet<>();
        private SafeSkull(Level level,ServerPlayer owner) {super(EntityType.WITHER_SKULL,level);this.owner=owner;setOwner(owner);}
        @Override public boolean shouldBeSaved() {return false;}
        // Portal cloning by the vanilla entity type would lose the safe impact override.
        @Override public boolean canUsePortal(boolean allowPassengers) {return false;}
        @Override public boolean canChangeDimensions(Level oldLevel,Level newLevel) {return false;}
        @Override protected boolean canHitEntity(Entity entity) {return entity!=owner && super.canHitEntity(entity);}
        @Override public void tick() {
            double remaining=MAX_DISTANCE-traveled;
            if(remaining<=.000001) {discard();return;}
            var velocity=getDeltaMovement();
            if(velocity.length()>remaining) setDeltaMovement(velocity.normalize().scale(remaining));
            if(level() instanceof net.minecraft.server.level.ServerLevel server) {
                // Only the current/next flight chunks are ticketed; never retain the traversed corridor.
                var next=new java.util.HashSet<net.minecraft.world.level.ChunkPos>();
                next.add(chunkPosition());
                next.add(new net.minecraft.world.level.ChunkPos(net.minecraft.core.BlockPos.containing(position().add(getDeltaMovement()))));
                var chunks=server.getChunkSource();
                for(var chunk:next) {
                    chunks.addRegionTicket(FLIGHT_TICKET,chunk,3,getUUID());
                    if(!tickets.contains(chunk)) server.getChunk(chunk.x,chunk.z);
                }
                for(var chunk:tickets) if(!next.contains(chunk)) chunks.removeRegionTicket(FLIGHT_TICKET,chunk,3,getUUID());
                tickets.clear();tickets.addAll(next);
            }
            var previous=position();super.tick();
            traveled+=position().distanceTo(previous);
            if(traveled>=MAX_DISTANCE-.000001) discard();
        }
        @Override public void remove(Entity.RemovalReason reason) {
            if(level() instanceof net.minecraft.server.level.ServerLevel server)
                for(var chunk:tickets) server.getChunkSource().removeRegionTicket(FLIGHT_TICKET,chunk,3,getUUID());
            tickets.clear();super.remove(reason);
        }
        // Never run vanilla wither damage, healing or terrain-changing explosion logic.
        @Override protected void onHit(HitResult result) {
            burst(owner,this,result.getLocation().subtract(getDeltaMovement().normalize().scale(.05)));
            discard();
        }
    }
    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {cancel(event.getEntity().getUUID());}
}
