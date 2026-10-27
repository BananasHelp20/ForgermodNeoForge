package net.bananashelp20.forgermod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.item.custom.attacks.axe.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import java.util.*;

@EventBusSubscriber(modid=ForgerMod.MOD_ID,value=Dist.CLIENT)
public final class AxeAnimationClient {
    public record Sample(AxeAnimationData.Clip clip,double seconds,double weight) {}
    private record Playback(long sequence,AxeAnimationData.Clip clip,long start,String item,int slot) {}
    private static final Map<UUID,Playback> PLAYING=new HashMap<>();
    private static final Map<UUID,Long> SEEN=new HashMap<>();
    private static Object world;
    private static boolean firstPerson;
    private static final Map<Integer,ModelPart> VIEW_ARMS=new HashMap<>();
    private AxeAnimationClient(){}
    public static boolean eligible(Player player){return player!=null && player.isAlive() && !player.isSpectator()
            && !player.isSleeping() && !player.isFallFlying() && !player.isUsingItem() && AxeAnimationItems.isModAxe(player.getMainHandItem());}
    public static Sample sample(Player player,float partial){
        if(!eligible(player)){if(player!=null){PLAYING.remove(player.getUUID());SEEN.put(player.getUUID(),player.getPersistentData().getLong(AxeAnimationNetwork.SEQUENCE));}return null;}
        if(world!=player.level()){PLAYING.clear();SEEN.clear();world=player.level();}
        String item=BuiltInRegistries.ITEM.getKey(player.getMainHandItem().getItem()).toString();
        var data=player.getPersistentData();long seq=data.getLong(AxeAnimationNetwork.SEQUENCE);var old=PLAYING.get(player.getUUID());
        if(seq>0 && SEEN.getOrDefault(player.getUUID(),0L)!=seq){
            SEEN.put(player.getUUID(),seq);int clip=data.getInt(AxeAnimationNetwork.CLIP);
            if(clip>=0 && clip<AxeAnimationData.Clip.values().length)old=new Playback(seq,AxeAnimationData.Clip.values()[clip],data.getLong(AxeAnimationNetwork.START),item,player.getInventory().selected);
            if(old!=null)PLAYING.put(player.getUUID(),old);
        }
        if(old!=null && (!old.item.equals(item) || old.slot!=player.getInventory().selected)){PLAYING.remove(player.getUUID());old=null;}
        if(old!=null){
            double time=(player.level().getGameTime()-old.start+partial)/20.0,length=AxeAnimationData.length(old.clip);
            if(time>=0 && time<length+.15)return new Sample(old.clip,time,time<=length?1:Math.max(0,1-(time-length)/.15));
        }
        boolean walking=player.walkAnimation.speed(partial)>.01;
        return new Sample(walking?AxeAnimationData.Clip.WALK:AxeAnimationData.Clip.IDLE,walking?player.walkAnimation.position(partial)/(2*Math.PI):0,1);
    }
    public static boolean authoredPose(Player player,float partial){
        var sample=sample(player,partial);
        return sample!=null && !(player.swinging && (sample.clip==AxeAnimationData.Clip.IDLE || sample.clip==AxeAnimationData.Clip.WALK));
    }
    private static boolean slim(Player player){return player instanceof AbstractClientPlayer client && client.getSkin().model()==net.minecraft.client.resources.PlayerSkin.Model.SLIM;}
    private static String bone(Player player,String name){
        if(player.getMainArm()!=HumanoidArm.LEFT)return name;
        return name.startsWith("left_")?name.replace("left_","right_"):name.startsWith("right_")?name.replace("right_","left_"):name;
    }
    public static void pose(Player player,PlayerModel<?> model,float partial){
        if(firstPerson)return;var sample=sample(player,partial);if(sample==null)return;
        // Block breaking keeps vanilla arm swings whenever no authored attack is playing.
        if(player.swinging && (sample.clip==AxeAnimationData.Clip.IDLE || sample.clip==AxeAnimationData.Clip.WALK))return;
        apply(player,model.head,"head",sample);apply(player,model.rightArm,"right_arm",sample);apply(player,model.leftArm,"left_arm",sample);
        apply(player,model.rightLeg,"right_leg",sample);apply(player,model.leftLeg,"left_leg",sample);
        model.hat.copyFrom(model.head);model.rightSleeve.copyFrom(model.rightArm);model.leftSleeve.copyFrom(model.leftArm);
        model.rightPants.copyFrom(model.rightLeg);model.leftPants.copyFrom(model.leftLeg);model.jacket.copyFrom(model.body);
    }
    private static void apply(Player player,ModelPart part,String name,Sample sample){
        String bone=bone(player,name);var value=AxeAnimationData.sample(sample.clip,bone,sample.seconds);float w=(float)sample.weight;
        float mirror=player.getMainArm()==HumanoidArm.LEFT?-1:1;
        boolean arm=name.endsWith("_arm");
        float x=(float)Math.toRadians(-value.rotation().x()*w),y=(float)Math.toRadians(-value.rotation().y()*mirror*w),z=(float)Math.toRadians(value.rotation().z()*mirror*w);
        if(arm){
            // The cube's rest rotation is independent of the parent bone (and held axe).
            // Source arm pivots are +/-4, whereas vanilla uses +/-5 with offset cubes.
            if(bone.equals("right_arm"))x-=(float)Math.toRadians(37.5);
            part.xRot=x;part.yRot=y;part.zRot=z;
            float side=name.equals("right_arm")?-1:1;
            var offset=new org.joml.Vector3f(side*(slim(player)?1.5F:1),0,0).rotate(new org.joml.Quaternionf().rotationZYX(z,y,x));
            part.x=side*4+offset.x;part.y=2+offset.y;part.z=offset.z;
        }else if(AxeAnimationData.has(sample.clip,bone,"rotation")){
            if(name.equals("head")){part.xRot+=x;part.yRot+=y;part.zRot+=z;}
            else{part.xRot=x;part.yRot=y;part.zRot=z;}
        }
        if(name.endsWith("_leg"))part.x=name.equals("right_leg")?-2:2;
        part.x-=value.position().x()*mirror*w;part.y-=value.position().y()*w;part.z+=value.position().z()*w;
    }
    public static void root(AbstractClientPlayer player,float partial,PoseStack pose){
        if(firstPerson)return;var sample=sample(player,partial);if(sample==null)return;
        var root=AxeAnimationData.sample(sample.clip,"player_model",sample.seconds);float mirror=player.getMainArm()==HumanoidArm.LEFT?-1:1,w=(float)sample.weight;
        float yaw=net.minecraft.util.Mth.rotLerp(partial,player.yBodyRotO,player.yBodyRot);
        pose.mulPose(Axis.YP.rotationDegrees(-yaw));pose.translate(-root.position().x()*mirror*w*.9375/16,root.position().y()*w*.9375/16,-root.position().z()*w*.9375/16);
        pose.translate(0,17.33333/16*.9375,0);rotate(pose,-root.rotation().x()*w,root.rotation().y()*mirror*w,-root.rotation().z()*mirror*w);pose.translate(0,-17.33333/16*.9375,0);
        pose.mulPose(Axis.YP.rotationDegrees(yaw));
    }
    private static void rotate(PoseStack pose,double x,double y,double z){pose.mulPose(Axis.ZP.rotationDegrees((float)z));pose.mulPose(Axis.YP.rotationDegrees((float)y));pose.mulPose(Axis.XP.rotationDegrees((float)x));}
    /** Called in arm-local model coordinates; imported axe is a child of the right arm. */
    public static void held(Player player,ItemStack stack,HumanoidArm arm,PoseStack pose,MultiBufferSource buffers,int light,float partial){
        heldTransform(player,stack,arm,pose,partial);
        Minecraft.getInstance().getItemRenderer().renderStatic(player,stack,ItemDisplayContext.NONE,arm==HumanoidArm.LEFT,pose,buffers,player.level(),light,OverlayTexture.NO_OVERLAY,player.getId());
    }
    /** Shared by rendering and the opt-in rig comparison against source-space transforms. */
    public static void heldTransform(Player player,ItemStack stack,HumanoidArm arm,PoseStack pose,float partial){
        var sample=sample(player,partial);if(sample==null)return;var axe=AxeAnimationData.sample(sample.clip,"axe",sample.seconds);
        double mirror=arm==HumanoidArm.LEFT?-1:1,w=sample.weight;
        // Undo only the cube offset/rest rotation to recover its animated parent bone.
        pose.translate(mirror*(slim(player)?1.5:1)/16,0,0);
        if(bone(player,arm==HumanoidArm.RIGHT?"right_arm":"left_arm").equals("right_arm"))pose.mulPose(Axis.XP.rotationDegrees(37.5F));
        pose.translate((-2-axe.position().x()*w)*mirror/16,(6.75-axe.position().y()*w)/16,(-5+axe.position().z()*w)/16);
        rotate(pose,90-axe.rotation().x()*w,(-37.5-axe.rotation().y()*w)*mirror,(-90+axe.rotation().z()*w)*mirror);
        pose.scale((float)-mirror,-1,1);
        // The animation author scaled the converted axe to 21/32 and gripped it at
        // original mesh Y=20.27142, not at its inventory/model origin Y=10.
        // Adapt the mesh, leaving all authored animation curves unchanged.
        if(Minecraft.getInstance().getItemRenderer().getModel(stack,player.level(),player,0).isGui3d()){
            pose.translate(0,0,-.0375/16);pose.scale(.65625F,.65625F,.65625F);
            double origin=BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath().startsWith("axe_of_the_void")?21:10;
            pose.translate(0,(origin-20.27142095238)/16,0);
        }
    }
    @SubscribeEvent(priority=net.neoforged.bus.api.EventPriority.LOWEST)
    public static void hand(RenderHandEvent event){
        var player=Minecraft.getInstance().player;if(event.isCanceled() || event.getHand()!=InteractionHand.MAIN_HAND || !eligible(player) || !authoredPose(player,event.getPartialTick())
                || Minecraft.getInstance().gameMode!=null && Minecraft.getInstance().gameMode.isDestroying())return;
        event.setCanceled(true);var renderer=(PlayerRenderer)Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(player);var model=renderer.getModel();
        var pose=event.getPoseStack();pose.pushPose();firstPerson=true;
        try {
            model.setupAnim(player,0,0,player.tickCount+event.getPartialTick(),0,0);
            firstPerson=false;pose(player,model,event.getPartialTick());firstPerson=true;
            pose.translate(0,-.12,0);pose.scale(-1,-1,1);
            var buffer=event.getMultiBufferSource();var skin=buffer.getBuffer(RenderType.entityTranslucent(player.getSkin().texture()));
            if(!player.isInvisible()){
                boolean slim=player.getSkin().model()==net.minecraft.client.resources.PlayerSkin.Model.SLIM;
                renderViewArm(model.rightArm,false,false,slim,pose,skin,event.getPackedLight());
                renderViewArm(model.rightSleeve,false,true,slim,pose,skin,event.getPackedLight());
                if(player.getOffhandItem().isEmpty()){
                    renderViewArm(model.leftArm,true,false,slim,pose,skin,event.getPackedLight());
                    renderViewArm(model.leftSleeve,true,true,slim,pose,skin,event.getPackedLight());
                }
            }
            pose.pushPose();(player.getMainArm()==HumanoidArm.RIGHT?model.rightArm:model.leftArm).translateAndRotate(pose);held(player,player.getMainHandItem(),player.getMainArm(),pose,buffer,event.getPackedLight(),event.getPartialTick());pose.popPose();
        }finally{firstPerson=false;pose.popPose();}
    }
    /** The body mesh's shoulder cap belongs inside the torso, never in the first-person view. */
    private static void renderViewArm(ModelPart source,boolean left,boolean sleeve,boolean slim,PoseStack pose,
                                      com.mojang.blaze3d.vertex.VertexConsumer buffer,int light){
        if(!source.visible)return;
        int key=(left?1:0)|(sleeve?2:0)|(slim?4:0);
        var arm=VIEW_ARMS.computeIfAbsent(key,ignored->{
            var faces=EnumSet.allOf(net.minecraft.core.Direction.class);faces.remove(net.minecraft.core.Direction.DOWN);
            int u=left?(sleeve?48:32):40,v=left?48:(sleeve?32:16);float grow=sleeve?.25F:0;
            var cube=new ModelPart.Cube(u,v,left?-1:(slim?-2:-3),-2,-2,slim?3:4,12,4,grow,grow,grow,false,64,64,faces);
            return new ModelPart(List.of(cube),Map.of());
        });
        arm.copyFrom(source);arm.render(pose,buffer,light,OverlayTexture.NO_OVERLAY);
    }
    private static void rig(org.joml.Matrix4f matrix,Sample sample,String bone,float x,float y,float z,float rx,float ry,float rz){
        var value=AxeAnimationData.sample(sample.clip,bone,sample.seconds);matrix.translate((float)value.position().x(),(float)value.position().y(),(float)value.position().z());
        matrix.translate(x,y,z).rotateZ((float)Math.toRadians(rz+value.rotation().z())).rotateY((float)Math.toRadians(ry+value.rotation().y())).rotateX((float)Math.toRadians(rx+value.rotation().x())).translate(-x,-y,-z);
    }
    private static void emit(AbstractClientPlayer player,Sample sample){
        var matrix=new org.joml.Matrix4f();rig(matrix,sample,"player_model",0,17.33333F,1,0,0,0);rig(matrix,sample,"right_arm",4,22,1,0,0,0);
        rig(matrix,sample,"axe",6,15.25F,-4,-90,37.5F,-90);rig(matrix,sample,"tornado_emitter",6,30.6F,-4.05F,0,0,0);
        double yaw=Math.toRadians(player.yBodyRot),mirror=player.getMainArm()==HumanoidArm.LEFT?-1:1;
        for(float z:new float[]{-13.1F,4.9F}){
            var local=matrix.transformPosition(new org.joml.Vector3f(6,30.6F,z));double x=-local.x*.9375/16*mirror,dz=-(local.z-1)*.9375/16;
            double wx=x*Math.cos(yaw)-dz*Math.sin(yaw),wz=x*Math.sin(yaw)+dz*Math.cos(yaw);
            // Authored disc/upward acceleration/lifetime; bound the original 2,000/s emitter for multiplayer.
            for(int n=0;n<4;n++) {
                double angle=player.getRandom().nextDouble()*Math.PI*2;
                player.level().addParticle(net.bananashelp20.forgermod.particle.AxeCastParticles.SWIRL.get(),player.getX()+wx+Math.cos(angle)*5,player.getY()+local.y*.9375/16+.5,player.getZ()+wz+Math.sin(angle)*5,0,player.getRandom().nextDouble()*5,0);
            }
        }
    }
    @SubscribeEvent public static void tick(ClientTickEvent.Post event){
        var level=Minecraft.getInstance().level;if(level==null){PLAYING.clear();SEEN.clear();world=null;return;}
        var ids=new HashSet<UUID>();for(var player:level.players()){
            ids.add(player.getUUID());var sample=sample(player,0);
            if(sample!=null && sample.clip==AxeAnimationData.Clip.GRAVITY_CAST && sample.seconds>=1.75 && sample.seconds<=6)emit(player,sample);
        }PLAYING.keySet().retainAll(ids);SEEN.keySet().retainAll(ids);
    }
}
