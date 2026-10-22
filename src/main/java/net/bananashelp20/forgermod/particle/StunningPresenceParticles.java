package net.bananashelp20.forgermod.particle;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bananashelp20.forgermod.ForgerMod;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** One pattern packet per ring; the client creates its bounded number of short-lived motes. */
public final class StunningPresenceParticles {
    public static final double RADIUS = 10;
    public static final int VISUAL_INTERVAL = 5;
    public static final int LIFETIME = 7;
    public static final DeferredRegister<ParticleType<?>> TYPES = DeferredRegister.create(Registries.PARTICLE_TYPE,ForgerMod.MOD_ID);
    public static final DeferredHolder<ParticleType<?>,ParticleType<Options>> RING = TYPES.register("stunning_presence_ring",()->new ParticleType<Options>(true) {
        @Override public MapCodec<Options> codec() {return Options.CODEC;}
        @Override public StreamCodec<? super RegistryFriendlyByteBuf,Options> streamCodec() {return Options.STREAM_CODEC;}
    });
    private StunningPresenceParticles() {}

    public record Options(int owner, int rank, float radius, float rotation, boolean boundary) implements ParticleOptions {
        public Options {
            rank=Math.clamp(rank,1,4);
            radius=Float.isFinite(radius)?Math.clamp(radius,0,(float)RADIUS):0;
            rotation=Float.isFinite(rotation)?rotation:0;
        }
        public static final MapCodec<Options> CODEC = RecordCodecBuilder.mapCodec(i->i.group(
                Codec.INT.fieldOf("owner").forGetter(Options::owner),Codec.INT.fieldOf("rank").forGetter(Options::rank),
                Codec.FLOAT.fieldOf("radius").forGetter(Options::radius),Codec.FLOAT.fieldOf("rotation").forGetter(Options::rotation),
                Codec.BOOL.fieldOf("boundary").forGetter(Options::boundary)).apply(i,Options::new));
        public static final StreamCodec<RegistryFriendlyByteBuf,Options> STREAM_CODEC = StreamCodec.of(
                (buffer,value)->{buffer.writeVarInt(value.owner());buffer.writeVarInt(value.rank());buffer.writeFloat(value.radius());buffer.writeFloat(value.rotation());buffer.writeBoolean(value.boundary());},
                buffer->new Options(buffer.readVarInt(),buffer.readVarInt(),buffer.readFloat(),buffer.readFloat(),buffer.readBoolean()));
        @Override public ParticleType<?> getType() {return RING.get();}
    }

    public static int points(Options options) {
        return options.boundary()?4*(32+8*options.rank()):3*(16+4*options.rank());
    }

    public static double radiusAtAge(Options options, int age) {
        return options.boundary()?RADIUS:Math.min(RADIUS,options.radius()+Math.max(0,age));
    }

    public static Vec3 position(Vec3 center, Options options, int index, int age) {
        double angle=options.rotation()+2*Math.PI*index/points(options);
        double radius=radiusAtAge(options,age);
        return center.add(Math.cos(angle)*radius,(options.boundary()?.15:.35)+.1*Math.sin(angle*4),Math.sin(angle)*radius);
    }
}
