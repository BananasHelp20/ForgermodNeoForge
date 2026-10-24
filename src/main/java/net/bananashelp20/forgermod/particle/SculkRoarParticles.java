package net.bananashelp20.forgermod.particle;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bananashelp20.forgermod.ForgerMod;
import net.minecraft.core.particles.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.registries.*;

/** One pattern packet creates a bounded burst of sculk-colored sound ripples on each viewing client. */
public final class SculkRoarParticles {
    public static final int TRAVEL_TICKS=24, WAVE_DELAY=6;
    public static final DeferredRegister<ParticleType<?>> TYPES=DeferredRegister.create(Registries.PARTICLE_TYPE,ForgerMod.MOD_ID);
    public static final DeferredHolder<ParticleType<?>,ParticleType<Options>> ROAR=TYPES.register("sculk_roar",()->new ParticleType<Options>(true){
        @Override public MapCodec<Options> codec(){return Options.CODEC;}
        @Override public StreamCodec<? super RegistryFriendlyByteBuf,Options> streamCodec(){return Options.STREAM;}
    });
    public record Options(int rank,float radius) implements ParticleOptions {
        public Options{rank=Math.clamp(rank,1,8);radius=Float.isFinite(radius)?Math.clamp(radius,1,50):1;}
        public static final MapCodec<Options> CODEC=RecordCodecBuilder.mapCodec(i->i.group(
                Codec.INT.fieldOf("rank").forGetter(Options::rank),Codec.FLOAT.fieldOf("radius").forGetter(Options::radius)).apply(i,Options::new));
        public static final StreamCodec<RegistryFriendlyByteBuf,Options> STREAM=StreamCodec.of(
                (b,o)->{b.writeVarInt(o.rank);b.writeFloat(o.radius);},b->new Options(b.readVarInt(),b.readFloat()));
        @Override public ParticleType<?> getType(){return ROAR.get();}
    }
    private SculkRoarParticles(){}
    public static int waves(Options options){return options.rank()>=5?3:2;}
    public static int points(Options options){return 96+8*options.rank();}
    public static double progress(int wave,int age){return Math.clamp((age-wave*WAVE_DELAY)/(double)TRAVEL_TICKS,0,1);}
    public static double radius(Options options,int wave,int age){return options.radius()*(1-Math.pow(1-progress(wave,age),1.8));}
    public static float alpha(int wave,int age){
        int elapsed=age-wave*WAVE_DELAY;
        return elapsed<0?0:(float)Math.min(1,Math.max(0,(TRAVEL_TICKS+3-elapsed)/5.0));
    }
    public static Vec3 position(Vec3 center,Options options,int wave,int index,int age){
        double angle=2*Math.PI*index/points(options)+wave*.12,progress=progress(wave,age),radius=radius(options,wave,age);
        return center.add(Math.cos(angle)*radius,.25+wave*.7+.2*Math.sin(angle*7+progress*8)*progress,Math.sin(angle)*radius);
    }
    public static void emit(ServerPlayer player,int rank,int radius){
        var options=new Options(rank,radius);double visibility=radius+32;
        // Include players in the full super-roar range, beyond the vanilla 32-block particle packet limit.
        for(var viewer:player.serverLevel().players())if(viewer.distanceToSqr(player)<=visibility*visibility)
            player.serverLevel().sendParticles(viewer,options,true,player.getX(),player.getY(),player.getZ(),1,0,0,0,0);
    }
}
