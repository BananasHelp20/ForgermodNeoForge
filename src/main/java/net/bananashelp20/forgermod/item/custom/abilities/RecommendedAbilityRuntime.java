package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.augmentation.RecommendedAbilities;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Dispatch learned recommendations by identity, independent of their primary/secondary ordering. */
public final class RecommendedAbilityRuntime {
    private RecommendedAbilityRuntime() {}
    public static boolean activate(ServerPlayer player,ItemStack stack,String id) {
        if(!player.isAlive() || player.isSpectator() || stack!=player.getMainHandItem()
                || !Augmentations.ids(stack,true).contains(id)) return false;
        if(RecommendedAbilities.EXPLOSION.equals(id)) {
            player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE,40,3));
            // Vanilla excludes the causing entity from explosion damage/knockback.
            player.level().explode(player,player.getX(),player.getY(),player.getZ(),3,false,Level.ExplosionInteraction.NONE);
            return true;
        }
        if(RecommendedAbilities.CINDER_DECOY.equals(id)) return CinderDecoyEvents.activate(player,stack);
        if(RecommendedAbilities.EXPLOSIVE_HITS.equals(id)) return ExplosiveHitsEvents.activate(player,stack);
        if(RecommendedAbilities.CRUCIBLE_HOOK.equals(id)) return CrucibleHookEvents.activate(player,stack);
        if(RecommendedAbilities.STRONG_ARM.equals(id)) return StrongArmAbility.activate(player,stack);
        if(RecommendedAbilities.SWING_ATTACK.equals(id)) return SwingAttackAbility.activate(player,stack);
        return false;
    }
    public static boolean active(ServerPlayer player,String id) {
        return RecommendedAbilities.CINDER_DECOY.equals(id) && CinderDecoyEvents.active(player)
                || RecommendedAbilities.EXPLOSIVE_HITS.equals(id) && ExplosiveHitsEvents.active(player)
                || RecommendedAbilities.CRUCIBLE_HOOK.equals(id) && CrucibleHookEvents.active(player);
    }
    public static void cancel(ServerPlayer player,String id) {
        if(RecommendedAbilities.CINDER_DECOY.equals(id)) CinderDecoyEvents.cancel(player.getUUID());
        if(RecommendedAbilities.EXPLOSIVE_HITS.equals(id)) ExplosiveHitsEvents.cancel(player.getUUID());
        if(RecommendedAbilities.CRUCIBLE_HOOK.equals(id)) CrucibleHookEvents.cancel(player.getUUID());
    }
}
