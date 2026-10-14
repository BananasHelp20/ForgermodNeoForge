package net.bananashelp20.forgermod.item.custom.abilities;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.particle.VoidPulseParticles;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import java.util.*;
@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class VoidPulseAbility {
    private record Pulse(ItemStack stack,int slot,ServerLevel level,int rank,long release) {}
    private static final Map<UUID,Pulse> PULSES=new HashMap<>();
    private VoidPulseAbility() {}
    public static boolean activate(ServerPlayer player,ItemStack stack) {
        int rank=Augmentations.level(stack,AdditionalAbilities.VOID_PULSE);
        if(rank<=0 || active(player))return false;
        PULSES.put(player.getUUID(),new Pulse(stack,player.getInventory().selected,player.serverLevel(),rank,player.level().getGameTime()+VoidPulseParticles.CHARGE_TICKS));
        double radius=3+rank;
        for(int i=0;i<64;i++) {
            double a=i*Math.PI/32,r=radius*(.8+.2*(i%3)/2.0);
            player.serverLevel().sendParticles(new VoidPulseParticles.Options(false,player.getId()),player.getX()+Math.cos(a)*r,player.getY()+.25+(i%4)*.45,player.getZ()+Math.sin(a)*r,0,0,0,0,1);
        }
        player.serverLevel().playSound(null,player.blockPosition(),SoundEvents.AMETHYST_BLOCK_CHIME,player.getSoundSource(),.6f,.5f);return true;
    }
    public static boolean active(ServerPlayer player) {
        var pulse=PULSES.get(player.getUUID());if(pulse==null)return false;
        if(!player.isAlive() || player.isSpectator() || player.getMainHandItem()!=pulse.stack || player.getInventory().selected!=pulse.slot || player.serverLevel()!=pulse.level || !Augmentations.ids(pulse.stack,true).contains(AdditionalAbilities.VOID_PULSE)){cancel(player.getUUID());return false;}
        if(player.level().getGameTime()>=pulse.release){PULSES.remove(player.getUUID());release(player,pulse.rank);return false;}return true;
    }
    public static void cancel(UUID owner){PULSES.remove(owner);}
    private static void release(ServerPlayer player,int rank) {
        double radius=3+rank,strength=.4+.2*rank;
        for(var target:player.level().getEntitiesOfClass(LivingEntity.class,player.getBoundingBox().inflate(radius),e->e.isAlive() && (e instanceof Mob || WeaponAbilityTargets.enemy(player,e)))) {
            if(player.distanceToSqr(target)>radius*radius || !player.hasLineOfSight(target))continue;
            double dx=target.getX()-player.getX(),dz=target.getZ()-player.getZ();
            if(dx*dx+dz*dz<.0001){dx=player.getLookAngle().x;dz=player.getLookAngle().z;if(dx*dx+dz*dz<.0001)dz=1;}
            double length=Math.sqrt(dx*dx+dz*dz);var v=target.getDeltaMovement();
            // Explicit impulse reaches passive and knockback-resistant mobs too, with ordinary collision physics.
            target.setDeltaMovement(v.x/2+dx/length*strength,target.onGround()?Math.min(.4,v.y/2+strength):v.y,v.z/2+dz/length*strength);target.hasImpulse=true;
            if(target instanceof ServerPlayer other)other.connection.send(new net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket(other));
        }
        Vec3 center=player.position().add(0,player.getBbHeight()*.5,0);
        for(int i=0;i<96;i++) {
            var velocity=net.bananashelp20.forgermod.particle.VoidPulseMotion.outward(i,radius);
            player.serverLevel().sendParticles(new VoidPulseParticles.Options(true,player.getId()),center.x,center.y,center.z,0,velocity.x,velocity.y,velocity.z,1);
        }
        player.serverLevel().sendParticles(ParticleTypes.FLASH,center.x,center.y,center.z,1,0,0,0,0);
        player.serverLevel().playSound(null,player.blockPosition(),SoundEvents.GENERIC_EXPLODE.value(),player.getSoundSource(),.6f,1.4f);
    }
    @SubscribeEvent public static void tick(PlayerTickEvent.Post event){if(event.getEntity() instanceof ServerPlayer player)active(player);}
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent event){cancel(event.getEntity().getUUID());}
}
