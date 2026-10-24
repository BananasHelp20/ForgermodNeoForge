package net.bananashelp20.forgermod.particle;

import net.bananashelp20.forgermod.ForgerMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

@EventBusSubscriber(modid=ForgerMod.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class SculkRoarParticle extends NoRenderParticle {
    private final SculkRoarParticles.Options options;private final SpriteSet sprites;
    private SculkRoarParticle(ClientLevel level,double x,double y,double z,SculkRoarParticles.Options options,SpriteSet sprites){
        super(level,x,y,z);this.options=options;this.sprites=sprites;
    }
    @Override public void tick(){
        var center=new Vec3(x,y,z);
        for(int wave=0;wave<SculkRoarParticles.waves(options);wave++)for(int index=0;index<SculkRoarParticles.points(options);index++)
            Minecraft.getInstance().particleEngine.add(new Mote(level,center,options,wave,index,sprites));
        remove();
    }
    private static final class Mote extends TextureSheetParticle {
        private final Vec3 center;private final SculkRoarParticles.Options options;private final int wave,index;
        Mote(ClientLevel level,Vec3 center,SculkRoarParticles.Options options,int wave,int index,SpriteSet sprites){
            super(level,center.x,center.y,center.z);this.center=center;this.options=options;this.wave=wave;this.index=index;
            lifetime=SculkRoarParticles.TRAVEL_TICKS+wave*SculkRoarParticles.WAVE_DELAY+4;hasPhysics=false;gravity=0;
            rCol=.05f+.04f*wave;gCol=.85f-.12f*wave;bCol=1;pickSprite(sprites);locate();xo=x;yo=y;zo=z;
        }
        private void locate(){
            var point=SculkRoarParticles.position(center,options,wave,index,age);setPos(point.x,point.y,point.z);
            alpha=SculkRoarParticles.alpha(wave,age);quadSize=.16f+.25f*(float)SculkRoarParticles.progress(wave,age);
        }
        @Override public void tick(){xo=x;yo=y;zo=z;if(age++>=lifetime){remove();return;}locate();}
        @Override public ParticleRenderType getRenderType(){return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;}
        @Override public int getLightColor(float partialTick){return 0xF000F0;}
    }
    @SubscribeEvent public static void register(RegisterParticleProvidersEvent event){
        event.registerSpriteSet(SculkRoarParticles.ROAR.get(),sprites->(options,level,x,y,z,vx,vy,vz)->new SculkRoarParticle(level,x,y,z,options,sprites));
    }
}
