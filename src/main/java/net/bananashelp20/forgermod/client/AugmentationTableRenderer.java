package net.bananashelp20.forgermod.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.bananashelp20.forgermod.ForgerMod;
import net.bananashelp20.forgermod.block.entity.ModBlockEntities;
import net.bananashelp20.forgermod.block.entity.custom.AugmentationTableBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.*;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.phys.AABB;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.model.data.ModelData;

/** Cached baked assemblies: animation changes only transforms, never builds meshes or spawns entities. */
@EventBusSubscriber(modid=ForgerMod.MOD_ID,bus=EventBusSubscriber.Bus.MOD,value=Dist.CLIENT)
public final class AugmentationTableRenderer implements BlockEntityRenderer<AugmentationTableBlockEntity> {
    private static ModelResourceLocation model(String part){return ModelResourceLocation.standalone(
            ResourceLocation.fromNamespaceAndPath(ForgerMod.MOD_ID,"block/augmentation_table_"+part));}
    private static final ModelResourceLocation RING=model("ring"),RUNES=model("runes");
    private final net.minecraft.world.item.ItemStack sapphire;
    public AugmentationTableRenderer(BlockEntityRendererProvider.Context context){
        sapphire=new net.minecraft.world.item.ItemStack(net.bananashelp20.forgermod.item.ModItems.SAPPHIRE_GEMSTONE.get());
    }
    @SubscribeEvent public static void registerModels(ModelEvent.RegisterAdditional event){
        event.register(RING);event.register(RUNES);
    }
    @SubscribeEvent public static void registerRenderer(EntityRenderersEvent.RegisterRenderers event){
        event.registerBlockEntityRenderer(ModBlockEntities.AUGMENTATION_TABLE_BE.get(),AugmentationTableRenderer::new);
    }
    @Override public int getViewDistance(){return 32;}
    @Override public AABB getRenderBoundingBox(AugmentationTableBlockEntity table){return new AABB(table.getBlockPos()).inflate(.25,0,.25).expandTowards(0,1.125,0);}
    private static void draw(AugmentationTableBlockEntity table,ModelResourceLocation part,PoseStack poses,
            MultiBufferSource buffers,int light,int overlay,float red,float green,float blue){
        var client=Minecraft.getInstance();
        client.getBlockRenderer().getModelRenderer().renderModel(poses.last(),buffers.getBuffer(RenderType.solid()),
                table.getBlockState(),client.getModelManager().getModel(part),red,green,blue,light,overlay,ModelData.EMPTY,RenderType.solid());
    }
    @Override public void render(AugmentationTableBlockEntity table,float partial,PoseStack poses,MultiBufferSource buffers,int light,int overlay){
        if(table.getLevel()==null)return;
        // The floating assembly retracts under an obstructing block rather than drawing through ceilings.
        if(!table.getLevel().getBlockState(table.getBlockPos().above()).getCollisionShape(table.getLevel(),table.getBlockPos().above()).isEmpty())return;
        float spin=table.visualSpin(partial),working=table.visualActivity(partial);
        float lift=working*working*(3-2*working);
        double time=table.getLevel().getGameTime()+partial+(table.getBlockPos().asLong()&255);
        float pulse=.86F+.14F*Mth.sin((float)(time*.1));
        for(int ring=0;ring<2;ring++){
            poses.pushPose();
            poses.translate(.5,Mth.lerp(lift,1F,1.22F+ring*.26F)+Math.sin(time*.045+ring*Math.PI)*(.025+.025*working)*lift,.5);
            poses.mulPose(Axis.YP.rotationDegrees(ring==0?spin:-spin*.85F));
            poses.mulPose(Axis.XP.rotationDegrees(((ring==0?17:-23)+Mth.sin((float)(time*.025))*5)*lift));
            if(ring==1)poses.scale(.85F,.85F,.85F);
            poses.translate(-.5,0,-.5);
            draw(table,RING,poses,buffers,light,overlay,1,1,1);
            draw(table,RUNES,poses,buffers,LightTexture.FULL_BRIGHT,overlay,.45F*pulse,.8F*pulse,pulse);
            poses.popPose();
        }
        var items=Minecraft.getInstance().getItemRenderer();
        var ground=items.getModel(sapphire,table.getLevel(),null,0).getTransforms().getTransform(ItemDisplayContext.GROUND);
        float gemSize=Mth.lerp(lift,.5F,1F);
        // A generated item's depth is one texel. Its lower face rests on the engraving at idle.
        float idleGemY=15.0625F/16+.5F*ground.scale.z()/32;
        poses.pushPose();
        poses.translate(.5,Mth.lerp(lift,idleGemY,1.75F)+Math.sin(time*.1)*.1*lift,Mth.lerp(lift,.63F,.5F));
        poses.mulPose(Axis.YP.rotationDegrees(-spin*.2F));
        poses.mulPose(Axis.XP.rotationDegrees(90*(1-lift)));
        poses.scale(gemSize,gemSize,gemSize);
        // Centre the real GROUND model before tilting it flat/upright, preserving its normal item scale.
        poses.translate(-ground.translation.x(),-ground.translation.y(),-ground.translation.z());
        items.renderStatic(sapphire,ItemDisplayContext.GROUND,LightTexture.FULL_BRIGHT,overlay,poses,buffers,table.getLevel(),table.getBlockPos().hashCode());
        poses.popPose();
        var item=table.displayItem();
        if(!item.isEmpty()){
            // Move the workpiece under the crystal only once the rising cap is clear.
            float itemLift=Mth.clamp((lift-.35F)/.65F,0,1);itemLift=itemLift*itemLift*(3-2*itemLift);
            poses.pushPose();poses.translate(.5,Mth.lerp(itemLift,1.10F,1.06F)+Math.sin(time*.055)*.025,Mth.lerp(itemLift,.2F,.5F));
            poses.mulPose(Axis.YP.rotationDegrees(spin*.2F));poses.mulPose(Axis.XP.rotationDegrees(90));float size=Mth.lerp(itemLift,.22F,.62F);poses.scale(size,size,size);
            Minecraft.getInstance().getItemRenderer().renderStatic(item,ItemDisplayContext.FIXED,light,overlay,poses,buffers,table.getLevel(),table.getBlockPos().hashCode());
            poses.popPose();
        }
    }
}
