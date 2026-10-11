package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.AdditionalAbilities;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
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
    private record Sleep(ItemStack stack,int selected,Mob mob,boolean noAi,boolean noGravity,LivingEntity target,Vec3 position,long expires) {}
    private static final Map<UUID,Sleep> SLEEP=new HashMap<>();
    private LullabyEvents() {}
    public static boolean activate(ServerPlayer player,ItemStack stack) {
        if(active(player)) return false;
        int rank=Augmentations.level(stack,AdditionalAbilities.LULLABY);
        var target=WeaponAbilityTargets.aimedEnemy(player,8+2*rank);
        if(target==null) return false;
        if(target instanceof Mob mob) {
            if(SLEEP.values().stream().anyMatch(s->s.mob()==mob)) return false;
            SLEEP.put(player.getUUID(),new Sleep(stack,player.getInventory().selected,mob,mob.isNoAi(),mob.isNoGravity(),mob.getTarget(),mob.position(),player.level().getGameTime()+60));
            mob.setTarget(null);mob.setNoAi(true);mob.setNoGravity(true);mob.setDeltaMovement(Vec3.ZERO);
        } else target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN,60,1));
        player.serverLevel().sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD,target.getX(),target.getEyeY()+.3,target.getZ(),8,.3,.2,.3,0);
        return true;
    }
    public static boolean active(ServerPlayer player) {
        var sleep=SLEEP.get(player.getUUID());if(sleep==null) return false;
        if(!player.isAlive() || player.isSpectator() || player.getMainHandItem()!=sleep.stack()
            || player.getInventory().selected!=sleep.selected() || !sleep.mob().isAlive() || sleep.mob().level()!=player.level()
            || player.level().getGameTime()>=sleep.expires() || !Augmentations.ids(sleep.stack(),true).contains(AdditionalAbilities.LULLABY)) {
            cancel(player.getUUID());return false;
        }
        return true;
    }
    public static void cancel(UUID owner) {
        var sleep=SLEEP.remove(owner);if(sleep==null) return;
        sleep.mob().setNoAi(sleep.noAi());sleep.mob().setNoGravity(sleep.noGravity());
        if(sleep.mob().isAlive() && sleep.target()!=null && sleep.target().isAlive() && sleep.target().level()==sleep.mob().level()) sleep.mob().setTarget(sleep.target());
    }
    @SubscribeEvent
    public static void wake(LivingDamageEvent.Post event) {
        if(event.getNewDamage()<=0 && event.getReduction(net.neoforged.neoforge.common.damagesource.DamageContainer.Reduction.ABSORPTION)<=0) return;
        for(var owner:java.util.List.copyOf(SLEEP.keySet())) if(SLEEP.get(owner).mob()==event.getEntity()) cancel(owner);
    }
    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post event) {
        if(event.getEntity() instanceof ServerPlayer player && active(player)) {
            var sleep=SLEEP.get(player.getUUID());sleep.mob().setDeltaMovement(Vec3.ZERO);sleep.mob().setPos(sleep.position());sleep.mob().setTarget(null);
        }
    }
    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {cancel(event.getEntity().getUUID());}
}
