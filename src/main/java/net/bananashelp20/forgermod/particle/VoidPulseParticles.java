package net.bananashelp20.forgermod.particle;
import net.bananashelp20.forgermod.ForgerMod;
import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.particles.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.*;
import net.neoforged.neoforge.registries.*;
public final class VoidPulseParticles {
    public static final int CHARGE_TICKS=10;
    public static final DeferredRegister<ParticleType<?>> TYPES=DeferredRegister.create(Registries.PARTICLE_TYPE,ForgerMod.MOD_ID);
    public static final DeferredHolder<ParticleType<?>,ParticleType<Options>> PULSE=TYPES.register("void_pulse",()->new ParticleType<Options>(false) {
        public MapCodec<Options> codec(){return Options.CODEC;}
        public StreamCodec<? super RegistryFriendlyByteBuf,Options> streamCodec(){return Options.STREAM_CODEC;}
    });
    private VoidPulseParticles() {}
    public record Options(boolean outward,int ownerId) implements ParticleOptions {
        public static final MapCodec<Options> CODEC=RecordCodecBuilder.mapCodec(i->i.group(Codec.BOOL.fieldOf("outward").forGetter(Options::outward),Codec.INT.fieldOf("owner").forGetter(Options::ownerId)).apply(i,Options::new));
        public static final StreamCodec<RegistryFriendlyByteBuf,Options> STREAM_CODEC=StreamCodec.composite(ByteBufCodecs.BOOL,Options::outward,ByteBufCodecs.VAR_INT,Options::ownerId,Options::new);
        public ParticleType<?> getType(){return PULSE.get();}
    }
}
