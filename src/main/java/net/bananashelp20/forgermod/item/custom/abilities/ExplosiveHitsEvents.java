package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.augmentation.RecommendedAbilities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class ExplosiveHitsEvents {
    private static final class Charges {
        final ItemStack stack; final int selected; int hits=10;
        Charges(ServerPlayer player,ItemStack stack) { this.stack=stack; selected=player.getInventory().selected; }
    }
    private static final Map<UUID,Charges> CHARGES=new HashMap<>();
    private ExplosiveHitsEvents() {}
    public static boolean active(ServerPlayer player) {
        Charges charges=CHARGES.get(player.getUUID()); if(charges==null) return false;
        if(!player.isAlive() || player.isSpectator() || player.getMainHandItem()!=charges.stack
                || player.getInventory().selected!=charges.selected || charges.hits<=0
                || !Augmentations.ids(charges.stack,true).contains(RecommendedAbilities.EXPLOSIVE_HITS)) {
            cancel(player.getUUID()); return false;
        }
        return true;
    }
    public static boolean activate(ServerPlayer player,ItemStack stack) {
        if(active(player) || !Augmentations.ids(stack,true).contains(RecommendedAbilities.EXPLOSIVE_HITS)) return false;
        CHARGES.put(player.getUUID(),new Charges(player,stack)); return true;
    }
    public static void cancel(UUID owner) { CHARGES.remove(owner); }
    @SubscribeEvent
    public static void onHit(LivingDamageEvent.Post event) {
        if(event.getNewDamage()<=0 || !event.getSource().is(DamageTypes.PLAYER_ATTACK)
                || !(event.getSource().getEntity() instanceof ServerPlayer player) || !active(player)) return;
        Charges charges=CHARGES.get(player.getUUID());
        if(--charges.hits==0) cancel(player.getUUID());
        var center=event.getEntity().getBoundingBox().getCenter();
        double radius=1+.5*Augmentations.level(charges.stack,RecommendedAbilities.EXPLOSIVE_HITS);
        float damage=(float)player.getAttributeValue(Attributes.ATTACK_DAMAGE)*.3F;
        var level=player.serverLevel();
        level.sendParticles(ParticleTypes.EXPLOSION,center.x,center.y,center.z,1,0,0,0,0);
        level.playSound(null,center.x,center.y,center.z,SoundEvents.GENERIC_EXPLODE,SoundSource.PLAYERS,.5F,1.2F);
        for(LivingEntity target:level.getEntitiesOfClass(LivingEntity.class,event.getEntity().getBoundingBox().inflate(radius),
                target->target!=player && target.isAlive() && !target.isAlliedTo(player)
                        && target.getBoundingBox().getCenter().distanceToSqr(center)<=radius*radius)) {
            if(level.clip(new ClipContext(center,target.getEyePosition(),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,player))
                    .getType()!=HitResult.Type.MISS) continue;
            int immunity=target.invulnerableTime; target.invulnerableTime=0;
            // Explosion source prevents these secondary hits from recursively triggering melee abilities.
            target.hurt(player.damageSources().explosion(player,player),damage);
            target.invulnerableTime=Math.max(immunity,target.invulnerableTime);
        }
    }
    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) { cancel(event.getEntity().getUUID()); }
}
