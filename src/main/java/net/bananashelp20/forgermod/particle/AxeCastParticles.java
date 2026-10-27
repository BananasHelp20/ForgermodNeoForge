package net.bananashelp20.forgermod.particle;
import net.bananashelp20.forgermod.ForgerMod;
import net.minecraft.core.particles.*;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.*;
public final class AxeCastParticles {
    public static final DeferredRegister<ParticleType<?>> TYPES=DeferredRegister.create(Registries.PARTICLE_TYPE,ForgerMod.MOD_ID);
    public static final DeferredHolder<ParticleType<?>,SimpleParticleType> SWIRL=TYPES.register("axe_cast_swirl",()->new SimpleParticleType(false));
    private AxeCastParticles(){}
}
