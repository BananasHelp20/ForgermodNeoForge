package net.bananashelp20.forgermod.item.custom.abilities;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.minecraft.server.level.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.*;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import java.util.*;
@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class GhostwalkEvents {
    private static final class Ghost {
        final ServerPlayer player;final ItemStack stack;final int slot;final ServerLevel level;final long expires;MobEffectInstance previous,owned;boolean expiredNaturally;
        final int duration;
        Ghost(ServerPlayer player,ItemStack stack) {this.player=player;this.stack=stack;slot=player.getInventory().selected;level=player.serverLevel();duration=JadeAugments.duration(stack,AdditionalAbilities.GHOSTWALK,200+40*Math.max(0,Augmentations.level(stack,AdditionalAbilities.GHOSTWALK)-4));expires=level.getGameTime()+duration;previous=copy(player.getEffect(MobEffects.INVISIBILITY));}
    }
    private static final Map<UUID,Ghost> GHOSTS=new HashMap<>();
    private static final Set<UUID> INTERNAL=new HashSet<>();
    private record InvisibleHit(int target,long tick,ItemStack stack) {}
    private static final Map<UUID,InvisibleHit> HITS=new HashMap<>();
    private GhostwalkEvents() {}
    private static MobEffectInstance copy(MobEffectInstance effect) {return effect==null?null:MobEffectInstance.load((net.minecraft.nbt.CompoundTag)effect.save());}
    private static void add(ServerPlayer player,MobEffectInstance effect) {INTERNAL.add(player.getUUID());try {player.addEffect(effect);} finally {INTERNAL.remove(player.getUUID());}}
    public static boolean activate(ServerPlayer player,ItemStack stack) {
        if(active(player)) return false;var ghost=new Ghost(player,stack);GHOSTS.put(player.getUUID(),ghost);player.removeEffect(MobEffects.INVISIBILITY);add(player,new MobEffectInstance(MobEffects.INVISIBILITY,ghost.duration,JadeAugments.amplifier(stack,AdditionalAbilities.GHOSTWALK,0)));ghost.owned=player.getEffect(MobEffects.INVISIBILITY);
        if(ghost.owned==null) {cancel(player.getUUID());return false;}
        GhostwalkVisibility.set(player,true);return true;
    }
    public static boolean active(ServerPlayer player) {
        var ghost=GHOSTS.get(player.getUUID());if(ghost==null) return false;
        if(!player.isAlive() || player.isSpectator() || player.getMainHandItem()!=ghost.stack || player.getInventory().selected!=ghost.slot || player.serverLevel()!=ghost.level || player.level().getGameTime()>=ghost.expires
            || player.getEffect(MobEffects.INVISIBILITY)!=ghost.owned || !Augmentations.ids(ghost.stack,true).contains(AdditionalAbilities.GHOSTWALK)) {cancel(player.getUUID());return false;}return true;
    }
    public static void cancel(UUID owner) {
        HITS.remove(owner);
        var ghost=GHOSTS.remove(owner);if(ghost==null) return;
        GhostwalkVisibility.set(ghost.player,false);
        if(ghost.player.getEffect(MobEffects.INVISIBILITY)==ghost.owned || ghost.expiredNaturally && ghost.player.getEffect(MobEffects.INVISIBILITY)==null) {ghost.player.removeEffect(MobEffects.INVISIBILITY);if(ghost.player.isAlive() && ghost.previous!=null) add(ghost.player,ghost.previous);}
    }
    /** Air/block swings clear only an unconfirmed attack, not invisibility. */
    public static void swing(ServerPlayer player) {HITS.remove(player.getUUID());}
    @SubscribeEvent
    public static void attack(AttackEntityEvent event) {
        if(!event.isCanceled() && event.getEntity() instanceof ServerPlayer player)prepareAttack(player,event.getTarget());
    }
    public static void prepareAttack(ServerPlayer player,net.minecraft.world.entity.Entity target) {
        if(WeaponControlLocks.frozen(player))return;
        HITS.remove(player.getUUID());
        if(active(player) && Augmentations.level(player.getMainHandItem(),AdditionalAbilities.GHOSTWALK)>=4
                && target instanceof net.minecraft.world.entity.LivingEntity)
            HITS.put(player.getUUID(),new InvisibleHit(target.getId(),player.level().getGameTime(),player.getMainHandItem()));
    }
    @SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.LOWEST)
    public static void damage(net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent event) {
        if(event.isCanceled() || !(event.getSource().getEntity() instanceof ServerPlayer player)
                || !event.getSource().is(net.minecraft.world.damagesource.DamageTypes.PLAYER_ATTACK))return;
        var hit=HITS.get(player.getUUID());
        if(hit!=null && active(player) && hit.tick()==player.level().getGameTime() && hit.stack()==player.getMainHandItem() && hit.target()==event.getEntity().getId()) {
            HITS.remove(player.getUUID());event.setAmount(event.getAmount()*2);
        }
    }
    @SubscribeEvent
    public static void successfulHit(net.neoforged.neoforge.event.entity.living.LivingDamageEvent.Post event) {
        if((event.getNewDamage()>0 || event.getReduction(net.neoforged.neoforge.common.damagesource.DamageContainer.Reduction.ABSORPTION)>0)
                && event.getSource().is(net.minecraft.world.damagesource.DamageTypes.PLAYER_ATTACK)
                && event.getSource().getEntity() instanceof ServerPlayer player && event.getEntity()!=player && active(player))
            cancel(player.getUUID());
    }
    @SubscribeEvent
    public static void block(PlayerInteractEvent.LeftClickBlock event) {if(!event.isCanceled() && event.getEntity() instanceof ServerPlayer player) swing(player);}
    @SubscribeEvent
    public static void tick(PlayerTickEvent.Pre event) {if(event.getEntity() instanceof ServerPlayer player) {
        var hit=HITS.get(player.getUUID());if(hit!=null && hit.tick()!=player.level().getGameTime())HITS.remove(player.getUUID());
        if(active(player)) {var ghost=GHOSTS.get(player.getUUID());if(ghost.previous!=null && !ghost.previous.tick(player,()->{})) ghost.previous=null;}
    }}
    @SubscribeEvent
    public static void expired(MobEffectEvent.Expired event) {
        var ghost=GHOSTS.get(event.getEntity().getUUID());if(!event.isCanceled() && ghost!=null && event.getEffectInstance()==ghost.owned) ghost.expiredNaturally=true;
    }
    @SubscribeEvent
    public static void added(MobEffectEvent.Added event) {
        if(!(event.getEntity() instanceof ServerPlayer player) || INTERNAL.contains(player.getUUID()) || !event.getEffectInstance().getEffect().equals(MobEffects.INVISIBILITY)) return;
        var ghost=GHOSTS.get(player.getUUID());if(ghost!=null) {var effect=copy(event.getEffectInstance());if(ghost.previous==null) ghost.previous=effect;else ghost.previous.update(effect);}
    }
    @SubscribeEvent
    public static void travel(net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent event) {HITS.remove(event.getEntity().getUUID());cancel(event.getEntity().getUUID());}
    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {HITS.remove(event.getEntity().getUUID());cancel(event.getEntity().getUUID());}
}
