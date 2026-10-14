package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.augmentation.RecommendedAbilities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Dispatch learned recommendations by identity, independent of their primary/secondary ordering. */
public final class RecommendedAbilityRuntime {
    private RecommendedAbilityRuntime() {}
    public static boolean activate(ServerPlayer player,ItemStack stack,String id) {
        if(!player.isAlive() || player.isSpectator() || WeaponControlLocks.frozen(player) || stack!=player.getMainHandItem()
                || !Augmentations.ids(stack,true).contains(id)) return false;
        if(RecommendedAbilities.EXPLOSION.equals(id)) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,40,3));
            // Vanilla excludes the causing entity from explosion damage/knockback.
            player.level().explode(player,player.getX(),player.getY(),player.getZ(),4,false,Level.ExplosionInteraction.NONE);
            var level=player.serverLevel();
            double x=player.getX(), y=player.getY()+.8, z=player.getZ();
            level.sendParticles(ParticleTypes.EXPLOSION, x,y,z, 24, 2.3,1.3,2.3, 0);
            level.sendParticles(ParticleTypes.POOF, x,y,z, 40, 2.6,1.5,2.6, .12);
            level.sendParticles(ParticleTypes.LARGE_SMOKE, x,y,z, 18, 2,1.2,2, .04);
            return true;
        }
        if(RecommendedAbilities.CINDER_DECOY.equals(id)) return CinderDecoyEvents.activate(player,stack);
        if(RecommendedAbilities.EXPLOSIVE_HITS.equals(id)) return ExplosiveHitsEvents.activate(player,stack);
        if(RecommendedAbilities.CRUCIBLE_HOOK.equals(id)) return CrucibleHookEvents.activate(player,stack);
        if(RecommendedAbilities.STRONG_ARM.equals(id)) return StrongArmAbility.activate(player,stack);
        if(RecommendedAbilities.SWING_ATTACK.equals(id)) return SwingAttackAbility.activate(player,stack);
        if(RecommendedAbilities.DEATH_STARE.equals(id)) return DeathStareAbility.activate(player,stack);
        if(RecommendedAbilities.NULL_RECEIPT.equals(id)) return NullReceiptEvents.activate(player,stack);
        if(RecommendedAbilities.FLAMING_STOMP.equals(id)) return FlamingStompAbility.arm(player,stack);
        if(RecommendedAbilities.FLAMEBORN.equals(id)) return FlamebornAbility.activate(player,stack);
        if(RecommendedAbilities.ENDER_SWING.equals(id)) return EnderSwingAbility.arm(player,stack);
        if(RecommendedAbilities.FALSE_AWAKENING.equals(id)) return FalseAwakeningEvents.activate(player,stack);
        if(RecommendedAbilities.DELUSION.equals(id)) return DelusionEvents.activate(player,stack);
        if(RecommendedAbilities.GARDENERS.equals(id)) return GardenersEvents.activate(player,stack);
        if(RecommendedAbilities.ECHO_HEARING.equals(id)) return EchoHearingAbility.activate(player,stack);
        return AdditionalAbilityRuntime.activate(player,stack,id);
    }
    public static boolean active(ServerPlayer player,String id) {
        return AdditionalAbilityRuntime.active(player,id) || RecommendedAbilities.CINDER_DECOY.equals(id) && CinderDecoyEvents.active(player)
                || RecommendedAbilities.FLAMEBORN.equals(id) && FlamebornAbility.active(player)
                || RecommendedAbilities.ENDER_SWING.equals(id) && EnderSwingAbility.active(player)
                || RecommendedAbilities.EXPLOSIVE_HITS.equals(id) && ExplosiveHitsEvents.active(player)
                || RecommendedAbilities.CRUCIBLE_HOOK.equals(id) && CrucibleHookEvents.active(player)
                || RecommendedAbilities.NULL_RECEIPT.equals(id) && NullReceiptEvents.active(player)
                || RecommendedAbilities.FLAMING_STOMP.equals(id) && FlamingStompAbility.active(player)
                || RecommendedAbilities.FALSE_AWAKENING.equals(id) && FalseAwakeningEvents.active(player)
                || RecommendedAbilities.DELUSION.equals(id) && DelusionEvents.active(player)
                || RecommendedAbilities.GARDENERS.equals(id) && GardenersEvents.active(player)
                || RecommendedAbilities.ECHO_HEARING.equals(id) && EchoHearingAbility.active(player);
    }
    public static void cancel(ServerPlayer player,String id) {
        AdditionalAbilityRuntime.cancel(player,id);
        if(RecommendedAbilities.FLAMEBORN.equals(id)) FlamebornAbility.cancel(player);
        if(RecommendedAbilities.ENDER_SWING.equals(id)) EnderSwingAbility.cancel(player.getUUID());
        if(RecommendedAbilities.CINDER_DECOY.equals(id)) CinderDecoyEvents.cancel(player.getUUID());
        if(RecommendedAbilities.EXPLOSIVE_HITS.equals(id)) ExplosiveHitsEvents.cancel(player.getUUID());
        if(RecommendedAbilities.CRUCIBLE_HOOK.equals(id)) CrucibleHookEvents.cancel(player.getUUID());
        if(RecommendedAbilities.NULL_RECEIPT.equals(id)) NullReceiptEvents.cancel(player.getUUID());
        if(RecommendedAbilities.FLAMING_STOMP.equals(id)) FlamingStompAbility.cancel(player.getUUID());
        if(RecommendedAbilities.FALSE_AWAKENING.equals(id)) FalseAwakeningEvents.cancel(player.getUUID());
        if(RecommendedAbilities.DELUSION.equals(id)) DelusionEvents.cancel(player.getUUID());
        if(RecommendedAbilities.GARDENERS.equals(id)) GardenersEvents.cancel(player.getUUID());
        if(RecommendedAbilities.ECHO_HEARING.equals(id)) EchoHearingAbility.cancel(player.getUUID());
    }
}
