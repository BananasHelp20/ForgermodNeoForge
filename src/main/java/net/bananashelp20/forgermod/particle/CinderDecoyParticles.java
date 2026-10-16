package net.bananashelp20.forgermod.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bananashelp20.forgermod.ForgerMod;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.ArrayList;
import java.util.List;

/** One networked emitter expands into a bounded, facing-aligned flame silhouette on the client. */
public final class CinderDecoyParticles {
    public static final int HOLD_TICKS = 3;
    public static final int BURST_TICKS = 12;
    public static final DeferredRegister<ParticleType<?>> TYPES = DeferredRegister.create(Registries.PARTICLE_TYPE, ForgerMod.MOD_ID);
    public static final DeferredHolder<ParticleType<?>, ParticleType<Options>> BURST = TYPES.register("cinder_decoy_burst", () -> new ParticleType<Options>(false) {
        public MapCodec<Options> codec() { return Options.CODEC; }
        public StreamCodec<? super RegistryFriendlyByteBuf, Options> streamCodec() { return Options.STREAM_CODEC; }
    });
    private CinderDecoyParticles() {}

    public record Options(float yaw, float radius) implements ParticleOptions {
        public static final MapCodec<Options> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Codec.FLOAT.fieldOf("yaw").forGetter(Options::yaw),
                Codec.FLOAT.fieldOf("radius").forGetter(Options::radius)).apply(i, Options::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, Options> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.FLOAT, Options::yaw, ByteBufCodecs.FLOAT, Options::radius, Options::new);
        public ParticleType<?> getType() { return BURST.get(); }
    }

    public static List<Vec3> silhouette(float yaw) {
        var points = new ArrayList<Vec3>();
        shell(points, -.25, 1.5, -.25, .25, 2, .25); // Head, including the equipped player head.
        shell(points, -.25, .75, -.14, .25, 1.5, .14);
        shell(points, -.48, .75, -.13, -.26, 1.5, .13);
        shell(points, .26, .75, -.13, .48, 1.5, .13);
        shell(points, -.24, .05, -.13, -.02, .75, .13);
        shell(points, .02, .05, -.13, .24, .75, .13);
        double angle = -Math.toRadians(yaw);
        return points.stream().map(point -> point.yRot((float)angle)).toList();
    }

    private static void shell(List<Vec3> points, double x0, double y0, double z0, double x1, double y1, double z1) {
        int nx = (int)Math.ceil((x1-x0)/.18), ny = (int)Math.ceil((y1-y0)/.18), nz = (int)Math.ceil((z1-z0)/.18);
        for (int x=0; x<=nx; x++) for (int y=0; y<=ny; y++) for (int z=0; z<=nz; z++)
            if (x==0 || x==nx || y==0 || y==ny || z==0 || z==nz)
                points.add(new Vec3(x0+(x1-x0)*x/nx, y0+(y1-y0)*y/ny, z0+(z1-z0)*z/nz));
    }

    /** Starts at rest, then rapidly expands before slowing and fading. */
    public static double expansion(int age) {
        double t = Math.clamp((age-HOLD_TICKS)/(double)BURST_TICKS, 0, 1);
        return 1-Math.pow(1-t, 3);
    }
}
