package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.augmentation.RecommendedAbilities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class CrucibleHookEvents {
    private record Hook(ItemStack stack,int selected,LivingEntity target,long expires,int rank) {}
    private static final Map<UUID,Hook> HOOKS=new HashMap<>();
    private CrucibleHookEvents() {}
    public static boolean activate(ServerPlayer player,ItemStack stack) {
        if(active(player)) return false;
        var target=WeaponAbilityTargets.aimedEnemy(player,20);
        int rank=Augmentations.level(stack,RecommendedAbilities.CRUCIBLE_HOOK);
        if(target==null || rank<=0) return false;
        HOOKS.put(player.getUUID(),new Hook(stack,player.getInventory().selected,target,player.level().getGameTime()+40,rank));
        advance(player); return true;
    }
    public static boolean active(ServerPlayer player) {
        Hook hook=HOOKS.get(player.getUUID()); if(hook==null) return false;
        if(!player.isAlive() || player.isSpectator() || player.getMainHandItem()!=hook.stack()
                || player.getInventory().selected!=hook.selected() || !hook.target().isAlive()
                || hook.target().level()!=player.level() || player.level().getGameTime()>=hook.expires()
                || player.distanceToSqr(hook.target())>24*24
                || !Augmentations.ids(hook.stack(),true).contains(RecommendedAbilities.CRUCIBLE_HOOK)
                || !player.hasLineOfSight(hook.target()) || !WeaponAbilityTargets.enemy(player,hook.target())) {
            cancel(player.getUUID()); return false;
        }
        return true;
    }
    public static void cancel(UUID owner) { HOOKS.remove(owner); }
    private static void advance(ServerPlayer player) {
        if(!active(player)) return;
        Hook hook=HOOKS.get(player.getUUID()); var target=hook.target();
        if(player.distanceToSqr(target)<=player.entityInteractionRange()*player.entityInteractionRange()) {
            cancel(player.getUUID()); player.swing(InteractionHand.MAIN_HAND);
            int immunity=target.invulnerableTime; target.invulnerableTime=0;
            float damage=(float)player.getAttributeValue(Attributes.ATTACK_DAMAGE)*(1+.25F*hook.rank());
            if(target.hurt(player.damageSources().playerAttack(player),damage)) {
                target.setRemainingFireTicks(Math.max(target.getRemainingFireTicks(),(2+hook.rank())*20));
                hook.stack().getItem().postHurtEnemy(hook.stack(),target,player);
            } else target.invulnerableTime=immunity;
            player.serverLevel().sendParticles(ParticleTypes.CRIT,target.getX(),target.getY()+.3,target.getZ(),24,.6,.2,.6,.1);
            player.serverLevel().playSound(null,target.blockPosition(),SoundEvents.IRON_GOLEM_ATTACK,SoundSource.PLAYERS,1,.7F);
            return;
        }
        var destination=player.position().add(player.getLookAngle().multiply(1,0,1).scale(1.5));
        var direction=destination.subtract(target.position());
        target.setDeltaMovement(direction.normalize().scale(Math.min(direction.length(),.7+.1*hook.rank())));
        target.hasImpulse=true;
        if(target instanceof ServerPlayer other) other.connection.send(new ClientboundSetEntityMotionPacket(other));
        player.serverLevel().sendParticles(ParticleTypes.FLAME,target.getX(),target.getY()+1,target.getZ(),3,.1,.2,.1,0);
    }
    @SubscribeEvent
    public static void onTick(PlayerTickEvent.Post event) {
        if(event.getEntity() instanceof ServerPlayer player) advance(player);
    }
    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) { cancel(event.getEntity().getUUID()); }
}
