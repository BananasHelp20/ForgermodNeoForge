package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.AdditionalAbilities;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.WeakHashMap;

@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class UnfinishedDreamEvents {
    private static final Map<LivingEntity,Set<UUID>> INJURED=new WeakHashMap<>();
    private UnfinishedDreamEvents() {}
    public static int healing(int rank) { return 2*Math.clamp(rank,1,3); }
    @SubscribeEvent
    public static void damaged(LivingDamageEvent.Post event) {
        if(PassiveActors.entity(event.getEntity()) instanceof LivingEntity player
                && event.getNewDamage()+event.getReduction(DamageContainer.Reduction.ABSORPTION)>0
                && PassiveActors.attacker(event.getSource()) instanceof LivingEntity attacker && WeaponAbilityTargets.enemy(player,attacker))
            INJURED.computeIfAbsent(attacker,key->new HashSet<>()).add(player.getUUID());
    }
    @SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.LOWEST)
    public static void death(LivingDeathEvent event) {
        if(event.isCanceled() || event.getEntity().level().isClientSide) return;
        var injured=INJURED.remove(event.getEntity());
        if(!(PassiveActors.entity(PassiveActors.attacker(event.getSource())) instanceof LivingEntity player) || !player.isAlive() || player.isSpectator()
                || !Augmentations.hasPassive(PassiveActors.weapon(player,AdditionalAbilities.UNFINISHED_DREAM),AdditionalAbilities.UNFINISHED_DREAM)) return;
        var victim=event.getEntity();
        boolean enemy=victim!=player && !victim.isAlliedTo(player) && (victim instanceof Enemy
                || victim instanceof Mob mob && mob.getTarget()==player
                || victim instanceof Player other && PassiveActors.owner(player).server.isPvpAllowed() && PassiveActors.owner(player).canHarmPlayer(other));
        if(enemy && (injured==null || !injured.contains(player.getUUID()))) player.heal(healing(Augmentations.level(PassiveActors.weapon(player,AdditionalAbilities.UNFINISHED_DREAM),AdditionalAbilities.UNFINISHED_DREAM)));
    }
    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {
        var owner=event.getEntity().getUUID();INJURED.values().forEach(set->set.remove(owner));INJURED.values().removeIf(Set::isEmpty);
    }
    @SubscribeEvent public static void leave(net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent event){
        if(event.getLevel().isClientSide)return;var id=event.getEntity().getUUID();INJURED.values().forEach(set->set.remove(id));INJURED.remove(event.getEntity());INJURED.values().removeIf(Set::isEmpty);
    }
}
