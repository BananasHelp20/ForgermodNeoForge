package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.AdditionalAbilities;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class GravityWellEvents {
    private record Pull(ItemStack stack,int selected,long expires,net.minecraft.server.level.ServerLevel level,
                        net.minecraft.world.phys.Vec3 destination,double speed,Set<LivingEntity> awaiting) {}
    private static final Map<UUID,Pull> PULLS=new HashMap<>();
    private GravityWellEvents() {}
    public static boolean activate(ServerPlayer player,ItemStack stack) {
        if(active(player)) return false;
        int rank=Augmentations.level(stack,AdditionalAbilities.GRAVITY_WELL);
        if(rank<=0) return false;
        var targets=player.level().getEntitiesOfClass(LivingEntity.class,player.getBoundingBox().inflate(6),
                e->WeaponAbilityTargets.enemy(player,e) && player.distanceToSqr(e)<=36 && player.hasLineOfSight(e));
        if(targets.isEmpty()) return false;
        // Capture the point once: turning or moving must not drag the destination around.
        var forward=net.minecraft.world.phys.Vec3.directionFromRotation(0,player.getYRot());
        var destination=player.position().add(forward.scale(2));
        PULLS.put(player.getUUID(),new Pull(stack,player.getInventory().selected,player.level().getGameTime()+40,
                player.serverLevel(),destination,.5+.1*rank,new HashSet<>(targets)));
        advance(player);return true;
    }
    public static boolean active(ServerPlayer player) {
        var pull=PULLS.get(player.getUUID());if(pull==null) return false;
        if(!player.isAlive() || player.isSpectator() || player.getMainHandItem()!=pull.stack()
                || player.level()!=pull.level()
                || player.getInventory().selected!=pull.selected() || player.level().getGameTime()>=pull.expires()
                || !Augmentations.ids(pull.stack(),true).contains(AdditionalAbilities.GRAVITY_WELL)) {cancel(player.getUUID());return false;}
        return true;
    }
    public static void cancel(UUID owner) {PULLS.remove(owner);}
    private static void advance(ServerPlayer player) {
        if(!active(player)) return;
        var pull=PULLS.get(player.getUUID());
        pull.awaiting().removeIf(target->{
            if(!target.isAlive() || target.level()!=player.level() || !WeaponAbilityTargets.enemy(player,target) || !player.hasLineOfSight(target)) return true;
            var remaining=pull.destination().subtract(target.position());
            if(remaining.lengthSqr()>.16) {
                // Continue steering under ordinary collision physics, without overshooting the point.
                target.setDeltaMovement(remaining.normalize().scale(Math.min(pull.speed(),remaining.length())));
                target.hasImpulse=true;
                if(target instanceof ServerPlayer other) other.connection.send(new net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket(other));
                return false;
            }
            target.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
            target.hasImpulse=true;
            if(target instanceof ServerPlayer other) other.connection.send(new net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket(other));
            if(player.distanceToSqr(target)>player.entityInteractionRange()*player.entityInteractionRange()) return false;
            int immunity=target.invulnerableTime;
            target.invulnerableTime=0;
            float damage=(float)player.getAttributeValue(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
            if(target.hurt(player.damageSources().playerAttack(player),damage)) {
                var weapon=Augmentations.canonical(pull.stack());
                if(weapon!=null) weapon.applyMaterialEffect(target,pull.stack());
            } else target.invulnerableTime=immunity;
            player.swing(InteractionHand.MAIN_HAND);
            player.serverLevel().sendParticles(ParticleTypes.CLOUD,target.getX(),target.getY()+.2,target.getZ(),12,.5,.1,.5,.05);
            player.serverLevel().playSound(null,target.blockPosition(),SoundEvents.IRON_GOLEM_ATTACK,SoundSource.PLAYERS,.6F,.7F);
            return true;
        });
        if(pull.awaiting().isEmpty()) cancel(player.getUUID());
    }
    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post event) {if(event.getEntity() instanceof ServerPlayer player) advance(player);}
    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {cancel(event.getEntity().getUUID());}
}
