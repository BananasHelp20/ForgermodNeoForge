package net.bananashelp20.forgermod.item.custom.abilities;

import com.mojang.math.Transformation;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import java.util.*;

/** Four bent roots and a closed binding loop; no terrain changes, collisions, AI, saves or drops. */
public final class RootingRootsVisuals {
    public static final int ANIMATION_TICKS=10;
    public static final int MODELS_PER_BINDING=12;
    private static final Map<UUID,Binding> ACTIVE=new HashMap<>();
    private static final List<Binding> RETIRING=new ArrayList<>();
    public static final class Root extends Display.BlockDisplay {
        private final float radius,angle;
        private final int segment;
        Root(ServerLevel level,float radius,float angle,int segment,float height) {
            super(EntityType.BLOCK_DISPLAY,level);this.radius=radius;this.angle=angle;this.segment=segment;
            setNoGravity(true);setInvulnerable(true);noPhysics=true;shape(height,false,0);
        }
        void shape(float height,boolean extended,int duration) {
            var foot=point(angle,radius*1.6f,-.12f);
            var elbow=point(angle+.45f,radius*1.15f,height*.45f);
            var grip=point(angle+.9f,radius,height);
            var nextGrip=point(angle+.9f+(float)Math.PI/2,radius,height);
            var from=segment==0?foot:segment==1?elbow:grip;
            var to=segment==0?elbow:segment==1?grip:nextGrip;
            var direction=new Vector3f(to).sub(from);
            float length=direction.length();
            var rotation=new Quaternionf().rotationTo(new Vector3f(0,1,0),direction.normalize());
            float thickness=segment==2?.12f:.16f;
            var translation=extended?new Vector3f(from):point(angle,radius*1.6f,-.2f);
            translation.add(rotation.transform(new Vector3f(-thickness/2,0,-thickness/2)));
            var transform=new Transformation(translation,rotation,new Vector3f(extended?thickness:.001f,extended?length:.001f,extended?thickness:.001f),new Quaternionf());
            var tag=new CompoundTag();tag.put("block_state",NbtUtils.writeBlockState(Blocks.MANGROVE_ROOTS.defaultBlockState()));
            tag.put("transformation",Transformation.EXTENDED_CODEC.encodeStart(NbtOps.INSTANCE,transform).getOrThrow());
            tag.putInt("interpolation_duration",duration);tag.putInt("start_interpolation",0);
            tag.putFloat("shadow_radius",0);tag.putFloat("width",radius*4+1);tag.putFloat("height",height+1);
            readAdditionalSaveData(tag);
        }
        private static Vector3f point(float angle,float radius,float y) {
            return new Vector3f((float)Math.cos(angle)*radius,y,(float)Math.sin(angle)*radius);
        }
        @Override public boolean shouldBeSaved() {return false;}
    }
    private static final class Binding {
        final UUID owner;final Mob target;final ServerLevel level;final double ground;final long born;final List<Root> models=new ArrayList<>();
        boolean grown;float lastHeight;long retreat=-1;
        Binding(UUID owner,Mob target,ServerLevel level,double ground) {this.owner=owner;this.target=target;this.level=level;this.ground=ground;born=level.getGameTime();}
        float height() {return (float)Math.max(.3,target.getY()-ground+Math.min(2,target.getBbHeight()*.7));}
    }
    private RootingRootsVisuals() {}
    public static void grow(UUID owner,Mob target) {
        if(!(target.level() instanceof ServerLevel level)) return;
        var start=target.position().add(0,.1,0);
        var hit=level.clip(new ClipContext(start,start.add(0,-32,0),ClipContext.Block.COLLIDER,ClipContext.Fluid.NONE,target));
        if(hit.getType()==HitResult.Type.MISS) return;
        var old=ACTIVE.remove(target.getUUID());if(old!=null) retreat(old);
        var binding=new Binding(owner,target,level,hit.getLocation().y);
        float radius=target.getBbWidth()*.5f+.1f;
        binding.lastHeight=binding.height();
        for(int i=0;i<MODELS_PER_BINDING;i++) {
            float angle=(float)((i/3)*Math.PI/2);var root=new Root(level,radius,angle,i%3,binding.lastHeight);
            root.setPos(target.getX(),binding.ground,target.getZ());
            if(level.addFreshEntity(root)) binding.models.add(root);
        }
        if(!binding.models.isEmpty()) ACTIVE.put(target.getUUID(),binding);
    }
    private static void retreat(Binding binding) {
        if(binding.retreat>=0) return;
        // A rapid replacement may have one old animation, never an unbounded display pile.
        RETIRING.removeIf(old->{if(old.target.getUUID().equals(binding.target.getUUID())) {destroy(old);return true;}return false;});
        binding.retreat=binding.level.getGameTime();
        for(var root:binding.models) root.shape(binding.lastHeight,false,ANIMATION_TICKS);
        RETIRING.add(binding);
    }
    public static void retractOwner(UUID owner) {
        for(var binding:List.copyOf(ACTIVE.values())) if(binding.owner.equals(owner)) {ACTIVE.remove(binding.target.getUUID());retreat(binding);}
    }
    public static void retractTarget(UUID target) {var binding=ACTIVE.remove(target);if(binding!=null) retreat(binding);}
    private static void destroy(Binding binding) {binding.models.forEach(Entity::discard);}
    public static void tick(ServerLevel level,RootingRootsState state) {
        long now=level.getGameTime();
        for(var binding:List.copyOf(ACTIVE.values())) {
            if(binding.level!=level) continue;
            if(!binding.target.isAlive() || binding.target.isRemoved() || binding.target.level()!=level
                || !state.rootedBy(binding.target.getUUID(),binding.owner,now)) {ACTIVE.remove(binding.target.getUUID());retreat(binding);continue;}
            if(!binding.grown && now>binding.born) {
                binding.grown=true;binding.lastHeight=binding.height();
                for(var root:binding.models) root.shape(binding.lastHeight,true,ANIMATION_TICKS);
            } else if(binding.grown && Math.abs(binding.height()-binding.lastHeight)>.05f) {
                // Adapt only to actual vertical movement, never restart a stationary hold.
                binding.lastHeight=binding.height();
                for(var root:binding.models) root.shape(binding.lastHeight,true,ANIMATION_TICKS);
            }
        }
        RETIRING.removeIf(binding->{if(binding.level==level && now>=binding.retreat+ANIMATION_TICKS+2) {destroy(binding);return true;}return false;});
    }
}
