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
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.*;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.joml.Vector3f;
import java.util.*;

/** Collision-respecting velocity bursts. Neither player position nor view angles are assigned. */
@EventBusSubscriber(modid=ForgerMod.MOD_ID)
public final class ThunderclapEvents {
    public static final double AIM_RANGE=20, CHAIN_RANGE=10;
    private static final DustParticleOptions GOLD=new DustParticleOptions(new Vector3f(1,.78F,.15F),1.1F);
    private static final DustParticleOptions WHITE=new DustParticleOptions(new Vector3f(1,1,.9F),.75F);
    private static final class Dash {
        final ServerPlayer owner;final ItemStack stack;final int slot,rank;final ServerLevel level;final boolean noGravity;
        final Set<UUID> visited=new HashSet<>();Mob target;Vec3 previous;int legTicks,stalled;
        Dash(ServerPlayer player,ItemStack stack,Mob target,int rank){owner=player;this.stack=stack;slot=player.getInventory().selected;
            this.target=target;this.rank=rank;level=player.serverLevel();noGravity=player.isNoGravity();previous=player.position();}
    }
    private static final Map<UUID,Dash> DASHES=new HashMap<>();
    private ThunderclapEvents(){}
    public static double speed(int rank){return 2.4+.25*(Math.clamp(rank,1,4)-1);}
    private static boolean eligible(Mob mob){return mob.isAlive() && !mob.isRemoved() && !mob.isSpectator();}
    private static boolean clear(ServerLevel level,Vec3 from,Vec3 to,ServerPlayer player){
        return level.clip(new ClipContext(from,to,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,player)).getType()==HitResult.Type.MISS;
    }
    private static Mob aimed(ServerPlayer player){
        Vec3 from=player.getEyePosition(),to=from.add(player.getLookAngle().scale(AIM_RANGE));
        var wall=player.level().clip(new ClipContext(from,to,ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,player));
        if(wall.getType()!=HitResult.Type.MISS)to=wall.getLocation();
        var hit=ProjectileUtil.getEntityHitResult(player,from,to,player.getBoundingBox().expandTowards(to.subtract(from)).inflate(1),
                entity->entity instanceof Mob mob && eligible(mob),from.distanceToSqr(to));
        return hit==null?null:(Mob)hit.getEntity();
    }
    public static boolean activate(ServerPlayer player,ItemStack stack){
        if(active(player) || stack!=player.getMainHandItem() || !(stack.getItem() instanceof ElectriumWeapon weapon) || weapon.isAxe()
                || !player.isAlive() || player.isSpectator() || player.isPassenger() || player.isFallFlying() || WeaponControlLocks.frozen(player))return false;
        int rank=Augmentations.level(stack,AdditionalAbilities.THUNDERCLAP);if(rank<1)return false;
        Mob target=aimed(player);if(target==null)return false;
        var dash=new Dash(player,stack,target,rank);DASHES.put(player.getUUID(),dash);
        player.setNoGravity(true);player.fallDistance=0;
        dash.level.playSound(null,player.blockPosition(),SoundEvents.TRIDENT_THUNDER.value(),SoundSource.PLAYERS,.55F,1.6F);
        tick(player);return true;
    }
    public static boolean active(ServerPlayer player){
        var dash=DASHES.get(player.getUUID());if(dash==null)return false;
        if(player.isAlive() && !player.isRemoved() && !player.isSpectator() && !player.isPassenger() && !player.isFallFlying()
                && !WeaponControlLocks.frozen(player) && player.serverLevel()==dash.level && player.getMainHandItem()==dash.stack
                && player.getInventory().selected==dash.slot && Augmentations.level(dash.stack,AdditionalAbilities.THUNDERCLAP)>0
                && dash.target.level()==dash.level && eligible(dash.target) && dash.legTicks<40 && dash.stalled<8)return true;
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
        var delta=target.position().subtract(player.position());var horizontal=new Vec3(delta.x,0,delta.z);
        if(horizontal.lengthSqr()<.000001)horizontal=new Vec3(0,0,1);
        double gap=target.getBbWidth()*.5+player.getBbWidth()*.5+.4;
        return target.position().subtract(horizontal.normalize().scale(gap));
    }
    public static void tick(ServerPlayer player){
        if(!active(player))return;var dash=DASHES.get(player.getUUID());dash.legTicks++;
        var now=player.position();double moved=now.distanceToSqr(dash.previous);
        if(moved>64){cancel(player.getUUID());return;} // An external teleport must not become a dash hit.
        if(moved>.0001){trail(dash.level,dash.previous.add(0,.9,0),now.add(0,.9,0),player.tickCount);dash.stalled=0;}
        else if(dash.legTicks>1)dash.stalled++;
        dash.previous=now;player.fallDistance=0;
        Vec3 point=destination(player,dash.target),direction=point.subtract(now);
        if(!dash.level.getWorldBorder().isWithinBounds(dash.target.blockPosition()) || player.distanceToSqr(dash.target)>24*24
                || !player.hasLineOfSight(dash.target)){cancel(player.getUUID());return;}
        if(direction.lengthSqr()<=.35*.35 && player.canInteractWithEntity(dash.target,0)){
            motion(player,Vec3.ZERO);Mob struck=dash.target;dash.visited.add(struck.getUUID());
            strike(player,dash,struck);
            if(DASHES.get(player.getUUID())!=dash)return;
            if(!player.isAlive() || player.getMainHandItem()!=dash.stack || dash.stack.isEmpty()){cancel(player.getUUID());return;}
            // Each hop is measured from the last victim, not from the caster's original position.
            Mob next=dash.level.getEntitiesOfClass(Mob.class,struck.getBoundingBox().inflate(CHAIN_RANGE),mob->eligible(mob)
                    && !dash.visited.contains(mob.getUUID()) && struck.distanceToSqr(mob)<=CHAIN_RANGE*CHAIN_RANGE
                    && player.hasLineOfSight(mob) && clear(dash.level,struck.getEyePosition(),mob.getEyePosition(),player))
                    .stream().min(Comparator.comparingDouble(struck::distanceToSqr)).orElse(null);
            if(next==null){cancel(player.getUUID());return;}
            dash.target=next;dash.legTicks=0;dash.stalled=0;direction=destination(player,next).subtract(now);
        }
        double length=direction.length();if(length<.000001)return;
        Vec3 velocity=direction.scale(Math.min(speed(dash.rank),length)/length);
        var swept=player.getBoundingBox().expandTowards(velocity).deflate(.02);
        if(!dash.level.hasChunksAt(BlockPos.containing(swept.minX,swept.minY,swept.minZ),BlockPos.containing(swept.maxX,swept.maxY,swept.maxZ))
                || !dash.level.noCollision(player,swept)){cancel(player.getUUID());return;}
        motion(player,velocity);
    }
    private static void strike(ServerPlayer player,Dash dash,Mob target){
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
        if(!landed)target.invulnerableTime=immunity;
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
        trail(dash.level,center.add(0,2.5,0),center,player.tickCount);
        dash.level.playSound(null,target.blockPosition(),SoundEvents.LIGHTNING_BOLT_IMPACT,SoundSource.PLAYERS,.75F,1.5F);
    }
    /** Short jagged gold/white strokes follow real movement, rather than a beam ahead of the player. */
    private static void trail(ServerLevel level,Vec3 from,Vec3 to,int seed){
        int samples=Math.clamp((int)Math.ceil(from.distanceTo(to)*3),2,10);
        for(int i=0;i<=samples;i++){
            double t=i/(double)samples,jitter=(i==0 || i==samples)?0:((i+seed)%2==0?.13:-.13);
            var point=from.lerp(to,t).add(jitter,-jitter,jitter*.5);
            level.sendParticles(GOLD,point.x,point.y,point.z,1,0,0,0,0);
            level.sendParticles(WHITE,point.x,point.y+.025,point.z,1,0,0,0,0);
            if(i%2==0)level.sendParticles(ParticleTypes.ELECTRIC_SPARK,point.x,point.y,point.z,1,.035,.035,.035,.03);
        }
    }
    @SubscribeEvent public static void playerTick(PlayerTickEvent.Post event){if(event.getEntity() instanceof ServerPlayer player)tick(player);}
    @SubscribeEvent public static void logout(PlayerEvent.PlayerLoggedOutEvent event){cancel(event.getEntity().getUUID());}
    @SubscribeEvent public static void dimension(PlayerEvent.PlayerChangedDimensionEvent event){cancel(event.getEntity().getUUID());}
}
