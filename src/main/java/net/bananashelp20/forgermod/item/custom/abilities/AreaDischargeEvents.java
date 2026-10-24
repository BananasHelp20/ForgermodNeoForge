package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.bananashelp20.forgermod.item.custom.ElectriumWeapon;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import java.util.*;

@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class AreaDischargeEvents {
    private static final class Cast {
        final ItemStack stack;final int slot;final ServerLevel level;final long started;final float damage;
        final boolean selfStrike;final int selfStrikeDelay;
        final List<ZeusStrikePlan.Strike<Mob>> strikes;final Set<UUID> hit=new HashSet<>();int next;
        Cast(ServerPlayer player,ItemStack stack,List<ZeusStrikePlan.Strike<Mob>> strikes,float damage){this.stack=stack;slot=player.getInventory().selected;level=player.serverLevel();started=level.getGameTime();this.strikes=strikes;this.damage=damage;selfStrike=!LightningRodEvents.source(player).isEmpty();
            selfStrikeDelay=strikes.stream().mapToInt(ZeusStrikePlan.Strike::delayTicks).max().orElse(0)+10;}
    }
    private static final Map<UUID,Cast> CASTS=new HashMap<>();
    private AreaDischargeEvents(){}
    private static boolean eligible(ServerPlayer player,Mob mob,boolean zeus){
        return mob.isAlive() && !mob.isRemoved() && !mob.isAlliedTo(player)
                && (mob instanceof Enemy || zeus && mob.getTarget()!=null && mob.getTarget().isAlive());
    }
    public static boolean activate(ServerPlayer player,ItemStack stack){
        if(active(player))return false;
        int rank=Augmentations.level(stack,Augmentations.AREA_DISCHARGE);boolean zeus=Augmentations.isSuper(stack,Augmentations.AREA_DISCHARGE,rank);
        if(rank<=0 || stack!=player.getMainHandItem() || !player.isAlive() || player.isSpectator() || WeaponControlLocks.frozen(player))return false;
        int waves=Augmentations.displayRank(stack,Augmentations.AREA_DISCHARGE,rank);double radius=zeus?ZeusStrikePlan.waveRadius(waves):5;
        var initial=AreaDischargeTargets.select(player.serverLevel().getEntitiesOfClass(Mob.class,player.getBoundingBox().inflate(radius)),
                mob->eligible(player,mob,zeus),player::distanceToSqr,radius);
        if(initial.isEmpty())return false;
        float damage=(float)player.getAttributeValue(Attributes.ATTACK_DAMAGE);
        if(!zeus){boolean struck=false;for(var target:initial)struck|=strike(player,target,null,damage*.5F);return struck;}
        var plan=ZeusStrikePlan.select(initial,waves,mob->eligible(player,mob,true),player::distanceToSqr,
                mob->player.serverLevel().getEntitiesOfClass(Mob.class,mob.getBoundingBox().inflate(10)),
                Mob::distanceToSqr,Mob::getUUID,()->ZeusStrikePlan.startsChain(player.getRandom().nextFloat()));
        CASTS.put(player.getUUID(),new Cast(player,stack,plan,damage));
        for(var strike:plan)mark(player,strike.target());tick(player);return true;
    }
    public static boolean active(ServerPlayer player){
        var cast=CASTS.get(player.getUUID());if(cast==null)return false;
        if(player.isAlive() && !player.isSpectator() && player.serverLevel()==cast.level && player.getMainHandItem()==cast.stack
                && player.getInventory().selected==cast.slot && Augmentations.ids(cast.stack,true).contains(Augmentations.AREA_DISCHARGE)
                && (cast.next<cast.strikes.size() || cast.selfStrike))return true;
        cancel(player.getUUID());return false;
    }
    public static void cancel(UUID owner){CASTS.remove(owner);}
    private static void mark(ServerPlayer player,Mob target){
        player.serverLevel().sendParticles(player,ParticleTypes.ELECTRIC_SPARK,true,target.getX(),target.getY()+target.getBbHeight()+.2,target.getZ(),3,.15,.1,.15,.02);
    }
    private static boolean strike(ServerPlayer player,Mob target,Mob previous,float damage){
        if(!target.isAlive() || target.isRemoved() || target.level()!=player.level())return false;
        var bolt=LightningRodEvents.visualStrike(player,target.position());
        int immunity=target.invulnerableTime;target.invulnerableTime=0;
        // The lightning entity is direct; the player remains the damage/kill owner.
        if(!target.hurt(player.damageSources().source(DamageTypes.LIGHTNING_BOLT,bolt,player),damage*ElectriumWeapon.chargeMultiplier(player))){target.invulnerableTime=immunity;return false;}
        ElectriumWeapon.recordLightningHit(target,player);
        if(previous!=null){
            var start=previous.position().add(0,previous.getBbHeight()*.5,0);var end=target.position().add(0,target.getBbHeight()*.5,0);
            for(int point=1;point<8;point++){
                var position=start.lerp(end,point/8.0);player.serverLevel().sendParticles(ParticleTypes.ELECTRIC_SPARK,position.x,position.y,position.z,1,0,0,0,0);
            }
        }
        return true;
    }
    public static void tick(ServerPlayer player){
        if(!active(player))return;var cast=CASTS.get(player.getUUID());long elapsed=cast.level.getGameTime()-cast.started;
        while(cast.next<cast.strikes.size() && cast.strikes.get(cast.next).delayTicks()<=elapsed){
            var planned=cast.strikes.get(cast.next++);
            if((planned.previous()==null || cast.hit.contains(planned.previous().getUUID()))
                    && strike(player,planned.target(),planned.previous(),cast.damage*planned.damageMultiplier()))cast.hit.add(planned.target().getUUID());
        }
        if(cast.next==cast.strikes.size()){
            if(cast.selfStrike && elapsed<cast.selfStrikeDelay)return;
            // Recheck current learned equipment; either valid held rod can supply the final wave.
            if(cast.selfStrike)LightningRodEvents.chargeStrike(player);
            cancel(player.getUUID());return;
        }
        if(elapsed%4==0)for(int i=cast.next;i<cast.strikes.size();i++){
            var target=cast.strikes.get(i).target();if(target.isAlive() && target.level()==cast.level)mark(player,target);
        }
    }
    @SubscribeEvent public static void tick(PlayerTickEvent.Post event){if(event.getEntity() instanceof ServerPlayer player)tick(player);}
}
