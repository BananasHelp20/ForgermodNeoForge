package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.AdditionalAbilities;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.augmentation.JadeAugments;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class LullabyEvents {
    private record Sleep(ItemStack stack,int selected,LivingEntity target,long expires) {}
    private static final Map<UUID,Sleep> SLEEP=new HashMap<>();
    private LullabyEvents() {}
    private static LivingEntity aimedTarget(ServerPlayer player,double range) {
        var start=player.getEyePosition();var end=start.add(player.getLookAngle().scale(range));
        var wall=player.level().clip(new net.minecraft.world.level.ClipContext(start,end,
                net.minecraft.world.level.ClipContext.Block.COLLIDER,net.minecraft.world.level.ClipContext.Fluid.NONE,player));
        if(wall.getType()!=net.minecraft.world.phys.HitResult.Type.MISS) end=wall.getLocation();
        var hit=net.minecraft.world.entity.projectile.ProjectileUtil.getEntityHitResult(player,start,end,
                player.getBoundingBox().expandTowards(end.subtract(start)).inflate(1),
                entity->entity instanceof LivingEntity living && living!=player && living.isAlive() && !living.isRemoved() && !living.isSpectator(),start.distanceToSqr(end));
        return hit==null?null:(LivingEntity)hit.getEntity();
    }
    public static boolean activate(ServerPlayer player,ItemStack stack) {
        if(active(player)) return false;
        int rank=Augmentations.level(stack,AdditionalAbilities.LULLABY);
        if(rank<=0) return false;
        var target=aimedTarget(player,8+2*rank);
        if(target==null) return false;
        if(!(target instanceof net.minecraft.world.entity.player.Player)) {
            if(SLEEP.values().stream().anyMatch(s->s.target()==target)) return false;
            int duration=JadeAugments.duration(stack,AdditionalAbilities.LULLABY,60);
            SLEEP.put(player.getUUID(),new Sleep(stack,player.getInventory().selected,target,player.level().getGameTime()+duration));
            WeaponControlLocks.acquire(target,player.getUUID(),AdditionalAbilities.LULLABY,duration);
        } else target.addEffect(JadeAugments.reinforce(stack,AdditionalAbilities.LULLABY,
                new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,60,1)));
        player.serverLevel().sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD,target.getX(),target.getEyeY()+.3,target.getZ(),8,.3,.2,.3,0);
        return true;
    }
    public static boolean active(ServerPlayer player) {
        var sleep=SLEEP.get(player.getUUID());if(sleep==null) return false;
        if(!player.isAlive() || player.isSpectator() || player.getMainHandItem()!=sleep.stack()
            || player.getInventory().selected!=sleep.selected() || !sleep.target().isAlive() || sleep.target().isRemoved() || sleep.target().level()!=player.level()
            || player.level().getGameTime()>=sleep.expires() || !Augmentations.ids(sleep.stack(),true).contains(AdditionalAbilities.LULLABY)) {
            cancel(player.getUUID());return false;
        }
        return true;
    }
    public static void cancel(UUID owner) {
        var sleep=SLEEP.remove(owner);if(sleep==null) return;
        WeaponControlLocks.release(owner,AdditionalAbilities.LULLABY);
    }
    @SubscribeEvent
    public static void wake(LivingDamageEvent.Post event) {
        if(event.getNewDamage()<=0 && event.getReduction(net.neoforged.neoforge.common.damagesource.DamageContainer.Reduction.ABSORPTION)<=0) return;
        for(var owner:java.util.List.copyOf(SLEEP.keySet())) if(SLEEP.get(owner).target()==event.getEntity()) cancel(owner);
    }
    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post event) {
        if(event.getEntity() instanceof ServerPlayer player && active(player)) {
            // Shared server control locks maintain position and original flags.
        }
    }
    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {cancel(event.getEntity().getUUID());}
}
