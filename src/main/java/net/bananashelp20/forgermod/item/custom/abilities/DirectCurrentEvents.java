package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.AdditionalAbilities;
import net.bananashelp20.forgermod.augmentation.Augmentations;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import java.util.*;

@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class DirectCurrentEvents {
    public static final ResourceKey<DamageType> DAMAGE = ResourceKey.create(Registries.DAMAGE_TYPE,
            ResourceLocation.fromNamespaceAndPath(ForgerMod.MOD_ID,"direct_current"));
    private static final String FIELD_LOCK = AdditionalAbilities.DIRECT_CURRENT + ".field";
    private static final class Circuit {
        final ItemStack stack;final int selected,rank;final ServerLevel level;long expires,nextField,nextParticles;
        boolean connected;
        final List<LivingEntity> anchors=new ArrayList<>();final Map<UUID,Vec3> previous=new HashMap<>();final Map<UUID,Long> shockReady=new HashMap<>();
        Circuit(ServerPlayer player,ItemStack stack) {this.stack=stack;selected=player.getInventory().selected;rank=Augmentations.level(stack,AdditionalAbilities.DIRECT_CURRENT);level=player.serverLevel();expires=level.getGameTime()+600;}
    }
    private record Barrier(Vec3 from,Vec3 to) {}
    private static final Map<UUID,Circuit> CIRCUITS=new HashMap<>();
    private DirectCurrentEvents() {}
    private static boolean validTarget(ServerPlayer player,LivingEntity target) {
        return target!=player && target.isAlive() && !target.isRemoved() && (target instanceof net.minecraft.world.entity.Mob || WeaponAbilityTargets.enemy(player,target));
    }
    public static boolean activate(ServerPlayer player,ItemStack stack) {
        if(stack!=player.getMainHandItem() || !player.isAlive() || player.isSpectator() || Augmentations.level(stack,AdditionalAbilities.DIRECT_CURRENT)<=0)return false;
        var aimed=WeaponAbilityTargets.aimedLoadedEntity(player,50,e->e instanceof LivingEntity living && validTarget(player,living));if(!(aimed instanceof LivingEntity target))return false;
        boolean running=active(player);var circuit=running?CIRCUITS.get(player.getUUID()):new Circuit(player,stack);
        if(circuit.anchors.contains(target) || circuit.anchors.size()>=Math.min(4,circuit.rank+1) || circuit.anchors.stream().anyMatch(e->e.distanceToSqr(target)>2500))return false;
        if(!running)CIRCUITS.put(player.getUUID(),circuit);
        circuit.anchors.add(target);if(circuit.anchors.size()<2)return true;
        long now=player.level().getGameTime();
        if(!circuit.connected){circuit.connected=true;circuit.expires=now+400;circuit.nextField=now+20;}
        for(var anchor:circuit.anchors)WeaponControlLocks.acquire(anchor,player.getUUID(),AdditionalAbilities.DIRECT_CURRENT,(int)(circuit.expires-now));
        circuit.nextParticles=now;seed(circuit);return true;
    }
    private static boolean tooFar(Circuit circuit) {
        for(int i=0;i<circuit.anchors.size();i++) for(int j=i+1;j<circuit.anchors.size();j++) if(circuit.anchors.get(i).distanceToSqr(circuit.anchors.get(j))>2500) return true;
        return false;
    }
    public static boolean active(ServerPlayer player) {
        var circuit=CIRCUITS.get(player.getUUID());if(circuit==null) return false;
        if(!player.isAlive() || player.isSpectator() || player.getMainHandItem()!=circuit.stack || player.getInventory().selected!=circuit.selected || player.serverLevel()!=circuit.level
            || player.level().getGameTime()>=circuit.expires || !Augmentations.ids(circuit.stack,true).contains(AdditionalAbilities.DIRECT_CURRENT)
            || tooFar(circuit) || circuit.anchors.stream().anyMatch(e->e.level()!=player.level() || !validTarget(player,e))) {cancel(player.getUUID());return false;}
        return true;
    }
    public static long remainingTicks(ServerPlayer player) {return active(player)?CIRCUITS.get(player.getUUID()).expires-player.level().getGameTime():0;}
    public static int marked(ServerPlayer player) {return active(player)?CIRCUITS.get(player.getUUID()).anchors.size():0;}
    public static void cancel(UUID owner) {CIRCUITS.remove(owner);WeaponControlLocks.release(owner,AdditionalAbilities.DIRECT_CURRENT);WeaponControlLocks.release(owner,FIELD_LOCK);}
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
    public static boolean inside(Vec3 point,List<Vec3> polygon){
        if(polygon.size()<3)return false;
        double minY=Double.POSITIVE_INFINITY,maxY=Double.NEGATIVE_INFINITY,area=0;boolean contained=false;
        for(int i=0,j=polygon.size()-1;i<polygon.size();j=i++){
            var a=polygon.get(j);var b=polygon.get(i);var origin=polygon.getFirst();minY=Math.min(minY,b.y);maxY=Math.max(maxY,b.y);area+=(a.x-origin.x)*(b.z-origin.z)-(b.x-origin.x)*(a.z-origin.z);
            if((a.z>point.z)!=(b.z>point.z) && point.x<(b.x-a.x)*(point.z-a.z)/(b.z-a.z)+a.x)contained=!contained;
        }
        return Math.abs(area)>.01 && contained && point.y>=minY-.5 && point.y<=maxY+2.5;
    }
    /** A simple boundary through every mark, including a fourth corner inside the other three. */
    public static List<Vec3> fieldPolygon(List<Vec3> points){
        var ordered=new ArrayList<>(new LinkedHashSet<>(points));if(ordered.size()<3)return List.copyOf(ordered);
        var center=new Vec3(ordered.stream().mapToDouble(p->p.x).average().orElse(0),0,ordered.stream().mapToDouble(p->p.z).average().orElse(0));
        ordered.sort(Comparator.comparingDouble((Vec3 point)->Math.atan2(point.z-center.z,point.x-center.x))
                .thenComparingDouble(point->(point.x-center.x)*(point.x-center.x)+(point.z-center.z)*(point.z-center.z)));
        return List.copyOf(ordered);
    }
    private static boolean nonDegenerate(List<Vec3> polygon){
        if(polygon.size()<3)return false;double area=0;var origin=polygon.getFirst();
        for(int i=1;i<polygon.size()-1;i++)area+=side(origin,polygon.get(i),polygon.get(i+1));return Math.abs(area)>.01;
    }
    private static void showInterior(Circuit circuit,List<Vec3> polygon){
        double minX=polygon.stream().mapToDouble(p->p.x).min().orElse(0),maxX=polygon.stream().mapToDouble(p->p.x).max().orElse(0);
        double minZ=polygon.stream().mapToDouble(p->p.z).min().orElse(0),maxZ=polygon.stream().mapToDouble(p->p.z).max().orElse(0);
        double y=polygon.stream().mapToDouble(p->p.y).min().orElse(0)+.08;
        // Bounded fill sampling: large fields never create one particle per world block.
        for(int x=0;x<6;x++)for(int z=0;z<6;z++){
            var point=new Vec3(minX+(x+.5)*(maxX-minX)/6,y,minZ+(z+.5)*(maxZ-minZ)/6);
            if(inside(point,polygon))circuit.level.sendParticles(net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK,point.x,point.y,point.z,1,0,.03,0,.01);
        }
    }
    /** A narrow electrical curtain intersects the entity's body, even without movement. */
    private static boolean touching(LivingEntity entity,Barrier barrier) {
        var a=barrier.from();var b=barrier.to();var body=entity.getBoundingBox();
        if(body.maxY<Math.min(a.y,b.y)-.5 || body.minY>Math.max(a.y,b.y)+2.5) return false;
        var horizontal=new AABB(body.minX-.15,-1,body.minZ-.15,body.maxX+.15,1,body.maxZ+.15);
        var from=new Vec3(a.x,0,a.z);var to=new Vec3(b.x,0,b.z);
        return horizontal.contains(from) || horizontal.contains(to) || horizontal.clip(from,to).isPresent();
    }
    private static void damage(ServerPlayer player,LivingEntity target,float amount) {
        int immunity=target.invulnerableTime;target.invulnerableTime=0;
        var type=player.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(DAMAGE);
        var source=target==player?new DamageSource(type):new DamageSource(type,player);
        try {target.hurt(source,amount);}finally {target.invulnerableTime=Math.max(immunity,target.invulnerableTime);}
    }
    @SubscribeEvent
    public static void tick(PlayerTickEvent.Post event) {
        if(!(event.getEntity() instanceof ServerPlayer player) || !active(player)) return;var circuit=CIRCUITS.get(player.getUUID());if(circuit.anchors.size()<2) return;long now=player.level().getGameTime();
        var occupants=circuit.level.getEntitiesOfClass(LivingEntity.class,bounds(circuit).inflate(8),LivingEntity::isAlive);var seen=new HashSet<UUID>();
        var barriers=new ArrayList<Barrier>();var polygon=fieldPolygon(circuit.anchors.stream().map(LivingEntity::position).toList());boolean closed=circuit.rank>=4 && circuit.anchors.size()>=3;
        boolean field=closed && nonDegenerate(polygon);
        var endpoints=closed?polygon.stream().map(point->circuit.anchors.stream().filter(anchor->anchor.position().equals(point)).findFirst().orElseThrow()).toList():circuit.anchors;
        int edges=closed?endpoints.size():endpoints.size()-1;boolean unobstructed=true;
        for(int i=0;i<edges;i++){
            var a=endpoints.get(i);var b=endpoints.get((i+1)%endpoints.size());boolean visible=a.hasLineOfSight(b);unobstructed&=visible;
            if(visible)barriers.add(new Barrier(a.position(),b.position()));
        }
        field&=unobstructed;
        boolean fieldPulse=field && now>=circuit.nextField;
        for(var entity:occupants) {
            if(circuit.anchors.contains(entity) || !validTarget(player,entity))continue;
            var id=entity.getUUID();seen.add(id);var old=circuit.previous.getOrDefault(id,new Vec3(entity.xo,entity.yo,entity.zo));
            boolean contacted=false;
            for(var barrier:barriers) if(crosses(old,entity.position(),barrier.from(),barrier.to())
                || !circuit.anchors.contains(entity) && touching(entity,barrier)) {contacted=true;break;}
            boolean withinField=field && inside(entity.position(),polygon);
            if((field?fieldPulse && (contacted || withinField):contacted) && now>=circuit.shockReady.getOrDefault(id,0L)) {
                damage(player,entity,field?1+.25F*Math.max(0,circuit.rank-4):4);
                if(field)WeaponControlLocks.acquire(entity,player.getUUID(),FIELD_LOCK,10);
                circuit.shockReady.put(id,now+(field?20:10));
            }
            circuit.previous.put(id,entity.position());
        }
        circuit.previous.keySet().retainAll(seen);circuit.shockReady.keySet().retainAll(seen);if(fieldPulse) circuit.nextField=now+20;
        if(now>=circuit.nextParticles) {
            for(var barrier:barriers) {
                var from=barrier.from().add(0,1,0);var delta=barrier.to().subtract(barrier.from());int samples=Math.min(24,Math.max(2,(int)(delta.length()/2)));
                for(int n=0;n<=samples;n++) {var p=from.add(delta.scale((double)n/samples));circuit.level.sendParticles(net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK,p.x,p.y,p.z,1,0,.2,0,.01);}
            }
            if(field)showInterior(circuit,polygon);
            circuit.nextParticles=now+10;
        }
    }
    @SubscribeEvent
    public static void logout(PlayerEvent.PlayerLoggedOutEvent event) {cancel(event.getEntity().getUUID());}
}
