package net.bananashelp20.forgermod.particle;
import net.bananashelp20.forgermod.ForgerMod;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
/** Inward motes track the moving caster; release motes have explicit outward motion. */
@EventBusSubscriber(modid=ForgerMod.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class VoidPulseParticle extends TextureSheetParticle {
    private final boolean outward;
    private final int owner;
    private final Vec3 offset;
    private VoidPulseParticle(ClientLevel level,double x,double y,double z,double vx,double vy,double vz,VoidPulseParticles.Options options,SpriteSet sprites) {
        super(level,x,y,z);outward=options.outward();owner=options.ownerId();
        var caster=level.getEntity(owner);var center=caster==null?new Vec3(x,y,z):caster.position().add(0,caster.getBbHeight()*.5,0);
        offset=new Vec3(x,y,z).subtract(center);xd=vx;yd=vy;zd=vz;
        lifetime=outward?8:VoidPulseParticles.CHARGE_TICKS;hasPhysics=false;gravity=0;quadSize=outward?.14f:.09f;
        rCol=.65f+random.nextFloat()*.25f;gCol=.15f+random.nextFloat()*.25f;bCol=1;pickSprite(sprites);
    }
    @Override public ParticleRenderType getRenderType(){return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;}
    @Override public int getLightColor(float partial){return 0xF000F0;}
    @Override public void tick() {
        xo=x;yo=y;zo=z;
        if(age++>=lifetime){remove();return;}
        double t=(double)age/lifetime;
        if(outward){move(xd,yd,zd);xd*=.88;yd*=.88;zd*=.88;alpha=(float)(1-t*t);}
        else {
            var caster=level.getEntity(owner);if(caster==null || !caster.isAlive()){remove();return;}
            var center=caster.position().add(0,caster.getBbHeight()*.5,0);
            var position=center.add(VoidPulseMotion.inward(offset,t));
            setPos(position.x,position.y,position.z);
            quadSize=.09f+(float)t*.06f;alpha=(float)(.6+.4*t);
        }
    }
    @SubscribeEvent public static void register(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(VoidPulseParticles.PULSE.get(),sprites->(options,level,x,y,z,vx,vy,vz)->new VoidPulseParticle(level,x,y,z,vx,vy,vz,options,sprites));
    }
}
