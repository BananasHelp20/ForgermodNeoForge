package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.AdditionalAbilities;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.damagesource.DamageTypes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import java.util.*;

@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class AlternatingCurrentEvents {
    private static final class Link {
        final ItemStack stack;final int selected;long expires,nextPulse;LivingEntity first,second;boolean pulseFirst=true;
        Link(ServerPlayer player,ItemStack stack) {this.stack=stack;selected=player.getInventory().selected;expires=player.level().getGameTime()+600;}
    }
    private static final Map<UUID,Link> LINKS=new HashMap<>();
    private AlternatingCurrentEvents() {}
    public static boolean activate(ServerPlayer player,ItemStack stack) {if(active(player)) return false;LINKS.put(player.getUUID(),new Link(player,stack));return true;}
    public static boolean active(ServerPlayer player) {
        var link=LINKS.get(player.getUUID());if(link==null) return false;
        if(!player.isAlive() || player.isSpectator() || player.getMainHandItem()!=link.stack || player.getInventory().selected!=link.selected
            || player.level().getGameTime()>=link.expires || !Augmentations.ids(link.stack,true).contains(AdditionalAbilities.ALTERNATING_CURRENT)
            || link.first!=null && (!WeaponAbilityTargets.enemy(player,link.first) || !WeaponAbilityTargets.enemy(player,link.second)
                || link.first.level()!=player.level() || link.second.level()!=player.level() || link.first.distanceToSqr(link.second)>400)) {cancel(player.getUUID());return false;}
        return true;
    }
    public static long remainingTicks(ServerPlayer player) {return active(player)?LINKS.get(player.getUUID()).expires-player.level().getGameTime():0;}
    public static void cancel(UUID owner) {LINKS.remove(owner);WeaponControlLocks.release(owner,AdditionalAbilities.ALTERNATING_CURRENT);}
    @SubscribeEvent
    public static void landed(LivingDamageEvent.Post event) {
        if(!event.getSource().is(DamageTypes.PLAYER_ATTACK) || !(event.getSource().getEntity() instanceof ServerPlayer player) || !active(player)
            || event.getNewDamage()+event.getReduction(DamageContainer.Reduction.ABSORPTION)<=0 || !WeaponAbilityTargets.enemy(player,event.getEntity())) return;
        var link=LINKS.get(player.getUUID());if(link.first!=null) return;var first=event.getEntity();
        var next=player.level().getEntitiesOfClass(LivingEntity.class,first.getBoundingBox().inflate(20),e->e!=first && WeaponAbilityTargets.enemy(player,e) && first.distanceToSqr(e)<=400 && first.hasLineOfSight(e))
            .stream().min(Comparator.comparingDouble(first::distanceToSqr)).orElse(null);if(next==null) return;
        link.first=first;link.second=next;link.expires=player.level().getGameTime()+1200;link.nextPulse=player.level().getGameTime()+20;
        pulse(player,link,next);
    }
    private static void pulse(ServerPlayer player,Link link,LivingEntity target) {
        if(!link.first.hasLineOfSight(link.second)) return;
        WeaponControlLocks.acquire(target,player.getUUID(),AdditionalAbilities.ALTERNATING_CURRENT,10);
        var from=link.first.getEyePosition();var delta=link.second.getEyePosition().subtract(from);int count=Math.min(32,Math.max(2,(int)(delta.length()*2)));
        for(int i=0;i<=count;i++) {var p=from.add(delta.scale((double)i/count));player.serverLevel().sendParticles(net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK,p.x,p.y,p.z,1,0,0,0,.01);}
    }
    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post event) {
        if(!(event.getEntity() instanceof ServerPlayer player) || !active(player)) return;var link=LINKS.get(player.getUUID());if(link.first==null || player.level().getGameTime()<link.nextPulse) return;
        pulse(player,link,link.pulseFirst?link.first:link.second);link.pulseFirst=!link.pulseFirst;link.nextPulse=player.level().getGameTime()+20;
    }
    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {cancel(event.getEntity().getUUID());}
}
