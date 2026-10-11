package net.bananashelp20.forgermod.item.custom.abilities;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class LastStandEvents {
    public static final String COOLDOWN="forgermod_last_stand_cooldown";
    private LastStandEvents() {}
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void damage(LivingDamageEvent.Pre event) {
        if(!(event.getEntity() instanceof ServerPlayer player) || !player.isAlive() || player.isSpectator() || player.getHealth()<=1
            || !Augmentations.hasPassive(player.getMainHandItem(),AdditionalAbilities.LAST_STAND)) return;
        long now=player.server.overworld().getGameTime();
        if(now<player.getPersistentData().getLong(COOLDOWN) || Math.max(0,event.getNewDamage()-player.getAbsorptionAmount())<player.getHealth()) return;
        // NeoForge 21.1.93 applies absorption AFTER this event. Consume it normally and leave one real HP.
        event.setNewDamage(player.getHealth()-1+player.getAbsorptionAmount());player.getPersistentData().putLong(COOLDOWN,now+2400);
        player.serverLevel().sendParticles(ParticleTypes.SOUL,player.getX(),player.getY()+1,player.getZ(),24,.4,.5,.4,.03);
        player.serverLevel().playSound(null,player.blockPosition(),SoundEvents.TOTEM_USE,player.getSoundSource(),.7F,1);
    }
    @SubscribeEvent
    public static void clone(PlayerEvent.Clone event) {event.getEntity().getPersistentData().putLong(COOLDOWN,event.getOriginal().getPersistentData().getLong(COOLDOWN));}
}
