package net.bananashelp20.forgermod.client;

import com.google.gson.Gson;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.block.custom.WeaponAssemblerBlock;
import net.bananashelp20.forgermod.block.entity.ModBlockEntities;
import net.bananashelp20.forgermod.block.entity.custom.WeaponAssemblerBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.model.data.ModelData;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Cached meshes on continuous joint transforms; no entities or per-frame mesh generation. */
@EventBusSubscriber(modid=ForgerMod.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class WeaponAssemblerRenderer implements BlockEntityRenderer<WeaponAssemblerBlockEntity> {
    private record Joint(float[] base,float[] elbow,float[] wrist,float[][] jaws){}
    private static final List<String> ARMS=List.of("lamp","glue","hammer","claw");
    private static final Map<String,ModelResourceLocation> MODELS=new LinkedHashMap<>();
    static{
        for(String arm:ARMS)for(String section:List.of("lower","upper","tool"))addModel(arm+"_"+section);
        addModel("claw_jaw0");addModel("claw_jaw1");
    }
    private static void addModel(String name){MODELS.put(name,ModelResourceLocation.standalone(
            ResourceLocation.fromNamespaceAndPath(ForgerMod.MOD_ID,"block/weapon_assembler/"+name)));}
    private final Map<String,Joint> rig;
    public WeaponAssemblerRenderer(BlockEntityRendererProvider.Context context){
        var location=ResourceLocation.fromNamespaceAndPath(ForgerMod.MOD_ID,"rendering/weapon_assembler.json");
        try(var stream=Minecraft.getInstance().getResourceManager().getResource(location).orElseThrow().open();
            var reader=new InputStreamReader(stream,StandardCharsets.UTF_8)){
            var type=new com.google.gson.reflect.TypeToken<Map<String,Joint>>(){}.getType();
            rig=new Gson().fromJson(reader,type);
            for(String arm:ARMS){
                Joint joint=Objects.requireNonNull(rig.get(arm),arm);
                for(float[] point:new float[][]{joint.base,joint.elbow,joint.wrist})
                    if(point==null || point.length!=3 || !Float.isFinite(point[0]+point[1]+point[2]))throw new IllegalArgumentException("Invalid joint: "+arm);
            }
        }catch(java.io.IOException exception){throw new IllegalStateException("Cannot load assembler joint anchors",exception);}
    }
    @SubscribeEvent public static void models(ModelEvent.RegisterAdditional event){MODELS.values().forEach(event::register);}
    @SubscribeEvent public static void renderer(EntityRenderersEvent.RegisterRenderers event){
        event.registerBlockEntityRenderer(ModBlockEntities.WEAPON_ASSEMBLER_BE.get(),WeaponAssemblerRenderer::new);
    }
    @Override public int getViewDistance(){return 64;}
    @Override public AABB getRenderBoundingBox(WeaponAssemblerBlockEntity table){return new AABB(table.getBlockPos()).inflate(.7,0,.7).expandTowards(0,1,0);}
    private static void joint(PoseStack poses,float[] pivot,Axis axis,float degrees){
        poses.translate(pivot[0],pivot[1],pivot[2]);poses.mulPose(axis.rotationDegrees(degrees));poses.translate(-pivot[0],-pivot[1],-pivot[2]);
    }
    private static void draw(WeaponAssemblerBlockEntity table,String name,PoseStack poses,MultiBufferSource buffers,int light,int overlay){
        var client=Minecraft.getInstance();var type=RenderType.cutout();
        client.getBlockRenderer().getModelRenderer().renderModel(poses.last(),buffers.getBuffer(type),table.getBlockState(),
                client.getModelManager().getModel(MODELS.get(name)),1,1,1,light,overlay,ModelData.EMPTY,type);
    }
    @Override public void render(WeaponAssemblerBlockEntity table,float partial,PoseStack poses,MultiBufferSource buffers,int light,int overlay){
        if(table.getLevel()==null)return;
        float activity=table.visualActivity(partial);activity=activity*activity*(3-2*activity);
        double time=table.animationTime(partial),cycle=time*Math.PI*2/48;
        float strike=(float)Math.pow(Math.max(0,Math.cos(cycle)),6);
        float sweep=(float)Math.sin(cycle*.5),grip=(float)Math.sin(cycle*.5+1.2);
        poses.pushPose();
        poses.translate(.5,0,.5);poses.mulPose(Axis.YP.rotationDegrees(180-table.getBlockState().getValue(WeaponAssemblerBlock.FACING).toYRot()));poses.translate(-.5,0,-.5);
        for(String arm:ARMS){
            Joint anchor=rig.get(arm);Axis axis=arm.equals("lamp") || arm.equals("glue")?Axis.ZP:Axis.XP;
            float lower,upper,wrist;
            switch(arm){
                case "lamp"->{lower=-12+2*sweep;upper=-34+4*sweep;wrist=24-4*sweep;}
                case "glue"->{lower=15+3*sweep;upper=28+7*sweep;wrist=-18-4*sweep;}
                case "hammer"->{lower=-8-8*strike;upper=-22-32*strike;wrist=10+12*strike;}
                default->{lower=15+2*grip;upper=28+3*grip;wrist=-14-2*grip;}
            }
            poses.pushPose();
            joint(poses,anchor.base,axis,lower*activity);draw(table,arm+"_lower",poses,buffers,light,overlay);
            joint(poses,anchor.elbow,axis,upper*activity);draw(table,arm+"_upper",poses,buffers,light,overlay);
            joint(poses,anchor.wrist,axis,wrist*activity);draw(table,arm+"_tool",poses,buffers,light,overlay);
            if(arm.equals("claw"))for(int jaw=0;jaw<2;jaw++){
                poses.pushPose();joint(poses,anchor.jaws[jaw],Axis.XP,(jaw==0?-1:1)*(8+5*grip)*activity);
                draw(table,"claw_jaw"+jaw,poses,buffers,light,overlay);poses.popPose();
            }
            poses.popPose();
        }
        poses.popPose();
    }
}
