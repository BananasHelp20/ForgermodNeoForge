package net.bananashelp20.forgermod.particle;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bananashelp20.forgermod.ForgerMod;
import net.minecraft.core.particles.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.*;

/** One compact packet per lightning stroke; the client renders bounded jagged ribbons. */
public final class LightningArcParticles {
    public static final int SEGMENTS=16;
    public static final DeferredRegister<ParticleType<?>> TYPES=DeferredRegister.create(Registries.PARTICLE_TYPE,ForgerMod.MOD_ID);
    public static final DeferredHolder<ParticleType<?>,ParticleType<Options>> ARC=TYPES.register("lightning_arc",()->new ParticleType<Options>(true){
        public MapCodec<Options> codec(){return Options.CODEC;}
        public StreamCodec<? super RegistryFriendlyByteBuf,Options> streamCodec(){return Options.STREAM;}
    });
    public record Options(float dx,float dy,float dz,int owner,long seed,int ticks) implements ParticleOptions {
        public Options {dx=safe(dx);dy=safe(dy);dz=safe(dz);ticks=Math.clamp(ticks,1,8);}
        private static float safe(float value){return Float.isFinite(value)?Math.clamp(value,-64,64):0;}
        public Vec3 delta(){return new Vec3(dx,dy,dz);}
        public static final MapCodec<Options> CODEC=RecordCodecBuilder.mapCodec(i->i.group(
                Codec.FLOAT.fieldOf("dx").forGetter(Options::dx),Codec.FLOAT.fieldOf("dy").forGetter(Options::dy),Codec.FLOAT.fieldOf("dz").forGetter(Options::dz),
                Codec.INT.fieldOf("owner").forGetter(Options::owner),Codec.LONG.fieldOf("seed").forGetter(Options::seed),Codec.INT.fieldOf("ticks").forGetter(Options::ticks)).apply(i,Options::new));
        public static final StreamCodec<RegistryFriendlyByteBuf,Options> STREAM=StreamCodec.of(
                (b,o)->{b.writeFloat(o.dx);b.writeFloat(o.dy);b.writeFloat(o.dz);b.writeVarInt(o.owner);b.writeLong(o.seed);b.writeVarInt(o.ticks);},
                b->new Options(b.readFloat(),b.readFloat(),b.readFloat(),b.readVarInt(),b.readLong(),b.readVarInt()));
        public ParticleType<?> getType(){return ARC.get();}
    }
    private LightningArcParticles(){}
    public static void emit(ServerLevel level,Vec3 from,Vec3 to,int owner,int ticks){
        var delta=to.subtract(from);if(delta.lengthSqr()<.000001)return;
        level.sendParticles(new Options((float)delta.x,(float)delta.y,(float)delta.z,owner,level.random.nextLong(),ticks),from.x,from.y,from.z,1,0,0,0,0);
    }
    public static Vec3 point(Vec3 delta,int index,long seed,int age){
        if(index<=0)return Vec3.ZERO;if(index>=SEGMENTS)return delta;
        var axis=delta.normalize();var side=axis.cross(new Vec3(0,1,0));if(side.lengthSqr()<.00001)side=new Vec3(1,0,0);side=side.normalize();var up=axis.cross(side).normalize();
        double t=index/(double)SEGMENTS,amplitude=Math.min(.65,delta.length()*.065)*Math.sin(Math.PI*t);
        double phase=(seed&65535)*.017+index*19.17+(age/2)*2.3;
        return delta.scale(t).add(side.scale(Math.sin(phase)*amplitude)).add(up.scale(Math.cos(phase*1.7)*amplitude*.65));
    }
}
