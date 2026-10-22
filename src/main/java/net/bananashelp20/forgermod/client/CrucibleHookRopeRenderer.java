package net.bananashelp20.forgermod.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.item.custom.abilities.CrucibleHookRope;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;

/** Two narrow shaded ribbons, like a native leash; no particles or extra ticking entities. */
@EventBusSubscriber(modid=ForgerMod.MOD_ID,value=Dist.CLIENT)
public final class CrucibleHookRopeRenderer {
    private CrucibleHookRopeRenderer() {}
    @SubscribeEvent public static void render(RenderLevelStageEvent event) {
        if(event.getStage()!=RenderLevelStageEvent.Stage.AFTER_ENTITIES)return;
        var minecraft=Minecraft.getInstance();var level=minecraft.level;if(level==null)return;
        float partial=event.getPartialTick().getGameTimeDeltaPartialTick(false);
        var camera=event.getCamera().getPosition();var buffers=minecraft.renderBuffers().bufferSource();
        for(var owner:level.players()) {
            var target=CrucibleHookRope.target(owner);if(target==null)continue;
            var start=owner.getRopeHoldPosition(partial);
            var end=target.getPosition(partial).add(0,target.getBbHeight()*.55,0);
            if(start.distanceToSqr(end)<.000001)continue;
            var axis=end.subtract(start).normalize();var side=axis.cross(new Vec3(0,1,0));
            if(side.lengthSqr()<.000001)side=new Vec3(1,0,0);
            side=side.normalize().scale(.016);var other=axis.cross(side).normalize().scale(.016);
            int startLight=LevelRenderer.getLightColor(level,BlockPos.containing(start));
            int endLight=LevelRenderer.getLightColor(level,BlockPos.containing(end));
            var poses=event.getPoseStack();poses.pushPose();poses.translate(start.x-camera.x,start.y-camera.y,start.z-camera.z);
            // Isolate strips from native leashes and other simultaneous hook ropes.
            buffers.endBatch(RenderType.leash());var vertices=buffers.getBuffer(RenderType.leash());
            for(int strip=0;strip<2;strip++)for(int n=0;n<=CrucibleHookRope.SEGMENTS;n++) {
                int index=strip==0?n:CrucibleHookRope.SEGMENTS-n;
                float t=index/(float)CrucibleHookRope.SEGMENTS;
                int light=LightTexture.pack((int)(LightTexture.block(startLight)*(1-t)+LightTexture.block(endLight)*t),
                        (int)(LightTexture.sky(startLight)*(1-t)+LightTexture.sky(endLight)*t));
                var point=CrucibleHookRope.point(start,end,index).subtract(start);var offset=strip==0?side:other;
                float shade=(index+strip)%2==0?1:.7F;
                vertex(vertices,poses.last().pose(),point.subtract(offset),shade,light);
                vertex(vertices,poses.last().pose(),point.add(offset),shade,light);
            }
            buffers.endBatch(RenderType.leash());poses.popPose();
        }
    }
    private static void vertex(VertexConsumer vertices,Matrix4f pose,Vec3 point,float shade,int light) {
        vertices.addVertex(pose,(float)point.x,(float)point.y,(float)point.z).setColor(.5F*shade,.35F*shade,.2F*shade,1).setLight(light);
    }
}
