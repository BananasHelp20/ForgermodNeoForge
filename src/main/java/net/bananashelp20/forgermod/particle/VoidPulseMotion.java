package net.bananashelp20.forgermod.particle;
import net.minecraft.world.phys.Vec3;
/** Shared deterministic motion, independent of rendering or particle texture animation. */
public final class VoidPulseMotion {
    private VoidPulseMotion() {}
    public static Vec3 inward(Vec3 offset,double progress) {
        double t=Math.clamp(progress,0,1),shrink=(1-t)*(1-t),c=Math.cos(t*.7),s=Math.sin(t*.7);
        return new Vec3((offset.x*c-offset.z*s)*shrink,offset.y*shrink,(offset.x*s+offset.z*c)*shrink);
    }
    public static Vec3 outward(int index,double radius) {
        double y=1-2*(index+.5)/96,h=Math.sqrt(1-y*y),a=index*2.399963229728653,speed=.9+radius*.25;
        return new Vec3(Math.cos(a)*h*speed,y*.55*speed,Math.sin(a)*h*speed);
    }
}
