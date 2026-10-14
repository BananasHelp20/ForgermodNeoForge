package net.bananashelp20.forgermod.item.custom.abilities;

import com.mojang.math.Transformation;
import net.minecraft.nbt.*;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.*;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import java.util.*;

/** Three native display models per binding; no terrain changes, collisions, AI, saves or drops. */
public final class RootingRootsVisuals {
    public static final int ANIMATION_TICKS=10;
    private static final Map<UUID,Binding> ACTIVE=new HashMap<>();
    private static final List<Binding> RETIRING=new ArrayList<>();
    public static final class Root extends Display.BlockDisplay {
        private final float radius,angle;
        Root(ServerLevel level,float radius,float angle,float height) {
            super(EntityType.BLOCK_DISPLAY,level);this.radius=radius;this.angle=angle;
            setNoGravity(true);setInvulnerable(true);noPhysics=true;shape(height,false,0);
        }
        void shape(float height,boolean extended,int duration) {
            float lean=(float)Math.atan2(radius*.55,height);
            float length=(float)Math.hypot(height,radius*.55);
            var rotation=new Quaternionf().rotateY(-angle).rotateZ(lean);
            var transform=new Transformation(new Vector3f(-.08f,-.12f,-.08f),rotation,new Vector3f(.16f,extended?length:.001f,.16f),new Quaternionf());
            var tag=new CompoundTag();tag.put("block_state",NbtUtils.writeBlockState(Blocks.MANGROVE_ROOTS.defaultBlockState()));
            tag.put("transformation",Transformation.EXTENDED_CODEC.encodeStart(NbtOps.INSTANCE,transform).getOrThrow());
            tag.putInt("interpolation_duration",duration);tag.putInt("start_interpolation",0);
            tag.putFloat("shadow_radius",0);tag.putFloat("width",radius*2+1);tag.putFloat("height",height+1);
            readAdditionalSaveData(tag);
        }
        @Override public boolean shouldBeSaved() {return false;}
    }
    private static final class Binding {
        final UUID owner;final Mob target;final ServerLevel level;final double ground;final long born;final List<Root> models=new ArrayList<>();
        boolean grown;long retreat=-1;
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
        for(int i=0;i<3;i++) {
            float angle=(float)(i*2*Math.PI/3);var root=new Root(level,radius,angle,binding.height());
            root.setPos(target.getX()+Math.cos(angle)*radius,binding.ground,target.getZ()+Math.sin(angle)*radius);
            if(level.addFreshEntity(root)) binding.models.add(root);
        }
        if(!binding.models.isEmpty()) ACTIVE.put(target.getUUID(),binding);
    }
    private static void retreat(Binding binding) {
        if(binding.retreat>=0) return;
        // A rapid replacement may have one old animation, never an unbounded display pile.
        RETIRING.removeIf(old->{if(old.target.getUUID().equals(binding.target.getUUID())) {destroy(old);return true;}return false;});
        binding.retreat=binding.level.getGameTime();
        for(var root:binding.models) root.shape(binding.height(),false,ANIMATION_TICKS);
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
                binding.grown=true;for(var root:binding.models) root.shape(binding.height(),true,ANIMATION_TICKS);
            } else if(binding.grown && now-binding.born>ANIMATION_TICKS && now%20==0) {
                for(var root:binding.models) root.shape(binding.height(),true,ANIMATION_TICKS);
            }
        }
        RETIRING.removeIf(binding->{if(binding.level==level && now>=binding.retreat+ANIMATION_TICKS+2) {destroy(binding);return true;}return false;});
    }
}
