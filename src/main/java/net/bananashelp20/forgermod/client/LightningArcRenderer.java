package net.bananashelp20.forgermod.client;

import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.particle.LightningArcParticles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.NoRenderParticle;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import java.util.*;

@EventBusSubscriber(modid=ForgerMod.MOD_ID,value=Dist.CLIENT)
public final class LightningArcRenderer {
    private record Arc(ClientLevel level,Vec3 start,LightningArcParticles.Options options,long created){}
    private static final List<Arc> ARCS=new ArrayList<>();
    private LightningArcRenderer(){}
    @SubscribeEvent public static void tick(ClientTickEvent.Post event){
        var level=Minecraft.getInstance().level;
        ARCS.removeIf(arc->level==null || arc.level!=level || level.getGameTime()-arc.created>=arc.options.ticks());
    }
    @SubscribeEvent public static void render(RenderLevelStageEvent event){
        if(event.getStage()!=RenderLevelStageEvent.Stage.AFTER_PARTICLES)return;
        var mc=Minecraft.getInstance();var level=mc.level;
        ARCS.removeIf(arc->arc.level!=level || level==null || level.getGameTime()-arc.created>=arc.options.ticks());
        if(ARCS.isEmpty())return;
        var buffers=mc.renderBuffers().bufferSource();var vertices=buffers.getBuffer(RenderType.lightning());var poses=event.getPoseStack();var camera=event.getCamera().getPosition();
        float partial=event.getPartialTick().getGameTimeDeltaPartialTick(false);
        for(var arc:ARCS){
            var options=arc.options;var start=arc.start;var delta=options.delta();
            if(options.owner()>=0){
                var owner=level.getEntity(options.owner());if(owner==null || owner.isRemoved())continue;
                // Attach the stroke's tip to the rendered player, so it never runs ahead of them.
                start=owner.getPosition(partial).add(0,.9,0).subtract(delta);
            }
            int age=(int)(level.getGameTime()-arc.created);float fade=1-age/(float)options.ticks();
            poses.pushPose();poses.translate(start.x-camera.x,start.y-camera.y,start.z-camera.z);var matrix=poses.last().pose();
            for(int i=0;i<LightningArcParticles.SEGMENTS;i++){
                var a=LightningArcParticles.point(delta,i,options.seed(),age);var b=LightningArcParticles.point(delta,i+1,options.seed(),age);
                var axis=b.subtract(a).normalize();var side=axis.cross(new Vec3(0,1,0));if(side.lengthSqr()<.001)side=new Vec3(1,0,0);side=side.normalize();var up=axis.cross(side).normalize();
                for(int layer=0;layer<2;layer++)for(var offset:List.of(side,up)){
                    offset=offset.scale(layer==0?.055:.019);float r=layer==0?.12F:1,g=layer==0?.48F:.83F,bl=layer==0?1:.1F;
                    for(var v:List.of(a.subtract(offset),a.add(offset),b.add(offset),b.subtract(offset)))vertices.addVertex(matrix,(float)v.x,(float)v.y,(float)v.z).setColor(r,g,bl,fade*(layer==0?.55F:1));
                }
            }
            poses.popPose();
        }
        buffers.endBatch(RenderType.lightning());
    }
    @EventBusSubscriber(modid=ForgerMod.MOD_ID,value=Dist.CLIENT,bus=EventBusSubscriber.Bus.MOD)
    public static final class Registration {
        @SubscribeEvent public static void register(RegisterParticleProvidersEvent event){
            event.registerSpecial(LightningArcParticles.ARC.get(),(options,level,x,y,z,dx,dy,dz)->{
                if(ARCS.size()>=128)ARCS.removeFirst();ARCS.add(new Arc(level,new Vec3(x,y,z),options,level.getGameTime()));
                return new NoRenderParticle(level,x,y,z){ {lifetime=1;} };
            });
        }
    }
}
