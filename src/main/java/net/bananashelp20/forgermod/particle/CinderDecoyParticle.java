package net.bananashelp20.forgermod.particle;

import net.bananashelp20.forgermod.ForgerMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.NoRenderParticle;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

@EventBusSubscriber(modid=ForgerMod.MOD_ID, bus=EventBusSubscriber.Bus.MOD, value=Dist.CLIENT)
public final class CinderDecoyParticle extends NoRenderParticle {
    private final CinderDecoyParticles.Options options;
    private final SpriteSet sprites;
    private CinderDecoyParticle(ClientLevel level, double x, double y, double z, CinderDecoyParticles.Options options, SpriteSet sprites) {
        super(level, x, y, z); this.options=options; this.sprites=sprites;
    }
    @Override public void tick() {
        for (var offset : CinderDecoyParticles.silhouette(options.yaw()))
            Minecraft.getInstance().particleEngine.add(new Flame(level, new Vec3(x,y,z).add(offset), offset,
                    Math.clamp(options.radius(),2,5), sprites));
        remove();
    }
    private static final class Flame extends TextureSheetParticle {
        private final Vec3 origin;
        private final Vec3 travel;
        private final float size;
        private Flame(ClientLevel level, Vec3 origin, Vec3 offset, float radius, SpriteSet sprites) {
            super(level, origin.x, origin.y, origin.z);
            this.origin=origin;
            travel=offset.subtract(0,1,0).normalize().scale(radius*(.7+random.nextDouble()*.3));
            lifetime=CinderDecoyParticles.HOLD_TICKS+CinderDecoyParticles.BURST_TICKS;
            hasPhysics=false; gravity=0; size=.075f+random.nextFloat()*.025f; quadSize=size;
            pickSprite(sprites);
        }
        @Override public void tick() {
            xo=x; yo=y; zo=z;
            if (age++>=lifetime) { remove(); return; }
            double expansion=CinderDecoyParticles.expansion(age);
            var point=origin.add(travel.scale(expansion));
            setPos(point.x,point.y,point.z);
            quadSize=size*(1+(float)expansion*.8f);
            alpha=1-(float)Math.pow(expansion,4);
        }
        @Override public ParticleRenderType getRenderType() { return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT; }
        @Override public int getLightColor(float partialTick) { return 0xF000F0; }
    }
    @SubscribeEvent public static void register(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(CinderDecoyParticles.BURST.get(), sprites -> (options,level,x,y,z,vx,vy,vz) ->
                new CinderDecoyParticle(level,x,y,z,options,sprites));
    }
}
