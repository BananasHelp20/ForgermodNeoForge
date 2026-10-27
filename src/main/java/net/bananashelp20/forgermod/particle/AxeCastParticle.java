package net.bananashelp20.forgermod.particle;
import net.bananashelp20.forgermod.ForgerMod;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
@EventBusSubscriber(modid=ForgerMod.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class AxeCastParticle extends TextureSheetParticle {
    private AxeCastParticle(ClientLevel level,double x,double y,double z,double vx,double vy,double vz,SpriteSet sprites){
        super(level,x,y,z,vx,vy,vz);setSpriteFromAge(sprites);lifetime=20;quadSize=.4F;hasPhysics=false;gravity=0;friction=.8F;xd=vx;yd=vy;zd=vz;roll=(float)(level.random.nextDouble()*Math.PI*2);
    }
    @Override public ParticleRenderType getRenderType(){return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;}
    @Override public int getLightColor(float partial){return 0xF000F0;}
    @Override public void tick(){super.tick();yd+=.0125;roll+=.00436;alpha=Math.max(0,1-(float)age/lifetime);}
    @SubscribeEvent public static void register(RegisterParticleProvidersEvent event){event.registerSpriteSet(AxeCastParticles.SWIRL.get(),sprites->(type,level,x,y,z,vx,vy,vz)->new AxeCastParticle(level,x,y,z,vx,vy,vz,sprites));}
}
