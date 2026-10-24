package net.bananashelp20.forgermod.item.custom.abilities;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.augmentation.*;
import net.bananashelp20.forgermod.item.custom.ElectriumWeapon;
import net.bananashelp20.forgermod.mixin.AttackStrengthAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.*;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.*;
import net.minecraft.sounds.*;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import java.util.*;

/** Collision-respecting velocity bursts with a single view snap when selecting each new chain target. */
@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class ThunderclapEvents {
    public static final double AIM_RANGE=10, CHAIN_RANGE=10;
    private static final class Dash {
        final ServerPlayer owner;final ItemStack stack;final int slot,rank;final ServerLevel level;final boolean noGravity;
        final Set<UUID> visited=new HashSet<>();final Deque<Vec3> route=new ArrayDeque<>();Mob target;Vec3 previous;int legTicks,stalled,replans;
        Dash(ServerPlayer player,ItemStack stack,Mob target,int rank){owner=player;this.stack=stack;slot=player.getInventory().selected;
            this.target=target;this.rank=rank;level=player.serverLevel();noGravity=player.isNoGravity();previous=player.position();}
    }
    private static final Map<UUID,Dash> DASHES=new HashMap<>();
    private ThunderclapEvents(){}
    public static double speed(int rank){return 3.5+.1*(Math.clamp(rank,1,4)-1);}
    private static boolean eligible(Mob mob){return mob.isAlive() && !mob.isRemoved() && !mob.isSpectator();}
    private static boolean clear(ServerLevel level,Vec3 from,Vec3 to,ServerPlayer player){
        return level.clip(new ClipContext(from,to,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,player)).getType()==HitResult.Type.MISS;
    }
    private static Mob aimed(ServerPlayer player){
        var entity=WeaponAbilityTargets.aimedLoadedEntity(player,AIM_RANGE,target->target instanceof Mob mob && eligible(mob));
        return entity instanceof Mob mob?mob:null;
    }
    public static boolean activate(ServerPlayer player,ItemStack stack){
        if(active(player) || stack!=player.getMainHandItem() || !(stack.getItem() instanceof ElectriumWeapon weapon) || weapon.isAxe()
                || !player.isAlive() || player.isSpectator() || player.isPassenger() || player.isFallFlying() || WeaponControlLocks.frozen(player))return false;
        int rank=Augmentations.level(stack,AdditionalAbilities.THUNDERCLAP);if(rank<1)return false;
        Mob target=aimed(player);if(target==null)return false;
        var dash=new Dash(player,stack,target,rank);DASHES.put(player.getUUID(),dash);
        player.setNoGravity(true);player.fallDistance=0;
        dash.level.playSound(null,player.blockPosition(),SoundEvents.TRIDENT_THROW.value(),SoundSource.PLAYERS,.55F,1.7F);
        tick(player);return true;
    }
    public static boolean active(ServerPlayer player){
        var dash=DASHES.get(player.getUUID());if(dash==null)return false;
        if(player.isAlive() && !player.isRemoved() && !player.isSpectator() && !player.isPassenger() && !player.isFallFlying()
                && !WeaponControlLocks.frozen(player) && player.serverLevel()==dash.level && player.getMainHandItem()==dash.stack
                && player.getInventory().selected==dash.slot && Augmentations.level(dash.stack,AdditionalAbilities.THUNDERCLAP)>0
                && dash.target.level()==dash.level && (eligible(dash.target) || dash.visited.contains(dash.target.getUUID())) && dash.legTicks<40 && dash.stalled<8)return true;
        cancel(player.getUUID());return false;
    }
    public static void cancel(UUID owner){
        var dash=DASHES.remove(owner);if(dash==null)return;
        dash.owner.setNoGravity(dash.noGravity);motion(dash.owner,Vec3.ZERO);
    }
    private static void motion(ServerPlayer player,Vec3 velocity){
        player.setDeltaMovement(velocity);player.hasImpulse=true;player.hurtMarked=true;
        player.connection.send(new ClientboundSetEntityMotionPacket(player));
    }
    private static Vec3 destination(ServerPlayer player,Mob target){
        var box=target.getBoundingBox();var now=player.position();
        double x=Math.clamp(now.x,box.minX,box.maxX),z=Math.clamp(now.z,box.minZ,box.maxZ);
        var toward=new Vec3(x-now.x,0,z-now.z);if(toward.lengthSqr()>.000001){var gap=toward.normalize().scale(player.getBbWidth()*.5+.35);x-=gap.x;z-=gap.z;}
        double y=Math.clamp(now.y,box.minY-player.getBbHeight()+.2,box.maxY-.2);
        return new Vec3(x,y,z);
    }
    public static double bodyDistanceSqr(AABB first,AABB second){
        double x=Math.max(0,Math.max(first.minX-second.maxX,second.minX-first.maxX));
        double y=Math.max(0,Math.max(first.minY-second.maxY,second.minY-first.maxY));
        double z=Math.max(0,Math.max(first.minZ-second.maxZ,second.minZ-first.maxZ));return x*x+y*y+z*z;
    }
    private static boolean visibleBody(ServerPlayer player,Mob target){return visibleBody(player,target,player.getEyePosition());}
    private static boolean visibleBody(ServerPlayer player,Mob target,Vec3 eye){
        var box=target.getBoundingBox();
        var point=new Vec3(Math.clamp(eye.x,box.minX,box.maxX),Math.clamp(eye.y,box.minY,box.maxY),Math.clamp(eye.z,box.minZ,box.maxZ));
        return clear(player.serverLevel(),eye,point,player) || clear(player.serverLevel(),eye,target.getEyePosition(),player);
    }
    /** First entry into the rounded body-distance envelope, even when the endpoint passes beyond it. */
    public static double contactFraction(AABB body,AABB target,Vec3 movement,double radius){
        double squared=radius*radius;
        if(bodyDistanceSqr(body,target)<=squared)return 0;
        if(movement.lengthSqr()<1E-12 || bodyDistanceSqr(body.expandTowards(movement),target)>squared)return -1;
        // Distance between translated boxes is convex along a line; find its minimum before the entry root.
        double low=0,high=1;
        for(int i=0;i<32;i++){
            double left=(2*low+high)/3,right=(low+2*high)/3;
            if(bodyDistanceSqr(body.move(movement.scale(left)),target)<=bodyDistanceSqr(body.move(movement.scale(right)),target))high=right;else low=left;
        }
        double closest=(low+high)*.5;
        if(bodyDistanceSqr(body.move(movement),target)<bodyDistanceSqr(body.move(movement.scale(closest)),target))closest=1;
        if(bodyDistanceSqr(body.move(movement.scale(closest)),target)>squared)return -1;
        low=0;high=closest;
        for(int i=0;i<24;i++){
            double middle=(low+high)*.5;
            if(bodyDistanceSqr(body.move(movement.scale(middle)),target)<=squared)high=middle;else low=middle;
        }
        return high;
    }
    private static Vec3 arrivalVelocity(ServerPlayer player,Mob target,Vec3 velocity){
        if(bodyDistanceSqr(player.getBoundingBox(),target.getBoundingBox())<=.81)return velocity;
        double contact=contactFraction(player.getBoundingBox(),target.getBoundingBox(),velocity,.9);
        return contact<0?velocity:velocity.scale(contact);
    }
    private static boolean crossedContact(Dash dash,Vec3 previous,Vec3 now){
        Vec3 movement=now.subtract(previous);var body=dash.owner.getBoundingBox().move(previous.subtract(now));
        double contact=contactFraction(body,dash.target.getBoundingBox(),movement,1);
        if(contact<0)return false;
        var step=movement.scale(contact);var eye=previous.add(step).add(0,dash.owner.getEyeHeight(),0);
        return visibleBody(dash.owner,dash.target,eye) && passable(dash,body,step);
    }
    public static void tick(ServerPlayer player){
        if(!active(player))return;var dash=DASHES.get(player.getUUID());dash.legTicks++;
        var now=player.position();var previous=dash.previous;double moved=now.distanceToSqr(previous);
        if(moved>64){cancel(player.getUUID());return;} // An external teleport must not become a dash hit.
        if(moved>.000001){net.bananashelp20.forgermod.particle.LightningArcParticles.emit(dash.level,previous.add(0,.9,0),now.add(0,.9,0),player.getId(),2);dash.stalled=0;}
        else if(dash.legTicks>1)dash.stalled++;
        dash.previous=now;player.fallDistance=0;
        if(dash.visited.contains(dash.target.getUUID())){
            // A confirmed hit ends this leg. Never chase its victim again after knockback or death.
            Mob struck=dash.target;
            Mob next=dash.level.getEntitiesOfClass(Mob.class,struck.getBoundingBox().inflate(CHAIN_RANGE),mob->eligible(mob)
                    && !dash.visited.contains(mob.getUUID()) && struck.distanceToSqr(mob)<=CHAIN_RANGE*CHAIN_RANGE
                    && visibleBody(player,mob))
                    .stream().min(Comparator.comparingDouble(struck::distanceToSqr)).orElse(null);
            if(next==null){cancel(player.getUUID());return;}
            player.lookAt(net.minecraft.commands.arguments.EntityAnchorArgument.Anchor.EYES,next.getEyePosition());
            dash.target=next;dash.route.clear();dash.legTicks=0;dash.stalled=0;dash.replans=0;previous=now;
        }
        if(!dash.level.getWorldBorder().isWithinBounds(dash.target.blockPosition()) || bodyDistanceSqr(player.getBoundingBox(),dash.target.getBoundingBox())>24*24){cancel(player.getUUID());return;}
        boolean inRange=bodyDistanceSqr(player.getBoundingBox(),dash.target.getBoundingBox())<=1.00001 && visibleBody(player,dash.target);
        if(inRange || moved>.000001 && crossedContact(dash,previous,now)){
            motion(player,Vec3.ZERO);dash.route.clear();dash.stalled=0;
            // A rejected native attack must not repeatedly ram or swing at the same body.
            if(!strike(player,dash,dash.target)){cancel(player.getUUID());return;}
            if(DASHES.get(player.getUUID())!=dash)return;
            // The next tick immediately launches the next hop, after this confirmed impact packet.
            dash.visited.add(dash.target.getUUID());return;
        }
        Vec3 point=destination(player,dash.target);
        while(!dash.route.isEmpty() && dash.route.peekFirst().distanceToSqr(now)<.01)dash.route.removeFirst();
        Vec3 direction=(dash.route.isEmpty()?point:dash.route.peekFirst()).subtract(now);
        double length=direction.length();if(length<.000001){motion(player,Vec3.ZERO);return;}
        Vec3 velocity=arrivalVelocity(player,dash.target,direction.scale(Math.min(speed(dash.rank),length)/length));
        if(!passable(dash,player.getBoundingBox(),velocity)){
            // Clear one-block ledges in three real movement legs: up, across, then down.
            // The complete body corridor must be loaded and clear, including the landing.
            dash.route.clear();
            if(++dash.replans>3 || !overLedge(dash,point) && !routeAround(dash)){cancel(player.getUUID());return;}
            direction=dash.route.peekFirst().subtract(now);length=direction.length();
            velocity=arrivalVelocity(player,dash.target,direction.scale(Math.min(speed(dash.rank),length)/length));
        }
        motion(player,velocity);
    }
    private static boolean passable(Dash dash,AABB box,Vec3 movement){
        var swept=box.expandTowards(movement).deflate(.0001);
        return dash.level.hasChunksAt(BlockPos.containing(swept.minX,swept.minY,swept.minZ),BlockPos.containing(swept.maxX,swept.maxY,swept.maxZ))
                && net.minecraft.world.entity.Entity.collideBoundingBox(dash.owner,movement,box,dash.level,
                    dash.level.getEntityCollisions(dash.owner,box.expandTowards(movement))).distanceToSqr(movement)<.00000001;
    }
    private static boolean overLedge(Dash dash,Vec3 destination){
        var origin=dash.owner.position();var box=dash.owner.getBoundingBox();
        double base=Math.max(origin.y,destination.y);
        for(double lift=.5;lift<=1.25;lift+=.25){
            var top=new Vec3(origin.x,base+lift,origin.z);var across=new Vec3(destination.x,top.y,destination.z);
            var up=top.subtract(origin);var horizontal=across.subtract(top);
            if(passable(dash,box,up) && passable(dash,box.move(up),horizontal)
                    && passable(dash,box.move(across.subtract(origin)),destination.subtract(across))){
                dash.route.addLast(top);dash.route.addLast(across);dash.stalled=0;return true;
            }
        }
        return false;
    }
    private record Step(int x,int y,int z){Vec3 offset(){return new Vec3(x*.5,y*.5,z*.5);}}
    private record Visit(Step step,double cost,double estimate){}
    private static boolean routeAround(Dash dash){
        var origin=dash.owner.position();var box=dash.owner.getBoundingBox();var target=dash.target.getBoundingBox();
        var start=new Step(0,0,0);var open=new PriorityQueue<Visit>(Comparator.comparingDouble(Visit::estimate));
        var scores=new HashMap<Step,Double>();var parents=new HashMap<Step,Step>();scores.put(start,0.0);open.add(new Visit(start,0,0));
        int lowY=Math.max(-28,Math.min(-8,(int)Math.floor((target.minY-box.minY)*2)-4));
        int highY=Math.min(28,Math.max(8,(int)Math.ceil((target.minY-box.minY)*2)+4));
        int[][] directions={{1,0,0},{-1,0,0},{0,0,1},{0,0,-1},{0,1,0},{0,-1,0}};int expanded=0;
        while(!open.isEmpty() && expanded++<512){
            var visit=open.remove();var step=visit.step();if(visit.cost()>scores.getOrDefault(step,Double.MAX_VALUE))continue;
            var offset=step.offset();var body=box.move(offset);
            if(bodyDistanceSqr(body,target)<=.81 && visibleBody(dash.owner,dash.target,origin.add(offset).add(0,dash.owner.getEyeHeight(),0))){
                var path=new ArrayList<Vec3>();var cursor=step;while(!cursor.equals(start)){path.add(origin.add(cursor.offset()));cursor=parents.get(cursor);}Collections.reverse(path);
                var from=origin;int first=0;
                while(first<path.size()){
                    int last=first;while(last+1<path.size() && passable(dash,box.move(from.subtract(origin)),path.get(last+1).subtract(from)))last++;
                    dash.route.addLast(path.get(last));from=path.get(last);first=last+1;
                }
                dash.stalled=0;return !dash.route.isEmpty();
            }
            for(var direction:directions){
                var next=new Step(step.x()+direction[0],step.y()+direction[1],step.z()+direction[2]);
                if(Math.abs(next.x())>28 || Math.abs(next.z())>28 || next.y()<lowY || next.y()>highY)continue;
                var delta=new Vec3(direction[0]*.5,direction[1]*.5,direction[2]*.5);var nextBody=box.move(next.offset());
                if(!dash.level.getWorldBorder().isWithinBounds(BlockPos.containing(origin.add(next.offset()))) || !passable(dash,body,delta)
                        || !dash.level.noCollision(dash.owner,nextBody.deflate(.0001)))continue;
                double cost=visit.cost()+.5;if(cost>=scores.getOrDefault(next,Double.MAX_VALUE))continue;
                scores.put(next,cost);parents.put(next,step);double remaining=Math.max(0,Math.sqrt(bodyDistanceSqr(nextBody,target))-.9);
                open.add(new Visit(next,cost,cost+remaining*1.25));
            }
        }
        return false;
    }
    private static boolean strike(ServerPlayer player,Dash dash,Mob target){
        float damage=(float)player.getAttributeValue(Attributes.ATTACK_DAMAGE),health=target.getHealth()+target.getAbsorptionAmount();
        var access=(AttackStrengthAccess)player;int previous=access.forgermod$getAttackStrengthTicker();
        int charged=(int)Math.ceil(player.getCurrentItemAttackStrengthDelay())+1;boolean sprinting=player.isSprinting();
        int immunity=target.invulnerableTime;target.invulnerableTime=0;
        access.forgermod$setAttackStrengthTicker(charged);player.setSprinting(true); // A focused strike, without vanilla's area sweep.
        try{player.swing(InteractionHand.MAIN_HAND,true);player.attack(target);}
        finally{
            player.setSprinting(sprinting);
            if(access.forgermod$getAttackStrengthTicker()==charged)access.forgermod$setAttackStrengthTicker(previous);
        }
        boolean landed=target.getHealth()+target.getAbsorptionAmount()<health;
        if(!landed){target.invulnerableTime=immunity;return false;}
        var bolt=LightningRodEvents.visualStrike(player,target.position());
        if(landed && target.isAlive()){
            int meleeImmunity=target.invulnerableTime;target.invulnerableTime=0;
            if(target.hurt(player.damageSources().source(DamageTypes.LIGHTNING_BOLT,bolt,player),damage*.5F*ElectriumWeapon.chargeMultiplier(player)))
                ElectriumWeapon.recordLightningHit(target,player);
            else target.invulnerableTime=meleeImmunity;
        }
        var center=target.getBoundingBox().getCenter();
        dash.level.sendParticles(ParticleTypes.FLASH,center.x,center.y,center.z,1,0,0,0,0);
        dash.level.sendParticles(ParticleTypes.ELECTRIC_SPARK,center.x,center.y,center.z,32,.4,.65,.4,.15);
        net.bananashelp20.forgermod.particle.LightningArcParticles.emit(dash.level,center.add(0,2.5,0),center,-1,4);
        dash.level.playSound(null,target.blockPosition(),SoundEvents.LIGHTNING_BOLT_IMPACT,SoundSource.PLAYERS,.75F,1.5F);return true;
    }
    @SubscribeEvent public static void playerTick(PlayerTickEvent.Post event){if(event.getEntity() instanceof ServerPlayer player)tick(player);}
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent event){cancel(event.getEntity().getUUID());}
    @SubscribeEvent public static void dimension(PlayerEvent.PlayerChangedDimensionEvent event){cancel(event.getEntity().getUUID());}
}
