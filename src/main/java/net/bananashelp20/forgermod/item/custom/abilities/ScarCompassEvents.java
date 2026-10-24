package net.bananashelp20.forgermod.item.custom.abilities;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.minecraft.server.level.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.damagesource.DamageTypes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.*;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.EntityTravelToDimensionEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import java.util.*;
@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class ScarCompassEvents {
    private record Mark(ItemStack stack,int slot,ServerLevel level,LivingEntity target,long expires) {}
    private static final class Glow {
        final Map<UUID,Long> owners=new HashMap<>();MobEffectInstance previous,owned;
        Glow(LivingEntity entity) {previous=copy(entity.getEffect(MobEffects.GLOWING));}
    }
    private static final Map<UUID,Mark> MARKS=new HashMap<>();
    private static final Map<LivingEntity,Glow> GLOWS=new HashMap<>();
    private static final Set<LivingEntity> INTERNAL=new HashSet<>();
    private ScarCompassEvents() {}
    private static MobEffectInstance copy(MobEffectInstance effect) {return effect==null?null:MobEffectInstance.load((net.minecraft.nbt.CompoundTag)effect.save());}
    private static void add(LivingEntity target,MobEffectInstance effect) {INTERNAL.add(target);try {target.addEffect(effect);} finally {INTERNAL.remove(target);}}
    private static void refresh(LivingEntity target,Glow glow) {
        long until=glow.owners.values().stream().mapToLong(Long::longValue).max().orElse(target.level().getGameTime());
        int amplifier=glow.owners.keySet().stream().anyMatch(owner -> {
            Mark mark=MARKS.get(owner);
            return mark!=null && JadeAugments.bonus(mark.stack(),AdditionalAbilities.SCAR_COMPASS)>0;
        }) ? 1 : 0;
        target.removeEffect(MobEffects.GLOWING);add(target,new MobEffectInstance(MobEffects.GLOWING,(int)Math.max(1,until-target.level().getGameTime()),amplifier,false,false,true));glow.owned=target.getEffect(MobEffects.GLOWING);
    }
    public static void end(UUID owner) {
        var mark=MARKS.remove(owner);if(mark==null) return;var glow=GLOWS.get(mark.target);if(glow==null) return;glow.owners.remove(owner);
        if(!glow.owners.isEmpty()) {if(mark.target.getEffect(MobEffects.GLOWING)==glow.owned) refresh(mark.target,glow);return;}
        GLOWS.remove(mark.target);
        if(mark.target.getEffect(MobEffects.GLOWING)==glow.owned) {mark.target.removeEffect(MobEffects.GLOWING);if(mark.target.getHealth()>0 && glow.previous!=null) add(mark.target,glow.previous);}
    }
    @SubscribeEvent
    public static void hit(LivingDamageEvent.Post event) {
        if(!PassiveActors.melee(event.getSource()) || !(PassiveActors.entity(PassiveActors.attacker(event.getSource())) instanceof LivingEntity player) || !player.isAlive() || player.isSpectator()
            || !Augmentations.hasPassive(PassiveActors.weapon(player,AdditionalAbilities.SCAR_COMPASS),AdditionalAbilities.SCAR_COMPASS) || !WeaponAbilityTargets.enemy(player,event.getEntity())
            || event.getNewDamage()+event.getReduction(DamageContainer.Reduction.ABSORPTION)<=0) return;
        var previous=MARKS.get(player.getUUID());var target=event.getEntity();
        if(previous!=null && (previous.target!=target || previous.stack!=PassiveActors.weapon(player,AdditionalAbilities.SCAR_COMPASS) || previous.slot!=PassiveActors.selected(player))) end(player.getUUID());
        var oldGlow=GLOWS.get(target);if(oldGlow!=null && target.getEffect(MobEffects.GLOWING)!=oldGlow.owned) {for(var owner:oldGlow.owners.keySet()) MARKS.remove(owner);GLOWS.remove(target);}
        var glow=GLOWS.computeIfAbsent(target,Glow::new);long expires=player.level().getGameTime()
                +JadeAugments.duration(PassiveActors.weapon(player,AdditionalAbilities.SCAR_COMPASS),AdditionalAbilities.SCAR_COMPASS,12000);
        glow.owners.put(player.getUUID(),expires);MARKS.put(player.getUUID(),new Mark(PassiveActors.weapon(player,AdditionalAbilities.SCAR_COMPASS),PassiveActors.selected(player),PassiveActors.level(player),target,expires));refresh(target,glow);
    }
    @SubscribeEvent
    public static void tick(ServerTickEvent.Post event) {
        for(var owner:List.copyOf(MARKS.keySet())) {
            var player=PassiveActors.find(event.getServer(),owner);var mark=MARKS.get(owner);
            if(player==null || !player.isAlive() || player.isSpectator() || PassiveActors.weapon(player,AdditionalAbilities.SCAR_COMPASS)!=mark.stack || PassiveActors.selected(player)!=mark.slot || PassiveActors.level(player)!=mark.level
                || !mark.target.isAlive() || mark.target.level()!=mark.level || mark.level.getGameTime()>=mark.expires || !Augmentations.hasPassive(mark.stack,AdditionalAbilities.SCAR_COMPASS)) end(owner);
        }
        for(var entry:List.copyOf(GLOWS.entrySet())) {
            var target=entry.getKey();var glow=entry.getValue();
            if(target.getEffect(MobEffects.GLOWING)!=glow.owned) {for(var owner:glow.owners.keySet()) MARKS.remove(owner);GLOWS.remove(target);continue;}
            if(glow.previous!=null && !glow.previous.tick(target,()->{})) glow.previous=null;
        }
    }
    @SubscribeEvent
    public static void added(MobEffectEvent.Added event) {
        var glow=GLOWS.get(event.getEntity());if(glow==null || INTERNAL.contains(event.getEntity()) || !event.getEffectInstance().getEffect().equals(MobEffects.GLOWING)) return;
        var effect=copy(event.getEffectInstance());if(glow.previous==null) glow.previous=effect;else glow.previous.update(effect);
    }
    @SubscribeEvent
    public static void travel(EntityTravelToDimensionEvent event) {
        if(event.getEntity() instanceof LivingEntity target) {var glow=GLOWS.get(target);if(glow!=null) for(var owner:List.copyOf(glow.owners.keySet())) end(owner);}
        end(event.getEntity().getUUID());
    }
    @SubscribeEvent
    public static void leave(net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent event) {
        if(event.getLevel().isClientSide()) return;
        if(event.getEntity() instanceof LivingEntity target) {var glow=GLOWS.get(target);if(glow!=null) for(var owner:List.copyOf(glow.owners.keySet())) end(owner);}
        end(event.getEntity().getUUID());
    }
    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {end(event.getEntity().getUUID());}
}
