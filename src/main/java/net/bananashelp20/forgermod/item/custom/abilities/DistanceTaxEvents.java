package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.augmentation.RecommendedAbilities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class DistanceTaxEvents {
    private record Mark(ItemStack stack,int selected,LivingEntity target,long due,int rank) {}
    private static final Map<UUID,Mark> MARKS=new HashMap<>();
    private DistanceTaxEvents() {}
    public static float damage(double distance,int rank) { return (float)Math.min(40,Math.max(0,distance)*(.25+.25*Math.clamp(rank,1,4))); }
    private static boolean valid(ServerPlayer player,Mark mark) {
        return player.isAlive() && !player.isSpectator() && player.getMainHandItem()==mark.stack()
                && player.getInventory().selected==mark.selected() && mark.target().isAlive()
                && mark.target().level()==player.level() && WeaponAbilityTargets.enemy(player,mark.target())
                && Augmentations.hasPassive(mark.stack(),RecommendedAbilities.DISTANCE_TAX);
    }
    @SubscribeEvent
    public static void onHit(LivingDamageEvent.Post event) {
        if(event.getNewDamage()<=0 || !event.getSource().is(DamageTypes.PLAYER_ATTACK)
                || !(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        var stack=player.getMainHandItem();
        if(!player.isAlive() || player.isSpectator() || !Augmentations.hasPassive(stack,RecommendedAbilities.DISTANCE_TAX)
                || !WeaponAbilityTargets.enemy(player,event.getEntity())) return;
        var old=MARKS.get(player.getUUID());
        if(old!=null && valid(player,old) && old.target()==event.getEntity()) return;
        MARKS.put(player.getUUID(),new Mark(stack,player.getInventory().selected,event.getEntity(),player.level().getGameTime()+200,
                Augmentations.level(stack,RecommendedAbilities.DISTANCE_TAX)));
    }
    @SubscribeEvent
    public static void onTick(PlayerTickEvent.Post event) {
        if(!(event.getEntity() instanceof ServerPlayer player)) return;
        var mark=MARKS.get(player.getUUID()); if(mark==null) return;
        if(!valid(player,mark)) { MARKS.remove(player.getUUID()); return; }
        var target=mark.target();
        if(player.level().getGameTime()>=mark.due()) {
            MARKS.remove(player.getUUID());
            float damage=damage(player.distanceTo(target),mark.rank());
            if(damage>0) {
                int immunity=target.invulnerableTime; target.invulnerableTime=0;
                // Player-owned magic cannot re-mark or recursively trigger melee-hit abilities.
                target.hurt(player.damageSources().indirectMagic(player,player),damage);
                target.invulnerableTime=Math.max(immunity,target.invulnerableTime);
                player.serverLevel().sendParticles(ParticleTypes.PORTAL,target.getX(),target.getY()+1,target.getZ(),20,.3,.5,.3,.3);
            }
        } else if(player.level().getGameTime()%10==0)
            player.serverLevel().sendParticles(ParticleTypes.REVERSE_PORTAL,target.getX(),target.getY()+1,target.getZ(),3,.3,.5,.3,.01);
    }
    @SubscribeEvent
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) { MARKS.remove(event.getEntity().getUUID()); }
}
