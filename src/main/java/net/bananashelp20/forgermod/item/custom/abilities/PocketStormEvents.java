package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.AdditionalAbilities;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.damagesource.DamageTypes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import java.util.*;

@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class PocketStormEvents {
    private static final class Gust {
        final ItemStack stack;final int selected,rank;final ServerLevel level;long expires;LivingEntity target;
        Gust(ServerPlayer player,ItemStack stack) {this.stack=stack;selected=player.getInventory().selected;rank=Augmentations.level(stack,AdditionalAbilities.POCKET_STORM);level=player.serverLevel();expires=level.getGameTime()+600;}
    }
    private static final Map<UUID,Gust> GUSTS=new HashMap<>();
    private PocketStormEvents() {}
    public static boolean activate(ServerPlayer player,ItemStack stack) {if(active(player)) return false;GUSTS.put(player.getUUID(),new Gust(player,stack));return true;}
    public static boolean active(ServerPlayer player) {
        var gust=GUSTS.get(player.getUUID());if(gust==null) return false;
        if(!player.isAlive() || player.isSpectator() || player.getMainHandItem()!=gust.stack || player.getInventory().selected!=gust.selected || player.serverLevel()!=gust.level
            || player.level().getGameTime()>=gust.expires || !Augmentations.ids(gust.stack,true).contains(AdditionalAbilities.POCKET_STORM)) {cancel(player.getUUID());return false;}return true;
    }
    public static void cancel(UUID owner) {GUSTS.remove(owner);}
    @SubscribeEvent
    public static void landed(LivingDamageEvent.Post event) {
        if(!event.getSource().is(DamageTypes.PLAYER_ATTACK) || !(event.getSource().getEntity() instanceof ServerPlayer player) || !active(player)
            || event.getNewDamage()+event.getReduction(DamageContainer.Reduction.ABSORPTION)<=0 || !WeaponAbilityTargets.hostileTo(player,event.getEntity())) return;
        var gust=GUSTS.get(player.getUUID());if(gust.target!=null) return;gust.target=event.getEntity();gust.expires=player.level().getGameTime()+2;
    }
    @SubscribeEvent
    public static void finish(ServerTickEvent.Post event) {
        for(var id:List.copyOf(GUSTS.keySet())) {
            var player=event.getServer().getPlayerList().getPlayer(id);if(player==null) {cancel(id);continue;}if(!active(player)) continue;var gust=GUSTS.get(id);var target=gust.target;if(target==null) continue;
            cancel(id);if(target.level()!=player.level()) continue;
            var center=target.position();float damage=(float)player.getAttributeValue(Attributes.ATTACK_DAMAGE);
            var victims=player.serverLevel().getEntitiesOfClass(LivingEntity.class,target.getBoundingBox().inflate(3),e->WeaponAbilityTargets.enemy(player,e));
            if(target.isAlive() && !victims.contains(target)) victims.add(target);
            for(var victim:victims) {
                if(victim.position().distanceToSqr(center)>9 || !player.hasLineOfSight(victim)) continue;
                if(victim!=target) victim.hurt(player.damageSources().indirectMagic(player,player),damage);
                double dx=center.x-victim.getX(),dz=center.z-victim.getZ();if(dx*dx+dz*dz<.0001) {dx=player.getX()-victim.getX();dz=player.getZ()-victim.getZ();}
                double strength=.8+.2*gust.rank;victim.knockback(strength,dx,dz);
                var motion=victim.getDeltaMovement();double resistance=Math.max(0,Math.min(1,victim.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE)));
                victim.setDeltaMovement(motion.x,Math.max(motion.y,(.25+.05*gust.rank)*(1-resistance)),motion.z);victim.hasImpulse=true;
                if(victim instanceof ServerPlayer other) other.connection.send(new net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket(other));
            }
            player.serverLevel().sendParticles(net.minecraft.core.particles.ParticleTypes.GUST_EMITTER_SMALL,center.x,center.y+.2,center.z,3,.6,.2,.6,0);
            player.serverLevel().playSound(null,center.x,center.y,center.z,net.minecraft.sounds.SoundEvents.MACE_SMASH_GROUND,player.getSoundSource(),1,1);
        }
    }
    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {cancel(event.getEntity().getUUID());}
}
