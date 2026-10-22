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

@EventBusSubscriber(modid=ForgerMod.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class StunningPresenceParticle extends NoRenderParticle {
    private final StunningPresenceParticles.Options options;
    private final SpriteSet sprites;
    private StunningPresenceParticle(ClientLevel level,double x,double y,double z,StunningPresenceParticles.Options options,SpriteSet sprites) {
        super(level,x,y,z);this.options=options;this.sprites=sprites;
    }
    @Override public void tick() {
        var center=new Vec3(x,y,z);
        for(int i=0;i<StunningPresenceParticles.points(options);i++)
            Minecraft.getInstance().particleEngine.add(new Mote(level,center,options,i,sprites));
        remove();
    }

    private static final class Mote extends TextureSheetParticle {
        private final Vec3 fallback;
        private final StunningPresenceParticles.Options options;
        private final int index;
        Mote(ClientLevel level,Vec3 center,StunningPresenceParticles.Options options,int index,SpriteSet sprites) {
            super(level,center.x,center.y,center.z);this.fallback=center;this.options=options;this.index=index;
            lifetime=StunningPresenceParticles.LIFETIME;hasPhysics=false;gravity=0;
            quadSize=.14f+.01f*options.rank();pickSprite(sprites);
            rCol=options.boundary()?.15f:.55f;gCol=options.boundary()?.9f:.8f;bCol=1;
            locate();xo=x;yo=y;zo=z;
        }
        private void locate() {
            var owner=level.getEntity(options.owner());
            if(owner!=null && !owner.isAlive()) {remove();return;}
            var position=StunningPresenceParticles.position(owner==null?fallback:owner.position(),options,index,age);
            setPos(position.x,position.y,position.z);
        }
        @Override public void tick() {
            xo=x;yo=y;zo=z;
            if(age++>=lifetime) {remove();return;}
            locate();alpha=(float)Math.min(1,(lifetime-age)/2.0);
        }
        @Override public ParticleRenderType getRenderType() {return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;}
        @Override public int getLightColor(float partialTick) {return 0xF000F0;}
    }

    @SubscribeEvent
    public static void register(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(StunningPresenceParticles.RING.get(),sprites->
                (options,level,x,y,z,vx,vy,vz)->new StunningPresenceParticle(level,x,y,z,options,sprites));
    }
}
