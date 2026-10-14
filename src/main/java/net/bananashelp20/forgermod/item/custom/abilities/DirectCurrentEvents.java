package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.AdditionalAbilities;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.damagesource.DamageTypes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import java.util.*;

@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class DirectCurrentEvents {
    private static final class Circuit {
        final ItemStack stack;final int selected,rank;final ServerLevel level;long expires,nextField,nextParticles;
        final List<LivingEntity> anchors=new ArrayList<>();final Map<UUID,Vec3> previous=new HashMap<>();final Map<UUID,Long> shockReady=new HashMap<>();
        Circuit(ServerPlayer player,ItemStack stack) {this.stack=stack;selected=player.getInventory().selected;rank=Augmentations.level(stack,AdditionalAbilities.DIRECT_CURRENT);level=player.serverLevel();expires=level.getGameTime()+600;}
    }
    private record Barrier(Vec3 from,Vec3 to) {}
    private static final Map<UUID,Circuit> CIRCUITS=new HashMap<>();
    private DirectCurrentEvents() {}
    public static boolean activate(ServerPlayer player,ItemStack stack) {if(active(player)) return false;CIRCUITS.put(player.getUUID(),new Circuit(player,stack));return true;}
    private static boolean tooFar(Circuit circuit) {
        for(int i=0;i<circuit.anchors.size();i++) for(int j=i+1;j<circuit.anchors.size();j++) if(circuit.anchors.get(i).distanceToSqr(circuit.anchors.get(j))>2500) return true;
        return false;
    }
    public static boolean active(ServerPlayer player) {
        var circuit=CIRCUITS.get(player.getUUID());if(circuit==null) return false;
        if(!player.isAlive() || player.isSpectator() || player.getMainHandItem()!=circuit.stack || player.getInventory().selected!=circuit.selected || player.serverLevel()!=circuit.level
            || player.level().getGameTime()>=circuit.expires || !Augmentations.ids(circuit.stack,true).contains(AdditionalAbilities.DIRECT_CURRENT)
            || tooFar(circuit) || circuit.anchors.stream().anyMatch(e->e.level()!=player.level() || !WeaponAbilityTargets.enemy(player,e))) {cancel(player.getUUID());return false;}
        return true;
    }
    public static long remainingTicks(ServerPlayer player) {return active(player)?CIRCUITS.get(player.getUUID()).expires-player.level().getGameTime():0;}
    public static int marked(ServerPlayer player) {return active(player)?CIRCUITS.get(player.getUUID()).anchors.size():0;}
    public static void cancel(UUID owner) {CIRCUITS.remove(owner);WeaponControlLocks.release(owner,AdditionalAbilities.DIRECT_CURRENT);}
    @SubscribeEvent
    public static void landed(LivingDamageEvent.Post event) {
        if(!event.getSource().is(DamageTypes.PLAYER_ATTACK) || !(event.getSource().getEntity() instanceof ServerPlayer player) || !active(player)
            || event.getNewDamage()+event.getReduction(DamageContainer.Reduction.ABSORPTION)<=0 || !WeaponAbilityTargets.enemy(player,event.getEntity())) return;
        var circuit=CIRCUITS.get(player.getUUID());var target=event.getEntity();
        if(circuit.anchors.contains(target) || circuit.anchors.size()>=Math.min(4,circuit.rank+1) || circuit.anchors.stream().anyMatch(e->e.distanceToSqr(target)>2500)) return;
        circuit.anchors.add(target);if(circuit.anchors.size()<2) return;
        long now=player.level().getGameTime();circuit.expires=now+400;
        for(var anchor:circuit.anchors) WeaponControlLocks.acquire(anchor,player.getUUID(),AdditionalAbilities.DIRECT_CURRENT,400);
        if(circuit.anchors.size()==2 || circuit.anchors.size()==3) circuit.nextField=now+20;
        circuit.nextParticles=now;seed(circuit);
    }
    private static AABB bounds(Circuit circuit) {
        var first=circuit.anchors.getFirst();AABB box=first.getBoundingBox();for(var anchor:circuit.anchors) box=box.minmax(anchor.getBoundingBox());return box.inflate(2,3,2);
    }
    private static void seed(Circuit circuit) {for(var e:circuit.level.getEntitiesOfClass(LivingEntity.class,bounds(circuit).inflate(8),LivingEntity::isAlive)) circuit.previous.put(e.getUUID(),e.position());}
    private static double side(Vec3 a,Vec3 b,Vec3 p) {return (b.x-a.x)*(p.z-a.z)-(b.z-a.z)*(p.x-a.x);}
    public static boolean crosses(Vec3 from,Vec3 to,Vec3 a,Vec3 b) {
        if(Math.abs(side(a,b,from))<.000001 && Math.abs(side(a,b,to))<.000001) return false;
        if(side(a,b,from)*side(a,b,to)>0 || side(from,to,a)*side(from,to,b)>0) return false;
        double y=(from.y+to.y)*.5;return y>=Math.min(a.y,b.y)-.5 && y<=Math.max(a.y,b.y)+2.5;
    }
    public static boolean inside(Vec3 point,Vec3 a,Vec3 b,Vec3 c) {
        if(Math.abs(side(a,b,c))<.01 || point.y<Math.min(a.y,Math.min(b.y,c.y))-.5 || point.y>Math.max(a.y,Math.max(b.y,c.y))+2.5) return false;
        double x=side(a,b,point),y=side(b,c,point),z=side(c,a,point);return !(x<-.000001 || y<-.000001 || z<-.000001) || !(x>.000001 || y>.000001 || z>.000001);
    }
    private static void damage(ServerPlayer player,LivingEntity target,float amount) {
        int immunity=target.invulnerableTime;target.invulnerableTime=0;
        try {target.hurt(target==player?player.damageSources().magic():player.damageSources().indirectMagic(player,player),amount);}finally {target.invulnerableTime=Math.max(immunity,target.invulnerableTime);}
    }
    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post event) {
        if(!(event.getEntity() instanceof ServerPlayer player) || !active(player)) return;var circuit=CIRCUITS.get(player.getUUID());if(circuit.anchors.size()<2) return;long now=player.level().getGameTime();
        var occupants=circuit.level.getEntitiesOfClass(LivingEntity.class,bounds(circuit).inflate(8),LivingEntity::isAlive);var seen=new HashSet<UUID>();
        var barriers=new ArrayList<Barrier>();boolean[][] visible=new boolean[4][4];
        for(int i=0;i<circuit.anchors.size();i++) for(int j=i+1;j<circuit.anchors.size();j++) {
            var a=circuit.anchors.get(i);var b=circuit.anchors.get(j);visible[i][j]=a.hasLineOfSight(b);
            if(visible[i][j]) barriers.add(new Barrier(a.position(),b.position()));
        }
        boolean field=circuit.rank>=4 && circuit.anchors.size()>=3 && visible[0][1] && visible[1][2] && visible[0][2];
        boolean fieldPulse=field && now>=circuit.nextField;
        for(var entity:occupants) {
            var id=entity.getUUID();seen.add(id);var old=circuit.previous.getOrDefault(id,new Vec3(entity.xo,entity.yo,entity.zo));
            boolean crossed=false;
            for(var barrier:barriers) if(crosses(old,entity.position(),barrier.from(),barrier.to())) {crossed=true;break;}
            if(crossed && now>=circuit.shockReady.getOrDefault(id,0L)) {damage(player,entity,4);circuit.shockReady.put(id,now+10);}
            if(fieldPulse && inside(entity.position(),circuit.anchors.get(0).position(),circuit.anchors.get(1).position(),circuit.anchors.get(2).position())) damage(player,entity,1+.25f*Math.max(0,circuit.rank-4));
            circuit.previous.put(id,entity.position());
        }
        circuit.previous.keySet().retainAll(seen);circuit.shockReady.keySet().retainAll(seen);if(fieldPulse) circuit.nextField=now+20;
        if(now>=circuit.nextParticles) {
            for(var barrier:barriers) {
                var from=barrier.from().add(0,1,0);var delta=barrier.to().subtract(barrier.from());int samples=Math.min(24,Math.max(2,(int)(delta.length()/2)));
                for(int n=0;n<=samples;n++) {var p=from.add(delta.scale((double)n/samples));circuit.level.sendParticles(net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK,p.x,p.y,p.z,1,0,.2,0,.01);}
            }
            circuit.nextParticles=now+10;
        }
    }
    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {cancel(event.getEntity().getUUID());}
}
