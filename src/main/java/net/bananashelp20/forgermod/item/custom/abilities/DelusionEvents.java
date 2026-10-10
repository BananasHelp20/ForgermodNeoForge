package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.augmentation.RecommendedAbilities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class DelusionEvents {
    private static final class Dream {
        final ServerPlayer owner; final ItemStack stack; final int selected; final ServerLevel level;
        long expires; Mob target;
        Dream(ServerPlayer owner,ItemStack stack) { this.owner=owner; this.stack=stack; selected=owner.getInventory().selected; level=owner.serverLevel(); expires=level.getGameTime()+600; }
    }
    private static final Map<UUID,Dream> DREAMS=new HashMap<>();
    private static final Map<UUID,Set<UUID>> TARGETS=new HashMap<>();
    private DelusionEvents() {}
    public static boolean activate(ServerPlayer player,ItemStack stack) {
        if(active(player)) return false;
        DREAMS.put(player.getUUID(),new Dream(player,stack)); return true;
    }
    public static boolean active(ServerPlayer player) {
        var dream=DREAMS.get(player.getUUID()); if(dream==null) return false;
        if(!player.isAlive() || player.isSpectator() || player.getMainHandItem()!=dream.stack || player.getInventory().selected!=dream.selected
                || player.level()!=dream.level || player.level().getGameTime()>=dream.expires
                || dream.target!=null && (!dream.target.isAlive() || dream.target.level()!=player.level() || dream.target.isAlliedTo(player))
                || !Augmentations.ids(dream.stack,true).contains(RecommendedAbilities.DELUSION)) {
            cancel(player.getUUID()); return false;
        }
        return true;
    }
    public static void cancel(UUID owner) {
        var dream=DREAMS.remove(owner); if(dream==null || dream.target==null) return;
        var owners=TARGETS.get(dream.target.getUUID());
        if(owners!=null) { owners.remove(owner); if(owners.isEmpty()) TARGETS.remove(dream.target.getUUID()); }
    }
    private static boolean suppressed(Mob mob) {
        var owners=TARGETS.get(mob.getUUID()); if(owners==null) return false;
        for(var owner:Set.copyOf(owners)) {
            var dream=DREAMS.get(owner); if(dream!=null && dream.target==mob && active(dream.owner)) return true;
        }
        return false;
    }
    private static void forget(Mob mob) {
        if(mob.getTarget()!=null) mob.setTarget(null);
        mob.setLastHurtByMob(null); mob.setAggressive(false);
        if(mob instanceof NeutralMob neutral) neutral.stopBeingAngry();
        var brain=mob.getBrain(); brain.eraseMemory(MemoryModuleType.ATTACK_TARGET); brain.eraseMemory(MemoryModuleType.HURT_BY);
        brain.eraseMemory(MemoryModuleType.HURT_BY_ENTITY); brain.eraseMemory(MemoryModuleType.ANGRY_AT);
        brain.eraseMemory(MemoryModuleType.UNIVERSAL_ANGER); brain.eraseMemory(MemoryModuleType.ROAR_TARGET);
    }
    @SubscribeEvent
    public static void onHit(LivingDamageEvent.Post event) {
        if(event.getNewDamage()<=0 || !event.getSource().is(DamageTypes.PLAYER_ATTACK)
                || !(event.getSource().getEntity() instanceof ServerPlayer player) || !active(player)
                || !(event.getEntity() instanceof Mob mob) || !WeaponAbilityTargets.enemy(player,mob)) return;
        var dream=DREAMS.get(player.getUUID()); if(dream.target!=null) return;
        dream.target=mob; dream.expires=player.level().getGameTime()+400;
        TARGETS.computeIfAbsent(mob.getUUID(),ignored->new HashSet<>()).add(player.getUUID());
        mob.goalSelector.getAvailableGoals().stream().filter(goal->goal.isRunning()).forEach(goal->goal.stop()); mob.getNavigation().stop();
        mob.getBrain().eraseMemory(MemoryModuleType.WALK_TARGET); mob.getBrain().eraseMemory(MemoryModuleType.LOOK_TARGET);
        forget(mob);
    }
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void onTarget(LivingChangeTargetEvent event) {
        if(event.getEntity() instanceof Mob mob && !mob.level().isClientSide && suppressed(mob)) event.setNewAboutToBeSetTarget(null);
    }
    @SubscribeEvent
    public static void onMobTick(EntityTickEvent.Pre event) {
        if(event.getEntity() instanceof Mob mob && !mob.level().isClientSide && suppressed(mob)) forget(mob);
    }
    @SubscribeEvent
    public static void onMobPost(EntityTickEvent.Post event) {
        if(event.getEntity() instanceof Mob mob && !mob.level().isClientSide && suppressed(mob)) forget(mob);
    }
    @SubscribeEvent
    public static void onTick(PlayerTickEvent.Post event) {
        if(!(event.getEntity() instanceof ServerPlayer player) || !active(player)) return;
        var dream=DREAMS.get(player.getUUID());
        if(dream.target!=null && player.level().getGameTime()%10==0)
            player.serverLevel().sendParticles(ParticleTypes.ENCHANT,dream.target.getX(),dream.target.getY()+1,dream.target.getZ(),4,.3,.4,.3,0);
    }
    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) { cancel(event.getEntity().getUUID()); }
}
